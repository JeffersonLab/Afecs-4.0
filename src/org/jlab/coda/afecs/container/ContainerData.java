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

package org.jlab.coda.afecs.container;

import org.jlab.coda.afecs.agent.AReportingTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *     Container data that contains the list of
 *     agent names deployed on this container.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */
public class ContainerData  implements Serializable {
    private AReportingTime reportingTime;
    private List<String> containerAgents= new ArrayList<>();

    public AReportingTime getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(AReportingTime reportingTime) {
        this.reportingTime = reportingTime;
    }

    public List<String> getContainerAgents() {
        return containerAgents;
    }

    public void setContainerAgents(List<String> containerAgents) {
        this.containerAgents = containerAgents;
    }

    public void addAgent(String n){
        containerAgents.add(n);
    }

}
