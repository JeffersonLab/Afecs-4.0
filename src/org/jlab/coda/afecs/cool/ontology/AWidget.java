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

public class AWidget extends AOntologyConcept implements Serializable, Cloneable {
    private String name;
    private String title;
    private int number;
    private String type;
    private String subType;
    private String icon;
    private String editable;
    private ArrayList<AService> actionServices = new ArrayList<AService>();
    private ArrayList<AProcess> actionProcesses = new ArrayList<AProcess>();
    private ArrayList<AState> actionStates = new ArrayList<AState>();

    public AWidget(){
        // this is a concept of org.jlab.coda.afecs.cool.ontology
        setOntology("afecs");
        // the name of the concept
        setConceptName("Widget");
        // slot hasName
        addPrimitiveSlot("hasName",1,true,"String");
        // slot hasType
        addPrimitiveSlot("hasType",1,true,"String");
        // slot hasSubType
        addPrimitiveSlot("hasDescription",1,false,"String");
        addPrimitiveSlot("hasSubType",1,false,"String");
        // slot hasTitle
        addPrimitiveSlot("hasTitle",1,false,"String");
        // slot hasIcon
        addPrimitiveSlot("hasIcon",1,false,"String");
        // slot isEditable
        addPrimitiveSlot("isEditable",1,false,"String");
        // slot hasNumber
        addPrimitiveSlot("hasNumber",1,false,"Integer");
        // slot startsService
        addConceptSlot("startsService",2,false,"AService");
        // slot startsProcess
        addConceptSlot("startsProcess",2,false,"AProcess");
        // slot movesToStates
        addConceptSlot("moveToState",2,false,"AState");
    }

    protected Object clone() throws CloneNotSupportedException {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            if(AConstants.debug.get()) e.printStackTrace();
        }
        AWidget b = new AWidget();
        b.setName(getName());
        b.setType(getType());
        b.setSubType(getSubType());
        b.setTitle(getTitle());
        b.setIcon(getIcon());
        b.setEditable(getEditable());
        b.setNumber(getNumber());
        b.setActionServices(getActionServices());
        b.setActionProcesses(getActionProcesses());
        b.setActionStates(getActionStates());
        return b;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        String description1 = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getEditable() {
        return editable;
    }

    public void setEditable(String editable) {
        this.editable = editable;
    }


    public ArrayList<AService> getActionServices() {
        return actionServices;
    }

    public void setActionServices(ArrayList<AService> actionServices) {
        this.actionServices = actionServices;
    }


    public ArrayList<AProcess> getActionProcesses() {
        return actionProcesses;
    }

    public void setActionProcesses(ArrayList<AProcess> actionProcesses) {
        this.actionProcesses = actionProcesses;
    }

    public ArrayList<AState> getActionStates() {
        return actionStates;
    }

    public void setActionStates(ArrayList<AState> actionStates) {
        this.actionStates = actionStates;
    }

}
