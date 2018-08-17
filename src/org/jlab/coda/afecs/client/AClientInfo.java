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

package org.jlab.coda.afecs.client;

import org.jlab.coda.afecs.system.AConstants;

import java.io.Serializable;
import java.util.Arrays;

public class AClientInfo implements Serializable {
    private String name          = AConstants.udf;
    private String hostName      = AConstants.udf;
    private String[] hostIps     = null;
    private String[] hostBroadcastAddresses     = null;
    private String containerHost = AConstants.udf;
    private int portNumber;
    private int requestId;

    /**
     * Constructor
     * @param s The name of this agent
     */
    public AClientInfo(String s){
        if(s!=null && s.trim().equals("")){
            name =s;
        } else {
            name = AConstants.udf;
        }
        hostName = AConstants.udf;
    }

    public AClientInfo(){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getContainerHost() {
        return containerHost;
    }

    public void setContainerHost(String containerHost) {
        this.containerHost = containerHost;
    }

    public String[] getHostIps() {
        return hostIps;
    }

    public void setHostIps(String[] hostIps) {
        this.hostIps = hostIps;
    }

    public String[] getHostBroadcastAddresses() {
        return hostBroadcastAddresses;
    }

    public void setHostBroadcastAddresses(String[] hostBroadcastAddresses) {
        this.hostBroadcastAddresses = hostBroadcastAddresses;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "AClientInfo{" +
                "name='" + name + '\'' +
                ", hostName='" + hostName + '\'' +
                ", hostIps=" + Arrays.toString(hostIps) +
                ", hostBroadcastAddresses=" + Arrays.toString(hostBroadcastAddresses) +
                ", containerHost='" + containerHost + '\'' +
                ", portNumber=" + portNumber +
                ", requestId=" + requestId +
                '}';
    }
}
