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
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.cMsg.cMsgCallbackAdapter;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class ControlCB extends cMsgCallbackAdapter{

    private CodaRcGui owner;

    public ControlCB(CodaRcGui owner){
        this.owner = owner;
    }

    /************************************************************************
     * Private inner class for responding messages from supervisor agent of the specific control system.
     */
    public void callback(cMsgMessage msg, Object userObject){
        if(msg!=null) {

            String type = msg.getType();
            if (type != null) {
                new AAction(msg).execute();
            }
        }
    }


    private class AAction extends SwingWorker<Integer, Void>{

        private cMsgMessage msgR;

        public AAction(cMsgMessage messageReceived){
            this.msgR = messageReceived;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            int out = -1;
            String receivedType = msgR.getType();

            // supervisor requests confirmation to reset
            if(receivedType.equals(AConstants.UIControlRequestReset)){
                if(!(owner.dialog_is_up.get() ||
                        owner._runState.equals(AConstants.booted) ||
//                        owner._runState.equals(AConstants.configured) ||
                        owner._runState.equals(AConstants.connected)
                ) && !owner.ResetRequest.get()){
                    out = 1;
                }
                // supervisor asks ui to confirm that we need to disable disconnected components
            } else if(receivedType.equals(AConstants.UIControlRequestDisableDisconnected)){
                if(msgR.isGetRequest()){
                    out = 2;
                }
            } else if(receivedType.equals(AConstants.UIControlRequestSetRunNumber)){
                if(msgR.getPayloadItem(AConstants.RUNNUMBER)!=null){
                    out = 3;
                }
            } else if(receivedType.equals(AConstants.UIControlPopupInfo)){
                out = 4;
            } else if(receivedType.equals(AConstants.UIControlContainerDead)){
                out = 5;
            }
            return out;
        }

        @Override
        protected void done() {
            super.done();
            String[] buttons = new String[2];
            buttons[1] = "Cancel";
            int ans;
            try {
                switch(get()){
                    case 1:
                        owner.dialog_is_up.set(true);
                        String receivedText = msgR.getText();
                        buttons[0] = "Reset";
                        ans = owner.popupQuestionDialog(receivedText,buttons);
                        if(ans==JOptionPane.YES_OPTION){
                            owner.ResetRequest.set(true);
                            if(!(owner._runState.equals(AConstants.disconnected) ||
                                    owner._runState.equals(AConstants.udf) ||
                                    owner._runState.equals(RcStates.CONFIGURING.getName()))) {
                                try {
                                    owner.updateDaLogTable(owner.base.myName, " Reset issued.", AConstants.INFO, 1);
                                    owner.base.p2pSend(owner._supervisorName, AConstants.AgentControlRequestMoveToState,AConstants.reseted, 15000);
                                } catch (AException e) {
                                    owner.updateDaLogTable(owner.base.myName,
                                            " Reset failed due to slow communication. Try resetting again. ",
                                            AConstants.WARN, 1);
                                }
                            }
                        }
                        owner.dialog_is_up.set(false);
                        break;
                    case 2:
                        try {
                            cMsgMessage mr = msgR.response();
                            mr.setSubject(AConstants.udf);
                            mr.setType(AConstants.udf);

                            String message = "  Disable disconnected components?\n" +
                                    "  Attention! \n" +
                                    "  Components will be disabled until the next Configure transition.";
                            buttons[0] = "Disable";
                            ans = owner.popupQuestionDialog(message,buttons);
                            if(ans== JOptionPane.OK_OPTION){
                                // message content to the supervisor is disable
                                mr.setText("disable");
                                owner.getRunStatePanel().setBackground(Color.YELLOW);
                            } else {
                                // message content is cancel
                                mr.setText("cancel");
                            }
                            owner.base.myPlatformConnection.send(mr);
                        } catch (cMsgException e) {
                            if(AConstants.debug.get()) e.printStackTrace();
                        }
                        break;
                    case 3:
                        try {
                            owner._runNumber = msgR.getPayloadItem(AConstants.RUNNUMBER).getInt();
                            owner.getRunNumberTextField().setText(Integer.toString(owner._runNumber));
                        } catch (cMsgException e) {
                            if(AConstants.debug.get()) e.printStackTrace();
                        }
                        break;
                    case 4:
                        try {
                            if(msgR.getPayloadItem("MSGCONTENT")!=null){
                                String tmp = msgR.getPayloadItem("MSGCONTENT").getString();
                                owner.popupInfoDialog(tmp);
                                if(tmp.contains("Denied")){
                                    owner.gDriver._updateControlBtNs(RcStates.DISCONNECTED);
                                }
                            }
                        } catch (cMsgException e) {
                            if(AConstants.debug.get()) e.printStackTrace();
                        }
                        break;
                    case 5:
//                        owner.updateDaLogTable(owner.base.myName, " Detected dead agent container", AConstants.error, 9);
                        break;
                }
            } catch (InterruptedException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            } catch (ExecutionException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            }
        }
    }
}


