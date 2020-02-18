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

public class AProcess extends AOntologyConcept implements Serializable {
    private String name;
    private String before      = AConstants.udf;
    private String after       = AConstants.udf;
    private String status      = AConstants.udf;
    private String initiator   = AConstants.udf;
    private String description = AConstants.udf;
    private String critical    = AConstants.setoff;
    private String sync        = AConstants.setoff;
    private int periodicity;
    private int delay          = 3;
    private int timeout        = AConstants.TIMEOUT;
    private int requestid;
    private ArrayList<AScript> scripts = new ArrayList<AScript>();
    private ArrayList<APackage> sendPackages = new ArrayList<APackage>();
    private ArrayList<APackage> receivePackages = new ArrayList<APackage>();

    public AProcess(){
        // this is a concept of org.jlab.coda.afecs.cool.ontology
        setOntology("afecs");
        // the name of the concept
        setConceptName("Process");
        addPrimitiveSlot( "hasName",1,false,"String");
        addPrimitiveSlot( "hasDescription",1,true,"String");
        addPrimitiveSlot( "isCritical",1,true,"String");
        addPrimitiveSlot( "before",1,true,"String");
        addPrimitiveSlot( "after",1,true,"String");
        addPrimitiveSlot( "hasIpc",1,true,"String");
        addPrimitiveSlot( "hasPeriodicity",1,true,"Integer");
        addPrimitiveSlot( "hasDelay",1,true,"Integer");
        addPrimitiveSlot( "hasTimeOut",1,true,"Integer");
        addPrimitiveSlot( "hasRequestId",1,true,"Integer");
        addPrimitiveSlot( "status",1,true,"String");
        addPrimitiveSlot( "isInitiator",1,true,"String");
        addConceptSlot("runs",2,true, "AScript");
        addConceptSlot("sends",2,true, "APackage");
        addConceptSlot("receives",2,true, "APackage");
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(int periodicity) {
        this.periodicity = periodicity;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setRequestid(int requestid) {
        this.requestid = requestid;
    }

    public ArrayList<AScript> getScripts() {
        return scripts;
    }

    public void setScripts(ArrayList<AScript> scripts) {
        this.scripts = scripts;
    }

    public ArrayList<APackage> getSendPackages() {
        return sendPackages;
    }

    public void setSendPackages(ArrayList<APackage> sendPackages) {
        this.sendPackages = sendPackages;
    }

    public void addSendPackage(APackage p){
        this.sendPackages.add(p);
    }

    public ArrayList<APackage> getReceivePackages() {
        return receivePackages;
    }

    public void setReceivePackages(ArrayList<APackage> receivePackages) {
        this.receivePackages = receivePackages;
    }

    public void addReceivePackage(APackage p){
        this.receivePackages.add(p);
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCritical(String critical) {
        this.critical = critical;
    }


    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

    @Override
    public String toString() {
        return "AProcess{" +
                "name='" + name + '\'' +
                ", before='" + before + '\'' +
                ", after='" + after + '\'' +
                ", status='" + status + '\'' +
                ", initiator='" + initiator + '\'' +
                ", description='" + description + '\'' +
                ", critical='" + critical + '\'' +
                ", sync='" + sync + '\'' +
                ", periodicity=" + periodicity +
                ", delay=" + delay +
                ", timeout=" + timeout +
                ", requestid=" + requestid +
                ", scripts=" + scripts +
                ", sendPackages=" + sendPackages +
                ", receivePackages=" + receivePackages +
                '}';
    }
}
