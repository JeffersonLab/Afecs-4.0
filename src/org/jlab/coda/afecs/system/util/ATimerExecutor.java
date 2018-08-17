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

package org.jlab.coda.afecs.system.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Periodically executes an abstract method
 * <p>
 *
 * @author gurjyan
 *         Date: 11/5/15 Time: 8:59 AM
 * @version 3.x
 */
public abstract class ATimerExecutor extends TimerTask {

    private Timer timer;
    private int period;

    public ATimerExecutor(int period){

        this.period = period;

        // running timer task as daemon thread
        timer = new Timer(true);
//        System.out.println("TimerTask begins! :" + new Date());
    }

    abstract public void doSomeWork();

    public void start(){
        timer.scheduleAtFixedRate(this, 0, period);
    }

    public void stop(){
        timer.cancel();
    }

    @Override
    public void run() {
        doSomeWork();
    }

}
