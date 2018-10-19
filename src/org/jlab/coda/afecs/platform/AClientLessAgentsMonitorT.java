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

package org.jlab.coda.afecs.platform;

import org.jlab.coda.afecs.codarc.CodaRCAgent;
import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;

import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgMessage;

import java.util.Map;

/**
 * <p>
 * This thread will periodically ask future agent to report sync its state.
 * Whenever agent will be created on the container, it will respond back
 * to this callback. In the same time periodically agent's (real or virtual(not started))
 * will be sent to all GUIs for visualization.
 * </p>
 *
 * @author gurjyan
 *         Date: 4/6/15 Time: 8:53 AM
 * @version 4.x
 */
public class AClientLessAgentsMonitorT extends ABase implements Runnable {

    private Map<String, AComponent> agents;
    private String session;
    private String runType;
    private String registrationKey;
    private Map<String, Thread> registrationMap;
    private boolean keepRunning = true;

    private APlatform myPlatform;

    AClientLessAgentsMonitorT(APlatform platform, Map<String, AComponent> agents,
                              String session, String runType,
                              Map<String, Thread> registrationMap) {

        myPlatform = platform;
        this.agents = agents;
        this.session = session;
        this.runType = runType;
        registrationKey = session + "_" + runType;
        this.registrationMap = registrationMap;

        myName = AfecsTool.generateName(AConstants.ORPHANAGENTMONITOR);

        // Connect to the platform cMsg domain server
        myPlatformConnection = platformConnect();
        if (!isPlatformConnected()) {
            System.out.println(" Problem starting OrphanAgentMonitor. " +
                    "Cannot connect to the platform.");
            stopMe();
        }
    }

    private void stopMe() {
        keepRunning = false;
        registrationMap.remove(registrationKey);
    }

    @Override
    public void run() {
        while (keepRunning) {
            boolean stop = true;
            for (AComponent c : agents.values()) {
                if (c.getState().equals(AConstants.udf) ||
                        c.getState().equals(AConstants.checking)
                        ) {
                    stop = false;
                    if (myPlatform.container.getContainerAgents().containsKey(c.getName())) {
                        CodaRCAgent ta = myPlatform.container.getContainerAgents().get(c.getName());
                        c.setState(ta.me.getState());
                    }
                }
            }
            send(AConstants.GUI,
                    session + "_" + runType + "/agents",
                    "udf",
                    agents);

            if (stop) {
                System.out.println("DDD ----| "+myName +" Info: stop orphan client monitoring thread.");
                stopMe();
            }

            AfecsTool.sleep(3000);

        }
    }
}
