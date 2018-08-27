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

package org.jlab.coda.afecs.system.thread;

import org.jlab.coda.afecs.agent.AParent;
import org.jlab.coda.afecs.system.AConstants;

import org.jlab.coda.afecs.system.util.AfecsTool;

/**
 *  <p>
 *     Sends status (payload of AComponent object) to every
 *     one subscribing to the subject = session. N.B. This
 *     is a design choice. All supervisor/s must subscribe
 *     the subject session. This is done in order to simplify
 *     the life of any graphical interface that is ready to
 *     visualize information of the specific agent.
 *  </p>
 *
 * @author gurjyan
 *         Date: 11/12/13 Time: 1:51 PM
 * @version 4.x
 */

public class ReportStatus implements Runnable {

    public boolean isRunning = false;
    private AParent owner;
    private volatile boolean isPeriodic;

    /**
     * Constructor
     */
    public ReportStatus(AParent owner, boolean isPeriodic){
        this.owner = owner;
        this.isPeriodic = isPeriodic;
        if(isPeriodic) isRunning = true;
    }

    /**
     * <p>
     *     Actual method that sends the data
     * </p>
     * @param isObjectRequested if true we need to send entire AComponent
     *                          object for this agent to the requester.
     *                          Otherwise we send only subset of the AComponent
     *                          object including only run time specific parameters
     *                          as a payload items.
     * @return status of the operation.
     */
    private boolean reportStatus(boolean isObjectRequested){
        boolean stat = false;
        // broadcast status messages to entire subject = session
        if(owner.me!=null &&
                owner.me.getSession()!=null &&
                owner.me.getRunType()!=null &&
                !owner.me.getSession().equals(AConstants.udf) &&
                !owner.me.getType().equals(AConstants.udf)){
            if(isObjectRequested){
                stat = owner.send(owner.me.getSession(),
                        owner.me.getRunType(),
                        owner.me);
            } else {
                stat = owner.send(owner.me.getSession(),
                        owner.me.getRunType(),
                        owner.me.getRunTimeDataAsPayload());
            }
        }
        return stat;
    }

    public void run() {
        if(!isPeriodic){
            reportStatus(true);
        } else {
            while(isRunning){
                reportStatus(true);
                try {
                    Thread.sleep(owner.me.getReportingInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
