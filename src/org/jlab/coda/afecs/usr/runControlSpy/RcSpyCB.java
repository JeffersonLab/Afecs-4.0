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

package org.jlab.coda.afecs.usr.runControlSpy;

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgCallbackAdapter;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describe...
 *
 * @author gurjyan
 *         Date: 7/30/14 Time: 10:45 AM
 * @version 2
 */
public class RcSpyCB extends cMsgCallbackAdapter {
    String agentsSRType, supervisorSRType;



    public RcSpyCB(String session, String runType) {
        agentsSRType        = session+"_"+runType+"/agents";
        supervisorSRType    = session+"_"+runType+"/supervisor";
    }

    /************************************************************************
     * Private inner class for responding messages from supervisor agent of the specific control system.
     */
    public void callback(cMsgMessage msg, Object userObject) {
        if (msg != null) {

            String type = msg.getType();
            if (type != null) {
                // check if the message is coming from component agents
                if (type.equals(agentsSRType)) {
                    Map<String, AComponent> cmpU = null;
                    try {
                        if (msg.getByteArray() != null) {
                            cmpU = (Map<String, AComponent>) AfecsTool.B2O(msg.getByteArray());
                        }
                    } catch (IOException e) {
                        if (AConstants.debug.get()) e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        if (AConstants.debug.get()) e.printStackTrace();
                    }
                    // sort according to their types
                    LinkedHashMap<String, AComponent> cmp = AfecsTool.getSortedByType(cmpU);
                    if (cmp != null) {
                        for (AComponent c : cmp.values()) System.out.println(c);
                    }
                    // check if the message author is the supervisor agent
                } else if (type.equals(supervisorSRType)) {

                    try {
                        System.out.println("\n Supervisor_Data");
                        System.out.println("\n-----------------");
                        // supervisor reports its state
                        if (msg.getPayloadItem(AConstants.STATE) != null) {
                            String _runState = msg.getPayloadItem(AConstants.STATE).getString();
                            System.out.println("state                    = " + _runState);
                            // run number
                            if (msg.getPayloadItem(AConstants.RUNNUMBER) != null && msg.getPayloadItem(AConstants.RUNNUMBER).getInt() > 0) {
                                int _runNumber = msg.getPayloadItem(AConstants.RUNNUMBER).getInt();
                                System.out.println("run_number               = " + _runNumber);
                            }
                            // get supervisor run start and times, event and time limits, as well as autoStart
                            if (msg.getPayloadItem(AConstants.RUNSTARTTIME) != null) {
                                String _runStartTime = msg.getPayloadItem(AConstants.RUNSTARTTIME).getString();
                                System.out.println("run_start_time           = " + _runStartTime);
                            }
                            if (msg.getPayloadItem(AConstants.RUNENDTIME) != null) {
                                String _runEndTime = msg.getPayloadItem(AConstants.RUNENDTIME).getString();
                                System.out.println("run_end_time             = " + _runEndTime);
                            }
                            if (msg.getPayloadItem(AConstants.AUTOSTART) != null) {
                                String _autoStart = msg.getPayloadItem(AConstants.AUTOSTART).getString();
                                System.out.println("auto-start               = " + _autoStart);
                            }
                            if (msg.getPayloadItem(AConstants.EVENTLIMIT) != null) {
                                int _eventLimit = msg.getPayloadItem(AConstants.EVENTLIMIT).getInt();
                                System.out.println("event_limit              = " + _eventLimit);
                            }
                            if (msg.getPayloadItem(AConstants.TIMELIMIT) != null) {
                                long _timeLimit = msg.getPayloadItem(AConstants.TIMELIMIT).getLong();
                                System.out.println("time_limit               = " + _timeLimit);
                            }
                            if (msg.getPayloadItem(AConstants.SCHEDULEDRUNS) != null) {
                                int _nScheduledRunsRemaining = msg.getPayloadItem(AConstants.SCHEDULEDRUNS).getInt() - 1;
                                System.out.println("scheduled_runs_remaining = " + _nScheduledRunsRemaining);
                            }
                        }
                    } catch (cMsgException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
