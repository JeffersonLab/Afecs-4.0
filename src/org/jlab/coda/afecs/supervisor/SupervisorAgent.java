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
import org.jlab.coda.afecs.codarc.CodaRCAgent;
import org.jlab.coda.afecs.container.AContainer;
import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.cool.ontology.AControl;
import org.jlab.coda.afecs.cool.ontology.AProcess;
import org.jlab.coda.afecs.cool.ontology.AService;
import org.jlab.coda.afecs.cool.parser.ACondition;
import org.jlab.coda.afecs.influx.InfluxInjector;
import org.jlab.coda.afecs.supervisor.thread.AStatusReportT;
import org.jlab.coda.afecs.supervisor.thread.ServiceExecutionT;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;

import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.*;
import org.jlab.coda.jinflux.JinFluxException;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
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
 * @version 4.x
 */
public class SupervisorAgent extends AParent implements Serializable {

    // Containers
    // Synchronized map holding supervised agents
    transient public ConcurrentHashMap<String, CodaRCAgent>
            myComponents = new ConcurrentHashMap<>();

    // Synchronized map holding agentReportingTimes
    transient ConcurrentHashMap<String, AReportingTime>
            myCompReportingTimes = new ConcurrentHashMap<>();

    // Sorted component list to be reported to UIs
    public Map<String, AComponent>
            sortedComponentList =
            Collections.synchronizedMap(
                    new LinkedHashMap<>());

    // Sorting components in the order of
    // being allowed to write an output file
    transient Map<String, AComponent>
            sortedByOutputList =
            Collections.synchronizedMap(
                    new LinkedHashMap<>());

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

    // Thread that runs a described service
    transient private ServiceExecutionT serviceExecutionThread;


    transient public SimpleDateFormat startEndFormatter =
            new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    transient public AtomicBoolean activeServiceExec =
            new AtomicBoolean(false);

    transient AtomicBoolean haveCoda2Component =
            new AtomicBoolean(false);

    // Object references
    // Reference to the COOL service analyser object
    transient public CoolServiceAnalyser coolServiceAnalyser;

    // Provides utility methods
    transient public SUtility sUtility;

    // Reference to this supervisor
    private SupervisorAgent mySelf;


    // CODA rc specific fields
    public long timeLimit;
    public int _numberOfRuns;
    public long absTimeLimit;
    transient public AtomicBoolean autoStart =
            new AtomicBoolean(false);

    // Persistent and trigger component agent objects
    private AComponent persistencyComponent;
    private CodaRCAgent triggerComponent;

    // Flag that is set by the request of the gui to
    // enable/disable data file output
    public String s_fileWriting = "enabled";

    // private variable that stores requested service
    // (for e.g. CodaRcDownload, CodaRcStartRun, etc.)
    transient private String _requestedService = AConstants.udf;

    // flag indicating if disconnected component
    // was detected during the active state.
    transient public AtomicBoolean isClientProblemAtActive = new AtomicBoolean(false);


    transient public boolean wasConfigured = false;

    transient private ScheduledFuture<?> agnetsCheck;

    transient private ExecutorService es;

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
    public SupervisorAgent(AComponent comp, AContainer container) {
        super(comp, container, container.myPlatform);
        coolServiceAnalyser = new CoolServiceAnalyser(this);
        sUtility = new SUtility(this);
        me.setExpid(myConfig.getPlatformExpid());

        // basic subscriptions
        basicSubscribe();

        AfecsTool.sleep(1000);

        wasConfigured = false;

        mySelf = this;
        // start influxDB injector thread
        if (myPlatform.influxDb) {
            new Thread(() -> {
                try {
                    new InfluxInjector(this,true);
                } catch (JinFluxException e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }

    /**
     * <p>
     * Method for gracefully exiting this agent
     * by stopping all running threads and
     * removing subscriptions.
     * </p>
     */
    private void sup_exit() throws cMsgException {
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
    private void sup_clean() {
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

        // Asking all supervised agents to configure
        //        for (CodaRCAgent com : myComponents.values()) {
        myComponents.values().parallelStream().forEach(CodaRCAgent::agentControlRequestSetup);
//        }
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
    private void _moveToState(String stateName) {

// ======== added 10.11.16 ============== Execute a reset attached script after active state ====================
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

        // If required state is "reseted" set the
        // state of this supervisor configured.
        if (stateName.equals(AConstants.reseted) ||
                stateName.equals(AConstants.emergencyreset)) {
            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                    myName,
                    1,
                    AConstants.
                            INFO,
                    " " + stateName + " is started.");

            // Ask all components to set their run-numbers
            for (CodaRCAgent cn : myComponents.values()) {
                cn._moveToState(AConstants.reseted);
            }

            // stop periodic processes
            stopPeriodicProcesses();

            for (AProcess bp : me.getProcesses()) {
                if (bp != null) {
                    if (bp.getBefore() != null && !bp.getBefore().equals(AConstants.udf)) {

                        if (bp.getBefore().equals(AConstants.reseted)) {
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
            for (CodaRCAgent s : myComponents.values()) {
                s.agentControlRequestReleaseAgent();
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
    public ConcurrentHashMap<String, CodaRCAgent> getMyComponents() {
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
            e.printStackTrace();
        }

    }

    /**
     * <p>
     * Supervisor agent specific subscriptions
     * </p>
     */
    void agentSubscribe() {

        if (agnetsCheck != null) {
            agnetsCheck.cancel(true);
        }
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        Runnable check = this::checkComponents;

        agnetsCheck = executorService.scheduleAtFixedRate(
                check,
                0,
                1, TimeUnit.SECONDS);
    }

    /**
     * <p>
     * Un-subscribes supervisor agent
     * specific subscriptions.
     * </p>
     */
    private void _un_subscribe_all() {
        if (agnetsCheck != null) {
            agnetsCheck.cancel(true);
        }

        try {
            if (isPlatformConnected()) {
                if (superControlSH != null)
                    myPlatformConnection.unsubscribe(superControlSH);
                if (userRequestSH != null)
                    myPlatformConnection.unsubscribe(userRequestSH);
                if (controlSH != null)
                    myPlatformConnection.unsubscribe(controlSH);
            }
        } catch (cMsgException e) {
            e.printStackTrace();
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
    private void startAgentMonitors() {

        // Stop first
        stopAgentMonitors();

        agentStatusReport = new AStatusReportT(this);
        agentStatusReport.start();
    }

    /**
     * <p>
     * Stops agent monitoring threads
     * in case they are running.
     * </p>
     */
    private void stopAgentMonitors() {
        if (agentStatusReport != null)
            agentStatusReport.setRunning(false);
    }

    /**
     * <p>
     * Stops service execution thread
     * </p>
     */
    private void stopServiceExecutionThread() {
        if (es != null && !es.isTerminated() && !es.isShutdown()) {
            es.shutdownNow();
        }
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
            es = Executors.newSingleThreadExecutor();
            es.submit(serviceExecutionThread);

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
    private void codaRC_stopRun() {
        if (myServiceConditions.containsKey("CodaRcEnd")) {

            serviceExecutionThread =
                    new ServiceExecutionT(mySelf, "CodaRcEnd");
            es = Executors.newSingleThreadExecutor();
            es.submit(serviceExecutionThread);

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
            for (CodaRCAgent c : myComponents.values()) {
                statuses.add(c.me.getState());
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
        for (CodaRCAgent c : myComponents.values()) {
            if (!c.me.getState().equals(AConstants.downloaded)) {
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
    public String[] defineDestinationNames() {
        AComponent c = sortedByOutputList.entrySet().iterator().next().getValue();
        return c.getDestinationNames();
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

        Map<String, String> remRtv = AfecsTool.readRTVFile(myRunType, mySession);

        StringBuilder sb = new StringBuilder();
        sb.append("<coda runtype = ").append("\"").append(myRunType).append("\"").
                append(" session = ").append("\"").append(mySession).append("\"").
                append(">").append("\n");
        sb.append("   <run-start>").append("\n");
        sb.append("      <run-number>").append(me.getRunNumber()).append("</run-number>").append("\n");
        sb.append("      <start-time>").append(me.getRunStartTime()).append("</start-time>").append("\n");

        if(me.getDestinationNames()!=null) {
            for (String fileName : me.getDestinationNames()) {
                sb.append("      <out-file>").append(fileName).append("</out-file>").append("\n");
            }
        }
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
        for (CodaRCAgent comp : getMyComponents().values()) {
            sb.append("         <component name = ").append("\"").append(comp.me.getName()).append("\"").
                    append(" type = ").append("\"").append(comp.me.getType()).append("\"").
                    append(">").append("\n");
            sb.append("            <evt-rate>").append(comp.me.getEventRateAverage()).append("</evt-rate>").append("\n");
            sb.append("            <data-rate>").append(comp.me.getDataRateAverage()).append("</data-rate>").append("\n");
            sb.append("            <evt-number>").append(comp.me.getEventNumber()).append("</evt-number>").append("\n");
            sb.append("            <min-evt-size>").append(comp.me.getMinEventSize()).append("</min-evt-size>").append("\n");
            sb.append("            <max-evt-size>").append(comp.me.getMaxEventSize()).append("</max-evt-size>").append("\n");
            sb.append("            <average-evt-size>").append(comp.me.getAvgEventSize()).append("</average-evt-size>").append("\n");
            sb.append("            <min-evt-build-time>").append(comp.me.getMinTimeToBuild()).append("</min-evt-build-time>").append("\n");
            sb.append("            <max-evt-build-time>").append(comp.me.getMaxTimeToBuild()).append("</max-evt-build-time>").append("\n");
            sb.append("            <average-evt-build-time>").append(comp.me.getMeanTimeToBuild()).append("</average-evt-build-time>").append("\n");
            sb.append("            <chunk-x-et-buffer>").append(comp.me.getChunkXEtBuffer()).append("</chunk-x-et-buffer>").append("\n");


            if(comp.me.getDestinationNames()!=null) {
                for (String cFileName : comp.me.getDestinationNames()) {
                    sb.append("            <out-file>").append(cFileName).append("</out-file>").append("\n");
                }
            }

            sb.append("         </component>").append("\n");
        }

    }

    public void requestPopUpDialog(String text) {
        ArrayList<cMsgPayloadItem> al = new ArrayList<>();
        try {
            al.add(new cMsgPayloadItem("MSGCONTENT", text));

        } catch (cMsgException e) {
            e.printStackTrace();
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
                        for (CodaRCAgent c : myComponents.values()) {
                            if (c.me.getType().equals(ACodaType.ER.name())) {
                                System.out.println("DDD ----| Info: setting fileWriting = " +
                                        s_fileWriting + " for component = " + c.me.getName());
                                c.agentControlRequestSetFileWriting(s_fileWriting);
                            }
                        }
                        break;

                    case "PEB_class":
                        for (CodaRCAgent c : myComponents.values()) {

                            if (c.me.getType().equals(ACodaType.PEB.name())) {
                                c.agentControlRequestSetFileWriting(s_fileWriting);
                            }
                        }
                        break;

                    case "SEB_class":
                        for (CodaRCAgent c : myComponents.values()) {

                            if (c.me.getType().equals(ACodaType.SEB.name())) {
                                c.agentControlRequestSetFileWriting(s_fileWriting);
                            }
                        }
                        break;
                }
            } else {
                CodaRCAgent c = myComponents.get(persistencyComponent.getName());
                if (c != null) {
                    c.agentControlRequestSetFileWriting(s_fileWriting);
                }
            }
        }
    }

    public void supervisorControlRequestFailTransition() {
        send(AConstants.GUI,
                me.getSession() + "_" + me.getRunType() + "/supervisor",
                me.getRunTimeDataAsPayload());
        reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                myName,
                11,
                AConstants.ERROR,
                " transition failed.");
        dalogMsg(myName,
                11,
                AConstants.ERROR,
                " transition failed.");

        // Ask components to reset
        _moveToState(AConstants.reseted);
    }

    public void supervisorControlRequestReleaseAgent(String requestData) {
        if (myComponents.containsKey(requestData)) {
            myComponents.get(requestData).me.setState(AConstants.removed);
            me.setState(AConstants.booted);
            send(AConstants.GUI,
                    me.getSession() + "_" + me.getRunType() + "/supervisor",
                    me.getRunTimeDataAsPayload());
        }
        if (myComponents.containsKey(requestData)) {
            myComponents.get(requestData).me.setState(AConstants.removed);
        }
    }

    private void runPauseScript(){
        for (AProcess bp : me.getProcesses()) {
            if (bp != null) {
                if (bp.getBefore() != null && !bp.getBefore().equals(AConstants.udf)) {

                    if (bp.getBefore().equals("paused")) {
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
    }

    private void runResumeScript(){
        for (AProcess bp : me.getProcesses()) {
            if (bp != null) {
                if (bp.getBefore() != null && !bp.getBefore().equals(AConstants.udf)) {

                    if (bp.getBefore().equals("resumed")) {
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
                                e.printStackTrace();
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
                            es = Executors.newSingleThreadExecutor();
                            es.submit(serviceExecutionThread);

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

                case AConstants.SupervisorControlRequestReleaseRunType:
                    _un_subscribe_all();
                    stopAgentMonitors();
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

                // Change the run number in the cool db if we
                // are not active or not in the middle of a transition
                case AConstants.SupervisorControlRequestSetRunNumber:

                    if (!(me.getState().equals(AConstants.active) ||
                            me.getState().equals(AConstants.prestarted) ||
                            me.getState().endsWith("ing"))) {

                        if (msg.getPayloadItem(AConstants.RUNNUMBER) != null) {

                            int rn = 0;
                            try {
                                rn = msg.getPayloadItem(AConstants.RUNNUMBER).getInt();
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                            myPlatform.platformRegistrationRequestSetRunNum(me.getSession(), me.getRunType(), rn);
                        }
                    }
                    break;

                // The following is coda run control specific
                // request such as setting run-control limits.
                case AConstants.SupervisorControlRequestSetEventLimit:
                    try {
                        int i = Integer.parseInt(requestData);
                        if (i >= 0) {
                            me.setEventLimit(i);
                            // Ask platform registrar agent to update cool
                            myPlatform.registrar.updateOptionsEventLimit(me.getRunType(), me.getEventLimit());
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;

                case AConstants.SupervisorControlRequestResetEventLimit:
                    me.setEventLimit(0);
                    myPlatform.registrar.updateOptionsEventLimit(me.getRunType(), me.getEventLimit());
                    break;

                case AConstants.SupervisorControlRequestSetDataLimit:
                    try {
                        long i = Long.parseLong(requestData);
                        if (i >= 0) {
                            me.setDataLimit(i);

                            // Ask platform registrar agent to update cool
                            myPlatform.registrar.updateOptionsEventLimit(me.getRunType(), me.getEventLimit());
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    break;

                case AConstants.SupervisorControlRequestResetDataLimit:
                    me.setDataLimit(0);
                    myPlatform.registrar.updateOptionsEventLimit(me.getRunType(), me.getEventLimit());
                    break;

                case AConstants.SupervisorControlRequestSetTimeLimit:
                    try {
                        int i = Integer.parseInt(requestData);
                        if (i > 0) {
                            absTimeLimit = i * 60 * 1000;
                            me.setTimeLimit(absTimeLimit);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
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
                        e.printStackTrace();
                    }
                    break;

                case AConstants.SupervisorControlRequestResetNumberOfRuns:
                    _numberOfRuns = 0;
                    me.setnScheduledRuns(_numberOfRuns);
                    break;

                case AConstants.SupervisorControlRequestPause:
                    // send to all agents pause command
                    myComponents.values().parallelStream().forEach(CodaRCAgent::pause);
                    // triggerComponent.pause();

                    // execute a process attached to the pause
                    runPauseScript();

                    // change my state to paused
                    me.setState(AConstants.paused);
                    send(AConstants.GUI,
                            me.getSession() + "_" + me.getRunType() + "/supervisor",
                            me.getRunTimeDataAsPayload());
                    break;

                case AConstants.SupervisorControlRequestResume:
                    // send to all agents resume command
                    myComponents.values().parallelStream().forEach(CodaRCAgent::resume);
                    // triggerComponent.resume();

                    // execute a process attached to the resume
                    runResumeScript();

                    // change my state to active
                    me.setState(AConstants.active);
                    send(AConstants.GUI,
                            me.getSession() + "_" + me.getRunType() + "/supervisor",
                            me.getRunTimeDataAsPayload());
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

        UserRequestServiceThread(cMsgMessage msg) {
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
                                for (CodaRCAgent comp : myComponents.values()) {
                                    sb.append(comp.me.getName()).
                                            append(" ").
                                            append(comp.me.getState()).
                                            append("\n");
                                }
                                mr.setText(sb.toString());
                                break;

                            case AConstants.SupervisorReportComponentEventNumber:
                                tmpCmp = myComponents.get(txt).me;
                                if (tmpCmp != null) {
                                    mr.setText(Long.toString(tmpCmp.getEventNumber()));
                                }
                                break;

                            case AConstants.SupervisorReportComponentEventRate:
                                tmpCmp = myComponents.get(txt).me;
                                if (tmpCmp != null) {
                                    mr.setText(Float.toString(tmpCmp.getEventRate()));
                                }
                                break;

                            case AConstants.SupervisorReportComponentDataRate:
                                tmpCmp = myComponents.get(txt).me;
                                if (tmpCmp != null) {
                                    mr.setText(Double.toString(tmpCmp.getDataRate()));
                                }
                                break;

                            case AConstants.SupervisorReportComponentState:
                                tmpCmp = myComponents.get(txt).me;
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
                                for (CodaRCAgent comp : myComponents.values()) {
                                    l.add(comp.me.getName() + " " + comp.me.getState());
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
                                tmpCmp = myComponents.get(txt).me;
                                if (tmpCmp != null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("evtnumber_p", tmpCmp.getEventNumber()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportComponentOutputFile_p: {
                                tmpCmp = myComponents.get(txt).me;
                                if (tmpCmp != null && tmpCmp.getDestinationNames()!=null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("outputfile_p", tmpCmp.getDestinationNames()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportComponentEventRate_p: {
                                tmpCmp = myComponents.get(txt).me;
                                if (tmpCmp != null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("compevtrate_p", tmpCmp.getEventRate()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportComponentDataRate_p: {
                                tmpCmp = myComponents.get(txt).me;
                                if (tmpCmp != null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("compdatarate_p", tmpCmp.getDataRate()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportComponentLiveTime_p: {
                                tmpCmp = myComponents.get(txt).me;
                                if (tmpCmp != null) {
                                    mr.addPayloadItem(new cMsgPayloadItem("complivetime_p", tmpCmp.getLiveTime()));
                                }
                                break;
                            }

                            case AConstants.SupervisorReportComponentState_p: {
                                tmpCmp = myComponents.get(txt).me;
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
                                if (triggerComponent != null) mr.setText(triggerComponent.me.getName());
                                else mr.setText(AConstants.udf);
                                break;

                            default:
                                break;
                        }

                        myPlatformConnection.send(mr);
                    } catch (cMsgException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void supervisorControlRequestSetup(AControl control) {
        wasConfigured = false;
        if (control != null) {
            // differentiate supervisor
            differentiate(control.getSupervisor());

            System.out.println("DDD -----| Info: " + myName + " setting up control system.");
            // supervisor setup
            _setup(control);
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

                if (msg.isGetRequest()) {
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
                    case AConstants.AgentControlRequestPlatformDisconnect: // not used
                        try {
                            sup_exit();
                        } catch (cMsgException e) {
                            e.printStackTrace();
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

                    case AConstants.AgentControlRequestSetRunNumber: // not used ?
                        if (msg.getPayloadItem(AConstants.RUNNUMBER) != null) {
                            try {
                                int rn = msg.getPayloadItem(AConstants.RUNNUMBER).getInt();
                                me.setRunNumber(rn);
                                if (msg.getPayloadItem("ForAgentOnly") == null) {
                                    runControlSetRunNumber(rn);
                                }
                            } catch (cMsgException e) {
                                e.printStackTrace();
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
                            e.printStackTrace();
                        }
                        break;
                }
            } else {
                System.out.println("DDD ----| Warning: message is NULL");
                me.setState(AConstants.booted);
            }
        }
    }


    private void checkComponents() {
        for (CodaRCAgent ac : myComponents.values()) {
            sortedComponentList.put(ac.me.getName(), ac.me);
            if (myCompReportingTimes.containsKey(ac.me.getName())) {
                myCompReportingTimes.get(ac.me.getName()).setCurrentReportingTime(new Date().getTime());
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
                } else if (ac.me.getName().equals(persistencyComponent.getName())) {

                    // Set supervisor event number
                    me.setEventNumber(ac.me.getEventNumber());
                    me.setNumberOfLongs(ac.me.getNumberOfLongs());

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
