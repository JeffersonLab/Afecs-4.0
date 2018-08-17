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

package org.jlab.coda.afecs.ui.rcgui;

import org.jlab.coda.afecs.system.AConfigFileParser;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.ui.rcgui.util.ShowMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RcGuiApplication {

    private CommandLine cmd = new CommandLine();

    private void checkCmdLine(String[] args){
        for(int i=0;i<args.length;i++){

            if(args[i].equals("-e"))cmd.setExpid(args[i+1]);
            else if(args[i].equals("-h"))cmd.setPlatformHost(args[i+1]);
            else if(args[i].equals("-tp")){
                try{
                    cmd.setPlatformTcpPort(Integer.parseInt(args[i+1]));
                } catch(NumberFormatException e){}
            }
            else if(args[i].equals("-up")){
                try{
                    cmd.setPlatformUdpPort(Integer.parseInt(args[i+1]));
                    cmd.setMulticast(true);
                } catch(NumberFormatException e){}
            }
            else if(args[i].equals("-f"))cmd.setConfigFileName(args[i+1]);
            else if(args[i].equals("-s"))cmd.setSession(args[i+1]);
            else if(args[i].equals("-g"))cmd.setDebug(true);
            else if(args[i].equals("-nofx"))cmd.setNoFx(true);
            else if(args[i].equals("-c"))cmd.setAutoConnect(true);
            else if(args[i].equals("-m"))cmd.setMonitorOnly(true);
            else if(args[i].equals("-help"))printSynopsis();
        }

// platform name (expid)
        if(cmd.getExpid().equals(AConstants.udf)){
            if(!cmd.getConfigFileName().equals(AConstants.udf)){
                String fileName = cmd.getConfigFileName();
                AConfigFileParser myConfigParser = new AConfigFileParser(fileName);
                if(!myConfigParser.getPlatformExpid().equals(AConstants.udf)) {
                    cmd.setExpid(myConfigParser.getPlatformExpid());
                }
            }
            if(cmd.getExpid().equals(AConstants.udf)){
                cmd.setExpid(System.getenv("EXPID"));
                if(cmd.getExpid()==null) {
                    System.out.println(" Error: Platform name (EXPID) is not defined !");
                    new ShowMessage("Error"," Afecs control platform name (EXPID) is not defined!");
                    sleep4ever();
                }
            }
        }

// platform hostname
        if(cmd.getPlatformHost().equals(AConstants.udf)){
            String platformHost = "loaclhost";
            try {
                InetAddress addr = InetAddress.getLocalHost();
                platformHost     = addr.getHostName();
            } catch (UnknownHostException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            }
            if(!cmd.getConfigFileName().equals(AConstants.udf)){
                String fileName = cmd.getConfigFileName();
                AConfigFileParser myConfigParser = new AConfigFileParser(fileName);
                if(!myConfigParser.getPlatformHost().equals(AConstants.udf)){
                    cmd.setPlatformHost(myConfigParser.getPlatformHost());
                }
            }
            if(cmd.getPlatformHost().equals(AConstants.udf)){
                cmd.setPlatformHost(platformHost);
            }
        }

// platform tcp port
        if(cmd.getPlatformTcpPort()<=0){
            if(!cmd.getConfigFileName().equals(AConstants.udf)){
                String fileName = cmd.getConfigFileName();
                AConfigFileParser myConfigParser = new AConfigFileParser(fileName);
                if(myConfigParser.getPlatformTcpPort()>0){
                    cmd.setPlatformTcpPort(myConfigParser.getPlatformTcpPort());
                }
            }
            if(cmd.getPlatformTcpPort()<=0){
                cmd.setPlatformTcpPort(AConstants.defaultTcpPort);
            }
        }

// platform udp port
        if(cmd.getPlatformUdpPort()<=0){
            if(!cmd.getConfigFileName().equals(AConstants.udf)){
                String fileName = cmd.getConfigFileName();
                AConfigFileParser myConfigParser = new AConfigFileParser(fileName);
                if(myConfigParser.getPlatformUdpPort()>0){
                    cmd.setPlatformUdpPort(myConfigParser.getPlatformUdpPort());
                    cmd.setMulticast(true);
                }
            }
            if(cmd.getPlatformUdpPort()<=0){
                cmd.setPlatformTcpPort(AConstants.defaultUdpPort);
                cmd.setMulticast(true);
            }
        }

// session
        if(cmd.getSession().equals(AConstants.udf)){
            if(!cmd.getConfigFileName().equals(AConstants.udf)){
                String fileName = cmd.getConfigFileName();
                AConfigFileParser myConfigParser = new AConfigFileParser(fileName);
                if(!myConfigParser.getSession().equals(AConstants.udf)) {
                    cmd.setSession(myConfigParser.getSession());
                }
            }
            if(cmd.getSession().equals(AConstants.udf)){
                cmd.setSession(System.getenv("SESSION"));
                if(cmd.getSession()==null) {
                    System.out.println(" Warning: Control Session is not defined !");
                }
            }
        }

        cmd.setPlatformMulticastUdl("cMsg://multicast:"+cmd.getPlatformUdpPort()+"/cMsg/"+cmd.getExpid()+"?cmsgpassword="+cmd.getExpid());
        cmd.setPlatformUdl("cMsg://"+cmd.getPlatformHost()+":"+cmd.getPlatformTcpPort()+"/cMsg/"+cmd.getExpid()+"?cmsgpassword="+cmd.getExpid());
    }

    private void printSynopsis() {
        System.out.println("Synopsis: rcgui -<option> <value>" +
                "\n-help" +
                "\n-s  <session name> " +
                "\n-e  <experiment id> " +
                "\n-h  <platform host name> " +
                "\n-tp <platform TCP port number> " +
                "\n-up <platform UDP port number>" +
                "\n-f  <xml configuration file name>"+
                "\n-nofx  no JavaFX"+
                "\n-g  ");
        System.exit(0);
    }

    private static void sleep4ever(){
        for(;;){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                if(AConstants.debug.get()) e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){

        RcGuiApplication app = new RcGuiApplication();
        app.checkCmdLine(args);
//        new RcUI(app.cmd);
        new CodaRcGui(app.cmd);
    }

}
