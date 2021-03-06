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
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 * Run control main configuration worker thread
 * </p>
 *
 * @author gurjyan
 * Date: 11/21/14 Time: 1:51 PM
 * @version 4.x
 */

public class RcConfigure extends SwingWorker<Integer, Void> {

    private CodaRcGui owner;
    private boolean start = false;

    RcConfigure(CodaRcGui owner) {
        this.owner = owner;
        if (this.owner._runType.equals(AConstants.udf) ||
                this.owner._session.equals(AConstants.udf)) {
            this.owner.updateDaLogTable(this.owner.getName(),
                    "Select or set session and runType",
                    AConstants.WARN, 7);
        } else {

            this.owner.resetScheduler();
            this.owner.hardResetUi(false);

            // reset total events and component watch fields
            this.owner.getMonitorCompTextField().setText("");
            this.owner.getEventNumberTextField().setText("0");
            this.owner.getOutputFileComboBox().removeAllItems();

            this.owner.ResetRequest.set(false);

            // restore the color of the RunStatePanel
            this.owner.getRunStatePanel().setBackground(new Color(245, 246, 247));

            this.owner.updateDaLogTable(owner.base.myName,
                    " Configure is started. ",
                    AConstants.INFO,
                    1);

            start = true;
        }
    }

    @Override
    protected Integer doInBackground() throws Exception {
        int out = -1;
        if (start) {
            if (owner.base.isPlatformConnected()) {

                // tell an existing supervisor to enable fileWriting
                owner.base.send(owner._supervisorName, AConstants.SupervisorControlRequestResumeFileOutput, 0);

                // rcgui subscriptions
                owner.un_Subscribe();
                owner.doSubscriptions();

                // Ask ControlDesigner to configure.
                ArrayList<cMsgPayloadItem> al = new ArrayList<>();
                try {
                    al.add(new cMsgPayloadItem(AConstants.SESSION, owner._session));
                    al.add(new cMsgPayloadItem(AConstants.RUNTYPE, owner._runType));
                } catch (cMsgException e1) {
                    e1.printStackTrace();
                }

                // ask control designer to configure the run and send back the list of user
                // defined RTVs in the cool file (%()) that are not defined
                try {
                    cMsgMessage m = owner.base.p2pSend(AConstants.CONTROLDESIGNER,
                            AConstants.DesignerControlRequestConfigureControl,
                            "",
                            owner.getRTVMap(),
                            al,
                            AConstants.TIMEOUT);

                    if (m.getText().equals("config_failed")) {
                        out = 4;
                        return out;
                    }

                    try {
                        Map<String, String> remRtv = (Map<String, String>) AfecsTool.B2O(m.getByteArray());

                        // we got back those rtvs that are actually defined in the cool
                        // update owner rtv map
                        if (remRtv != null) {
                            owner.getRTVMap().clear();
                            for (String s : remRtv.keySet()) {
                                owner.addRTV(s, remRtv.get(s));
                            }
                        }

                        // add system rtv's
                        addSystemRTVs();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                } catch (AException e) {
                    System.out.println(e.getMessage());
                    out = 5;
                    return out;
                }

                // check the registry system in the loop until runType supervisor is registered.
                int trails = 0;
                cMsgMessage msgB;
                // wait for 20 seconds in the loop
                int waitForSupervisor = 30;
                do {
                    msgB = owner.base.p2pSend(owner.base.getPlEXPID(),
                            AConstants.PlatformInfoRequestisRegistered,
                            owner._supervisorName, 1000);
                    trails++;
                    AfecsTool.sleep(50);
                } while ((trails < waitForSupervisor) &&
                        (msgB == null || msgB.getText().equals(AConstants.no)) &&
                        !owner.ResetRequest.get() &&
                        !owner._runState.equals(AConstants.configured));

                if (trails >= waitForSupervisor) {
                    out = 1;
                } else if (owner.ResetRequest.get()) {
                    out = 2;
                } else {
                    out = 3;
                    owner.defineDefaultWatch();
                    owner.addRemoveSelected2Graph(true);
                }
            }
        }
        return out;
    }

    @Override
    protected void done() {
        super.done();
        try {
            switch (get()) {
                case 1:
                    owner.updateDaLogTable(owner.getName(),
                            "  Control supervisor is not registered. Try configuring again",
                            AConstants.WARN, 7);
                    System.out.println("Control supervisor is not registered. Try configuring again");
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                case 5:
                    owner.updateDaLogTable(owner.getName(),
                            "  Problem communicating with the ControlDesigner agent.",
                            AConstants.WARN, 7);
                    System.out.println("Problem communicating with the ControlDesigner agent.");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    private void addSystemRTVs() {
        // add config and dir RTVs
        owner.addRTV("%(session)", owner._session);
        owner.addRTV("%(rt)", owner._runType);
        owner.addRTV("%(rn)", owner.getRunNumberTextField().getText());
        owner.addRTV("%(config)", owner.getUserRunConfig());
        owner.addRTV("%(dir)", owner.getUserDir());
        owner.addRTV("%(ddb)", owner.base.myConfig.getCoolHome() + File.separator +
                owner.base.myConfig.getPlatformExpid() + File.separator + "ddb");
        owner.addRTV("%(udl)", owner.base.myConfig.getPlatformUdl());
    }
}
