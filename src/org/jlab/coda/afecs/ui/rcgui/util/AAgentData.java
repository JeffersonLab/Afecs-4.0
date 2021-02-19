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

package org.jlab.coda.afecs.ui.rcgui.util;

import org.jlab.coda.afecs.system.AConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AAgentData {
    private String   name       = AConstants.udf;
    private String   type       = AConstants.udf;
    private String   state      = AConstants.udf;
    private boolean  isMaster   = false;
    private List<Float>      evtRate    = Collections.synchronizedList(new ArrayList<Float>());
    private List<Double>     dataRate   = Collections.synchronizedList(new ArrayList<Double>());
    private float    liveTime   = -1;
    private long eventNumber;
    private float evtRateA;
    private double dataRateA;
    private String[] destinationNames;


    public AAgentData(){
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setEvtNumber(long evtNumber) {
        this.eventNumber = evtNumber;
    }


    public void setEvtRate(float evtRatev) {

        if(evtRate.size()>AConstants.AVERAGING_SIZE) {
            evtRate.remove(0);
        }
        evtRate.add(evtRatev);
    }

    public float getEvtRate(int averageOver) {
        float tmpv = 0.0f;
        int ind = 0;
        if(evtRate.size()>0){
            for(int i=evtRate.size()-averageOver;i<evtRate.size(); i++){
                if(evtRate.get(i)>0.0f){
                    tmpv += evtRate.get(i);
                    ind++;
                }
            }
            if(ind!=0) tmpv = tmpv/ind;
        }
        return tmpv;
    }

    public void setEvtRateA(float evtRateA) {
        this.evtRateA = evtRateA;
    }

    public void setDataRate(double dataRatev) {

        if(dataRate.size()>AConstants.AVERAGING_SIZE) {
            dataRate.remove(0);
        }
        dataRate.add(dataRatev);
    }

    public double getDataRate(int averageOver) {

        double tmpv = 0.0;
        int ind = 0;

        if((dataRate.size()>0) && (dataRate.size()-averageOver>0)){
            for(int i=dataRate.size()-averageOver;i<dataRate.size(); i++){
                if(dataRate.get(i)>0.0f){
                    tmpv += dataRate.get(i);
                    ind++;
                }
            }
            if(ind!=0) tmpv = tmpv/ind;
        }
        if (tmpv==0.0) tmpv = 0.1;
        return tmpv;
    }

    public void setDataRateA(double dataRateA) {
        this.dataRateA = dataRateA;
    }

    public float getLiveTime() {
        return liveTime;
    }

    public void setLiveTime(float liveTime) {
        this.liveTime = liveTime;
    }


    public String[] getDestinationNames() {
        return destinationNames;
    }

    public void setDestinationNames(String[] destinationNames) {
        this.destinationNames = destinationNames;
    }
}
