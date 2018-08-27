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

public class APackage extends AOntologyConcept implements Serializable {
    private String name;
    private String isForRcClient      = AConstants.udf;
    private String isForNativecMsg    = AConstants.udf;
    private String sendSubject        = AConstants.udf;
    private String sendType           = AConstants.udf;
    private String sendText           = AConstants.udf;
    private String receivedSubject    = AConstants.udf;
    private String receivedType       = AConstants.udf;
    private ArrayList<String> receivedText = new ArrayList<String>();
    private String definesState       = AConstants.udf;
    private ArrayList<AChannel> channels = new ArrayList<AChannel>();

    public APackage(){
        // this is a concept of org.jlab.coda.afecs.cool.ontology
        setOntology("afecs");
        // the name of the concept
        setConceptName("Package");
        addPrimitiveSlot("hasName",1,false,"String");
        addPrimitiveSlot("hasDescription",1,true,"String");
        addPrimitiveSlot("isForRcClient",1,true,"String");
        addPrimitiveSlot("isForNativecMsg",1,true,"String");
        addPrimitiveSlot("isForShellClient",1,true,"String");
        addPrimitiveSlot("hasSendSubject",1,true,"String");
        addPrimitiveSlot("hasSendType",1,true,"String");
        addPrimitiveSlot("hasSendText",1,true,"String");
        addPrimitiveSlot("hasReceivedSubject",1,true,"String");
        addPrimitiveSlot("hasReceivedType",1,true,"String");
        addPrimitiveSlot("hasReceivedText",2,true,"String");
        addPrimitiveSlot("definesState",1,true,"String");
        addConceptSlot("hasChannel",2,true, "AChannel");
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getForRcClient() {
        return isForRcClient;
    }

    public void setForRcClient(String forRcClient) {
        this.isForRcClient = forRcClient;
    }

    public String getForNativecMsg() {
        return isForNativecMsg;
    }

    public void setForNativecMsg(String forNativecMsg) {
        isForNativecMsg = forNativecMsg;
    }

    public void setForShellClient(String forShellClient) {
        String isForShellClient = forShellClient;
    }

    public ArrayList<AChannel> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<AChannel> channels) {
        this.channels = channels;
    }

    public void addChannel(AChannel c){
        this.channels.add(c);
    }

    public void setDescription(String description) {
        String description1 = description;
    }


    public String getSendSubject() {
        return sendSubject;
    }

    public void setSendSubject(String sendSubject) {
        this.sendSubject = sendSubject;
    }

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public String getSendText() {
        return sendText;
    }

    public void setSendText(String sendText) {
        this.sendText = sendText;
    }

    public String getReceivedSubject() {
        return receivedSubject;
    }

    public void setReceivedSubject(String receivedSubject) {
        this.receivedSubject = receivedSubject;
    }

    public String getReceivedType() {
        return receivedType;
    }

    public void setReceivedType(String receivedType) {
        this.receivedType = receivedType;
    }

    public ArrayList<String> getReceivedText() {
        return receivedText;
    }

    public void setReceivedText(ArrayList<String> receivedText) {
        this.receivedText = receivedText;
    }


}
