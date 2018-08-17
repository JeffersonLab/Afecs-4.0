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

package org.jlab.coda.afecs.platform;

import org.jlab.coda.afecs.client.AClientInfo;
import org.jlab.coda.afecs.cool.ontology.*;
import org.jlab.coda.afecs.cool.parser.CParser;
import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.ALogger;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgCallbackAdapter;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * <p>
 * Starts {@link CParser} object. Accepts messages to configure
 * particular control system, described in the form of control
 * oriented ontology language files. Sends message to the
 * {@link org.jlab.coda.afecs.container.AContainer} agents to
 * start described agents. Agents will then register with the
 * platform registrar system. This is a class that parses COOL
 * configuration files and designs control system or systems.
 * This is a class that responds gui requests to send the list
 * of configuration file names ($COOL_HOME/config/Control dir).
 * </p>
 *
 * @author gurjyan
 *         Date: 11/16/14 Time: 2:51 PM
 * @version 3.x
 */
public class AControlDesigner extends ABase {

    private Map<String, String> definedRTVs;

    // Local instance of the logger object
    private ALogger lg = ALogger.getInstance();

    private Map<String, Thread> _orphanAgentMap = new ConcurrentHashMap<>();

    /**
     * <p>
     * Constructor does cMsg connect
     * </p>
     */
    public AControlDesigner() {
        myName = AConstants.CONTROLDESIGNER;

        // Connect to the platform cMsg domain server
        myPlatformConnection = platformConnect();
        if (isPlatformConnected()) {
            subscribe();
        } else {
            lg.logger.severe(" Problem starting ControlDesigner. " +
                    "Cannot connect to the platform.");
        }
    }

    /**
     * <p>
     * Designer subscription
     * </p>
     *
     * @return status of the subscription
     */
    private boolean subscribe() {
        boolean stat = true;
        try {
            // Subscribe messages asking designer to configure
            myPlatformConnection.subscribe(myName,
                    AConstants.DesignerControlRequest,
                    new DesignerControlCB(),
                    null);

            // Subscribe messages asking specific info,
            // for e.g. list of configuration file names
            myPlatformConnection.subscribe(myName,
                    AConstants.DesignerInfoRequest,
                    new DesignerInfoCB(),
                    null);

        } catch (cMsgException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
            stat = false;
        }
        return stat;
    }


    /**
     * <p>
     * Parses control rdf file and sends
     * the list of agent/components to the GUI
     * </p>
     *
     * @param session config session
     * @param runType config name
     * @return true if file is opened properly
     */
    public boolean parseControlDescription(String session, String runType) {
        boolean stat = true;

        CParser p = new CParser(null);
        if (p.openFile(runType + "/" + runType + ".rdf", false)) {

            // parse control for only components, do not add supervisor agent to the control
            AControl control = p.parseControl(runType, true);
            Map<String, AComponent> comps = new LinkedHashMap<>();

            for (AComponent c : control.getComponents()) {
                c.setState(AConstants.udf);
                comps.put(c.getName(), c);
            }

            // Report all GUIs sorted list of components
            send(AConstants.GUI,
                    session + "_" + runType + "/agents",
                    "udf",
                    comps);

            // start named thread that will periodically getState of
            // the components. This objects will be stored under
            // key = session_runType in a orphan clients database.
            // The next method in this code (parseControlDescription with 3 parameters)
            // will stop the corresponding thread from the map and remove thread object
            // from the map.
            String k = session + "_" + runType;
            if (!_orphanAgentMap.containsKey(k)) {
                Thread v = new Thread(new AClientLessAgentsMonitorT(comps, session, runType, _orphanAgentMap));
                v.start();
                _orphanAgentMap.put(k, v);
            }

        } else {
            reportAlarmMsg(session + "/" + runType,
                    myName,
                    9,
                    AConstants.ERROR,
                    "Problem parsing control configuration file.");
            lg.logger.severe(myName +
                    ": parsing configuration file.");
            stat = false;
        }
        return stat;
    }


    /**
     * <p>
     * Parses COOL control description file.
     * Creates CODA state machine services for the
     * supervisor agent. Defines CODA states for
     * agents representing CODA real world components.
     * <p>
     * </p>
     *
     * @param session    in which control runs
     * @param runType    the name of the configuration file
     * @param usrSetRTVs map containing user set rtvs and their values
     * @return parsing status
     */
    public boolean parseControlDescription(String session,
                                           String runType,
                                           Map<String, String> usrSetRTVs) {
        boolean stat = true;

        CParser p = new CParser(usrSetRTVs);

        if (p.openFile(runType + "/" + runType + ".rdf", false)) {


            // false parameter means to create and add supervisor agent to the control
            AControl control = p.parseControl(runType, false);

            if (control != null) {

                AComponent duper = control.getSupervisor();

                // Program supervisor agent by defining
                // CODA state machine services
                // Configure service
                duper.addService(createConfigureService());

                // Download service
                duper.addService(createDownloadService());

                // Prestart service
                duper.addService(createPrestartService());

                // Go service
                duper.addService(createGoService());

                // End service
                duper.addService(createEndService());

                // Start run service
                duper.addService(createStartRunService());

                // This is a sorted map of agents to report GUI's
                Map<String, AComponent> comps = new LinkedHashMap<>();

                for (AComponent c : control.getComponents()) {

                    if (c.getCodaComponent().equals(AConstants.seton)) {

                        // Add configured state
                        c.addState(createConfiguredState());

                        // Add downloaded state
                        c.addState(createDownloadedState(c.getCoda2Component()));

                        // Add prestarted state
                        c.addState(createPrestartedState(c.getCoda2Component()));

                        // Add active state
                        c.addState(createActiveState(c.getCoda2Component()));

                        // Add ended state
                        c.addState(createEndedState(c.getCoda2Component()));

                        // Add paused state
                        c.addState(createPausedState());

                        // Add reseted state
                        c.addState(createResetedState(c.getCoda2Component()));
                    }

                    // Defines default priority if it is not cool defined
                    definePriority(c);
                    comps.put(c.getName(), c);
                }

                // Report all GUIs sorted list of components
                send(AConstants.GUI,
                        session + "_" + runType + "/agents",
                        comps);

                // Start designing a control if required RTVs are set
                stat = designControl(control, session, runType);

            } else {
                reportAlarmMsg(session + "/" + runType,
                        myName,
                        9,
                        AConstants.ERROR,
                        "null control.");
                lg.logger.severe(myName +
                        ": null control.");
            }

        } else {
            reportAlarmMsg(session + "/" + runType,
                    myName,
                    9,
                    AConstants.ERROR,
                    "Problem parsing control configuration file.");
            lg.logger.severe(myName +
                    ": parsing configuration file.");
            stat = false;
        }
        return stat;
    }

    /**
     * <p>
     * This method is called by the request of the supervisor
     * that needed to restart an agent on some other container.
     * Parses COOL control description file, if control contains
     * requested agent start that agent on the specified container.
     * </p>
     *
     * @param session       in which control runs
     * @param runType       the name of the configuration file
     * @param agentName     the name of the agent
     * @param containerHost the name of the container
     * @param usrSetRTVs    map containing user set rtvs and their values
     * @return parsing status
     */
    public boolean parseAndStartAgent(String session,
                                      String runType,
                                      String agentName,
                                      String containerHost,
                                      Map<String, String> usrSetRTVs) {

        boolean stat = true;
        CParser p = new CParser(usrSetRTVs);

        if (p.openFile(runType + "/" + runType + ".rdf", false)) {

            // false operator means to create and add supervisor agent for the control
            AControl control = p.parseControl(runType, false);
            if (control != null) {
                for (AComponent c : control.getComponents()) {
                    if (c.getName().equals(agentName)) {
                        if (c.getCodaComponent().equals(AConstants.seton)) {

                            // Add configured state
                            c.addState(createConfiguredState());

                            // Add downloaded state
                            c.addState(createDownloadedState(c.getCoda2Component()));

                            // Add prestarted state
                            c.addState(createPrestartedState(c.getCoda2Component()));

                            // Add active state
                            c.addState(createActiveState(c.getCoda2Component()));

                            // Add ended state
                            c.addState(createEndedState(c.getCoda2Component()));

                            // Add paused state
                            c.addState(createPausedState());

                            // Add reseted state
                            c.addState(createResetedState(c.getCoda2Component()));
                        }

                        // Defines default priority if it is not cool defined
                        definePriority(c);
                        startAgent(c, session, runType, containerHost);
                        break;
                    }
                }
            } else {
                reportAlarmMsg(session + "/" + runType,
                        myName,
                        9,
                        AConstants.ERROR,
                        "Problem parsing a control.");
                lg.logger.severe(myName +
                        ": Problem parsing a control.");
            }
        } else {
            reportAlarmMsg(session + "/" + runType,
                    myName,
                    9,
                    AConstants.ERROR,
                    "Configuration file was not found.");
            lg.logger.severe(myName +
                    ": Configuration file was not found.");
            stat = false;
        }
        return stat;
    }

    /**
     * <p>
     * Sets default priority for a component in case
     * the definition in the cool is missing
     * </p>
     *
     * @param c {@link AComponent} object
     */
    public void definePriority(AComponent c) {
        if (c.getPriority() == 0) {
            if (c.getType().equalsIgnoreCase(ACodaType.USR.name())) c.setPriority(ACodaType.USR.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.SLC.name())) c.setPriority(ACodaType.SLC.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.WNC.name())) c.setPriority(ACodaType.WNC.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.ER.name())) c.setPriority(ACodaType.ER.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.FCS.name())) c.setPriority(ACodaType.FCS.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.PEB.name())) c.setPriority(ACodaType.PEB.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.SEB.name())) c.setPriority(ACodaType.SEB.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.EB.name())) c.setPriority(ACodaType.EB.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.CDEB.name())) c.setPriority(ACodaType.CDEB.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.DC.name())) c.setPriority(ACodaType.DC.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.ROC.name())) c.setPriority(ACodaType.ROC.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.GT.name())) c.setPriority(ACodaType.GT.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.TS.name())) c.setPriority(ACodaType.TS.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.SMS.name())) c.setPriority(ACodaType.SMS.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.RCS.name())) c.setPriority(ACodaType.RCS.priority());
            else if (c.getType().equalsIgnoreCase(ACodaType.FILE.name())) c.setPriority(ACodaType.FILE.priority());
            else lg.logger.severe("Error: " +
                        c.getType() +
                        " type is not defined for the component " + " " + c.getName());
        }
    }

    /**
     * <p>
     * Creates CODA paused state
     * </p>
     *
     * @return {@link AState} object
     */
    private AState createPausedState() {
        AState s = new AState();
        s.setName("Paused");
        s.setDescription("Coda state transition Paused state");

        // Process
        AProcess pr = new AProcess();
        pr.setName("Pause_Process");
        pr.setDescription("Pause transition achieved through this process");
        pr.setInitiator(AConstants.seton);

        // Send package of the process
        APackage pks = new APackage();
        pks.setName("Pause_Send_Package");
        pks.setDescription("Pause process send package");
        pks.setForRcClient(AConstants.seton);
        pks.setSendType("run/transition/pause");
        pks.setSendText("pause");

        // Received package of the process
        APackage pkr = new APackage();
        pkr.setName("Pause_Receive_Package");
        pkr.setDescription("Pause process receive package");
        pkr.setForRcClient(AConstants.seton);
        pkr.setReceivedType("rc/report/status");
        ArrayList<String> rt = new ArrayList<>();
        rt.add("paused");
        pkr.setReceivedText(rt);

        // Add packages to the process
        pr.addSendPackage(pks);
        pr.addReceivePackage(pkr);

        // Add process to the state
        s.addProcess(pr);
        return s;
    }

    /**
     * <p>
     * Creates CODA configure state
     * </p>
     *
     * @return {@link AState} object
     */
    private AState createConfiguredState() {
        AState s = new AState();
        s.setName("configured");
        s.setDescription("configur");

        AProcess pr;
        APackage pks;

        // Process
        pr = new AProcess();
        pr.setName("Configure_Process");
        pr.setDescription("Configure transition achieved through this process");
        pr.setInitiator(AConstants.seton);

        // Send package of the process
        pks = new APackage();
        pks.setName("Configure_Send_Package");
        pks.setDescription("Configure process send package");
        pks.setForRcClient(AConstants.seton);
        pks.setSendType("run/transition/configure");
        pks.setSendText("configure");

        // Received package of the process
        APackage pkr = new APackage();
        pkr.setName("Configure_Receive_Package");
        pkr.setDescription("Configure process receive package");
        pkr.setForRcClient(AConstants.seton);
        pkr.setReceivedType("rc/report/status");
        ArrayList<String> rt = new ArrayList<>();
        rt.add("configured");
        pkr.setReceivedText(rt);

        // Add packages to the process
        pr.addSendPackage(pks);
        pr.addReceivePackage(pkr);

        // Add process to the state
        s.addProcess(pr);
        return s;
    }

    /**
     * <p>
     * Creates CODA download state
     * </p>
     *
     * @param isCoda2Component defines if the component COOL
     *                         description requires to be the
     *                         old CODA2 component
     * @return {@link AState} object
     */
    private AState createDownloadedState(String isCoda2Component) {
        AState s = new AState();
        s.setName("downloaded");
        s.setDescription("download");

        // Process
        AProcess pr = new AProcess();
        pr.setName("Download_Process");
        pr.setDescription("Download transition achieved through this process");
        pr.setSync(AConstants.setoff);
        pr.setInitiator(AConstants.seton);

        // Received package of the process
        APackage pkr = new APackage();
        pkr.setName("Download_Receive_Package");
        pkr.setDescription("Download process receive package");
        pkr.setForRcClient(AConstants.seton);
        pkr.setReceivedType("rc/report/status");
        ArrayList<String> rt = new ArrayList<>();
        rt.add("downloaded");
        pkr.setReceivedText(rt);

        // Send package of the process
        APackage pks = new APackage();
        pks.setName("Download_Send_Package");
        pks.setDescription("Download process send package");

        // Check if coda2 component
        if (isCoda2Component.equals(AConstants.seton)) {

            // DP channel
            AChannel ch = new AChannel();
            ch.setName("DP");
            ch.setDescription("DP download");
            ch.setSetValue("download $runtype");
            ch.setValueType(206);
            pks.addChannel(ch);
        } else {
            pks.setForRcClient(AConstants.seton);
            pks.setSendType("run/transition/download");
            pks.setSendText("download");
        }

        // Add packages to the process
        pr.addSendPackage(pks);
        pr.addReceivePackage(pkr);

        // Add process to the state
        s.addProcess(pr);
        return s;
    }

    /**
     * <p>
     * Creates CODA prestart state
     * </p>
     *
     * @param isCoda2Component defines if the component COOL
     *                         description requires to be the
     *                         old CODA2 component
     * @return {@link AState} object
     */
    private AState createPrestartedState(String isCoda2Component) {
        AState s = new AState();
        s.setName("prestarted");
        s.setDescription("prestart");

        // Process
        AProcess pr = new AProcess();
        pr.setName("Prestart_Process");
        pr.setDescription("Prestart transition achieved through this process");
        pr.setSync(AConstants.setoff);
        pr.setInitiator(AConstants.seton);

        // Received package of the process
        APackage pkr = new APackage();
        pkr.setName("Prestart_Receive_Package");
        pkr.setDescription("Prestart process receive package");
        pkr.setForRcClient(AConstants.seton);
        pkr.setReceivedType("rc/report/status");
        ArrayList<String> rt = new ArrayList<>();
        rt.add("paused");
        pkr.setReceivedText(rt);

        // Send package of the process
        APackage pks = new APackage();
        pks.setName("Prestart_Send_Package");
        pks.setDescription("Prestart process send package");

        // Check if coda2 component
        if (isCoda2Component.equals(AConstants.seton)) {

            // DP channel
            AChannel ch = new AChannel();
            ch.setName("DP");
            ch.setDescription("DP prestart");
            ch.setSetValue("prestart");
            ch.setValueType(206);
            pks.addChannel(ch);
        } else {
            pks.setForRcClient(AConstants.seton);
            pks.setSendType("run/transition/prestart");
            pks.setSendText("prestart");
        }

        // Add packages to the process
        pr.addSendPackage(pks);
        pr.addReceivePackage(pkr);

        // Add process to the state
        s.addProcess(pr);
        return s;
    }

    /**
     * <p>
     * Creates CODA active state
     * </p>
     *
     * @param isCoda2Component defines if the component COOL
     *                         description requires to be the
     *                         old CODA2 component
     * @return {@link AState} object
     */
    private AState createActiveState(String isCoda2Component) {
        AState s = new AState();
        s.setName("active");
        s.setDescription("active");

        // Process
        AProcess pr = new AProcess();
        pr.setName("Go_Process");
        pr.setDescription("Go transition achieved through this process");
        pr.setSync(AConstants.setoff);
        pr.setInitiator(AConstants.seton);

        // Received package of the process
        APackage pkr = new APackage();
        pkr.setName("Go_Receive_Package");
        pkr.setDescription("Go process receive package");
        pkr.setForRcClient(AConstants.seton);
        pkr.setReceivedType("rc/report/status");
        ArrayList<String> rt = new ArrayList<>();
        rt.add("active");
        pkr.setReceivedText(rt);

        // Send package
        APackage pks = new APackage();
        pks.setName("Go_Send_Package");
        pks.setDescription("Go process send package");

        // Check if coda2 component
        if (isCoda2Component.equals(AConstants.seton)) {

            // DP channel
            AChannel ch = new AChannel();
            ch.setName("DP");
            ch.setDescription("DP go");
            ch.setSetValue("go");
            ch.setValueType(206);
            pks.addChannel(ch);
        } else {
            pks.setForRcClient(AConstants.seton);
            pks.setSendType("run/transition/go");
            pks.setSendText("go");
        }

        // Add packages to the process
        pr.addSendPackage(pks);
        pr.addReceivePackage(pkr);

        // Add process to the state
        s.addProcess(pr);
        return s;
    }

    /**
     * <p>
     * Creates CODA ended state
     * </p>
     *
     * @param isCoda2Component defines if the component COOL
     *                         description requires to be the
     *                         old CODA2 component
     * @return {@link AState} object
     */
    private AState createEndedState(String isCoda2Component) {
        AState s = new AState();
        s.setName("ended");
        s.setDescription("end");

        // Process
        AProcess pr = new AProcess();
        pr.setName("End_Process");
        pr.setDescription("End transition achieved through this process");
        pr.setSync(AConstants.setoff);
        pr.setInitiator(AConstants.seton);

        // Received package of the process
        APackage pkr = new APackage();
        pkr.setName("End_Receive_Package");
        pkr.setDescription("End process receive package");
        pkr.setForRcClient(AConstants.seton);
        pkr.setReceivedType("rc/report/status");
        ArrayList<String> rt = new ArrayList<>();
        rt.add("downloaded");
        pkr.setReceivedText(rt);

        // Send package of the process
        APackage pks = new APackage();
        pks.setName("End_Send_Package");
        pks.setDescription("End process send package");

        // Check if coda2 component
        if (isCoda2Component.equals(AConstants.seton)) {

            // DP channel
            AChannel ch = new AChannel();
            ch.setName("DP");
            ch.setDescription("DP end");
            ch.setSetValue("end");
            ch.setValueType(206);
            pks.addChannel(ch);
        } else {
            pks.setForRcClient(AConstants.seton);
            pks.setSendType("run/transition/end");
            pks.setSendText("end");
        }

        // Add packages to the process
        pr.addSendPackage(pks);
        pr.addReceivePackage(pkr);

        // Add process to the state
        s.addProcess(pr);
        return s;
    }

    /**
     * <p>
     * Creates CODA reset state
     * </p>
     *
     * @param isCoda2Component defines if the component COOL
     *                         description requires to be the
     *                         old CODA2 component
     * @return {@link AState} object
     */
    private AState createResetedState(String isCoda2Component) {
        AState s = new AState();
        s.setName("reseted");
        s.setDescription("configur");

        // Process
        AProcess pr = new AProcess();
        pr.setName("Reset_Process");
        pr.setDescription("Reset transition achieved through this process");
        pr.setSync(AConstants.setoff);
        pr.setInitiator(AConstants.seton);

        // Received package of the process
        APackage pkr = new APackage();
        pkr.setName("Reset_Receive_Package");
        pkr.setDescription("Reset process receive package");
        pkr.setForRcClient(AConstants.seton);
        pkr.setReceivedType("rc/report/status");
        ArrayList<String> rt = new ArrayList<>();
        rt.add("configured");
        rt.add("booted");
        pkr.setReceivedText(rt);

        // Send package of the process
        APackage pks = new APackage();
        pks.setName("Reset_Send_Package");
        pks.setDescription("Reset process send package");

        // Check if coda2 component
        if (isCoda2Component.equals(AConstants.seton)) {

            // DP channel
            AChannel ch = new AChannel();
            ch.setName("DP");
            ch.setDescription("DP reset");
            ch.setSetValue("exit");
            ch.setValueType(206);
            pks.addChannel(ch);
        } else {
            pks.setForRcClient(AConstants.seton);
            pks.setSendType("run/transition/reset");
            pks.setSendText("reset");
        }

        // Add packages to the process
        pr.addSendPackage(pks);
        pr.addReceivePackage(pkr);

        // Add process to the state
        s.addProcess(pr);
        return s;
    }

    /**
     * <p>
     * Creates CODA configure service.
     * This is a COOL description of the CODA run-control service.
     * The service is hardcoded to prevent users messing with this
     * critical service description.
     * </p>
     *
     * @return {@link AService} object
     */
    private AService createConfigureService() {
        AService s = new AService();
        s.setName("CodaRcConfigure");
        s.setDescription("Coda configure state transition");

        // Rule of the service
        ARule r = new ARule();
        r.setName("configur");
        r.setDescription("Description of the Coda configure transition");
        r.setCode("if ( (priority++ in_state booted) || " +
                "(priority++ in_state configured) ) {\n" +
                "priority move_to configured;\n" +
                "}");

        s.setStateMachineRule(r);
        return s;
    }

    /**
     * <p>
     * Creates CODA download service.
     * This is a COOL description of the CODA run-control service.
     * The service is hardcoded to prevent users messing with this
     * critical service description.
     * </p>
     *
     * @return {@link AService} object
     */
    private AService createDownloadService() {
        AService s = new AService();
        s.setName("CodaRcDownload");
        s.setDescription("Coda download state transition");

        // Rule of the service
        ARule r = new ARule();
        r.setName("download");
        r.setDescription("Description of the Coda download transition");
        r.setCode("if ( ( priority++ in_state configured ) || " +
                "( priority++ in_state downloaded ) ) {\n" +
                "priority move_to downloaded;\n" +
                "}");

        s.setStateMachineRule(r);
        return s;
    }

    /**
     * <p>
     * Creates CODA prestart service.
     * This is a COOL description of the CODA run-control service.
     * The service is hardcoded to prevent users messing with this
     * critical service description.
     * </p>
     *
     * @return {@link AService} object
     */
    private AService createPrestartService() {
        AService s = new AService();
        s.setName("CodaRcPrestart");
        s.setDescription("Coda prestart state transition");

        // Rule of the service
        ARule r = new ARule();
        r.setName("prestart");
        r.setDescription("Description of the Coda prestart transition");
        r.setCode("if ( priority++ in_state downloaded ) {\n" +
                "priority move_to prestarted;\n" +
                "}");

        s.setStateMachineRule(r);
        return s;
    }

    /**
     * <p>
     * Creates CODA go service.
     * This is a COOL description of the CODA run-control service.
     * The service is hardcoded to prevent users messing with this
     * critical service description.
     * </p>
     *
     * @return {@link AService} object
     */
    private AService createGoService() {
        AService s = new AService();
        s.setName("CodaRcGo");
        s.setDescription("Coda go state transition");

        // Rule of the service
        ARule r = new ARule();
        r.setName("active");
        r.setDescription("Description of the Coda go transition");
        r.setCode("if ( priority++ in_state paused ) {\n" +
                "priority move_to active;\n" +
                "}");

        s.setStateMachineRule(r);
        return s;
    }

    /**
     * <p>
     * Creates CODA end service.
     * This is a COOL description of the CODA run-control service.
     * The service is hardcoded to prevent users messing with this
     * critical service description.
     * </p>
     *
     * @return {@link AService} object
     */
    private AService createEndService() {
        AService s = new AService();
        s.setName("CodaRcEnd");
        s.setDescription("Coda end state transition");

        // Rule of the service
        ARule r = new ARule();
        r.setName("end");
        r.setDescription("Description of the Coda end transition");
        r.setCode("if ( (priority-- in_state downloaded ) || " +
                "(priority-- in_state paused ) || " +
                "(priority-- in_state active) ) {\n" +
                "priority move_to ended;\n" +
                "}");

        s.setStateMachineRule(r);
        return s;
    }

    /**
     * <p>
     * Creates CODA start-run service.
     * This is a COOL description of the CODA run-control service.
     * The service is hardcoded to prevent users messing with this
     * critical service description.
     * </p>
     *
     * @return {@link AService} object
     */
    private AService createStartRunService() {
        AService s = new AService();
        s.setName("CodaRcStartRun");
        s.setDescription("Coda startRun state transition");

        // Rule of the service
        ARule r = new ARule();
        r.setName("active");
        r.setDescription("Description of the Coda startRun transition");
        r.setCode("            if ( (priority++ in_state configured)) {\n" +
                "            priority move_to downloaded;\n" +
                "            }\n" +
                "            if ( (priority++ in_state downloaded )) {\n" +
                "            priority move_to prestarted;\n" +
                "            }\n" +
                "            if ( (priority++ in_state paused )) {\n" +
                "            priority move_to active;\n" +
                "            }"
        );


        s.setStateMachineRule(r);
        return s;
    }


    /**
     * <p>
     * Starts agent(component) on the specified container
     * </p>
     *
     * @param com           {@link AComponent} object reference
     * @param session       session name
     * @param runType       runType/configuration name
     * @param containerName container name
     * @return stat status of the operation
     */
    private boolean startAgent(AComponent com,
                               String session,
                               String runType,
                               String containerName) {


        ConcurrentHashMap<String, AComponent> registeredComps;

        registeredComps = APlatform.registrar.getAgentDir();

        if (registeredComps != null &&
                !registeredComps.containsKey(com.getName())) {

            // Set required session, runType and
            // supervisor agent name for every component
            com.setSession(session);
            com.setRunType(runType);
            com.setSupervisor("sms_" + runType);
            com.setHost(containerName);

            // First see if the container administrator
            // exists for the required agent
            if (registeredComps.containsKey(com.getHost() + "_admin")) {
                if (APlatform.registrar.getClientDir().containsKey(com.getName())) {
                    AClientInfo ci = APlatform.registrar.getClientDir().get(com.getName());
                    ci.setContainerHost(com.getHost());
                    com.setClient(ci);
                }

                // Ask container admin to start a new agent
                APlatform.container.startAgent(com);

                // Wait until agent is registered
                cMsgMessage msg_b = null;
                try {
                    msg_b = p2pSend(myConfig.getPlatformName(),
                            AConstants.PlatformInfoRequestisRegistered,
                            com.getName(),
                            AConstants.TIMEOUT);
                } catch (AException e) {
                    lg.logger.severe(AfecsTool.stack2str(e));
                }
                if (msg_b == null) return false;
                if (msg_b.getText() == null ||
                        msg_b.getText().equals(AConstants.no)) {
                    reportAlarmMsg(session + "/" + runType,
                            myName,
                            9,
                            AConstants.ERROR,
                            "Agent " + com.getName() + " is not registered.");
                    lg.logger.severe(myName +
                            ":  Agent " +
                            com.getName() + " is not registered.");
                    return false;
                }
            } else {
                reportAlarmMsg(session + "/" + runType,
                        myName,
                        9,
                        AConstants.ERROR,
                        "Can not find container_admin on the node " +
                                com.getHost());
                lg.logger.severe(myName +
                        ": Can not find container_admin on the node " +
                        com.getHost());
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * Control designing method.
     * For every component in the control checks to see if it
     * has an representing agent register with the platform,
     * and starts and configures an agent.
     * </p>
     *
     * @param c       {@link AControl} object reference
     * @param session required session of the control
     * @param runType required runType of the control
     * @return stat status of the operation
     */
    private boolean designControl(AControl c,
                                  String session,
                                  String runType) {

        if (c.getComponents() != null && !c.getComponents().isEmpty()) {

            System.out.println("DDD ----| Info: ControlDesigner starts designing a control system.");

            // Assign configuration id to the runType
            int conf_id = APlatform.registrar.addConfigId(runType);

            for (AComponent com : c.getComponents()) {

                AClientInfo ci = null;
                com.setExpid(getPlEXPID());
                com.setSession(session);
                com.setRunType(runType);
                com.setConfigID(conf_id);
                com.setSupervisor("sms_" + runType);

//                 set the client of the agent if client already requested an agent
                if (APlatform.registrar.getClientDir() != null &&
                        APlatform.registrar.getClientDir().containsKey(com.getName())) {
                    ci = APlatform.registrar.getClientDir().get(com.getName());
                    com.setClient(ci);
                }

                // See if component in the control description is already active
                if (APlatform.registrar.getAgentDir() != null &&
                        APlatform.registrar.getAgentDir().containsKey(com.getName())) {

                    AComponent regComp = APlatform.registrar.getAgentDir().get(com.getName());

                    // Configured in a different session
                    if (!regComp.getSession().equals(AConstants.udf) &&
                            !regComp.getSession().equals(com.getSession())) {

                        // Deny configuration/design
                        ArrayList<cMsgPayloadItem> al = new ArrayList<>();
                        try {
                            al.add(new cMsgPayloadItem("MSGCONTENT",
                                    "Configuration is Denied !\n\nComponent " +
                                            regComp.getName() +
                                            "is configured in \nsession = " +
                                            regComp.getSession() +
                                            "\nruntype = " +
                                            regComp.getRunType()));

                            al.add(new cMsgPayloadItem("MSGACTION",
                                    "GLOBALRESET"));
                        } catch (cMsgException e) {
                            System.out.println(AfecsTool.stack2str(e));
                            lg.logger.severe(AfecsTool.stack2str(e));
                        }

                        // Inform GUIs
                        send(session + "/" + runType,
                                AConstants.UIControlPopupInfo,
                                al);
                        return false;

                        // Not configured, i.e. session = undefined
                    } else {
                        regComp.setExpid(getPlEXPID());
                        regComp.setSession(session);
                        regComp.setRunType(runType);
                        regComp.setConfigID(conf_id);
                        regComp.setSupervisor("sms_" + runType);
                        regComp.setClient(ci);
                    }

                    // No registration of the required agent
                    // has been found. This is a new request
                } else {
                    // Container of the agent is undefined
                    if (com.getHost().equals(AConstants.udf)) {

                        // See if there is a client with the same name
                        // registered. Assign this agent to the registered client
                        // and set the container host to be the platform host
                        com.setHost(myConfig.getPlatformHost());
                    }

                    APlatform.container.startAgent(com);
                    // Ask container admin to start a new agent

                    // wait for agent registration
                    int tout = 0;
                    do {
                        AfecsTool.sleep(100);
                        tout++;
                    } while ((tout < AConstants.TIMEOUT) &&
                            !APlatform.registrar.getAgentDir().containsKey(com.getName()));
                    if (tout > AConstants.TIMEOUT) {
                        reportAlarmMsg(session + "/" + runType,
                                myName,
                                9,
                                AConstants.ERROR,
                                "Agent " + com.getName() + " is not registered.");
                        lg.logger.severe(myName +
                                ":  Agent " + com.getName() + " is not registered.");
                        return false;
                    }
                }
            }

            // Update client database in the COOL_HOME
            APlatform.registrar.dumpClientDatabase();

//            AfecsTool.sleep(100);

            // Update client database in the memory by
            // reading back the client database file
            // from the COOL
//            APlatform.registrar.readClientDatabase();

            // See if supervisor agent exists
            if (c.getSupervisor() != null) {
                c.getSupervisor().setSession(session);
                c.getSupervisor().setRunType(runType);
                if (APlatform.registrar.getAgentDir().containsKey(c.getSupervisor().getName())) {

                    // This will setup supervisor agent, which also
                    // will send setup request to all supervised agents
                    send(c.getSupervisor().getName(),
                            AConstants.SupervisorControlRequestSetup, c);

                    // Start a new supervisor agent
                } else {
                    c.getSupervisor().setHost(myConfig.getContainerHost());

                    // Ask container admin to start a new supervisor agent
                    send(c.getSupervisor().getHost() + "_admin",
                            AConstants.ContainerControlStartSupervisor,
                            c);

                    // Wait until supervisor is registered
                    int tout = 0;
                    do {
                        AfecsTool.sleep(100);
                        tout++;
                    } while ((tout < AConstants.TIMEOUT) &&
                            !APlatform.registrar.getAgentDir().containsKey("sms_" + runType));

                    if (tout < AConstants.TIMEOUT) {

                        // Send setup message to the supervisor. 2 attempts.
                        try {
                            p2pSend(c.getSupervisor().getName(),
                                    AConstants.SupervisorControlRequestSetup,
                                    c,5000);
                        } catch (AException e) {
                            try {
                                p2pSend(c.getSupervisor().getName(),
                                        AConstants.SupervisorControlRequestSetup,
                                        c,5000);
                            } catch (AException ee) {
                                reportAlarmMsg(session + "/" + runType,
                                        myName,
                                        9,
                                        AConstants.ERROR,
                                        "Supervisor Agent " +
                                                c.getSupervisor().getName() + " is not active.");
                                lg.logger.severe(myName +
                                        ":  Supervisor Agent " +
                                        c.getSupervisor().getName() + " is not active.");
                                return false;
                            }
                        }
                    } else if (tout > AConstants.TIMEOUT) {
                        reportAlarmMsg(session + "/" + runType,
                                myName,
                                9,
                                AConstants.ERROR,
                                "Supervisor Agent " +
                                        c.getSupervisor().getName() + " is not registered.");
                        lg.logger.severe(myName +
                                ":  Supervisor Agent " +
                                c.getSupervisor().getName() + " is not registered.");
                        return false;
                    } else {
                        lg.logger.severe(myName +
                                ":  Problem communication with the platform registrar agent.");
                        return false;
                    }

                }
            } else {
                System.out.println("DDD ----| ERROR: control system supervisor is NULL");
                reportAlarmMsg(session + "/" + runType,
                        myName,
                        9,
                        AConstants.ERROR,
                        "Described control does not have a supervisor agent.");
            }
        } else {
            reportAlarmMsg(session + "/" + runType,
                    myName,
                    9,
                    AConstants.ERROR,
                    "Described control does not have components.");
            lg.logger.severe(myName +
                    ": Described control does not have components");
        }
        return true;
    }

    /**
     * <p>
     * Reads the $COOL_HOME/config/Control dir and
     * send the list of all file names to the sender.
     * </p>
     *
     * @return cMsgPayloadItem
     */
    private cMsgPayloadItem reportConfigs() {
        cMsgPayloadItem fl = null;
        ArrayList<String> al = new ArrayList<>();
        File dir = new File(myConfig.getCoolHome() + File.separator +
                myConfig.getPlatformExpid() + File.separator +
                "config" + File.separator +
                "Control");
        if (dir.exists()) {
            for (String s : dir.list()) {
                if (!(s.equals(".svn") || s.contains("~"))) {
                    al.add(s);
                }
            }
            try {
                fl = new cMsgPayloadItem("configFileNames", al.toArray(new String[al.size()]));
            } catch (cMsgException e) {
                lg.logger.severe(AfecsTool.stack2str(e));
            }
        }
        return fl;
    }


    /***************************************************************
     */

    /**
     * <p>
     * Private inner class for responding to control
     * messages addressed to the ControlDesigner agent
     * </p>
     */
    private class DesignerControlCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {


                if (definedRTVs != null) {
                    definedRTVs.clear();
                }
                String type = msg.getType();
                if (msg.getPayloadItem(AConstants.SESSION) == null ||
                        msg.getPayloadItem(AConstants.RUNTYPE) == null) return;
                try {
                    String session = msg.getPayloadItem(AConstants.SESSION).getString();
                    String runType = msg.getPayloadItem(AConstants.RUNTYPE).getString();

                    // get the map object from the message
                    // storing user runtime set RTVs
                    Map<String, String> setRTVs = null;
                    if (msg.getByteArray() != null) {
                        try {
                            setRTVs = (Map<String, String>) AfecsTool.B2O(msg.getByteArray());
                        } catch (IOException | ClassNotFoundException e) {
                            lg.logger.severe(AfecsTool.stack2str(e));
                        }
                    }

                    definedRTVs = AfecsTool.readRTVFile(runType, session);

                    if (definedRTVs != null) {

                        //update defined RTVs map with user current settings
                        if (setRTVs != null) {
                            for (String s : setRTVs.keySet()) {
                                if (definedRTVs.containsKey(s)) {
                                    definedRTVs.put(s, setRTVs.get(s));
                                }
                            }
                        }

                        // Update RTV map with new values for session,
                        // runType, userConfig and userDirectory, as well
                        // as resolve for any env variable that was defined
                        // in the cool
                        AfecsTool.updateRTV(definedRTVs, session, runType);

                        // update runType specific rtv file in the COOL_HOME
                        String fileName = myConfig.getCoolHome() + File.separator +
                                myConfig.getPlatformExpid() + File.separator +
                                "user" + File.separator +
                                "rtv" + File.separator +
                                runType + File.separator +
                                session + "_rtv.xml";

                        AfecsTool.writeRTVFile(fileName, definedRTVs);

                        // Update/exchange base _RTVs map
                        updateRTVMap(definedRTVs);
                    } else {
                        System.out.println("Error reading rtv file. See log for more details.");
                    }

                    // Configure request
                    if (type.equals(AConstants.DesignerControlRequestConfigureControl)) {
                        if (msg.isGetRequest()) {
                            cMsgMessage mr;

                            mr = msg.response();
                            mr.setSubject(AConstants.udf);
                            mr.setType(AConstants.udf);
                            mr.setText("success");

                            try {
                                // If there are unset RTVs other than %(rn) tell gui's
                                // that configuration fails due to unset RTVs
                                if (definedRTVs != null) {
                                    for (String s : definedRTVs.keySet()) {
                                        if (!s.equals("%(rn)") && definedRTVs.get(s).equals("unset")) {
                                            mr.setText("config_failed");
                                            myPlatformConnection.send(mr);
                                            return;
                                        }
                                    }

                                    mr.setByteArray(AfecsTool.O2B(definedRTVs));

                                    // design the control system
                                    parseControlDescription(session, runType, definedRTVs);
                                }

                            } catch (cMsgException e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                                mr.setText("config_failed");
                            }

                            myPlatformConnection.send(mr);
                        }

                    } else if (type.equals(AConstants.DesignerControlRequestConfigureControl_RCAPI)) {
                        if (msg.isGetRequest()) {
                            cMsgMessage mr;

                            mr = msg.response();
                            mr.setSubject(AConstants.udf);
                            mr.setType(AConstants.udf);
                            mr.setText("success");

                            // design the control system
                            parseControlDescription(session, runType, definedRTVs);

                            myPlatformConnection.send(mr);
                        }

                    } else if (type.equals(AConstants.DesignerControlRequestConfigureAgent)) {

                        // This message is sent by supervisor agent, asking
                        // to start specific agent on the specific container
                        if (msg.getPayloadItem(AConstants.AGENT) == null ||
                                msg.getPayloadItem(AConstants.CONTAINER) == null) return;
                        String agentName = msg.getPayloadItem(AConstants.AGENT).getString();
                        String containerHost = msg.getPayloadItem(AConstants.CONTAINER).getString();
                        if (msg.isGetRequest()) {
                            cMsgMessage mr;
                            mr = msg.response();
                            mr.setSubject(AConstants.udf);
                            mr.setType(AConstants.udf);
                            mr.setText("success");

                            mr.setByteArray(AfecsTool.O2B(definedRTVs));

                            try {

                                // If there are unset RTVs other than %(rn)tell gui's
                                // that configuration fails due to unset RTVs
                                if (definedRTVs != null) {
                                    for (String s : definedRTVs.keySet()) {
                                        if (!s.equals("%(rn)") && definedRTVs.get(s).equals("unset")) {
                                            mr.setText("config_failed");
                                            myPlatformConnection.send(mr);
                                            return;
                                        }
                                    }
                                }

                                // design the component
                                parseAndStartAgent(session,
                                        runType,
                                        agentName,
                                        containerHost,
                                        definedRTVs);

                            } catch (cMsgException e) {
                                lg.logger.severe(AfecsTool.stack2str(e));
                                mr.setText("config_failed");
                            }
                            myPlatformConnection.send(mr);
                        }
                    }
                } catch (Exception e) {
                    lg.logger.severe(AfecsTool.stack2str(e));
                }
            }
        }
    }

    /**
     * <p>
     * Private inner class for responding to
     * info messages addressed to this agent
     * </p>
     */
    private class DesignerInfoCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {

                String type = msg.getType();

                if (type.equals(AConstants.DesignerInfoRequestGetConfigs)) {
                    if (msg.isGetRequest()) {
                        try {
                            cMsgMessage mr = msg.response();
                            mr.setSubject(AConstants.udf);
                            mr.setType(AConstants.udf);
                            cMsgPayloadItem pi = reportConfigs();
                            if (pi != null) mr.addPayloadItem(pi);
                            myPlatformConnection.send(mr);
                        } catch (cMsgException e) {
                            lg.logger.severe(AfecsTool.stack2str(e));
                        }
                    }
                } else if (type.equals(AConstants.DesignerInfoRequestGetDefinedRTVs)) {
                    if (msg.isGetRequest()) {
                        if (msg.getPayloadItem(AConstants.SESSION) == null ||
                                msg.getPayloadItem(AConstants.RUNTYPE) == null) return;

                        String session = null;
                        String runType = null;
                        try {
                            session = msg.getPayloadItem(AConstants.SESSION).getString();
                            runType = msg.getPayloadItem(AConstants.RUNTYPE).getString();
                        } catch (cMsgException e) {
                            lg.logger.severe(AfecsTool.stack2str(e));
                        }
                        cMsgMessage mr = null;

                        try {
                            Map<String, String> rtvs = AfecsTool.readRTVFile(runType, session);
                            mr = msg.response();
                            mr.setSubject(AConstants.udf);
                            mr.setType(AConstants.udf);
                            if (rtvs != null) {
                                mr.setByteArray(AfecsTool.O2B(rtvs));
                            } else {
                                System.out.println("Error reading rtv file. See log for more details.");
                            }

                            myPlatformConnection.send(mr);
                        } catch (cMsgException | IOException e) {
                            lg.logger.severe(AfecsTool.stack2str(e));
                            if (mr != null) mr.setText(e.getMessage());
                        }

                    }
                } else if (type.equals(AConstants.DesignerInfoRequestControlAgents)) {
                    try {
                        String session = msg.getPayloadItem(AConstants.SESSION).getString();
                        String runType = msg.getPayloadItem(AConstants.RUNTYPE).getString();
                        if (msg.isGetRequest()) {
                            cMsgMessage mr;
                            mr = msg.response();
                            mr.setSubject(AConstants.udf);
                            mr.setType(AConstants.udf);
                            mr.setText("success");
                            if (msg.getPayloadItem(AConstants.SESSION) != null &&
                                    msg.getPayloadItem(AConstants.RUNTYPE) != null) {

                                // design the control system
                                parseControlDescription(session, runType);
                            }
                            myPlatformConnection.send(mr);
                        }
                    } catch (cMsgException e) {
                        lg.logger.severe(AfecsTool.stack2str(e));
                    }
                }
            }
        }
    }

}
