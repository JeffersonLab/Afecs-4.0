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

import com.cosylab.gui.components.spikechart.ChartUpdateRequest;
import com.cosylab.gui.components.spikechart.Interval;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.afecs.ui.rcgui.util.AAgentData;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RcGraphUpdate {
    private CodaRcGui owner;
    private Timer timer;
    private float prevEvtRate = 10;
    private double prevDataRate = 10;

    ActionListener actionListener = new ActionListener() {

        public void actionPerformed(ActionEvent actionEvent) {
            if(owner._runState.equals(AConstants.active)){
                if(!owner.graphingCompList.isEmpty()){
                    for(AAgentData ad:owner.graphingCompList.values()){
                        float evtRate = ad.getEvtRate(owner.AIV);
                        double dataRate = ad.getDataRate(owner.AIV);

                        // event rate update
                        if(owner.EvtRateTG!=null && owner.EvtRateTG.getModel(ad.getName())!=null){
                            if(evtRate >= 0){
                                if (evtRate >= owner.EvtRateTG.getYmax() || evtRate <= owner.EvtRateTG.getYmin()){
                                    if(evtRate>0) {
                                        prevEvtRate = evtRate;
                                        owner.EvtRateTG.setPm(evtRate * 0.5);
                                        owner.EvtRateTG.setYmax(evtRate + owner.EvtRateTG.getPm());
                                    } else {
                                        owner.EvtRateTG.setPm(prevEvtRate * 0.5);
                                        owner.EvtRateTG.setYmax(prevEvtRate + owner.EvtRateTG.getPm());
                                    }
                                    owner.EvtRateTG.setYmin(0.0);
                                    owner.EvtRateTG.getBaseChart().getViewManager().setYScale(
                                            new Interval(owner.EvtRateTG.getYmin(), owner.EvtRateTG.getYmax(), false));
                                    AfecsTool.sleep(1000);
                                }
                                owner.EvtRateTG.getModel(ad.getName()).generatePoint((double) evtRate);
                                owner.EvtRateTG.getBaseChart().updateChart(ChartUpdateRequest.UPDATE_ALL);
                            }
                        }

                        // data rate update
                        if(owner.DataRateTG!=null && owner.DataRateTG.getModel(ad.getName())!=null){
                            if(dataRate >=0){
                                if (dataRate >= owner.DataRateTG.getYmax() || dataRate <= owner.DataRateTG.getYmin()){
                                    if(dataRate>0) {
                                        prevDataRate = dataRate;
                                        owner.DataRateTG.setPm(dataRate * 0.5);
                                        owner.DataRateTG.setYmax(dataRate + owner.DataRateTG.getPm());
                                    } else {
                                        owner.DataRateTG.setPm(prevDataRate * 0.5);
                                        owner.DataRateTG.setYmax(prevDataRate + owner.DataRateTG.getPm());
                                    }
                                    owner.DataRateTG.setYmin(0.0);
                                    owner.DataRateTG.getBaseChart().getViewManager().setYScale(
                                            new Interval(owner.DataRateTG.getYmin(), owner.DataRateTG.getYmax(), false));
                                    AfecsTool.sleep(100);
                                }
                                owner.DataRateTG.getModel(ad.getName()).generatePoint(dataRate);
                                owner.DataRateTG.getBaseChart().updateChart(ChartUpdateRequest.UPDATE_ALL);
                            }
                        }
                    }
                }

                // live time update
                if(owner.tsComponent !=null){
                        if(owner.LiveTimeTG!=null && owner.LiveTimeTG.getModel(owner.tsComponent.getName())!=null){
                            if(owner.tsComponent.getLiveTime()>=0){
                                if (owner.tsComponent.getLiveTime() >= owner.LiveTimeTG.getYmax() ||
                                        owner.tsComponent.getLiveTime() <= owner.LiveTimeTG.getYmin()){
                                    owner.LiveTimeTG.setYmax(owner.tsComponent.getLiveTime()+((owner.tsComponent.getLiveTime()*20.0)/100.0));
                                    owner.LiveTimeTG.setYmin(0.0);
                                    owner.LiveTimeTG.getBaseChart().getViewManager().setYScale(
                                            new Interval(owner.LiveTimeTG.getYmin(), owner.LiveTimeTG.getYmax(), false));
                                    AfecsTool.sleep(100);
                                }
                                owner.LiveTimeTG.getModel(owner.tsComponent.getName()).generatePoint((double) owner.tsComponent.getLiveTime());
                                owner.LiveTimeTG.getBaseChart().updateChart(ChartUpdateRequest.UPDATE_ALL);
                            }
                        }
                }
            }
//            owner.getDateLabel().setText(owner.dateFormatter.format(new Date()));
        }
    };

    public RcGraphUpdate(CodaRcGui owner){
        this.owner = owner;
        int delay = 1000;
        timer = new Timer(delay, actionListener);
    }

    public void start(){
        timer.start();
    }

    public void stop(){
        timer.stop();
    }

}
