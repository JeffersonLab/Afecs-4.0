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

import org.jlab.coda.afecs.platform.ASessionInfo;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.ui.rcgui.util.ADocViewer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 *     Run Control Gui initialization swing thread class
 *     <ul>
 *         <li>Reads COOL database session and runType database</li>
 *         <li>Creates rcGUI session and runType menus</li>
 *         <li>Creates rcGUI help menu</li>
 *         <li> Defines and sets rcGUI size</li>
 *     </ul>
 * </p>
 *
 * @author gurjyan
 *         Date: 11/20/14 Time: 2:51 PM
 * @version 4.x
 */
public class RcInit extends SwingWorker<String, Void> {

    private CodaRcGui owner;

    public RcInit(CodaRcGui owner){
        this.owner = owner;
        this.owner.hardResetUi(true);

        // Creates time and bar graphs
        if(this.owner.isEnable_fx()){
            this.owner._prepareGraphs_fx();
        } else {
            this.owner._createNewGraphs_cosy();
        }

        // Start cMsg connection
        // heart beat checking timer
        this.owner.cMsgConHB.start();
    }

    @Override
    protected String doInBackground() {
        String res = "ok";
        System.out.println(" Retrieving Cool-Db runTypes. Please wait....");
        try {
            owner.dbDriver.getPlatformRegisteredRunTypes(AConstants.TIMEOUT);
        } catch (AException e) {
            return "Problem communicating with the Platform.\n  " +
                    "(Platform registered configurations request)";
        }

        System.out.println(" Retrieving Cool-Db sessions. Please wait....");
        try {
            owner.dbDriver.getPlatformRegisteredSessions(AConstants.TIMEOUT);
        } catch (AException e) {
            return "Problem communicating with the Platform.\n  " +
                    "(Platform registered sessions request)";
        }

        System.out.println(" Retrieving Cool-Db documents. Please wait...");
        try {
            owner.dbDriver.getPlatformRegisteredDocs(AConstants.TIMEOUT);
        } catch (AException e) {
            return "Problem communicating with the Platform.\n  " +
                    "(Platform registered documents request)";
        }
        return res;
    }

    @Override
    protected void done() {
        super.done();
        try {
            String out = get();
            if(out.equals("ok")){

                // Recreate rcGUI session menu
                owner.getSessionMenu().removeAll();
                JMenuItem smi = new JMenuItem();
                smi.setText("New...");
                smi.setAction(owner.getAction22());
                owner.getSessionMenu().add(smi);
                owner.getSessionMenu().addSeparator();
                for(ASessionInfo si:owner.platformSessions.values()){

                    // See if env variable defined session name is
                    // registered in the platform. If yes then set
                    // session and runType text fields
                    if(si.getName().equals(owner.cl.getSession())){
                        owner.getSessionTextField().setText(owner.cl.getSession());
                        owner._session = owner.cl.getSession();
                        owner.getConfigurationTextField().setText(si.getConfigName());
                        owner._runType = si.getConfigName();
                        owner.getRunNumberTextField().setText(Integer.toString(si.getRunNumber()));
                        owner.getCoolMenuItem().setEnabled(true);
                        owner.getConfigMenu().setEnabled(true);
                        owner.getConnectMenuItem().setEnabled(true);
                        owner.getDisconnectMenuItem().setEnabled(false);
                        owner.getReleaseComponentMenuItem().setEnabled(false);
                    }

                    // Create and add menu items with action listeners
                    JMenuItem mi = new JMenuItem();
                    mi.setText(si.getName());
                    mi.addActionListener(
                            new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    JMenuItem m = (JMenuItem) e.getSource();
                                    owner.getSessionTextField().setText(m.getText());
                                    owner.getConfigurationTextField().setText(owner.platformSessions.get(m.getText()).getConfigName());
                                    owner.getRunNumberTextField().setText(Integer.toString(owner.platformSessions.get(m.getText()).getRunNumber()));
                                    owner.getCoolMenuItem().setEnabled(true);
                                    owner.getConfigMenu().setEnabled(true);
                                    owner.getConnectMenuItem().setEnabled(true);
                                    owner.getDisconnectMenuItem().setEnabled(false);
                                    owner.getReleaseComponentMenuItem().setEnabled(false);
                                }
                            }
                    );
                    owner.getSessionMenu().add(mi);
                    owner.getSessionMenu().addSeparator();
                }

                // Recreating rcGUI help documentation menu
                owner.getHelpMenu().removeAll();
                if(owner.platformDocs!=null && !owner.platformDocs.isEmpty()) {
                    for(String s:owner.platformDocs.keySet()){

                        // Create and add menu items
                        JMenuItem mi = new JMenuItem();
                        mi.setText(s);
                        mi.addActionListener(
                                new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        JMenuItem m = (JMenuItem) e.getSource();
                                        new ADocViewer(owner.platformDocs.get(m.getText()));
                                    }
                                }
                        );
                        owner.getHelpMenu().add(mi);
                        owner.getHelpMenu().addSeparator();
                    }
                }

                // Control buttons set to undefined state
                owner.gDriver._updateControlBtNs(RcStates.GUISTARTUP);

            } else {
                System.out.println("Debug: " + out);
                System.exit(1);
            }

            // Sets the size of the UI
            owner.setSize(1200, 980);
            owner.setVisible(true);

        } catch (InterruptedException | ExecutionException e) {
            if(AConstants.debug.get()) e.printStackTrace();
        }

    }
}
