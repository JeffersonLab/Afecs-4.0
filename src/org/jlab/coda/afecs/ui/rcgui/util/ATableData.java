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

public class ATableData {
    private String name;
    private String columnName;
    private int type;
    private Object[] values;
    private Object value;
    private Float[] floatArrayOfValues;
    private Float floatOfValue;
    private String[] stringArrayOfValues;
    private String stringOfValue;


    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }


    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Float[] getFloatArrayOfValues() {
        return floatArrayOfValues;
    }

    public void setFloatArrayOfValues(Float[] floatArrayOfValues) {
        this.floatArrayOfValues = floatArrayOfValues;
    }

    public Float getFloatOfValue() {
        return floatOfValue;
    }

    public void setFloatOfValue(Float floatOfValue) {
        this.floatOfValue = floatOfValue;
    }

    public String[] getStringArrayOfValues() {
        return stringArrayOfValues;
    }

    public void setStringArrayOfValues(String[] stringArrayOfValues) {
        this.stringArrayOfValues = stringArrayOfValues;
    }

    public String getStringOfValue() {
        return stringOfValue;
    }

    public void setStringOfValue(String stringOfValue) {
        this.stringOfValue = stringOfValue;
    }

    public String toString(){
        return "name = "+name+"\n columnName = "+columnName+"\ntype = "+type;
    }
}
