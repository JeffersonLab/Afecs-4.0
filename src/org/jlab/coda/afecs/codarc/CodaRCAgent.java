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

package org.jlab.coda.afecs.codarc;

import org.jlab.coda.afecs.agent.AParent;
import org.jlab.coda.afecs.client.AClientInfo;
import org.jlab.coda.afecs.container.AContainer;
import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.cool.ontology.APackage;
import org.jlab.coda.afecs.cool.ontology.AProcess;
import org.jlab.coda.afecs.cool.ontology.AState;
import org.jlab.coda.afecs.fcs.FcsEngine;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


/**
 * <p>
 * CODA run-control component agent. Contains the
 * following subscriptions for messages coming from
 * the CODA component, represented by this agent
 * through the RC domain:
 * <ul>
 * <li> status </li>
 * <li> daLog </li>
 * <li> rc/response </li>
 * </ul>
 * As well as cMsg domain subscriptions
 * for messages, such as:
 * <ul>
 * <li> agent control </li>
 * <li>
 * EMU reports about the
 * ET buffer levels
 * </li>
 * </ul>
 * <p>
 * This class also creates threads for:
 * <ul>
 * <li>monitoring client's health</li>
 * <li>
 * monitoring client's transitioning
 * to a required state
 * </li>
 * </ul>
 * </p>
 * </p>
 *
 * @author gurjyan
 * Date: 11/11/14 Time: 2:51 PM
 * @version 4.x
 */
public class CodaRCAgent extends AParent {

    // Subscription handler for the status
    // messages coming from the coda component
    private cMsgSubscriptionHandle statusSH;

    // Subscription handler for the daLog
    // messages coming from the coda component
    private cMsgSubscriptionHandle daLogSH;

    // Subscription handler for the type =
    // rc/response messages coming from the
    // coda component
    private cMsgSubscriptionHandle responseSH;

    // Subscription handler for cMsg domain
    // subscription that subscribes to EMU
    // requesting client to set events per ET
    // buffer level.
    private cMsgSubscriptionHandle emuEventsPerEtBufferLevel;

    // Used to calculate cumulative moving average
    // for event rate and data rate
    private static int AVERAGE_SIZE = 10;
    long averageCount;
    ArrayList<Float> eventRateSlide = new ArrayList<>();
    ArrayList<Double> dataRateSlide = new ArrayList<>();

    // Used to calculate integrated average
    long startTime;


    // The message received time from the client
    AtomicLong
            clientLastReportedTime = new AtomicLong(0);

    // If this agent is configured as an FCS
    // we need to create FcsEngine object.
    private FcsEngine fcsEngine;

    // State transition monitor executor
    private ExecutorService es;

    //client heart beat monitor
    boolean clientHeartBeatIsRunning;

    /**
     * <p>
     * Constructor calls the parent constructor that will
     * create cMsg domain connection object and register
     * this agent with the platform registration services.
     * The parent will also provide a subscription to the
     * agent info request messages, as well as status
     * reporting thread. For more information:
     * {@link AParent}
     * </p>
     *
     * @param comp Reference to the component object that is
     *             going to be represented by this agent.
     */
    public CodaRCAgent(AComponent comp, AContainer container) {
        super(comp, container, container.myPlatform);
        boolean b = _connect2Client();
        // if client connection is successful ask platform
        // registration service to register the client
        // This will write a record in the cool_home/ddb/clientRegistration.xml
        if (b) {

            // Check to see if any other DB registered client
            // share the same host and port, remove if there is.
            myPlatform.registrar.checkDeleteClientDB(
                    me.getClient().getName(),
                    me.getClient().getHostName(),
                    me.getClient().getPortNumber());

            if (myPlatform.registrar.getClientDir().containsKey(me.getClient().getName())) {
                myPlatform.registrar.getClient(
                        me.getClient().getName()).setHostName(me.getClient().getHostName());
                myPlatform.registrar.getClient(
                        me.getClient().getName()).setPortNumber(me.getClient().getPortNumber());
                myPlatform.registrar.getClient(
                        me.getClient().getName()).setContainerHost(me.getClient().getContainerHost());
            } else {

                // This is the first JoinThePlatform request
                myPlatform.registrar.addClient(me.getClient());
            }
        }
        // Open a file in the CoolHome and
        // write ClientDir hash table content
        myPlatform.registrar.dumpClientDatabase();

        // Subscribe control messages to this agent
        me.setState(AConstants.booted);

    }

    public void updateComponent(AComponent component) {
        me = component;
    }


    /**
     * <p>
     * Stops all running threads and
     * subscriptions except agent
     * control subscription.
     * </p>
     */
    public void _stopCommunications() {
        // Stops status reporting thread and
        // active periodic processes (parent method)
        stop_rpp();

        // first stop dangling transition monitor threads if any
        _stopStateTransitioningMonitor();

        // un-subscribe coda client information messages
        _codaClientUnSubscribe();
    }

    /**
     * <p>
     * Sets the client last reported
     * event and  data rates to 0
     * </p>
     */
    void resetClientData() {
        me.setEventRate(0);
        me.setDataRate(0);
    }

    /**
     * <p>
     * Checks to see if agent has a proper connection
     * to its representing client. If it has:
     * <ul>
     * <li>
     * If the real world client is of the
     * CODA type = FCS, creates an instance
     * of the farm control supervisor engine
     * that knows what to do for specific
     * CODA transitions.
     * </li>
     * <li>
     * Every time client boots up this method
     * will be called. So, check if the state
     * is connected (previously dead client was
     * considered disconnected) report that client
     * is restarted.
     * </li>
     * <li>
     * Subscribes client status messages.
     *
     * @see AConstants#RcReportStatus
     * </li>
     * <li>
     * Subscribes client response messages.
     * @see AConstants#RcResponse
     * </li>
     * <li>
     * Subscribes client daLog messages.
     * @see AConstants#RcReportDalog
     * </li>
     * <li>
     * Starts status reporting thread,
     * that will report periodically to
     * the supervisor agent.
     * </li>
     * <li> Tells the client to reset</li>
     * <li>
     * Starts the client health monitoring
     * thread. This Thread class will also
     * tell client to start reporting.
     * </li>
     * <li> Tells the client about the configured
     * session and the runType
     * </li>
     * <li>
     * And finally tels the client to move
     * to the state = configure
     * </li>
     * </ul>
     * </p>
     */
    private void _setClientCommunications() throws AException {

        if (isRcClientConnected() && me.getClient() != null) {

            // FCS case
            if (me.getType().equals(ACodaType.FCS.name())) {
                if (fcsEngine == null) fcsEngine = new FcsEngine(this);
            }

            // See if this is reconnect request
            if (me.getState().equals(AConstants.connected)) {
                reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                        myName, 5,
                        AConstants.WARN,
                        " Client is restarted.");
            }

            // Subscribe client information
            _codaClientSubscribe();

            // move to reset state
            boolean b = _moveToState(AConstants.reseted);
            if (!b) {
                throw new AException("Reset failed");
            }

            // Sleep for a 0.5 sec before issuing configure
            AfecsTool.sleep(500);

            try {
                // Ask representing client to start reporting
                sessionControlStartReporting();

                // tell client linked component network information
                for (String com : me.getLinkedComponentNames()) {

                    if (myContainer.getContainerAgents().containsKey(com)) {
                        myContainer.getContainerAgents().get(com).agentControlRequestNetworkDetails(
                                myName,
                                me.getClient().getHostIps(),
                                me.getClient().getHostBroadcastAddresses());
                    }
                }

            } catch (cMsgException e) {
                e.printStackTrace();
            }

            // Start agent health watching thread.
            // This tells client to start reporting
            _startClientHeartBeatMonitor();

            // Tell client about the session and the runType.
            try {
                sessionControlSetSession(me.getSession());
                runControlSetRunType(me.getRunType());
            } catch (cMsgException e) {
                e.printStackTrace();
            }

        } else {
            throw new AException(" Not connected to the client. Possible restart/power-cycle of the client might be required.");
        }
    }

    FcsEngine getFcsEngine() {
        return fcsEngine;
    }

    /**
     * <p>
     * Restarts client communications and
     * does reset and configure.
     *
     * @see #_setClientCommunications()
     * </p>
     */
    private boolean _setup() {

        boolean stat = true;
        _stopCommunications();
        try {
            _setClientCommunications();
        } catch (AException e) {
            e.printStackTrace();
            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                    me.getName(),
                    11,
                    AConstants.ERROR,
                    e.getMessage());
            dalogMsg(me.getName(),
                    11,
                    AConstants.ERROR,
                    e.getMessage());

            me.setState(AConstants.failed);
            stat = false;
//            send(me.getSession(), me.getRunType(), me);

            // finally check to see if we lost connection
            // with the client and set the state = disconnected.
            if (!isRcClientConnected()) {
                me.setState(AConstants.disconnected);
                stat = false;
//                send(me.getSession(), me.getRunType(), me);
            }
        }
        return stat;
    }

    /**
     * <p>
     * Main method of this agent for transitioning
     * to a required state. Utilizes parent's
     * method.
     * Does substitution of RTV = %(rn) with run
     * number in all COOL configured processes.
     * This substitution is done for prestarted,
     * active and ended CODA transitions.
     * Starts state transition monitoring thread
     * to wait for client to transition to a
     * required state.
     * <p>
     * </p>
     *
     * @param stateName the name of the state
     * @return true if succeeded
     */
    public boolean _moveToState(String stateName) {
        boolean b;

        System.out.println("DDD ----| Info: " + myName + " requested to transition to " + stateName);
        // First stop dangling transition
        // monitor threads if any
        _stopStateTransitioningMonitor();

        if (stateName.equals(AConstants.reseted)) {
            // Move to reset state
            b = transition(stateName);
            _reset();
        } else {

            // Reset rates if not request to end the run
            if (!stateName.equals(AConstants.ended)) {
                me.setEventRateAverage(0.0f);
                me.setDataRateAverage(0.0);
            } else {
                // if the request is to end we stop periodic processes
                stopPeriodicProcesses();
            }

            // Move to required state
            b = transition(stateName);

            // Start a new thread to monitor
            // state transitioning process
            _startStateTransitioningMonitor(stateName);
        }
        return b;
    }

    public void pause(){
        try {
            rcSend(me.getName(),
                    "run/transition/pause",
                    "pause",null);
        } catch (cMsgException e) {
            e.printStackTrace();
        }
    }
    public void resume(){
        try {
            rcSend(me.getName(),
                    "run/transition/resume",
                    "resume", null);
        } catch (cMsgException e) {
            e.printStackTrace();
        }
    }
    /**
     * <p>
     * Client reset method. Stops client health
     * and transitioning monitoring threads.
     * </p>
     */
    private void _reset() {
        isResetting.set(true);

//        AfecsTool.sleep(1000);
//        _getClientState(AConstants.TIMEOUT, 1000);

        stopPeriodicProcesses();

        // Stop transitioning monitoring thread.
        _stopStateTransitioningMonitor();


    }

    /**
     * <p>
     * Synchronously ask the state of the client.
     * If communication with the client is broken
     * set the state of this agent = disconnected
     * and stop all communications with the client.
     * </p>
     *
     * @param timeout int describing time in
     *                seconds required to wait
     *                for the response
     * @return state of the client
     */
    public String _getClientState(int timeout, int stepTimeout) {
        String tmp;
        String status = AConstants.udf;

        int to = timeout / stepTimeout;
        for (int i = 0; i < to; i++) {
            try {

                System.out.println("DDD -----| Info: " + AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                        myName + ": --> rc_syncGetState time=" + stepTimeout);

                tmp = rcClientInfoSyncGetState(stepTimeout);
                if (tmp != null && !tmp.equals(AConstants.failed)) {
                    status = tmp;
                    break;
                }

                System.out.println("DDD -----| Info: " + AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                        myName + ": <-- rc_syncGetState = " + tmp);
            } catch (AException e) {
                if (e.getMessage() != null && e.getMessage().trim().equals("Broken pipe")) break;
            }
        }
        if (status.equals(AConstants.udf)) {
            // Stop client communications  and disconnect
            me.setState(AConstants.disconnected);
//            send(me.getSession(), me.getRunType(), me);
            _stopCommunications();
            rcClientDisconnect();
        }
        return status;
    }

    /**
     * <p>
     * Returns the expected response strings described in
     * the cool state descriptions. N.B. for the coda run
     * control expected state name is described as the
     * received package text. If there are more then one
     * process described to be executed at least one must
     * have a return text described, or all must return
     * the same text. This text is going to be assigned as
     * a state name for the client representing agent.
     * </p>
     *
     * @param requiredState actual state name to be transitioned
     * @return the response string
     */
    ArrayList<String> getStateRequiredClientResponse(String requiredState) {
        ArrayList<String> expectedResponses = new ArrayList<>();

        for (AState st : me.getStates()) {
            if (st.getName().equals(requiredState)) {

                // for all processes necessary to be executed
                // to achieve the required state
                for (AProcess pr : st.getProcesses()) {
                    for (APackage pk : pr.getReceivePackages()) {
                        if (pk.getReceivedText() != null) {
                            expectedResponses.addAll(pk.getReceivedText());
                        }
                    }
                }
                break;
            }
        }
        return expectedResponses;
    }


    /**
     * <p>
     * Calculates average values
     * for event rate and data rate
     * </p>
     */
    private void _calculateSlidingAverages() {

        if (me.getEventNumber() != 0 &&
                me.getDataRate() >= 0 &&
                me.getEventRate() >= 0) {

            if (averageCount < AVERAGE_SIZE) {
                averageCount++;
                eventRateSlide.add(me.getEventRate());
                dataRateSlide.add(me.getDataRate());
                me.setEventRateAverage(me.getEventLimit());
                me.setDataRateAverage(me.getDataRate());
            } else {

                // average event rate
                if (eventRateSlide.size() > 0) {
                    eventRateSlide.remove(0);
                    eventRateSlide.add(me.getEventRate());

                    //calculate average
                    float es = 0F;
                    for (Float f : eventRateSlide) {
                        es += f;
                    }
                    me.setEventRateAverage(es / AVERAGE_SIZE);
                }


                // average data rate
                if (dataRateSlide.size() > 0) {
                    dataRateSlide.remove(0);
                    dataRateSlide.add(me.getDataRate());

                    //calculate average
                    double ds = 0D;
                    for (Double d : dataRateSlide) {
                        ds += d;
                    }
                    me.setDataRateAverage(ds / AVERAGE_SIZE);
                }
            }
        }
    }

    /**
     * <p>
     * Calculates integrated average values
     * for event rate and data rate
     * </p>
     */
    private void _calculateIntegratedAverages() {
        long currentTime = System.currentTimeMillis();

        long time = currentTime - startTime;
        me.setEventRateAverage((me.getEventNumber() / (float)time) * 1000F);
        me.setDataRateAverage((me.getNumberOfLongs() / (double)time) * 4.0);

/*
        try {
            Math.addExact(me.getEventNumber(), 0);
            me.setEventRateAverage((float)(me.getEventNumber() / time));
        } catch (ArithmeticException e){
            BigInteger Bx = BigInteger.valueOf(me.getEventNumber());
            me.setEventRateAverage(Bx.divide(BigInteger.valueOf(time)).floatValue());
        }

        try {
            Math.addExact(me.getNumberOfLongs(), 0);
            me.setDataRateAverage((double)me.getNumberOfLongs() / time);
        } catch (ArithmeticException e){
            BigInteger Bx = BigInteger.valueOf(me.getNumberOfLongs());
            me.setDataRateAverage(Bx.divide(BigInteger.valueOf(time)).doubleValue());
        }
*/
     }

    /**
     * <p>
     * Subscribes using RC domain to client messages, such as:
     * <ul>
     * <li>
     * Subscribes client status messages.
     *
     * @see AConstants#RcReportStatus
     * </li>
     * <li>
     * Subscribes client response messages.
     * @see AConstants#RcResponse
     * </li>
     * <li>
     * Subscribes client daLog messages.
     * @see AConstants#RcReportDalog
     * </li>
     * </ul>
     * Asks platform to return the linked emu name and
     * subscribes emu direct messages, informing this
     * agent the number events per et buffer.
     * Note that this agent must represent ROC for this
     * subscription to be valid.
     * </p>
     */
    private void _codaClientSubscribe() {
        if (isRcClientConnected()) {
            try {
                // subscribe client messages
                statusSH = myCRCClientConnection.subscribe(myName,
                        AConstants.RcReportStatus,
                        new StatusMsgCB(),
                        null);
                startTime = System.currentTimeMillis();

                responseSH = myCRCClientConnection.subscribe(myName,
                        AConstants.RcResponse,
                        new ResponseMsgCB(),
                        null);
                daLogSH = myCRCClientConnection.subscribe(myName,
                        AConstants.RcReportDalog,
                        new DaLogMsgCB(),
                        null);

                // Ask platform for the linked emu name
                if (me.getType().equals(ACodaType.ROC.name()) ||
                        me.getType().equals(ACodaType.GT.name()) ||
                        me.getType().equals(ACodaType.FPGA.name()) ||
                        me.getType().equals(ACodaType.TS.name())) {
                    ArrayList<cMsgPayloadItem> l = new ArrayList<>();
                    try {
                        l.add(new cMsgPayloadItem(
                                AConstants.DEFAULTOPTIONDIRS,
                                me.getDod().toArray(new String[me.getDod().size()])));
                    } catch (cMsgException e) {
                        e.printStackTrace();
                    }

                    // Send the message to the platform and wait for 5sec.
                    cMsgMessage msg = null;
                    try {
                        msg = p2pSend(myConfig.getPlatformName(),
                                AConstants.PlatformInfoRequestGetRocDataLink,
                                me.getName() + ".dat",
                                l,
                                AConstants.TIMEOUT);
                    } catch (AException e) {
                        e.printStackTrace();
                    }

                    if (msg != null && msg.getText() != null) {
                        // Subscribe emu direct messages
                        String emu_name = msg.getText();
                        emuEventsPerEtBufferLevel =
                                myPlatformConnection.subscribe(
                                        emu_name,
                                        "eventsPerBuffer",
                                        new RocEvtPerBufferMsgCB(),
                                        null);
                    }
                }

            } catch (cMsgException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>
     * Stops client and linked emu subscriptions
     * </p>
     */
    private void _codaClientUnSubscribe() {
        if (isRcClientConnected()) {
            try {
                if (statusSH != null) myCRCClientConnection.unsubscribe(statusSH);
                if (responseSH != null) myCRCClientConnection.unsubscribe(responseSH);
                if (daLogSH != null) myCRCClientConnection.unsubscribe(daLogSH);
                if (emuEventsPerEtBufferLevel != null)
                    myPlatformConnection.unsubscribe(emuEventsPerEtBufferLevel);
            } catch (cMsgException e) {
                e.printStackTrace();
            }
        }
    }

    void restartClientStatusSubscription() {
        try {
            if (statusSH != null) myCRCClientConnection.unsubscribe(statusSH);
            // subscribe client messages
            statusSH = myCRCClientConnection.subscribe(myName,
                    AConstants.RcReportStatus,
                    new StatusMsgCB(),
                    null);
        } catch (cMsgException e) {
            e.printStackTrace();
        }

    }

    /**
     * <p>
     * Stops thread monitoring the client's
     * transitioning to a CODA state.
     * </p>
     */
    private void _stopStateTransitioningMonitor() {
        if (es != null && !es.isTerminated() && !es.isShutdown()) {
            es.shutdownNow();
        }
    }

    /**
     * <p>
     * Starts thread monitoring the client's
     * transitioning to a CODA state.
     * {@link StateTransitioningMonitor}
     * </p>
     */
    private void _startStateTransitioningMonitor(String stateName) {
        _stopStateTransitioningMonitor();

        StateTransitioningMonitor stateTransitionMonitor =
                new StateTransitioningMonitor(this, stateName);
        es = Executors.newSingleThreadExecutor();
        es.submit(stateTransitionMonitor);
    }

    private void _startClientHeartBeatMonitor() {
        _stopClientHeartBeatMonitor();
        ClientHeartBeatMonitor clientHealthMonitor =
                new ClientHeartBeatMonitor(this, 120000, 20000);
        clientHeartBeatIsRunning = true;
        clientHealthMonitor.start();
    }

    public void _stopClientHeartBeatMonitor() {
        clientHeartBeatIsRunning = false;
        AfecsTool.sleep(1000);
    }


    /**
     * <p>
     * Returns this agents component, as a result of
     * differentiation to a COOL described component
     * </p>
     *
     * @return reference to a AComponent object
     */
    public AComponent getAgentCOOLComponent() {
        return me;
    }

    /**
     * <p>
     * Connect to the physical client using rcDomain server
     * started and running in the physical world client.
     * This method uses the list of host IPs sent by the
     * client in the case client has multiple network
     * interface cards.
     * This is so called 3rd leg of the rcDomain connection.
     * </p>
     *
     * @return status of the connection
     */
    private boolean _connect2Client() {
        boolean stat;

        if (me != null && me.getClient() != null) {

            me.setState(AConstants.checking);

            if (me.getClient().getHostIps() != null &&
                    me.getClient().getHostIps().length > 0 &&
                    me.getClient().getPortNumber() > 0 &&
                    me.getClient().getHostBroadcastAddresses() != null &&
                    me.getClient().getHostBroadcastAddresses().length > 0 &&
                    me.getClient().getHostIps().length == me.getClient().getHostBroadcastAddresses().length
            ) {

                // Disconnect from the client if connected
                rcClientDisconnect();

                // Connect to the physical client
                String validHost;
                String udl = AConstants.udf;
                StringBuilder ul = new StringBuilder();
                for (int i = 0; i < me.getClient().getHostIps().length; i++) {
                    validHost = me.getClient().getHostIps()[i];
                    ul.append("rcs://");
                    ul.append(validHost);
                    ul.append(":");
                    ul.append(me.getClient().getPortNumber());
                    ul.append("/");
                    ul.append(me.getClient().getHostBroadcastAddresses()[i]);
                    ul.append(";");
                }
                if (ul.length() > 1) {
                    String tmp = ul.toString();
                    udl = tmp.substring(0, tmp.length() - 1);
                }

                if (!udl.equals(AConstants.udf)) {
                    if (!rcClientConnect(udl)) {
                        System.out.println(myName +
                                " Cannot connect to the physical component using udl = " +
                                udl);
                        reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                                me.getName(),
                                11,
                                AConstants.ERROR,
                                "Cannot connect to the physical component using udl = " +
                                        udl);
                        me.setState(AConstants.disconnected);
                        stat = false;
                    } else {

                        System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                                myName + ":Info - connected to the client " + udl);

                        stat = true;
                        me.setState(AConstants.connected);

                        // getString() returns host and port of a client that made a successful connection
                        String x = myCRCClientConnection.getInfo(null);
                        if (x != null) {
                            String[] tokens = x.split(":");
                            me.getClient().setHostName(tokens[0]);
                        }
                    }
                } else {
                    System.out.println(myName +
                            " Undefined client udl = " + udl);
                    reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                            me.getName(),
                            11,
                            AConstants.ERROR,
                            "Undefined client udl = " +
                                    udl);
                    me.setState(AConstants.disconnected);
                    stat = false;
                }

            } else {
                stat = false;
                System.out.println("Undefined host and port for the client.");
                reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                        me.getName(),
                        11,
                        AConstants.ERROR,
                        "Undefined host and port for the client.");
            }
        } else {
            stat = false;
        }
        return stat;
    }

    public void _reconnectResponse(AClientInfo cInfo) {

        boolean b = _connect2Client();
        // if client connection is successful ask platform
        // registration service to register the client
        // This will write a record in the cool_home/ddb/clientRegistration.xml
        if (b) {
            // Ask platform to register client
            myPlatform.platformControlRegisterClient(cInfo);
            // Differentiate agent
            differentiate(me);

            // Establish connections, reset
            // specific client  and configure
            _setup();

        } else {
            System.out.println("ERROR:" + cInfo.getName() + " can not complete connection to the client.");
        }

    }

    /**
     * <p>
     * Private inner class for responding to the status
     * messages from the RC client, represented by this
     * agent in the Afecs platform.
     * </p>
     */
    private class StatusMsgCB extends cMsgCallbackAdapter {

        public void callback(cMsgMessage msg, Object userObject) {

            if (msg != null) {

                // Define reporting time
                if (clientLastReportedTime != null) {
                    clientLastReportedTime.set(new Date().getTime());
                }

                // If we are not in a middle of transitioning to a CODA state
                // then update agent-component parameters based on the client
                // data.
                if (!isTransitioning.get()) {
                    try {
                        if (msg.getPayloadItem(AConstants.CODACLASS) != null) {
                            String tp = msg.getPayloadItem(AConstants.CODACLASS).getString().trim();
                            if (tp.equals(me.getType())) {
                                me.setType(tp);
                            }
                        }
                        if (msg.getPayloadItem(AConstants.OBJECTTYPE) != null)
                            me.setObjectType(msg.getPayloadItem(AConstants.OBJECTTYPE).getString());

                        if (msg.getPayloadItem(AConstants.STATE) != null) {
                            String st = msg.getPayloadItem(AConstants.STATE).getString();
                            me.setState(st);

                        }
                        if (msg.getPayloadItem(AConstants.EVENTNUMBER64) != null) {
                            me.setEventNumber(msg.getPayloadItem(AConstants.EVENTNUMBER64).getLong());
                        } else if (msg.getPayloadItem(AConstants.EVENTNUMBER) != null) {
                            int _evt_numner = msg.getPayloadItem(AConstants.EVENTNUMBER).getInt();
                            if (_evt_numner < 0){
                                long _l_evt_number = Integer.MAX_VALUE + (_evt_numner * (-1));
                                me.setEventNumber(_l_evt_number);
                            } else {
                                me.setEventNumber(msg.getPayloadItem(AConstants.EVENTNUMBER).getInt());
                            }
                        }

                        if (msg.getPayloadItem(AConstants.EVENTRATE) != null)
                            me.setEventRate(msg.getPayloadItem(AConstants.EVENTRATE).getFloat());

                        if (msg.getPayloadItem(AConstants.DATARATE) != null) {
                            me.setDataRate((msg.getPayloadItem(AConstants.DATARATE).getDouble() * 4.0) / 1000.0);

                        }
                        if (msg.getPayloadItem(AConstants.NUMBEROFLONGS) != null)
                            me.setNumberOfLongs(msg.getPayloadItem(AConstants.NUMBEROFLONGS).getLong());

                        if (msg.getPayloadItem(AConstants.LIVETIME) != null)
                            me.setLiveTime(msg.getPayloadItem(AConstants.LIVETIME).getFloat());

                        if (msg.getPayloadItem(AConstants.DESTINATIONNAMES) != null) {
                            me.setDestinationNames(msg.getPayloadItem(AConstants.DESTINATIONNAMES).getStringArray());
                        } else if (msg.getPayloadItem(AConstants.FILENAME) != null) {
                            me.setFileName(msg.getPayloadItem(AConstants.FILENAME).getString());
                        }

                        if (msg.getPayloadItem("minEventSize") != null)
                            me.setMinEventSize(msg.getPayloadItem("minEventSize").getInt());

                        if (msg.getPayloadItem("maxEventSize") != null)
                            me.setMaxEventSize(msg.getPayloadItem("maxEventSize").getInt());

                        if (msg.getPayloadItem("avgEventSize") != null)
                            me.setAvgEventSize(msg.getPayloadItem("avgEventSize").getInt());

                        if (msg.getPayloadItem("timeToBuild") != null) {
                            int[] d = msg.getPayloadItem("timeToBuild").getIntArray();
                            if (d.length > 5) {
                                me.setMinTimeToBuild(d[3]);
                                me.setMaxTimeToBuild(d[4]);
                                me.setMeanTimeToBuild(d[2]);
                            }
                        }

                        if (msg.getPayloadItem("chunk_X_EtBuf") != null) {
                            me.setChunkXEtBuffer(msg.getPayloadItem("chunk_X_EtBuf").getInt());
                        }

                        // input/output buffers
                        // Input buffers
                        if (msg.getPayloadItem("inputChanNames") != null && msg.getPayloadItem("inputChanLevels") != null) {
                            int[] values = msg.getPayloadItem("inputChanLevels").getIntArray();
                            String[] names = msg.getPayloadItem("inputChanNames").getStringArray();
                            me.getInBuffers().clear();
                            for (int i = 0; i < values.length; i++) {
                                me.getInBuffers().put(me.getName() + "." + names[i], values[i]);
                            }

                        }

                        // Output buffers
                        if (msg.getPayloadItem("outputChanNames") != null && msg.getPayloadItem("outputChanLevels") != null) {
                            int[] values = msg.getPayloadItem("outputChanLevels").getIntArray();
                            String[] names = msg.getPayloadItem("outputChanNames").getStringArray();
                            me.getOutBuffers().clear();
                            for (int i = 0; i < values.length; i++) {
                                me.getOutBuffers().put(me.getName() + "." + names[i], values[i]);
                            }

                        }

                        // check the output buffer level from the ROC
                        if (msg.getPayloadItem("outputLevel") != null) {
                            String v = msg.getPayloadItem("outputLevel").getString();
                            if (v.contains(".")) {
                                String[] value = v.split("\\.");
                                me.getOutBuffers().put(me.getName() + "." + value[0], Integer.valueOf(value[1]));
                            }
                        }

                    } catch (cMsgException e) {
                        e.printStackTrace();
                    }

                    // If we are in the active state calculate averages
                    if (me.getState().equals(AConstants.active)) {
                        _calculateIntegratedAverages();
                    }
                }
            }

        }
    }

    /**
     * <p>
     * Private inner class for responding to the daLog
     * messages from the RC client, represented by this
     * agent in the Afecs platform.
     * Client's daLog message contains following payload
     * items with proper names:
     * <ul>
     * <li>codaid: component ID (int 0-255)</li>
     * <li>runType: config ID (int)</li>
     * <li>severity: severity text (INFO, WARN, ERROR, SERROR)</li>
     * <li>state: client state</li>
     * <li>codaClass: client type (ROC, EB, etc.)</li>
     * </ul>
     * </p>
     */
    private class DaLogMsgCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {

                String severityString = AConstants.udf;
                try {

                    if (msg.getPayloadItem(AConstants.DALOGSEVERITY) != null) {
                        severityString = msg.getPayloadItem(AConstants.DALOGSEVERITY).getString();
                    }
                    // Here agent adds some additional payload items to the message
                    if (msg.getText() != null) {
                        msg.addPayloadItem(
                                new cMsgPayloadItem(AConstants.DALOGTEXT,
                                        msg.getText()));
                    }
                    msg.addPayloadItem(
                            new cMsgPayloadItem(AConstants.DALOGSEVERITYID,
                                    msg.getUserInt()));

                    msg.addPayloadItem(
                            new cMsgPayloadItem(AConstants.EXPID,
                                    me.getExpid()));
                    msg.addPayloadItem(
                            new cMsgPayloadItem(AConstants.CODANAME,
                                    me.getName()));
                    msg.addPayloadItem(
                            new cMsgPayloadItem(AConstants.CLIENTHOST,
                                    me.getClient().getHostName()));
                    msg.addPayloadItem(
                            new cMsgPayloadItem(AConstants.USERNAME,
                                    "afecs"));
                    msg.addPayloadItem(
                            new cMsgPayloadItem(AConstants.SESSION,
                                    me.getSession()));
                    msg.addPayloadItem(
                            new cMsgPayloadItem(AConstants.RUNTYPE,
                                    me.getRunType()));
                    msg.addPayloadItem(
                            new cMsgPayloadItem(AConstants.RUNNUMBER,
                                    me.getRunNumber()));
                    msg.addPayloadItem(
                            new cMsgPayloadItem("tod",
                                    AfecsTool.getCurrentTimeInH()));

                    // Forward message to the cMsg domain
                    msgForward(msg);

                    // NOTE: as a severity ID the userInt is used set by clients
                    // Check to see if severity ID is error and above ( 9 and above )
                    // if so, tell rcGui to show it on the message board.
                    if (msg.getUserInt() >= 9) {
                        reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                                myName, msg.getUserInt(),
                                severityString,
                                msg.getText() + " (client msg)");
                    }
                } catch (cMsgException e) {
                    e.printStackTrace();
                } catch (NullPointerException ignored) {
                }
            }
        }

    }

    /**
     * <p>
     * Private inner class for responding to the rc/response messages
     * from the RC client. This is to a aSync requests to the client
     * such as coda/info/get... requests.
     * Warning messages will generated in case request results are
     * not consistent with the agent data.
     * </p>
     */
    private class ResponseMsgCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {

            if (msg != null) {

                // Data comes as a message text
                String txt = msg.getText();

                if (txt != null) {

                    if (msg.getType().equals(AConstants.RcResponseGetState)) {
                        me.setState(txt);

                    } else if (msg.getType().equals(AConstants.RcResponseGetCodaClass)) {
                        if (!me.getType().equals(AConstants.udf) && !me.getType().equals(txt)) {
                            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                                    myName,
                                    7,
                                    AConstants.WARN,
                                    "Conflict with the clients CodaClass. client-codaClass = " +
                                            txt +
                                            " vs. " +
                                            myName + "-codaClass = " +
                                            me.getType());
                            dalogMsg(myName,
                                    7,
                                    AConstants.WARN,
                                    "Conflict with the clients CodaClass. client-codaClass = " +
                                            txt +
                                            " vs. " +
                                            myName +
                                            "-codaClass = " +
                                            me.getType());
                        }
                        me.setType(txt);

                    } else if (msg.getType().equals(AConstants.RcResponseGetSession)) {
                        if (!me.getSession().equals(txt)) {
                            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                                    myName,
                                    11,
                                    AConstants.ERROR,
                                    "Conflict with the clients session. client-session = " +
                                            txt +
                                            " vs. " +
                                            myName +
                                            "-session = " +
                                            me.getSession());
                            dalogMsg(myName, 11, AConstants.ERROR,
                                    "Conflict with the clients session. client-session = " +
                                            txt +
                                            " vs. " +
                                            myName +
                                            "-session = " +
                                            me.getSession());
                        }

                    } else if (msg.getType().equals(AConstants.RcResponseGetRunNumber)) {
                        if (me.getRunNumber() != Integer.parseInt(txt)) {
                            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                                    myName,
                                    11,
                                    AConstants.ERROR,
                                    "Conflict with the clients runnumber. client-runnumber = " +
                                            txt +
                                            " vs. " +
                                            myName +
                                            "-runnumber = " +
                                            me.getRunNumber());
                            dalogMsg(myName,
                                    11,
                                    AConstants.ERROR,
                                    "Conflict with the clients runnumber. client-runnumber = " +
                                            txt +
                                            " vs. " +
                                            myName +
                                            "-runnumber = " +
                                            me.getRunNumber());
                        }

                    } else if (msg.getType().equals(AConstants.RcResponseGetRunType)) {
                        if (!me.getRunType().equals(txt)) {
                            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                                    myName,
                                    11,
                                    AConstants.ERROR,
                                    "Conflict with the clients runType. client-runType = " +
                                            txt +
                                            " vs. " +
                                            myName +
                                            "-runType = " +
                                            me.getRunType());
                            dalogMsg(myName,
                                    11,
                                    AConstants.ERROR,
                                    "Conflict with the clients runType. client-runType = " +
                                            txt +
                                            " vs. " +
                                            myName +
                                            "-runType = " +
                                            me.getRunType());
                        }

                    } else if (msg.getType().equals(AConstants.RcResponseGetConfigId)) {
                        try {
                            if (me.getConfigID() == Integer.parseInt(txt)) {
                                reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                                        myName,
                                        11,
                                        AConstants.ERROR,
                                        "Conflict with the clients configID. client-configID = " +
                                                txt +
                                                " vs. " +
                                                myName +
                                                "-configID = " +
                                                me.getConfigID());
                                dalogMsg(myName,
                                        11,
                                        AConstants.ERROR,
                                        "Conflict with the clients configID. client-configID = " +
                                                txt +
                                                " vs. " +
                                                myName + "-configID = " +
                                                me.getConfigID());
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Private inner class for responding to the messages
     * from the EMU client, asking to set proper number of
     * events per ET buffer. This info is passed to the
     * representative client (type = ROC) through rc domain.
     * </p>
     */
    private class RocEvtPerBufferMsgCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {

                try {
                    if (msg.getUserInt() > 0 && isRcClientConnected()) {
                        msg.setSubject(myName);
                        msg.setType(AConstants.RunControlSetRocBufferLevel);
                        msg.setText("setRocBufferLevel");
                        myCRCClientConnection.send(msg);
                    }
                } catch (cMsgException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void agentControlRequestReleaseAgent() {
        try {
            // send client to stop reporting
            sessionControlStopReporting();

            // update session and runType info
            // in the platform registration agent
            me.setSession(AConstants.udf);
            me.setRunType(AConstants.udf);
            update_registration();

            // set session of the client undefined
            sessionControlSetSession(me.getSession());
        } catch (cMsgException e) {
            e.printStackTrace();
        }
        _reset();
        _stopCommunications();
        me.setState(AConstants.disconnected);
    }

    public void agentControlRequestNetworkDetails(String sender, String[] ip, String[] br) {
        // store linked component network information
        me.addLinkedIp(sender, ip);
        me.addLinkedBa(sender, br);
    }

    public void agentControlRequestSetFileWriting(String control) {
        if (control.equals("disabled")) {
            me.setFileWriting(0);
        } else {
            me.setFileWriting(1);
        }
    }


    public void agentControlRequestSetup() {

        // differentiate agent
        differentiate(me);

        // start communications with the
        // client, reset and configure
        boolean stat = _setup();
        if (stat) {
            // Move to configured
            me.setDestinationNames(null);
            isResetting.set(false);
            _moveToState(AConstants.configured);
        }
    }

}
