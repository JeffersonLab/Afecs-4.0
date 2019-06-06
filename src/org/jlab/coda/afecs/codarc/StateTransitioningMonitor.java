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

import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConstants;

import org.jlab.coda.afecs.system.util.AfecsTool;

import java.util.ArrayList;

/**
 * <p>
 *    The agent state is set as a result of process
 *    execution using described plugin, or in the
 *    cMsg callback. This thread will check the
 *    state of this agent until it is set or timeout
 *    has ben reached.
 * </p>
 * @author gurjyan
 *         Date: 11/12/14 Time: 2:51 PM
 * @version 4.x
 */
public class StateTransitioningMonitor implements Runnable {

    private String            state2transition;
    private CodaRCAgent       owner;
    private ArrayList<String>
            stateExpectedResponses;

    /**
     * Constructor
     * @param owner reference to the agent
     * @param sn State name of a state that
     *           this agent must transition.
     */
    public StateTransitioningMonitor(CodaRCAgent owner,
                                     String sn){
        this .owner      = owner;
        state2transition = sn;
        stateExpectedResponses =
                owner.getStateRequiredClientResponse(sn);
        if(stateExpectedResponses.isEmpty() && !sn.equals(AConstants.configured)){
            owner.reportAlarmMsg(owner.me.getSession() +
                            "/"+owner.me.getRunType(),owner.myName,
                    9,
                    AConstants.ERROR,
                    " State name for the state = " +
                            sn +
                            " is not described in the COOL");
            owner.dalogMsg(owner.myName,
                    9,
                    AConstants.ERROR,
                    " State name for the state = " +
                            sn +
                            " is not described in the COOL");
        }
    }

    @Override
    public void run() {

        long startTime = AfecsTool.getCurrentTimeInMs();

       // Main loop of the monitor
        do{
            AfecsTool.sleep(100);
         } while(!owner.me.getState().equalsIgnoreCase(AConstants.failed) &&
                !owner.me.getState().equals(AConstants.disconnected) &&
                !stateExpectedResponses.contains(owner.me.getState()) &&
                !owner.isResetting.get());


        if(!owner.isResetting.get()){

            // State transition failed
            if(owner.me.getState().equalsIgnoreCase(AConstants.failed)){
                owner.reportAlarmMsg(owner.me.getSession()+
                                "/"+owner.me.getRunType(),
                        owner.myName,
                        9,
                        AConstants.ERROR,
                        state2transition +
                                " transition failed. One or more components are in \"error\" state");
                owner.dalogMsg(owner.myName,
                        9,
                        AConstants.ERROR,
                        state2transition +
                                " transition failed. One or more components are in \"error\" state");

                // Client is disconnected
            } else if(owner.me.getState().equals(AConstants.disconnected)){
                owner.reportAlarmMsg(owner.me.getSession()+
                                "/"+owner.me.getRunType(),owner.myName,
                        9,
                        AConstants.ERROR,
                        " Lost connection to the client.");
                owner.dalogMsg(owner.myName,
                        9,
                        AConstants.ERROR,
                        " Lost connection to the client.");

                // Client is reporting. Check reported
                // responses against the required state name.
            } else {

                // if the required stat to transition is "active"
                // reset counter to calculate running averages.
                if(owner.me.getState().equals(AConstants.active)){
                    owner.averageCount = 0;
                    owner.eventRateSlide.clear();
                    owner.dataRateSlide.clear();

                    owner.startTime = System.currentTimeMillis();
                }
                // Farm control client state transition.
                // Uses local reference to the FCS client.
                if(owner.me.getType().equals(ACodaType.FCS.name())) {
                    switch (state2transition) {
                        case AConstants.configured:
                            owner.getFcsEngine().configure();
                            break;
                        case AConstants.downloaded:
                            owner.getFcsEngine().download();
                            break;
                        case AConstants.prestarted:
                            owner.getFcsEngine().prestart();
                            break;
                        case AConstants.active:
                            owner.getFcsEngine().go();
                            break;
                        case AConstants.ended:
                            owner.getFcsEngine().end();
                            break;
                    }
                }

            }

            long endTime = AfecsTool.getCurrentTimeInMs();
            System.out.println("DDD -----| Info: " + owner.me.getName() + " transitioned to " +
                    state2transition + " in " + (endTime - startTime) + " msec.");

        }
    }
}


