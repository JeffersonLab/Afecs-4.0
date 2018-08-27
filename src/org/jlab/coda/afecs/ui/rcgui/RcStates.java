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

import org.jlab.coda.afecs.system.AConstants;

public enum RcStates {
    GUISTARTUP("startup", "startup"),
    UNDEFINED   (AConstants.udf,"undefined"),
    BOOTED      (AConstants.booted,"booting"),
    DISCONNECTED(AConstants.disconnected, "disconnecting"),
    INITIALIZING("initializing","initializing"),
    INITIALIZED ("initialized", "initializing"),
    CONNECTING  ("connecting","connecting"),
    CONNECTED   (AConstants.connected, "connecting"),
    CONFIGURING ("configuring","configuring"),
    CONFIGURED  (AConstants.configured, "configuring"),
    DOWNLOADING ("downloading","downloading"),
    DOWNLOADED  (AConstants.downloaded, "downloading"),
    PRESTARTING ("prestarting","prestarting"),
    PRESTARTED  (AConstants.prestarted, "prestarting"),
    ACTIVATING  ("activating","activating"),
    ACTIVE      (AConstants.active, "activating"),
    ENDING      ("ending","ending"),
    ENDED       (AConstants.ended, "ending"),
    RESETTING   ("resetting","resetting"),
    RESETED     (AConstants.reseted, "resetting"),
    ABORTING    ("aborting","aborting"),
    ABORTED     (AConstants.aborted, "aborting"),
    PAUSED(AConstants.paused, "paused"),
    FAILED      (AConstants.failed, "failed");

    private String name;

    RcStates(String name, String action){
        this.name = name;
        String action1 = action;
    }

    public String getName() {
        return this.name;
    }

    public static RcStates getEnum(String type){
        for(RcStates dt: RcStates.values()){
            if (dt.getName().equals(type.trim())) return dt;
        }
        return null;
    }

}
