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

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.supervisor.SupervisorAgent;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConstants;

import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This is a thread that periodically reports
 * supervised agent information to GUIs
 * </p>
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */
public class AStatusReportT extends Thread {

    private SupervisorAgent owner;

    private volatile boolean isRunning = true;

    private static final int INFLUX_INJECT_DELAY = 5;
    private static final int RUNLOG_CREATE_DELAY = 10;

    public AStatusReportT(SupervisorAgent owner) {
        this.owner = owner;

    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void run() {
        super.run();
        int delay = RUNLOG_CREATE_DELAY;
        int influx_delay = INFLUX_INJECT_DELAY;
        List<String> states = new ArrayList<>();
        states.add(AConstants.ERROR);
        states.add(AConstants.error);
        states.add(AConstants.booted);
        states.add(AConstants.disconnected);
        states.add(AConstants.udf);

        while (isRunning) {
            owner.send(AConstants.GUI,
                    owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                    owner.me.getRunTimeDataAsPayload());

            // aggregate ER class component parameters
            if (owner.sortedComponentList.containsKey("ER_class")) {
                long eventNumber = 0L;
                float eventRate = 0f;
                float eventRateAverage = 0f;
                double dataRate = 0d;
                double dataRateAverage = 0d;
                long numberOfLongs = 0;
                int maxEventSize = 0;
                int minEventSize = 0;
                int avgEventSize = 0;
                String[] destinationNameArray = new String[0];

                for (AComponent c : owner.sortedComponentList.values()) {
                    if (c.getType().equals(ACodaType.ER.name()) && !c.getName().contains("class")) {
                        eventNumber += c.getEventNumber();
                        eventRate += c.getEventRate();
                        eventRateAverage += c.getEventRateAverage();
                        dataRate += c.getDataRate();
                        dataRateAverage += c.getDataRateAverage();
                        numberOfLongs += c.getNumberOfLongs();
                        maxEventSize += c.getMaxEventSize();
                        minEventSize += c.getMinEventSize();
                        avgEventSize += c.getAvgEventSize();
                        if(c.getDestinationNames()!=null && c.getDestinationNames().length > 0) {

                            destinationNameArray = AfecsTool.concatenate(
                                    destinationNameArray, c.getDestinationNames());
                        }
                    }
                }


                AComponent tc = owner.sortedComponentList.get("ER_class");
                tc.setEventNumber(eventNumber);
                tc.setEventRate(eventRate);
                tc.setEventRateAverage(eventRateAverage);
                tc.setDataRate(dataRate);
                tc.setDataRateAverage(dataRateAverage);
                tc.setNumberOfLongs(numberOfLongs);
                tc.setMaxEventSize(maxEventSize);
                tc.setMinEventSize(minEventSize);
                tc.setAvgEventSize(avgEventSize);
                tc.setDestinationNames(destinationNameArray);
                tc.setExpid(owner.getPlEXPID());
            }

            // aggregate SED class component parameters
            if (owner.sortedComponentList.containsKey("SEB_class")) {
                int eventNumber = 0;
                float eventRate = 0f;
                float eventRateAverage = 0f;
                double dataRate = 0d;
                double dataRateAverage = 0d;
                long numberOfLongs = 0;
                int maxEventSize = 0;
                int minEventSize = 0;
                int avgEventSize = 0;
                String[] destinationNameArray = new String[0];


                for (AComponent c : owner.sortedComponentList.values()) {
                    if (c.getType().equals(ACodaType.SEB.name()) && !c.getName().contains("class")) {
                        eventNumber += c.getEventNumber();
                        eventRate += c.getEventRate();
                        eventRateAverage += c.getEventRateAverage();
                        dataRate += c.getDataRate();
                        dataRateAverage += c.getDataRateAverage();
                        numberOfLongs += c.getNumberOfLongs();
                        maxEventSize += c.getMaxEventSize();
                        minEventSize += c.getMinEventSize();
                        avgEventSize += c.getAvgEventSize();
                        if(c.getDestinationNames()!=null) {
                            destinationNameArray = (String[]) AfecsTool.concatenate(
                                    destinationNameArray, c.getDestinationNames());
                        }
                    }
                }
                AComponent tc = owner.sortedComponentList.get("SEB_class");
                tc.setEventNumber(eventNumber);
                tc.setEventRate(eventRate);
                tc.setEventRateAverage(eventRateAverage);
                tc.setDataRate(dataRate);
                tc.setDataRateAverage(dataRateAverage);
                tc.setNumberOfLongs(numberOfLongs);
                tc.setMaxEventSize(maxEventSize);
                tc.setMinEventSize(minEventSize);
                tc.setAvgEventSize(avgEventSize);
                tc.setDestinationNames(destinationNameArray);
                tc.setExpid(owner.getPlEXPID());
            }
            // aggregate PED class component parameters
            if (owner.sortedComponentList.containsKey("PEB_class")) {
                int eventNumber = 0;
                float eventRate = 0f;
                float eventRateAverage = 0f;
                double dataRate = 0d;
                double dataRateAverage = 0d;
                long numberOfLongs = 0;
                int maxEventSize = 0;
                int minEventSize = 0;
                int avgEventSize = 0;
                String[] destinationNameArray = new String[0];

                for (AComponent c : owner.sortedComponentList.values()) {
                    if (c.getType().equals(ACodaType.PEB.name()) && !c.getName().contains("class")) {
                        eventNumber += c.getEventNumber();
                        eventRate += c.getEventRate();
                        eventRateAverage += c.getEventRateAverage();
                        dataRate += c.getDataRate();
                        dataRateAverage += c.getDataRateAverage();
                        numberOfLongs += c.getNumberOfLongs();
                        maxEventSize += c.getMaxEventSize();
                        minEventSize += c.getMinEventSize();
                        avgEventSize += c.getAvgEventSize();
                        if(c.getDestinationNames()!=null) {
                            destinationNameArray = (String[]) AfecsTool.concatenate(
                                    destinationNameArray, c.getDestinationNames());
                        }
                    }
                }
                AComponent tc = owner.sortedComponentList.get("PEB_class");
                tc.setEventNumber(eventNumber);
                tc.setEventRate(eventRate);
                tc.setEventRateAverage(eventRateAverage);
                tc.setDataRate(dataRate);
                tc.setDataRateAverage(dataRateAverage);
                tc.setNumberOfLongs(numberOfLongs);
                tc.setMaxEventSize(maxEventSize);
                tc.setMinEventSize(minEventSize);
                tc.setAvgEventSize(avgEventSize);
                tc.setDestinationNames(destinationNameArray);
                tc.setExpid(owner.getPlEXPID());
            }

            try {
                if (owner.me.getState().contains("ing")) {
                    sleep(100);
                } else {
                    sleep(1000);
                    delay = delay - 1;
                    influx_delay = influx_delay - 1;
                }

                if (!owner.sortedComponentList.isEmpty()) {

                    // report agent states to all GUIs
                    // fileWriting report tot GUI
                    owner.send(AConstants.GUI,
                            owner.me.getSession() + "_" + owner.me.getRunType() + "/agents",
                            owner.s_fileWriting,
                            owner.sortedComponentList);
//                    }

                    // get the second (the first might be -clas component) entry of the agents map and define the state of the agent
                    Iterator it = owner.sortedComponentList.entrySet().iterator();
                    Map.Entry<String, AComponent> entry1 = (Map.Entry<String, AComponent>) it.next();
//                    Map.Entry<String, AComponent> entry2 = (Map.Entry<String, AComponent>) it.next(); // commented out vg 09.23.21

                    String tmpState = AConstants.booted;
                    if (entry1 != null && !entry1.getValue().getState().equals("na")) {
                        tmpState = entry1.getValue().getState();
                    }
//                    else if (entry2 != null && !entry2.getValue().getState().equals("na")) { // commented out vg 09.23.21
//                        tmpState = entry2.getValue().getState();
//                    }

                    // this will be true in case all agents of the reporting map will report the same state
                    boolean allSet = true;

                    for (AComponent c : owner.sortedComponentList.values()) {
                        if (c.getName().contains("class")) continue;

                        if (!c.getState().equals(tmpState)) allSet = false;

                        if (c.getState().equals(AConstants.disconnected)) {
                            if (owner.me.getState().equals(AConstants.active) && !owner.isResetting.get()) {

                                if (!owner.isClientProblemAtActive.get()) {
                                    owner.send(owner.me.getSession() + "/" + owner.me.getRunType(),
                                            AConstants.UIControlRequestReset,
                                            "Detected disconnected component in the system. Reset? \n\n" +
                                                    "Note: if you decided not to reset and continue with the CODA state machine, \n" +
                                                    "         we suggest disabling the component by selecting " +
                                                    "\"Expert\" menu \"Disable Component\" or \"Disable disconnected/Error\" menu item.");

                                    owner.isClientProblemAtActive.set(true);
                                }
                            } else {
                                owner.me.setState(AConstants.booted);
                                owner.isTransitioning.set(false);
                                owner.isResetting.set(false);
                                owner.send(AConstants.GUI,
                                        owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                                        owner.me.getRunTimeDataAsPayload());
                                if (!owner.isClientProblemAtActive.get()) {
                                    owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                                            owner.me.getName(),
                                            9,
                                            AConstants.ERROR,
                                            " transition failed. "
                                                    + c.getName()
                                                    + " is in state disconnected. Possible restart/power-cycle of the client might be required.");
                                    owner.dalogMsg(owner.me,
                                            9,
                                            "ERROR",
                                            " transition failed. "
                                                    + c.getName()
                                                    + " is in state disconnected. Possible restart/power-cycle of the client might be required.");
                                    owner.isClientProblemAtActive.set(true);
                                }
                            }
                            break;

                        } else if (c.getState().equalsIgnoreCase(AConstants.ERROR) ||
                                c.getState().equalsIgnoreCase(AConstants.failed)) {
                            if (owner.me.getState().equals(AConstants.active) && !owner.isResetting.get()) {

                                if (!owner.isClientProblemAtActive.get()) {
                                    owner.send(owner.me.getSession() + "/" + owner.me.getRunType(),
                                            AConstants.UIControlRequestReset,
                                            "Detected component in the system, reporting an Error state. Reset? \n\n" +
                                                    "Note: if you decided not to reset and continue with the CODA state machine, \n" +
                                                    "         we suggest disabling the component by selecting " +
                                                    "\"Expert\" menu \"Disable Component\" or \"Disable disconnected/Error\" menu item.");
                                    owner.isClientProblemAtActive.set(true);
                                }
                            } else {
                                owner.me.setState(AConstants.booted);
                                owner.isTransitioning.set(false);
                                owner.isResetting.set(false);
                                owner.send(AConstants.GUI,
                                        owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                                        owner.me.getRunTimeDataAsPayload());
                                if (!owner.isClientProblemAtActive.get()) {
                                    owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                                            owner.me.getName(),
                                            11,
                                            AConstants.ERROR,
                                            " transition failed. "
                                                    + c.getName()
                                                    + " is in state failed. Possible restart/power-cycle of the client might be required.");
                                    owner.dalogMsg(owner.me,
                                            11,
                                            "ERROR",
                                            " transition failed. "
                                                    + c.getName()
                                                    + " is in state failed. Possible restart/power-cycle of the client might be required.");
                                    owner.isClientProblemAtActive.set(true);
                                }
                            }
                            break;

                        } else if (c.getState().equalsIgnoreCase(AConstants.booted)) {
                            if (owner.me.getState().equals(AConstants.active) && !owner.isResetting.get()) {
                                if (!owner.isClientProblemAtActive.get()) {
                                    owner.send(owner.me.getSession() + "/" + owner.me.getRunType(),
                                            AConstants.UIControlRequestReset,
                                            "Detected component in the system, reporting a Booted state. Reset? \n\n" +
                                                    "Note: if you decided not to reset and continue with the CODA state machine, \n" +
                                                    "         we suggest disabling the component by selecting " +
                                                    "\"Expert\" menu \"Disable Component\" or \"Disable disconnected/Error\" menu item.");
                                    owner.isClientProblemAtActive.set(true);
                                }
                            } else {
                                owner.me.setState(AConstants.booted);
                                owner.isResetting.set(false);
                                owner.isTransitioning.set(false);
                                owner.send(AConstants.GUI,
                                        owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                                        owner.me.getRunTimeDataAsPayload());
                            }
                            break;
                        } else if (c.getState().equalsIgnoreCase(AConstants.removed)) {
                            owner.me.setState(AConstants.booted);
                            owner.isTransitioning.set(false);
                            owner.isResetting.set(false);
                            owner.send(AConstants.GUI,
                                    owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                                    owner.me.getRunTimeDataAsPayload());
                            if (!owner.isClientProblemAtActive.get()) {
                                owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                                        owner.me.getName(),
                                        11,
                                        AConstants.ERROR,
                                        " transition failed. "
                                                + c.getName()
                                                + " is removed. Possible restart/power-cycle of the client might be required.");
                                owner.dalogMsg(owner.me,
                                        11,
                                        "ERROR",
                                        " transition failed. "
                                                + c.getName()
                                                + " is removed. Possible restart/power-cycle of the client might be required.");
                                owner.isClientProblemAtActive.set(true);
                            }
                            break;
                        } else if (c.getState().equalsIgnoreCase(AConstants.busy)) {
                            owner.me.setState(AConstants.booted);
                            owner.isTransitioning.set(false);
                            owner.isResetting.set(false);
                            owner.send(AConstants.GUI,
                                    owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                                    owner.me.getRunTimeDataAsPayload());
                            if (!owner.isClientProblemAtActive.get()) {
                                owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                                        owner.me.getName(),
                                        7,
                                        AConstants.WARN,
                                        " Component "
                                                + c.getName()
                                                + " is owned by an other runType.");
                                owner.dalogMsg(owner.me,
                                        7,
                                        "WARNING",
                                        " Component "
                                                + c.getName()
                                                + " is owned by an other runType.");
                                owner.isClientProblemAtActive.set(true);
                            }
                            break;
                        }
                    }

                    // if all the agents are reporting configured set supervisor state to configured
                    if (allSet && (tmpState.equals(AConstants.configured))) {
                        owner.isResetting.set(false);
                        owner.me.setState(AConstants.configured);
                        owner.send(AConstants.GUI,
                                owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                                owner.me.getRunTimeDataAsPayload());
//                        if (!owner.isClientProblemAtActive.get()) {
                        if (!owner.wasConfigured) {
                            owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                                    owner.myName,
                                    1,
                                    AConstants.INFO,
                                    " Configure succeeded.");
//                            owner.isClientProblemAtActive.set(true);
                        }
//                        }
                        owner.wasConfigured = true;
                    }
                    if (owner.isResetting.get()) owner.me.setState("resetting");

                    //******* RUN_LOG_FILE  persisting run specific statistics *************
                    // We send a message to the platform registrar asking to update the new run archive
                    // file (current_run.log).
                    if (owner.me.getState().equals(AConstants.active) &&
                            !owner.me.getRunStartTime().equals("0") &&
                            delay <= 0) {
                        delay = RUNLOG_CREATE_DELAY;
                        ArrayList<cMsgPayloadItem> al = new ArrayList<>();
                        try {
                            al.add(new cMsgPayloadItem(AConstants.RUNTYPE, owner.myRunType));
                            al.add(new cMsgPayloadItem(AConstants.SESSION, owner.mySession));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String xmlLog = owner.createRunLogXML(false);
                        owner.send(owner.myConfig.getPlatformName(),
                                AConstants.PlatformControlRunLogBegin,
                                xmlLog,
                                al);

                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
