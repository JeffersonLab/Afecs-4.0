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

package org.jlab.coda.afecs.fcs;

import org.jlab.coda.afecs.codarc.CodaRCAgent;
import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.system.AConfig;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.*;
import org.jlab.coda.cMsg.cMsgConstants;
import org.jlab.coda.cMsg.cMsgDomain.server.cMsgNameServer;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gurjyan
 *         Date: 1/2/14 Time: 4:52 PM
 * @version 4.x
 */
public class FcsEngine {

    // main map holding information of all user processes under control of this agent.
    // key is the host, i.e. only one process on a host will be recorded
    private ConcurrentHashMap<String, FProcessInfo> fProcesses = new ConcurrentHashMap<String, FProcessInfo>();

    private CodaRCAgent myAgent;
    private AComponent myCodaComponent;

    private AConfig myConfig;

    private String fcs_udl = AConstants.udf;
    private String fcs_multicast_udl = AConstants.udf;

    private List<String> _nodes = null;
    private String downloadLaunchCmd = AConstants.udf;
    private String prestartLaunchCmd = AConstants.udf;
    private String goLaunchCmd = AConstants.udf;
    private String endLaunchCmd = AConstants.udf;

    // Constructor
    public FcsEngine(CodaRCAgent agent) {
        myAgent = agent;
        myCodaComponent = myAgent.getAgentCOOLComponent();


        // get platform configuration information
        myConfig = AConfig.getInstance();

        // define local cMsg server parameters
        int fcs_tcp_port = myConfig.getPlatformTcpPort() + 7;
        int fcs_tcp_domain_port = myConfig.getPlatformTcpDomainPort() + 7;
        int fcs_udp_port = myConfig.getPlatformUdpPort() + 7;
        String fcs_server_name = myConfig.getPlatformExpid() + "_fcs";

        // start local cMsg server for private FCS communications
        startFcsLocalServer(fcs_server_name, fcs_tcp_port, fcs_tcp_domain_port, fcs_udp_port);


        // define UDL of the local cMsg server
        String localHost;
        // first get the local host of the fcs
        try {
            InetAddress addr = InetAddress.getLocalHost();
            localHost = addr.getHostName();
            fcs_udl = "cMsg://" + localHost + ":" + fcs_tcp_port + "/cMsg/" + fcs_server_name + "?cmsgpassword=" + myConfig.getPlatformExpid();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            e.printStackTrace();
        }

        fcs_multicast_udl = "cMsg://multicast:" + fcs_udp_port + "/cMsg/" + fcs_server_name + "?cmsgpassword=" + myConfig.getPlatformExpid();
    }

    public boolean configure() {
        String configureLaunchCmd = AConstants.udf;
        downloadLaunchCmd = AConstants.udf;
        prestartLaunchCmd = AConstants.udf;
        goLaunchCmd = AConstants.udf;
        endLaunchCmd = AConstants.udf;

        // find et information from the system configuration file in Options dir
        String systemConfig = requestSystemConfig(myCodaComponent);

        // parse user configuration, containing node and farm process command descriptions.
        String[] tags = {"nodes",
                "configure-cmd",
                "download-cmd",
                "prestart-cmd",
                "go-cmd",
                "end-cmd"
        };
        List<XMLContainer> fcd = null;

        if (!myCodaComponent.getUserConfig().equals(AConstants.udf)) {
            // informing rcgui start of a parsing process of the user config file
            myAgent.reportAlarmMsg(myAgent.me.getSession() + "/" + myAgent.me.getRunType(),
                    myAgent.myName,
                    1,
                    AConstants.INFO,
                    " User config = " + myCodaComponent.getUserConfig());
            fcd = AfecsTool.parseXML(myCodaComponent.getUserConfig(),
                    "farm", tags);
        }

        if (systemConfig != null && fcd != null && !fcd.isEmpty()) {

            // parse the system config file from the Options
            // dir to get JCedit specified et information
            FCSConfigData data = AfecsTool.parseFcsSystemConfigXml(systemConfig);

            for (XMLContainer xmlCont : fcd) {
                String nodes;
                for (XMLTagValue tv : xmlCont.getContainer()) {
                    if (tv.getTag().equals("nodes")) {
                        nodes = tv.getValue();
                        if (!nodes.equals(AConstants.udf)) {
                            // get the names of farm nodes
                            _nodes = new ArrayList<>();
                            StringTokenizer st = new StringTokenizer(nodes);
                            while (st.hasMoreTokens()) {
                                String s = st.nextToken();
                                _nodes.add(s);
                            }
                        }
                    } else if (tv.getTag().equals("configure-cmd")) {
                        configureLaunchCmd = EtUdlSubstitute(tv.getValue(), data);
                    } else if (tv.getTag().equals("download-cmd")) {
                        downloadLaunchCmd = EtUdlSubstitute(tv.getValue(), data);
                    } else if (tv.getTag().equals("prestart-cmd")) {
                        prestartLaunchCmd = EtUdlSubstitute(tv.getValue(), data);
                    } else if (tv.getTag().equals("go-cmd")) {
                        goLaunchCmd = EtUdlSubstitute(tv.getValue(), data);
                    } else if (tv.getTag().equals("end-cmd")) {
                        endLaunchCmd = EtUdlSubstitute(tv.getValue(), data);
                    }
                }

                try {
                    if (!configureLaunchCmd.equals(AConstants.udf) && _nodes != null && !_nodes.isEmpty()) {
                        for (String nn : _nodes) {
                            myAgent.reportAlarmMsg(myAgent.me.getSession() + "/" + myAgent.me.getRunType(),
                                    myAgent.myName,
                                    1,
                                    AConstants.INFO,
                                    " started process = " + nn + " " + configureLaunchCmd);

                            System.out.println("FCS-info:" + AfecsTool.getCurrentTime() + " started process = " + nn + " " + configureLaunchCmd);
//                            AfecsTool.sleep(200);

                            StdOutput o = AfecsTool.fork("ssh  " + nn + " " + configureLaunchCmd, false);
                            String err = o.getStdErr();
                            if (err != null && !err.equals("")) {
                                System.out.println("Afecs-FCS: " + err);
                            }
                        }
                    }
                } catch (AException e) {
                    e.printStackTrace();
                }

                // stop all user processes using local stored pids
                killAllRegisteredProcesses();

            }
        } else {
            System.out.println(myCodaComponent.getName() + " Failed retrieving the configuration file. ");
        }
        return true;
    }

    private String EtUdlSubstitute(String cd, FCSConfigData data) {
        String cmd = cd;
        cmd = cmd.replaceAll("%inETName", data.getInputEtName());
        cmd = cmd.replaceAll("%inETHost", data.getInputEtHost());
        cmd = cmd.replaceAll("%inETPort", Integer.toString(data.getInputEtPort()));

        cmd = cmd.replaceAll("%outETName", data.getOutputEtName());
        cmd = cmd.replaceAll("%outETHost", data.getOutputEtHost());
        cmd = cmd.replaceAll("%outETPort", Integer.toString(data.getOutputEtPort()));

        cmd = cmd.replaceAll("%udl", fcs_udl);
        cmd = cmd.replaceAll("%mUdl", fcs_multicast_udl);
        return cmd;
    }

    public boolean download() {
        try {
            if (!downloadLaunchCmd.equals(AConstants.udf) && _nodes != null && !_nodes.isEmpty()) {
                for (String nn : _nodes) {

                    myAgent.reportAlarmMsg(myAgent.me.getSession() + "/" + myAgent.me.getRunType(),
                            myAgent.myName,
                            1,
                            AConstants.INFO,
                            " started process = " + nn + " " + downloadLaunchCmd);
                    System.out.println("FCS-info:" + AfecsTool.getCurrentTime() + " started process = " + nn + " " + downloadLaunchCmd);
                    AfecsTool.sleep(200);
                    // start processes
                    StdOutput o = AfecsTool.fork("ssh  " + nn + " " + downloadLaunchCmd + " &", false);
                    FProcessInfo pi = new FProcessInfo();
                    String io = o.getStdio();
                    String err = o.getStdErr();

                    if (err != null && !err.equals("")) {
                        System.out.println("Afecs-FCS: " + err);
                    }

                    if (io != null && !io.equals("")) {
                        StringTokenizer st = new StringTokenizer(io);
                        st.nextToken();
                        pi.setId(AfecsTool.isNumber(st.nextToken()));
                        pi.setCommand(downloadLaunchCmd);
                        pi.setHost(nn);
                        fProcesses.put(pi.getHost(), pi);
                    }
                }
            }
        } catch (AException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean prestart() {
        try {
            if (!prestartLaunchCmd.equals(AConstants.udf) && _nodes != null && !_nodes.isEmpty()) {
                for (String nn : _nodes) {

                    myAgent.reportAlarmMsg(myAgent.me.getSession() + "/" + myAgent.me.getRunType(),
                            myAgent.myName,
                            1,
                            AConstants.INFO,
                            " started process = " + nn + " " + prestartLaunchCmd);
                    System.out.println("FCS-info:" + AfecsTool.getCurrentTime() + " started process = " + nn + " " + prestartLaunchCmd);
                    AfecsTool.sleep(200);

                    // start processes
                    StdOutput o = AfecsTool.fork("ssh  " + nn + " " + prestartLaunchCmd + " &", false);
                    FProcessInfo pi = new FProcessInfo();
                    String io = o.getStdio();
                    String err = o.getStdErr();

                    if (err != null && !err.equals("")) {
                        System.out.println("Afecs-FCS: " + err);
                    }

                    if (io != null && !io.equals("")) {
                        StringTokenizer st = new StringTokenizer(io);
                        st.nextToken();
                        pi.setId(AfecsTool.isNumber(st.nextToken()));
                        pi.setCommand(prestartLaunchCmd);
                        pi.setHost(nn);
                        fProcesses.put(pi.getHost(), pi);
                    }
                }
            }
        } catch (AException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean go() {
        try {
            if (!goLaunchCmd.equals(AConstants.udf) && _nodes != null && !_nodes.isEmpty()) {
                for (String nn : _nodes) {

                    myAgent.reportAlarmMsg(myAgent.me.getSession() + "/" + myAgent.me.getRunType(),
                            myAgent.myName,
                            1,
                            AConstants.INFO,
                            " started process = " + nn + " " + goLaunchCmd);
                    System.out.println("FCS-info:" + AfecsTool.getCurrentTime() + " started process = " + nn + " " + goLaunchCmd);
                    AfecsTool.sleep(200);

                    // start processes
                    StdOutput o = AfecsTool.fork("ssh  " + nn + " " + goLaunchCmd + " &", false);
                    FProcessInfo pi = new FProcessInfo();
                    String io = o.getStdio();
                    String err = o.getStdErr();

                    if (err != null && !err.equals("")) {
                        System.out.println("Afecs-FCS: " + err);
                    }

                    if (io != null && !io.equals("")) {
                        StringTokenizer st = new StringTokenizer(io);
                        st.nextToken();
                        pi.setId(AfecsTool.isNumber(st.nextToken()));
                        pi.setCommand(goLaunchCmd);
                        pi.setHost(nn);
                        fProcesses.put(pi.getHost(), pi);
                    }
                }
            }
        } catch (AException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean end() {
        try {
            if (!endLaunchCmd.equals(AConstants.udf) && _nodes != null && !_nodes.isEmpty()) {
                for (String nn : _nodes) {

                    myAgent.reportAlarmMsg(myAgent.me.getSession() + "/" + myAgent.me.getRunType(),
                            myAgent.myName,
                            1,
                            AConstants.INFO,
                            " started process = " + nn + " " + endLaunchCmd);
                    System.out.println("FCS-info:" + AfecsTool.getCurrentTime() + " started process = " + nn + " " + endLaunchCmd);
                    AfecsTool.sleep(200);

                    // start processes
                    StdOutput o = AfecsTool.fork("ssh  " + nn + " " + endLaunchCmd + " &", false);
                    FProcessInfo pi = new FProcessInfo();
                    String io = o.getStdio();
                    String err = o.getStdErr();

                    if (err != null && !err.equals("")) {
                        System.out.println("Afecs-FCS: " + err);
                    }

                    if (io != null && !io.equals("")) {
                        StringTokenizer st = new StringTokenizer(io);
                        st.nextToken();
                        pi.setId(AfecsTool.isNumber(st.nextToken()));
                        pi.setCommand(endLaunchCmd);
                        pi.setHost(nn);
                        fProcesses.put(pi.getHost(), pi);
                    }
                }
            }
        } catch (AException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void killAllRegisteredProcesses() {

        // stop all user processes  using local pids
        if (!fProcesses.isEmpty()) {
            for (FProcessInfo pi : fProcesses.values()) {
                if (pi != null) {
                    try {
                        StdOutput o = AfecsTool.fork("ssh  " + pi.getHost() + " kill -9 " + pi.getId(), false);
                        String err = o.getStdErr();
                        if (err != null && !err.equals("")) {
                            System.out.println("Afecs-FCS: " + err);
                        }
                    } catch (AException e) {
                        e.printStackTrace();
                    }
                }
            }
            fProcesses.clear();
        }
    }

    public String requestSystemConfig(AComponent comp) {
        String fileContent = null;
        String conf = comp.getName() + "_fc.xml";

        if (!conf.equals(AConstants.udf)) {
            List<cMsgPayloadItem> res;
            try {
                res = myAgent.myPlatform.platformInfoRequestReadConfigFile(conf, comp.getDod());
                if (res != null && !res.isEmpty()) {
                    for (cMsgPayloadItem pi : res) {
                        if (pi.getName().equals(AConstants.FILECONTENT)) {
                            fileContent = pi.getString();
                        }
                    }
                }
            } catch (IOException | cMsgException e) {
                e.printStackTrace();
            }
        }
        return fileContent;
    }

    /**
     * Starts fcs local cMsg server
     */
    private void startFcsLocalServer(String name,
                                     int tcpPort,
                                     int tcpDPort,
                                     int udpPort) {
        cMsgNameServer fcsServer = new cMsgNameServer(tcpPort, tcpDPort, udpPort,
                false,
                false,
                myConfig.getPlatformExpid(),
                null,
                null,
                cMsgConstants.debugNone,
                10);
        fcsServer.setName(name);
        fcsServer.startServer();

    }


}
