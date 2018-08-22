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

import org.jlab.coda.afecs.supervisor.SupervisorAgent;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;

/**
 * Thread that is used to send message to all components
 * under this supervisor agent to move to a CODA state.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */
public class MoveToStateT extends Thread {
    private String stateName;
    private String componentName;
    private SupervisorAgent owner;

    public MoveToStateT(SupervisorAgent owner,
                        String comp,
                        String name) {
        this.owner = owner;
        stateName = name;
        componentName = comp;
    }

    public void run() {
        super.run();

        if (componentName.equals("all")) {
            for (String s : owner.myComponents.keySet()) {
                owner.send(s,
                        AConstants.AgentControlRequestMoveToState,
                        stateName);
            }
        } else {
            try {
                owner.p2pSend(componentName,
                        AConstants.AgentControlRequestMoveToState,
                        stateName, 5000);
            } catch (AException e) {
                owner.reportAlarmMsg(owner.me.getSession() +
                                "/" + owner.me.getRunType(),
                        owner.me.getName(),
                        7,
                        AConstants.WARN,
                        " Lost communication. Please reset and try again.");
            }
        }

    }

}
