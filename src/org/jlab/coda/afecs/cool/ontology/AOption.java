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

public class AOption extends AOntologyConcept implements Serializable {
    private String name           = AConstants.udf;
    private String description    = AConstants.udf;
    private String dataFile       = AConstants.udf;
    private String dataFilePrefix = AConstants.udf;
    private String coda2Component = AConstants.udf;
    private String autoStart      = AConstants.udf;
    private String startRun       = AConstants.udf;
    private String configFile     = AConstants.udf;
    private String configString   = AConstants.udf;
    private String downloadString = AConstants.udf;
    private String prestartString = AConstants.udf;
    private String goString       = AConstants.udf;
    private String endString      = AConstants.udf;
    private int dataLimit;
    private int eventLimit;

    public AOption(){
        // this is a concept of org.jlab.coda.afecs.cool.ontology
        setOntology("afecs");
        setConceptName("Option");

        addPrimitiveSlot( "hasName",          1,false,"String");
        addPrimitiveSlot( "hasDataFile",      1,true,"String");
        addPrimitiveSlot( "hasDescription",   1,true,"String");
        addPrimitiveSlot( "hasDataFilePrefix",1,true,"String");
        addPrimitiveSlot( "hasCoda2Component",1,true,"String");
        addPrimitiveSlot( "hasAutoStart",     1,true,"String");
        addPrimitiveSlot( "hasStartRun",      1,true,"String");
        addPrimitiveSlot( "hasConfigFile",    1,true,"String");
        addPrimitiveSlot( "hasDataLimit",     1,true,"Integer");
        addPrimitiveSlot( "hasEventLimit",    1,true,"Integer");
        addPrimitiveSlot( "hasConfigFile",    1,true,"String");
        addPrimitiveSlot( "hasConfigString",  1,true,"String");
        addPrimitiveSlot( "hasDownloadString",1,true,"String");
        addPrimitiveSlot( "hasPrestartString",1,true,"String");
        addPrimitiveSlot( "hasGoString",      1,true,"String");
        addPrimitiveSlot( "hasEndString",     1,true,"String");
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public void setDataFilePrefix(String dataFilePrefix) {
        this.dataFilePrefix = dataFilePrefix;
    }

    public void setAutoStart(String autoStart) {
        this.autoStart = autoStart;
    }

    public void setStartRun(String startRun) {
        this.startRun = startRun;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public int getDataLimit() {
        return dataLimit;
    }

    public void setDataLimit(int dataLimit) {
        this.dataLimit = dataLimit;
    }

    public int getEventLimit() {
        return eventLimit;
    }

    public void setEventLimit(int eventLimit) {
        this.eventLimit = eventLimit;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoda2Component(String coda2Component) {
        this.coda2Component = coda2Component;
    }

    public String getConfigString() {
        return configString;
    }

    public void setConfigString(String configString) {
        this.configString = configString;
    }

    public String getDownloadString() {
        return downloadString;
    }

    public void setDownloadString(String downloadString) {
        this.downloadString = downloadString;
    }

    public String getEndString() {
        return endString;
    }

    public void setEndString(String endString) {
        this.endString = endString;
    }

    public String getGoString() {
        return goString;
    }

    public void setGoString(String goString) {
        this.goString = goString;
    }

    public String getPrestartString() {
        return prestartString;
    }

    public void setPrestartString(String prestartString) {
        this.prestartString = prestartString;
    }
}

