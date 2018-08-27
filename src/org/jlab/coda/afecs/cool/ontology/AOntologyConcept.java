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
import java.util.ArrayList;

public class AOntologyConcept implements Serializable {

    private ArrayList<AOntologySlot> slots = new ArrayList<AOntologySlot>();

    public void setConceptName(String name) {
        String name1 = name;
    }

    public void addSlot(AOntologySlot slot) {
        this.slots.add(slot);
    }

    public void setOntology(String ontology) {
        String ontology1 = ontology;
    }

    /**
     *
     * @param name of the slot
     * @param cardinality single or multiple ( 1 is single, 2 is multiple)
     * @param optional true = optional , false = mandatory
     * @param objType the class name of the Java implementation of the primitive types.
     */
    public void addPrimitiveSlot(String name, int cardinality, boolean optional, String objType){
        AOntologySlot os = new AOntologySlot();
        os.setName(name);
        os.setCardinality(cardinality);
        os.setOptional(optional);
        os.setJavaObject(true);
        os.setJavaObjectType(objType);
        addSlot(os);
    }

    /**
     *
     * @param name of the slot
     * @param cardinality single or multiple ( 1 is single, 2 is multiple)
     * @param optional true = optional , false = mandatory
     * @param objType class name implementing concept
     */
    public void addConceptSlot(String name, int cardinality, boolean optional, String objType){
        AOntologySlot os = new AOntologySlot();
        os.setName(name);
        os.setCardinality(cardinality);
        os.setOptional(optional);
        os.setOntologyConcept(true);
        os.setConceptType(objType);
        addSlot(os);
    }


}
