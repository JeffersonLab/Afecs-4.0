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

public class APanel extends AOntologyConcept implements Serializable, Cloneable {
    private String name;
    private String description;
    private String color;
    private String title;
    private ArrayList<AWidget> widgets = new ArrayList<AWidget>();
    private int number;

    public APanel(){
         // this is a concept of org.jlab.coda.afecs.cool.ontology
         setOntology("afecs");
         // the name of the concept
         setConceptName("Panel");
         // slot hasName
         addPrimitiveSlot( "hasName",1,true,"String");
         // slot hasColor
         addPrimitiveSlot( "hasColor",1,false,"String");
         addPrimitiveSlot( "hasDescription",1,false,"String");
         // slot hasTitle
         addPrimitiveSlot( "hasTitle",1,false,"String");
         // slot hasNumber
         addPrimitiveSlot( "hasNumber",1,false,"String");
         // slot widget
         addConceptSlot( "hasWidget",2,false,"AWidget");
     }

    protected Object clone() throws CloneNotSupportedException {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            if(AConstants.debug.get()) e.printStackTrace();
        }
        APanel b = new APanel();
        b.setName(getName());
        b.setColor(getColor());
        b.setTitle(getTitle());
        b.setNumber(getNumber());
        return b;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setWidgets(ArrayList<AWidget> widgets) {
        this.widgets = widgets;
    }

}
