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

package org.jlab.coda.afecs.container;

import org.jlab.coda.afecs.client.AClientInfo;
import org.jlab.coda.afecs.codarc.CodaRCAgent;
import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.cool.ontology.AControl;
import org.jlab.coda.afecs.platform.APlatform;
import org.jlab.coda.afecs.supervisor.SupervisorAgent;
import org.jlab.coda.afecs.system.*;
import org.jlab.coda.afecs.system.util.AClassLoader;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * Afecs Container.
 * <br>
 * Starts RC(p2p) multiCast server to listen udp
 * multiCast messages from physical clients, asking
 * to have a representative agent on the platform.
 * Subscribes basic container subscriptions.
 * Subscribe messages asking container to start
 * or stop agents.
 * Subscribes basic container subscriptions.
 * <ul>
 * <li>
 * Subscribe messages asking
 * container to start or stop agents
 * </li>
 * <li>
 * Subscribe messages asking to report
 * status of the container, that includes
 * the names and loads of all agents running
 * in this container.
 * </li>
 * </ul>
 * Starts agent on this container. Dynamically
 * load the requested agent class and create an
 * object of an agent to represent the real world
 * component.
 * <p></p>
 * Periodically reports ( every 3sec.) to the platform
 * administrator container specific details along with
 * agents information running in this container.
 * <p></p>
 * Starts a thread that will check platform connection
 * every 3 sec. In case platform is unresponsive it will
 * try to reconnect every 1sec.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */
public class AContainer extends ABase {


    // Platform broadcast server to listen
    // connect/join messages from physical clients
    private cMsg rcMultiCastServer;

    // Singleton object containing
    // system parameters and constants
    private AConfig myConfig;

    // Local container of all agents started by the
    // help of this container and running in the same jvm
    private ConcurrentHashMap<String, CodaRCAgent>
            containerAgents;

    // Local container of all supervisors started by the
    // help of this container and running in the same jvm
    private ConcurrentHashMap<String, SupervisorAgent>
            containerSupervisors;

    // AComponent object describing
    // this container admin agent
    private AComponent me;

    private boolean isMultiCast;

    private static HashMap<String, ClientJoinRequestPacket>
            requestPacketCounts = new HashMap<>();

    public APlatform myPlatform;

    public ConcurrentHashMap<String, CodaRCAgent> getContainerAgents() {
        return containerAgents;
    }

    public ConcurrentHashMap<String, SupervisorAgent> getContainerSupervisors() {
        return containerSupervisors;
    }

    /**
     * <p>
     * Constructor
     * </p>
     *
     * @param isMultiCast indicates if we need to connect
     *                    to the platform through multiCast
     */
    public AContainer(boolean isMultiCast, APlatform platform) {
        super();

        myPlatform = platform;
        this.isMultiCast = isMultiCast;
        me = new AComponent();
        myConfig = AConfig.getInstance();
        myName = myConfig.getContainerName();
        me.setName(myName);
        me.setDescription("Container Manager");
        me.setHost(myConfig.getContainerHost());
        me.setExpid(myConfig.getPlatformExpid());

        cMsgMessage msg = rcMonitor(300);
        if (msg != null) {
            if (!cMsgUtilities.isHostLocal(msg.getSenderHost())) {
                System.out.println("Attention!!! Detected platform already running on the host = " +
                        AfecsTool.getHostFromIP(msg.getSenderHost()) + " .Exiting...");
                System.exit(1);
            }

        }

        // Start RC multiCast server
        if (!startContainerRcMultiCastServer()) {
            System.err.println("Error: EXPID = " +
                    me.getExpid() +
                    " is in use.");
        } else {

            // Connect to the platform cMsg domain server
            if (isMultiCast) {
                try {
                    myPlatformConnection =
                            platformConnect(myConfig.getPlatformMulticastUdl());
                } catch (cMsgException e) {
                    e.printStackTrace();
                }
            } else {
                myPlatformConnection =
                        platformConnect();
            }

            if (isPlatformConnected()) {
                me.setStartTime(AfecsTool.getCurrentTime());

                // Complete container specific subscriptions
                doRcSubscription();

                // Register with the platform
                register();

            } else {
                register();
                System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                        " Severe: " + myName + ": Can not connect to the platform.");
                System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                        " Severe: " + myName + ": Can not connect to the platform. Exiting...");
                System.exit(1);
            }

            String stb = "**************************************************" + "\n" +
                    "*             Afecs-4 Container                  *" + "\n" +
                    "**************************************************" + "\n" +
                    "- Name                  = " + myConfig.getContainerName() + "\n" +
                    "- Host                  = " + myConfig.getContainerHost() + "\n" +
                    "- Start time            = " + AfecsTool.getCurrentTime() + "\n" +
                    "- Connected to:" + "\n" +
                    "- Platform Name         = " + myConfig.getPlatformName() + "\n" +
                    "- Platform Host         = " + myConfig.getPlatformHost() + "\n" +
                    "- Platform TCP port     = " + myConfig.getPlatformTcpPort() + "\n" +
                    "- Platform UDP port     = " + myConfig.getPlatformUdpPort() + "\n" +
                    "- Platform RC UDP port  = " + myConfig.getPlatformRcDomainUdpPort() + "\n" +
                    "**************************************************" + "\n";

            System.out.println(stb);

            // Platform connection monitoring thread
            new LifeLineT().start();
        }
        containerAgents = new ConcurrentHashMap<>();
        containerSupervisors = new ConcurrentHashMap<>();
    }

    /**
     * <p>
     * Starts RC(p2p) multiCast server to listen udp
     * multiCast messages from physical clients, asking
     * to have a representative agent on the platform.
     * </p>
     *
     * @return status
     */
    private boolean startContainerRcMultiCastServer() {
        boolean status = true;
        try {
            rcMultiCastServer =
                    new cMsg(myConfig.getPlatformRcMulticastServerUdl(),
                            myConfig.getPlatformRcDomainServerName(),
                            myConfig.getPlatformRcDomainServerDescription());
            rcMultiCastServer.connect();
            rcMultiCastServer.start();
        } catch (cMsgException e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    /**
     * <p>
     * Registration of the container
     * control agent with the platform
     * </p>
     */
    private void register() {
        myPlatform.platformRegistrationRequestAdd(me);
    }

    /**
     * Subscribe JoinThePlatform requests messages
     * coming from the real world physical components.
     */
    private void doRcSubscription() {

        if (rcMultiCastServer != null) {
            try {
                rcMultiCastServer.subscribe(myConfig.getPlatformName(),
                        AConstants.PlatformJoinRequest,
                        new JoinPlatformCB(),
                        null);
            } catch (cMsgException e) {
                e.printStackTrace();

                System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                        " Severe: " + myName +
                        ": JoinPlatform subscription fails. Exiting...");
                System.exit(1);
            }
        }
    }

    /**
     * <p>
     * Starts agent on this container. Dynamically
     * load the requested agent class and create an
     * object of an agent to represent the real world
     * component.
     * </p>
     *
     * @param a agent described by
     *          the {@link AComponent} object
     */
    public void startAgent(AComponent a) {
        if (!containerAgents.containsKey(a.getName())) {
            boolean stat = true;
            CodaRCAgent agent = null;
            if (a.getClassName().equals(AConstants.udf)) {
                agent = new CodaRCAgent(a, this);
            } else {

                // Dynamically load the requested agent class
                if (!_dynLoadAgentClass(a)) {
                    stat = false;
                }
            }
            if (stat && (agent != null) && (a.getName() != null)) {
                containerAgents.put(a.getName(), agent);
            }
        }
    }

    /**
     * <p>
     * Starts control supervisor agent.
     * </p>
     *
     * @param c supervisor agent, described
     *          {@link AControl} object
     */
    public void startSupervisor(AControl c) {
        boolean stat = true;
        SupervisorAgent sup = null;
        if (c.getSupervisor() != null &&
                c.getSupervisor().getType() != null) {
            if ((c.getSupervisor().getType().equalsIgnoreCase(ACodaType.SMS.name())) ||
                    (c.getSupervisor().getType().equalsIgnoreCase(ACodaType.RCS.name()))) {
                sup = new SupervisorAgent(c.getSupervisor(), this);
            }
        } else {
            stat = false;
        }
        if (stat && (sup != null) && (c.getSupervisor().getName() != null)) {
            containerSupervisors.put(c.getSupervisor().getName(), sup);
        }
    }

    /**
     * <p>
     * Dynamically loads a class.
     * The name of the class and the path to the
     * class is described in {@link AComponent}
     * object. Note that component getClassName
     * returns the full path name to a class file.
     * <p>
     * </p>
     *
     * @param a AComponent object
     * @return false if loading and creating
     * a new instance fails.
     */
    private boolean _dynLoadAgentClass(AComponent a) {

        String comClassName = a.getClassName();
        if (!comClassName.equals(AConstants.udf)) {

            // Get the class path
            String comClassPath = a.getClassPath();
            try {
                URL[] urls;

                // Convert the file object to a URL
                File dir = new File(comClassPath + File.separator);
                URL url;
                url = dir.toURI().toURL();
                urls = new URL[]{url};

                // Create a new class loader with the directory
                AClassLoader cl = new AClassLoader(urls);
                cl.setClassesToLoad(new String[]{comClassName});

                // Load the class
                Class<?> cls = cl.loadClass(comClassName);

                // Do not instantiate if requested
                // agent class is not extending ARAgent
                if (cls.getSuperclass().getName().equals("ARAgent")) {
                    try {
                        Constructor c = cls.getConstructor(AComponent.class);
                        Object o = c.newInstance(a);
                        if (o instanceof CodaRCAgent) return true;
                    } catch (NoSuchMethodException | InvocationTargetException e) {
                        e.printStackTrace();

                    }
                }
            } catch (MalformedURLException | ClassNotFoundException
                    | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();

            }
        }
        return false;
    }


    /**
     * <p>
     * Message is coming from the real world client.
     * Following information are available:
     * <ul>
     * <l>The name of the client = message sender</l>
     * <l>Client's host = message senderHost</l>
     * <l>Client's port = message userInt</l>
     * <l>
     * List of IP addresses in case host
     * client host has multiple network cards.
     * If multiple host are reported client
     * host will be reassigned to the first IP
     * address in the list.
     * </l>
     * <p>
     * <p>
     * </ul>
     * <p>
     * </p>
     *
     * @param msg cMsgMessage object
     */
    private synchronized void joinPlatformAction(cMsgMessage msg) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {

            String sender = msg.getSender();
            String clHost = msg.getSenderHost();

            int clPort = msg.getUserInt();

            // Create client info object, used
            // to partially fill AComponent object
            AClientInfo cif = new AClientInfo();
            cif.setName(sender);
            cif.setHostName(clHost);
            cif.setPortNumber(clPort);
            cif.setContainerHost(me.getHost());

            // Get client IPs in case client has
            // multiple network cards/interfaces
            try {
                if (msg.getPayloadItem("IpAddresses") != null) {
                    cif.setHostIps(msg.getPayloadItem(
                            "IpAddresses").getStringArray());
                    System.out.println("DDD 2 ====================================");
                    System.out.println(cif.getHostIps());
                    System.out.println("DDD 2 ====================================");

                }

                if (msg.getPayloadItem("BroadcastAddresses") != null) {
                    cif.setHostBroadcastAddresses(msg.getPayloadItem("BroadcastAddresses").getStringArray());
                }

                if (msg.getPayloadItem("SenderId") != null) {
                    cif.setRequestId(msg.getPayloadItem("SenderId").getInt());
                }
            } catch (cMsgException e) {
                e.printStackTrace();

            }

            System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                    " " + myName +
                    ": Request to join the platform from " +
                    sender +
                    ": host = " + clHost + " port = " + clPort);
            reportAlarmMsg(me.getSession() + "/" + me.getRunType(),
                    myName,
                    7,
                    AConstants.WARN,
                    " Request to join the platform from " +
                            sender +
                            ": host = " + clHost + " port = " + clPort +
                            ". " + AfecsTool.getCurrentTimeInH());

            if (!clHost.equals(AConstants.udf) && clPort > 0) {

                if (containerAgents.containsKey(sender)) {
                    CodaRCAgent _agent = containerAgents.get(sender);
                    String clientState = _agent._getClientState(3000, 1000);
                    if (!clientState.equals(AConstants.udf)) {
                        System.out.println("DDD ----| Warning: Rejecting (state = " +
                                clientState + ") " +
                                sender +
                                " request to connect...");
                        return;
                    }

                    if (_agent.me.getClient() != null &&
                            _agent.me.getClient().getRequestId() != cif.getRequestId()) {
                        System.out.println("DDD ----| Info: Reconnecting to the client = " + sender);
                    }
                    _agent.me.setClient(cif);
                    _agent._stopCommunications();
                    _agent._stopClientHeartBeatMonitor();
                    _agent._reconnectResponse(_agent.me.getClient());
                } else {
                    // Create AComponent object for this client
                    AComponent comp = new AComponent();
                    comp.setName(sender);
                    comp.setHost(myConfig.getContainerHost());
                    comp.setClient(cif);
                    startAgent(comp);
                }
            }
            CodaRCAgent newUpdatedAgent = getContainerAgents().get(sender);
            for (String linkedCompName : newUpdatedAgent.me.getLinkedComponentNames()) {
                CodaRCAgent linkedAgent = getContainerAgents().get(linkedCompName);
                linkedAgent.agentControlRequestNetworkDetails(
                        newUpdatedAgent.myName,
                        newUpdatedAgent.me.getClient().getHostIps(),
                        newUpdatedAgent.me.getClient().getHostBroadcastAddresses()
                );
            }

        });
        executorService.shutdown();
    }


    /**
     * <p>
     * Private inner class for responding
     * to the messages from the physical clients,
     * requesting to join the platform.
     * </p>
     */
    private class JoinPlatformCB extends cMsgCallbackAdapter {

        // overwrite to increase callback queue size (1000 messages)
        public int getMaximumQueueSize() {
            return 10000;
        }

        public void callback(cMsgMessage msg, Object userObject) {


            if (msg != null &&
                    msg.getPayloadItem("packetCount") != null &&
                    msg.getPayloadItem("SenderId") != null) {
                String sender = msg.getSender();
                System.out.println("DDD 1 ====================================");
                System.out.println(sender +" request to join the platform");
                System.out.println("DDD 1 ====================================");

                try {
                    int packetCount = msg.getPayloadItem("packetCount").getInt();
                    int senderId = msg.getPayloadItem("SenderId").getInt();

                    if (!requestPacketCounts.containsKey(sender)) {
                        requestPacketCounts.put(sender, new ClientJoinRequestPacket(senderId, packetCount));
                        joinPlatformAction(msg);
                    } else {
                        ClientJoinRequestPacket jr = requestPacketCounts.get(sender);

                        if (jr.getSenderId() != senderId) {

                            jr.setSenderId(senderId);
                            jr.setPacketCount(packetCount);
                            joinPlatformAction(msg);
                        } else {
                            // do not service the callback if this is a burst of requests

                            if (packetCount <= jr.getPacketCount()) {

                                jr.setPacketCount(packetCount);
                                joinPlatformAction(msg);
                            }
                        }
                    }
                } catch (cMsgException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                        " " + myName +
                        ": ERROR - Malformed request. PacketCount payload is missing. ");
            }
        }
    }


    /**
     * <p>
     * Thread will check platform connection every 3 sec.
     * In case platform is unresponsive it will
     * try to reconnect every 1sec.
     * </p>
     */
    public class LifeLineT extends Thread {

        private boolean isActive = true;

        @Override
        public void run() {
            super.run();
            while (isActive) {

                if (!isPlatformConnected()) {
                    System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                            " " + myName +
                            ": Info -  Lost connection to the platform!");
                    while (!isPlatformConnected()) {
                        if (isMultiCast) {
                            try {
                                myPlatformConnection =
                                        platformConnect(myConfig.getPlatformMulticastUdl());
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        } else {
                            myPlatformConnection =
                                    platformConnect();
                        }
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();

                        }
                    }

                    // Register with the platform
                    register();

                }

                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }
        }

    }

}
