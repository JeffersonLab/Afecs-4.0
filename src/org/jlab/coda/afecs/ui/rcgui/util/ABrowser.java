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

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;

/** Very simplistic "Web browser" using Swing. Supply a URL on the
 *  command line to see it initially, and to set the destination
 *  of the "home" button.
 *  1998 Marty Hall, http://www.apl.jhu.edu/~hall/java/
 */

public class ABrowser extends JFrame implements HyperlinkListener{
  public static void main(String[] args) {
    if (args.length == 0)
//      new ABrowser("http://sites.google.com/site/afecscoda/home");
      new ABrowser("http://www.jlab.org");
    else
      new ABrowser(args[0]);
  }

  private JEditorPane htmlPane;

    public ABrowser(String initialURL) {
    super("Simple Swing Browser");
    try {
        htmlPane = new JEditorPane();
        htmlPane.setPage(initialURL);
        htmlPane.setEditable(false);
        htmlPane.addHyperlinkListener(this);
        JScrollPane scrollPane = new JScrollPane(htmlPane);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    } catch(IOException ioe) {
       warnUser("Can't build HTML pane for " + initialURL
                + ": " + ioe);
    }

    Dimension screenSize = getToolkit().getScreenSize();
    int width = screenSize.width * 8 / 10;
    int height = screenSize.height * 8 / 10;
    setBounds(width/8, height/8, width, height);
    setVisible(true);
  }



  public void hyperlinkUpdate(HyperlinkEvent event) {
    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        htmlPane.setPage(event.getURL());
      } catch(IOException ioe) {
        warnUser("Can't follow link to "
                 + event.getURL().toExternalForm() + ": " + ioe);
      }
    }
  }

  private void warnUser(String message) {
    JOptionPane.showMessageDialog(this, message, "Error",
                                  JOptionPane.ERROR_MESSAGE);
  }
}
