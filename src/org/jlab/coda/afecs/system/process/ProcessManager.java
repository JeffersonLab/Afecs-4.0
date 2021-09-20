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

package org.jlab.coda.afecs.system.process;

import com.sun.scenario.effect.impl.sw.java.JSWBlend_SRC_OUTPeer;
import org.jlab.coda.afecs.agent.AParent;
import org.jlab.coda.afecs.cool.ontology.*;
import org.jlab.coda.afecs.plugin.IAClientCommunication;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;

import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.afecs.system.util.StdOutput;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>
 * Process manager class.
 * Maintains the database of periodic processes:
 * {@link PeriodicProcess}
 * Presents methods for process execution:
 * {@link AProcess}
 * </p>
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */
public class ProcessManager {


    // HashMap of periodically running  processes
    private List<PeriodicProcess>
            monProcesses = new ArrayList<>();

    // Reference to an agent that
    // will instantiate this class
    private AParent owner;


    /**
     * <p>
     * Constructor stores the reference of
     * the agent that owns this process manager
     * </p>
     *
     * @param p AParent object reference
     */
    public ProcessManager(AParent p) {
        owner = p;
    }

    AParent getParent() {
        return owner;
    }

    /**
     * <p>
     * Stops all recorded periodic
     * process and clears the map.
     * </p>
     */
    public void stopAllPeriodicProcesses() {
        if (!monProcesses.isEmpty()) {
            for (PeriodicProcess pp : monProcesses) {
                pp.prStop();
            }
            monProcesses.clear();
            getParent().reportAlarmMsg(getParent().me.getSession() + "/" + getParent().me.getRunType(), getParent().me.getName(), 4,
                    AConstants.INFO, "Periodic scripts are stopped");

        }
    }

    /**
     * <p>
     * Executes the process defined in COOL.
     * </p>
     *
     * @param p      Reference to the process object
     * @param plugin reference to the plugin object, that extends
     *               {@link IAClientCommunication}
     *               interface
     * @param comp   Reference to the AComponent object
     *               {@link AComponent}
     * @return status of the execution ( true if succeeded)
     */
    public boolean executeProcess(AProcess p,
                                  IAClientCommunication plugin,
                                  AComponent comp) {
        boolean stat1 = true;
        boolean stat2 = true;
        if (p.getPeriodicity() == 0) {

            // Execute scripts first
            if (p.getScripts() != null && !p.getScripts().isEmpty()) {
                stat1 = _execShellScripts(p.getScripts(), comp, false);
            }

            // Now defined cMsg processes
            if (p.getSendPackages() != null && !p.getSendPackages().isEmpty()) {

                // Synchronous messaging
                if (p.getSync() != null && p.getSync().equals(AConstants.seton)) {

                    // Send a described packages.
                    for (APackage pck : p.getSendPackages()) {

                        // If cool does not define send or received subject,
                        // set the subject to the name of this agent. This
                        // means this agent represents the same name physical
                        // client
                        if (pck.getSendSubject().equals(AConstants.udf)) {
                            pck.setSendSubject(owner.myName);
                        }
                        if (pck.getReceivedSubject().equals(AConstants.udf)) {
                            pck.setReceivedSubject(owner.myName);
                        }

                        // Execution rc domain
                        if (pck.getForRcClient().equals(AConstants.seton)) {
                            stat2 = _sync_sendPckgUsingRc(pck, comp, p.getTimeout());

                            // Execution cMsg domain
                        } else if (pck.getForNativecMsg().equals(AConstants.seton)) {
                            stat2 = _sync_sendPckgUsingcMsg(pck, comp, p.getTimeout());

                            // Execution using described plugin
                            // timeout is defined from passed process
                        } else if (plugin != null) {
                            stat2 = _sync_execProcUsingPlugin(plugin, p, pck, comp);
                        }

                        // One of the send packages of the process failed to send
                        // fail the process execution
                        if (!stat2) return false;
                    }

                    // Asynchronous process execution.
                    // Async processes will always return false, meaning
                    // that the state of the agent will not be changed
                    // now, but only after receiving state from the client.
                    // Send a described packages.
                } else {

                    for (APackage pck : p.getSendPackages()) {

                        if (pck.getSendSubject().equals(AConstants.udf)) {
                            pck.setSendSubject(owner.myName);
                        }
                        if (pck.getReceivedSubject().equals(AConstants.udf)) {
                            pck.setReceivedSubject(owner.myName);
                        }

                        // Execution rc domain
                        if (pck.getForRcClient().equals(AConstants.seton)) {
                            stat2 = _async_sendPckgUsingRc(pck, comp);

                            // Execution using native cMsg
                        } else if (pck.getForNativecMsg().equals(AConstants.seton)) {
                            _async_sendPckgUsingcMsg(pck, comp);

                            // Execution using described plugin
                        } else if (plugin != null) {
                            stat2 = _async_execProcUsingPlugin(plugin, pck, comp);
                        }

                        // One of the send packages of the process failed to send
                        // fail the process execution
                        if (!stat2) return false;
                    }
                }
            }

            // periodic process request
        } else {

            monProcesses.add((_startPeriodicProcess(p, plugin, comp)));
        }
        return stat1;
    }


    /**
     * <p>
     * Executes a process by the name
     * </p>
     *
     * @param processName the name of the process
     * @param plugin      reference to the plugin object, that extends
     *                    {@link org.jlab.coda.afecs.plugin.IAClientCommunication}
     *                    interface
     * @param comp        Reference to the AComponent object
     *                    {@link org.jlab.coda.afecs.cool.ontology.AComponent}
     */
    public void executeDoProcess(String processName,
                                 IAClientCommunication plugin,
                                 AComponent comp) {
        if (comp.getProcesses() != null && !comp.getProcesses().isEmpty()) {
            for (AProcess p : comp.getProcesses()) {
                if (p.getName().equals(processName)) {
                    executeProcess(p, plugin, comp);
                    break;
                }
            }
        }
    }


    /**
     * <p>
     * Creates an instance of the PeriodicProcess
     * class and starts the periodic process tread.
     * </p>
     *
     * @param pr     AProcess reference
     * @param plugin AFecs plugin reference
     * @param comp   Reference to the AComponent object
     */
    private PeriodicProcess _startPeriodicProcess(AProcess pr,
                                                  IAClientCommunication plugin,
                                                  AComponent comp) {
        PeriodicProcess pp = new PeriodicProcess(pr, plugin, comp, this);
        pp.start();
        return pp;
    }

    /**
     * <p>
     * Defines and returns a payload structure
     * to be sent with the process execution
     * message in case process must be executed
     * using cMsg. This is a critical method since
     * CODA state machine processes are executed
     * using this method defined payload structure.
     * </p>
     *
     * @param pck  Process send package object reference.
     * @param comp Reference to the component object,
     *             namely to access defined configuration
     *             files. Note that configuration file
     *             name and content is passed to the
     *             external process as a parameter during
     *             the execution.
     * @return List of payload items required for the
     * process execution.
     */
    private ArrayList<cMsgPayloadItem> _defineSendPackagePayload(APackage pck,
                                                                 AComponent comp) {
        ArrayList<cMsgPayloadItem> al = null;

        if (pck.getSendText() != null) {
            al = new ArrayList<>();
//            System.out.println("DDD 3 =====================================");
//            System.out.println(comp);
//            System.out.println("DDD 3 =====================================");

            try {
                al.add(new cMsgPayloadItem(AConstants.RUNTYPE, comp.getRunType()));
                al.add(new cMsgPayloadItem(AConstants.CONFIGID, comp.getConfigID()));
                al.add(new cMsgPayloadItem(AConstants.CODAID, comp.getId()));
                al.add(new cMsgPayloadItem(AConstants.STREAMCOUNT, comp.getStreamCount()));
                al.add(new cMsgPayloadItem(AConstants.STREAMID, comp.getStreamId()));
                if (comp.getLinkNames() != null && comp.getLinkNames().length > 0)
                    al.add(new cMsgPayloadItem(AConstants.INPUTLINKS, comp.getLinkNames()));

                System.out.println("DDD 3 =====================================");
                System.out.println(comp.getName()+" "+" "+comp.getLinkedComponentNames().size() +" "+comp.getLinkNames() );
                System.out.println("DDD 3 ===================================== ");

                for (String n : comp.getLinkedIp().keySet()) {
                    al.add(new cMsgPayloadItem(AConstants.IPADDRESSLIST + "_" + n, comp.getLinkedIp().get(n)));
                }

                for (String n : comp.getLinkedBa().keySet()) {
                    al.add(new cMsgPayloadItem(AConstants.BROADCASTADDRESSLIST + "_" + n, comp.getLinkedBa().get(n)));
                }

            } catch (cMsgException e) {
                e.printStackTrace();
            }

            // For the coda rc communications if the text of the message contains $runtype,
            // this means we have to use the configured runType.
            if (pck.getSendText().contains("$runtype")) {
                String sendT = pck.getSendText().replace("$runtype", comp.getRunType());
                pck.setSendText(sendT);
            } else if (pck.getSendText().contains("configure")) {

                // Add platform hostIP and port payloads
                al.addAll(owner.getPlatformAddress());

                if (comp.getOption() != null) {

                    // If config file is defined read the content and send it  as a text
                    // otherwise send string=config
                    String conf = comp.getOption().getConfigFile();
                    if (!conf.equals(AConstants.udf)) {
                        // add component system configuration file data.
                        al.addAll(owner.getConfigFileContent(conf, comp, true));
                    }

                    // Add config string for the component if any
                    String confS = comp.getOption().getConfigString();
                    if (!confS.equals(AConstants.udf)) {
                        try {
                            al.add(new cMsgPayloadItem(AConstants.CONFSTRING, confS));
                        } catch (cMsgException e) {
                            e.printStackTrace();
                        }
                    }

                    // Add component user configuration file data.
                    String usrConf = comp.getUserConfig();
                    if (!usrConf.equals(AConstants.udf)) {
                        al.addAll(owner.getConfigFileContent(usrConf, comp, false));
                    }
                }

                // This is coda state machine specific process related staff
            } else if (pck.getName().equals("Download_Send_Package")) {
                try {
                    if (comp.getOption() != null) {
                        String s = comp.getOption().getDownloadString();
                        if (!s.equals(AConstants.udf)) {
                            al.add(new cMsgPayloadItem(AConstants.DOWNLOADSTRING, s));

                        }
                    }
                } catch (cMsgException e) {
                    e.printStackTrace();
                }

            } else if (pck.getName().equals("Prestart_Send_Package")) {
                try {
                    al.add(new cMsgPayloadItem(AConstants.RUNNUMBER, comp.getRunNumber()));
                    al.add(new cMsgPayloadItem(AConstants.FILEWRITING, comp.getFileWriting()));

                    if (comp.getOption() != null) {
                        String s = comp.getOption().getPrestartString();
                        if (!s.equals(AConstants.udf)) {
                            al.add(new cMsgPayloadItem(AConstants.PRESTARTSTRING, s));

                        }
                    }
                } catch (cMsgException e) {
                    e.printStackTrace();
                }

            } else if (pck.getName().equals("Go_Send_Package")) {
                try {
                    if (comp.getOption() != null) {
                        String s = comp.getOption().getGoString();
                        if (!s.equals(AConstants.udf)) {
                            al.add(new cMsgPayloadItem(AConstants.GOSTRING, s));

                        }
                    }
                } catch (cMsgException e) {
                    e.printStackTrace();
                }

            } else if (pck.getName().equals("End_Send_Package")) {
                try {
                    if (comp.getOption() != null) {
                        String s = comp.getOption().getEndString();
                        if (!s.equals(AConstants.udf)) {
                            al.add(new cMsgPayloadItem(AConstants.ENDSTRING, s));

                        }
                    }
                } catch (cMsgException e) {
                    e.printStackTrace();
                }
            }
        }

        return al;
    }

    /**
     * <p>
     * Executes the list of scripts defined for the process
     * Note that exit code = 777 means exit code is ignored.
     * </p>
     *
     * @param scr list of AProcess object references
     * @return true if succeeded
     */
    boolean _execShellScripts(ArrayList<AScript> scr,
                              AComponent comp, boolean isPeriodic) {
        boolean b = true;

        for (AScript sc : scr) {

            String commandLine = sc.getCommandString();

            // Run number substitution
            if (commandLine.contains("%(rn)")) {
                commandLine = commandLine.replace("%(rn)",
                        Integer.toString(comp.getRunNumber()));
            }


            if (!isPeriodic) {
                owner.reportAlarmMsg(comp.getSession() + "/" + comp.getRunType(), comp.getName(), 4,
                        AConstants.INFO, "Script (" + commandLine + ")");
            }
            if (sc.getSync() != null && sc.getSync().trim().equals(AConstants.seton)) {
                owner.reportAlarmMsg(comp.getSession() + "/" + comp.getRunType(), comp.getName(), 6,
                        AConstants.WARN, "Waiting sync-script to complete...");
            }

            StdOutput sp = null;
            ArrayList<String> l = new ArrayList<>();
            if (commandLine != null) {
                StringTokenizer st = new StringTokenizer(commandLine);
                while (st.hasMoreElements()) {
                    l.add(st.nextToken());
                }
                if (!l.isEmpty()) {
                    if (sc.getSync() != null && sc.getSync().trim().equals(AConstants.seton)) {
                        try {
                            sp = AfecsTool.fork(l, true);
                            System.err.println("Error: "
                                    + " executing  script \n"
                                    + sp.getStdErr());
                        } catch (AException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            sp = AfecsTool.fork(l, false);
                        } catch (AException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (sc.getExitCode() != 777 &&
                        sp != null && sp.getExitValue() != 777 &&
                        (sp.getExitValue() != sc.getExitCode())) {
                    b = false;
                }
            } else {
                b = false;
            }
        }
        return b;
    }

    /**
     * <p>
     * Private method for sync sending a package
     * using RC domain. The method will check the
     * returned text with the required one for
     * defining if the process failed or not.
     * </p>
     *
     * @param pck  package reference
     * @param comp reference to the component object
     * @param to   time out in seconds
     * @return true if succeeded
     */
    private boolean _sync_sendPckgUsingRc(APackage pck,
                                          AComponent comp,
                                          int to) {
        boolean b = true;

        to = to * 1000;

        ArrayList<cMsgMessage> backMessages = new ArrayList<>();

        ArrayList<cMsgPayloadItem> al = _defineSendPackagePayload(pck, comp);

        try {
            if (pck.getSendText() != null) {
                backMessages.add(owner.rcp2pSend(pck.getSendSubject(),
                        pck.getSendType(),
                        pck.getSendText(),
                        al,
                        to));
            } else {
                backMessages.add(owner.rcp2pSend(pck.getSendSubject(),
                        pck.getSendType(),
                        "",
                        al,
                        to));
            }
        } catch (AException e) {
            e.printStackTrace();
            __reconnect();
        }
        // check the return message
        for (cMsgMessage m : backMessages) {
            if (m != null &&
                    m.getSubject() != null &&
                    m.getType() != null &&
                    m.getSubject().equals(pck.getReceivedSubject()) &&
                    m.getType().equals(pck.getReceivedType())) {
                if (!pck.getReceivedText().contains(m.getText())) {
                    b = false;
                }
            }
        }

        return b;
    }

    /**
     * <p>
     * Private method for async sending a
     * package using RC domain. This method
     * sends a package through RC and forgets.
     * </p>
     *
     * @param pck  package reference
     * @param comp reference to the component object
     * @return true if succeeded
     */
    private boolean _async_sendPckgUsingRc(APackage pck,
                                           AComponent comp) {
        boolean b = true;
        ArrayList<cMsgPayloadItem> al = _defineSendPackagePayload(pck, comp);

        try {
            if (pck.getSendText() != null) {
                owner.rcSend(pck.getSendSubject(),
                        pck.getSendType(),
                        pck.getSendText(),
                        al);
            } else {
                owner.rcSend(pck.getSendSubject(),
                        pck.getSendType(),
                        "",
                        al);
            }
        } catch (cMsgException e) {
            e.printStackTrace();
            b = false;
        }
        return b;
    }

    /**
     * <p>
     * Private method for sync sending a package
     * using cMsg domain. (i.e. using platform cMsg
     * server) The method will check the returned
     * text with the required one for defining if
     * the process failed or not.
     * </p>
     *
     * @param pck  package reference
     * @param comp reference to the component object
     * @param to   time out in seconds
     * @return true if succeeded
     */
    private boolean _sync_sendPckgUsingcMsg(APackage pck,
                                            AComponent comp,
                                            int to) {
        boolean b = true;

        to = to * 1000;

        ArrayList<cMsgMessage> backMessages = new ArrayList<>();

        ArrayList<cMsgPayloadItem> al =
                _defineSendPackagePayload(pck, comp);

        cMsgMessage mx;
        // using native cMsg for execution
        if (pck.getSendText() != null) {
            try {
                mx = owner.p2pSend(pck.getSendSubject(),
                        pck.getSendType(),
                        pck.getSendText(),
                        al,
                        to);
            } catch (AException ignored) {
                return false;
            }
            if (mx != null) {
                backMessages.add(mx);
            }
        } else {
            try {
                mx = owner.p2pSend(pck.getSendSubject(),
                        pck.getSendType(),
                        "",
                        al,
                        to);
            } catch (AException ignored) {
                return false;
            }
            if (mx != null) {
                backMessages.add(mx);
            }
        }
        // check the return message
        for (cMsgMessage m : backMessages) {
            if (m.getSubject().equals(pck.getReceivedSubject()) &&
                    m.getType().equals(pck.getReceivedType())) {
                if (!pck.getReceivedText().contains(m.getText())) {
                    b = false;
                }
            }
        }
        return b;
    }

    /**
     * <p>
     * Private method for async sending a package
     * using cMsg domain. (i.e. platform cMsg server)
     * This method sends a package through RC and forgets.
     * </p>
     *
     * @param pck  package reference
     * @param comp reference to the component object
     */
    private void _async_sendPckgUsingcMsg(APackage pck,
                                          AComponent comp) {

        ArrayList<cMsgPayloadItem> al =
                _defineSendPackagePayload(pck, comp);

        // using native cMsg for execution
        if (pck.getSendText() != null) {
            owner.send(pck.getSendSubject(),
                    pck.getSendType(),
                    pck.getSendText(),
                    al);
        } else {
            owner.send(pck.getSendSubject(),
                    pck.getSendType(),
                    "",
                    al);
        }
    }

    /**
     * <p>
     * Private method for sync executing a process
     * using user specified plugin. The method will
     * check the returned package channels (plugin.
     * readChannel) and will compare with the required
     * channel settings, for defining if the process
     * failed or not.
     * </p>
     *
     * @param plugin reference to the plugin object
     * @param p      reference to the process object
     * @param pck    reference to the package object
     * @param comp   reference to the component object
     * @return true if described channels are properly set
     */
    private boolean _sync_execProcUsingPlugin(IAClientCommunication plugin,
                                              AProcess p,
                                              APackage pck,
                                              AComponent comp) {
        boolean b = true;
        for (AChannel ch : pck.getChannels()) {
            if (ch.getValueType() == AConstants.STRING) {
                String tmp = (String) ch.getSetValue();
                String ntmp;
                if (tmp.contains("$runtype")) {
                    ntmp = tmp.replace("$runtype", comp.getRunType());
                } else {
                    ntmp = tmp;
                }
                ch.setSetValue(ntmp);
            }

            if (plugin != null) {
                try {
                    plugin.setChannel(ch);
                } catch (AException e) {
                    owner.reportAlarmMsg(comp.getSession() + "/" + comp.getRunType(),
                            comp.getName(),
                            11,
                            AConstants.ERROR,
                            plugin.getDescription() + " communication error.");
                    owner.dalogMsg(comp,
                            11,
                            "ERROR",
                            plugin.getDescription() + " communication error.");
                    e.printStackTrace();
                    return false;
                }
            }
        }
        int tcount = 0;

        // check required receive packages.
        for (APackage ppck : p.getReceivePackages()) {

            // Read channels and compare with the
            // described receive package content.
            for (AChannel pch : ppck.getChannels()) {
                if (plugin != null) {
                    try {
                        while (!_isChannelSet(pch, plugin.readChannel(pch.getName()))) {
                            if (p.getTimeout() > 0) {
                                if (++tcount > p.getTimeout()) {
                                    b = false;
                                    break;
                                }
                                AfecsTool.sleep(1000);
                            }
                        }
                    } catch (AException e) {
                        owner.reportAlarmMsg(comp.getSession() + "/" + comp.getRunType(),
                                comp.getName(),
                                11,
                                AConstants.ERROR,
                                plugin.getDescription() + " communication error.");
                        owner.dalogMsg(comp,
                                11,
                                "ERROR",
                                plugin.getDescription() + " communication error.");
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        }
        return b;
    }

    /**
     * <p>
     * Private method for async executing a process
     * using user specified plugin.
     * </p>
     *
     * @param plugin reference to the plugin object
     * @param pck    reference to the package object
     * @param comp   reference to the component object
     * @return true if described channels are properly set
     */
    private boolean _async_execProcUsingPlugin(IAClientCommunication plugin,
                                               APackage pck,
                                               AComponent comp) {
        boolean b = true;
        for (AChannel ch : pck.getChannels()) {
            if (ch.getValueType() == AConstants.STRING) {
                String tmp = (String) ch.getSetValue();
                String ntmp;
                if (tmp.contains("$runtype")) {
                    ntmp = tmp.replace("$runtype", comp.getRunType());
                } else {
                    ntmp = tmp;
                }
                ch.setSetValue(ntmp);
            }

            if (plugin != null) {
                try {
                    plugin.setChannel(ch);
                } catch (AException e) {
                    owner.reportAlarmMsg(comp.getSession() + "/" + comp.getRunType(),
                            comp.getName(),
                            11,
                            AConstants.ERROR,
                            plugin.getDescription() + " communication error.");
                    owner.dalogMsg(comp,
                            11,
                            "ERROR",
                            plugin.getDescription() + " communication error.");
                    e.printStackTrace();
                    b = false;
                }
            }
        }
        return b;
    }


    /**
     * <p>
     * Compares two {@link org.jlab.coda.afecs.cool.ontology.AChannel} objects
     * </p>
     *
     * @param c1 AChannel object that was required to set
     * @param c2 AChannel object read after setting
     * @return true if equal
     */
    private boolean _isChannelSet(AChannel c1, AChannel c2) {
        return c1.equals(c2);
    }

    private void __reconnect() {
        if (owner.me != null && owner.me.getClient() != null) {

            if (owner.me.getClient().getHostIps() != null &&
                    owner.me.getClient().getHostIps().length > 0 &&
                    owner.me.getClient().getPortNumber() > 0) {

                // Disconnect from the client if connected
                owner.rcClientDisconnect();

                // Connect to the physical client
                String validHost;
                String udl = AConstants.udf;
                StringBuilder ul = new StringBuilder();
                for (int i = 0; i < owner.me.getClient().getHostIps().length; i++) {
                    validHost = owner.me.getClient().getHostIps()[i];
                    ul.append("rcs://");
                    ul.append(validHost);
                    ul.append(":");
                    ul.append(owner.me.getClient().getPortNumber());
                    ul.append("/");
                    ul.append(owner.me.getClient().getHostBroadcastAddresses()[i]);
                    ul.append(";");
                }
                if (ul.length() > 1) {
                    String tmp = ul.toString();
                    udl = tmp.substring(0, tmp.length() - 1);
                }
                if (!udl.equals(AConstants.udf)) {
                    if (!owner.rcClientConnect(udl)) {
                        System.out.println(owner.myName +
                                " Cannot connect to the physical component using udl = " +
                                udl);
                        owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                                owner.me.getName(),
                                11,
                                AConstants.ERROR,
                                "Cannot connect to the physical component using udl = " +
                                        udl);
                        owner.me.setState(AConstants.disconnected);

                    } else {
                        System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                                owner.myName + ":Info - connected to the client " + udl);

                        owner.me.setState(AConstants.connected);

                        // getString() returns host and port of a client that made a successful connection
                        String x = owner.myCRCClientConnection.getInfo(null);
                        if (x != null) {
                            String[] tokens = x.split(":");
                            owner.me.getClient().setHostName(tokens[0]);
                        }
                    }
                } else {
                    System.out.println(owner.myName +
                            " Undefined client udl = " + udl);
                    owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                            owner.me.getName(),
                            11,
                            AConstants.ERROR,
                            "Undefined client udl = " +
                                    udl);
                    owner.me.setState(AConstants.disconnected);
                }

            } else {
                System.out.println("Undefined host and port for the client.");
                owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                        owner.me.getName(),
                        11,
                        AConstants.ERROR,
                        "Undefined host and port for the client.");
            }
        }
    }

}
