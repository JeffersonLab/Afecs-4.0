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

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.platform.ASessionInfo;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *     Communicates with the Afecs platform registration services
 *     to get COOL as well as CODA2 msQL database information.
 *     namely:
 *     <ul>
 *         <li>
 *              Does cMsg send and get to the ControlDesigner agent
 *              and gets the names of all configuration files in the
 *              COOL_HOME/config directory. Color cods those runTypes
 *              that are running in the current gui selected session.
 *         </li>
 *         <li>
 *              Asks platform administrator to report runTypes recorded
 *              in the Coda2 database *
 *          </li>
 *          <li>
 *              Does cMsg send and get to the platform registration
 *              agent and gets the registered sessions.
 *          </li>
 *          <li>
 *              Asks platform administrator to report sessions
 *              recorded in the coda2 database
 *          </li>
 *          <li>
 *              Does cMsg send-and-get to the platform registrar agent
 *              and gets the registered documents.
 *          </li>
 *          <li>
 *              Does cMsg send-and-get to the platform registrar agent
 *              and gets run numbers stored both in COOL and msQL databases.
 *          </li>
 *     </ul>
 * </p>
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 3.x
 */
public class DbDriver {

    private CodaRcGui owner;
    public DbDriver(CodaRcGui owner){
        this.owner = owner;
    }

    /**
     * Gets the names of all registered configurations and
     * colors red the configuration that has an active supervisor agent
     * @param timeout timeout in milli seconds
     * @throws AException
     */
    public void getPlatformRegisteredRunTypes(int timeout) throws AException{
        timeout = timeout/1000;
        cMsgMessage msgb = null;

        for (int i=0; i<timeout;i++){
         try{
            msgb = owner.base.p2pSend(AConstants.CONTROLDESIGNER,
                    AConstants.DesignerInfoRequestGetConfigs,
                    "",
                    1000);
        } catch (Exception e){}

        if(msgb!=null) break;
        }

        String [] sa;
        if(msgb!=null){
            try {
                if(msgb.getPayloadItem("configFileNames")==null){
                    throw new AException("  Missing configuration files.");
                }
                sa = msgb.getPayloadItem("configFileNames").getStringArray();
            } catch (cMsgException e) {
                throw new AException(e.getMessage());
            }
            if(sa.length<0){
                throw new AException("  Missing RunType descriptions.");
            } else {
                owner._sessionConfigsColored.clear();

                for (String aSa : sa) {
                    owner._sessionConfigsColored.put(aSa, 0);
                }
                // ask platform to send all registered agents names
                cMsgMessage msg = owner.base.p2pSend(owner.base.getPlEXPID(),
                        AConstants.PlatformInfoRequestAgents,
                        "",
                        AConstants.TIMEOUT);
                if(msg!=null && msg.getByteArray()!=null && msg.getByteArrayLength()>0){
                    try {
                        // get platform registered agents hash map
                        ConcurrentHashMap<String, AComponent> registeredComs =
                                (ConcurrentHashMap<String, AComponent>) AfecsTool.B2O(msg.getByteArray());

                        if(registeredComs!=null && !registeredComs.isEmpty()){
                            for(String s:owner._sessionConfigsColored.keySet()){
                                // platform configuration supervisor agents name = sms_configName
                                if(registeredComs.containsKey("sms_"+s)) {

                                    if(registeredComs.get("sms_"+s).getSession().equals(owner._session)){
                                        owner._sessionConfigsColored.put(s,1);
                                    } else {
                                        owner._sessionConfigsColored.put(s,0);
                                    }
                                } else {
                                    owner._sessionConfigsColored.put(s,0);
                                }
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        throw new AException(e.getMessage());
                    }
                }
            }
        } else {
            throw new AException("  Problem communicating with the " +
                    "ControlDesigner of the Platform.\n  " +
                    "(Platform registered configurations request)");
        }
    }

    /**
     * Gets platform registered sessions
     * @param timeout in milli seconds
     * @throws AException
     */
    public void getPlatformRegisteredSessions(int timeout) throws AException{
        ConcurrentHashMap<String,ASessionInfo> smap;

        timeout = timeout/1000;
        cMsgMessage msgb = null;
        for (int i=0; i<timeout; i++){
            try{
            msgb = owner.base.p2pSend(owner.base.getPlEXPID(),
                    AConstants.PlatformInfoRequestSessions,
                    "",
                    1000);
            } catch (Exception e){}

            if(msgb!=null && msgb.getByteArray()!=null) break;
        }

        if(msgb!=null && msgb.getByteArray()!=null){
            try {
                smap = (ConcurrentHashMap<String,ASessionInfo>)AfecsTool.B2O(msgb.getByteArray());
            } catch (IOException | ClassNotFoundException e) {
                throw new AException(e.getMessage());
            }
            if(smap!=null){
                owner.platformSessions = smap;
            } else {
                throw new AException("  Problem getting platform registered sessions !\n  " +
                        "(Platform registered sessions request)");
            }

        } else {
            throw new AException("  Problem communicating with the ControlDesigner " +
                    "of the Platform.\n  " +
                    "(Platform registered sessions request)");
        }
    }

    /**
     * <p>
     *     Does cMsg send-and-get to the platform registrar
     *     agent and gets the registered documents.
     * </p>
     * @param timeout in milli seconds
     * @throws AException
     */
    public void getPlatformRegisteredDocs(int timeout) throws AException{
        ConcurrentHashMap<String,String> dmap = new ConcurrentHashMap<>();
        ArrayList<String> dNames;
        String dt;
        cMsgMessage msgb = null;
        timeout = timeout/1000;

        for (int i=0; i<timeout;i++){
            try{
            msgb = owner.base.p2pSend(owner.base.getPlEXPID(),
                    AConstants.PlatformInfoRequestDocs,
                    "",
                    1000);
            } catch(AException e){}

            if(msgb!=null && msgb.getByteArray()!=null) break;
        }

        if(msgb!=null &&
                msgb.getByteArray()!=null){
            try {
                dNames = (ArrayList<String>)AfecsTool.B2O(msgb.getByteArray());
            } catch (IOException | ClassNotFoundException e) {
                throw new AException(e.getMessage());
            }

            if(dNames!=null) {
                for(String dn:dNames){
                    msgb = owner.base.p2pSend(owner.base.getPlEXPID(),
                            AConstants.PlatformInfoRequestDocument,
                            dn,
                            AConstants.TIMEOUT);
                    if(msgb!=null &&
                            msgb.getByteArray()!=null){
                        try {
                            dt = (String)AfecsTool.B2O(msgb.getByteArray());
                        } catch (IOException | ClassNotFoundException e) {
                            throw new AException(e.getMessage());
                        }
                        if(dt!=null){
                            dmap.put(dn,dt);
                        }
                    }
                }
            }
            owner.platformDocs = dmap;
        } else {
            throw new AException("  Problem communicating with the Platform.\n  (" +
                    "Platform registered docs request)");
        }
    }

    /**
     * <p>
     *     Gets the run number from both COOL and msQL databases.
     * </p>
     *
     * @param  newRunNumber new value of the run number
     * @return int array of two elements:
     * first run number from the coda2 DB and
     * the second from the cool db.
     * @throws AException exception object
     */
    public int[] setAndGetRunNumbers ( int newRunNumber)
            throws AException {
        cMsgMessage msgrB;
        int[] res;
        if(newRunNumber>0){
            // ask supervisor to set COOL DB run number
            ArrayList<cMsgPayloadItem> al = new ArrayList<>();
            try {
                al.add(new cMsgPayloadItem(AConstants.RUNNUMBER,newRunNumber));
                owner.base.send(owner._supervisorName,
                        AConstants.SupervisorControlRequestSetRunNumber,
                        al);
            } catch (Exception e2) {
                throw new AException(" cMsgPayLoafItem creation error.");
            }
        }
        AfecsTool.sleep(1000);
        // read back from the platform
        msgrB = owner.base.p2pSend(owner.base.getPlEXPID(),
                AConstants.PlatformInfoRequestRunNumbers,
                owner._session,AConstants.TIMEOUT);
        if(msgrB!=null && msgrB.getByteArray()!=null){
            try {
                res = (int[])AfecsTool.B2O(msgrB.getByteArray());
            } catch (IOException | ClassNotFoundException e1) {
                e1.printStackTrace();
                throw new AException(e1.getMessage());
            }
        } else {
            throw new AException(" Error communicating with the platform admin agent");
        }
        return res;
    }

}
