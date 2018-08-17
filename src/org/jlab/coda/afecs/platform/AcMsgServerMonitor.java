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

import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.util.ALogger;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgConstants;
import org.jlab.coda.cMsg.cMsgDomain.server.cMsgNameServer;

public class AcMsgServerMonitor extends ABase {
    
    int interval;
    boolean go;
    // local instance of the logger object
    private ALogger lg = ALogger.getInstance();


    private void start(){
        
        while(go){
          if(pingServer()<=0){
              // daLog report about the severe error
              
              // restart cMsg server
              restartServer();
              AfecsTool.sleep(interval);
          }
            AfecsTool.sleep(interval);
        }
    }
    
    
    private boolean restartServer(){
        cMsgNameServer platformServer;
        platformServer = new cMsgNameServer(myConfig.getPlatformTcpPort(),
                myConfig.getPlatformTcpDomainPort(),
                myConfig.getPlatformUdpPort(),
                false,
                false,
                myConfig.getPlatformExpid(),
                null,
                null,
                cMsgConstants.debugNone,
                10);
        platformServer.setName(myConfig.getPlatformName());
        platformServer.startServer();
        return platformServer.isAlive();
    }
    
    public void stopMonitor(){
        go = false;
    }
}
