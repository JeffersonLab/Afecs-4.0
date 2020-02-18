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

package org.jlab.coda.afecs.usr.sudo;

import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.util.ArrayList;
import java.util.Random;

public class AAccount extends ABase {

    private String node;
    private String usr;
    private String password;

    public AAccount(){
        super();
         myName = "AUser-"+new Random().nextInt(100);
    }

    public static void main(String[] args){

        AAccount a = new AAccount();

        if(a.isPlatformConnected() && a.decodeCommandLine(args)){
            if(!a.addUser(a.node, a.usr, a.password)){
                System.out.println("Error registering account with the platform...");
            }
        } else {
            System.out.println("synopsis: -n node, -u user, -p password");
        }
    }

    /**
     * decodes command line
     * @param args arguments
     * @return true if node, usr and password parameters are defined
     */
    public boolean decodeCommandLine(String[] args){
        if(args.length%2==0){
            for(int i=0;i<args.length;i=i+2){
                if(args[i].equalsIgnoreCase("-n")){
                    node = args[i+1];
                } else if(args[i].equalsIgnoreCase("-u")){
                    usr = args[i+1];
                } else if(args[i].equalsIgnoreCase("-p")){
                    password = args[i+1];
                }
            }
        }
        return node != null && usr != null && password != null;
    }

    /**
     * sends request to the platform normative agent to register an account
     * @param n node
     * @param u usr
     * @param p password
     * @return result
     */
    public boolean addUser(String n, String u, String p){
        ArrayList<cMsgPayloadItem> al = new ArrayList<cMsgPayloadItem>();
        try {
            al.add(new cMsgPayloadItem(AConstants.NODE,n));
            al.add(new cMsgPayloadItem(AConstants.USERNAME,u));
            al.add(new cMsgPayloadItem(AConstants.PASSWORD,p));
            return send(myConfig.getPlatformName(),AConstants.PlatformControlRegisterAccount,"",al);
        } catch (cMsgException e) {
            if(AConstants.debug.get()) e.printStackTrace();
            return false;
        }
    }
}
