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

package org.jlab.coda.afecs.usr.rcapi;

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgNetworkConstants;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.io.IOException;
import java.util.*;

public class RcApi extends ABase {


    public RcApi(){
        super();
    }

    public boolean  pl_connect(String eXPid){
        // disconnect from the platform
        if(!platformDisconnect()){
            System.out.println("Error connecting to the platform = "+myPlatformConnection.getUDL());
            return false;
        }
        //sleep for a sec
        AfecsTool.sleep(1000);
        // define a new name
        myName = "Rcg-"+new Random().nextInt(100);
        // define the udl
        String udl = "cMsg://multicast:"+ cMsgNetworkConstants.nameServerUdpPort+"/cMsg/"+eXPid+"?cmsgpassword="+getPlEXPID();
        // connect to the platform
        try {
            myPlatformConnection = platformConnect(udl);
        } catch (cMsgException e) {
            e.printStackTrace();
        }
        if(!isPlatformConnected()){
            System.out.println("Error connecting to the platform = "+udl);
            return false;
        }
        return true;
    }

    public boolean  pl_connect(String pLHost, String eXPid){
        // disconnect from the platform
        if(!platformDisconnect()){
            System.out.println("Error while connecting to the platform = "+myPlatformConnection.getUDL());
            return false;
        }
        //sleep for a sec
        AfecsTool.sleep(1000);
        // define a new name
        myName = "Rcg-"+new Random().nextInt(100);
        // define the udl
        String udl = "cMsg://"+pLHost+":"+cMsgNetworkConstants.nameServerTcpPort+"/cMsg/"+eXPid+"?cmsgpassword="+getPlEXPID();
        // connect to the platform
        try {
            myPlatformConnection = platformConnect(udl);
        } catch (cMsgException e) {
            e.printStackTrace();
        }
        if(!isPlatformConnected()){
            System.out.println("Error connecting to the platform = "+udl);
            return false;
        }
        return true;
    }

    public boolean  pl_disconnect(){
        if(!platformDisconnect()){
            System.out.println("Error connecting to the platform "+myPlatformConnection.getUDL());
            return false;
        } else {
            return true;
        }
    }

    public String[]  getPlatformRegisteredRunTypes(){
        String [] sa;
        ArrayList<String> l = new ArrayList<String>();
        cMsgMessage msg = null;
        try {
            msg = p2pSend(AConstants.CONTROLDESIGNER,AConstants.DesignerInfoRequestGetConfigs,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("configFileNames")!=null){
                    sa = msg.getPayloadItem("configFileNames").getStringArray();
                    if(sa.length>0){
                        Collections.addAll(l, sa);
                        return l.toArray(new String[l.size()]);
                    }
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public String[] getPlatformRegisteredSessions(){
        String [] sa;
        cMsgMessage msg = null;
        try {
            msg = p2pSend(getPlEXPID(), AConstants.PlatformInfoRequestSessions_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("sessions_p")!=null){
                    sa = msg.getPayloadItem("sessions_p").getStringArray();
                    if(sa.length>0){
                        return sa;
                    }
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public String[] getPlatformRegisteredAgents(){
        String [] sa;
        cMsgMessage msg = null;
        try {
            msg = p2pSend(getPlEXPID(), AConstants.PlatformInfoRequestAgents_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("agents_p")!=null){
                    sa = msg.getPayloadItem("agents_p").getStringArray();
                    if(sa.length>0){
                        return sa;
                    }
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println(" communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public String[] getPlatformRegisteredDocs(){
        String [] sa;
        cMsgMessage msg = null;
        try {
            msg = p2pSend(getPlEXPID(), AConstants.PlatformInfoRequestDocs_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("docs_p")!=null){
                    sa = msg.getPayloadItem("docs_p").getStringArray();
                    if(sa.length>0){
                        return sa;
                    }
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println(" communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public int      getSupervisorRunNumber(String runType){
        int rn = -1;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportRunNumber_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("runnumber_p")!=null){
                    rn = msg.getPayloadItem("runnumber_p").getInt();
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return rn;
    }

    public String   getSupervisorState(String runType){
        String s;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportRunState_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("runstate_p")!=null){
                    s = msg.getPayloadItem("runstate_p").getString();
                    return s;
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public String[] getSupervisorComponentsStates(String runType){
        String [] sa;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportComponentStates_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("states_p")!=null){
                    sa = msg.getPayloadItem("states_p").getStringArray();
                    if(sa.length>0){
                        return sa;
                    }
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public String   getControlStatus(String runType){
        String s;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportSchedulerStatus_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("scedulerstatus_p")!=null){
                    s = msg.getPayloadItem("scedulerstatus_p").getString();
                    return s;
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public String   getSupervisorSession(String runType){
        String s;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportSession_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("supsession_p")!=null){
                    s = msg.getPayloadItem("supsession_p").getString();
                    return s;
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public String   getSupervisorRunStartTime(String runType){
        String s;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportRunStartTime_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("supstarttime_p")!=null){
                    s = msg.getPayloadItem("supstarttime_p").getString();
                    return s;
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public String   getSupervisorRunEndTime(String runType){
        String s;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportRunEndTime_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("supendtime_p")!=null){
                    s = msg.getPayloadItem("supendtime_p").getString();
                    return s;
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public String   getActiveRunType(String session){
        String s;
        cMsgMessage msg = null;
        try {
            msg = p2pSend(getPlEXPID(), AConstants.PlatformInfoRequestActiveRunType,session,AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("activeruntype_p")!=null){
                    s = msg.getPayloadItem("activeruntype_p").getString();
                    return s;
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public List<AComponent> getActiveRunTypes(){
        List<AComponent> l = null;
        cMsgMessage msg = null;
        try {
            msg = p2pSend(getPlEXPID(), AConstants.PlatformInfoRequestActiveRunTypes,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msg!=null && msg.getByteArray()!=null){
                try {
                    l = (ArrayList<AComponent>)AfecsTool.B2O(msg.getByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        return l;
    }

    public int      getComponentEventNumber(String runType, String compName){
        int rn = -1;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportComponentEventNumber_p,compName,AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("evtnumber_p")!=null){
                    rn = msg.getPayloadItem("evtnumber_p").getInt();
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return rn;
    }

    public String   getComponentOutputFile(String runType, String compName){
        String s;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportComponentOutputFile_p,compName,AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("outputfile_p")!=null){
                    s = msg.getPayloadItem("outputfile_p").getString();
                    return s;
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public float    getComponentEventRate(String runType, String compName){
        float r = -1;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportComponentEventRate_p,compName,AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("compevtrate_p")!=null){
                    r = msg.getPayloadItem("compevtrate_p").getFloat();
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return r;
    }

    public double   getComponentDataRate(String runType, String compName){
        double r = -1;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportComponentDataRate_p,compName,AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("compdatarate_p")!=null){
                    r = msg.getPayloadItem("compdatarate_p").getFloat();
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return r;
    }

    public String   getComponentState(String runType, String compName){
        String s;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportComponentState_p,compName,AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        try {
            if(msg!=null){
                if(msg.getPayloadItem("compstate_p")!=null){
                    s = msg.getPayloadItem("compstate_p").getString();
                    return s;
                } else {
                    System.out.println("missing payload");
                }
            } else {
                System.out.println("communication error");
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) System.out.println(e.getMessage());
        }
        return null;
    }

    public Map<String,String> getRtvs(String runType){
        Map<String,String> rtvs = null;
        cMsgMessage msg = null;
        try {
            msg = p2pSend("sms_"+runType, AConstants.SupervisorReportRtvs_p,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msg!=null && msg.getByteArray()!=null){
                try {
                    rtvs = (Map<String,String>)AfecsTool.B2O(msg.getByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        return rtvs;
    }

    /**
     * added 12.9.16
     * @param session controls session
     * @param runType controls name
     */
    public void  rcgConfigure(String  session, String  runType){
       if(isPlatformConnected()){

           // Ask ControlDesigner to configure.
           ArrayList<cMsgPayloadItem> al = new ArrayList<>();
           try {
               al.add(new cMsgPayloadItem(AConstants.SESSION,session));
               al.add(new cMsgPayloadItem(AConstants.RUNTYPE,runType));
           } catch (cMsgException e1) {
               e1.printStackTrace();
           }

           try {
               p2pSend(AConstants.CONTROLDESIGNER,
                       AConstants.DesignerControlRequestConfigureControl_RCAPI,
                       "",
                       al,
                       AConstants.TIMEOUT);

           } catch (AException e) {
               e.printStackTrace();  //Exception handling
           }
       }
    }

    public void     rcgDownload(String  runType){
        String st = getSupervisorState(runType);
        if(st.equals(AConstants.downloaded) || st.equals(AConstants.configured)){
            // Ask ControlDesigner to configure.
            ArrayList<cMsgPayloadItem> al = new ArrayList<cMsgPayloadItem>();
            if(!send("sms_"+runType, AConstants.SupervisorControlRequestStartService,"CodaRcDownload")){
                System.out.println("Error: Command failed.");
                return;
            }
            if(!waitUntilItsDone(runType,AConstants.downloaded,AConstants.TIMEOUT)){
                System.out.println("Error: Transition failed.");
            }
        } else {
            System.out.println("Forbidden transition.");
        }
    }

    public void     rcgPrestart(String  runType){
        String st = getSupervisorState(runType);
        if(st.equals(AConstants.downloaded) || st.equals(AConstants.ended)){
            // Ask ControlDesigner to configure.
            ArrayList<cMsgPayloadItem> al = new ArrayList<cMsgPayloadItem>();
            if(!send("sms_"+runType, AConstants.SupervisorControlRequestStartService,"CodaRcPrestart")){
                System.out.println("Error: Command failed.");
                return;
            }
            if(!waitUntilItsDone(runType,AConstants.prestarted,AConstants.TIMEOUT)){
                System.out.println("Error: Transition failed.");
            }
        } else {
            System.out.println("Forbidden transition.");
        }
    }

    public void     rcgGo(String  runType){
        String st = getSupervisorState(runType);
        if(st.equals(AConstants.prestarted)){
            // Ask ControlDesigner to configure.
            ArrayList<cMsgPayloadItem> al = new ArrayList<cMsgPayloadItem>();
            if(!send("sms_"+runType, AConstants.SupervisorControlRequestStartService,"CodaRcGo")){
                System.out.println("Error: Command failed.");
                return;
            }
            if(!waitUntilItsDone(runType,AConstants.active,AConstants.TIMEOUT)){
                System.out.println("Error: Transition failed.");
            }
        } else {
            System.out.println("Forbidden transition.");
        }
    }

    public void     rcgEnd(String  runType){
        String st = getSupervisorState(runType);
        if(st.equals(AConstants.downloaded) || st.equals(AConstants.prestarted) || st.equals(AConstants.active)){
            // Ask ControlDesigner to configure.
            ArrayList<cMsgPayloadItem> al = new ArrayList<cMsgPayloadItem>();
            if(!send("sms_"+runType, AConstants.SupervisorControlRequestStartService,"CodaRcEnd")){
                System.out.println("Error: Command failed.");
                return;
            }
            if(!waitUntilItsDone(runType,AConstants.ended,AConstants.TIMEOUT)){
                System.out.println("Error: Transition failed.");
            }
        } else {
            System.out.println("Forbidden transition.");
        }
    }

    public void     rcgStartRun(String  runType){
        boolean a,b;
        a = send("sms_" + runType.trim(), AConstants.SupervisorControlRequestEnableAutoMode,0);
        b = send("sms_"+runType, AConstants.SupervisorControlRequestStartService,"CodaRcStartRun");
        if(a && b){
            if(!waitUntilItsDone(runType,AConstants.active,AConstants.TIMEOUT)){
                System.out.println("Error: Transition failed.");
            }
        } else {
            System.out.println("Error: Command failed.");
        }
    }

    public void     rcgReset(String  runType){
        if(!send("sms_"+runType, AConstants.AgentControlRequestMoveToState, AConstants.reseted)){
            System.out.println("Error: Command failed.");
        }
    }

    public void     rcgProgramScheduler(String  runType, int numberOfRuns, int eventLimit, int timeLimit){
        boolean a,b,c;
        a = send("sms_"+runType.trim(), AConstants.SupervisorControlRequestSetNumberOfRuns,Integer.toString(numberOfRuns));
        b = send("sms_"+runType.trim(), AConstants.SupervisorControlRequestSetEventLimit, Integer.toString(eventLimit));
        c = send("sms_"+runType.trim(), AConstants.SupervisorControlRequestSetTimeLimit, Integer.toString(timeLimit));
        if(a && b && c) {
        } else {
            System.out.println("Error: Command failed.");
        }
    }

    public void     rcgResetScheduler(String  runType){
        boolean a,b,c;
        a = send("sms_"+runType.trim(), AConstants.SupervisorControlRequestSetNumberOfRuns,"0");
        b = send("sms_"+runType.trim(), AConstants.SupervisorControlRequestSetEventLimit, "0");
        c = send("sms_"+runType.trim(), AConstants.SupervisorControlRequestSetTimeLimit, "0");
        if(a && b && c) {
        } else {
            System.out.println("Error: Command failed.");
        }
    }

    public void     rcgDisableScheduler(String  runType){
        if(!send("sms_" + runType.trim(), AConstants.SupervisorControlRequestDisableAutoMode, 0)){
            System.out.println("Error: Command failed.");
        }
    }

    public void rcgReleaseAgents(String  runType){
        if(!send("sms_"+runType, AConstants.SupervisorControlRequestReleaseAgents,"")){
            System.out.println("Error: Command failed.");
        }
    }

    /**
     *
     * @param runType
     * @param response
     * @param tout  in milli seconds
     * @return
     */
    private boolean waitUntilItsDone(String  runType, String  response, int tout){

       tout = tout/1000;

        for(int i=0;i<tout;i++){
            // ask supervisor it's state in sync
            cMsgMessage msgb = null;
            try {
                msgb = p2pSend("sms_" + runType.trim(), AConstants.SupervisorReportRunState, "", 1000);
            } catch (AException e) {
                System.out.println("Error: "+e.getMessage());

            }
            if(msgb!=null && msgb.getText().equals(response)){
                return true;
            }
            AfecsTool.sleep(1000);
        }
        return false;
    }

}
