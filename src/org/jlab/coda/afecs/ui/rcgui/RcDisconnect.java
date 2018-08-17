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

import javax.swing.*;
import java.awt.*;


/**
 * <p>
 *     Swing worker thread that does run-control
 *     supervisor disconnect procedure.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/21/14 Time: 1:51 PM
 * @version 2
 */
public class RcDisconnect extends SwingWorker<Void, Void> {

    private CodaRcGui owner;

    public RcDisconnect(CodaRcGui owner){
        this.owner = owner;
    }

    @Override
    protected Void doInBackground() throws Exception {
        owner.un_Subscribe();
        return null;
    }

    @Override
    protected void done() {
        super.done();
        owner.getRunStatePanel().setBackground(new Color(245,246,247));
        owner._clear_gui();
        owner.gDriver._updateControlBtNs(RcStates.GUISTARTUP);
        owner.getUserRunConfigTextField().setText("unset");
        owner.getUserDirTestField().setText("unset");
    }
}
