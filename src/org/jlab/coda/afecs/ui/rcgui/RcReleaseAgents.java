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

import javax.swing.*;

/**
 * <p>
 *      After confirmation dialog asks
 *      supervisor agent to release
 *      agents under its supervision.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/21/14 Time: 1:51 PM
 * @version 2
 */
public class RcReleaseAgents extends SwingWorker {

    private CodaRcGui owner;
    private int ans;

    public RcReleaseAgents(CodaRcGui owner){
        this.owner = owner;
        String message = "  Are you sure you want to release "+owner._runType+
                "  components from the session "+owner._session+" ?\n";
        String[] buttons = {"Release", "Cancel"};
        ans = owner.popupQuestionDialog(message,buttons);

    }

    @Override
    protected Object doInBackground() throws Exception {
        if( ans == JOptionPane.YES_OPTION){
            new RcCodaSMCmd(owner, CodaSM.RESET).execute();
            AfecsTool.sleep(1000);
            owner.base.send(owner._supervisorName,
                    AConstants.SupervisorControlRequestReleaseAgents,
                    "");
            AfecsTool.sleep(1000);

            // ask supervisor agent to release runType.
            // Att. this next request will repeat some
            // of the actions performed in the previous
            // request.
            owner.base.send(owner._supervisorName,
                    AConstants.SupervisorControlRequestReleaseRunType,
                    "");

            if(owner.isEnable_fx()){
             owner.resetCharts_fx();
            } else {
            owner.softResetCharts_cosy();
            }
            owner.un_Subscribe();
        }
        return null;
    }

    @Override
    protected void done() {
        super.done();
        owner._clear_gui();
        owner.gDriver._updateControlBtNs(RcStates.DISCONNECTED);

    }
}
