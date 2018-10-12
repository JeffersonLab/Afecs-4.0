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
import org.jlab.coda.cMsg.cMsgMessage;

import javax.swing.*;
import java.util.Date;

/**
 * <p>
 *      Coda state machine command execution
 *      in a swing worker thread
 * </p>
 *
 * @author gurjyan
 *         Date: 11/21/14 Time: 1:51 PM
 * @version 4.x
 */
public class RcCodaSMCmd extends SwingWorker<Integer, Void>{

    private CodaRcGui owner;
    private CodaSM    smRequest;

    RcCodaSMCmd(CodaRcGui owner, CodaSM smr){
        this.owner = owner;
        this.smRequest = smr;
        owner.ResetRequest.set(false);

    }

    @Override
    protected Integer doInBackground() throws Exception {
        Integer out = AConstants.OK;
        switch(smRequest){
            case DOWNLOAD:
            case PRESTART:
            case GO:
            case END:
            case STARTRUN:

                cMsgMessage mb;
                int timeout = 15000;
                mb = owner.base.p2pSend(owner._supervisorName,
                        AConstants.SupervisorControlRequestStartService,
                        smRequest.cmd(), timeout);
                if (mb == null) {
                    owner.popupWarningDialog("Heavy network traffic detected!");
                    System.out.println("Heavy network traffic detected!" +
                            "\nError: cMsg p2pSend command timeout. " +
                            "\nIt is recommended to restart the GUI and try the command again." );
                    return out;
                } else {
                    // update gui
                    owner.gDriver._updateControlBtNs(owner.gDriver.getRcState(smRequest.inActionString()));
                }

                // wait for response from supervisor agent. The state of the supervisor
                // agent will be set as a text for RunStateTextField of the gui
                do{
                    AfecsTool.sleep(50);
                } while(!(owner._runState.equals(smRequest.response()) ||
                        owner.ResetRequest.get() ));
                if (owner.ResetRequest.get()){
                    out = AConstants.RESET;
                }
                break;
            case RESET:
                owner.base.p2pSend(owner._supervisorName,
                        AConstants.AgentControlRequestMoveToState,
                        AConstants.reseted, 15000);
                break;
        }

        return out;
    }

    @Override
    protected void done() {
        super.done();
        switch(smRequest){
            case RESET :
                owner.updateDaLogTable(owner.base.myName, " Reset issued.", AConstants.INFO, 1);
                break;
            case PRESTART:
                // update run preStart time
                owner.getStartTimeTextField().setText(owner.startEndFormatter.format(new Date()));
                owner.getEndTimeTextField().setText("");
                break;
            case GO:
            case STARTRUN:
                // update run start time
                owner.getStartTimeTextField().setText(owner.startEndFormatter.format(new Date()));
                owner.getEndTimeTextField().setText("");

                break;
            case END:
                // update run end time
                owner.getEndTimeTextField().setText(owner.startEndFormatter.format(new Date()));
                break;
        }
        owner.previousState = smRequest.response();
    }

}
