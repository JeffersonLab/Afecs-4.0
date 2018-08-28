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

package org.jlab.coda.afecs.system;

import org.jlab.coda.cMsg.cMsgNetworkConstants;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <p>
 *    This singleton class is a source for Afecs platform system parameters,
 *    including expid, platform cMsg and RC domain server parameters.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/12/13 Time: 1:51 PM
 * @version 4.x
 */
public class AConfig {
    private static AConfig     ourInstance      = new AConfig();
    private String             coolHome;
    private String             platformExpid;

    // platform cMsg domain server
    private String             platformName;
    private String             containerName;
    private String             platformDescription;
    private String             platformHost;
    private String             localHost;
    private String             containerHost;
    private int                platformTcpPort;
    private int                platformTcpDomainPort;
    private int                platformUdpPort;
    private String             platformUdl;
    private String             platformMulticastUdl;

    // platform rc domain server
    private String             platformRcDomainServerName;
    private String             platformRcDomainServerDescription;
    private int                platformRcDomainUdpPort;
    private String             platformRcMulticastServerUdl;
    private String             platformRcDomainUdl;

    // control session
    private String             session;

    // sql database
    private String             dbUrl     = AConstants.udf;
    private String             dbDriver  = AConstants.udf;


    /**
     * Private constructor.
     */
    private AConfig() {
        // Setting default values
        coolHome = System.getenv("COOL_HOME");
        if(coolHome == null) {
            System.out.println("Severe Error: Afecs database " +
                    "directory ($COOL_HOME) is not defined.");
            System.exit(1);
        }


        platformExpid = System.getenv("EXPID");
        if(platformExpid == null) {
            System.out.println("Severe Error: Platform EXPID is not defined.");
            System.exit(1);
        }

        dbUrl = System.getenv("MSQL_TCP_HOST");

        if(dbUrl == null){
            System.out.println("CODA2 SQL db host is not defined.");
            dbUrl = AConstants.udf;
        } else {
            dbUrl    = "jdbc:msql://"+dbUrl+":8101/"+platformExpid;
            dbDriver = "com.imaginary.sql.msql.MsqlDriver";
        }

        platformName                      = platformExpid;
        platformDescription               = "Afecs-4 Platform for experiment "+platformExpid;
        platformRcDomainServerName        = "undefined";
        platformRcDomainServerDescription = "Afecs-4 Platform rc domain for experiment "+platformExpid;
        session = System.getenv("SESSION");
        if(session == null) session = AConstants.udf;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            localHost = addr.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        platformHost                     = localHost;
        containerHost                    = localHost;
        containerName                    = containerHost+"_admin";
        platformTcpPort                  = cMsgNetworkConstants.nameServerTcpPort;
        platformTcpDomainPort            = cMsgNetworkConstants.domainServerPort;
        platformUdpPort                  = cMsgNetworkConstants.nameServerUdpPort;
        platformRcDomainUdpPort          = cMsgNetworkConstants.rcMulticastPort;
        platformUdl                      = "cMsg://"+platformHost+":"+platformTcpPort+
                "/cMsg/"+platformExpid+
                "?cmsgpassword="+platformExpid;
        platformMulticastUdl             = "cMsg://multicast:"+platformUdpPort+"/cMsg/"+platformExpid+"?cmsgpassword="+platformExpid;
        platformRcMulticastServerUdl     = "rcm://"+platformRcDomainUdpPort+"/"+platformExpid+"?multicastTO=5";
        platformRcDomainUdl              = "cMsg:rc://multicast/"+platformExpid+"&multicastTO=5&connectTO=5";

    }

    /**
     * Friendly printout all parameters, defined for the platform
     * @return String representing parameters
     */
    public String toString(){
        String dbPasswd = " ";
        String dbUser = " ";
        return "Afecs db dir                   = " + coolHome + "\n" +
                "Platform host                  = " + platformHost + "\n" +
                "Platform name                  = " + platformName + "\n" +
                "Platform description           = " + platformDescription + "\n" +
                "Platform expid                 = " + platformExpid + "\n" +
                "Platform tcpPort               = " + platformTcpPort + "\n" +
                "Platform udpPort               = " + platformUdpPort + "\n" +
                "Platform udl                   = " + platformUdl + "\n" +
                "Platform muticast udl          = " + platformMulticastUdl + "\n" +
                "Platform Rc name               = " + platformRcDomainServerName + "\n" +
                "Platform Rc description        = " + platformRcDomainServerDescription + "\n" +
                "Platform Rc udpPort            = " + platformRcDomainUdpPort + "\n" +
                "Platform RcMulticastServer udl = " + platformRcMulticastServerUdl + "\n" +
                "Cool Core                      = " + AConstants.COOL_CORE + "\n\n" +
                "Session                        = " + session + "\n" +
                "Sql db rl                      = " + dbUrl + "\n" +
                "Sql db driver                  = " + dbDriver + "\n" +
                "Sql db user                    = " + dbUser + "\n" +
                "Sql db password                = " + dbPasswd + "\n";
    }

    /**
     * This method returns a reference to the single instane of this singleton class
     * @return instance of this class
     */
    public static AConfig getInstance() {
        return ourInstance;
    }

    public String getCoolHome() {
        return coolHome;
    }


    public String getPlatformUdl() {
        return platformUdl;
    }

    public String getPlatformMulticastUdl() {
        return platformMulticastUdl;
    }

    public String getPlatformDescription() {
        return platformDescription;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getPlatformExpid() {
        return platformExpid;
    }

    public String getPlatformHost() {
        return platformHost;
    }

    public String getContainerHost() {
        return containerHost;
    }

    public int getPlatformTcpPort() {
        return platformTcpPort;
    }

    public int getPlatformUdpPort() {
        return platformUdpPort;
    }

    public String getPlatformRcDomainServerName() {
        return platformRcDomainServerName;
    }

    public void setPlatformRcDomainServerName(String name) {
        platformRcDomainServerName = "rc_"+name;
    }

    public String getPlatformRcDomainServerDescription() {
        return platformRcDomainServerDescription;
    }

    public int getPlatformRcDomainUdpPort() {
        return platformRcDomainUdpPort;
    }

    public String getPlatformRcMulticastServerUdl() {
        return platformRcMulticastServerUdl;
    }

    public String getPlatformRcDomainUdl() {
        return platformRcDomainUdl;
    }


    public String getSession() {
        return session;
    }

    public String getLocalHost() {
        return localHost;
    }

    public int getPlatformTcpDomainPort() {
        return platformTcpDomainPort;
    }
}
