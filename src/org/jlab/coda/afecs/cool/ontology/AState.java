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

public class AState extends AOntologyConcept implements Serializable {
    private String name, description;
    private ArrayList<AProcess> processes = new ArrayList<AProcess>();

    public AState(){
        // this is a concept of org.jlab.coda.afecs.cool.ontology
        setOntology("afecs");
        // the name of the concept
        setConceptName("State");
        //slot hasName
        addPrimitiveSlot("hasName",1,false,"String");
        //slot hasDescription
        addPrimitiveSlot("hasDescription",1,true,"String");
        // slot achievedThrough
        addConceptSlot("achievedThrough",2,true, "AProcess");
        //slot belongsTo
        addPrimitiveSlot("addressedTo",2,true,"String");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ArrayList<AProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(ArrayList<AProcess> processes) {
        this.processes = processes;
    }

    public void addProcess(AProcess p){
        this.processes.add(p);        
    }


    /**
     * sets the names of all components that will get thus state change request
     * @param componentNames ArrayList of component names.
     */
    public void setComponentNames(ArrayList<String> componentNames) {
        ArrayList<String> componentNames1 = componentNames;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
