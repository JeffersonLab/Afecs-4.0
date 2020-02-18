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

package org.jlab.coda.afecs.ui.rcgui.util.chart.fx;

/**
Describe.....

@author gurjyan
@version 4.x
@since 7/10/14
*/


import org.jlab.coda.afecs.system.util.AfecsTool;

import javax.swing.*;
import java.util.HashMap;

public class FxBarChartTest {
    public static HashMap dataMap = new HashMap();
    public  static FxAnimatedBarChart mainFrame;

    public static void main ( String[] args){

        DataGenerator gen =  new DataGenerator();
        gen.start();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainFrame = new FxAnimatedBarChart("Test", dataMap, 800, 400, "Readout Controller ID", "KByte", 500, false, 0.0);
                mainFrame.setVisible(true);
            }
        });
    }
}

class DataGenerator extends Thread {
    public void run() {
        for(int i=0;i<100;i++){
            FxBarChartTest.dataMap.put("aman", Math.random() * 100);
            FxBarChartTest.dataMap.put("chaman", Math.random() * 100);
            FxBarChartTest.dataMap.put("tun",Math.random()*100);
            FxBarChartTest.dataMap.put("tex", Math.random() * 100);

            AfecsTool.sleep(500);
            if(i>50){
                System.out.printf("stop....");
                FxBarChartTest.mainFrame.stopUpdating();
                break;
            }
        }
    }
}


