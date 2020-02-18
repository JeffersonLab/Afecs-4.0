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

package org.jlab.coda.afecs.ui.rcgui;

import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.cMsg.cMsgNetworkConstants;

public class CommandLine {
    private String  session              = AConstants.udf;
    private String  expid                = AConstants.udf;
    private String  platformHost         = AConstants.udf;
    private int     platformTcpPort      = cMsgNetworkConstants.nameServerTcpPort;
    private int     platformUdpPort      = cMsgNetworkConstants.nameServerUdpPort;
    private boolean debug                = false;
    private String  platformUdl          = AConstants.udf;
    private String  platformMulticastUdl = AConstants.udf;
    private String  configFileName       = AConstants.udf;
    private boolean noFx                 = false;
    private boolean autoConnect          = false;
    private boolean monitorOnly          = false;


    public String getName() {
        String name = AConstants.udf;
        return name;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getExpid() {
        return expid;
    }

    public void setExpid(String expid) {
        this.expid = expid;
    }

    public String getPlatformHost() {
        return platformHost;
    }

    public void setPlatformHost(String platformHost) {
        this.platformHost = platformHost;
    }

    public int getPlatformTcpPort() {
        return platformTcpPort;
    }

    public void setPlatformTcpPort(int platformTcpPort) {
        this.platformTcpPort = platformTcpPort;
    }

    public int getPlatformUdpPort() {
        return platformUdpPort;
    }

    public void setPlatformUdpPort(int platformUdpPort) {
        this.platformUdpPort = platformUdpPort;
    }

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }


    public String getPlatformUdl() {
        return platformUdl;
    }

    public void setPlatformUdl(String platformUdl) {
        this.platformUdl = platformUdl;
    }

    public String getPlatformMulticastUdl() {
        return platformMulticastUdl;
    }

    public void setPlatformMulticastUdl(String platformMulticastUdl) {
        this.platformMulticastUdl = platformMulticastUdl;
    }

    public void setMulticast(boolean multicast) {
        boolean isMulticast = multicast;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public boolean isNoFx() {
        return noFx;
    }

    public void setNoFx(boolean noFx) {
        this.noFx = noFx;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public boolean isMonitorOnly() {
        return monitorOnly;
    }

    public void setMonitorOnly(boolean monitorOnly) {
        this.monitorOnly = monitorOnly;
    }
}
