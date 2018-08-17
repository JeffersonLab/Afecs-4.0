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

package org.jlab.coda.afecs.codarc;

import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.ALogger;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;

import java.util.Date;

/**
 * <p>
 * Thread that monitors the health of the Client.
 * Uses client representing agents
 *
 * @author gurjyan
 *         Date: 11/12/14 Time: 2:51 PM
 * @version 3.x
 * @see CodaRCAgent#clientLastReportedTime variable
 * to report warning or error messages to supervisor
 * and GUIs. In case of missing reporting more than
 * limit that is defined to consider client as
 * disconnected this class will try sync requests
 * to get the state of the client, and if that also
 * fails will declare client true disconnected.
 * </p>
 */

public class ClientHeartBeatMonitor extends Thread {

    private long _timeAfterLastReporting;
    private long _absoluteTime;
    private int _disconnectTimeLimit; // in msec.
    private int _warningTimeLimit;
    private CodaRCAgent owner;
    private volatile boolean _hadWarning = false;
    private volatile boolean _stopWarning = false;

    // Local instance of the logger object
    private ALogger lg = ALogger.getInstance();

    /**
     * Constructor
     *
     * @param owner           reference to the agent
     * @param disconnectLimit tine limit (in  milli sec) after which
     *                        client is declared disconnected
     * @param warningLimit    time limit (in milli sec) after which warning
     *                        messages started to be generated
     */
    public ClientHeartBeatMonitor(CodaRCAgent owner,
                                  int disconnectLimit,
                                  int warningLimit) {
        this.owner = owner;
        _disconnectTimeLimit = disconnectLimit;
        _warningTimeLimit = warningLimit;
    }

    @Override
    public void run() {
        super.run();

        // main loop of monitor
        while (true) {
            // Increment the absolute time
            _absoluteTime++;

            // N.B. every time status is reported the date will be
            // stored in the owners clientLastReportingTime var.
            if (owner.clientLastReportedTime.get() > 0) {

                // Get the current time
                long currentTime = new Date().getTime();

                // Calculate how long time ago client had reported
                _timeAfterLastReporting =
                        currentTime - owner.clientLastReportedTime.get();

                // No reporting for more than disconnect time limit
                if (_timeAfterLastReporting >= _disconnectTimeLimit) {

                    owner.a_println("DDD -----| Info: " + AfecsTool.getCurrentTime("HH:mm:ss") + " " +
                            owner.myName + ": --> heartbeat listening thread - timeAfterLastReporting = " + _timeAfterLastReporting +
                            " disconnectTimeLimit = " + _disconnectTimeLimit);

                    _issueError(currentTime);
                    _stopWarning = true;
                } else if (_timeAfterLastReporting >= _warningTimeLimit) {
                    if (!_stopWarning) _issueWarning(10);
                }
            } else {
                if (_hadWarning) {

                    // There was at least one warning message
                    // because client was not reporting.
                    // Inform that client is reporting again
                    owner.reportAlarmMsg(owner.me.getSession() +
                                    "/" + owner.me.getRunType(),
                            owner.myName,
                            1,
                            AConstants.INFO,
                            " Client resumed reporting. ");
                    owner.dalogMsg(owner.myName,
                            1,
                            AConstants.INFO,
                            " Client resumed reporting. ");
                    _hadWarning = false;
                }
            }

            if (owner.me.getState().equals(AConstants.disconnected)) {
                this.stop();
            }
            // sleep for a second
            AfecsTool.sleep(1000);
        }
    }

    /**
     * <p>
     * Broadcast warning message to GUIs.
     * </p>
     *
     * @param tReport int sec after which the warning
     *                message must be broadcast
     */
    private void _issueWarning(int tReport) {
        if (_absoluteTime % tReport == 0) {
//        if(_absoluteTime % tReport == 0 &&
//                !(owner.me.getState().equals(AConstants.booted))) {
            owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                    owner.myName,
                    5,
                    AConstants.WARN,
                    " Client has not reported for " +
                            _timeAfterLastReporting / 1000 +
                            " sec.");
            owner.dalogMsg(owner.myName,
                    5,
                    AConstants.WARN,
                    " Client has not reported for " +
                            _timeAfterLastReporting / 1000 +
                            " sec.");
            _hadWarning = true;
        }
    }

    /**
     * <p>
     * Before issuing error and declaring client
     * as disconnected will sync get state of the
     * client 3 times with 1sec timeout each.
     * Otherwise if state finally came back it
     * will ask the client to resurrect status
     * reporting.
     * </p>
     *
     * @param currentTime the current time defined
     *                    within this class
     */
    private void _issueError(long currentTime) {

        // Ask explicitly the state of the client (9 sec total timeout with 3 sec steps)
        String st = owner._getClientState(9000, 3000);

        if (st.equals(AConstants.udf)) {
            // Client is not responding
            owner.reportAlarmMsg(owner.me.getSession() +
                            "/" + owner.me.getRunType(),
                    owner.myName,
                    9,
                    AConstants.ERROR,
                    " Client is unresponsive.");
            owner.dalogMsg(owner.myName,
                    9,
                    AConstants.ERROR,
                    " Client is unresponsive.");
            System.out.printf(AfecsTool.getCurrentTime("HH:mm:ss") + " " + owner.myName + ":Info - Client is unresponsive.");

            // Reset client last reporting time
            owner.clientLastReportedTime.set(0);

            // set last reported event and  data rates to 0
            owner.resetClientData();

            // Stop this thread
            this.stop();

        } else {
            owner.clientLastReportedTime.set(currentTime);
            owner.me.setState(st);

            // restart client status subscription
            owner.restartClientStatusSubscription();

            // Ask representing client to restart reporting
            try {
                owner.sessionControlStartReporting();
            } catch (cMsgException e) {
                lg.logger.severe(AfecsTool.stack2str(e));
                owner.a_println(AfecsTool.stack2str(e));
            }

            owner.a_println("DDD -----| Info: " + AfecsTool.getCurrentTime("HH:mm:ss") + " " + owner.myName +
                    ": --> heartbeat listening thread - getClientState = " + owner.me.getState());

        }
    }

}


