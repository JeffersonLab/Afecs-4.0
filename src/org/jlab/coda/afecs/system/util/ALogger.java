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

package org.jlab.coda.afecs.system.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * <p>
 *     Afecs logger.
 *     Logs in the $COOL_HOME/$EXPID/log directory file
 *     named as afecs.log
 * </p>
 *
 *
 * @author gurjyan
 *         Date: 3/27/14 Time: 10:29 AM
 * @version 3.x
 */
public class ALogger {

    public final Logger logger = Logger.getLogger("Afecs");
    private static ALogger instance = null;
    private static String logFileDir =
            System.getenv("COOL_HOME")+File.separator+
            System.getenv("EXPID")+File.separator+"log";


    public static ALogger getInstance(){
        if(instance == null) {

            instance = new ALogger ();
            instance.initLogger();
        }
        return instance;
    }

    private void initLogger(){

        FileHandler myFileHandler;
        File f = new File(logFileDir);

        if(!f.exists()){
            if(f.mkdirs()) System.out.println("can not create the log file.");
        }

        try {
            myFileHandler = new FileHandler(logFileDir+ File.separator+"afecs.log");
            myFileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(myFileHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.FINEST);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


}


