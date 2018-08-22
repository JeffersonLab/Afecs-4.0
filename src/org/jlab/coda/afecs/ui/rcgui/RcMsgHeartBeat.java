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
import org.jlab.coda.cMsg.cMsgMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * <p>
 * Contains a timer that fires up an event
 * every defined time interval (default = 1sec.)
 * that will check if it is connected to the
 * platform cMsg server. If not will keep trying
 * to connect util cMSg server, i.e. platform
 * will be up and running again.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */
public class RcMsgHeartBeat {

    private int delay = 2000;
    private CodaRcGui owner;
    private Timer timer;
    private boolean wasConnected = true;
    private boolean checked = false;
    private Frame popUpFrame;
    private String dalogArchiverState = AConstants.udf;

    ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
            new AAction().execute();
        }
    };

    public RcMsgHeartBeat(CodaRcGui owner) {
        this.owner = owner;
        timer = new Timer(delay, actionListener);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    private class AAction extends SwingWorker<Integer, Void> {

        @Override
        protected Integer doInBackground() throws Exception {
            if (!owner.base.isPlatformConnected()) {
                // get a new name for a new connection
                owner.base.myName = "rcGui-" + new Random().nextInt(100);

                if (owner.isMulticast) {
                    owner.base.myPlatformConnection =
                            owner.base.platformConnect(owner.UIMulticastUDL);
                } else {
                    owner.base.myPlatformConnection =
                            owner.base.platformConnect(owner.base.getPlUDL());
                }
            } else {
                //connected ask platform if dalogArchiver is active
                cMsgMessage msb = owner.base.p2pSend(owner.base.getPlEXPID(),
                        AConstants.PlatformControlArchiverState,
                        " ", 30000);
                if (msb != null) {
                    if (msb.getText() != null) {
                        if (!dalogArchiverState.equals(msb.getText())) {
                            dalogArchiverState = msb.getText();
                        }
                    }
                    checked = true;
                } else {
                    checked = false;
                }
            }

            return null;
        }

        @Override
        protected void done() {
            super.done();
            if (checked) return;
            if (!owner.base.isPlatformConnected() && wasConnected) {
                wasConnected = false;
                owner.updateDaLogTable(owner.base.myName,
                        " Lost connection to the platform",
                        AConstants.ERROR, 9);
                owner.gDriver._updateControlBtNs(RcStates.UNDEFINED);
                owner.getConnectMenuItem().setEnabled(false);
            } else if (owner.base.isPlatformConnected() && !wasConnected) {
                wasConnected = true;
                owner.updateDaLogTable(owner.base.myName,
                        " Restored connection to the platform",
                        AConstants.INFO,
                        1);
                if (popUpFrame != null) popUpFrame.dispose();
                popUpFrame = null;
                owner.gDriver._updateControlBtNs(RcStates.CONNECTED);

                owner.hardResetUi(true);
            }
        }
    }
}
