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

package org.jlab.coda.afecs.system.process;

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.cool.ontology.AProcess;
import org.jlab.coda.afecs.cool.ontology.AScript;
import org.jlab.coda.afecs.plugin.IAClientCommunication;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.AfecsTool;

/**
 * <p>
 *     Thread that runs COOL described periodic processes.
 *     Periodic processes can be infinite or limited.
 *     Note that currently only shell scripts as a processes are supported
 * </p>
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */

public class PeriodicProcess extends Thread{

    private AProcess process;
    private AComponent component;
    private ProcessManager pm;

    private volatile boolean isRunning = true;

    /**
     * <p>
     *     Constructor.
     *     After constructing the process object is added to
     *     the Agent process manager map of periodic processes.
     * </p>
     * @param pr AProcess object containing the
     *           details of the described process
     * @param plugin Plugin object reference,
     *               implementing Afecs
     *               IAClientCommunication interface.
     * @param comp Reference to the AComponent object itself.
     * @param pm Reference to the Afecs process manager object
     */
    public PeriodicProcess(AProcess pr,
                           IAClientCommunication plugin,
                           AComponent comp,
                           ProcessManager pm){
        this.pm = pm;
        this.process = pr;
        this.component = comp;
        if(process.getScripts()!=null  && !process.getScripts().isEmpty()) {
            for(AScript sc:process.getScripts()) {
                pm.getParent().reportAlarmMsg(comp.getSession() + "/" + comp.getRunType(), comp.getName(), 4,
                        AConstants.INFO, "Periodic script (" + sc.getCommandString() + ")");
            }
        }

    }

    void prStop(){
        isRunning = false;
    }

    @Override
    /**
     * <p>
     *     Negative integer for the periodicity means
     *     execution of a process until thread is not killed.
     *     Yet, positive integer indicates how many times
     *     required process must be executed.
     *     Delay between process executions is defined
     *     in the process object itself.
     * </p>
     */
    public void run() {
        super.run();
        if(process.getPeriodicity()<0){
            while(isRunning){
                if(process.getScripts()!=null  && !process.getScripts().isEmpty()) {
                    pm._execShellScripts(process.getScripts(), component, true);
                }
                AfecsTool.sleep(process.getDelay()*1000);
            }
        } else if(process.getPeriodicity()>0){
            for(int i=0;i< process.getPeriodicity();i++){
                if(isRunning) {
                    if (process.getScripts() != null && !process.getScripts().isEmpty()) {
                        pm._execShellScripts(process.getScripts(), component, true);
                    }
                    AfecsTool.sleep(process.getDelay() * 1000);
                }
            }
        }
    }
}
