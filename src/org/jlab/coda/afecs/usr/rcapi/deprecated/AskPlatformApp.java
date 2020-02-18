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


public class AskPlatformApp {


    public static void main(String[] args){
        AskPlatformApp map = new AskPlatformApp();
        if(args.length==0){
            map.printSynopsis();
            System.exit(0);
        } else if(args[0].equals("-help")){
            map.printSynopsis();
            System.exit(0);
        }
        map.checkCmdLine(args);
    }

    private void checkCmdLine(String[] args){
        String runType;
        String session;
        boolean f = false;

        AskPlatform  _ask = new AskPlatform();

        if(args.length==1){
            if(args[0].equals("-plRunTypes")){
                f = true;
                _ask.getPlatformRegisteredRuntypes();
            }
            else if(args[0].equals("-plSessions")){
                f = true;
                _ask.getPlatformRegisteredSessions();
            }
            else if(args[0].equals("-plAgents")){
                f = true;
                _ask.getPlatformRegisteredAgents();
            }
            else if(args[0].equals("-plDocs")){
                f = true;
                _ask.getPlatformRegisteredDocs();
            }

        } else if (args.length==3){
            if(args[0].equals("-rt")){
                runType = args[1];
                if(args[2].equals("-spSession")){
                    f = true;
                    _ask.getSupervisorSession(runType,false);
                }
                else if(args[2].equals("-spRunNumber")){
                    f = true;
                    _ask.getSupervisorRunNumber(runType,false);
                }
                else if(args[2].equals("-spState")){
                    f = true;
                    _ask.getSupervisorState(runType,false);
                }
                else if(args[2].equals("-spCompStates")){
                    f = true;
                    _ask.getSupervisorComponentsStates(runType,false);
                }
                else if(args[2].equals("-schedulerStatus")){
                    f = true;
                    _ask.getControlStatus(runType,false);
                }
                else if(args[2].equals("-spRunStartTime")){
                    f = true;
                    _ask.getSupervisorRunStartTime(runType,false);
                }
                else if(args[2].equals("-spRunEndTime")){
                    f = true;
                    _ask.getSupervisorRunEndTime(runType,false);
                }
                else if(args[2].equals("-all")){
                    f = true;
                    _ask.getSupervisorSession(runType,true);
                    _ask.getSupervisorRunNumber(runType,true);
                    _ask.getSupervisorRunStartTime(runType,true);
                    _ask.getSupervisorRunEndTime(runType,true);
                    _ask.getSupervisorState(runType,true);
                    _ask.getSupervisorComponentsStates(runType,true);
                }
            } else if(args[0].equals("-s")){
                session = args[1];
                if(args[2].equals("-runType")){
                    f = true;
                    _ask.getActiveRunType(session,false);
                }
            }

        } else if (args.length==5){
            if(args[0].equals("-rt")){
                runType = args[1];
                if(args[2].equals("-cmp")){
                    String compName = args[3];
                    if(args[4].equals("-cmpState")){
                        f = true;
                        _ask.getComponentState(runType, compName,false);
                    }
                    else if(args[4].equals("-cmpEventNumber")){
                        f = true;
                        _ask.getComponentEventNumber(runType, compName,false);
                    }
                    else if(args[4].equals("-cmpEventRate")){
                        f = true;
                        _ask.getComponentEventRate(runType, compName,false);
                    }
                    else if(args[4].equals("-cmpDataRate")){
                        f = true;
                        _ask.getComponentDataRate(runType, compName,false);
                    }
                    else if(args[4].equals("-cmpOutputFile")){
                        f = true;
                        _ask.getComponentOutputFile(runType, compName,false);
                    }
                    else if(args[4].equals("-all")){
                        f = true;
                        _ask.getComponentState(runType, compName,true);
                        _ask.getComponentEventNumber(runType, compName,true);
                        _ask.getComponentEventRate(runType, compName,true);
                        _ask.getComponentDataRate(runType, compName,true);
                        _ask.getComponentOutputFile(runType, compName,true);
                    }
                }
            }
        }
        if(!f) System.out.println("Unknown command...");
    }





    private void printSynopsis() {
        System.out.println("Synopsis: plask [<option> <value>...]" +
                "\n-help" +
                "\n"+
                "\n-plRunTypes   "+
                "\n-plSessions   "+
                "\n-plAgents     "+
                "\n-plDocs       "+
                "\n-dbRunTypes   "+
                "\n-dbSessions   "+
                "\n"+
                "\n-s <control session> -runType"+
                "\n"+
                "\n-rt <control runtype> -all"+
                "\n-rt <control runtype> -spSession"+
                "\n-rt <control runtype> -spRunNumber"+
                "\n-rt <control runtype> -spRunStartTime"+
                "\n-rt <control runtype> -spRunEndTime"+
                "\n-rt <control runtype> -spState"+
                "\n-rt <control runtype> -spCompStates"+
                "\n-rt <control runtype> -schedulerStatus"+
                "\n"+
                "\n-rt <control runtype> -cmp <component name> -all"+
                "\n-rt <control runtype> -cmp <component name> -cmpState"+
                "\n-rt <control runtype> -cmp <component name> -cmpEventNumber"+
                "\n-rt <control runtype> -cmp <component name> -cmpEventRate"+
                "\n-rt <control runtype> -cmp <component name> -cmpDataRate"+
                "\n-rt <control runtype> -cmp <component name> -cmpOutputFile"+
                "\n");
    }
}
