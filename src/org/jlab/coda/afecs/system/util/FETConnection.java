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

package org.jlab.coda.afecs.system.util;

//import org.jlab.coda.et.*;
//import org.jlab.coda.et.exception.*;

/**
 * Describe...
 *
 * @author gurjyan
 *         Date: 11/12/13 Time: 2:13 PM
 * @version 2
 */
public class FETConnection {
//    private final static String controlStationName         = "ControlEventStation";
//    private final static int    controlStationPosition     = 1;
//    private final static int    controlEventSelectCriteria = 0xC0DA;
//
//    private String stationName      = "undefined";
//    private int    stationPosition;
//    private EtAttachment attachment = null;
//    // local instance of the logger object
//    private ALogger lg = ALogger.getInstance();
//
//
//    public String getStationName() {
//        return stationName;
//    }
//
//    public int getStationPosition() {
//        return stationPosition;
//    }
//
//    public EtAttachment getAttachment() {
//        return attachment;
//    }
//
//    protected EtStationConfig configStation(String stName, int stPosition, int stSelectCriteria){
//
//        EtStationConfig  stationConfig = null;
//        stationName     = stName;
//        stationPosition = stPosition;
//
//        try {
//            // configuration of a new station
//            stationConfig = new EtStationConfig();
//            try {
//                stationConfig.setUserMode(EtConstants.stationUserSingle);
//            }
//            catch (EtException e) { /* never happen */
//                lg.logger.severe(AfecsTool.stack2str(e));
//            }
//
//            if(stSelectCriteria>0x0){
//                // Create filter for station
//                // Station filter is the built-in selection function.
//                int[] selects = new int[EtConstants.stationSelectInts];
//                Arrays.fill(selects, -1);
//                selects[0] = stSelectCriteria;
//                stationConfig.setSelect(selects);
//                stationConfig.setSelectMode(EtConstants.stationSelectMatch);
//            }
//        }
//        catch (Exception e) {/* never happen */
//            lg.logger.severe(AfecsTool.stack2str(e));
//        }
//        return stationConfig;
//    }
//
//
//    protected boolean connect(EtSystem etSystem,EtStationConfig stationConfig) {
//        boolean res = true;
//        try {
//            etSystem.open();
//        } catch (Exception e) {
//            res = false;
//            EtSystemOpenConfig config = etSystem.getConfig();
//            String errString = "cannot connect to ET " + config.getEtName() + ", ";
//            int method = config.getNetworkContactMethod();
//            if (method == EtConstants.direct) {
//                errString += " direct to " + config.getHost() + " on port " + config.getTcpPort();
//            }
//            else if (method == EtConstants.multicast) {
//                errString += " multicasting to port " + config.getUdpPort();
//            }
//            else if (method == EtConstants.broadcast) {
//                errString += " broadcasting to port " + config.getUdpPort();
//            }
//            else if (method == EtConstants.broadAndMulticast) {
//                errString += " multi/broadcasting to port " + config.getUdpPort();
//            }
//            lg.logger.severe(errString+" \n"+AfecsTool.stack2str(e));
//        }
//
//        try {
//            EtStation station = etSystem.createStation(stationConfig, stationName);
//            etSystem.setStationPosition(station, stationPosition, 0);
//
//            // attach to station
//            attachment = etSystem.attach(station);
//
//        } catch (EtExistsException e) {
//            res = false;
//            lg.logger.severe(AfecsTool.stack2str(e));
//        } catch (IOException e) {
//            res = false;
//            lg.logger.severe(AfecsTool.stack2str(e));
//        } catch (EtClosedException e) {
//            res = false;
//            lg.logger.severe(AfecsTool.stack2str(e));
//        } catch (EtException e) {
//            res = false;
//            lg.logger.severe(AfecsTool.stack2str(e));
//        } catch (EtDeadException e) {
//            res = false;
//            lg.logger.severe(AfecsTool.stack2str(e));
//        } catch (EtTooManyException e) {
//            res = false;
//            lg.logger.severe(AfecsTool.stack2str(e));
//        }
//        return res;
//    }
//
//
//    public EtSystem configConnection(String etName, String host,
//                                     String baddr,String maddr,
//                                     String conMethod,
//                                     int port, int uport,
//                                     int wait){
//
//        EtSystem etSystem = null;
//
//        // direct connection port
//        if(port<=0){
//            port = EtConstants.serverPort;
//        }
//
//        // broadcasting/multicasting (udp) port
//        if(uport<=0){
//            uport = EtConstants.broadcastPort;
//        }
//
//        // multicast address
//
//        String[] mAddrs;
//        Collection<String> mAddrList = null;
//
//        if (maddr != null) {
//            mAddrs = new String[] {maddr};
//            mAddrList = Arrays.asList(mAddrs);
//        }
//
//        // Are we willing to wait for an ET system to appear?
//        if(wait<=0){
//            wait = 0;
//        } else {
//            // wait is in milliseconds, str is in seconds
//            wait = 1000*wait;
//        }
//
//        // How do we contact the ET system?
//        int method = EtConstants.direct;
//
//        // broadcast address to use to connect to ET (not to listen on)
//        String[] bAddrs;
//        Collection<String> bAddrList = null;
//        // broadcast address to use to connect to ET
//        if (baddr != null) {
//            bAddrs = new String[] {baddr};
//            bAddrList = Arrays.asList(bAddrs);
//        }
//
//
//        // How do we connect to it? By default, assume
//        // it's anywhere and we need to broadcast.
//
//        if (conMethod == null) {
//            method = EtConstants.broadcast;
//        }
//        else if (conMethod.equalsIgnoreCase("cast")) {
//            method = EtConstants.broadAndMulticast;
//        }
//        else if (conMethod.equalsIgnoreCase("bcast")) {
//            method = EtConstants.broadcast;
//        }
//        else if (conMethod.equalsIgnoreCase("mcast")) {
//            method = EtConstants.multicast;
//        }
//        else if (conMethod.equalsIgnoreCase("direct")) {
//            method = EtConstants.direct;
//        }
//
//        // Where is the host the ET system is running on?
//        // Where do we look for it? By default assume
//        // it can be anywhere (local or remote).
//        if (host == null) {
//            host = EtConstants.hostAnywhere;
//        }
//        else if (host.equalsIgnoreCase("local")) {
//            host = EtConstants.hostLocal;
//        }
//        else if (host.equalsIgnoreCase("anywhere")) {
//            host = EtConstants.hostAnywhere;
//        }
//        else if (host.equalsIgnoreCase("remote")) {
//            host = EtConstants.hostRemote;
//        }
//
//        // Configure ET system connection
//        try {
//            EtSystemOpenConfig  openConfig =
//                    new EtSystemOpenConfig(etName, host,
//                            bAddrList, mAddrList,false, method,
//                            port, uport, uport,
//                            EtConstants.multicastTTL,
//                            EtConstants.policyError);
//            openConfig.setWaitTime(wait);
//
//            etSystem = new EtSystem(openConfig);
//        }
//        catch (EtException e) {
//            lg.logger.severe(AfecsTool.stack2str(e));
//        }
//        return etSystem;
//    }
}
