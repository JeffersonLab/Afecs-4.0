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

package org.jlab.coda.afecs.usr.rcapi.plask;

import com.martiansoftware.jsap.*;
import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.afecs.usr.rcapi.RcApi;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.jlab.coda.afecs.usr.rcapi.plask.PLAskCmd.valueOf;

/**
 * Command line Afecs
 *
 * @author gurjyan
 *         Date: 3/25/14 Time: 10:28 AM
 * @version 2
 */
public class PLAsk {
    private static final String ARG_HOST   = "host";
    private static final String ARG_EXPID  = "expid";
    private static final String ARG_INTER  = "inter";
    private static final String ARG_CMD    = "cmd";


    private static void setArguments(JSAP jsap) {

        FlaggedOption host = new FlaggedOption(ARG_HOST)
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(false)
                .setShortFlag('h')
                .setLongFlag("host");
        host.setHelp("Afecs platform host");
        FlaggedOption name = new FlaggedOption(ARG_EXPID)
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setShortFlag('n')
                .setLongFlag("name");
        name.setHelp("Afecs platform name");
        QualifiedSwitch inter = new QualifiedSwitch( ARG_INTER,
                JSAP.STRING_PARSER,
                JSAP.NO_DEFAULT,
                JSAP.NOT_REQUIRED,
                'i',
                "inter",
                "Program interactive mode" );
        String _cmdHelp = "List of supported commands" +
                "\n-c:rrt                         cool registered run-types" +
                "\n-c:rs                          cool registered sessions" +
                "\n-c:ra                          cool registered agents" +
                "\n-c:rdoc                        cool registered documentations" +
                "\n-c:art,<session>               active run-type in a specified session" +
                "\n-c:arts                        active run-types" +
                "\n-c:sst,<run-type>              supervisor state" +
                "\n-c:sse,<run-type>              supervisor session" +
                "\n-c:srn,<run-type>              supervisor run-number" +
                "\n-c:scs,<run-type>              supervised component's states" +
                "\n-c:srst,<run-type>             supervisor run start time" +
                "\n-c:sret,<run-type>             supervisor run end time" +
                "\n-c:srtv,<run-type>             supervisor run-time process variables" +
                "\n-c:cs,<run-type>,<comp-name>   component agent state" +
                "\n-c:cen,<run-type>,<comp-name>  component agent event-number" +
                "\n-c:cer,<run-type>,<comp-name>  component agent event-rate" +
                "\n-c:cdr,<run-type>,<comp-name>  component agent data-rate" +
                "\n-c:cof,<run-type>,<comp-name>  component agent output file" +
                "\n-c:download,<run-type>         coda download transition" +
                "\n-c:prestart,<run-type>         coda prestart transition" +
                "\n-c:go,<run-type>               coda go transition" +
                "\n-c:end,<run-type>              coda end transition" +
                "\n-c:start_run,<run-type>        coda end transition" +
                "\n-c:reset,<run-type>            coda reset";
        QualifiedSwitch command = (QualifiedSwitch) new QualifiedSwitch( ARG_CMD,
                JSAP.STRING_PARSER,
                JSAP.NO_DEFAULT,
                JSAP.NOT_REQUIRED,
                'c',
                "cmd",
                _cmdHelp).setList(true).setListSeparator(',');

        try {
            jsap.registerParameter(host);
            jsap.registerParameter(name);
            jsap.registerParameter(inter);
            jsap.registerParameter(command);
        } catch (JSAPException e) {
            if(AConstants.debug.get()) e.printStackTrace();
            System.exit(1);
        }
    }


    public static void main(String[] args) {
        /* Check the arguments */
        JSAP jsap = new JSAP();
        setArguments(jsap);
        JSAPResult config = jsap.parse(args);
        if (!config.success()) {
            System.err.println();
            System.err.println("Usage: plask " + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
        } else {
            // connect to the platform
            RcApi api = new RcApi();
            if(config.getString(ARG_HOST).equals("host")){
                if (!api.pl_connect(config.getString(ARG_EXPID))) {
                    System.out.println("Cannot connect to the platform  " +
                             " name = " + config.getString(ARG_EXPID));
                    System.exit(1);
                }
            } else {
                if (!api.pl_connect(config.getString(ARG_HOST), config.getString(ARG_EXPID))) {
                    System.out.println("Cannot connect to the platform at host = " +
                            config.getString(ARG_HOST) + " name = " + config.getString(ARG_EXPID));
                    System.exit(1);
                }
            }

            if(config.getBoolean(ARG_INTER)){
                Scanner iScan = new Scanner(System.in);
                String cmd;
                PLAskCmd[] cmdList = PLAskCmd.values();
                while(true){
                    System.out.print("<" +AfecsTool.getCurrentTime("HH:mm:ss")+" afecs >");
                    cmd = iScan.nextLine();
                    try{
                        switch (valueOf(cmd)){
                            case help: {
                                for(int i=0; i<=7; i++){
                                    System.out.println(cmdList[i].getHelp());
                                }
                                break;
                            }
                            case exit: {
                                System.exit(0);
                                break;
                            }
                            // runtime
                            // currently active run-type in the session
                            case art: {
                                System.out.println("enter session...");
                                String v = iScan.nextLine();
                                System.out.println(api.getActiveRunType(v));
                                break;
                            }
                            // all registered run-types with their sessions
                            case arts: {
                                List<AComponent> rts = api.getActiveRunTypes();
                                if(rts!=null && !rts.isEmpty()){
                                    for(AComponent cmp: rts){
                                    System.out.println("run-type="+cmp.getRunType()+
                                            " session="+cmp.getSession());
                                    }
                                }
                                break;
                            }
                            // cool database specific
                            // cool run-types
                            case rrt: {
                                String [] r =  api.getPlatformRegisteredRunTypes();
                                if(r!=null && r.length>0){
                                    for(String s:r){
                                        System.out.println(s);
                                    }
                                }
                                break;
                            }
                            // cool sessions
                            case rs: {
                                String [] r =  api.getPlatformRegisteredSessions();
                                if(r!=null && r.length>0){
                                    for(String s:r){
                                        System.out.println(s);
                                    }
                                }
                                break;
                            }
                            // platform registered agents
                            case ra: {
                                String [] r =  api.getPlatformRegisteredAgents();
                                if(r!=null && r.length>0){
                                    for(String s:r){
                                        System.out.println(s);
                                    }
                                }
                                break;
                            }
                            // cool registered docs
                            case rdoc: {
                                String [] r =  api.getPlatformRegisteredDocs();
                                if(r!=null && r.length>0){
                                    for(String s:r){
                                        System.out.println(s);
                                    }
                                }
                                break;
                            }

                            // control supervisor specific
                            case rt_select: {
                                System.out.println("enter run-type...");
                                String rt = iScan.nextLine();
                                boolean isRtSelected = true;
                                while(isRtSelected){
                                    System.out.print("<" +AfecsTool.getCurrentTime("HH:mm:ss")+" afecs-"+rt+ " >");
                                    cmd = iScan.nextLine();
                                    try{
                                        switch (valueOf(cmd)){
                                            case help: {
                                                for(int i=6; i<cmdList.length; i++){
                                                    System.out.println(cmdList[i].getHelp());
                                                }
                                                break;
                                            }
                                            case exit: {
                                                isRtSelected = false;
                                                break;
                                            }
                                            // supervisor run-number
                                            case srn: {
                                                System.out.println(api.getSupervisorRunNumber(rt));
                                                break;
                                            }
                                            // supervisor statue
                                            case sst: {
                                                System.out.println(api.getSupervisorState(rt));
                                                break;
                                            }
                                            // supervisor session
                                            case sse: {
                                                System.out.println(api.getSupervisorSession(rt));
                                                break;
                                            }
                                            // supervised components states
                                            case scs: {
                                                String [] r = api.getSupervisorComponentsStates(rt);
                                                if(r!=null && r.length>0){
                                                    for(String s:r){
                                                        System.out.println(s);
                                                    }
                                                }
                                                break;
                                            }
                                            // supervisor run start time
                                            case srst: {
                                                System.out.println(api.getSupervisorRunStartTime(rt));
                                                break;
                                            }
                                            // supervisor run end time
                                            case sret: {
                                                System.out.println(api.getSupervisorRunEndTime(rt));
                                                break;
                                            }
                                            // supervisor run-time process variables
                                            case srtv: {
                                                Map<String,String> map = api.getRtvs(rt);
                                                if(map!=null){
                                                    for(String rtv:map.keySet()){
                                                        System.out.println(rtv+" = "+map.get(rtv));
                                                    }
                                                }
                                                break;
                                            }
                                            // component agent specific
                                            // component event number
                                            case cen: {
                                                System.out.println("enter component name...");
                                                String v2 = iScan.nextLine();
                                                System.out.println(api.getComponentEventNumber(rt,v2));
                                                break;
                                            }
                                            // component output file
                                            case cof: {
                                                System.out.println("enter component name...");
                                                String v2 = iScan.nextLine();
                                                System.out.println(api.getComponentOutputFile(rt,v2));
                                                break;
                                            }
                                            // component event rate
                                            case cer: {
                                                System.out.println("enter component name...");
                                                String v2 = iScan.nextLine();
                                                System.out.println(api.getComponentEventRate(rt,v2));
                                                break;
                                            }
                                            // component data rate
                                            case cdr: {
                                                System.out.println("enter component name...");
                                                String v2 = iScan.nextLine();
                                                System.out.println(api.getComponentDataRate(rt,v2));
                                                break;
                                            }
                                            // component state
                                            case cs: {
                                                System.out.println("enter component name...");
                                                String v2 = iScan.nextLine();
                                                System.out.println(api.getComponentState(rt,v2));
                                                break;
                                            }
                                            // CODA state machine control
                                            // download
                                            case configure: {
                                                System.out.println("enter session...");
                                                String s = iScan.nextLine();
                                                System.out.println("enter run type...");
                                                String r = iScan.nextLine();
                                                api.rcgConfigure(s,r);
                                                break;
                                            }
                                            // download
                                            case download: {
                                                api.rcgDownload(rt);
                                                break;
                                            }
                                            // prestart
                                            case prestart: {
                                                api.rcgPrestart(rt);
                                                break;
                                            }
                                            // go
                                            case go: {
                                                api.rcgGo(rt);
                                                break;
                                            }
                                            // end
                                            case end: {
                                                api.rcgEnd(rt);
                                                break;
                                            }
                                            // reset
                                            case reset: {
                                                api.rcgReset(rt);
                                                break;
                                            }
                                            // start run
                                            case start_run: {
                                                api.rcgStartRun(rt);
                                                break;
                                            }
                                        }
                                    } catch (IllegalArgumentException e){
                                        System.out.println("unknown command");
                                    }
                                }
                                break;
                            }

                        }
                    } catch (IllegalArgumentException e){
                        System.out.println("unknown command");
                    }
                }
            } else {
                //  now interactive mode
                String[] _cmds = config.getStringArray(ARG_CMD);
                String _cmd = _cmds[0];
                try{
                    switch (valueOf(_cmd)){
                        // runtime
                        // currently active run-type in the session
                        case art: {
                            if(_cmds.length==2){
                                String _session = _cmds[1];
                                // 09.6.22 vg
                                // handle env variables for the session
                                if(_session.startsWith("$")){
                                    _session = System.getenv(_session);
                                }
                                System.out.println(api.getActiveRunType(_session));
                            }
                            break;
                        }
                        // cool database specific
                        // cool run-types
                        case rrt: {
                            String [] r =  api.getPlatformRegisteredRunTypes();
                            if(r!=null && r.length>0){
                                for(String s:r){
                                    System.out.println(s);
                                }
                            }
                            break;
                        }
                        // cool sessions
                        case rs: {
                            String [] r =  api.getPlatformRegisteredSessions();
                            if(r!=null && r.length>0){
                                for(String s:r){
                                    System.out.println(s);
                                }
                            }
                            break;
                        }
                        // platform registered agents
                        case ra: {
                            String [] r =  api.getPlatformRegisteredAgents();
                            if(r!=null && r.length>0){
                                for(String s:r){
                                    System.out.println(s);
                                }
                            }
                            break;
                        }
                        // cool registered docs
                        case rdoc: {
                            String [] r =  api.getPlatformRegisteredDocs();
                            if(r!=null && r.length>0){
                                for(String s:r){
                                    System.out.println(s);
                                }
                            }
                            break;
                        }
                        // supervisor run-number
                        case srn: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                System.out.println(api.getSupervisorRunNumber(_runtype));
                            }
                            break;
                        }
                        // supervisor statue
                        case sst: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                System.out.println(api.getSupervisorState(_runtype));
                            }
                            break;
                        }
                        // supervisor session
                        case sse: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                System.out.println(api.getSupervisorSession(_runtype));
                            }
                            break;
                        }
                        // supervised components states
                        case scs: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                String [] r = api.getSupervisorComponentsStates(_runtype);
                                if(r!=null && r.length>0){
                                    for(String s:r){
                                        System.out.println(s);
                                    }
                                }
                            }
                            break;
                        }
                        // supervisor run start time
                        case srst: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                System.out.println(api.getSupervisorRunStartTime(_runtype));
                            }
                            break;
                        }
                        // supervisor run end time
                        case sret: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                System.out.println(api.getSupervisorRunEndTime(_runtype));
                            }
                            break;
                        }
                        // component agent specific
                        // component event number
                        case cen: {
                            if(_cmds.length==3){
                                String _runtype = _cmds[1];
                                String _cmpname = _cmds[2];
                                System.out.println(api.getComponentEventNumber(_runtype,_cmpname));
                            }
                            break;

                        }
                        // component output file
                        case cof: {
                            if(_cmds.length==3){
                                String _runtype = _cmds[1];
                                String _cmpname = _cmds[2];
                                System.out.println(api.getComponentOutputFile(_runtype,_cmpname));
                            }
                            break;
                        }
                        // component event rate
                        case cer: {
                            if(_cmds.length==3){
                                String _runtype = _cmds[1];
                                String _cmpname = _cmds[2];
                                System.out.println(api.getComponentEventRate(_runtype,_cmpname));
                            }
                            break;
                        }
                        // component data rate
                        case cdr: {
                            if(_cmds.length==3){
                                String _runtype = _cmds[1];
                                String _cmpname = _cmds[2];
                                System.out.println(api.getComponentDataRate(_runtype,_cmpname));
                            }
                            break;
                        }
                        // component state
                        case cs: {
                            if(_cmds.length==3){
                                String _runtype = _cmds[1];
                                String _cmpname = _cmds[2];
                                System.out.println(api.getComponentState(_runtype,_cmpname));
                            }
                            break;
                        }
                        // CODA state machine control
                        // download
                        case download: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                api.rcgDownload(_runtype);
                            }
                            break;
                        }
                        // prestart
                        case prestart: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                api.rcgPrestart(_runtype);
                            }
                            break;
                        }
                        // go
                        case go: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                api.rcgGo(_runtype);
                            }
                            break;
                        }
                        // end
                        case end: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                api.rcgEnd(_runtype);
                            }
                            break;
                        }
                        // reset
                        case reset: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                api.rcgReset(_runtype);
                            }
                            break;
                        }
                        // start run
                        case start_run: {
                            if(_cmds.length==2){
                                String _runtype = _cmds[1];
                                api.rcgStartRun(_runtype);
                            }
                            break;
                        }
                    }
                } catch (IllegalArgumentException e){
                    System.out.println("unknown command");
                }
            }
        }

    }
}
