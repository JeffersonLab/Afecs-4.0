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

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AScrollSlider extends Scrollbar {
    private final static int DEFAULT_RANGE = 10; // number of descrete steps
    private final static int DEFAULT_VISIBLE = 1; // pixel width of thumb
    private double curFloat, minFloat, maxFloat;
    private boolean isLogScale;

    /**
     * constructs a FloatSlider using a given number of slider positions.
     * @param orientation - Scrollbar.VERTICAL or Scrollbar.HORIZONTAL.
     * @param cur - real valued initial value.
     * @param vis - same as in Scrollbar base class.
     * @param min - real valued range minimum.
     * @param max - real valued range maximum.
     * @param res - number of descrete slider positions.
     * @param log - log scale if true, linear otherwise.
     */
    public AScrollSlider(int orientation, double cur, int vis, double min, double max, int res, boolean log) {
        super(orientation, 0, vis, 0, res+vis);
        isLogScale = log;
        setAll(min, max, cur);
        addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent ae) {
                int
                    ival = AScrollSlider.super.getValue(),
                    vis = AScrollSlider.super.getVisibleAmount(),
                    min = AScrollSlider.super.getMinimum(),
                    max = AScrollSlider.super.getMaximum();
                double dval = transformRange(false,      min,      max-vis,  ival,
                                             isLogScale, minFloat, maxFloat);
                //System.out.println("getting: ival="+ival+" -> dval="+dval);
                setFloatValue(dval);
            }
        });
    }
    /**
     * uses default scale (linear).
     * @param orientation - Scrollbar.VERTICAL or Scrollbar.HORIZONTAL.
     * @param cur - real valued initial value.
     * @param vis - same as in Scrollbar base class.
     * @param min - real valued range minimum.
     * @param max - real valued range maximum.
     * @param res - number of descrete slider positions.
     */
    public AScrollSlider(int orientation, double cur, int vis, double min, double max, int res) {
        this(orientation, cur, vis, min, max, res, false);
    }

    /**
     * returns the closest integer in the range of the actual int extents of the base Scrollbar.
     */
    protected int rangeValue(double dval) {
        dval = clamp(dval, minFloat, maxFloat);
        int
            vis = super.getVisibleAmount(),
            min = super.getMinimum(),
            max = super.getMaximum();
        int ival = (int)Math.round(
                   transformRange(isLogScale, minFloat, maxFloat, dval,
                                  false,      min,      max-vis));
        //System.out.println("setting: dval="+dval+" -> ival="+ival);
        return ival;
    }

    public double getFloatMinimum() {
        return minFloat;
    }
    public double getFloatMaximum() {
        return maxFloat;
    }
    public double getFloatValue() {
        return curFloat;
    }

    public void setFloatValue(double newcur) {
        // update the model
        curFloat = newcur;
        // update the view
        super.setValues(rangeValue(newcur),
                        super.getVisibleAmount(),
                        super.getMinimum(),
                        super.getMaximum());
    }

    public void setAll(double newmin, double newmax, double newcur) {
        minFloat = newmin;
        maxFloat = newmax;
        setFloatValue(newcur);
    }

    private static double clamp(double x, double a, double b)
    {
        return x <= a ? a :
               x >= b ? b : x;
    }
    // linear interpolation
    private static double lerp(double a, double b, double t)
    {
        return a + (b-a) * t;
    }
    // geometric interpolation
    private static double gerp(double a, double b, double t)
    {
        return a * Math.pow(b/a, t);
    }
    // interpolate between A and B (linearly or geometrically)
    // by the fraction that x is between a and b (linearly or geometrically)
    private static double
    transformRange(boolean isLog, double a, double b, double x,
                   boolean IsLog, double A, double B)

    {
        if (isLog)
        {
            a = Math.log(a);
            b = Math.log(b);
            x = Math.log(x);
        }
        double t = (x-a) / (b-a);
        double X = IsLog ? gerp(A,B,t)
                         : lerp(A,B,t);
        return X;
    }

    /**
     * simple test program for ScrollSlider class.
     */
    public static void main(String args[]) {
        Frame frame = new Frame("ScrollSlider example");
        final AScrollSlider rslider = new AScrollSlider(Scrollbar.HORIZONTAL, 1.0f, 1,0.0f, 10.0f, 20);
        final Label curValue = new Label("Interval in sec.: " + rslider.getFloatValue());
        rslider.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent ae) {
                curValue.setText("Interval in sec.: " + rslider.getFloatValue());
            }
        });
        Container mainpanel = new Panel();
        mainpanel.setLayout(new GridLayout(3, 1));
        mainpanel.add(new Label("Range: " + rslider.getFloatMinimum() + " -> " + rslider.getFloatMaximum()));
        mainpanel.add(rslider);
        mainpanel.add(curValue);
        frame.add(mainpanel);
        frame.setSize(new Dimension(800, 100));
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(1);
            }
        });
        frame.setVisible(true);
    }

}
