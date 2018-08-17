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

package org.jlab.coda.afecs.usr.rcapi;

/**
 * Describe...
 *
 * @author gurjyan
 *         Date: 3/25/14 Time: 10:43 AM
 * @version 2
 */
public enum PLAskCmd {
    rrt       (" rrt:        cool registered run-types"),
    rs        (" rs:         cool registered sessions"),
    ra        (" ra:         cool registered agents"),
    rdoc      (" rdoc:       cool registered documentations"),
    art       (" art:        active run-type in a specified session"),
    arts      (" arts:       active run-types"),
    rt_select (" rt_select:  select a run-type supervisor"),
    exit      (" exit:       exit"),
    help      (" help:       list of all available command"),

    sst       (" sst:        supervisor state"),
    sse       (" sse:        supervisor session"),
    srn       (" srn:        supervisor run-number"),
    scs       (" scs:        supervised component's states"),
    srst      (" srst:       supervisor run start time"),
    sret      (" sret:       supervisor run end time"),
    srtv      (" srtv:       supervisor run-time process variables"),

    cs        (" cs:         component agent state"),
    cen       (" cen:        component agent event-number"),
    cer       (" cer:        component agent event-rate"),
    cdr       (" cdr:        component agent data-rate"),
    cof       (" cof:        component agent output file"),

    configure (" configure:  coda configure transition"),
    download  (" download:   coda download transition"),
    prestart  (" prestart:   coda prestart transition"),
    go        (" go:         coda go transition"),
    end       (" end:        coda end transition"),
    reset     (" reset:      coda reset"),
    start_run (" start_run:  bring coda control to an active state");



    private String h;

    PLAskCmd(String s) {
        h = s;
    }

    public String getHelp() {
        return h;
    }


}
