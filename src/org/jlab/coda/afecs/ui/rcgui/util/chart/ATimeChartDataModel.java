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

import com.cosylab.gui.components.spikechart.*;

public class ATimeChartDataModel extends AbstractDataModel {
        private PointCollection points= new PointCollection();
        private PointCollector collector = new PointCollector();

    /**
         * RandomTrendDataModel constructor comment.
         * @param name of the data channel
         */
        public ATimeChartDataModel(String name){
            super();
            super.name=name;
        }
        public PointIterator getPointIterator() {
            return this;
        }

        public boolean hasNext() {
            return points.size()>0;
        }
        public synchronized void generatePoint(double data) {

            points.add(collector.newPoint(System.currentTimeMillis()/1000.0, data));
        }
        public synchronized Point next() {
            collector.recyclePoint(point);
            point= points.removeFirst();
            return point;
        }
        /**
         * @see com.cosylab.gui.components.spikechart.ChartDataSource#getPointCount()
         */
        public int getPointCount() {
            return points.size();
        }



}
