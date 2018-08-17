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

import java.io.Serializable;

public class AOntologySlot implements Serializable {

    private String name;
    private boolean isOptional;
    private boolean isJavaObject;
    private boolean isOntologyConcept;
    private int cardinality;
    private String javaObjectType;
    private String conceptType;

    public void setName(String name) {
        this.name = name;
    }


    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    public void setJavaObjectType(String javaObjectType) {
        this.javaObjectType = javaObjectType;
    }

    public void setConceptType(String conceptType) {
        this.conceptType = conceptType;
    }

    public void setJavaObject(boolean javaObject) {
        isJavaObject = javaObject;
    }

    public void setOntologyConcept(boolean ontologyConcept) {
        isOntologyConcept = ontologyConcept;
    }

}
