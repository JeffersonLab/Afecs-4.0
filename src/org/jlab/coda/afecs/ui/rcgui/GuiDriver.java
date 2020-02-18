
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

import javax.swing.*;
import java.awt.*;

/**
 * <p>
 *     Drives rcGUI.
 *     Defines the button set of the UI
 *     based on the run control state.
 * </p>
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */
public class GuiDriver {
    private CodaRcGui owner;

    public GuiDriver(CodaRcGui owner){
        this.owner = owner;
    }

    private void _ing(RcStates rcs){
        owner._runState = rcs.getName();

        owner.getRunStateTextField().setText(owner._runState);
        owner.getRunStateTextField().setBackground(Color.YELLOW);
        owner.getStatusLabel().setIcon(new ImageIcon(owner.codaHome +"/image/triangle.gif"));

        owner.getConfigureButton().setEnabled(false);
        owner.getDownloadButton().setEnabled(false);
        owner.getPrestartButton().setEnabled(false);
        owner.getGoButton().setEnabled(false);
        owner.getEndButton().setEnabled(false);
        owner.getStartButton().setEnabled(false);
        owner.getResetButton().setEnabled(true);
        owner.getPauseButton().setEnabled(false);

    }

    private void _ed(RcStates rcs){
        owner._runState = rcs.getName();
        owner.getRunStateTextField().setText(owner._runState);
        owner.getStatusLabel().setIcon(new ImageIcon(owner.codaHome +"/image/afecs.png"));
    }

    public RcStates getRcState(String stateName){
        RcStates out = null;
        for(RcStates rs:RcStates.values()){
            if(rs.getName().equals(stateName)){
                out = rs;
            }
        }
        return out;
    }

    public void _updateControlBtNs(RcStates rcState){
        if(rcState!=null){
            if(owner.isMonitorOnly){
                owner.dissableControl();
                return;
            }
            switch(rcState){
                case GUISTARTUP:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(new Color(0,155,200));
                    owner.getConfigureButton().setEnabled(false);

                    owner.getConnectMenuItem().setEnabled(true);
                    owner.getDisconnectMenuItem().setEnabled(false);
                    owner.getReleaseComponentMenuItem().setEnabled(false);
                    owner.getSessionMenu().setEnabled(true);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(false);
                    owner.getPrestartButton().setEnabled(false);
                    owner.getGoButton().setEnabled(false);
                    owner.getEndButton().setEnabled(false);
                    owner.getStartButton().setEnabled(false);
                    owner.getResetButton().setEnabled(false);
                    owner.getPauseButton().setEnabled(false);
                    owner.getRunNumberSetMenuItem().setEnabled(false);

                    owner.dissableRTVMenu();

                    owner.getConfigurationTextField().setBackground(Color.LIGHT_GRAY);

                    owner.getRunStateTextField().setText("");
                    owner.getRunStateTextField().setBackground(Color.LIGHT_GRAY);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    break;
                case UNDEFINED:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(new Color(0, 155, 200));
                    owner.getConfigureButton().setEnabled(false);

                    owner.getConnectMenuItem().setEnabled(false);
                    owner.getDisconnectMenuItem().setEnabled(true);

                    owner.getReleaseComponentMenuItem().setEnabled(false);
                    owner.getSessionMenu().setEnabled(true);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(false);
                    owner.getPrestartButton().setEnabled(false);
                    owner.getGoButton().setEnabled(false);
                    owner.getEndButton().setEnabled(false);
                    owner.getStartButton().setEnabled(false);
                    owner.getResetButton().setEnabled(false);
                    owner.getPauseButton().setEnabled(false);
                    owner.getRunNumberSetMenuItem().setEnabled(false);

                    owner.dissableRTVMenu();

                    owner.getConfigurationTextField().setBackground(Color.LIGHT_GRAY);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    break;
                case DISCONNECTED:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(new Color(0, 155, 200));
                    owner.getConfigureButton().setEnabled(true);

                    owner.getConnectMenuItem().setEnabled(false);
                    owner.getDisconnectMenuItem().setEnabled(true);

                    owner.getReleaseComponentMenuItem().setEnabled(false);
                    owner.getSessionMenu().setEnabled(true);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(false);
                    owner.getPrestartButton().setEnabled(false);
                    owner.getGoButton().setEnabled(false);
                    owner.getEndButton().setEnabled(false);
                    owner.getStartButton().setEnabled(false);
                    owner.getResetButton().setEnabled(false);
                    owner.getPauseButton().setEnabled(false);
                    owner.getRunNumberSetMenuItem().setEnabled(false);

                    owner.dissableRTVMenu();
                    owner.getConfigurationTextField().setBackground(Color.GREEN);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    break;
                case INITIALIZING:
                    owner.getRunNumberSetMenuItem().setEnabled(false);
                    break;
                case INITIALIZED:
                    _ed(rcState);
                    owner.getStartTimeTextField().setText("");
                    owner.getEndTimeTextField().setText("");
                    owner.getEventNumberTextField().setText(" ");
                    owner.getMonitorCompTextField().setText((" "));
                    owner.getExpidTextField().setText(owner.base.getPlEXPID());
                    owner.selectedComponent = AConstants.udf;
                    owner.monitoredComponent = AConstants.udf;
                    owner.getRunNumberSetMenuItem().setEnabled(false);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    break;
                case CONNECTING:
                    owner.getRunNumberSetMenuItem().setEnabled(false);
                    owner.getConfigureButton().setEnabled(false);
                    _ing(rcState);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    break;
                case FAILED:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(new Color(0,155,200));
                    owner.getConfigureButton().setEnabled(true);
                    owner.getConnectMenuItem().setEnabled(false);
                    owner.getDisconnectMenuItem().setEnabled(true);
                    owner.getReleaseComponentMenuItem().setEnabled(true);
                    owner.getSessionMenu().setEnabled(false);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(false);
                    owner.getPrestartButton().setEnabled(false);
                    owner.getGoButton().setEnabled(false);
                    owner.getEndButton().setEnabled(false);
                    owner.getStartButton().setEnabled(false);
                    owner.getResetButton().setEnabled(true);
                    owner.getPauseButton().setEnabled(false);
                    owner.getRunNumberSetMenuItem().setEnabled(false);

                    owner.enableRTVMenu();
                    owner.getConfigurationTextField().setBackground(Color.GREEN);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    break;
                case BOOTED:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(new Color(0,155,200));
                    owner.getConfigureButton().setEnabled(true);
                    owner.getConnectMenuItem().setEnabled(false);
                    owner.getDisconnectMenuItem().setEnabled(true);
                    owner.getReleaseComponentMenuItem().setEnabled(true);
                    owner.getSessionMenu().setEnabled(false);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(false);
                    owner.getPrestartButton().setEnabled(false);
                    owner.getGoButton().setEnabled(false);
                    owner.getEndButton().setEnabled(false);
                    owner.getStartButton().setEnabled(false);
                    owner.getResetButton().setEnabled(true);
                    owner.getPauseButton().setEnabled(false);
                    owner.getRunNumberSetMenuItem().setEnabled(false);

                    owner.enableRTVMenu();
                    owner.getConfigurationTextField().setBackground(Color.GREEN);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    break;
                case CONNECTED:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(new Color(0,155,200));
                    owner.getConfigureButton().setEnabled(false);
//                    owner.getConnectMenuItem().setEnabled(true);
                    owner.getDisconnectMenuItem().setEnabled(true);
                    owner.getReleaseComponentMenuItem().setEnabled(true);
                    owner.getSessionMenu().setEnabled(false);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(false);
                    owner.getPrestartButton().setEnabled(false);
                    owner.getGoButton().setEnabled(false);
                    owner.getEndButton().setEnabled(false);
                    owner.getStartButton().setEnabled(false);
                    owner.getResetButton().setEnabled(false);
                    owner.getPauseButton().setEnabled(false);
                    owner.getRunNumberSetMenuItem().setEnabled(false);

                    owner.enableRTVMenu();
                    owner.getConfigurationTextField().setBackground(Color.GREEN);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);


                    break;
                case CONFIGURING:
                    owner.getRunNumberSetMenuItem().setEnabled(false);
                    _ing(rcState);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    break;
                case RESETED:
                case CONFIGURED:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(new Color(153,255,255));
                    owner.getConfigureButton().setEnabled(true);

                    owner.getConnectMenuItem().setEnabled(false);
                    owner.getDisconnectMenuItem().setEnabled(true);
                    owner.getReleaseComponentMenuItem().setEnabled(true);
                    owner.getSessionMenu().setEnabled(false);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(true);
                    owner.getPrestartButton().setEnabled(false);
                    owner.getGoButton().setEnabled(false);
                    owner.getEndButton().setEnabled(false);
                    owner.getStartButton().setEnabled(false);
                    owner.getResetButton().setEnabled(true);
                    owner.getPauseButton().setEnabled(false);
                    owner.getRunNumberSetMenuItem().setEnabled(true);

                    owner.enableRTVMenu();
                    owner.getConfigurationTextField().setBackground(Color.GREEN);

                    owner.getDisableFileOutputMenuItem().setEnabled(true);
                    owner.getEnableFileOutputMenuItem().setEnabled(true);

                    break;
                case DOWNLOADING:
                    _ing(rcState);
                    break;
                case DOWNLOADED:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(new Color(0,204,204));
                    owner.getConfigureButton().setEnabled(false);

                    owner.getConnectMenuItem().setEnabled(false);
                    owner.getDisconnectMenuItem().setEnabled(true);
                    owner.getReleaseComponentMenuItem().setEnabled(true);
                    owner.getSessionMenu().setEnabled(false);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(true);
                    owner.getGoButton().setEnabled(false);
                    owner.getEndButton().setEnabled(false);

                    if(owner.getAutoStart().equals(AConstants.seton)){
                        owner.getStartButton().setEnabled(false);
                        owner.getPrestartButton().setEnabled(false);
                    }else {
                        owner.getStartButton().setEnabled(true);
                        owner.getPrestartButton().setEnabled(true);
                    }

                    owner.getResetButton().setEnabled(true);
                    owner.getPauseButton().setEnabled(false);
                    owner.getRunNumberSetMenuItem().setEnabled(true);

                    owner.dissableRTVMenu();
                    owner.getConfigurationTextField().setBackground(Color.GREEN);

                    owner.getDisableFileOutputMenuItem().setEnabled(true);
                    owner.getEnableFileOutputMenuItem().setEnabled(true);

                    break;
                case PRESTARTING:
                    owner.getRunNumberSetMenuItem().setEnabled(false);
                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    _ing(rcState);
                    break;
                case PRESTARTED:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(new Color(204,204,0));
                    owner.getConfigureButton().setEnabled(false);

                    owner.getConnectMenuItem().setEnabled(false);
                    owner.getDisconnectMenuItem().setEnabled(true);
                    owner.getReleaseComponentMenuItem().setEnabled(true);
                    owner.getSessionMenu().setEnabled(false);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(false);
                    owner.getPrestartButton().setEnabled(false);
                    owner.getGoButton().setEnabled(true);
                    owner.getEndButton().setEnabled(true);
                    owner.getStartButton().setEnabled(false);
                    owner.getResetButton().setEnabled(true);
                    owner.getPauseButton().setEnabled(false);
                    owner.getRunNumberSetMenuItem().setEnabled(false);

                    owner.dissableRTVMenu();
                    owner.getConfigurationTextField().setBackground(Color.GREEN);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    break;
                case ACTIVATING:
                    owner.getRunNumberSetMenuItem().setEnabled(false);
                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    _ing(rcState);
                    break;
                case ACTIVE:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(Color.green);
                    owner.getConfigureButton().setEnabled(false);

                    owner.getConnectMenuItem().setEnabled(false);
                    owner.getDisconnectMenuItem().setEnabled(true);
                    owner.getReleaseComponentMenuItem().setEnabled(true);
                    owner.getSessionMenu().setEnabled(false);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(false);
                    owner.getPrestartButton().setEnabled(false);
                    owner.getGoButton().setEnabled(false);
                    owner.getEndButton().setEnabled(true);
                    owner.getStartButton().setEnabled(false);
                    owner.getResetButton().setEnabled(true);
                    owner.getPauseButton().setEnabled(true);
                    owner.getRunNumberSetMenuItem().setEnabled(false);

                    owner.dissableRTVMenu();
                    owner.getConfigurationTextField().setBackground(Color.GREEN);
                    owner.getPauseButton().setIcon(owner.pauseIcon);

                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    break;
                case ENDING:
                    owner.getRunNumberSetMenuItem().setEnabled(false);
                    owner.getDisableFileOutputMenuItem().setEnabled(false);
                    owner.getEnableFileOutputMenuItem().setEnabled(false);

                    _ing(rcState);
                    break;
                case ENDED:
                    _ed(rcState);
                    owner.getRunStateTextField().setBackground(new Color(204, 204, 0));
                    owner.getConfigureButton().setEnabled(false);

                    owner.getConnectMenuItem().setEnabled(false);
                    owner.getDisconnectMenuItem().setEnabled(true);
                    owner.getReleaseComponentMenuItem().setEnabled(true);
                    owner.getSessionMenu().setEnabled(false);
                    owner.getCoolMenuItem().setEnabled(true);
                    owner.getConfigMenu().setEnabled(true);

                    owner.getDownloadButton().setEnabled(false);
                    owner.getGoButton().setEnabled(false);
                    owner.getEndButton().setEnabled(true);

                    if(owner.getAutoStart().equals(AConstants.seton)){
                        owner.getStartButton().setEnabled(false);
                        owner.getPrestartButton().setEnabled(false);
                    }else {
                        owner.getStartButton().setEnabled(true);
                        owner.getPrestartButton().setEnabled(true);
                    }
                    owner.getResetButton().setEnabled(true);
                    owner.getPauseButton().setEnabled(false);
                    owner.getRunNumberSetMenuItem().setEnabled(true);

                    owner.dissableRTVMenu();
                    owner.getConfigurationTextField().setBackground(Color.GREEN);

                    owner.getDisableFileOutputMenuItem().setEnabled(true);
                    owner.getEnableFileOutputMenuItem().setEnabled(true);

                    break;
                case RESETTING:
                    owner.getRunNumberSetMenuItem().setEnabled(false);
                    _ing(rcState);
                    break;
                case ABORTING:
                    owner.getRunNumberSetMenuItem().setEnabled(false);
                    _ing(rcState);
                    break;
                case ABORTED:
                    owner.getRunNumberSetMenuItem().setEnabled(false);
                    break;
                case PAUSED:
                    owner.getRunStateTextField().setBackground(new Color(204, 204, 0));
                    owner.getPauseButton().setIcon(owner.goIcon);

                    break;
            }
        }
    }
}
