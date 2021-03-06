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

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Describe...
 *
 * @author gurjyan
 *         Date: 7/14/14 Time: 3:19 PM
 * @version 4.x
 */
public class FxAnimatedBarChart extends JFrame {
    private int width, height;
    private String xTitle, yTitle;
    private HashMap<String, Double> data;
    private boolean update = true;
    private int updateRate = 1000;
    private String title;

    JFXPanel fxPanel;

    public FxAnimatedBarChart(String title,
                              HashMap<String, Double> data,
                              int width, int height,
                              String xTitle, String yTitle, int updateRate,
                              boolean isYFixed, double yMax) {

        this.title = title;
        this.updateRate = updateRate;
        this.data = data;
        this.width = width;
        this.height = height;
        this.xTitle = xTitle;
        this.yTitle = yTitle;

        initSwingComponents();
        initFxComponents(isYFixed, yMax);
    }

    public void stopUpdating(){
        update = false;
    }

    public void destroy(){
        stopUpdating();
        dispose();
    }

    private void initSwingComponents(){

        JPanel mainPanel = new JPanel(new BorderLayout());
        fxPanel = new JFXPanel();
        mainPanel.add(fxPanel, BorderLayout.CENTER);

//        JLabel titleLabel = new JLabel("Charts in Swing applications");
//        mainPanel.add(titleLabel, BorderLayout.NORTH);

        this.add(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(width,height);
    }

    private void initFxComponents(final boolean isYFixed, final double yMax) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GridPane grid = new GridPane();
                Scene scene = new Scene(grid, width, height);

                /**
                 * Construct and populate Bar chart.
                 * It uses single series of data.
                 */
                NumberAxis lineYAxis;

                if (isYFixed) {
                    lineYAxis = new NumberAxis(0.0, yMax, yMax / 10.0);
                    lineYAxis.setLabel(yTitle);
                    lineYAxis.setAnimated(false);
                    lineYAxis.setAutoRanging(false);

                } else {
                    lineYAxis = new NumberAxis(0, 1_000, 0);
                    lineYAxis.setLabel(yTitle);
                    lineYAxis.setAnimated(true);
                    lineYAxis.setAutoRanging(true);
                }

                CategoryAxis lineXAxis = new CategoryAxis();
                lineXAxis.setAnimated(false);
//                lineXAxis.tickLabelFontProperty().set(Font.font(6));// works for java-8 only
                lineXAxis.setLabel(xTitle);
                final BarChart<String, Number> barChart = new BarChart<>(lineXAxis, lineYAxis);
                barChart.setCategoryGap(0);
                barChart.setBarGap(0.1);
                barChart.setPrefWidth(1600.0);
                barChart.setLegendVisible(false);
                barChart.setTitle(title);

                final XYChart.Series bar1 = new XYChart.Series<>();


                for (String m : data.keySet()) {
                    bar1.getData().add(new XYChart.Data<>(m, data.get(m)));
                }
                barChart.getData().addAll(bar1);

                grid.setVgap(2);
                grid.setHgap(0);
                grid.add(barChart, 2, 0);
                fxPanel.setScene(scene);

                final Timeline tl = new Timeline();
                tl.getKeyFrames().add(
                        new KeyFrame(Duration.millis(updateRate),
                                new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        for (XYChart.Series<String, Number> series : barChart.getData()) {
                                            for (XYChart.Data<String, Number> d : series.getData()) {
                                                if(d!=null && data!=null && d.getXValue()!=null && data.get(d.getXValue())!=null) {
                                                    d.setYValue(data.get(d.getXValue()));
                                                }
                                            }
                                            if (!update) tl.stop();
                                        }
                                    }
                                }
                        ));
                tl.setCycleCount(Animation.INDEFINITE);
                tl.setAutoReverse(true);
                tl.play();

            }
        });

    }
}