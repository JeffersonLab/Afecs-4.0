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

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>
 *     Afecs javaFX chart
 * </p>
 *
 * @author gurjyan
 * @version 3.x
 * @since 11/24/14
 */
public class AFxChart extends JFrame {
    private static final int MAX_DATA_POINTS = 50;

    private ConcurrentHashMap<String, XYChart.Series> Series =
            new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Number>> DataQs =
            new ConcurrentHashMap<>();

    private int xSeriesData = 0;
    private NumberAxis xAxis;
    private JFXPanel fxPanel;
    private String title;
    private String yTitle;

    /**
     * Constructor of the chart
     * @param title general title of the chart
     * @param yTitle yAxis title
     * @param sNames list of series names
     */
    public AFxChart(String title, String yTitle, List<String> sNames){
        this.title = title;
        this.yTitle = yTitle;
        init(sNames);
    }

    /**
     * <p>
     *     Initialization routine.
     *     Creates fx area chart with its own
     *     time0line and sets it in the swing JPanel.
     * </p>
     * @param sNames
     */
    public void init(final List<String> sNames) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        fxPanel = new JFXPanel();
        mainPanel.add(fxPanel, BorderLayout.CENTER);

        this.add(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800,300);

        Platform.setImplicitExit(false);

        create(sNames);

    }


    /**
     * <p>
     *     Basic initialization of the fx area chart and animation
     * </p>
     * @param sNames List of series names
     */
    public void create(List<String> sNames){
        for(String sn:sNames) {
            DataQs.put(sn, new ConcurrentLinkedQueue<Number>());
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                _initFX(fxPanel, title, yTitle);
                //-- Prepare TimeLine
                _prepareTimeLine();
            }
        });

    }

    /**
     * <p>
     *     Updates the data for a specific series
     * </p>
     * @param s the name of the series
     * @param data value to be shown on the chart
     */
    public void update(String s, Number data) {
            if(DataQs.containsKey(s)){
                DataQs.get(s).add(data);
            }
    }

    /**
     * <p>
     *     Clears data as well as series containers
     * </p>
     */
    public void reset(){
        DataQs.clear();
        Series.clear();
    }

    /**
     * <p>
     *     Gracefully exit...
     * </p>
     */
    public void exit(){
        reset();
        remove(fxPanel);
        dispose();
    }

    /**
     * <p>
     *     Core FX chart initialization routine.
     * </p>
     * @param fxPanel JFXPanel that can be added to the swing container
     * @param title  general title of the chart
     * @param yTitle yAxis title
     */
    private void _initFX(JFXPanel fxPanel, String title, String yTitle) {
        // minimum, maximum, how many xAxis steps the x number should be printed.
        xAxis = new NumberAxis(0,MAX_DATA_POINTS,MAX_DATA_POINTS/10);
        // if true will fix the 0, i.e always show 0 in the Y range
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        yAxis.setLabel(yTitle);

        //-- Chart
        final AreaChart<Number, Number> sc = new AreaChart<Number, Number>(xAxis, yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(Series<Number, Number> series,
                                         int itemIndex,
                                         Data<Number, Number> item) {}
        };

        // turning animation off as animation is designed for slower changing
        // data as it make that data animate on arrival. For this example that doesn't
        // make sense as the new data is arriving faster than the animation so it just
        // makes it look weird and also makes the data look wrong.
        sc.setAnimated(false);
        sc.setId("liveAreaChart");
        sc.setTitle(title);

        //-- Chart Series
        for(String sn: DataQs.keySet()){
            XYChart.Series serie = new AreaChart.Series<>();
            serie.setName(sn);
            sc.getData().add(serie);
            Series.put(sn, serie);
        }
        fxPanel.setScene(new Scene(sc));
    }

    /**
     * <p>
     *    TimeLine gets called in the JavaFX Main thread
     * </p>
     */
    private void _prepareTimeLine() {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    /**
     * <p>
     *     Adds 20 numbers to the plot for each series
     * </p>
     */
    private void addDataToSeries() {

        for (int i = 0; i < 20; i++) { //-- add 20 numbers to the plot+
            for (String sn : Series.keySet()) {

                ConcurrentLinkedQueue<Number> _dataQ = DataQs.get(sn);
                XYChart.Series _seria = Series.get(sn);


                if (_dataQ == null ||
                        _dataQ.isEmpty() ||
                        _seria == null) {
                    return;
                }
            }

            int xsd = xSeriesData++;
            for (String sn : Series.keySet()) {
                ConcurrentLinkedQueue<Number> _dataQ = DataQs.get(sn);
                XYChart.Series _seria = Series.get(sn);

                Number yd = _dataQ.remove();
                _seria.getData().add(new AreaChart.Data(xsd, yd));
            }

            // remove points to keep us at no more than MAX_DATA_POINTS
            for (String sn : Series.keySet()) {
                XYChart.Series _seria = Series.get(sn);
                if (_seria.getData().size() > MAX_DATA_POINTS) {
                    _seria.getData().remove(0,
                            _seria.getData().size() - MAX_DATA_POINTS);
                }
            }

            // update
            xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
            xAxis.setUpperBound(xSeriesData - 1);
        }
    }


    public static void main(String[] args) {
        final List<String> series = new ArrayList<>();
        series.add("simon");
        series.add("karo");
        series.add("alo");
//        series.add("blo");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final AFxChart st = new AFxChart("Data Rate","KByte/sec",series);
                new Thread() {
                    public void run() {
                        try {
                            while(true) {
                                for (String s : series) {
                                    st.update(s,Math.random());
                                }
                                sleep(100);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }
}
