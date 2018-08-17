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
import org.jlab.coda.afecs.ui.rcgui.util.CLRNSetGui;
import org.jlab.coda.cMsg.cMsgMessage;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 *     Asking a platform to set the run number
 * </p>
 *
 * @author gurjyan
 *         Date: 11/21/14 Time: 1:51 PM
 * @version 2
 */

public class RcSetRunNumber extends SwingWorker <int[], Void> {

    private CodaRcGui owner;

    public RcSetRunNumber(CodaRcGui owner){
        this.owner = owner;
    }

    @Override
    protected int[] doInBackground() throws Exception {
        int[] res = new int[2];
        if (!owner._runState.equals(AConstants.active) &&
                !owner._runState.equals(AConstants.prestarted)
                && !owner._runState.endsWith("ing") ){

            // send message to the platform and ask run numbers in both DBs
            cMsgMessage msg = owner.base.p2pSend(owner.base.getPlEXPID(),
                    AConstants.PlatformInfoRequestRunNumbers,
                    owner._session,
                    AConstants.TIMEOUT);
            if(msg!=null && msg.getByteArray()!=null){
                try {
                    res = (int[]) AfecsTool.B2O(msg.getByteArray());
                } catch (IOException|ClassNotFoundException e1) {
                    res[0] = -1;
                }
            }

        }  else {
            res[0] = -2;
        }
        return res;
    }

    @Override
    protected void done() {
        super.done();
        boolean setRN = true;
        try {
            int[] o = get();
            if(o!=null && o.length==2){
                switch (o[0]){
                    case -1:
                        owner.popupInfoDialog(" Error !\n" +
                                "  Communicating with the platform admin agent");
                        break;
                    case -2:
                        owner.popupInfoDialog(" Attention !\n" +
                                "  You can not reset run number if runControl is \n" +
                                "  in active or prestarted states, as well as in the \n" +
                                "  process of transitioning between states.");
                        setRN = false;
                        break;
                }

                if(setRN){
                        CLRNSetGui g = new CLRNSetGui(owner,o);
                        g.setVisible(true);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }
}
