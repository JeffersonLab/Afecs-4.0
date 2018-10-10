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
import org.jlab.coda.afecs.container.AContainer;
import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.influx.InfluxInjector;
import org.jlab.coda.afecs.platform.thread.PlatformSpy;
import org.jlab.coda.afecs.platform.thread.cMsgPingT;
import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.*;
import org.jlab.coda.cMsg.cMsgDomain.server.cMsgNameServer;
import org.jlab.coda.jinflux.JinFluxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * This is the main class that starts Afecs front-end
 * container, i.e. platform.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */
public class APlatform extends ABase {

    // Afecs platform start time
    private String startTime;

    // Platform registrar object
    public APlatformRegistrar registrar;

    // Platform container object
    public AContainer container;

    // Platform control designer object
    public AControlDesigner designer;

    private static boolean DaLogArchiveRequest = false;
    private static String DaLogArchivePath = "default";

    private ADaLogArchive daLogArchive;

    // the list of host ips in case multiple
    // network cards of a local host
    private Collection<String> platform_ips;

    private static boolean influxDb = false;

    /**
     * <p>
     * Platform main method
     * </p>
     *
     * @param args argument list
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "-archive":
                    DaLogArchiveRequest = true;
                    break;
                case "-debug":
                    AConstants.debug.set(true);
                    break;
                case "-web":
                    influxDb = true;
                    break;
                case "-h":
                case "-help":
                    System.out.println("Synopsis: platform -<option> <value>\n"
                            + "-h or -help\n"
                            + "-archive  [starts daLogMsg archiving normative agent] \n"
                            + "-web      [Enable web monitoring] \n"
                            + "-debug    [activates debugging flag] \n");
                    System.exit(0);
            }
        }

        if (cMsgConstants.version <= 3) {
            System.out.println("Afecs-4.x requires cMsg-4.0 and above to operate properly. " +
                    "Found cMsg version = " + cMsgConstants.version + " Exiting...");
            System.exit(1);
        }

        String coolHome = System.getenv("COOL_HOME");
        String platformExpid = System.getenv("EXPID");
        if (platformExpid == null) {
            System.out.println("Platform EXPID is not defined. Exiting...");
            System.exit(1);
        }

        if (coolHome == null) {
            System.out.println("Afecs database ($COOL_HOME) " +
                    "is not defined. Exiting....");
            System.exit(1);
        } else {
            File f = new File(coolHome);
            if (!f.exists()) {
                System.out.println("Afecs database does not exist. Exiting...");
                System.exit(1);
            }
            File ff = new File(coolHome + File.separator + platformExpid);
            if (!ff.exists()) {
                System.out.println("Afecs database for the EXPID = " + platformExpid +
                        " does not exist. Create a database with JCedit. Exiting...");
                System.exit(1);
            }
            File fff = new File(coolHome + File.separator + platformExpid + File.separator + "setup.xml");
            if (!fff.exists()) {
                System.out.println("Afecs database for the EXPID = " + platformExpid +
                        " does not have a proper structure. Recreate a database with JCedit. Exiting...");
                System.exit(-1);
            }

            File fcf = new File(coolHome + File.separator + platformExpid + File.separator + "config" + File.separator + "Control");
            if ((fcf.listFiles() != null) && (fcf.listFiles().length <= 0)) {
                System.out.println("Afecs database for the EXPID = " + platformExpid +
                        " is empty. Create a CODA configuration using JCedit. Exiting...");
                System.exit(1);
            }

            if (!f.canWrite()) {
                System.out.println("No write permission in COOL_HOME = " + coolHome + ". Exiting...");
                System.exit(1);
            }
        }

        // This is restart of the platform.
        // Remove the client registration database
        File ff = new File(coolHome +
                File.separator + platformExpid +
                File.separator + "ddb" +
                File.separator + "clientRegistration.xml");
        if (ff.exists()) {
            if (!ff.delete()) {
                System.out.println("Warning: can not delete " +
                        "clientRegistration.xml");
            }
        }
        new APlatform();
    }

    /**
     * <p>
     * Constructor
     * </p>
     */
    public APlatform() {
        super();
        myName = myConfig.getPlatformName();

        // Create registration databases, by reading
        // clientRegistration.xml and controlSessions.xml
        registrar = new APlatformRegistrar();

        // Start platform cMsg domain server
        startPlatformServer();
        AfecsTool.sleep(1000);

        // find IP addresses for this local host
        platform_ips = cMsgUtilities.getAllIpAddresses();

        // Connect to the platform cMsg domain server
        try {
            myPlatformConnection = platformConnect("cMsg://localhost" +
                    ":" + myConfig.getPlatformTcpPort() +
                    "/cMsg/" + myConfig.getPlatformName() + "?cmsgpassword=" + myConfig.getPlatformExpid());
        } catch (cMsgException e) {
            e.printStackTrace();
        }

        if (isPlatformConnected()) {

            // Start platform cMsg server pinging thread
            new cMsgPingT(myPlatformConnection).start();

            // Complete platform specific subscriptions
            doSubscriptions();
        } else {
            System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                    " " + myName +
                    ": Platform admin connection exception. Exiting ...");
            System.exit(1);
        }

        StringBuilder stb = new StringBuilder();
        startTime = AfecsTool.getCurrentTime();


        stb.append("**************************************************").append("\n");
        stb.append("*               Afecs-4 Platform                 *").append("\n");
        stb.append("**************************************************").append("\n");
        stb.append("- Name         = ").append(myConfig.getPlatformName()).append("\n");
        stb.append("- Host         = ").append(myConfig.getLocalHost()).append("\n");
        stb.append("- TCP port     = ").append(myConfig.getPlatformTcpPort()).append("\n");
        stb.append("- UDP port     = ").append(myConfig.getPlatformUdpPort()).append("\n");
        stb.append("- RC UDP port  = ").append(myConfig.getPlatformRcDomainUdpPort()).append("\n");
        stb.append("- Start time   = ").append(startTime).append("\n");
        stb.append("- Database at  = ").append(myConfig.getCoolHome()).append("\n");
        stb.append("- UDLRcMcast   = ").append(myConfig.getPlatformRcMulticastServerUdl()).append("\n");
        stb.append("**************************************************").append("\n");

        System.out.println(stb.toString());
        System.out.println("Searching for conflicting platforms. Please wait.....");

        // start FE container administrator
        container = new AContainer(false, this);

        // start daLogMsgArchive
        if (DaLogArchiveRequest) daLogArchive = new ADaLogArchive(DaLogArchivePath);

        // Start Platform Control Designer
        designer = new AControlDesigner(this);

        // Start a thread that periodically checks to see if other platform exists.
        // This is done by monitoring rcMulitcast servers with the same EXPID in the network.
        // Note that this thread will simply print the host and the port of the platform
        // (i.e. multicast server) on the console. No action will be taken. It is up to user
        // to stop the conflicting platform.
        new Thread(new PlatformSpy()).start();

        // start influxDB injector thread
        if (influxDb) {
            new Thread(() -> {
                try {
                    new InfluxInjector(true);
                } catch (JinFluxException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }


    /**
     * <p>
     * Starts Afecs platform cMsg server
     * </p>
     */
    private void startPlatformServer() {
        cMsgNameServer platformServer;
        platformServer = new cMsgNameServer(
                myConfig.getPlatformTcpPort(),
                myConfig.getPlatformTcpDomainPort(),
                myConfig.getPlatformUdpPort(),
                false,
                false,
                myConfig.getPlatformExpid(),
                null,
                null,
                cMsgConstants.debugNone,
                10);
        platformServer.setName(myConfig.getPlatformName());
        platformServer.startServer();
        startTime = AfecsTool.getCurrentTime();
    }

    /**
     * <p>
     * Platform Registration system basic subscriptions
     * </p>
     */
    private void doSubscriptions() {
        try {

            // Subscribe messages asking information about the platform
            myPlatformConnection.subscribe(myConfig.getPlatformName(),
                    AConstants.PlatformInfoRequest,
                    new PlatformInfoRequestCB(),
                    null);

            // Subscribe control messages
            myPlatformConnection.subscribe(myConfig.getPlatformName(),
                    AConstants.PlatformControl,
                    new PlatformControlRequestCB(),
                    null);


        } catch (cMsgException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     * Reads the $COOL_HOME/docs dir and
     * returns the list of all file names.
     * </p>
     *
     * @return List of document names
     */
    private List<String> reportDocs() {
        List<String> al = new ArrayList<>();
        if (new File(myConfig.getCoolHome() +
                File.separator + myConfig.getPlatformExpid() +
                File.separator + "docs").exists()) {
            File dir = new File(myConfig.getCoolHome() +
                    File.separator + myConfig.getPlatformExpid() +
                    File.separator + "docs");
            for (String s : dir.list()) {
                if (!(s.equals(".svn") || s.contains("~"))) al.add(s);
            }
        }
        return al;
    }

    /**
     * <p>
     * Reads the content of the document/file
     * and returns it as a string
     * </p>
     *
     * @param docName the name of the document
     * @return string of the content
     */
    private String getDocContent(String docName) {
        StringBuilder s = new StringBuilder();
        File f = new File(myConfig.getCoolHome() +
                File.separator + myConfig.getPlatformExpid() +
                File.separator + "docs" +
                File.separator + docName);
        if (f.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String d;
                if (docName.contains("html") || docName.contains("htm")) {
                    while ((d = br.readLine()) != null) {
                        s.append(d).append("\n");
                    }
                } else {
                    while ((d = br.readLine()) != null) {
                        s.append(d).append("<br>\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s.toString();
    }


    public void platformRegistrationRequestAdd(AComponent ar) {
        if (ar != null) {
            System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                    " " + myName +
                    ": Info -  Registration request from " +
                    ar.getName() +
                    " agent running at " +
                    ar.getHost());
            registrar.addAgent(ar);
        }
    }

    public void platformRegistrationRequestUpdate(AComponent ar) {
        System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                " " + myName +
                ": Info -  Registration update request from " +
                ar.getName() +
                " agent");
        registrar.addAgent(ar);
    }

    public void platformRegistrationRequestRemove(String name) {
        registrar.removeAgent(name);
    }

    public void platformRegistrationRequestSetRunNum(String session,
                                                     String runType,
                                                     int runNumber) {
        int cd = registrar.setSessionRunNumber(session, runNumber);
        if (cd < 0) {
            reportAlarmMsg(session +
                            "/" + runType,
                    myName,
                    9,
                    AConstants.ERROR,
                    " Error updating run number in the Cool DB.");
            if (container.getContainerSupervisors().containsKey("sms_" + runType)) {
                container.getContainerSupervisors().get("sms_" + runType).supervisorControlRequestFailTransition();
            }
        }
        registrar.dumpSessionsDatabase();
    }

    public int platformRegistrationRequestIncrementRunNum(String session,
                                                          String runType) {
        int cd = registrar.incrementSessionRunNumber(session);
        if (cd < 0) {
            reportAlarmMsg(session + "/" +
                            runType,
                    myName,
                    9,
                    AConstants.ERROR,
                    " Error incrementing run number in the Cool DB.");
            if (container.getContainerSupervisors().containsKey("sms_" + runType)) {
                container.getContainerSupervisors().get("sms_" + runType).supervisorControlRequestFailTransition();
            }
            return cd;
        } else {
            registrar.updateSessionRunType(session, runType);
            registrar.dumpSessionsDatabase();
            return registrar.getSessionRunNumber(session);
        }
    }

    public int platformRegistrationRequestReportRunNum(String session) {
        return registrar.getSessionRunNumber(session);
    }

    public void platformControlRegisterClient(AClientInfo cfo) {
        if (cfo == null) {
            System.out.println(myName + ":warning platform request to register Null client");
            return;
        }

        // Check to see if any other DB registered client
        // share the same host and port, remove if there is.
        registrar.checkDeleteClientDB(cfo.getName(),
                cfo.getHostName(),
                cfo.getPortNumber());

        if (registrar.getClientDir().containsKey(cfo.getName())) {
            registrar.getClient(cfo.getName()).setHostName(cfo.getHostName());
            registrar.getClient(cfo.getName()).setPortNumber(cfo.getPortNumber());
            registrar.getClient(cfo.getName()).setContainerHost(cfo.getContainerHost());
        } else {

            // This is the first JoinThePlatform request
            registrar.addClient(cfo);
        }

        // Open a file in the CoolHome and
        // write ClientDir hash table content
        registrar.dumpClientDatabase();

        if (registrar.getAgentDir().containsKey(cfo.getName())) {
            if (AConstants.debug.get())
                System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                        " " + myName +
                        ": Info -  agent for " + cfo.getName() +
                        " exists on the platform, adding new client information.");

            // Add new client information to the agent
            registrar.getAgentDir().get(cfo.getName()).setClient(cfo);
        }
        System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") +
                " " + myName +
                ": Info -  Component = " +
                cfo.getName() +
                " Host = " + cfo.getHostName() +
                " registered with the platform.");
        dalogMsg(myName, 3,
                "INFO",
                " Component = " +
                        cfo.getName() +
                        " Host = " +
                        cfo.getHostName() +
                        " registered with the platform.");

    }

    public Collection<String> getPlatform_ips() {
        return platform_ips;
    }


    /**
     * <p>
     * Private inner class for responding
     * to the platform control messages
     * </p>
     */
    private class PlatformControlRequestCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {

                String type = msg.getType();
                String requester = msg.getSender();

                // Agent name in question is passed through text field
                switch (type) {
                    case AConstants.PlatformControlGetAgentsDir:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.setByteArray(AfecsTool.O2B(registrar.getAgentDir()));
                                myPlatformConnection.send(mr);
                            } catch (cMsgException | IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            send(requester,
                                    AConstants.PlatformInfoResponseAgentsDir,
                                    registrar.getAgentDir());
                        }
                        break;

                    case AConstants.PlatformControlGetClientsDir:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.setByteArray(AfecsTool.O2B(registrar.getClientDir()));
                                myPlatformConnection.send(mr);
                            } catch (cMsgException | IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            send(requester,
                                    AConstants.PlatformInfoResponseClientsDir,
                                    registrar.getClientDir());
                        }
                        break;

                    case AConstants.PlatformControlRegisterClient:
                        break;

                    case AConstants.PlatformControlUpdateOptions:
                        if (msg.getPayloadItem(AConstants.RUNTYPE) != null &&
                                msg.getPayloadItem(AConstants.EVENTLIMIT) != null) {
                            try {
                                String rt = msg.getPayloadItem(AConstants.RUNTYPE).getString();
                                int evl = msg.getPayloadItem(AConstants.EVENTLIMIT).getInt();

                                registrar.updateOptionsEventLimit(rt, evl);

                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }

                        break;

                    case AConstants.PlatformControlArchiverStart:
                        if (daLogArchive == null) {
                            daLogArchive = new ADaLogArchive(DaLogArchivePath);
                            daLogArchive.startArchiving();
                        } else {
                            daLogArchive.startArchiving();
                        }

                        break;

                    case AConstants.PlatformControlArchiverStop:
                        if (daLogArchive != null) {
                            daLogArchive.stopArchiving();
                        }

                        break;
                    case AConstants.PlatformControlArchiverState:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                if (daLogArchive != null && daLogArchive.isArchiving()) {
                                    mr.setText(AConstants.active);
                                } else {
                                    mr.setText(AConstants.disabled);
                                }
                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case AConstants.PlatformControlRunLogBegin:
                        try {
                            if (msg.getPayloadItem(AConstants.SESSION) != null) {
                                String session = msg.getPayloadItem(AConstants.SESSION).getString();
                                registrar.createRunLog(msg.getText(), session, false);
                            }
                        } catch (cMsgException e) {
                            e.printStackTrace();
                        }

                        break;

                    case AConstants.PlatformControlRunLogEnd:
                        try {
                            if (msg.getPayloadItem(AConstants.SESSION) != null) {
                                String session = msg.getPayloadItem(AConstants.SESSION).getString();
                                if (msg.getPayloadItem(AConstants.CP_PREVIOUS_LOG) != null) {
                                    registrar.createRunLog(msg.getText(), session, true);
                                } else {
                                    registrar.createRunLog(msg.getText(), session, false);
                                }

                            }
                        } catch (cMsgException e) {
                            e.printStackTrace();
                        }

                        break;

                    case AConstants.PlatformControlRegisterAccount:
                        String node = null;
                        String userName = null;
                        String password = null;
                        try {
                            if (msg.getPayloadItem(AConstants.NODE) != null) {
                                node = msg.getPayloadItem(AConstants.NODE).getString();
                            }
                            if (msg.getPayloadItem(AConstants.USERNAME) != null) {
                                userName = msg.getPayloadItem(AConstants.USERNAME).getString();
                            }
                            if (msg.getPayloadItem(AConstants.PASSWORD) != null) {
                                password = msg.getPayloadItem(AConstants.PASSWORD).getString();
                            }
                        } catch (cMsgException e) {
                            e.printStackTrace();
                        }

                        if (node != null && userName != null && password != null) {
                            registrar.encryptAndSave(node, userName, password);
                        }
                        break;

                    case AConstants.PlatformControlGetSecretKey:
                        node = null;
                        userName = null;
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);

                                if (msg.getPayloadItem(AConstants.NODE) != null) {
                                    node = msg.getPayloadItem(AConstants.NODE).getString();
                                }
                                if (msg.getPayloadItem(AConstants.USERNAME) != null) {
                                    userName = msg.getPayloadItem(AConstants.USERNAME).getString();
                                }
                                if (node != null && userName != null) {
                                    byte[] sb = registrar.getSavedSecretKey(node, userName);
                                    if (sb != null && sb.length > 0) {
                                        mr.setByteArray(sb);
                                        myPlatformConnection.send(mr);
                                    }
                                }
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformControlGetPassword:
                        node = null;
                        userName = null;
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);

                                if (msg.getPayloadItem(AConstants.NODE) != null) {
                                    node = msg.getPayloadItem(AConstants.NODE).getString();
                                }
                                if (msg.getPayloadItem(AConstants.USERNAME) != null) {
                                    userName = msg.getPayloadItem(AConstants.USERNAME).getString();
                                }
                                if (node != null && userName != null) {
                                    byte[] pb = registrar.getSavedPassword(node, userName);
                                    if (pb != null && pb.length > 0) {
                                        mr.setByteArray(pb);
                                        myPlatformConnection.send(mr);
                                    }
                                }
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                }
            }
        }
    }

    public List<cMsgPayloadItem> platformInfoRequestReadConfgiFile(String txt,
                                                                   ArrayList<String> cif)
            throws IOException, cMsgException {

        List<cMsgPayloadItem> out = new ArrayList<>();
        for (String cfd : cif) {

            String f_name = cfd + File.separator + txt;
            String f_content = readFileAsString(f_name);
            long date = new File(f_name).lastModified();

            // Read emu based roc component configuration file.
            // It is constructed as roc_name.xml ( instead of roc_name.dat)
            if (txt.endsWith(".dat")) {

                // Then this is a roc. Read roc_name.xml file
                String emuFName = cfd + File.separator +
                        txt.substring(0, txt.lastIndexOf(".")) + ".xml";
                String emuFContent = readFileAsString(emuFName);
                if (emuFContent != null) {
                    out.add(new cMsgPayloadItem(AConstants.EMUROCCONFIG, emuFContent));
                }
            }
            if (f_content != null) {
                out.add(new cMsgPayloadItem(AConstants.FILECONTENT, f_content));
                out.add(new cMsgPayloadItem(AConstants.FILEDATE, date));
                break;
            }
        }
        return out;
    }

    /**
     * <p>
     * Private inner class for responding
     * to the platform info request messages
     * </p>
     */
    private class PlatformInfoRequestCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {

                String type = msg.getType();
                String requester = msg.getSender();
                String txt = msg.getText();

                // Agent name in question is passed through text field
                String agentName = msg.getText();
                switch (type) {
                    case AConstants.PlatformInfoRequestName:
                        send(requester,
                                AConstants.PlatformInfoResponseName,
                                myConfig.getPlatformName());

                        break;
                    case AConstants.PlatformInfoRequestHost:
                        send(requester,
                                AConstants.PlatformInfoResponseHost,
                                myConfig.getPlatformHost());

                        break;
                    case AConstants.PlatformInfoRequestAgents:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(requester);
                                mr.setType(AConstants.PlatformInfoResponseContainers);
                                mr.setByteArray(AfecsTool.O2B(registrar.getAgentDir()));
                                String confFileName = msg.getText();
                                if (confFileName != null) {

                                    long date = new File(confFileName).lastModified();
                                    String s = readFileAsString(confFileName);
                                    if (s != null) {
                                        mr.addPayloadItem(new cMsgPayloadItem(AConstants.FILECONTENT, s));
                                        mr.addPayloadItem(new cMsgPayloadItem(AConstants.FILEDATE, date));
                                    }
                                }
                                myPlatformConnection.send(mr);
                            } catch (cMsgException | IOException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                    case AConstants.PlatformInfoRequestGetRocDataLink:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                if (txt != null
                                        && msg.getPayloadItem(AConstants.DEFAULTOPTIONDIRS) != null) {
                                    String[] cif = msg.getPayloadItem(AConstants.DEFAULTOPTIONDIRS).getStringArray();
                                    for (String cfd : cif) {

                                        // Read emu based roc component configuration file.
                                        // It is constructed as roc_name.xml ( instead of roc_name.dat)
                                        if (txt.endsWith(".dat")) {

                                            // Then this is a roc. Read roc_name.xml file
                                            String emuFName = cfd + File.separator + txt;
                                            String emuSink = parseRocDataLink(emuFName);

                                            if (emuSink != null) {
                                                mr.setText(emuSink);
                                                break;
                                            }
                                        }
                                    }
                                }
                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                    case AConstants.PlatformInfoRequestContainers:
                        send(requester,
                                AConstants.PlatformInfoResponseContainers,
                                registrar.getContainerAdmins());

                        break;
                    case AConstants.PlatformInfoRequestPort:
                        send(requester,
                                AConstants.PlatformInfoResponsePort,
                                myConfig.getPlatformTcpPort());

                        break;
                    case AConstants.PlatformInfoRequestStartTime:
                        send(requester,
                                AConstants.PlatformInfoResponseStartTime,
                                startTime);
                        break;

                    case AConstants.PlatformInfoRequestAddNewSession:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.setText(AConstants.failed);
                                ASessionInfo si = (ASessionInfo) AfecsTool.B2O(msg.getByteArray());
                                if (si != null) {
                                    if (registrar.getSession(si.getName()) == null) {
                                        registrar.addSession(si);
                                        registrar.dumpSessionsDatabase();
                                        mr.setText("Ok");
                                    }
                                }
                                myPlatformConnection.send(mr);
                            } catch (cMsgException | ClassNotFoundException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestisRegistered:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                if (msg.getText() != null) {
                                    String n = msg.getText();
                                    if (registrar.getAgentDir().containsKey(n)) {
                                        mr.setText(AConstants.yes);
                                    } else {
                                        mr.setText(AConstants.no);
                                    }
                                }

                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestAgent:
                        if (agentName != null) {
                            send(requester,
                                    AConstants.PlatformInfoResponseAgent,
                                    registrar.getAgent(agentName));
                        }
                        break;

                    case AConstants.PlatformInfoRequestDocs:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.setByteArray(AfecsTool.O2B(reportDocs()));
                                myPlatformConnection.send(mr);
                            } catch (IOException | cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestDocument:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.setByteArray(AfecsTool.O2B(getDocContent(txt)));
                                myPlatformConnection.send(mr);
                            } catch (IOException | cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestSessions:
                        if (msg.isGetRequest()) {
                            registrar.readSessionDatabase();
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.setByteArray(AfecsTool.O2B(registrar.getSessionDir()));
                                myPlatformConnection.send(mr);
                            } catch (IOException | cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestSessionNames:
                        if (msg.isGetRequest()) {
                            registrar.readSessionDatabase();

                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.setByteArray(AfecsTool.O2B(registrar.getSessionNames()));
                                myPlatformConnection.send(mr);
                            } catch (IOException | cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestRunNumbers:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.setByteArray(AfecsTool.O2B(registrar.getRunNumbers(txt)));
                                myPlatformConnection.send(mr);
                            } catch (IOException | cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestIsArchiving:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                if (daLogArchive != null && daLogArchive.isArchiving()) {
                                    mr.setText(AConstants.yes);
                                } else {
                                    mr.setText(AConstants.no);
                                }
                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestActiveRunType:

                        // Sending through payload item so that it
                        // would be possible to use c/c++ clients
                        if (msg.isGetRequest()) {
                            String rt = AConstants.udf;
                            for (AComponent comp : registrar.getAgentDir().values()) {
                                if (comp.getName().startsWith("sms_")) {
                                    if (comp.getSession().equals(txt)) {
                                        rt = comp.getRunType();
                                        break;
                                    }
                                }
                            }

                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.addPayloadItem(new cMsgPayloadItem("activeruntype_p", rt));
                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case AConstants.PlatformInfoRequestActiveRunTypes:

                        // Sending through payload item so that it
                        // would be possible to use c/c++ clients
                        if (msg.isGetRequest()) {
                            ArrayList<AComponent> rts = new ArrayList<>();
                            for (AComponent comp : registrar.getAgentDir().values()) {
                                if (comp.getName().startsWith("sms_")) {
                                    rts.add(comp);
                                }
                            }

                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                if (!rts.isEmpty()) {
                                    try {
                                        mr.setByteArray(AfecsTool.O2B(rts));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestSessions_p:

                        // Sending through payload item so that it
                        // would be possible to use c/c++ clients
                        if (msg.isGetRequest()) {
                            registrar.readSessionDatabase();
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                ArrayList<String> l = registrar.getSessionNames();
                                mr.addPayloadItem(
                                        new cMsgPayloadItem("sessions_p",
                                                l.toArray(new String[l.size()])));
                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestAgents_p:

                        // Sending through payload item so that it
                        // would be possible to use c/c++ clients
                        if (msg.isGetRequest()) {
                            Set<String> st = registrar.getAgentDir().keySet();
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                mr.addPayloadItem(
                                        new cMsgPayloadItem("agents_p",
                                                st.toArray(new String[st.size()])));
                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case AConstants.PlatformInfoRequestDocs_p:
                        if (msg.isGetRequest()) {
                            try {
                                cMsgMessage mr = msg.response();
                                mr.setSubject(AConstants.udf);
                                mr.setType(AConstants.udf);
                                List<String> l = reportDocs();
                                if (l != null && !l.isEmpty()) {
                                    mr.addPayloadItem(
                                            new cMsgPayloadItem("docs_p",
                                                    l.toArray(new String[l.size()])));
                                }
                                myPlatformConnection.send(mr);
                            } catch (cMsgException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        }
    }

}
