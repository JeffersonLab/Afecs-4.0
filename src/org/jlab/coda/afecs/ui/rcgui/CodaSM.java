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

public enum CodaSM {

    DOWNLOAD(0,"CodaRcDownload",    AConstants.downloaded, "downloading"),
    PRESTART(1,"CodaRcPrestart",    AConstants.prestarted, "prestarting"),
    GO      (2,"CodaRcGo",          AConstants.active,     "activating"),
    END     (3,"CodaRcEnd",         AConstants.ended,      "ending"),
    STARTRUN(4,"CodaRcStartRun",    AConstants.active,     "activating"),
    RESET   (5,AConstants.reseted,  AConstants.reseted,    "resetting");

    private final int stateid;
    private final String cmd;
    private final String response;
    private final String inActionString;


    CodaSM(int astate, String acmd, String response, String inAction){
        this.stateid = astate;
        this.cmd = acmd;
        this.response = response;
        this.inActionString = inAction;
    }

    public String cmd(){
        return this.cmd;
    }

    public String response(){
        return this.response;
    }

    public String inActionString(){
        return this.inActionString;
    }
}
