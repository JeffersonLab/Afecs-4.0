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

package org.jlab.coda.afecs.cool.ontology;

import org.jlab.coda.afecs.system.AConstants;

import java.io.Serializable;

/**
 * <p>
 *     Afecs ontology Channel definition.
 *
 * </p>
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 3.x
 */

public class AChannel extends AOntologyConcept implements Serializable {
    private String name;
    private Object value;
    private Object minValue;
    private Object maxValue;
    private Object setValue;
    private int valueType;
    private String unit;
    private String description;

    public AChannel() {
        setOntology("afecs");
        setConceptName("Channel");

        addPrimitiveSlot("hasName", 1, false, "String");
        addConceptSlot("hasValue", 1, true, "Object");
        addConceptSlot("hasMinValue", 1, true, "Object");
        addConceptSlot("hasMaxValue", 1, true, "Object");
        addConceptSlot("hasSetValue", 1, true, "Object");
        addPrimitiveSlot("hasValueType", 1, true, "Integer");
        addPrimitiveSlot("hasUnit", 1, true, "String");
        addPrimitiveSlot("hasDescription", 1, true, "String");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setMinValue(Object minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(Object maxValue) {
        this.maxValue = maxValue;
    }

    public Object getSetValue() {
        return setValue;
    }

    public void setSetValue(Object setValue) {
        this.setValue = setValue;
    }

    public int getValueType() {
        return valueType;
    }

    public void setValueType(int valueType) {
        this.valueType = valueType;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("channel name           = ").append(name).append("\n");
        sb.append("--------------------------------\n");
        String type = AConstants.udf;
        switch (valueType) {
            case AConstants.BYTE:
                type = "byte";
                sb.append("channel current value  = ").append(value).append("\n");
                break;
            case AConstants.BYTE_ARRAY:
                type = "byte array";
                byte[] v = (byte[]) value;
                sb.append("channel current value  = ");
                for (byte aV : v) {
                    sb.append(aV).append(" ");
                }
                sb.append("\n");
                break;
            case AConstants.SHORT:
                type = "short";
                sb.append("channel current value  = ").append(value).append("\n");
                break;
            case AConstants.SHORT_ARRAY:
                type = "short array";
                short[] vs = (short[]) value;
                sb.append("channel current value  = ");
                for (short aV : vs) {
                    sb.append(aV).append(" ");
                }
                sb.append("\n");
                break;
            case AConstants.INT:
                type = "int";
                sb.append("channel current value  = ").append(value).append("\n");
                break;
            case AConstants.INT_ARRAY:
                type = "int array";
                int[] vi = (int[]) value;
                sb.append("channel current value  = ");
                for (int aV : vi) {
                    sb.append(aV).append(" ");
                }
                sb.append("\n");
                break;
            case AConstants.FLOAT:
                type = "float";
                sb.append("channel current value  = ").append(value).append("\n");
                break;
            case AConstants.FLOAT_ARRAY:
                type = "float array";
                float[] vf = (float[]) value;
                sb.append("channel current value  = ");
                for (float aV : vf) {
                    sb.append(aV).append(" ");
                }
                sb.append("\n");
                break;
            case AConstants.DOUBLE:
                type = "double";
                sb.append("channel current value  = ").append(value).append("\n");
                break;
            case AConstants.DOUBLE_ARRAY:
                type = "double array";
                double[] vd = (double[]) value;
                sb.append("channel current value  = ");
                for (double aV : vd) {
                    sb.append(aV).append(" ");
                }
                sb.append("\n");
                break;
            case AConstants.STRING:
                type = "string";
                sb.append("channel current value  = ").append(value).append("\n");
                break;
            case AConstants.STRING_ARRAY:
                type = "string array";
                String[] vss = (String[]) value;
                sb.append("channel current value  = ");
                for (String aV : vss) {
                    sb.append(aV).append(" ");
                }
                sb.append("\n");
                break;
        }
        sb.append("channel value type     = ").append(type).append("\n");
        sb.append("channel description    = ").append(description).append("\n");
        sb.append("channel value unit     = ").append(unit).append("\n");
        sb.append("channel min value      = ").append(minValue).append("\n");
        sb.append("channel max value      = ").append(maxValue).append("\n");
        sb.append("channel set value      = ").append(setValue).append("\n");
        return sb.toString();
    }



    @Override
    public boolean equals(Object obj) {
        boolean stat = false;
        if (obj instanceof AChannel) {
            stat = true;
            AChannel c1 = (AChannel) obj;
            if (c1.getName().equals(getName()) && c1.getValueType() == getValueType()) {
                switch (c1.getValueType()) {
                    case AConstants.BYTE:
                        Byte s1 = (Byte) c1.getSetValue();
                        Byte v1 = (Byte) getValue();
                        if (s1.byteValue() != v1.byteValue()) stat = false;
                        break;
                    case AConstants.SHORT:
                        Short s2 = (Short) c1.getSetValue();
                        Short v2 = (Short) getValue();
                        if (s2.shortValue() != v2.shortValue()) stat = false;
                        break;
                    case AConstants.INT:
                        Integer s3 = (Integer) c1.getSetValue();
                        Integer v3 = (Integer) getValue();
                        if (s3.intValue() != v3.intValue()) stat = false;
                        break;
                    case AConstants.DOUBLE:
                        Double s4 = (Double) c1.getSetValue();
                        Double v4 = (Double) getValue();
                        if (s4.doubleValue() != v4.doubleValue()) stat = false;
                        break;
                    case AConstants.FLOAT:
                        Float s5 = (Float) c1.getSetValue();
                        Float v5 = (Float) getValue();
                        if (s5.floatValue() != v5.floatValue()) stat = false;
                        break;
                    case AConstants.STRING:
                        if (!c1.getSetValue().equals(getValue())) stat = false;
                        break;
                    case AConstants.BYTE_ARRAY:
                        byte[] a1 = (byte[]) c1.getSetValue();
                        byte[] b1 = (byte[]) getValue();
                        for (int i = 0; i < a1.length; i++) {
                            if (a1[i] != b1[i]) stat = false;
                        }
                        break;
                    case AConstants.SHORT_ARRAY:
                        short[] a2 = (short[]) c1.getSetValue();
                        short[] b2 = (short[]) getValue();
                        for (int i = 0; i < a2.length; i++) {
                            if (a2[i] != b2[i]) stat = false;
                        }
                        break;
                    case AConstants.INT_ARRAY:
                        int[] a3 = (int[]) c1.getSetValue();
                        int[] b3 = (int[]) getValue();
                        for (int i = 0; i < a3.length; i++) {
                            if (a3[i] != b3[i]) stat = false;
                        }
                        break;
                    case AConstants.DOUBLE_ARRAY:
                        double[] a4 = (double[]) c1.getSetValue();
                        double[] b4 = (double[]) getValue();
                        for (int i = 0; i < a4.length; i++) {
                            if (a4[i] != b4[i]) stat = false;
                        }
                        break;
                    case AConstants.FLOAT_ARRAY:
                        float[] a5 = (float[]) c1.getSetValue();
                        float[] b5 = (float[]) getValue();
                        for (int i = 0; i < a5.length; i++) {
                            if (a5[i] != b5[i]) stat = false;
                        }
                        break;
                    case AConstants.STRING_ARRAY:
                        String[] a6 = (String[]) c1.getSetValue();
                        String[] b6 = (String[]) getValue();
                        for (int i = 0; i < a6.length; i++) {
                            if (!a6[i].equals(b6[i])) stat = false;
                        }
                        break;
                }
            }
        }
        return stat;
    }
}
