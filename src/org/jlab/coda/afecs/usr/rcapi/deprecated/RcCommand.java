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

package org.jlab.coda.afecs.usr.rcapi.deprecated;

import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.util.ArrayList;
import java.util.Random;

public class RcCommand extends ABase {


    public RcCommand(){
        super();
        myName = "Rcg-"+new Random().nextInt(100);
        // connect to the platform
        try {
            myPlatformConnection = platformConnect(getPlUDL());
        } catch (cMsgException e) {
            e.printStackTrace();
        }
        if(!isPlatformConnected()){
            System.out.println("Error connecting to the platform = "+getPlUDL());
            System.exit(0);
        }
    }

    public static void main(String[] args){
        String runType = AConstants.udf;
        String session;
        boolean f = false;
        RcCommand cmd = new RcCommand();
        if(args.length==0){
            cmd.printSynopsis();
            System.exit(0);
        }
        if(args.length==1){
            if(args[0].startsWith("-h"))cmd.printSynopsis();
        }  else if(args.length==3){
            if(args[0].equals("-rt")){
                runType = args[1];
                if(args[2].startsWith("-down")){
                    f = true;
                    cmd.rcgDownload(runType);
                }
                else if(args[2].startsWith("-p")){
                    f = true;
                    cmd.rcgPrestart(runType);
                }
                else if(args[2].startsWith("-g")){
                    f = true;
                    cmd.rcgGo(runType);
                }
                else if(args[2].startsWith("-e")){
                    f = true;
                    cmd.rcgEnd(runType);
                }
                else if(args[2].startsWith("-sta")){
                    f = true;
                    cmd.rcgStartRun(runType);
                }
                else if(args[2].equalsIgnoreCase("-reset")){
                    f = true;
                    cmd.rcgReset(runType);
                }
                else if(args[2].equalsIgnoreCase("-reset-scheduler")){
                    f = true;
                    cmd.rcgResetScheduler(runType);
                }
                else if(args[2].equalsIgnoreCase("-disable-scheduler")){
                    f = true;
                    cmd.rcgDisableScheduler(runType);
                }
                else if(args[2].equalsIgnoreCase("-release-components")){
                    f = true;
                    cmd.rcgReleaseAgents(runType);
                }
            }
        }
        else if(args.length==5 && args[0].equals("-s")){
            session = args[1];
            if(args[2].equals("-rt")){
                runType = args[3];
            }
            if(args[4].equals("-configure")&& !session.equals(AConstants.udf) && !runType.equals(AConstants.udf)){
                f = true;
                cmd.rcgConfigure(session, runType);
            }
        }
        else if(args.length==6 && args[0].equals("-rt") ){
            runType = args[1];
            if(args[2].equals("-schedule")){
                f = true;
                cmd.rcgProgramScheduler(runType, args[3], args[4], args[5]);
            }
        }

        if (!f){
            System.out.println("Unknown command.");
        }
    }

    private String getSupervisorState(String runtype){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_" + runtype.trim(), AConstants.SupervisorReportRunState, "", AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: "+ e.getMessage());
        }
        if(msgb!=null){
            return msgb.getText();
        }
        return AConstants.udf;
    }

    /**
     * Configure transition
     * @param session of the run
     * @param runType of the run
     */
    public void rcgConfigure(String session, String runType){
        String st = getSupervisorState(runType);
        if(st.equals(AConstants.udf) || st.equals(AConstants.booted) || st.equals(AConstants.configured)){
            // Ask ControlDesigner to configure.
            ArrayList<cMsgPayloadItem> al = new ArrayList<cMsgPayloadItem>();
            try {
                al.add(new cMsgPayloadItem(AConstants.SESSION,session));
                al.add(new cMsgPayloadItem(AConstants.RUNTYPE,runType));
            } catch (cMsgException e1) {
                if(AConstants.debug.get()) e1.printStackTrace();
            }
            send(AConstants.CONTROLDESIGNER, AConstants.DesignerControlRequestConfigureControl,al);
            waitUntilItsDone(runType,AConstants.configured,AConstants.TIMEOUT);
        } else {
            System.out.println("Forbidden transition.");
        }
    }

     public void rcgReleaseAgents(String runType){
         send("sms_"+runType, AConstants.SupervisorControlRequestReleaseAgents,"");
     }

    /**
     * Download transition
     * @param runType configuration name
     */
    public void rcgDownload(String runType){
        String st = getSupervisorState(runType);
        if(st.equals(AConstants.downloaded) || st.equals(AConstants.configured)){
            send("sms_"+runType, AConstants.SupervisorControlRequestStartService,"CodaRcDownload");
            waitUntilItsDone(runType, AConstants.downloaded, AConstants.TIMEOUT);
        } else {
            System.out.println("Forbidden transition.");
        }
    }

    /**
     * Prestart transition
     * @param runType configuration name
     */
    public void rcgPrestart(String runType){
        String st = getSupervisorState(runType);
        if(st.equals(AConstants.downloaded) || st.equals(AConstants.ended)){
            send("sms_"+runType, AConstants.SupervisorControlRequestStartService,"CodaRcPrestart");
            waitUntilItsDone(runType, AConstants.prestarted, AConstants.TIMEOUT);
        } else {
            System.out.println("Forbidden transition.");
        }
    }

    /**
     * Go transition
     * @param runType configuration name
     */
    public void rcgGo(String runType){
        String st = getSupervisorState(runType);
        if(st.equals(AConstants.prestarted)){
            send("sms_"+runType, AConstants.SupervisorControlRequestStartService,"CodaRcGo");
            waitUntilItsDone(runType, AConstants.active, AConstants.TIMEOUT);
        } else {
            System.out.println("Forbidden transition.");
        }
    }

    /**
     * End transition
     * @param runType configuration name
     */
    public void rcgEnd(String runType){
        String st = getSupervisorState(runType);
        if(st.equals(AConstants.downloaded) || st.equals(AConstants.prestarted) || st.equals(AConstants.active)){
            send("sms_"+runType, AConstants.SupervisorControlRequestStartService,"CodaRcEnd");
            waitUntilItsDone(runType, AConstants.ended, AConstants.TIMEOUT);
        } else {
            System.out.println("Forbidden transition.");
        }
    }

    /**
     * StartRun transition
     * @param runType configuration name
     */
    public void rcgStartRun(String runType){
        send("sms_" + runType.trim(), AConstants.SupervisorControlRequestEnableAutoMode,0);
        send("sms_"+runType, AConstants.SupervisorControlRequestStartService,"CodaRcStartRun");
        waitUntilItsDone(runType, AConstants.active, AConstants.TIMEOUT);
    }

    /**
     * Reset transition
     * @param runType configuration name
     */
    public void rcgReset(String runType){
        send("sms_"+runType, AConstants.AgentControlRequestMoveToState, AConstants.reseted);
    }

    /**
     * Program scheduler
     * @param numberOfRuns of runs
     * @param eventLimit event limit
     * @param timeLimit time limit in minutes
     * @param runType configuration name
     */
    public void rcgProgramScheduler(String runType, String numberOfRuns, String eventLimit, String timeLimit){
        send("sms_"+runType.trim(), AConstants.SupervisorControlRequestSetNumberOfRuns,numberOfRuns.trim());
        send("sms_"+runType.trim(), AConstants.SupervisorControlRequestSetEventLimit, eventLimit.trim());
        send("sms_"+runType.trim(), AConstants.SupervisorControlRequestSetTimeLimit, timeLimit.trim());
    }

    /**
     * Reset time limit
     * @param runType configuration name
     */
    public void rcgResetScheduler(String runType){
        send("sms_"+runType.trim(), AConstants.SupervisorControlRequestSetNumberOfRuns,"0");
        send("sms_"+runType.trim(), AConstants.SupervisorControlRequestResetEventLimit, "0");
        send("sms_"+runType.trim(), AConstants.SupervisorControlRequestResetTimeLimit,"0");
    }

    public void rcgDisableScheduler(String runType){
        send("sms_" + runType.trim(), AConstants.SupervisorControlRequestDisableAutoMode, 0);
    }

    private boolean waitUntilItsDone(String runType, String response, int timeout){
        for(int i=0;i<timeout;i++){
            // ask supervisor it's state in sync
            cMsgMessage msgb = null;
            try {
                msgb = p2pSend("sms_" + runType.trim(), AConstants.SupervisorReportRunState, "", AConstants.TIMEOUT);
            } catch (AException e) {
                System.out.println("Error: "+e.getMessage());
            }
            if(msgb!=null && msgb.getText().equals(response)){
                return true;
            }
            AfecsTool.sleep(1000);
        }
        System.out.println("Transition failed");
        return false;
    }

    public void printSynopsis(){
        System.out.println("Synopsis: plcmd -[option] <value>" +
                "\n-help" +
                "\n-s  <session> -rt <runType> -configure"+
                "\n-rt <runType> -download"+
                "\n-rt <runType> -prestart"+
                "\n-rt <runType> -go"+
                "\n-rt <runType> -end"+
                "\n-rt <runType> -startrun"+
                "\n-rt <runType> -reset"+
                "\n-rt <runType> -reset-scheduler" +
                "\n-rt <runType> -disable-scheduler" +
                "\n-rt <runType> -schedule <number of runs> <event limit> <time limit>"+
                "\n-rt <runType> -release-components"+
                "\n");
    }
}


