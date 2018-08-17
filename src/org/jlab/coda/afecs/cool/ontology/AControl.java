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
import java.util.ArrayList;

public class AControl extends AOntologyConcept implements Serializable, Cloneable{
    private ArrayList<AComponent> components = new ArrayList<AComponent>();
    private AComponent supervisor;
    private String name;
    private HMI gui;
    private AOption option;

    public AControl(){
        // this is a concept of org.jlab.coda.afecs.cool.ontology
        setOntology("afecs");
        // the name of the concept
        setConceptName("Control");
        // slot hasName
        addPrimitiveSlot("hasName",1,false,"String");
        // slot hasComponent
        addConceptSlot("hasComponent",2,true, "AComponent");
        // slot hasSupervisor
        addConceptSlot("hasSupervisor",1,true, "AComponent");
        addConceptSlot("hasHmi",1,true, "HMI");
        addConceptSlot("hasOption",1,true, "AOption");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            if(AConstants.debug.get()) e.printStackTrace();
        }
        AControl ac = new AControl();
        ac.setName(name);
        ac.setComponents(components);
        ac.setSupervisor(supervisor);
        ac.setOption(option);
        return ac;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<AComponent> getComponents() {
        return components;
    }

    public void setComponents(ArrayList<AComponent> components) {
        this.components = components;
    }

    public AComponent getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(AComponent supervisor) {
        this.supervisor = supervisor;
    }

    public void setGui(HMI gui) {
        this.gui = gui;
    }

    public AOption getOption() {
        return option;
    }

    public void setOption(AOption option) {
        this.option = option;
    }
}
