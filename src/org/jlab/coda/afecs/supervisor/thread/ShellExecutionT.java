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

import org.jlab.coda.afecs.system.AException;

import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.afecs.system.util.StdOutput;

import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * <p>
 *     Thread to run shell commands.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/13/14 Time: 2:51 PM
 * @version 4.x
 */
public class ShellExecutionT extends Thread {
    private String command;
    private int exitCode;
    private StdOutput proc;
    public boolean done = false;

    public void run(){
        ArrayList<String> l = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(command.trim());
        while(st.hasMoreTokens()){
            l.add(st.nextToken());
        }
        try {
            proc = AfecsTool.fork(l, true);
        } catch (AException e) {
            e.printStackTrace();
        }
        if(proc!=null){
            exitCode = proc.getExitValue();
        }
        done = true;
    }


    public int getExitCode() {
        return exitCode;
    }
}
