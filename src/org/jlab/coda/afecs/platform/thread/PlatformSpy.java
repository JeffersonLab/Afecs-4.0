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

package org.jlab.coda.afecs.platform.thread;

import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgUtilities;
import org.jlab.coda.cMsg.common.cMsgServerFinder;

/**
 * <p>
 * Thread that sleeps for 10 sec and checks to see if
 * there is another platform running with the same EXPID
 *
 * </p>
 *
 * @author gurjyan
 *         Date: 4/27/15 Time: 11:49 AM
 * @version 3.x
 */
public class PlatformSpy extends ABase implements Runnable {
    cMsgServerFinder finder;

    public PlatformSpy(){
        finder = new cMsgServerFinder();
    }

    @Override
    public void run() {
        myName = "spy";
        while(true){
            finder.findRcServers();
            cMsgMessage[] msgs = finder.getRcServers();
            if(msgs!=null){
                try {
                    for(cMsgMessage msg: msgs){
                        if(msg.getPayloadItem("host")!=null && msg.getPayloadItem("expid")!=null){
                            String h;
                            h = msg.getPayloadItem("host").getString();
                            String e = msg.getPayloadItem("expid").getString();
                            if(!cMsgUtilities.isHostLocal(h) && e.equals(getPlEXPID()) ) {
                                System.out.println("Warning! Detected conflicting platform " +
                                        "running on the host = "+ h +" witth EXPID = "+e);
                            }
                        }
                    }
                } catch (cMsgException e) {
                    e.printStackTrace();
                }
            }
            AfecsTool.sleep(10000);
        }
    }
}

