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

package org.jlab.coda.afecs.fcs;

import org.jlab.coda.afecs.system.AConstants;

import java.util.ArrayList;

/**
 * Describe...
 *
 * @author gurjyan
 *         Date: 1/6/14 Time: 1:48 PM
 * @version 4.x
 */
public class FCSConfigData {
    private String commandTemplate = AConstants.udf;
    private String inputEtName = AConstants.udf;
    private String inputEtHost = AConstants.udf;
    private int    inputEtPort;
    private String outputEtName = AConstants.udf;
    private String outputEtHost = AConstants.udf;
    private int    outputEtPort;
    private ArrayList<String> nodes = new ArrayList<String>();

    public void setCommandTemplate(String commandTemplate) {
        this.commandTemplate = commandTemplate;
    }

    public String getInputEtName() {
        return inputEtName;
    }

    public void setInputEtName(String inputEtName) {
        this.inputEtName = inputEtName;
    }

    public String getInputEtHost() {
        return inputEtHost;
    }

    public void setInputEtHost(String inputEtHost) {
        this.inputEtHost = inputEtHost;
    }

    public int getInputEtPort() {
        return inputEtPort;
    }

    public void setInputEtPort(int inputEtPort) {
        this.inputEtPort = inputEtPort;
    }

    public String getOutputEtName() {
        return outputEtName;
    }

    public void setOutputEtName(String outputEtName) {
        this.outputEtName = outputEtName;
    }

    public String getOutputEtHost() {
        return outputEtHost;
    }

    public void setOutputEtHost(String outputEtHost) {
        this.outputEtHost = outputEtHost;
    }

    public int getOutputEtPort() {
        return outputEtPort;
    }

    public void setOutputEtPort(int outputEtPort) {
        this.outputEtPort = outputEtPort;
    }

    public void addNode(String n){
        nodes.add(n);
    }

    @Override
    public String toString() {
        return "FCSConfigData{" +
                "commandTemplate='" + commandTemplate + '\'' +
                ", inputEtName='" + inputEtName + '\'' +
                ", inputEtHost='" + inputEtHost + '\'' +
                ", inputEtPort=" + inputEtPort +
                ", outputEtName='" + outputEtName + '\'' +
                ", outputEtHost='" + outputEtHost + '\'' +
                ", outputEtPort=" + outputEtPort +
                ", nodes=" + nodes +
                '}';
    }
}

