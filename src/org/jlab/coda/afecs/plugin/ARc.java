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

package org.jlab.coda.afecs.plugin;

import org.jlab.coda.afecs.cool.ontology.AChannel;

import java.util.ArrayList;

public class ARc implements IAClientCommunication {

    public void init() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDescription() {
        return "";
    }

    public void addChannel(AChannel c) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeChannel(String channelName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean setChannel(AChannel channel) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AChannel readChannel(String channelName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ArrayList<AChannel> getAllChannels() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getChannelCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void monitorOn(String channelName, int period) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void monitorOff(String channelName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setWorkingFor(String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
