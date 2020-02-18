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
import org.jlab.coda.cMsg.cMsgCallbackAdapter;
import org.jlab.coda.cMsg.cMsgMessage;

import javax.swing.*;
import java.util.concurrent.ExecutionException;

public class DaLogCB extends cMsgCallbackAdapter {

    private CodaRcGui owner;

    public DaLogCB(CodaRcGui owner){
        this.owner = owner;
    }

    /************************************************************************
     * Private inner class for responding messages from supervisor agent of the specific control system.
     */
    public void callback(cMsgMessage msg, Object userObject){
        if(msg!=null) {

            String type = msg.getType();
            if (type != null) {
                AAction a = new AAction(msg);
                a.execute();
            }
        }
    }

    private class AAction extends SwingWorker<Integer, Void> {

        private cMsgMessage msgR;
        private String messageTxt = AConstants.udf;
        private String sender     = AConstants.udf;
        private String severity   = AConstants.udf;
        private int severityId;

        public AAction(cMsgMessage messageReceived){
            msgR = messageReceived;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            int out = -1;

            if(msgR.getPayloadItem(AConstants.CODANAME)!=null)
                sender = msgR.getPayloadItem(AConstants.CODANAME).getString();
            if(msgR.getPayloadItem(AConstants.DALOGTEXT)!=null)
                messageTxt = msgR.getPayloadItem(AConstants.DALOGTEXT).getString();
            if(msgR.getPayloadItem(AConstants.DALOGSEVERITY)!=null)
                severity = msgR.getPayloadItem(AConstants.DALOGSEVERITY).getString();
            if(msgR.getPayloadItem(AConstants.DALOGSEVERITYID)!=null)
                severityId = msgR.getPayloadItem(AConstants.DALOGSEVERITYID).getInt();

            String type    = msgR.getType();
            String subject = msgR.getSubject();
            String rt = subject.substring(subject.lastIndexOf("/")+1);

            if(rt.equals(owner._runType) ||
                    (owner.msgReportingRuTypes.containsKey(rt) && owner.msgReportingRuTypes.get(rt)==1)){
                if(type.equals(AConstants.AgentReportAlarm)){
                    out = 1;
                } else if(type.equals(AConstants.AgentReportPopUpAlarm)) {
                    out = 2;
                }
            }

            return out;
        }

        @Override
        protected void done() {
            super.done();
            try {
                switch(get()){
                    case 1:
                        owner.updateDaLogTable(sender,messageTxt,severity,severityId);
                        break;
                    case 2:
                        owner.popupInfoDialog(messageTxt);
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

