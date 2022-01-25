/*
 *   Copyright (c) 2017.  Jefferson Lab (JLab). All rights reserved. Permission
 *   to use, copy, modify, and distribute  this software and its documentation for
 *   governmental use, educational, research, and not-for-profit purposes, without
 *   fee and without a signed licensing agreement.
 *
 *   IN NO EVENT SHALL JLAB BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL
 *   INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
 *   OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF JLAB HAS
 *   BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   JLAB SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE. THE CLARA SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY,
 *   PROVIDED HEREUNDER IS PROVIDED "AS IS". JLAB HAS NO OBLIGATION TO PROVIDE
 *   MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 *   This software was developed under the United States Government license.
 *   For more information contact author at gurjyan@jlab.org
 *   Department of Experimental Nuclear Physics, Jefferson Lab.
 */

package org.jlab.coda.afecs.supervisor.thread;

import org.jlab.coda.afecs.codarc.CodaRCAgent;
import org.jlab.coda.afecs.cool.ontology.AProcess;
import org.jlab.coda.afecs.cool.parser.ACondition;
import org.jlab.coda.afecs.cool.parser.AStatement;
import org.jlab.coda.afecs.supervisor.SupervisorAgent;
import org.jlab.coda.afecs.system.AConstants;

import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.util.*;


/**
 * <p>
 * This is the core class that does COOL service
 * execution in a separate thread.
 * <ul>
 * <li>
 * Runs a script attached to a state.
 * N.B.
 * a) supports only shell script execution
 * b) uses script timeout to define sync/a-sync
 * </li>
 * <li>
 * Creates run-log file after
 * a) prestart,
 * b) ending and
 * c) ended
 * states of the supervisor agent.
 * </li>
 * <li>
 * Communicates with the platform administrative
 * services at the prestart, asking to increment
 * run-number and store it in the platform local
 * database.
 * </li>
 * <li>
 * Does actual state machine logic execution, i.e.
 * COOL conditional statement execution.
 * </li>
 * <li>
 * Checks the validity of the COOL statements.
 * Supports service execution for a single agent,
 * or agents grouped by "type" and "priority".
 * </li>
 * <li>
 * Does ascending and descending priority execution
 * </li>
 * <li>
 * Organizes run auto-start
 * </li>
 * </ul>
 * </p>
 *
 * @author gurjyan
 * Date: 11/13/14 Time: 2:51 PM
 * @version 4.x
 */
public class ServiceExecutionT implements Runnable {
//public class ServiceExecutionT extends Thread {

    private String serviceName;
    private String stateName;
    private boolean scope;
    private static SupervisorAgent owner;

    public ServiceExecutionT(SupervisorAgent owner, String name) {
        ServiceExecutionT.owner = owner;
        serviceName = name;
        stateName = owner.myServices.get(serviceName).getStateMachineRule().getName();
        scope = false;
    }

    public void run() {
//        super.run();
        System.out.println("DDDUUUDDDUUUDDD "+ stateName);

        owner.isResetting.set(false);
        boolean failed = false;

        // See if required service is registered with this agent
        if (owner.myServiceConditions.containsKey(serviceName)) {

            if (!owner.myComponents.isEmpty() &&
                    (owner.sUtility.containsState(AConstants.disconnected) ||
                            owner.sUtility.containsState(AConstants.error) ||
                            owner.sUtility.containsState(AConstants.ERROR))) {
                owner.send(AConstants.GUI,
                        owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                        owner.me.getRunTimeDataAsPayload());
                owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                        owner.myName,
                        11,
                        AConstants.ERROR,
                        " " + serviceName + " service failed.");
                owner.dalogMsg(owner.myName,
                        11,
                        AConstants.ERROR,
                        " " + serviceName + " service failed.");
                return;
            }

            owner.activeServiceExec.set(true);

            // Define the state of the supervisor based on a required service/rule
            // Set state activating in case service request = active
            if (stateName.equals("active")) {
                owner.me.setState("activating");


                owner.send(AConstants.GUI,
                        owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                        owner.me.getRunTimeDataAsPayload());

                // Otherwise add ing to the service request name
            } else {
                owner.me.setState(stateName + "ing");
                owner.send(AConstants.GUI,
                        owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                        owner.me.getRunTimeDataAsPayload());

            }

            // Now check to see if the service request
            // is CodaRcPrestart or CodaRcStartRun
            if (serviceName.equals("CodaRcPrestart") ||
                    serviceName.equals("CodaRcStartRun")) {

                // StartRun is not supported if we have scheduler set
                if (serviceName.equals("CodaRcStartRun") &&
                        owner._numberOfRuns <= 0) {
                    owner.autoStart.set(false);
                    owner.me.setAutoStart(false);
                }

                // Ask platform to increment run number,
                // and update platform xml file
                int rn = owner.myContainer.myPlatform.platformRegistrationRequestIncrementRunNum(
                        owner.me.getSession(),
                        owner.me.getRunType());

                if (rn > 0) {
                    owner.me.setPrevious_runNumber(owner.me.getRunNumber());
                    owner.me.setRunNumber(rn);

                    ArrayList<cMsgPayloadItem> nl = new ArrayList<>();
                    try {
                        nl.add(new cMsgPayloadItem(AConstants.RUNNUMBER, rn));
                        nl.add(new cMsgPayloadItem("ForAgentOnly", AConstants.seton));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Send all gui's to update their run-numbers
                    owner.send(owner.me.getSession() + "/" + owner.me.getRunType(),
                            AConstants.UIControlRequestSetRunNumber,
                            nl);

                    try {

                        // Ask all components to set their run-numbers.
                        for (CodaRCAgent cn : owner.myComponents.values()) {
                            cn.me.setRunNumber(rn);
                            cn.runControlSetRunNumber(rn);
                        }
                    } catch (cMsgException e) {
                        e.printStackTrace();
                    }

                    // Platform request to increment
                    // run-number failed. Fail the transition.
                } else {
                    owner.send(AConstants.GUI,
                            owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                            owner.me.getRunTimeDataAsPayload());
                    owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                            owner.myName,
                            11,
                            AConstants.ERROR,
                            " " + serviceName + " service failed. Platform run-number increment failed.");
                    owner.dalogMsg(owner.myName,
                            11,
                            AConstants.ERROR,
                            " " + serviceName + " service failed. Platform run-number increment failed.");
                    return;
                }


                // For configure, download as well as go service requests we
                // p2p ask platform to report the run-number. Block and wait
                // until platform reads the run number from the controlSessions.xml
                // file which than will be reported to all gui's
            } else if (serviceName.equals("CodaRcConfigure") ||
                    serviceName.equals("CodaRcDownload") ||
                    serviceName.equals("CodaRcGo")) {

                owner.me.setRunNumber(owner.myContainer.myPlatform.platformRegistrationRequestReportRunNum(owner.me.getSession()));
                ArrayList<cMsgPayloadItem> nl = new ArrayList<>();
                try {
                    nl.add(new cMsgPayloadItem(AConstants.RUNNUMBER, owner.me.getRunNumber()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Inform GUIs the received run-number
                owner.send(owner.me.getSession() + "/" + owner.me.getRunType(),
                        AConstants.UIControlRequestSetRunNumber,
                        nl);
                if (serviceName.equals("CodaRcGo")) {
                    // inform all agents about the beginning of the end transition
                    for (CodaRCAgent c : owner.myComponents.values()) {
                        try {
                            c.sessionControlPreGo(owner.getPlEXPID(),
                                    owner.mySession,
                                    owner.myRunType,
                                    owner.me.getRunNumber());
                        } catch (cMsgException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else if (serviceName.equals("CodaRcEnd")) {
                // inform all agents about the beginning of the end transition
                for (CodaRCAgent c : owner.myComponents.values()) {
                    try {
                        c.sessionControlPreEnd(owner.getPlEXPID(),
                                owner.mySession,
                                owner.myRunType,
                                owner.me.getRunNumber());
                    } catch (cMsgException e) {
                        e.printStackTrace();
                    }
                }
            }

            // See if we have processes scheduled to be executed
            // before the state transition. Note that supervisor agent supports
            // scripts/processes at only pre and post state transitions.
            execProcessBeforeTransition();

            boolean execute = false;

            // Here goes actual state machine logic
            // (conditional statements of the service)
            // execution
            for (ACondition cond : owner.myServiceConditions.get(serviceName)) {

                // For every conditional statement in the service

                // This for loop checks the validity of the COOL description.
                // COOL supports service execution for agents grouped
                // by "type" and by "priority" only.
                for (AStatement st : cond.getConditionalStatements()) {

                    // See if COOL config has set the type group
                    // requirement, i.e. for e.g. type_EMU
                    if (st.getLeft().contains("type")) {
                        ArrayList<String> n =
                                owner.coolServiceAnalyser.getAgentNamesByCoolType(st.getLeft());

                        // Execute if the type is known to the supervisor,
                        // i.e. there are agents with this type configured.
                        if (n != null && !n.isEmpty()) execute = true;

                        // See if COOL config has priority requirement
                    } else if (st.getLeft().contains("priority")) {
                        execute = true;
                    } else {

                        // If this is a single component request see if the agent for
                        // that component is registered with this supervisor.
                        if (owner.myComponents.containsKey(st.getLeft())) execute = true;
                    }
                }

                // COOL supports only if and while operators
                if (cond.getKeyWord().equals("if") ||
                        cond.getKeyWord().equals("while")) {
                    scope = true;
                }
            }

            // Main execution
            if (execute && scope) {

                // create component priority list
                TreeSet<Integer> _priorities = new TreeSet<>();
                for (CodaRCAgent c : owner.getMyComponents().values()) {
                    _priorities.add(c.me.getPriority());
                }

                long startTime = AfecsTool.getCurrentTimeInMs();
                for (ACondition cond : owner.myServiceConditions.get(serviceName)) {
                    // Priority based state machine execution
                    AStatement stm = cond.getConditionalStatements().get(0);

                    // Ascending priority
                    switch (stm.getLeft()) {
                        case "priority++":

                            // Start sequencer through all priorities
                            for (Integer p : _priorities) {
                                // Check to see if we have COOL configured
                                // component with that priority
                                ArrayList<String> tmp_a =
                                        owner.coolServiceAnalyser.getAgentNamesByPriority(p);
                                failed = executePriority(p, tmp_a, cond);
                                if (failed) break;
                            }

                            // Restore the original state machine description,
                            // in order to avoid configuring again.
                            for (AStatement cs : cond.getConditionalStatements()) {
                                cs.setLeft("priority++");
                            }
                            for (AStatement stmt : cond.getActionStatements()) {
                                stmt.setLeft("priority");
                            }
                            break;

                        // Descending priority
                        case "priority--":
                            Iterator it = _priorities.descendingIterator();
                            while (it.hasNext()) {
                                int p = (Integer) it.next();
                                // Check to see if we have registered
                                // component with that priority
                                ArrayList<String> tmp_a =
                                        owner.coolServiceAnalyser.getAgentNamesByPriority(p);
                                failed = executePriority(p, tmp_a, cond);
                                if (failed) break;
                            }


                            // Restore the original state machine description,
                            // in order to avoid configuring again.
                            for (AStatement cs : cond.getConditionalStatements()) {
                                cs.setLeft("priority--");
                            }
                            // Now replace all priority strings with the priority
                            // number in the action statements of the condition
                            for (AStatement stmt : cond.getActionStatements()) {
                                stmt.setLeft("priority");
                            }
                            break;

                        default:

                            // Actual execution for a single agent
                            int r = owner.coolServiceAnalyser.executeCondition(cond);
                            failed = waitForServiceExecution(r, cond);
                            break;
                    }
                    if (failed) break;
                }

                long endTime = AfecsTool.getCurrentTimeInMs();
                System.out.println("DDD -----| Info: Supervisor " + owner.me.getName() + " completed COOL service = " +
                        serviceName + " in " + (endTime - startTime) + " msec.");

                // Service executed. Analyse the result
                if (failed) {
                    owner.me.setState(AConstants.failed);
                    owner.send(AConstants.GUI,
                            owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                            owner.me.getRunTimeDataAsPayload());
                    owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                            owner.myName,
                            11,
                            AConstants.ERROR,
                            " " + serviceName + " service failed.");
                    owner.dalogMsg(owner.myName,
                            11,
                            AConstants.ERROR,
                            " " + serviceName + " service failed.");
                } else {
                    if (stateName.equals("active")) {

                        // See if we have processes scheduled to be executed
                        // after the state active.
                        execProcessAfterTransition();

                        owner.me.setState(stateName);
                        owner.me.setRunStartTime(owner.startEndFormatter.format(new Date()));
                        owner.me.setRunEndTime("0");
                        owner.me.setDestinationNames(owner.defineDestinationNames());
                        owner.send(AConstants.GUI,
                                owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                                owner.me.getState(),
                                owner.me.getRunTimeDataAsPayload());


                        // Set run-end time limit if required
                        if (owner.absTimeLimit > 0 && owner.me.getState().equals(AConstants.active)) {
                            owner.timeLimit = System.currentTimeMillis() + owner.absTimeLimit;
                        }

                    } else {

                        // update run-end log
                        if (stateName.equals("end")) {
                            owner.me.setRunEndTime(owner.startEndFormatter.format(new Date()));
                            String xmlLog = owner.createRunLogXML(true);
                            owner.myPlatform.registrar.createRunLog(xmlLog, owner.mySession, false);
                        }

                        // See if we have processes scheduled to be executed
                        // after the state transition.
                        // Note that all components are already transitioned and before
                        // setting the state of the supervisor to the state of the control
                        // we check attached processes after a transition.
                        // This is a trick to  implement sync processes attached
                        // to after transition of the supervisor.
                        // So, for e.g. if the user specifies sync process attached
                        // to the end-after transition, then run control will not
                        // allow starting a new run before this script is done.

                        execProcessAfterTransition();

                        owner.me.setState(stateName + "ed");

                        owner.send(AConstants.GUI,
                                owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                                owner.me.getRunTimeDataAsPayload());

                        if (owner.me.getState().equals(AConstants.prestarted)) {

                            // We are just prestarted the run send a message to the platform
                            // registrar asking to copy last run run_log (i.e. current_run_log) file to previous_run_log.
                            // note: this is in case previous run was not ended properly.
                            String xmlLog = owner.createRunLogXML(false);
                            owner.myPlatform.registrar.createRunLog(xmlLog, owner.mySession, true);

                        }
                        if (owner.me.getState().equals(AConstants.ended)) {
                            owner.me.setRunEndTime(owner.startEndFormatter.format(new Date()));
                            String xmlLog = owner.createRunLogXML(true);
                            owner.myPlatform.registrar.createRunLog(xmlLog, owner.mySession, false);

                            owner.send(AConstants.GUI,
                                    owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                                    owner.me.getRunTimeDataAsPayload());


                            // stop periodic processes
                            owner.stopPeriodicProcesses();

                            // See if we need to auto start the run
                            if (owner.autoStart.get()) {
                                if (owner._numberOfRuns > 0) {
                                    // wait 10 sec to start a new run
                                    for (int i = 0; i < 10; i++) {
                                        if (!owner.autoStart.get()) {
                                            i = 11;
                                        }
                                        AfecsTool.sleep(1000);
                                    }


                                    // If during that 10 sec. user did not
                                    // reset auto start, then do auto start
                                    if (owner.autoStart.get()) {
                                        owner._numberOfRuns = owner._numberOfRuns - 1;
                                        owner.me.setnScheduledRuns(owner._numberOfRuns);

                                        if (owner.isRunEnded()) {
                                            owner.codaRC_startRun();
                                        }
                                    }

                                } else {
                                    owner.autoStart.set(false);
                                    owner.me.setAutoStart(false);
                                    owner.send(AConstants.GUI,
                                            owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                                            owner.me.getRunTimeDataAsPayload());
                                }
                            }
                        }
                    }

                    String codaState = owner.coolServiceAnalyser.decodeCodaSMServiceName(serviceName);
                    owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                            owner.myName,
                            1,
                            AConstants.INFO,
                            " " + codaState + " succeeded.");
                    owner.dalogMsg(owner.myName,
                            1,
                            AConstants.INFO,
                            " " + codaState + " succeeded.");
                }
            }
        }
        owner.activeServiceExec.set(false);
    }

    /**
     * Executes a condition for all components with a given priority
     *
     * @param priority  under consideration
     * @param compNames names of components with the same priority
     * @param cond      COOL condition to be executed
     * @return boolean
     */
    private boolean executePriority(int priority, ArrayList<String> compNames, ACondition cond) {
        boolean failed = false;
        if (compNames != null && compNames.size() > 0) {

            // If yes replace all priority++ strings with
            // priority number in conditional statement
            for (AStatement cs : cond.getConditionalStatements()) {
                cs.setLeft(Integer.toString(priority));
            }

            // Now replace all priority strings with the priority
            // number in the action statements of the condition
            for (AStatement stmt : cond.getActionStatements()) {
                stmt.setLeft(Integer.toString(priority));
            }

            // Actual execution
            int r = owner.coolServiceAnalyser.executeCondition(cond);
            failed = waitForServiceExecution(r, cond);
        }
        return failed;
    }

    private boolean waitForServiceExecution(int r, ACondition cond) {
        int tout = 0;
        boolean failed = false;
        List<String> error_states = new ArrayList<>();
        error_states.add(AConstants.disconnected);
        error_states.add(AConstants.failed);
        error_states.add(AConstants.ERROR);
        error_states.add(AConstants.booted);
        boolean errorThrown;
        boolean stateAchieved;


        if (r >= 0) {
            scope = false;
            do {
                AfecsTool.sleep(100);
                tout++;
                if (tout % 50 == 0) {
                    //warning every 5 sec.
                    if (owner.coolServiceAnalyser.getNotTransitioned(cond).length() != 0) {
                        owner.reportAlarmMsg(owner.me.getSession() + "/" +
                                        owner.me.getRunType(),
                                owner.myName,
                                5,
                                AConstants.WARN,
                                " waiting for... " + owner.coolServiceAnalyser.
                                        getNotTransitioned(cond).toString());
                        owner.dalogMsg(owner.myName,
                                5,
                                AConstants.WARN,
                                " waiting for... " + owner.coolServiceAnalyser.
                                        getNotTransitioned(cond).toString());
                    }
                }

                errorThrown = owner.sUtility.containsStates(error_states);
                stateAchieved = owner.coolServiceAnalyser.checkStatesDescribed(cond);

            }
            while (!errorThrown &&
                    !stateAchieved &&
                    !owner.isResetting.get()
            );
            if (errorThrown || !stateAchieved) {
                failed = true;
            }

//            while (!errorThrown &&
//                    !stateAchieved &&
//                    !owner.isResetting.get() &&
//                    !owner.me.getState().equals(AConstants.booted)
//            );
//
//            if(!owner.isResetting.get()) {
//                failed = true;
//            }

        }
        return failed;
    }


    private void execProcessBeforeTransition() {

        for (AProcess bp : owner.me.getProcesses()) {
            if (bp != null) {
                if (bp.getBefore() != null && !bp.getBefore().equals(AConstants.udf)) {
                    String pu = bp.getBefore().toUpperCase();
                    String su = serviceName.toUpperCase();
                    String pr;

                    if (pu.contains("ED")) {
                        pr = pu.substring(0, pu.lastIndexOf("ED"));
                    } else if (pu.equals("ACTIVE")) {
                        pr = "GO";
                    } else {
                        pr = pu;
                    }
                    if (su.contains(pr) ||
                            (serviceName.equals("CodaRcStartRun") && bp.getBefore().equals(AConstants.prestarted))) {
                        owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                                owner.myName,
                                1,
                                AConstants.INFO,
                                " Starting process = " + bp.getName());
                        // execute scripts before the state transition
                        owner.pm.executeProcess(bp, owner.myPlugin, owner.me);
                        owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                                owner.myName,
                                1,
                                AConstants.INFO,
                                " Done process = " + bp.getName());
                    }
                }
            }
        }
    }

    private void execProcessAfterTransition() {
        //  Note that agent supports
        // scripts/processes at only pre and post state transitions.
        for (AProcess bp : owner.me.getProcesses()) {
            if (bp != null) {
                if (bp.getAfter() != null && !bp.getAfter().equals(AConstants.udf)) {
                    String pu = bp.getAfter().toUpperCase();
                    String su = serviceName.toUpperCase();
                    String pr;
                    if (pu.contains("ED")) {
                        pr = pu.substring(0, pu.lastIndexOf("ED"));
                    } else if (pu.equals("ACTIVE")) {
                        pr = "GO";
                    } else {
                        pr = pu;
                    }
                    if (su.contains(pr) ||
                            (serviceName.equals("CodaRcStartRun") && bp.getAfter().equals(AConstants.active))) {
                        owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                                owner.myName,
                                1,
                                AConstants.INFO,
                                " Starting process = " + bp.getName());

                        // execute scripts before the state transition
                        owner.pm.executeProcess(bp, owner.myPlugin, owner.me);
                        owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                                owner.myName,
                                1,
                                AConstants.INFO,
                                " Done process = " + bp.getName());
                    }
                }
            }
        }
    }

}