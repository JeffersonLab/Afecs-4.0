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

public class ALink extends AOntologyConcept implements Serializable {
    private String name                     = AConstants.udf;
    private String description              = AConstants.udf;
    private String transport                = "ET";
    private String sourceComponentName      = AConstants.udf;
    private String sourceHost               = AConstants.udf;
    private int    sourcePort;
    private String destinationComponentName = AConstants.udf;
    private String destinationHost          = AConstants.udf;
    private int    destinationPort;
    private int    destinationETNumberEvents;
    private int    destinationETEventSize;

    public ALink(){
        setOntology("afecs");
        setConceptName(   "ALink");
        addPrimitiveSlot( "hasName",                   1, true, "String");
        addPrimitiveSlot( "hasDescription",            1, false,"String");
        addPrimitiveSlot( "hasTransport",              1, false,"String");
        addPrimitiveSlot( "sourceComponentName",       1, false,"String");
        addPrimitiveSlot( "sourceHost",                1, false,"String");
        addPrimitiveSlot( "sourcePort",                1, false,"Integer");
        addPrimitiveSlot( "destinationComponentName",  1, false,"String");
        addPrimitiveSlot( "destinationHost",           1, false,"String");
        addPrimitiveSlot( "destinationPort",           1, false,"Integer");
        addPrimitiveSlot( "destinationETNumberEvents", 1, false,"Integer");
        addPrimitiveSlot( "destinationETEventSize",    1, false,"Integer");
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDestinationComponentName(String destinationComponentName) {
        this.destinationComponentName = destinationComponentName;
    }

    public void setDestinationETEventSize(int destinationETEventSize) {
        this.destinationETEventSize = destinationETEventSize;
    }

    public void setDestinationETNumberEvents(int destinationETNumberEvents) {
        this.destinationETNumberEvents = destinationETNumberEvents;
    }

    public void setDestinationHost(String destinationHost) {
        this.destinationHost = destinationHost;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public void setSourceComponentName(String sourceComponentName) {
        this.sourceComponentName = sourceComponentName;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public void setEndX(double endX) {
        double endX1 = endX;
    }

    public void setEndY(double endY) {
        double endY1 = endY;
    }

    public void setStartX(double startX) {
        double startX1 = startX;
    }

    public void setStartY(double startY) {
        double startY1 = startY;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTransport() {
        return transport;
    }

    public String getSourceComponentName() {
        return sourceComponentName;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public String getDestinationComponentName() {
        return destinationComponentName;
    }

    public String getDestinationHost() {
        return destinationHost;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public int getDestinationETNumberEvents() {
        return destinationETNumberEvents;
    }

    public int getDestinationETEventSize() {
        return destinationETEventSize;
    }
}
