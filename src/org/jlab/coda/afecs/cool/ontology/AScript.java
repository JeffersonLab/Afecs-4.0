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

public class AScript extends AOntologyConcept implements Serializable {
    private String name          = AConstants.udf;
    private String description   = AConstants.udf;
    private String commandString = AConstants.udf;
    private int    exitCode      = 777;
    private String sync          = AConstants.udf;
    private int    timeout;

    public AScript(){
        // this is a concept of org.jlab.coda.afecs.cool.ontology
        setOntology("afecs");
        // the name of the concept
        setConceptName("Script");
        addPrimitiveSlot("hasName",1,false,"String");
        addPrimitiveSlot("hasDescription",1,false,"String");
        addPrimitiveSlot("hasCommandString",1,false,"String");
        addPrimitiveSlot("hasExitCode",1,true,"String");
        addPrimitiveSlot("isSynchronous",1,true,"String");
        addPrimitiveSlot("hasTimeout",1,true,"String");
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getCommandString() {
        return commandString;
    }

    public void setCommandString(String commandString) {
        this.commandString = commandString;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "AScript{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", commandString='" + commandString + '\'' +
                ", exitCode=" + exitCode +
                ", sync='" + sync + '\'' +
                ", timeout=" + timeout +
                '}';
    }
}
