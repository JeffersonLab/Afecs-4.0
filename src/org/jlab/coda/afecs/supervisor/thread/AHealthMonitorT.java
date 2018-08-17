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

package org.jlab.coda.afecs.supervisor.thread;

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.supervisor.SupervisorAgent;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.ALogger;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.util.ArrayList;
import java.util.Date;


/**
 * <p>
 * Thread that monitors the health of agents/container
 * </p>
 *
 * @author gurjyan
 *         Date: 11/13/14 Time: 2:51 PM
 * @version 3.x
 */
public class AHealthMonitorT extends Thread {

    private long timeTick;
    private static final int warningTO = 10000; // in milli-sec.
    private static final int errorTO = 60000;
    private static final int erCheckTimeout = 3; // in seconds

    private SupervisorAgent owner;

    // local instance of the logger object
    private ALogger lg = ALogger.getInstance();

    private boolean isRunning = true;

    public AHealthMonitorT(SupervisorAgent owner) {
        this.owner = owner;
    }

    public synchronized void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void run() {
        super.run();
        while (isRunning) {
            if (!owner.me.getState().equals(AConstants.disconnected)) {
                long checkTime = new Date().getTime();

                boolean _allConfigured = true;

                for (String s : owner.myCompReportingTimes.keySet()) {

                    AComponent aComponent = owner.myComponents.get(s);

                    // See if agent state is not removed
                    if (aComponent != null &&
//                            !aComponent.getHost().equals(AConstants.udf) &&
                            aComponent.getState() != null &&
                            !aComponent.getState().equals(AConstants.removed)) {

                        if (!aComponent.getState().equals(AConstants.configured)) _allConfigured = false;

                        long crt = owner.myCompReportingTimes.get(s).getCurrentReportingTime();
                        if (crt > 0) {
                            timeTick++;
                            long rt = checkTime - crt;

                            // Store time since last reporting into the map
                            owner.myCompReportingTimes.get(s).setTimeSinceLastReporting(rt);

                            if ((rt > warningTO) && (rt < errorTO)) {
                                // Warning
                                if (timeTick % 3 == 0) {
                                    owner.reportAlarmMsg(owner.me.getSession() +
                                                    "/" + owner.me.getRunType(),
                                            owner.myName,
                                            5,
                                            AConstants.WARN,
                                            " No response from agent " +
                                                    s + " " + rt / 1000 + " sec.");
                                    owner.dalogMsg(owner.myName,
                                            5,
                                            AConstants.WARN,
                                            " No response from agent " +
                                                    s + " " + rt / 1000 + " sec.");
                                }
                            } else if (rt > errorTO) {
                                _handleError(aComponent, erCheckTimeout);
                            }
                        }
                    }
                }
            }
            AfecsTool.sleep(1000);
        }
    }

    /**
     * <p>
     * Method that handles the case when agent
     * is not reporting more than errorTO
     * </p>
     *
     * @param aComponent reference to the AComponent object
     * @param timeout    this defines how many seconds (how many
     *                   times send-and-get with 1sec timeout) this
     *                   method must sync request the agent or the
     *                   container after declaring it unresponsive.
     */
    private void _handleError(AComponent aComponent, int timeout) {

        // Ask explicitly the state of the agent
        cMsgMessage msg_a = null;
        try {
            msg_a = owner.p2pSend(owner.me.getName(),
                    AConstants.AgentInfoRequestState,
                    "",
                    AConstants.TIMEOUT);
        } catch (AException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
        }

        if (msg_a == null || msg_a.getText() == null) {
            owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                    owner.myName,
                    9,
                    AConstants.ERROR,
                    " Agent " + aComponent.getName() + " is unresponsive!");

            // Seems agent is dead. Check to see if container
            // for that agent is ok Container name is constructed
            // as host of the agent +"_admin"
            cMsgMessage msg_c = null;
            for (int i = 0; i < timeout; i++) {
                try {
                    msg_c = owner.p2pSend(aComponent.getHost() + "_admin",
                            AConstants.ContainerInfoRequestStatus,
                            "",
                            1000);
                } catch (AException e) {
                    lg.logger.severe(AfecsTool.stack2str(e));
                }
                if (msg_c != null) break;
            }
            if (msg_c == null || msg_c.getText() == null) {

                // Container is dead
                owner.reportAlarmMsg(owner.me.getSession() + "/" + owner.me.getRunType(),
                        owner.myName,
                        9,
                        AConstants.ERROR,
                        " Container on the host " + aComponent.getHost() + " is unresponsive!");
                owner.dalogMsg(owner.myName,
                        9,
                        AConstants.ERROR,
                        " Container on the host " + aComponent.getHost() + " is unresponsive!");

            } else {

                // container is alive but agent is dead.
                // Ask platform ControlDesigner to restart
                // agent on the same container
                ArrayList<cMsgPayloadItem> al = new ArrayList<>();
                try {
                    al.add(new cMsgPayloadItem(AConstants.AGENT, aComponent.getName()));
                    al.add(new cMsgPayloadItem(AConstants.SESSION, aComponent.getSession()));
                    al.add(new cMsgPayloadItem(AConstants.RUNTYPE, aComponent.getRunType()));
                    al.add(new cMsgPayloadItem(AConstants.CONTAINER, aComponent.getHost()));
                } catch (cMsgException e) {
                    lg.logger.severe(AfecsTool.stack2str(e));
                }
                owner.send(AConstants.CONTROLDESIGNER,
                        AConstants.DesignerControlRequestConfigureAgent,
                        al);

                owner.myCompReportingTimes.get(aComponent.getName()).setCurrentReportingTime(
                        new Date().getTime());
            }
        } else {
            owner.myCompReportingTimes.get(aComponent.getName()).setCurrentReportingTime(
                    new Date().getTime());

        }
    }

}


