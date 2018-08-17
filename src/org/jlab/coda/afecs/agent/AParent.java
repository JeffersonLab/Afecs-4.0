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

package org.jlab.coda.afecs.agent;

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.cool.ontology.APlugin;
import org.jlab.coda.afecs.cool.ontology.AProcess;
import org.jlab.coda.afecs.cool.ontology.AState;
import org.jlab.coda.afecs.plugin.IAClientCommunication;
import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.process.ProcessManager;
import org.jlab.coda.afecs.system.thread.ReportStatus;
import org.jlab.coda.afecs.system.util.ALogger;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * This is the parent class for agents representing
 * software components, as well as agents representing
 * real world components.
 * <ul>
 * <li> Defines Afecs Component: base</li>
 * <li>
 * Defines status reporting thread, that sends entire
 * AComponent object to every one subscribing to the
 * subject = session.
 * N.B. This is a design choice. All supervisor/s must
 * subscribe the subject session. This is done in order
 * to simplify the life of any graphical interface that
 * is ready to visualize information of the specific agent.
 * </li>
 * <li>
 * Defines the ProcessManager object, that manages process
 * execution, including periodic processes and shell processes.
 * </li>
 * <li> Real world client communication driver plugin</li>
 * <li>
 * Atomic boolean indicating if agent is in the
 * middle of transitioning process, is resetting
 * or aborting the transition.
 * </li>
 * </ul>
 * <br>
 * Creates connection object to the Afecs platform.
 * Subscribes and presents a callback method for agent
 * info request messages.
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 3.x
 * @see org.jlab.coda.afecs.system.AConstants#AgentControlRequest
 * <br>
 * Presents methods for agent registration with the
 * platform registration services.
 * <p>
 * Most importantly this class provides Afecs 3 critical
 * methods:
 * <ul>
 * <li>
 * Basic agent differentiation method.
 * Note configuration details, defined in the COOL
 * (states, processes, etc.) are encapsulated in
 * the passed AComponent object.
 * Updates registration with the platform registrar,
 * creates an instance of the representative component
 * communication plugin.
 * N.B. plugin details are defined in the COOL
 * configuration file. By design all Afecs plugins
 * must implement IAClientCommunication interface.
 * Attention: Dynamic plugin replacement is not
 * supported, i.e. if after starting an agent with
 * one plugin you can not change cool configuration
 * and accept that the new plugin will be used instead.
 * </li>
 * <li>
 * Basic moveToState method. Uses described
 * processes required to transition to a state.
 * </li>
 * Also method to read configuration files and returning
 * list of payload items containing file content, file
 * date as well as emu roc configuration file contents.
 * </ul>
 * </p>
 * </p>
 */
public class AParent extends ABase implements Serializable {

    // This agent data object.
    public AComponent me;

    // Status reporting thread object.
    // Reports AComponent object,
    transient private Thread stRpT;

    // Process manager object reference
    transient public ProcessManager pm;

    // Client communication plug-in object
    transient public IAClientCommunication myPlugin;

    // action boolean indicators
    transient public AtomicBoolean isTransitioning = new AtomicBoolean(false);
    transient public AtomicBoolean isResetting = new AtomicBoolean(false);

    // System config file date the time it was last read
    transient private long _sysConfigFileReadDate = 0;

    // user config file date the time it was last read
    transient private long _userConfigFileReadDate = 0;

    // Subscription handler for AgentInfoRequest messages
    transient private cMsgSubscriptionHandle infoSH;

    // Local instance of the logger object
    transient private ALogger lg = ALogger.getInstance();

    transient private int pingDelay;

    /**
     * <p>
     * Constructor that creates a component
     * with default parameters.
     * Connects to the platform cMsg server.
     * Creates ProcessManager object.
     * </p>
     *
     * @param name The name of the agent
     */
    public AParent(String name) {
        super();
        if (name != null) {
            me = new AComponent();
            me.setName(name);
            myName = me.getName();
            mySession = me.getSession();
            myRunType = me.getRunType();
            me.setStartTime(AfecsTool.getCurrentTime());
            me.setHost(myConfig.getLocalHost());
            me.setExpid(myConfig.getPlatformExpid());
            // connect to platform cMsg server
            myPlatformConnection = platformConnect();
            if (isPlatformConnected()) {
                // register with the platform
                _register();

                // Subscribe agent info request messages
                try {
                    // un-subscribe first
                    if (myPlatformConnection != null) {
                        if (infoSH != null) myPlatformConnection.unsubscribe(infoSH);
                    }

                    // subscribe messages asking information about this agent
                    infoSH = myPlatformConnection.subscribe(myName,
                            AConstants.AgentInfoRequest,
                            new AgentInfoCB(),
                            null);
                } catch (cMsgException e) {
                    lg.logger.severe(AfecsTool.stack2str(e));
                }
            }
        }
        pm = new ProcessManager(this);
    }

    /**
     * <p>
     * Constructor that creates a component
     * with default parameters.
     * Connects to the platform cMsg server.
     * Creates ProcessManager object.
     * </p>
     *
     * @param comp AComponent object reference
     */
    public AParent(AComponent comp) {
        super();
        if (comp != null) {
            me = comp;
            myName = me.getName();
            // setting rcServer name the name of this agent
            myConfig.setPlatformRcDomainServerName(myName);

            mySession = me.getSession();
            myRunType = me.getRunType();
            me.setStartTime(AfecsTool.getCurrentTime());
            if (me.getHost().equals(AConstants.udf)) me.setHost(myConfig.getLocalHost());
            me.setExpid(myConfig.getPlatformExpid());
            // connect to cMsg server
            myPlatformConnection = platformConnect();
            if (isPlatformConnected()) {
                // register with the platform
                _register();

                // Subscribe agent info request messages
                try {
                    // un-subscribe first
                    if (myPlatformConnection != null) {
                        if (infoSH != null) myPlatformConnection.unsubscribe(infoSH);
                    }

                    // subscribe messages asking information about this agent
                    infoSH = myPlatformConnection.subscribe(myName,
                            AConstants.AgentInfoRequest,
                            new AgentInfoCB(),
                            null);
                } catch (cMsgException e) {
                    lg.logger.severe(AfecsTool.stack2str(e));
                }

            }
        }
        pm = new ProcessManager(this);
    }

    /**
     * <p>
     * Register with the platform
     * registration services
     * </p>
     *
     * @return status of the registration
     */
    private boolean _register() {
        return me != null && send(myConfig.getPlatformName(),
                AConstants.PlatformRegistrationRequestAdd,
                me);
    }


    /**
     * <p>
     * Removes registration with the
     * platform registration services.
     * <p/>
     *
     * @return status of the registration
     */
    public boolean remove_registration() {
        return me != null && send(myConfig.getPlatformName(),
                AConstants.PlatformRegistrationRequestRemove,
                me.getName());
    }

    /**
     * <p>
     * Update/re-register with the
     * platform registration services
     * <p/>
     *
     * @return status of the registration
     */
    public boolean update_registration() {
        return me != null && send(myConfig.getPlatformName(),
                AConstants.PlatformRegistrationRequestUpdate,
                me);
    }

    /**
     * <p>
     * Starts status reporting thread.
     * This is a thread that periodically reports
     * to the supervisor agent the status of this
     * agent.
     * This will stop previously running  status
     * reporting thread just making sure that we
     * have only one status reporting thread at all
     * times.
     * </p>
     */
    public void startStatusReporting() {
        stopStatusReporting();
        ReportStatus t = new ReportStatus(this, true);
        stRpT = new Thread(t);
        stRpT.start();
    }

    /**
     * <p>
     * Stops status reporting thread, if
     * it is active.
     * </p>
     */
    public void stopStatusReporting() {
        if (stRpT != null) {
            stRpT.stop();
        }
    }

    /**
     * <p>
     * Stops status reporting thread and
     * all active periodic processes.
     * </p>
     */
    public void stop_rpp() {
        stopStatusReporting();
        pm.stopAllPeriodicProcesses();
    }

    public void stopPeriodicProcesses() {
        pm.stopAllPeriodicProcesses();
    }

    /**
     * <p>
     * Starts a process. It will checks COOL
     * defined processes for an agent to see
     * if the required process is scheduled
     * to run. This method will use defined agent
     * plugin (defined at the differentiation process)
     *
     * @param pName the name of the process
     * @return true if the process is successfully executed.
     * @see #differentiate(org.jlab.coda.afecs.cool.ontology.AComponent)
     * to execute the process.
     * </p>
     */
    public boolean requestStartProcess(String pName) {
        boolean b = false;
        if (me.getProcesses() != null && !me.getProcesses().isEmpty()) {
            for (AProcess p : me.getProcesses()) {
                if (p != null && p.getName().equals(pName)) {
                    b = pm.executeProcess(p, myPlugin, me);
                    break;
                }
            }
        }
        return b;
    }

    /**
     * <p>
     * Basic agent differentiation method.
     * </p>
     *
     * @param ad AComponent object reference
     */
    public void differentiate(AComponent ad) {

        // cCheck if the request is addressed to this agent
        if (!myName.equals(ad.getName())) {
            dalogMsg(me, 9, "ERROR", " Wrong agent name.");
            lg.logger.severe(myName + ": Wrong agent name. ");
            return;
        }

        // expid is not defined in the cool database
        ad.setExpid(me.getExpid());
        ad.setClient(me.getClient());

        // Stop reporting threads and periodic processes
        stop_rpp();

        // Check to see if the new runType,
        // i.e. supervisor is changed..
        // N.B. change of the  configured session
        // is detected at at the AControlDesigner level.
        if (me != null && !me.getRunType().equals(ad.getRunType())) {

            // Send to the old supervisor a message
            // informing that this agent has been
            // requested to change the configuration/supervisor
            if (!me.getSupervisor().equals(AConstants.udf)) {
                send(me.getSupervisor(),
                        AConstants.SupervisorControlRequestReleaseAgent,
                        myName);
            }
        }

        // Get new configuration details (states, processes, etc.)
        // encapsulated in the passed AComponent object.
        me = ad;

        // Update agent's previous registration
        // information i.e. session and runType info.
        update_registration();

        // Get described plugin to be used to communicate
        // with the real world client.
        if (me != null && me.getPlugin() != null) {
            APlugin p = me.getPlugin();
            try {
                Class c = Class.forName(p.getClassName());
                myPlugin = (IAClientCommunication) c.newInstance();
                myPlugin.setWorkingFor(myName);
                myPlugin.init();
            } catch (ClassNotFoundException |
                    IllegalAccessException |
                    InstantiationException |
                    AException e) {
                reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                        me.getName(),
                        9,
                        AConstants.ERROR,
                        myPlugin.getDescription() + ". Plugin error.");
                lg.logger.severe(myPlugin.getDescription() +
                        ". Failure" +
                        AfecsTool.stack2str(e));
                dalogMsg(me,
                        9,
                        "ERROR",
                        myPlugin.getDescription() + ". Plugin error.");
            }
        }
    }

    /**
     * <p>
     * Basic moveToState method.
     * Checks to see of the required state is known
     * before transitioning to a required state
     * </p>
     *
     * @param sn the name of a state
     * @return true if execution of sync processes are successful
     * and fail in all other cases, including async process execution.
     */
    public boolean transition(final String sn) {
        boolean status = true;
        if (me.getStates() != null && !me.getStates().isEmpty()) {
            for (AState s : me.getStates()) {

                // Check to see if this is a known state
                if (s.getName().equals(sn)) {
                    isTransitioning.set(true);
                    status = true;

                    // Set the state ing/ating mode
                    if (s.getDescription().equals("active")) {
                        if (!me.getState().equals(AConstants.disconnected)) {
                            me.setState("activating");
                        }
                    } else {
                        if (!me.getState().equals(AConstants.disconnected)) {
                            me.setState(s.getDescription() + "ing");
                        }
                    }

                    // Report to the supervisor that we are transitioning
                    send(me.getSession(), me.getRunType(), me);

                    // See if we have processes scheduled to be executed
                    // before the state transition. Note that agent supports
                    // scripts/processes at only pre and post state transitions.
                    for (AProcess bp : me.getProcesses()) {
                        if (bp != null &&
                                bp.getBefore() != null &&
                                bp.getBefore().equals(sn)) {
                            status = pm.executeProcess(bp, myPlugin, me);
                        }
                    }

                    // Now execute described processes required
                    // to be executed to achieve the state.
                    if (s.getProcesses() != null) {
                        for (AProcess p : s.getProcesses()) {
                            if (p != null) {

                                status = pm.executeProcess(p, myPlugin, me);
                                if (!status) {

                                    // Process execution to achieve a state failed.
                                    // Set the state of the agent to failed.
                                    me.setState(AConstants.failed);

                                    // Report to the supervisor that we failed to transitions
                                    send(me.getSession(), me.getRunType(), me);
                                    isTransitioning.set(false);
                                    return false;
                                }
                            }
                        }
                    }

                    // See if we have processes scheduled to be
                    // executed after the state transition. This is running in a
                    // separate thread, since we need to make sure the client
                    // had successfully transitioned
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(
                            new Callable<Boolean>() {
                                public Boolean call() throws Exception {
                                    for (AProcess ap : me.getProcesses()) {
                                        if (ap != null &&
                                                ap.getAfter() != null &&
                                                ap.getAfter().equals(sn)) {

                                            // wait until client transitions
                                            while (me.getState().contains("ing")) {
                                                AfecsTool.sleep(300);
                                            }

                                            pm.executeProcess(ap, myPlugin, me);
                                        }
                                    }
                                    return true;
                                }
                            }
                    );
                    if (executorService.isTerminated()) throw new Error("unexpected");

                    isTransitioning.set(false);
                    break;
                }
            }
        }
        return status;
    }

    /**
     * <p>
     * Reads and returns the content of the configuration
     * file if it is a direct path, and if not it asks
     * platform to read and report the content of the config
     * file stored in the platform database directory pointed
     * by cool_home.
     * </p>
     *
     * @param conf     configuration file name
     * @param comp     object defining component
     * @param isSystem defines if configuration
     *                 is the component configuration
     *                 or userConf defined in the COOL.
     * @return List of cMsgPayloadItems containing:
     * <ul>
     * <li>fileName</li>
     * <li>fileDate</li>
     * <li>fileContent</li>
     * </ul>
     */
    public List<cMsgPayloadItem> getConfigFileContent(String conf, AComponent comp, boolean isSystem) {

        // indicates if file is changed. 1 = changed
        int fileChanged = 0;

        List<cMsgPayloadItem> al = new ArrayList<>();
        try {

            if (isSystem) {
                al.add(new cMsgPayloadItem(AConstants.FILENAME, conf));
            } else {
                al.add(new cMsgPayloadItem(AConstants.USRFILENAME, conf));
            }
            if (!conf.equals(AConstants.udf) && comp != null) {
                if (conf.startsWith(File.separator)) {
                    long lmd = new File(conf).lastModified();
                    al.add(new cMsgPayloadItem(AConstants.FILEDATE, lmd));

                    String s = readFileAsString(conf);
                    if (s != null) {
                        if (isSystem) {
                            al.add(new cMsgPayloadItem(AConstants.FILECONTENT, s));

                            // check to see if file was changed
                            if (_sysConfigFileReadDate != lmd) {
                                fileChanged = 1;
                                _sysConfigFileReadDate = lmd;
                            }
                            al.add(new cMsgPayloadItem(AConstants.ISCHANGED, fileChanged));
                        } else {
                            al.add(new cMsgPayloadItem(AConstants.USRFILECONTENT, s));

                            // check to see if file was changed
                            if (_userConfigFileReadDate != lmd) {
                                fileChanged = 1;
                                _userConfigFileReadDate = lmd;
                            }
                            al.add(new cMsgPayloadItem(AConstants.USRFILEISCHANGED, fileChanged));
                        }
                    }
                } else {

                    ArrayList<cMsgPayloadItem> cdl = new ArrayList<>();
                    try {
                        cdl.add(new cMsgPayloadItem(AConstants.DEFAULTOPTIONDIRS,
                                comp.getDod().toArray(new String[comp.getDod().size()])));
                    } catch (cMsgException e) {
                        lg.logger.severe(AfecsTool.stack2str(e));
                    }

                    cMsgMessage msg = p2pSend(myConfig.getPlatformName(),
                            AConstants.PlatformInfoRequestReadConfgiFile,
                            conf,
                            cdl,
                            AConstants.TIMEOUT);
                    if (msg != null) {
                        if (msg.getPayloadItem(AConstants.FILEDATE) != null) {
                            al.add(msg.getPayloadItem(AConstants.FILEDATE));
                            long lmd = msg.getPayloadItem(AConstants.FILEDATE).getLong();

                            if (msg.getPayloadItem(AConstants.FILECONTENT) != null) {
                                if (isSystem) {
                                    al.add(msg.getPayloadItem(AConstants.FILECONTENT));
                                    // check to see if file was changed
                                    if (_sysConfigFileReadDate != lmd) {
                                        fileChanged = 1;
                                        _sysConfigFileReadDate = lmd;
                                    }
                                    al.add(new cMsgPayloadItem(AConstants.ISCHANGED, fileChanged));
                                } else {
                                    al.add(new cMsgPayloadItem(AConstants.USRFILECONTENT,
                                            msg.getPayloadItem(AConstants.FILECONTENT).getString()));
                                    // check to see if file was changed
                                    if (_userConfigFileReadDate != lmd) {
                                        fileChanged = 1;
                                        _userConfigFileReadDate = lmd;
                                    }
                                    al.add(new cMsgPayloadItem(AConstants.USRFILEISCHANGED, fileChanged));
                                }
                            }
                        }
                        if (msg.getPayloadItem(AConstants.EMUROCCONFIG) != null) {
                            al.add(msg.getPayloadItem(AConstants.EMUROCCONFIG));
                        }
                    }
                }
            }
        } catch (IOException | cMsgException | AException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
        }
        return al;
    }

    /**
     * <p>
     * Request platform host IPs ( possibly a list of ips)
     * this will come with two payloads: list of IP addresses
     * and the port where platform cMsg server is running.
     * </p>
     *
     * @return List of payload items describing the host IP
     * addresses and ports where platform is running.
     */
    public List<cMsgPayloadItem> getPlatformAddress() {

        List<cMsgPayloadItem> al = new ArrayList<>();

        // Request platform host IPs ( possibly a list of ips)
        // this will come with two payloads: list of IP addresses
        // and the port where platform cMsg server is running.
        cMsgMessage msg = null;
        try {
            msg = p2pSend(myConfig.getPlatformName(),
                    AConstants.PlatformHostNameRequest,
                    " ",
                    AConstants.TIMEOUT);
        } catch (AException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
        }
        if (msg != null) {
            if (msg.getPayloadItem(AConstants.PLATFORMHOST) != null)
                al.add(msg.getPayloadItem(AConstants.PLATFORMHOST));
            if (msg.getPayloadItem(AConstants.PLATFORMPORT) != null)
                al.add(msg.getPayloadItem(AConstants.PLATFORMPORT));
        }
        return al;
    }

    /**
     * <p>
     * Private inner class for responding info
     * request messages addressed to this agent.
     * </p>
     */
    private class AgentInfoCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {
                String type = msg.getType();
                String sender = msg.getSender();

                switch (type) {
                    case AConstants.AgentInfoRequestState:
                        if (msg.isGetRequest()) {
                            pingDelay++;
                            cMsgMessage mr = null;
                            try {
                                mr = msg.response();
                            } catch (cMsgException e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                            }
                            try {

                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);

                                // ping real client every 60 seconds
                                if (pingDelay >= 60) {
                                    if (sender.contains(AConstants.ORPHANAGENTMONITOR)) {
                                        String tmp = rcClientInfoSyncGetState(1000);
                                        if (tmp != null && !tmp.equals(AConstants.failed)) {
                                            me.setState(tmp);
                                        } else {
                                            me.setState(AConstants.udf);
                                        }
                                    }
                                    pingDelay = 0;
                                }

                            } catch (AException e) {
                                me.setState(AConstants.disconnected);
                                pingDelay = 0;
                                lg.logger.severe(AfecsTool.stack2str(e));
                            }

                            mr.setText(me.getState());
                            try {
                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                            }

                        } else {
                            send(sender,
                                    AConstants.AgentInfoResponseState,
                                    me.getState());
                        }

                        break;
                    case AConstants.AgentInfoRequestStatus:
                        send(sender,
                                AConstants.AgentInfoResponseStatus,
                                me.getConfigurationDataAsPayload());

                        break;
                    case AConstants.AgentInfoRequestDescription:
                        send(sender,
                                AConstants.AgentInfoResponseDescription,
                                me.getDescription());

                        break;
                    case AConstants.AgentInfoRequestHost:
                        send(sender,
                                AConstants.AgentInfoResponseHost,
                                me.getHost());

                        break;
                    case AConstants.AgentInfoRequestStartTime:
                        send(sender,
                                AConstants.AgentInfoResponseStartTime,
                                me.getStartTime());

                        break;
                    case AConstants.AgentInfoRequestType:
                        send(sender,
                                AConstants.AgentInfoResponseType,
                                me.getType());

                        break;
                    case AConstants.AgentInfoRequestRuntype:
                        send(sender,
                                AConstants.AgentInfoResponseRuntype,
                                me.getRunType());

                        break;
                    case AConstants.AgentInfoRequestSession:
                        send(sender,
                                AConstants.AgentInfoResponseSession,
                                me.getSession());
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.addPayloadItem(new cMsgPayloadItem(
                                        AConstants.SESSION,
                                        me.getSession()));
                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                            }
                        }
                        break;
                }
            }
        }
    }

}
