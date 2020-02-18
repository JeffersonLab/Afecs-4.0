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

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.platform.ASessionInfo;
import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AskPlatform extends ABase{


    public AskPlatform(){
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

    /**
     * Does cMsg send and get to the ControlDesigner agent and gets the names of all configuration
     * files in the COOL_HOME/config directory.
     */
    public void getPlatformRegisteredRuntypes(){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend(AConstants.CONTROLDESIGNER,AConstants.DesignerInfoRequestGetConfigs,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());
        }
        String [] sa = new String[0];
        if(msgb!=null){
            try {
                if(msgb.getPayloadItem("configFileNames")==null){
                    return;
                }
                sa = msgb.getPayloadItem("configFileNames").getStringArray();
            } catch (cMsgException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            }
            if(sa.length<0){
                System.err.println("  Missing RunType descriptions.");

            } else {
                System.out.println(" Afecs Registered RunTypes ");
//                System.out.println(" ========================= ");
                for (String aSa : sa) {
                    if (aSa.contains(".rdf")) {
                        System.out.println(aSa.substring(0, aSa.indexOf(".rdf")));
                    }
                }
//                System.out.println(" ========================= ");
            }
        } else {
            System.out.println(" Problem communicating with the Platform !");
        }
    }

    /**
     * Does cMsg send and get to the platform normative agent and gets the registered sessions.
     */
    public void getPlatformRegisteredSessions(){

        ConcurrentHashMap<String, ASessionInfo> smap = null;
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend(getPlEXPID(), AConstants.PlatformInfoRequestSessions,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());
        }
        if(msgb!=null && msgb.getByteArray()!=null){
            try {
                smap = (ConcurrentHashMap<String,ASessionInfo>) AfecsTool.B2O(msgb.getByteArray());
            } catch (IOException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            } catch (ClassNotFoundException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            }
            if(smap!=null) {
                System.out.println(" Afecs Registered Sessions ");
//                System.out.println(" ========================= ");
                for(String s:smap.keySet()){
                    System.out.println(s);
                }
//                System.out.println(" ========================= ");
            }
        } else {
            System.out.println(" Problem communicating with the ControDesigner of the Platform !");
        }
    }

    public void getPlatformRegisteredAgents(){
        // ask platform to send all registered agents names
        cMsgMessage msg = null;
        try {
            msg = p2pSend(getPlEXPID(), AConstants.PlatformInfoRequestAgents,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msg!=null && msg.getByteArray()!=null && msg.getByteArrayLength()>0){
            try {
                // get platform registered agents hash map
                ConcurrentHashMap<String, AComponent> registeredComs = (ConcurrentHashMap<String, AComponent>)AfecsTool.B2O(msg.getByteArray());

                if(registeredComs!=null){
                    System.out.println(" Afecs Registered Agents ");
//                    System.out.println(" ========================= ");
                    for(String s:registeredComs.keySet()){
                        System.out.println(s);
                    }
//                    System.out.println(" ========================= ");
                }
            } catch (IOException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            } catch (ClassNotFoundException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            }
        } else {
            System.out.println(" Problem communicating with the Platform !");
        }
    }

    /**
     * Does cMsg send-and-get to the platform normative agent and gets the registered documents.
     */
    public void getPlatformRegisteredDocs(){
        ArrayList<String> dmap = null;
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend(getPlEXPID(), AConstants.PlatformInfoRequestDocs,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null && msgb.getByteArray()!=null){
            try {
                dmap = (ArrayList<String>)AfecsTool.B2O(msgb.getByteArray());
            } catch (IOException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            } catch (ClassNotFoundException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            }
            if(dmap!=null && !dmap.isEmpty()) {
                System.out.println(" User Documents ");
                for(String s:dmap){
                    System.out.println(s);
                }
            } else {
                System.out.println(" Problem communicating with the Platform !");
            }
        }
    }

    public void getSupervisorRunNumber(String runtype, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype, AConstants.SupervisorReportRunNumber,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("Run number   = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

    public void getSupervisorState(String runtype, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype, AConstants.SupervisorReportRunState,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("Run state    = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

    public void getSupervisorComponentsStates(String runtype, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype, AConstants.SupervisorReportComponentStates,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            System.out.println(msgb.getText());
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

    public void getControlStatus(String runtype, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype, AConstants.SupervisorReportSchedulerStatus,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("Run type     = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }

    }

    public void getSupervisorSession(String runtype, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype.trim(), AConstants.SupervisorReportSession,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("Session      = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

    public void getSupervisorRunStartTime(String runtype, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype.trim(), AConstants.SupervisorReportRunStartTime,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("Start time   = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

    public void getSupervisorRunEndTime(String runtype, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype.trim(), AConstants.SupervisorReportRunEndTime,"",AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("End time     = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

    public void getActiveRunType(String sessionName, boolean v){
        cMsgMessage msg = null;
        try {
            msg = p2pSend(getPlEXPID(), AConstants.PlatformInfoRequestAgents, "", AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msg!=null && msg.getByteArray()!=null && msg.getByteArrayLength()>0){
            try {
                // get platform registered agents hash map
                ConcurrentHashMap<String, AComponent> registeredComs =
                        (ConcurrentHashMap<String, AComponent>)AfecsTool.B2O(msg.getByteArray());

                if(registeredComs!=null){
                    for(AComponent comp: registeredComs.values()){
                        if(comp.getName().startsWith("sms_")){
                            if(comp.getSession().equals(sessionName)){
                                if(v){
                                    System.out.println("Run type     = "+comp.getRunType());
                                } else {
                                    System.out.println(comp.getRunType());
                                }
                                return;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            } catch (ClassNotFoundException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            }
        } else {
            System.out.println(" Problem communicating with the platform Registrar agent !");
        }
    }

    public void getComponentEventNumber(String runtype, String compName, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype.trim(), AConstants.SupervisorReportComponentEventNumber,compName.trim(),AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("Event number = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

    public void getComponentOutputFile(String runtype, String compName, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype.trim(), AConstants.SupervisorReportComponentOutputFile,compName.trim(),AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("Output file  = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

    public void getComponentEventRate(String runtype, String compName, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype.trim(), AConstants.SupervisorReportComponentEventRate,compName.trim(),AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("Event rate   = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

    public void getComponentDataRate(String runtype, String compName, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype.trim(), AConstants.SupervisorReportComponentDataRate,compName.trim(),AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: " + e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("Data rate    = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

    public void getComponentState(String runtype, String compName, boolean v){
        cMsgMessage msgb = null;
        try {
            msgb = p2pSend("sms_"+runtype.trim(), AConstants.SupervisorReportComponentState,compName.trim(),AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println("Error: "+e.getMessage());

        }
        if(msgb!=null){
            if(v){
                System.out.println("State        = "+msgb.getText());
            } else {
                System.out.println(msgb.getText());
            }
        } else {
            System.out.println(" Problem communicating with the Supervisor = "+"sms_"+runtype+" !");
        }
    }

}
