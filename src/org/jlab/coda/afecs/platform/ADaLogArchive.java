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
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.ALogger;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgCallbackAdapter;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgSubscriptionHandle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *     Listens daLog messages and archives them in the
 *     COOLHOME/dalogMsg directory (default location).
 *     User also can specify db or different directory
 *     for archiving daLog messages.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/16/14 Time: 2:51 PM
 * @version 3.x
 */
public class ADaLogArchive extends ABase {

    private cMsgSubscriptionHandle dalog;

    private List<cMsgMessage> basket =
            Collections.synchronizedList(new ArrayList<cMsgMessage>());

    private static final int basketSize = 100;

    private String archive_path;
    private String archivePath;

    private AtomicBoolean archiving = new AtomicBoolean(true);

    // Local instance of the logger object
    private ALogger lg = ALogger.getInstance();


    public ADaLogArchive(String ap){
        archive_path = ap;
        myName = AConstants.PLATFORMDALOGARCHIVER;

        // Connect to the platform cMsg domain server
        myPlatformConnection = platformConnect();
        if(isPlatformConnected()){
            subscribe();
        } else {
            lg.logger.severe(" Problem starting DaLogArchiver. " +
                    "Cannot connect to the platform.");
        }
    }

    /**
     * <p>
     *     Archive-er subscription
     * </p>
     * @return  status of the subscription
     */
    private boolean subscribe(){
        boolean stat = true;

        if(archive_path.equals("default")){
            archivePath =  myConfig.getCoolHome() +
                    File.separator + myConfig.getPlatformExpid();
        } else {
            archivePath = archive_path;
        }

        // Create dalogArchive dir in cool_home/expid if it does not exist
        boolean ka = (new File(archivePath +
                File.separator + "dalogArchive")).exists();
        if(!ka){
            stat = (new File(archivePath +
                    File.separator + "dalogArchive")).mkdirs();
            if (!stat) {
                lg.logger.severe("Failed to create "+ archivePath +
                        File.separator + "dalogArchive dir");
            }
        }

        try {
            // subscribe dalogMsges of entire platform
            dalog = myPlatformConnection.subscribe("*",
                    AConstants.DalogMsgType,
                    new DaLogMsgCB(),
                    null);

        } catch (cMsgException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
            stat = false;
        }
        return stat;
    }

    public void stopArchiving(){
        try {
            if(myPlatformConnection!=null &&
                    myPlatformConnection.isConnected()){
                myPlatformConnection.unsubscribe(dalog);
              archiving.set(false);
            }
        } catch (cMsgException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
        }
    }

    public void startArchiving(){
        try {
            if(myPlatformConnection!=null &&
                    myPlatformConnection.isConnected()){
                myPlatformConnection.unsubscribe(dalog);
                dalog = myPlatformConnection.subscribe("*",
                        AConstants.DalogMsgType,
                        new DaLogMsgCB(),
                        null);
                archiving.set(true);
            }
        } catch (cMsgException e) {
            lg.logger.severe(AfecsTool.stack2str(e));
        }
    }

    public boolean isArchiving(){
        return archiving.get();
    }

    /**
     * <p>
     *     Private inner class for responding
     *     to dalogMessages of the platform agents
     * </p>
     */
    private class DaLogMsgCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {

                // archive the message
                basket.add(msg);
                if (basket.size() > basketSize) {
                    // open file with the name dalog_<date>.dat
                    try {
                        BufferedWriter out = new BufferedWriter(
                                new FileWriter(archivePath +
                                        File.separator + "dalogArchive" +
                                        File.separator + "dalog_" +
                                        AfecsTool.getCurrentTime("yy.MM.dd_hh:mm:ss")));
                        for (cMsgMessage m : basket) {
                            out.write(AfecsTool.msg2xml(m));
                        }
                        out.close();
                        basket.clear();
                    } catch (IOException e) {
                        lg.logger.severe(AfecsTool.stack2str(e));
                    }
                }
            }
        }
    }
}
