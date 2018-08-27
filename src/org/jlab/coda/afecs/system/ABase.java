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

import org.jlab.coda.afecs.cool.ontology.AComponent;

import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsg;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * Afecs base class presenting methods
 * for cMsg communications. It owns
 * cMsg connection object references, such as:
 * <ul>
 * <li>platform cMsg server connection</li>
 * <li>client RC server connection</li>
 * </ul>
 * As well as:
 * <ul>
 * <li>UDLs defined AConfig</li>
 * <li>Map of RTVs</li>
 * </ul>
 * <p>
 * </p>
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */


public class ABase implements Serializable {

    // Connection to the platform cMsg server
    transient public volatile cMsg myPlatformConnection;

    // Connection to the client RC server
    transient public volatile cMsg myCRCClientConnection;


    public String myName = AConstants.udf;
    public String mySession = AConstants.udf;
    public String myRunType = AConstants.udf;

    transient private String plUDL = AConstants.udf;
    transient private String rcUDL = AConstants.udf;
    transient private String plMulticastUDL = AConstants.udf;
    transient private String plDescription = AConstants.udf;
    transient private String plEXPID = AConstants.udf;

    transient public AConfig myConfig;

    transient private static Map<String, String> _RTVs;

    /**
     * Constructor gets the singleton object of constants
     */
    public ABase() {
        myConfig = AConfig.getInstance();
        plUDL = myConfig.getPlatformUdl();
        rcUDL = myConfig.getPlatformRcDomainUdl();
        plMulticastUDL = myConfig.getPlatformMulticastUdl();
        plDescription = myConfig.getPlatformDescription();
        plEXPID = myConfig.getPlatformExpid();
    }

    /**
     * Used by GUI's only
     *
     * @param plUDL          udl of the platform
     * @param plMulticastUDL multi cast udl
     * @param plDescription  platform description
     * @param plEXPID        platform name/expid
     */
    public ABase(String plUDL, String plMulticastUDL, String plDescription, String plEXPID) {
        myConfig = AConfig.getInstance();
        this.plUDL = plUDL;
        this.plMulticastUDL = plMulticastUDL;
        this.plDescription = plDescription;
        this.plEXPID = plEXPID;
        rcUDL = "cMsg:rc://multicast/" + plEXPID + "&multicastTO=5&connectTO=5";
    }

    /**
     * Used by RcSpy
     *
     * @param plUDL   udl of the platform
     * @param name    of the RcSpy
     * @param plEXPID platform name/expid
     */
    public ABase(String name, String plUDL, String plEXPID) {
        myName = name;
        this.plUDL = plUDL;
        this.plEXPID = plEXPID;
    }


    /**
     * used by GUIs, after finding the platform host,
     * to connect to the platform cMsg server
     *
     * @param host platform host
     * @return UDL
     */
    public String updateHostUdl(String host, int port) {
        plUDL = "cMsg://" + host + ":" + port + "/cMsg/" + plEXPID + "?cmsgpassword=" + getPlEXPID() + "&connectTO=2";
        return plUDL;
    }

    public String getPlUDL() {
        return plUDL;
    }

    public String getPlMulticastUDL() {
        return plMulticastUDL;
    }

    public String getPlEXPID() {
        return plEXPID;
    }

    /**
     * Monitors platform rc domain multicast server.
     * Used to find out the host of the platform.
     * What we do is to multicast the platform multicast
     * server and the server will respond back the host
     * name where it is running, thus the host of the platform.
     *
     * @return respond cMsgMessage contains the responders,
     * i.e. platform front-end containers host.
     */
    public cMsgMessage rcMonitor(int sleep) {

        cMsgMessage m = null;

        if (!myName.equals(AConstants.udf)) {

            try {
                // define a cMsg connection object with the rcUdl
                cMsg myRcDomainConnection = new cMsg(rcUDL, "x", "y");
                // send a monitor packet
                m = myRcDomainConnection.monitor(Integer.toString(sleep));

            } catch (cMsgException e) {

                e.printStackTrace();
                e.printStackTrace();
                System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                        myName +
                        ": testing/monitoring RC domain server using \n udl = " +
                        rcUDL);

                e.printStackTrace();
            }
        }
        return m;
    }

    /**
     * Connects to the platform cMsg domain server
     *
     * @param udl         udl of the connection
     * @param name        name of the requester to connect
     * @param description of the connection
     * @return cMsg connection object
     */
    public cMsg connect(String udl, String name, String description) {
        cMsg connectionObj;
        if (name == null || name.equals(AConstants.udf)) {
            return null;
        }
        try {
            connectionObj = new cMsg(udl, name, description);
            connectionObj.connect();
            connectionObj.start();
        } catch (cMsgException e) {

            System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                    myName + ": connecting to the udl = " + udl);
            e.printStackTrace();
            return null;
        }

        return connectionObj;
    }

    /**
     * Connects to the platform cMsg domain server
     *
     * @return cMsg connection object
     */
    public cMsg platformConnect() {
        if (isPlatformConnected()) {
            return myPlatformConnection;
        } else {
            return connect(plUDL, myName, plDescription);
        }
    }

    /**
     * Connects to the platform cMsg domain server
     *
     * @param udl address of the connection
     * @return cMsg connection object
     */
    public cMsg platformConnect(String udl) throws cMsgException {
        if (isPlatformConnected()) {
            return myPlatformConnection;
        } else {
            return connect(udl, myName, plDescription);
        }
    }

    /**
     * Disconnects from the platform
     *
     * @return status of the disconnect operation
     */
    protected boolean platformDisconnect() {
        boolean stat = true;
        try {
            if (isPlatformConnected()) {
                myPlatformConnection.disconnect();
            }
        } catch (cMsgException e) {
            e.printStackTrace();

            stat = false;
        }
        return stat;
    }



    /**
     * See if the agent is connected to the platform
     * through cMsg native connection
     *
     * @return true if connected to the platform
     */
    public boolean isPlatformConnected() {
        return (myPlatformConnection != null &&
                myPlatformConnection.isConnected());
    }

    /**
     * Setts up a connection to the client.
     * Last leg of the triangle.
     *
     * @param udl client connection udl (rcs://host:port)
     * @return true if connection succeeds
     */
    public boolean rcClientConnect(String udl) {
        try {
            myCRCClientConnection = connect(udl,
                    myConfig.getPlatformRcDomainServerName(),
                    myConfig.getPlatformRcDomainServerDescription());

        } catch (Exception e) {

            System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                    myName + ": when connecting to the client " + udl);
            e.printStackTrace();

        }
        return isRcClientConnected();
    }

    /**
     * See if agent is connected to the
     * client through rcDomain connection
     *
     * @return true if there is a valid
     * rcDomain client connection
     */
    public boolean isRcClientConnected() {
        return (myCRCClientConnection != null &&
                myCRCClientConnection.isConnected());
    }

    /**
     * Disconnects from the rcDomain
     *
     * @return status of the disconnect operation
     */
    public boolean rcClientDisconnect() {
        boolean stat = true;
        try {
            if (isRcClientConnected()) {
                myCRCClientConnection.disconnect();
            }
        } catch (cMsgException e) {

            e.printStackTrace();
            stat = false;
        }
        return stat;
    }

    /**
     * Check to see if cMsg server is up and running
     * we send syncSend and if it returns -1 that
     * indicates that server is down.
     *
     * @return integer
     */
    public int pingServer() {
        int o = -1;
        cMsgMessage msg = new cMsgMessage();
        msg.setSubject("ping");
        msg.setType("ping");

        try {
            o = myPlatformConnection.syncSend(msg, 3000);
        } catch (cMsgException e) {

            e.printStackTrace();
        }
        return o;
    }


    /**
     * Forwards message from some other domain,
     * in our case from rc domain to cMsg domain.
     *
     * @param msg message to be forwarded
     * @return true if succeeded
     */
    public boolean msgForward(cMsgMessage msg) {
        boolean status = true;

        if (isPlatformConnected()) {
            try {
                myPlatformConnection.send(msg);
            } catch (cMsgException e) {

                e.printStackTrace();
                status = false;
            }
        } else {
            status = false;
        }
        return status;
    }

    /**
     * Sends cMsg Message
     *
     * @param subject of the message
     * @param type    of the message
     * @param text    of the message
     * @return status of the send operation
     */
    public boolean send(String subject,
                        String type,
                        String text) {
        return send(subject, type, text, null, null);
    }

    /**
     * Sends cMsg Message
     *
     * @param subject of the message
     * @param type    of the message
     * @param obj     that is going to be serialized and sent as a byte array
     * @return status of the send operation
     */
    public boolean send(String subject,
                        String type,
                        Object obj) {
        return send(subject, type, null, obj, null);
    }

    /**
     * Sends cMsg Message
     *
     * @param subject of the message
     * @param type    of the message
     * @param text    of the message
     * @param obj     that is going to be serialized and sent as a byte array
     * @return status of the send operation
     */
    public boolean send(String subject,
                        String type,
                        String text,
                        Object obj) {
        return send(subject, type, text, obj, null);
    }

    /**
     * Sends cMsg Message
     *
     * @param subject of the message
     * @param type    of the message
     * @param pis     of payload items of the message of the message
     * @return status of the send operation
     */
    public boolean send(String subject,
                        String type,
                        ArrayList<cMsgPayloadItem> pis) {
        return send(subject, type, null, null, pis);
    }

    /**
     * Sends cMsg Message
     *
     * @param subject of the message
     * @param type    of the message
     * @param text    of the message
     * @param pis     of payload items of the message of the message
     * @return status of the send operation
     */
    public boolean send(String subject,
                        String type,
                        String text,
                        ArrayList<cMsgPayloadItem> pis) {
        return send(subject, type, text, null, pis);
    }

    /**
     * Sends cMsg Message
     *
     * @param subject of the message
     * @param type    of the message
     * @param obj     that is going to be serialized and sent as a byte array
     * @param pis     of payload items of the message of the message
     * @return status of the send operation
     */
    public boolean send(String subject,
                        String type,
                        Object obj,
                        ArrayList<cMsgPayloadItem> pis) {
        return send(subject, type, null, obj, pis);
    }

    /**
     * Sends cMsg Message
     *
     * @param subject of the message
     * @param type    of the message
     * @param text    of the message
     * @param obj     that is going to be serialized and sent as a byte array
     * @param pis     of payload items of the message of the message
     * @return status of the send operation
     */
    public boolean send(String subject,
                        String type,
                        String text,
                        Object obj,
                        ArrayList<cMsgPayloadItem> pis) {
        boolean status = true;
        if (myPlatformConnection != null &&
                myPlatformConnection.isConnected() &&
                subject != null &&
                type != null) {
            cMsgMessage msg = new cMsgMessage();
            msg.setSubject(subject);
            msg.setType(type);

            if (text != null) msg.setText(text);

            if (pis != null && pis.size() > 0) {
                for (cMsgPayloadItem item : pis) {
                    msg.addPayloadItem(item);
                }
            }

            if (obj != null) {
                try {
                    msg.setByteArray(AfecsTool.O2B(obj));
                } catch (IOException e) {

                    System.out.println("DDD ----------- DDD");
                    e.printStackTrace();
                    System.out.println("DDD ----------- DDD");


                    e.printStackTrace();
                    status = false;
                }
            }

            try {
                myPlatformConnection.send(msg);
            } catch (cMsgException e) {

                System.out.println("DDD ----------- DDD");
                e.printStackTrace();
                System.out.println("DDD ----------- DDD");


                e.printStackTrace();
                status = false;
            }
        } else {
            status = false;
        }

        if (status &&
                AConstants.debug.get() &&
                !subject.equals(getPlEXPID()) &&
                !subject.equals(myConfig.getSession())) {
            a_println(AfecsTool.getCurrentTime("HH:mm:ss") +
                    " " + myName +
                    ": Info -  cmsg_send subject = " + subject +
                    " type = " + type +
                    " text = " + text);
        }
        return status;
    }

    /**
     * <p>
     * Creates a response message from the given
     * message and sends it through platform connection
     * </p>
     *
     * @param msg given message
     * @param txt text for the response message
     */
    public void sendResponse(cMsgMessage msg, String txt)
            throws cMsgException {
        if (msg.isGetRequest()) {
            cMsgMessage mr;
            mr = msg.response();
            mr.setSubject(AConstants.udf);
            mr.setType(AConstants.udf);
            mr.setText(txt);
            myPlatformConnection.send(mr);
        }
    }

    /**
     * cMsg sendAndGet sends a simple subject with a text
     *
     * @param subject subject
     * @param type    type
     * @param text    text
     * @param timeout timeout in milli sec.
     * @return {@link cMsgMessage} object
     */
    public cMsgMessage p2pSend(String subject,
                               String type,
                               String text,
                               int timeout) throws AException {
        return p2pSend(subject, type, text, null, null, timeout);
    }

    /**
     * cMsg sendAndGet with a text and payload
     *
     * @param subject subject
     * @param type    type
     * @param text    text
     * @param pis     of payload items of the message of the message
     * @param timeout timeout in milli sec.
     * @return {@link cMsgMessage} object
     */
    public cMsgMessage p2pSend(String subject,
                               String type,
                               String text,
                               ArrayList<cMsgPayloadItem> pis,
                               int timeout) throws AException {
        return p2pSend(subject, type, text, null, pis, timeout);
    }

    /**
     * cMsg sendAndGet with an object
     *
     * @param subject subject
     * @param type    type
     * @param obj     object sent through binary payload (byteArray)
     * @param timeout timeout in milli sec.
     * @return cMsgMessage object
     */
    public cMsgMessage p2pSend(String subject,
                               String type,
                               Object obj,
                               int timeout) throws AException {
        return p2pSend(subject, type, null, obj, null, timeout);
    }

    /**
     * cMsg sendAndGet with an payload
     *
     * @param subject subject
     * @param type    type
     * @param timeout timeout in milli sec.
     * @return {@link cMsgMessage} object
     */
    public cMsgMessage p2pSend(String subject,
                               String type,
                               List<cMsgPayloadItem> pis,
                               int timeout) throws AException {
        return p2pSend(subject, type, null, null, pis, timeout);
    }

    /**
     * cMsg sendAndGet with an everything
     *
     * @param subject subject
     * @param type    type
     * @param txt     text
     * @param obj     object sent through binary payload (byteArray)
     * @param timeout timeout in milli sec.
     * @return {@link cMsgMessage} object
     */
    public cMsgMessage p2pSend(String subject,
                               String type,
                               String txt,
                               Object obj,
                               List<cMsgPayloadItem> pis,
                               int timeout) throws AException {

        cMsgMessage msgBack = null;
        if (myPlatformConnection != null &&
                myPlatformConnection.isConnected() &&
                subject != null &&
                type != null) {
            cMsgMessage msg = new cMsgMessage();
            msg.setSubject(subject);
            msg.setType(type);

            if (txt != null) msg.setText(txt);

            if (pis != null && pis.size() > 0) {
                for (cMsgPayloadItem item : pis) {
                    msg.addPayloadItem(item);
                }
            }

            if (obj != null) {
                try {
                    msg.setByteArray(AfecsTool.O2B(obj));
                } catch (IOException e) {
                    throw new AException(e.getMessage());
                }
            }

            try {
                msgBack = myPlatformConnection.sendAndGet(msg, timeout);
            } catch (TimeoutException | cMsgException e) {
                throw new AException(e.getMessage());
            }

            if (msgBack == null) {
                throw new AException(myName + "-Error: Receiver does not exists.");
            }
        }
        return msgBack;
    }

    /**
     * Sends rcDomain cMsg Message (usually to the physical client)
     *
     * @param subject of the message
     * @param type    of the message
     * @param text    of the message
     * @return status of the send operation
     */
    public boolean rcSend(String subject,
                          String type,
                          String text) throws cMsgException {
        return rcSend(subject, type, text, null);
    }

    /**
     * Sends rcDomain cMsg Message
     *
     * @param subject of the message
     * @param type    of the message
     * @param text    of the message
     * @param pis     ArrayList of payload items of the message
     * @return status of the send operation
     */
    public boolean rcSend(String subject,
                          String type,
                          String text,
                          ArrayList<cMsgPayloadItem> pis)
            throws cMsgException {
        boolean status = true;
        cMsgMessage msg;
        if (myCRCClientConnection != null &&
                myCRCClientConnection.isConnected() &&
                subject != null &&
                type != null) {
            msg = new cMsgMessage();
            msg.setSubject(subject);
            msg.setType(type);

            if (text != null) msg.setText(text);

            if (pis != null && pis.size() > 0) {
                for (cMsgPayloadItem item : pis) {
                    msg.addPayloadItem(item);
                }
            }

            myCRCClientConnection.send(msg);

        } else {
            status = false;
        }
        if (status &&
                AConstants.debug.get() &&
                !subject.equals(getPlEXPID()) &&
                !subject.equals(myConfig.getSession())) {
            a_println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                    myName + ": Info - rc_send subject = " + subject +
                    " type = " + type);
        }
        return status;
    }

    public void rcSend(cMsgMessage msg) throws cMsgException {
        if (myCRCClientConnection != null &&
                myCRCClientConnection.isConnected()) {
            myCRCClientConnection.send(msg);
        }
    }

    /**
     * Implementation of cMsg sendAndGet for RC domain connections.
     * Method waits timeout seconds in 1sec increments until message
     * arrives in callback or timeout is reached. Then it will force
     * cMsg un-subscribe.
     *
     * @param subject subject to send the message
     * @param type    type to send the message
     * @param text    text of the send message
     * @param timeout timeout in milli sec.
     * @return cMsgMessage object
     */
    public cMsgMessage rcp2pSend(String subject,
                                 String type,
                                 String text,
                                 int timeout)
            throws AException {
        return rcp2pSend(subject, type, text, null, timeout);
    }

    /**
     * Implementation of cMsg sendAndGet for RC domain connections.
     * Method waits timeout seconds in 1sec increments until message
     * arrives in a callback or timeout is reached. Then it will force
     * cMsg un-subscribe.
     *
     * @param subject subject to send the message
     * @param type    type to send the message
     * @param text    text of the send message
     * @param pis     ArrayList of payload items of the message
     * @param timeout timeout in milli sec.
     * @return cMsgMessage object
     */
    public cMsgMessage rcp2pSend(String subject,
                                 String type,
                                 String text,
                                 List<cMsgPayloadItem> pis,
                                 int timeout)
            throws AException {
        cMsgMessage msgBack = null;
        cMsgMessage msg;
        if (myCRCClientConnection != null &&
                myCRCClientConnection.isConnected() &&
                subject != null &&
                type != null) {
            msg = new cMsgMessage();
            msg.setSubject(subject);
            msg.setType(type);

            if (text != null) msg.setText(text);

            if (pis != null && !pis.isEmpty()) {
                for (cMsgPayloadItem item : pis) {
                    msg.addPayloadItem(item);
                }
            }

            try {
                msgBack = myCRCClientConnection.sendAndGet(msg, timeout);
//                a_println(msgBack.toString());
            } catch (Exception e) {
                if (e.getMessage() == null) {
                    a_println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                            myName + ": Error - rc_sendAndGet subject = " + subject +
                            " type = " + type + " => timed out.");
                } else {
                    a_println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                            myName + ": Error - rc_sendAndGet subject = " + subject +
                            " type = " + type + " => exception message = " + e.getMessage());
                }
//                e.printStackTrace();

                throw new AException(e.getMessage(), e);
            }
            if (msgBack == null) {
                a_println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                        myName + ": Error - rc_sendAndGet subject = " + subject +
                        " type = " + type + " => null response from the client");
                throw new AException(myName + "-Error: null response from the client");
            }
//            else {
//                System.out.println(AfecsTool.getCurrentTime("HH:mm:ss")+" "+
//                        myName+": Info - rc_sendAndGet subject = "+subject+
//                        " type = "+type+" => "+msgBack);
//                a_println(AfecsTool.getCurrentTime("HH:mm:ss")+" "+
//                        myName+": Info - rc_sendAndGet subject = "+subject+
//                        " type = "+type+" => "+msgBack);
//            }

        }
        return msgBack;
    }


    /**
     * Sends daLogMsg subject = component name and type = rc/report/dalog
     *
     * @param c          {@link AComponent} object reference
     * @param severityId severity id
     * @param severity   severity string ( ERROR, WARNING, etc.)
     * @param text       of the dalog message
     * @return status of the operation
     */
    public boolean dalogMsg(AComponent c, int severityId, String severity, String text) {
        boolean status = false;
        if (myPlatformConnection != null && myPlatformConnection.isConnected()) {
            cMsgMessage msg = new cMsgMessage();
            msg.setSubject(c.getName());
            msg.setType(AConstants.RcReportDalog);
            msg.setUserInt(severityId);
            msg.setText(text);
            try {
                msg.addPayloadItem(new cMsgPayloadItem(AConstants.EXPID, myConfig.getPlatformExpid()));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.CODANAME, myName));

                // codaid comes from the client
                msg.addPayloadItem(new cMsgPayloadItem("codaid", "na"));

                if (c.getClient() != null && c.getClient().getHostName() != null) {
                    msg.addPayloadItem(new cMsgPayloadItem(AConstants.CLIENTHOST, c.getClient().getHostName()));
                } else {
                    msg.addPayloadItem(new cMsgPayloadItem(AConstants.CLIENTHOST, AConstants.udf));
                }
                msg.addPayloadItem(new cMsgPayloadItem(AConstants.USERNAME, "afecs"));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.SESSION, mySession));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.RUNTYPE, myRunType));

                // config ID is set (payload = "runType" is set by the client
                msg.addPayloadItem(new cMsgPayloadItem("runType", "na"));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.RUNNUMBER, c.getRunNumber()));

                if (msg.getText() != null) {
                    msg.addPayloadItem(new cMsgPayloadItem(AConstants.DALOGTEXT, msg.getText()));
                } else {
                    msg.addPayloadItem(new cMsgPayloadItem(AConstants.DALOGTEXT, AConstants.udf));
                }

                msg.addPayloadItem(new cMsgPayloadItem("severity", severity));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.DALOGSEVERITYID, msg.getUserInt()));

                msg.addPayloadItem(new cMsgPayloadItem("state", c.getState()));

                msg.addPayloadItem(new cMsgPayloadItem("codaClass", "agent"));

                msg.addPayloadItem(new cMsgPayloadItem("tod", AfecsTool.getCurrentTimeInH()));
            } catch (cMsgException e) {
                e.printStackTrace();

            }
            try {
                myPlatformConnection.send(msg);
            } catch (cMsgException e) {
                e.printStackTrace();

                status = false;
            }
        } else {
            status = false;
        }
        return status;
    }

    /**
     * Sends daLogMsg subject = component name and type = rc/report/dalog
     *
     * @param name       of the initiator
     * @param severityId severity id
     * @param severity   severity string ( ERROR, WARNING, etc.)
     * @param text       of the dalog message
     * @return status of the operation
     */
    public boolean dalogMsg(String name, int severityId, String severity, String text) {
        boolean status = false;
        if (myPlatformConnection != null && myPlatformConnection.isConnected()) {
            cMsgMessage msg = new cMsgMessage();
            msg.setSubject(name);
            msg.setType(AConstants.RcReportDalog);
            msg.setUserInt(severityId);
            msg.setText(text);
            try {
                msg.addPayloadItem(new cMsgPayloadItem(AConstants.EXPID, myConfig.getPlatformExpid()));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.CODANAME, myName));

                // codaid comes from the client
                msg.addPayloadItem(new cMsgPayloadItem("codaid", "na"));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.CLIENTHOST, "na"));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.USERNAME, "afecs"));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.SESSION, mySession));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.RUNTYPE, myRunType));

                // config ID is set (payload = "runType" is set by the client
                msg.addPayloadItem(new cMsgPayloadItem("runType", "na"));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.RUNNUMBER, "na"));

                if (msg.getText() != null) {
                    msg.addPayloadItem(new cMsgPayloadItem(AConstants.DALOGTEXT, msg.getText()));
                }

                msg.addPayloadItem(new cMsgPayloadItem("severity", severity));

                msg.addPayloadItem(new cMsgPayloadItem(AConstants.DALOGSEVERITYID, msg.getUserInt()));

                msg.addPayloadItem(new cMsgPayloadItem("state", "na"));

                msg.addPayloadItem(new cMsgPayloadItem("codaClass", "agent"));

                msg.addPayloadItem(new cMsgPayloadItem("tod", AfecsTool.getCurrentTimeInH()));
            } catch (cMsgException e) {
                e.printStackTrace();

            }
            try {
                myPlatformConnection.send(msg);

            } catch (cMsgException e) {
                e.printStackTrace();

                status = false;
            }
        } else {
            status = false;
        }
        return status;
    }


    /**
     * Sends alarm message to a predefined type
     * {@link org.jlab.coda.afecs.system.AConstants#AgentReportAlarm}
     *
     * @param s          the subject of the alarm message, usually receiver
     * @param name       of the component sending the dalog message
     * @param severityId severity id
     * @param severity   severity string ( ERROR, WARNING, etc.)
     * @param text       of the alarm message
     * @return status of the operation
     */
    public boolean reportAlarmMsg(String s, String name, int severityId, String severity, String text) {

        // define severity string from severity id if not set
        if (severity.equals(AConstants.udf)) {
            switch (severityId) {
                case 1:
                case 2:
                case 3:
                case 4:
                    severity = AConstants.INFO;
                    break;
                case 5:
                case 6:
                case 7:
                case 8:
                    severity = AConstants.WARN;
                    break;
                case 9:
                case 10:
                case 11:
                case 12:
                    severity = AConstants.ERROR;
                    break;
                case 13:
                case 14:
                    severity = AConstants.SERROR;
                    break;
                case 15:
                    severity = "SYSTEM";
                    break;

            }
        }
        ArrayList<cMsgPayloadItem> al = new ArrayList<>();
        try {
            al.add(new cMsgPayloadItem(AConstants.CODANAME, name));
            al.add(new cMsgPayloadItem(AConstants.TIMESTAMP, AfecsTool.getCurrentTimeInH()));
            al.add(new cMsgPayloadItem(AConstants.EXPID, plEXPID));
            al.add(new cMsgPayloadItem(AConstants.DALOGSEVERITY, severity));
            al.add(new cMsgPayloadItem(AConstants.DALOGSEVERITYID, severityId));
            al.add(new cMsgPayloadItem(AConstants.DALOGTEXT, text));
        } catch (cMsgException e) {
            e.printStackTrace();
         }
        return send(s, AConstants.AgentReportAlarm, al);
    }

    public String parseRocDataLink(String fileName) {
        String out = null;
        String s;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            while ((s = br.readLine()) != null) {
                if (s.startsWith("etName")) {
                    StringTokenizer st = new StringTokenizer(s, "_");
                    while (st.hasMoreTokens()) {
                        out = st.nextToken();
                    }
                    br.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        return out;
    }

    /**
     * Method that reads the configuration file ( being it
     * COOL or user specified) and substitutes RTV's with values
     * from the RTV map.
     *
     * @param filePath full path of the config file
     * @return content of a file with substituted RTV values
     * @throws java.io.IOException
     */
    public String readFileAsString(String filePath) throws java.io.IOException {

        String out = null;
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(filePath));
            int r = f.read(buffer);
            out = new String(buffer);
            if (AfecsTool.containsRTV(out)) {

                // substitute rtvs
                out = AfecsTool.checkRtvs(out, _RTVs);
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                    myName + ": Config file in " +
                    filePath + " was not found.");
            dalogMsg(myName, 9, AConstants.WARN, " Warning: Config file in " +
                    filePath + " was not found.");
        } finally {
            if (f != null) try {
                f.close();
            } catch (IOException e) {

                e.printStackTrace();
                System.out.println(AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                        myName + ": At closing config file in " + filePath);
                dalogMsg(myName, 9, AConstants.WARN,
                        " Warning: at closing config file in " + filePath);
            }
        }
        return out;
    }

    /**
     * Stores the RTV map
     *
     * @param rtvs .
     */
    public void updateRTVMap(Map<String, String> rtvs) {
        _RTVs = rtvs;
    }


    /**
     * @param to in milli seconds
     */
    public String rcClientInfoSyncGetState(int to) throws AException {
        cMsgMessage msg = rcp2pSend(myName, AConstants.CodaInfoGetState, "getState", to);
        if (msg != null && msg.getText() != null) {
            return msg.getText();
        } else {
            return AConstants.failed;
        }
    }

    // ----------------------------------------------------------------------
    public void sessionControlStartReporting() throws cMsgException {

        a_println("DDD -----| Info: " + AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                myName + ": --> startReporting");

        rcSend(myName, AConstants.SessionControlStartReporting, "startReporting");
    }


    public void sessionControlStopReporting() throws cMsgException {
        rcSend(myName, AConstants.SessionControlStopReporting, "stopReporting");
    }

    public void sessionControlSetSession(String s) throws cMsgException {

        a_println("DDD -----| Info: " + AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                myName + ": --> setSession");

        ArrayList<cMsgPayloadItem> nl = new ArrayList<>();
        nl.add(new cMsgPayloadItem(AConstants.SESSION, s));
        rcSend(myName, AConstants.SessionControlSetSession, "setSession", nl);
    }

    public void runControlSetRunNumber(int i) throws cMsgException {
        ArrayList<cMsgPayloadItem> nl = new ArrayList<>();
        nl.add(new cMsgPayloadItem(AConstants.RUNNUMBER, i));
        rcSend(myName, AConstants.SessionControlSetRunNumber, "setRunNumber", nl);
    }

    public void runControlSetRunType(String s) throws cMsgException {

        a_println("DDD -----| Info: " + AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                myName + ": --> setRunType");

        ArrayList<cMsgPayloadItem> nl = new ArrayList<>();
        nl.add(new cMsgPayloadItem(AConstants.RUNTYPE, s));
        rcSend(myName, AConstants.SessionControlSetRunType, "setRunType", nl);
    }

    /**
     * <p>
     * Broadcasts information about the platform,
     * along with a message passed as a parameter.
     * </p>
     *
     * @param info message text
     */
    public void a_println(String info) {
        if (isPlatformConnected()) {
//            send(getPlEXPID(), AConstants.RcConsoleReport, info);
            System.out.println(info);
        }
    }

}
