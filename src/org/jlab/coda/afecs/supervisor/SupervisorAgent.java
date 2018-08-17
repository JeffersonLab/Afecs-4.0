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

package org.jlab.coda.afecs.supervisor;

import org.jlab.coda.afecs.agent.AParent;
import org.jlab.coda.afecs.agent.AReportingTime;
import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.cool.ontology.AControl;
import org.jlab.coda.afecs.cool.ontology.AProcess;
import org.jlab.coda.afecs.cool.ontology.AService;
import org.jlab.coda.afecs.cool.parser.ACondition;
import org.jlab.coda.afecs.supervisor.thread.AHealthMonitorT;
import org.jlab.coda.afecs.supervisor.thread.AStatusReportT;
import org.jlab.coda.afecs.supervisor.thread.MoveToStateT;
import org.jlab.coda.afecs.supervisor.thread.ServiceExecutionT;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.ALogger;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.*;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Afecs control system supervisor agent.
 * Defines and maintains the following containers:
 * <ul>
 * <li>
 * Synchronized map holding supervised
 * agent objects
 * </li>
 * <li>
 * Synchronized map holding agents
 * reporting times (including current
 * and last reporting times)
 * </li>
 * <li>
 * Sorted component/agent list to be
 * reported to UIs
 * </li>
 * <li>
 * Sorting components in the order of
 * being allowed to write an output file
 * </li>
 * <li>
 * COOL described services ontology objects,
 * that describe rules (code, description,
 * etc.) of a service
 * </li>
 * <li>
 * COOL described condition ontology objects,
 * i.e. conditional operators, conditional
 * statements  and action statements.
 * </li>
 * </ul>
 * This class subscribes and provides callback
 * methods for:
 * <ul>
 * <li>
 * Callback for supervisor control messages
 * </li>
 * <li>
 * Callback for the messages coming from
 * the agents, under control of this
 * supervisor.
 * </li>
 * <li>
 * Callback for the requests from user
 * scripts or commandline programs.
 * </li>
 * <li>
 * Subscription handler for agent control
 * request messages, considering that
 * supervisor is also an agent.
 * </li>
 * </ul>
 * </p>
 * This class uses multi-threading for certain
 * operations. Hence it creates:
 * <ul>
 * <li>
 * Thread that periodically reports
 * supervised agents statuses.
 * </li>
 * <li>
 * Thread that monitors supervised
 * agents reporting
 * </li>
 * <li>
 * Thread that runs a required service
 * </li>
 * <li>
 * Thread that runs move-to-state requests
 * </li>
 * </ul>
 *
 * @author gurjyan
 *         Date: 11/13/14 Time: 2:51 PM
 * @version 3.x
 */
public class SupervisorAgent extends AParent implements Serializable {

    // Containers
    // Synchronized map holding supervised agents
    transient public ConcurrentHashMap<String, AComponent>
            myComponents =
            new ConcurrentHashMap<>();

    // Synchronized map holding agentReportingTimes
    transient public ConcurrentHashMap<String, AReportingTime>
            myCompReportingTimes =
            new ConcurrentHashMap<>();

    // Sorted component list to be reported to UIs
    public Map<String, AComponent>
            sortedComponentList =
            Collections.synchronizedMap(
                    new LinkedHashMap<String, AComponent>());

    // Sorting components in the order of
    // being allowed to write an output file
    transient public Map<String, AComponent>
            sortedByOutputList =
            Collections.synchronizedMap(
                    new LinkedHashMap<String, AComponent>());

    // Cool described Services
    transient public Map<String, AService>
            myServices =
            new HashMap<>();

    // Cool described service rules/conditions
    transient public Map<String, ArrayList<ACondition>>
            myServiceConditions =
            new HashMap<>();

    // Subscription handlers

    // Callback for supervisor control messages
    transient private cMsgSubscriptionHandle superControlSH;

    // Callback for the messages coming from the
    // agents under control of this supervisor.
    transient private cMsgSubscriptionHandle agentsStateSH;

    // Callback for the requests from user
    // scripts or commandline programs.
    transient private cMsgSubscriptionHandle userRequestSH;

    // Subscription handler for
    // AgentControlRequest messages
    transient private cMsgSubscriptionHandle controlSH;

    // Threads
    // Thread that periodically reports
    // supervised agents statuses
    transient private AStatusReportT agentStatusReport;

    // Thread that monitors supervised
    // agents reporting
    transient private AHealthMonitorT agentHealthMonitor;

    // Thread that runs a described service
    transient private ServiceExecutionT serviceExecutionThread;


    transient public SimpleDateFormat startEndFormatter =
            new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    transient public AtomicBoolean activeServiceExec =
            new AtomicBoolean(false);

    transient public AtomicBoolean haveCoda2Component =
            new AtomicBoolean(false);

    // Object references
    // Reference to the COOL service analyser object
    transient public CoolServiceAnalyser coolServiceAnalyser;

    // Provides utility methods
    transient public SUtility sUtility;

    // Reference to this supervisor
    public SupervisorAgent mySelf;


    // CODA rc specific fields
    public long timeLimit;
    public int _numberOfRuns;
    public long absTimeLimit;
    transient public AtomicBoolean autoStart =
            new AtomicBoolean(false);

    // Persistent and trigger component agent objects
    AComponent persistencyComponent;
    AComponent triggerComponent;

    // Flag that is set by the request of the gui to
    // enable/disable data file output
    public String s_fileWriting = "enabled";

    // Local instance of the logger object
    transient private ALogger lg = ALogger.getInstance();

    // private variable that stores requested service
    // (for e.g. CodaRcDownload, CodaRcStartRun, etc.)
    transient private String _requestedService = AConstants.udf;

    // flag indicating if disconnected component
    // was detected during the active state.
    transient public AtomicBoolean isClientProblemAtActive = new AtomicBoolean(false);



    transient public boolean wasConfigured = false;

    /**
     * <p>
     * Constructor calls the parent constructor that will
     * create cMsg domain connection object and register
     * this agent with the platform registration services.
     * The parent will also provide a subscription to the
     * agent info request messages, as well as status
     * reporting thread. For more information:
     * {@link AParent}
     * Creates COOL service executor and utility
     * class object references, as well as presents a
     * reference to itself.
     * <p>
     * </p>
     *
     * @param comp Object containing COLL
     *             description of this agent
     */
    public SupervisorAgent(AComponent comp) {
        super(comp);
        coolServiceAnalyser = new CoolServiceAnalyser(this);
        sUtility = new SUtility(this);
        me.setExpid(myConfig.getPlatformExpid());

        // basic subscriptions
        basicSubscribe();

        AfecsTool.sleep(1000);

        wasConfigured = false;

        mySelf = this;
    }

    /**
     * <p>
     * Method for gracefully exiting this agent
     * by stopping all running threads and
     * removing subscriptions.
     * </p>
     */
    public void sup_exit() throws cMsgException {
        remove_registration();
        sup_clean();
        _un_subscribe_all();
        platformDisconnect();
        wasConfigured = false;
    }

    /**
     * <p>
     * Supervisor soft reset.
     * Cleans maps, stops processes, etc.
     * </p>
     */
    public void sup_clean() {
        isResetting.set(false);
        isClientProblemAtActive.set(false);
        _un_subscribe_all();
        stop_rpp();
        stopServiceExecutionThread();
        stopAgentMonitors();
        // Reset all local maps.
        sUtility.clearLocalRegister();
    }

    /**
     * <p>
     * Supervisor control setup request
     * method.
     * <br>
     * Does COOL control analyses and
     * loads describes services, i.e.
     * agents, states and processes.
     * <br>
     * Starts threads to monitor agents
     * statuses and reporting times.
     * <br>
     * Tells supervised agents to configure.
     * Blocks until all agents report configured.
     * <p>
     * </p>
     *
     * @param cont COOL Control description
     *             object reference
     */
    private void _setup(AControl cont) {

        sup_clean();
        basicSubscribe();

        // Loads all the states and
        // processes, etc. ( cool staff).
        // This defines the list of agents
        // under control of this supervisor.
        // Subscribes specific messages
        coolServiceAnalyser.differentiate(cont);

        // Start monitoring agents
        // status and reporting times.
        startAgentMonitors();

        // Define persistency component agent,
        // i.e. the tail component who writes the file
        persistencyComponent = sUtility.getPersistencyComponent();

        // Define trigger source component agent
        triggerComponent = sUtility.getTriggerSourceComponent();

        // Report UIs that supervisor is
        // in the process of configuration.
        cont.getSupervisor().setState("configuring");
        send(AConstants.GUI,
                me.getSession() + "_" + me.getRunType() + "/supervisor",
                me.getRunTimeDataAsPayload());

        // Asking supervised agents to configure.
        for (AComponent com : myComponents.values()) {

            send(com.getName(),
                    AConstants.AgentControlRequestSetup,
                    com);
            AfecsTool.sleep(10);
        }

    }

    /**
     * <p>
     * Stops all dangling, old moveToState thread.
     * Starts a new moveToState thread that will
     * tell supervised agents to move to a desired
     * state.
     * <br>
     * Reports UIs that state transition is started.
     * Note: This method is used for reset only.
     * </p>
     *
     * @param stateName the name of a state to transition
     */
    public void _moveToState(String stateName) {

// ======== added 10.11.16 ============== Execute after active script at the reset ====================
        for (AProcess bp : me.getProcesses()) {
            if (bp != null) {
                if (bp.getAfter() != null && !bp.getAfter().equals(AConstants.udf)) {

                    if ((me.getState().equals(AConstants.active) || me.getState().equals("ending"))
                            && bp.getAfter().equals(AConstants.ended)) {
                        reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                                myName,
                                1,
                                AConstants.INFO,
                                " Starting process = " + bp.getName());

                        // execute scripts before the state transition
                        pm.executeProcess(bp, myPlugin, me);

                    }
                }
            }
        }
// ======== added 10.11.16 ============================================================================

        if (myComponents != null && !myComponents.isEmpty()) {
            MoveToStateT moveToStateThread = new MoveToStateT(this, "all", stateName);
            moveToStateThread.start();
            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                    myName,
                    1,
                    AConstants.
                            INFO,
                    " " + stateName + " is started.");
        }

        // If required state is "reseted" set the
        // state of this supervisor configured.
        if (stateName.equals(AConstants.reseted) ||
                stateName.equals(AConstants.emergencyreset)) {

            // stop periodic processes
            stopPeriodicProcesses();

            isResetting.set(true);
            isClientProblemAtActive.set(false);
        }

    }

    /**
     * <p>
     * Sends release agent message
     * to all supervised agents.
     * Sets it's state booted.
     * </p>
     */
    private void _releaseAgents() {
        // stop periodic processes
        stopPeriodicProcesses();

        isClientProblemAtActive.set(false);

        me.setState(AConstants.booted);
        send(AConstants.GUI,
                me.getSession() + "_" + me.getRunType() + "/supervisor",
                me.getRunTimeDataAsPayload());

        // Release agents under the control
        if (myComponents != null && !myComponents.isEmpty()) {
            for (String s : myComponents.keySet()) {
                send(s, AConstants.AgentControlRequestReleaseAgent, "");
            }
        }
    }

    /**
     * <p>
     * Public method that returns
     * reference to the local map
     * of supervised agents.
     * </p>
     *
     * @return reference to the components map.
     */
    public ConcurrentHashMap<String, AComponent> getMyComponents() {
        return myComponents;
    }

    /**
     * <p>
     * Basic supervisor subscriptions
     * This does not include agent status
     * message subscription
     * </p>
     */
    private void basicSubscribe() {
        try {

            // Subscribe to receive supervisor control messages,
            // e.g. start service, move to state , configure, etc.
            superControlSH = myPlatformConnection.subscribe(
                    myName,
                    AConstants.SupervisorControlRequest,
                    new SupervisorControlCB(),
                    null);

            // Subscribe messages coming from the user programs
            // asking about the state of the specific control
            userRequestSH = myPlatformConnection.subscribe(
                    myName,
                    AConstants.SupervisorUserRequest,
                    new UserRequestCB(),
                    null);

            // Subscribe messages sending control
            // messages to this agent
            controlSH = myPlatformConnection.subscribe(
                    myName,
                    AConstants.AgentControlRequest,
                    new AgentControlCB(),
                    null);

        } catch (cMsgException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
        }

    }

    /**
     * <p>
     * Supervisor agent specific subscriptions
     * </p>
     *
     * @return status of the subscriptions
     */
    public boolean agentSubscribe() {
        boolean status = true;

        try {

            // Un-subscribe first;
            if (agentsStateSH != null)
                myPlatformConnection.unsubscribe(agentsStateSH);

            // Subscribe messages coming from controlled agents,
            // reporting their states ( as an AComponent object
            // or payload items)
            agentsStateSH = myPlatformConnection.subscribe(
                    me.getSession(),
                    me.getRunType(),
                    new AgentsStatusCB(),
                    null);
        } catch (cMsgException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
            status = false;
        }
        return status;
    }

    /**
     * <p>
     * Un-subscribes supervisor agent
     * specific subscriptions.
     * </p>
     */
    private void _un_subscribe_all() {
        try {
            if (isPlatformConnected()) {
                if (superControlSH != null)
                    myPlatformConnection.unsubscribe(superControlSH);
                if (agentsStateSH != null)
                    myPlatformConnection.unsubscribe(agentsStateSH);
                if (userRequestSH != null)
                    myPlatformConnection.unsubscribe(userRequestSH);
                if (controlSH != null)
                    myPlatformConnection.unsubscribe(controlSH);
            }
        } catch (cMsgException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
        }
    }

    /**
     * <p>
     * Starts agent monitoring threads
     * <ul>
     * <li>Agent status monitoring</li>
     * <li>Agent reporting times</li>
     * </ul>
     * Threads will check periodically local
     * components map for a relevant information.
     * </p>
     */
    public void startAgentMonitors() {

        // Stop first
        stopAgentMonitors();

        agentStatusReport = new AStatusReportT(this);
        agentStatusReport.start();

        agentHealthMonitor = new AHealthMonitorT(this);
        agentHealthMonitor.start();
    }

    /**
     * <p>
     * Stops agent monitoring threads
     * in case they are running.
     * </p>
     */
    public void stopAgentMonitors() {
        if (agentStatusReport != null)
            agentStatusReport.setRunning(false);
        if (agentHealthMonitor != null)
            agentHealthMonitor.setRunning(false);
    }

    /**
     * <p>
     * Stops service execution thread
     * </p>
     */
    public void stopServiceExecutionThread() {
        if (serviceExecutionThread != null)
            serviceExecutionThread.stop();
    }

    /**
     * <p>
     * Start-run service organization
     * Starts the service execution thread
     * </p>
     */
    public void codaRC_startRun() {
        if (myServiceConditions.containsKey("CodaRcStartRun")) {
            serviceExecutionThread =
                    new ServiceExecutionT(mySelf, "CodaRcStartRun");
            serviceExecutionThread.start();

            String codaState =
                    coolServiceAnalyser.decodeCodaSMServiceName("CodaRcStartRun");
            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                    myName,
                    1,
                    AConstants.
                            INFO,
                    " " + codaState + " is started.");
        }
    }

    /**
     * <p>
     * Stop-run service organization
     * Starts the service execution thread
     * </p>
     */
    public void codaRC_stopRun() {
        if (myServiceConditions.containsKey("CodaRcEnd")) {

            serviceExecutionThread =
                    new ServiceExecutionT(mySelf, "CodaRcEnd");
            serviceExecutionThread.start();
            String codaState =
                    coolServiceAnalyser.decodeCodaSMServiceName("CodaRcEnd");
            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                    myName,
                    1,
                    AConstants.INFO,
                    " " + codaState + " is started.");
        }
    }

    @Deprecated
    public String getIntermediateState() {
        if (_requestedService != null && _requestedService.equals("CodaRcStartRun")) {
            List<String> statuses = new ArrayList<>();
            for (AComponent c : myComponents.values()) {
                statuses.add(c.getState());
            }
            if (statuses.contains("downloading")) {
                return "downloading";
            } else if (isAllReport(statuses, AConstants.downloaded)) {
                return AConstants.downloaded;
            } else if (statuses.contains("prestarting")) {
                return "prestarting";
            } else if (isAllReport(statuses, AConstants.paused)) {
                return AConstants.prestarted;
            } else if (statuses.contains("activating")) {
                return "activating";
            } else if (isAllReport(statuses, AConstants.active)) {
                return AConstants.active;
            }
        }
        return AConstants.udf;
    }

    @Deprecated
    private Boolean isAllReport(List<String> statuses,
                                String state) {
        for (String s : statuses) {
            if (!s.equals(state)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Defines if run is ended
     * </p>
     *
     * @return true if run is ended
     */
    public boolean isRunEnded() {
        for (AComponent c : myComponents.values()) {
            if (!c.getState().equals(AConstants.downloaded)) {
                return false;
            }
        }
        // stop periodic processes
        stopPeriodicProcesses();
        return true;
    }

    /**
     * <p>
     * Defines the output file name
     * based on components types.
     * </p>
     *
     * @return output file name
     */
    public String defineOutputFile() {
        AComponent c = sortedByOutputList.entrySet().iterator().next().getValue();
        return c.getFileName();
    }

    /**
     * <p>
     * Creates run-log xml file
     * </p>
     *
     * @param isEnded defines if this is end of the run.
     * @return xml string of a file
     */
    public String createRunLogXML(boolean isEnded) {

        // Ask control designer to get rtv map
        List<cMsgPayloadItem> al = new ArrayList<>();
        try {
            al.add(new cMsgPayloadItem(AConstants.SESSION, mySession));
            al.add(new cMsgPayloadItem(AConstants.RUNTYPE, myRunType));
        } catch (cMsgException e1) {
            lg.logger.severe(AfecsTool.stack2str(e1));
        }

        // Ask platform registrar rtv file content for a defined runType
        cMsgMessage m = null;
        try {
            m = p2pSend(AConstants.CONTROLDESIGNER,
                    AConstants.DesignerInfoRequestGetDefinedRTVs,
                    al, AConstants.TIMEOUT);
        } catch (AException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
        }

        Map<String, String> remRtv = null;
        if (m != null && m.getByteArray() != null) {
            try {
                remRtv = (Map<String, String>) AfecsTool.B2O(m.getByteArray());
            } catch (IOException | ClassNotFoundException e) {
                lg.logger.severe(AfecsTool.stack2str(e));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<coda runtype = ").append("\"").append(myRunType).append("\"").
                append(" session = ").append("\"").append(mySession).append("\"").
                append(">").append("\n");
        sb.append("   <run-start>").append("\n");
        sb.append("      <run-number>").append(me.getRunNumber()).append("</run-number>").append("\n");
        sb.append("      <start-time>").append(me.getRunStartTime()).append("</start-time>").append("\n");

        String fileName = me.getFileName();
        if (fileName.contains("?") || fileName.contains("^") || fileName.contains("<") || fileName.contains(">")) {
            fileName = "corrupted";
        }
        sb.append("      <out-file>").append(fileName).append("</out-file>").append("\n");

        sb.append("      <components>").append("\n");

        // write components data
        xmlComponentData(sb);

        sb.append("      </components>").append("\n");

        if (remRtv != null && !remRtv.isEmpty()) {
            sb.append("      <rtvs>").append("\n");
            for (String rtv : remRtv.keySet()) {
                String value = remRtv.get(rtv);
                if (value.contains("&")) value = value.replaceAll("&", "&amp;");
                sb.append("         <rtv name = ").append("\"").append(rtv).append("\"").
                        append(" value = ").append("\"").append(value).append("\"").
                        append("/>").append("\n");
            }
            sb.append("      </rtvs>").append("\n");
        }
        sb.append("      <update-time>").append(startEndFormatter.format(new Date())).append("</update-time>").append("\n");
        sb.append("      <total-evt>").append(me.getEventNumber()).append("</total-evt>").append("\n");

        sb.append("   </run-start>").append("\n");


        if (isEnded) {
            sb.append("   <run-end>").append("\n");
            sb.append("      <end-time>").append(me.getRunEndTime()).append("</end-time>").append("\n");
            sb.append("      <total-evt>").append(me.getEventNumber()).append("</total-evt>").append("\n");
            sb.append("      <components>").append("\n");

            // write components data
            xmlComponentData(sb);

            sb.append("      </components>").append("\n");
            sb.append("   </run-end>").append("\n");
        }
        sb.append("</coda>").append("\n");

        return sb.toString();
    }

    private void xmlComponentData(StringBuilder sb) {
        for (AComponent comp : getMyComponents().values()) {
            sb.append("         <component name = ").append("\"").append(comp.getName()).append("\"").
                    append(" type = ").append("\"").append(comp.getType()).append("\"").
                    append(">").append("\n");
            sb.append("            <evt-rate>").append(comp.getEventRateAverage()).append("</evt-rate>").append("\n");
            sb.append("            <data-rate>").append(comp.getDataRateAverage()).append("</data-rate>").append("\n");
            sb.append("            <evt-number>").append(comp.getEventNumber()).append("</evt-number>").append("\n");
            sb.append("            <min-evt-size>").append(comp.getMinEventSize()).append("</min-evt-size>").append("\n");
            sb.append("            <max-evt-size>").append(comp.getMaxEventSize()).append("</max-evt-size>").append("\n");
            sb.append("            <average-evt-size>").append(comp.getAvgEventSize()).append("</average-evt-size>").append("\n");
            sb.append("            <min-evt-build-time>").append(comp.getMinTimeToBuild()).append("</min-evt-build-time>").append("\n");
            sb.append("            <max-evt-build-time>").append(comp.getMaxTimeToBuild()).append("</max-evt-build-time>").append("\n");
            sb.append("            <average-evt-build-time>").append(comp.getMeanTimeToBuild()).append("</average-evt-build-time>").append("\n");
            sb.append("            <chunk-x-et-buffer>").append(comp.getChunkXEtBuffer()).append("</chunk-x-et-buffer>").append("\n");

            String cFileName = comp.getFileName();
            if (cFileName.contains("?") || cFileName.contains("^") || cFileName.contains("<") || cFileName.contains(">")) {
                cFileName = "corrupted";
            }
            sb.append("            <out-file>").append(cFileName).append("</out-file>").append("\n");

            sb.append("         </component>").append("\n");
        }

    }

    public void requestPopUpDialog(String text) {
        ArrayList<cMsgPayloadItem> al = new ArrayList<>();
        try {
            al.add(new cMsgPayloadItem("MSGCONTENT", text));

        } catch (cMsgException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
        }

        // Inform GUIs
        send(me.getSession() + "/" + me.getRunType(),
                AConstants.UIControlPopupInfo,
                al);
    }

    /**
     * Sends a message to enable/disable file_writing to
     * a file output responsible component/components
     */
    private void setFileWriting() {
        if (persistencyComponent != null) {
            if (persistencyComponent.getName().contains("class")) {
                switch (persistencyComponent.getName()) {
                    case "ER_class":
                        for (AComponent c : sortedComponentList.values()) {
                            if (c.getType().equals(ACodaType.ER.name())) {
                                System.out.println("DDD ------- setting fileWriting = " +
                                        s_fileWriting + " for component = " + c.getName());
                                send(c.getName(),
                                        AConstants.AgentControlRequestSetFileWriting,
                                        s_fileWriting);
                            }
                        }
                        break;

                    case "PEB_class":
                        for (AComponent c : sortedComponentList.values()) {
                            if (c.getType().equals(ACodaType.PEB.name())) {
                                send(c.getName(),
                                        AConstants.AgentControlRequestSetFileWriting,
                                        s_fileWriting);
                            }
                        }
                        break;

                    case "SEB_class":
                        for (AComponent c : sortedComponentList.values()) {
                            if (c.getType().equals(ACodaType.SEB.name())) {
                                send(c.getName(),
                                        AConstants.AgentControlRequestSetFileWriting,
                                        s_fileWriting);
                            }
                        }
                        break;
                }
            } else {
                send(persistencyComponent.getName(),
                        AConstants.AgentControlRequestSetFileWriting,
                        s_fileWriting);
            }
        }
    }

    /**
     * Private inner class for responding to control
     * request messages to this supervisor agent.
     */
    private class SupervisorControlCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg == null) return;

            String type = msg.getType();
            String requestData = msg.getText();
            _requestedService = requestData;
            ArrayList<cMsgPayloadItem> al = new ArrayList<>();

            switch (type) {
                case AConstants.SupervisorControlRequestStartService:
                    // In this case requestData is the
                    // name of the COOL service
                    if (requestData != null) {


                        // Check to see if we have disconnected component/s
                        // Send ui asking to pop-up a dialog for suggesting
                        // to disable disconnected components. Blocks for ever.
                        if (sUtility.containsState(AConstants.disconnected)) {
                            cMsgMessage mb = null;
                            try {
                                mb = p2pSend(
                                        me.getSession() + "/" + me.getRunType(),
                                        AConstants.UIControlRequestDisableDisconnected,
                                        "",
                                        AConstants.TIMEOUT);
                            } catch (AException e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                            }
                            if (mb != null && mb.getText() != null) {
                                if (mb.getText().equals("disable")) {
                                    for (AComponent c : sortedComponentList.values()) {
                                        if (c.getState().equals(AConstants.disconnected)) {
                                            c.setState(AConstants.disabled);
                                            myComponents.remove(c.getName());
                                            myCompReportingTimes.remove(c.getName());
                                        }
                                    }
                                }
                            }
                        }
                        if (myServiceConditions.containsKey(requestData)) {
                            serviceExecutionThread = new ServiceExecutionT(mySelf, requestData);
                            serviceExecutionThread.start();
                            String codaState = coolServiceAnalyser.decodeCodaSMServiceName(requestData);

                            // added 04.14.16
                            if (codaState.equals(AConstants.reseted)) isResetting.set(true);

                            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                                    myName,
                                    1,
                                    AConstants.INFO,
                                    " " + codaState + " is started.");
                        }
                    }

                    break;
                case AConstants.SupervisorControlRequestSopService:
                    stopServiceExecutionThread();

                    break;

                // request from the UI/user to disable the
                // component and the representative agent
                case AConstants.SupervisorControlRequestDisableAgent:
                    sortedComponentList.get(requestData).setState(AConstants.disabled);
                    myComponents.remove(requestData);
                    myCompReportingTimes.remove(requestData);

                    break;

                // request from the UI/user to disable disconnected
                // components and their representative agents
                case AConstants.SupervisorControlRequestDisableAllDisconnects:
                    for (AComponent c : sortedComponentList.values()) {
                        if (c.getState().equals(AConstants.disconnected) ||
                                c.getState().equals(AConstants.error) ||
                                c.getState().equals(AConstants.ERROR)) {
                            c.setState(AConstants.disabled);
                            myComponents.remove(c.getName());
                            myCompReportingTimes.remove(c.getName());
                        }
                    }

                    break;

                case AConstants.SupervisorControlRequestReleaseAgent:
                    if (myComponents.containsKey(requestData)) {
                        myComponents.get(requestData).setState(AConstants.removed);
                        me.setState(AConstants.booted);
                        send(AConstants.GUI,
                                me.getSession() + "_" + me.getRunType() + "/supervisor",
                                me.getRunTimeDataAsPayload());
                    }
                    if (myComponents.containsKey(requestData)) {
                        myComponents.get(requestData).setState(AConstants.removed);
                    }

                    break;

                case AConstants.SupervisorControlRequestReleaseRunType:
                    _un_subscribe_all();
                    stopAgentMonitors();
                    stopStatusReporting();
                    stopServiceExecutionThread();

                    sortedComponentList.clear();
                    myComponents.clear();
                    myCompReportingTimes.clear();
                    myServices.clear();
                    myServiceConditions.clear();

                    me.setSession(AConstants.udf);
                    me.setRunType(AConstants.udf);
                    me.setState(AConstants.udf);

                    update_registration();

                    break;

                case AConstants.SupervisorControlRequestInformDeadAgent:
                    if (myComponents.containsKey(requestData)) {
                        myComponents.get(requestData).setState(AConstants.disconnected);

                        me.setState(AConstants.booted);
                        send(AConstants.GUI,
                                me.getSession() + "_" + me.getRunType() + "/supervisor",
                                me.getRunTimeDataAsPayload());
                    }
                    break;

                // Change the run number in the cool db if we
                // are not active or not in the middle of a transition
                case AConstants.SupervisorControlRequestSetRunNumber:

                    if (!(me.getState().equals(AConstants.active) ||
                            me.getState().equals(AConstants.prestarted) ||
                            me.getState().endsWith("ing"))) {

                        if (msg.getPayloadItem(AConstants.RUNNUMBER) != null) {

                            // Ask platform to set run number, and update platform xml file
                            al.clear();
                            try {
                                al.add(new cMsgPayloadItem(AConstants.RUNTYPE, me.getRunType()));
                                al.add(new cMsgPayloadItem(AConstants.SESSION, me.getSession()));
                                al.add(msg.getPayloadItem(AConstants.RUNNUMBER));

                            } catch (Exception e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                            }

                            send(myConfig.getPlatformName(),
                                    AConstants.PlatformRegistrationRequestSetRunNum,
                                    al);
                        }
                    }

                    break;

                case AConstants.SupervisorControlRequestReportAgents:
                    if (msg.isGetRequest()) {
                        try {
                            cMsgMessage mr = msg.response();
                            mr.setSubject(AConstants.udf);
                            mr.setType(AConstants.udf);
                            mr.setByteArray(AfecsTool.O2B(sortedComponentList));
                            myPlatformConnection.send(mr);
                        } catch (cMsgException | IOException e) {
                            lg.logger.severe(AfecsTool.stack2str(e));
                        }
                    }
                    break;

                case AConstants.SupervisorControlRequestFailTransition:
                    send(AConstants.GUI,
                            me.getSession() + "_" + me.getRunType() + "/supervisor",
                            me.getRunTimeDataAsPayload());
                    reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                            myName,
                            9,
                            AConstants.ERROR,
                            " transition failed.");
                    dalogMsg(myName,
                            9,
                            AConstants.ERROR,
                            " transition failed.");

                    // Ask components to reset
                    _moveToState(AConstants.reseted);

                    break;

                // The following is coda run control specific
                // request such as setting run-control limits.
                case AConstants.SupervisorControlRequestSetEventLimit:
                    try {
                        int i = Integer.parseInt(requestData);
                        if (i >= 0) {
                            me.setEventLimit(i);

                            // Ask platform registrar agent to update cool
                            al.clear();
                            try {
                                al.add(new cMsgPayloadItem(AConstants.RUNTYPE, me.getRunType()));
                                al.add(new cMsgPayloadItem(AConstants.EVENTLIMIT, me.getEventLimit()));
                            } catch (cMsgException e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                            }
                            send(myConfig.getPlatformName(),
                                    AConstants.PlatformControlUpdateOptions,
                                    al);
                        }
                    } catch (NumberFormatException e) {
                        lg.logger.severe(AfecsTool.stack2str(e));
                    }

                    break;

                case AConstants.SupervisorControlRequestResetEventLimit:
                    me.setEventLimit(0);
                    al.clear();
                    try {
                        al.add(new cMsgPayloadItem(AConstants.RUNTYPE, me.getRunType()));
                        al.add(new cMsgPayloadItem(AConstants.EVENTLIMIT, me.getEventLimit()));
                    } catch (cMsgException e) {
                        lg.logger.severe(AfecsTool.stack2str(e));
                    }
                    send(myConfig.getPlatformName(),
                            AConstants.PlatformControlUpdateOptions,
                            al);

                    break;

                case AConstants.SupervisorControlRequestSetDataLimit:
                    try {
                        long i = Long.parseLong(requestData);
                        if (i >= 0) {
                            me.setDataLimit(i);

                            // Ask platform registrar agent to update cool
                            al.clear();
                            try {
                                al.add(new cMsgPayloadItem(AConstants.RUNTYPE, me.getRunType()));
                                al.add(new cMsgPayloadItem(AConstants.DATALIMIT, me.getDataLimit()));
                            } catch (cMsgException e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                            }
                            send(myConfig.getPlatformName(),
                                    AConstants.PlatformControlUpdateOptions,
                                    al);
                        }
                    } catch (NumberFormatException e) {
                        lg.logger.severe(AfecsTool.stack2str(e));
                    }

                    break;

                case AConstants.SupervisorControlRequestResetDataLimit:
                    me.setDataLimit(0);
                    al.clear();
                    try {
                        al.add(new cMsgPayloadItem(AConstants.RUNTYPE, me.getRunType()));
                        al.add(new cMsgPayloadItem(AConstants.DATALIMIT, me.getDataLimit()));
                    } catch (cMsgException e) {
                        lg.logger.severe(AfecsTool.stack2str(e));
                    }
                    send(myConfig.getPlatformName(),
                            AConstants.PlatformControlUpdateOptions,
                            al);

                    break;

                case AConstants.SupervisorControlRequestSetTimeLimit:
                    try {
                        int i = Integer.parseInt(requestData);
                        if (i > 0) {
                            absTimeLimit = i * 60 * 1000;
                            me.setTimeLimit(absTimeLimit);
                        }
                    } catch (NumberFormatException e) {
                        lg.logger.severe(AfecsTool.stack2str(e));
                    }

                    break;

                case AConstants.SupervisorControlRequestResetTimeLimit:
                    timeLimit = 0;
                    absTimeLimit = 0;
                    me.setTimeLimit(absTimeLimit);

                    break;

                case AConstants.SupervisorControlRequestEnableAutoMode:
                    if (me.getEventLimit() > 0 || me.getDataLimit() > 0 || absTimeLimit > 0) {
                        autoStart.set(true);
                        me.setAutoStart(true);
                    }

                    break;

                case AConstants.SupervisorControlRequestDisableAutoMode:
                    autoStart.set(false);
                    me.setAutoStart(false);

                    break;

                case AConstants.SupervisorControlRequestSetNumberOfRuns:
                    try {
                        if (_numberOfRuns <= 0) {
                            _numberOfRuns = Integer.parseInt(msg.getText());
                            me.setnScheduledRuns(_numberOfRuns);
                        }
                    } catch (NumberFormatException e) {
                        lg.logger.severe(AfecsTool.stack2str(e));
                    }

                    break;

                case AConstants.SupervisorControlRequestResetNumberOfRuns:
                    _numberOfRuns = 0;
                    me.setnScheduledRuns(_numberOfRuns);

                    break;

                case AConstants.SupervisorControlRequestPause:
                    send(triggerComponent.getName(),
                            "run/transition/pause",
                            "pause");
                    break;

                case AConstants.SupervisorControlRequestResume:
                    send(triggerComponent.getName(),
                            "run/transition/resume",
                            "resume");
                    break;


                case AConstants.SupervisorControlRequestNoFileOutput:
                    s_fileWriting = "disabled";
                    setFileWriting();
                    break;

                case AConstants.SupervisorControlRequestResumeFileOutput:
                    s_fileWriting = "enabled";
                    setFileWriting();
                    break;

                case AConstants.SupervisorControlRequestReportReady:
//                    if (msg.isGetRequest()) {
//                        try {
//                            cMsgMessage mr = msg.response();
//                            mr.setSubject(AConstants.udf);
//                            mr.setType(AConstants.udf);
//                            mr.setText("IamAlive");
//                            myPlatformConnection.send(mr);
//                        } catch (cMsgException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    break;
            }
            if (msg.isGetRequest()) {
                try {
                    cMsgMessage mr = msg.response();
                    mr.setSubject(AConstants.udf);
                    mr.setType(AConstants.udf);
                    mr.setText("IamAlive");
                    myPlatformConnection.send(mr);
                } catch (cMsgException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * <p>
     * Private inner class for listening user
     * program requests. Launches a separate
     * thread to service the request.
     * </p>
     */
    private class UserRequestCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg == null) return;

            new UserRequestServiceThread(msg).start();
        }
    }

    /**
     * <p>
     * Private thread class to service user requests.
     * Only sand-and-get requests are supported.
     * </p>
     */
    private class UserRequestServiceThread extends Thread {
        cMsgMessage msg;

        public UserRequestServiceThread(cMsgMessage msg) {
            this.msg = msg;
        }

        public void run() {
            if (msg != null) {
                if (msg.isGetRequest()) {
                    try {
                        String type = msg.getType();
                        String txt = msg.getText();
                        cMsgMessage mr = msg.response();
                        mr.setSubject(AConstants.udf);
                        mr.setType(AConstants.udf);

                        AComponent tmpCmp;

                        switch (type) {

                            case AConstants.SupervisorReportRunNumber:
                                mr.setText(Integer.toString(me.getRunNumber()));
                                break;

                            case AConstants.SupervisorReportRunState:
                                mr.setText(me.getState());
                                break;

                            case AConstants.SupervisorReportSession:
                                mr.setText(me.getSession());
                                break;

                            case AConstants.SupervisorReportRunType:
                                mr.setText(me.getRunType());
                                break;

                            case AConstants.SupervisorReportSchedulerStatus:
                                mr.setText("event-limit = " +
                                        me.getEventLimit() +
                                        "\ntime-limit = " +
                                        timeLimit +
                                        "\nauto-mode =  " +
                                        autoStart +
                                        "\nremaining-runs = " +
                                        _numberOfRuns);
                                break;

                            case AConstants.SupervisorReportComponentStates:
                                StringBuilder sb = new StringBuilder();
                                for (AComponent comp : myComponents.values()) {
                                    sb.append(comp.getName()).
                                            append(" ").
                                            append(comp.getState()).
                                            append("\n");
                                }
                                mr.setText(sb.toString());
                                break;

                            case AConstants.SupervisorReportComponentEventNumber:
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.setText(Long.toString(tmpCmp.getEventNumber()));
                                }
                                break;

                            case AConstants.SupervisorReportComponentOutputFile:
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.setText(tmpCmp.getFileName());
                                }
                                break;

                            case AConstants.SupervisorReportComponentEventRate:
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.setText(Float.toString(tmpCmp.getEventRate()));
                                }
                                break;

                            case AConstants.SupervisorReportComponentDataRate:
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.setText(Double.toString(tmpCmp.getDataRate()));
                                }
                                break;

                            case AConstants.SupervisorReportComponentState:
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.setText(tmpCmp.getState());
                                }
                                break;

                            case AConstants.SupervisorReportRunStartTime:
                                mr.setText(me.getRunStartTime());
                                break;

                            case AConstants.SupervisorReportRunEndTime:
                                mr.setText(me.getRunEndTime());
                                break;

                            case AConstants.SupervisorReportRunNumber_p:
                                mr.addPayloadItem(
                                        new cMsgPayloadItem("runnumber_p", me.getRunNumber()));
                                break;

                            case AConstants.SupervisorReportRunState_p:
                                mr.addPayloadItem(
                                        new cMsgPayloadItem("runstate_p", me.getState()));
                                break;

                            case AConstants.SupervisorReportComponentStates_p:
                                ArrayList<String> l = new ArrayList<>();
                                for (AComponent comp : myComponents.values()) {
                                    l.add(comp.getName() + " " + comp.getState());
                                }
                                mr.addPayloadItem(
                                        new cMsgPayloadItem("states_p", l.toArray(new String[l.size()])));
                                break;

                            case AConstants.SupervisorReportSchedulerStatus_p:
                                mr.addPayloadItem(new cMsgPayloadItem("scedulerstatus_p",
                                        "event-limit = " + me.getEventLimit() +
                                                "\ntime-limit = " + timeLimit +
                                                "\nauto-mode =  " + autoStart +
                                                "\nremaining-runs = " + _numberOfRuns));
                                break;

                            case AConstants.SupervisorReportSession_p:
                                mr.addPayloadItem(new cMsgPayloadItem("supsession_p", me.getSession()));
                                break;

                            case AConstants.SupervisorReportRunStartTime_p:
                                mr.addPayloadItem(new cMsgPayloadItem("supstarttime_p", me.getRunStartTime()));
                                break;

                            case AConstants.SupervisorReportRunEndTime_p:
                                mr.addPayloadItem(new cMsgPayloadItem("supendtime_p", me.getRunEndTime()));
                                break;

                            case AConstants.SupervisorReportComponentEventNumber_p: {
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("evtnumber_p", tmpCmp.getEventNumber()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportComponentOutputFile_p: {
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("outputfile_p", tmpCmp.getFileName()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportComponentEventRate_p: {
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("compevtrate_p", tmpCmp.getEventRate()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportComponentDataRate_p: {
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("compdatarate_p", tmpCmp.getDataRate()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportComponentLiveTime_p: {
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("complivetime_p", tmpCmp.getLiveTime()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportComponentState_p: {
                                tmpCmp = myComponents.get(txt);
                                if (tmpCmp != null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("compstate_p", tmpCmp.getState()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportRtvs_p:
                                Map<String, String> map = AfecsTool.readRTVFile(myRunType, mySession);
                                if (map != null) {
                                    try {
                                        mr.setByteArray(AfecsTool.O2B(map));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    System.out.println(myName + ":Error reading rtv file. See log for more details.");
                                }
                                break;

                            case AConstants.SupervisorReportPersistencyComponent:
                                if (persistencyComponent != null) mr.setText(persistencyComponent.getName());
                                else mr.setText(AConstants.udf);
                                break;

                            case AConstants.SupervisorReportTriggerSourceComponent:
                                if (triggerComponent != null) mr.setText(triggerComponent.getName());
                                else mr.setText(AConstants.udf);
                                break;
                        }

                        myPlatformConnection.send(mr);
                    } catch (cMsgException e) {
                        lg.logger.severe(AfecsTool.stack2str(e));
                    }
                }
            }
        }
    }


    /**
     * <p>
     * Private inner class for listening
     * supervised agents messages.
     * Checks for conditions to end the run,
     * such as event, time limits, etc.
     * </p>
     */
    private class AgentsStatusCB extends cMsgCallbackAdapter {

        // overwrite to increase callback queue size (1000 messages)
        public int getMaximumQueueSize() {
            return 10000;
        }

        public void callback(final cMsgMessage msg, Object userObject) {

            if (msg != null && msg.getByteArray() != null) {

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(new Runnable() {
                    public void run() {

                        String sender = msg.getSender();

                        if (myComponents.containsKey(sender)) {

                            AComponent ac = null;
                            try {
                                ac = (AComponent) AfecsTool.B2O(msg.getByteArray());
                            } catch (IOException | ClassNotFoundException e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                            }
                            if (ac != null && ac.getState() != null) {

                                // Update local maps
                                myComponents.put(sender, ac);

                                sortedComponentList.put(sender, ac);
                                if (myCompReportingTimes.contains(sender)) {
                                    myCompReportingTimes.get(sender).setCurrentReportingTime(new Date().getTime());
                                }
                                // Supervisor's event number is set to
                                // the persistent component event number.
                                // Check limits against persistent component
                                // reporting and decide if we need to end the run
                                if (persistencyComponent != null) {

                                    // check to see if persistent component is of class type
                                    if (persistencyComponent.getName().contains("class")) {
                                        me.setEventNumber(persistencyComponent.getEventNumber());
                                        me.setNumberOfLongs(persistencyComponent.getNumberOfLongs());

                                        // persistent component is a real reporting component
                                    } else if (ac.getName().equals(persistencyComponent.getName())) {

                                        // Set supervisor event number
                                        me.setEventNumber(ac.getEventNumber());
                                        me.setNumberOfLongs(ac.getNumberOfLongs());

                                    }
                                    // Check if event limit has reached
                                    if (me.getEventLimit() > 0 &&
                                            me.getState().equals(AConstants.active)) {
                                        if (me.getEventLimit() < me.getEventNumber()) {
                                            codaRC_stopRun();
                                        }
                                    }

                                    // Check if data limit has reached
                                    if (me.getDataLimit() > 0 &&
                                            me.getState().equals(AConstants.active)) {
                                        if (me.getDataLimit() < me.getNumberOfLongs()) {
                                            codaRC_stopRun();
                                        }
                                    }
                                }
                            }
                        }

                    }
                });
                executorService.shutdown();

                // Check if time limit has reached
                if (timeLimit > 0) {
                    long lDateTime = new Date().getTime();
                    if (lDateTime > timeLimit &&
                            me.getState().equals(AConstants.active)) {
                        codaRC_stopRun();
                    }
                }
            }

        }
    }

    /**
     * <p>
     * Private inner class for responding to
     * control messages addressed to this agent.
     * Runs every control request in a separate thread.
     * </p>
     */
    private class AgentControlCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {

                if(msg.isGetRequest()){
                    try {
                        cMsgMessage mr = msg.response();
                        mr.setSubject(AConstants.udf);
                        mr.setType(AConstants.udf);
                        myPlatformConnection.send(mr);
                    } catch (cMsgException e) {
                        System.out.println(e.getMessage());
                    }
                }

                String type = msg.getType();
                String txt = msg.getText();

                switch (type) {
                    case AConstants.SupervisorControlRequestSetup:
                        AControl control = null;
                        wasConfigured = false;
                        try {
                            control = (AControl) AfecsTool.B2O(msg.getByteArray());
                        } catch (IOException | ClassNotFoundException e) {
                            lg.logger.severe(AfecsTool.stack2str(e));
                        }
                        if (control != null) {

                            // differentiate supervisor
                            differentiate(control.getSupervisor());

                            System.out.println("DDD -----| Info: " + myName + " setting up control system.");
                            // supervisor setup
                            _setup(control);
                        }
                        break;

                    case AConstants.AgentControlRequestPlatformDisconnect:
                        try {
                            sup_exit();
                        } catch (cMsgException e) {
                            lg.logger.severe(AfecsTool.stack2str(e));
                        }
                        break;

                    case AConstants.AgentControlRequestExecuteProcess:
                        // process name is passed through the cMsg test field
                        if (txt != null) {
                            requestStartProcess(txt);
                        }
                        break;

                    case AConstants.AgentControlRequestMoveToState:
                        // state name is passed through
                        // the cMsg test field
                        if (txt != null) {
                            isResetting.set(false);
                            s_fileWriting = "enabled";
                            setFileWriting();
                            _moveToState(txt);
                        }
                        break;

                    case AConstants.AgentControlRequestSetRunNumber:
                        if (msg.getPayloadItem(AConstants.RUNNUMBER) != null) {
                            try {
                                int rn = msg.getPayloadItem(AConstants.RUNNUMBER).getInt();
                                me.setRunNumber(rn);
                                if (msg.getPayloadItem("ForAgentOnly") == null) {
                                    runControlSetRunNumber(rn);
                                }
                            } catch (cMsgException e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                            }
                        }
                        break;

                    case AConstants.SupervisorControlRequestReleaseAgents:

                        _releaseAgents();

                        // Exit. no one needs supervisor
                        // without agents to supervise
                        try {
                            sup_exit();
                        } catch (cMsgException e) {
                            lg.logger.severe(AfecsTool.stack2str(e));
                        }
                        break;

                    case AConstants.AgentControlRequestStartReporting:
                        startStatusReporting();
                        break;

                    case AConstants.AgentControlRequestStopReporting:
                        // stop if reporting thread is active
                        stopStatusReporting();
                        break;
                }
            } else {
                System.out.println("DDD MESSAGE IS NULL");
                me.setState(AConstants.booted);
            }
        }
    }
}
