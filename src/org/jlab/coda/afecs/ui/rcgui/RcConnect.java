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
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 *     Swing worker thread that does run-control
 *     supervisor connect procedure.
 *     Starts UI updating thread that periodically
 *     checks the graphing components map.
 *     Does appropriate subscriptions.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/21/14 Time: 1:51 PM
 * @version 4.x
 */
public class RcConnect extends SwingWorker<String, Void> {

    private CodaRcGui owner;
    private String supSession = "";
    private boolean start = false;
    private boolean isSessionDefined = true;

    public RcConnect(CodaRcGui owner){
        this.owner = owner;


        if(owner.getSessionTextField().getText().equals(AConstants.udf)){
            owner.updateDaLogTable(owner.getName(),"Session is not defined", AConstants.WARN,7);
            isSessionDefined = false;
        } else {

            owner.hardResetUi(true);
            owner.ResetRequest.set(false);
            if(owner.getConfigurationTextField().getText()!=null &&
                    !owner.getConfigurationTextField().getText().trim().equals(AConstants.udf) &&
                    owner.getSessionTextField().getText()!=null &&
                    !owner.getSessionTextField().getText().trim().equals(AConstants.udf)) {

                owner._runType = owner.getConfigurationTextField().getText().trim();
                owner._session = owner.getSessionTextField().getText().trim();
                owner._supervisorName = "sms_"+owner._runType;

                owner.gDriver._updateControlBtNs(RcStates.UNDEFINED);

                start = true;

            } else {
                owner.updateDaLogTable(owner.getName(),"Session and/or runType is not defined",
                        AConstants.WARN,7);
                isSessionDefined = false;
            }
        }
    }

    @Override
    protected String doInBackground() throws Exception {
        String out = "missing";

        if(start){
            // supervisor is already configured in the required session
            // start subscriptions
            owner.doSubscriptions();

            List<cMsgPayloadItem> al = new ArrayList<>();
            try {
                al.add(new cMsgPayloadItem(AConstants.SESSION,owner._session));
                al.add(new cMsgPayloadItem(AConstants.RUNTYPE,owner._runType));
            } catch (cMsgException e1) {
                if(AConstants.debug.get()) e1.printStackTrace();
            }

            // ask platform registrar to return rtv
            // file content for a defined runType
            cMsgMessage m = owner.base.p2pSend(AConstants.CONTROLDESIGNER,
                    AConstants.DesignerInfoRequestGetDefinedRTVs,
                    al,
                    AConstants.TIMEOUT);

            if(m!=null && m.getByteArray()!=null){
                try{
                    Map<String,String> remRtv = (Map<String,String>) AfecsTool.B2O(m.getByteArray());

                    // we got back those rtvs that are actually
                    // defined in the cool update owner rtv map
                    owner.getRTVMap().clear();
                    for(String s:remRtv.keySet()){
                        owner.addRTV(s,remRtv.get(s));
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            // ask platform to parse config and report the list of agents
            owner.base.p2pSend(AConstants.CONTROLDESIGNER,
                    AConstants.DesignerInfoRequestControlAgents,
                    al,
                    AConstants.TIMEOUT);

            // Ask platform registrar if supervisor is registered.
            // N.B. supervisor name = sms_config name (runType)
            cMsgMessage msgb = owner.base.p2pSend(
                    owner.base.getPlEXPID(),
                    AConstants.PlatformInfoRequestisRegistered,
                    owner._supervisorName, AConstants.TIMEOUT);

            if(msgb!=null &&
                    msgb.getText()!=null &&
                    msgb.getText().equals(AConstants.yes)) {

                // Supervisor for the required runType is registered
                // Ask supervisor to report session for which it was configured
                msgb = owner.base.p2pSend(owner._supervisorName,
                        AConstants.AgentInfoRequestSession,
                        "",
                        AConstants.TIMEOUT);
                if(msgb!=null &&
                        msgb.getPayloadItem(AConstants.SESSION)!=null){
                    supSession = msgb.getPayloadItem(AConstants.SESSION).getString();
                    if(supSession.equals(owner._session)){

                        // get the state of the active supervisor
                        msgb = owner.base.p2pSend(owner._supervisorName,
                                AConstants.AgentInfoRequestState,
                                "",
                                AConstants.TIMEOUT);
                        if(msgb!=null &&
                                msgb.getText()!=null){
                            out = msgb.getText();
                        } else {
                            out = "error";
                        }
                    } else if(msgb.getPayloadItem(AConstants.SESSION).getString().equals(AConstants.udf)){
                        out ="undefined";
                    } else {
                        // supervisor is configured in another session
                        out = "busy";
                    }
                }
            }
        }
        return out;
    }

    @Override
    protected void done() {
        super.done();
        owner.updateRTVGui();
        try {
            String res = get();
            if(!res.equals("missing")){
                switch (res) {
                    case "error":
                        // supervisor is no responding
                        owner.updateDaLogTable(owner.getName(),
                                "Problem communicating with the supervisor agent",
                                AConstants.ERROR,9);
                        break;

                    case "undefined":
                        // supervisor exists but not configured
                        owner.updateDaLogTable(owner.base.myName,
                                " Supervisor is not configured.",
                                AConstants.WARN,
                                5);

                        // Start ui updating thread
                        if (!owner.isEnable_fx()) owner.gUpdateThread.start();

                        owner.gDriver._updateControlBtNs(RcStates.CONNECTED);
                        owner.monitoredComponent = AConstants.udf;
                        break;

                    case "busy":
                        // supervisor is configured in another session
                        owner.updateDaLogTable(owner.base.myName,
                                "RunType is in the session = "+supSession
                                        +  ".Change the session if you want to join",
                                AConstants.WARN,
                                5);
                        break;

                    default:
                        // configured supervisor in the required session
                        owner.getRunStateTextField().setText(res);
                        owner._runState = res;
                        owner.previousState = res;

                        // start gui graphics updating timer thread
                        if (!owner.isEnable_fx()) owner.gUpdateThread.start();
                        owner.gDriver._updateControlBtNs(owner.gDriver.getRcState(res));
                        owner.monitoredComponent = AConstants.udf;
                        break;
                }
            } else {
                if (!owner.isEnable_fx()) owner.gUpdateThread.start();
                if(isSessionDefined) {
                    owner.gDriver._updateControlBtNs(RcStates.CONNECTED);
                } else {
                    owner.gDriver._updateControlBtNs(RcStates.UNDEFINED);
                }
                owner.monitoredComponent = AConstants.udf;
                owner.getRunStateTextField().setText(AConstants.udf);
                owner.getConfigureButton().setEnabled(true);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
