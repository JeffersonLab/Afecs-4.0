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
import org.jlab.coda.afecs.ui.rcgui.util.ADialogList;

import javax.swing.*;


/**
 * <p>
 *      Swing worker will ask platform to report COOL runTypes.
 *      This will pop up scrollable list showing the cool
 *      configuration runType ($COOL_HOME/config/control dir)
 *      and will set the forGround color to red to those runTypes
 *      that are configured in the selected session.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/21/14 Time: 1:51 PM
 * @version 2
 */
public class RcClRunTypes extends SwingWorker<Void , Void> {

    private CodaRcGui owner;

    public RcClRunTypes(CodaRcGui owner){
        this.owner = owner;

    }

    @Override
    protected Void doInBackground() throws Exception {
        // show the list of runTypes in the pop-up list
        owner.dbDriver.getPlatformRegisteredRunTypes(AConstants.TIMEOUT);
        return null;
    }

    @Override
    protected void done() {
        super.done();
        if(owner._sessionConfigsColored==null){
            owner.popupInfoDialog("  COOL RunType descriptions were not found.");
        } else if(owner._sessionConfigsColored.size()==0){
            owner.popupInfoDialog("  COOL RunType descriptions were not found.");
        } else if(owner._sessionConfigsColored.size()>0){

            String[] confNames = owner._sessionConfigsColored.keySet().toArray(new String[owner._sessionConfigsColored.size()]);
            Integer[] confColors = owner._sessionConfigsColored.values().toArray(new Integer[owner._sessionConfigsColored.size()]);

            // show the list of COOL runTypes
//            AListDDialog rd = new AListDDialog();
//            rd.showDialog(owner,confNames,confColors,"Configuration","Cool RunTypes",null);

            new ADialogList(owner,confNames,confColors,"Configuration","Cool RunTypes",null);

         }

    }
}
