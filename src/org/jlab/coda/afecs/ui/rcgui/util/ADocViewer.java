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

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.IOException;

public class ADocViewer extends JFrame implements HyperlinkListener{
    private JEditorPane htmlPane = new JEditorPane();

    public ADocViewer(String initialURL) {
        super("Afecs Document Browser");
        htmlPane.setContentType("text/html");
        htmlPane.setEditable(false);
        htmlPane.addHyperlinkListener(this);
        JScrollPane scrollPane = new JScrollPane(htmlPane);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // add an html editor kit
        HTMLEditorKit kit = new HTMLEditorKit();
        htmlPane.setEditorKit(kit);

        // add some styles to the html
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
        styleSheet.addRule("h1 {color: blue;}");
        styleSheet.addRule("h2 {color: #ff0000;}");
        styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");


        // create a document, set it on the jeditorpane, then add the html
        Document doc = kit.createDefaultDocument();
        htmlPane.setDocument(doc);
        htmlPane.setText(initialURL);
        // scroll to the top
        htmlPane.setCaretPosition(0);

        // make it easy to close the application
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // display the frame
        setSize(new Dimension(700,900));

        // center the jframe, then make it visible
        setLocationRelativeTo(null);
        setVisible(true);
    }


    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//            URL url = null;
//            try {
//                URLConnection conn = null;
//                url = new URL(event.getURL().toString());
// Enable the properties used for proxy support
//                System.getProperties().put("proxySet","true");
//                System.getProperties().put("proxyHost","jprox.jlab.org");
//                System.getProperties().put("proxyPort",8080);
//
//                conn = url.openConnection();
//
//            } catch (MalformedURLException e) {
//                if(AConstants.debug.get()) e.printStackTrace();
//            } catch (IOException e) {
//                if(AConstants.debug.get()) e.printStackTrace();
//            }
            try {
                    htmlPane.setPage(event.getURL());
//                    htmlPane.setPage(url);
                } catch (IOException e) {
                    if(AConstants.debug.get()) e.printStackTrace();
                }
        }
    }

}


