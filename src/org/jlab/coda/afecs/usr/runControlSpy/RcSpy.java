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

package org.jlab.coda.afecs.usr.runControlSpy;

import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsg;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgNetworkConstants;

/**
 * Run Control spy
 *
 * @author gurjyan
 *         Date: 7/30/14 Time: 8:50 AM
 * @version 2
 */
public class RcSpy {


    public static void main(String[] args) {

        String expid    = AConstants.udf; // string = "undefined"
        String session  = AConstants.udf;
        String runType  = AConstants.udf;
        String subsType;

        String pl_host;
        String m_cast_udl;
        String pl_udl;

        ABase pl_con       = null;
        cMsg pl_connection = null;

        // get command line arguments
        if(args.length == 6){
            if(args[0].equals("-s")){
                session = args[1];
            } else {
                _printSynopsis();
            }
            if(args[2].equals("-r")){
                runType = args[3];
            } else {
                _printSynopsis();
            }
            if(args[4].equals("-e")){
                expid = args[5];
            } else {
                _printSynopsis();
            }
        } else if(args.length == 4){
            if(args[0].equals("-s")){
                session = args[1];
            } else {
                _printSynopsis();
            }
            if(args[2].equals("-r")){
                runType = args[3];
            } else {
                _printSynopsis();
            }
        } else {
            _printSynopsis();
        }

        // if not requested specific expid get it from env variable
        if(expid.equals(AConstants.udf)){
            expid = System.getenv("EXPID");
            if(expid == null) {
                System.out.println(" Error: Platform name (EXPID) is not defined.");
                System.exit(1);
            }
        }


        try {
            // define multi-cast URL to be used to find the host of the platform
            m_cast_udl = "cMsg:rc://multicast/"+expid+"&multicastTO=5&connectTO=5";

            // finding the host name of the platform, by multi-casting available platform multi-cast servers
            cMsg m_cast_connection;
            m_cast_connection = new cMsg(m_cast_udl,"x","y");

            // send a monitor packet and wait the response for 3 sec.
            cMsgMessage m = m_cast_connection.monitor(Integer.toString(3000));

            if(m!=null){
                pl_host = m.getSenderHost();
                // update platform udl and connect
                if(pl_host!=null){

                    // since we fond the platform host create UDL for connecting cMSg server of the platform
                    pl_udl = "cMsg://"+pl_host+":"+cMsgNetworkConstants.nameServerTcpPort+"/cMsg/"+expid+"?cmsgpassword="+expid;

                    // every client of the platform must have a unique name. generate a unique name.
                    // the name generated will be = rcspy_<random number>
                    String name = AfecsTool.generateName("rcspy");

                    // connect to the platform
                    pl_con = new ABase(name, pl_udl,"expid");
                    pl_connection = pl_con.platformConnect();

                }
                if(pl_con!=null && !pl_connection.isConnected()){
                    System.out.println(" Can not connect to the "+expid+" platform.");
                    System.exit(1);
                }
            } else {
                System.out.println(" Can not find the "+expid+" platform.");
                System.exit(1);
            }

            // define subscription type to listen run control messages
            subsType = session+"_"+runType+"/*";

            // subscribe run control messages
            pl_connection.subscribe(AConstants.GUI, subsType, new RcSpyCB(session, runType), null);

            // sleep until kill....
            while(true){
                AfecsTool.sleep(7000);
            }

        } catch (cMsgException e) {
            e.printStackTrace();
        }

    }

    private static void _printSynopsis(){
        System.out.println("rcspy -s <session> -r <runType> -e <expid>(optional)");
        System.exit(1);
    }

}
