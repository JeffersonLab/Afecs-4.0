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

package org.jlab.coda.afecs.ui.rcgui.util.chart;

import com.cosylab.gui.components.spikechart.BaseChart;
import com.cosylab.gui.components.spikechart.DecoratedChart;

import javax.swing.*;
import java.awt.*;

public class ACosiTimeChart extends JPanel {
    public ATimeChartDataModel[] models=null;


    private javax.swing.JPanel ivjJFrameContentPane = null;
    private BaseChart ivjBaseChart = null;
    private javax.swing.JLabel ivjJLabel1 = null;
    private javax.swing.JLabel ivjJLabel2 = null;
    private javax.swing.JPanel ivjJPanel1 = null;
    private javax.swing.JSlider ivjUpdaterSlider = null;
    private JButton updateButton;
    private double pm = 300.0;
    private double Ymin = 0.0;
    private double Ymax = 10.0;

    public ACosiTimeChart() {
    }

    public double getPm() {
        return pm;
    }

    public void setPm(double pm) {
        this.pm = pm;
    }

    public double getYmin() {
        return Ymin;
    }

    public void setYmin(double ymin) {
        this.Ymin = ymin;
    }

    public double getYmax() {
        return Ymax;
    }

    public void setYmax(double ymax) {
        this.Ymax = ymax;
    }

    public com.cosylab.gui.components.spikechart.BaseChart getBaseChart() {
        if (ivjBaseChart == null) {
            try {
                ivjBaseChart = new DecoratedChart();
                ivjBaseChart.setName("BaseChart");
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
        }
        return ivjBaseChart;
    }
    /**
     * Return the JFrameContentPane property value.
     * @return javax.swing.JPanel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    public javax.swing.JPanel getJFrameContentPane() {
        if (ivjJFrameContentPane == null) {
            try {
                ivjJFrameContentPane = new javax.swing.JPanel();
                ivjJFrameContentPane.setName("JFrameContentPane");
                ivjJFrameContentPane.setLayout(new java.awt.BorderLayout());
//                getJFrameContentPane().add(getJPanel1(), "South");
                getJFrameContentPane().add(getBaseChart(), "Center");
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
        }
        return ivjJFrameContentPane;
    }
    /**
     * Return the JLabel1 property value.
     * @return javax.swing.JLabel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private javax.swing.JLabel getJLabel1() {
        if (ivjJLabel1 == null) {
            try {
                ivjJLabel1 = new javax.swing.JLabel();
                ivjJLabel1.setName("JLabel1");
                ivjJLabel1.setText("updates per s");
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
        }
        return ivjJLabel1;
    }
    /**
     * Return the JLabel2 property value.
     * @return javax.swing.JLabel
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private javax.swing.JLabel getJLabel2() {
        if (ivjJLabel2 == null) {
            try {
                ivjJLabel2 = new javax.swing.JLabel();
                ivjJLabel2.setName("JLabel2");
                ivjJLabel2.setText("1");
                ivjJLabel2.setMaximumSize(new java.awt.Dimension(21, 14));
                ivjJLabel2.setPreferredSize(new java.awt.Dimension(21, 14));
                ivjJLabel2.setMinimumSize(new java.awt.Dimension(21, 14));
                ivjJLabel2.setHorizontalAlignment(
                        javax.swing.SwingConstants.RIGHT);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
        }
        return ivjJLabel2;
    }
    private javax.swing.JPanel getJPanel1() {
        if (ivjJPanel1 == null) {
            try {
                ivjJPanel1 = new javax.swing.JPanel();
                ivjJPanel1.setName("JPanel1");
                ivjJPanel1.setLayout(new java.awt.GridBagLayout());

                java.awt.GridBagConstraints constraintsUpdateButton =
                        new java.awt.GridBagConstraints(
                                0,
                                0,
                                1,
                                1,
                                0.0,
                                0.0,
                                GridBagConstraints.CENTER,
                                GridBagConstraints.HORIZONTAL,
                                new Insets(11, 11, 11, 11),
                                0,
                                0);
                getJPanel1().add(getUpdateButton(), constraintsUpdateButton);

                java.awt.GridBagConstraints constraintsUpdaterSlider =
                        new java.awt.GridBagConstraints(
                                1,
                                0,
                                1,
                                1,
                                1.0,
                                0.0,
                                GridBagConstraints.CENTER,
                                GridBagConstraints.HORIZONTAL,
                                new Insets(11, 11, 11, 2),
                                0,
                                0);
                getJPanel1().add(getUpdaterSlider(), constraintsUpdaterSlider);

                java.awt.GridBagConstraints constraintsJLabel2 =
                        new java.awt.GridBagConstraints(
                                2,
                                0,
                                1,
                                1,
                                0.0,
                                0.0,
                                GridBagConstraints.CENTER,
                                GridBagConstraints.HORIZONTAL,
                                new Insets(4, 2, 4, 2),
                                0,
                                0);
                getJPanel1().add(getJLabel2(), constraintsJLabel2);

                java.awt.GridBagConstraints constraintsJLabel1 =
                        new java.awt.GridBagConstraints(
                                3,
                                0,
                                1,
                                1,
                                0.0,
                                0.0,
                                GridBagConstraints.CENTER,
                                GridBagConstraints.HORIZONTAL,
                                new Insets(11, 2, 11, 11),
                                0,
                                0);
                getJPanel1().add(getJLabel1(), constraintsJLabel1);

            } catch (java.lang.Throwable ivjExc) {
                handleException(ivjExc);
            }
        }
        return ivjJPanel1;
    }
    /**
     * Return the UpdaterSlider property value.
     * @return javax.swing.JSlider
     */
    /* WARNING: THIS METHOD WILL BE REGENERATED. */
    private javax.swing.JSlider getUpdaterSlider() {
        if (ivjUpdaterSlider == null) {
            try {
                ivjUpdaterSlider = new javax.swing.JSlider();
                ivjUpdaterSlider.setName("UpdaterSlider");
                ivjUpdaterSlider.setPaintLabels(false);
                ivjUpdaterSlider.setPaintTicks(true);
                ivjUpdaterSlider.setValue(1);
                ivjUpdaterSlider.setMajorTickSpacing(10);
                ivjUpdaterSlider.setSnapToTicks(true);
                ivjUpdaterSlider.setMinimum(0);
                ivjUpdaterSlider.setMinorTickSpacing(1);
                ivjUpdaterSlider.setMaximum(30);
                // user code begin {1}
                // user code end
            } catch (java.lang.Throwable ivjExc) {
                // user code begin {2}
                // user code end
                handleException(ivjExc);
            }
        }
        return ivjUpdaterSlider;
    }

    private JButton getUpdateButton() {
        if (updateButton == null) {
            updateButton = new JButton();
            updateButton.setActionCommand("Update");
            updateButton.setName("UpdateButton");
            updateButton.setText("Update");
        }
        return updateButton;
    }
    /**
     * Called whenever the part throws an exception.
     * @param exception java.lang.Throwable
     */
    private void handleException(java.lang.Throwable exception) {

        /* Uncomment the following lines to print uncaught exceptions to stdout */
        System.out.println("--------- UNCAUGHT EXCEPTION ---------");
        exception.printStackTrace(System.out);
    }


    public ATimeChartDataModel getModel(String name){
        if(models!=null){
            for(ATimeChartDataModel dm:models){
                if(dm.getName().equals(name))return dm;
            }
        }
        return null;
    }

}
