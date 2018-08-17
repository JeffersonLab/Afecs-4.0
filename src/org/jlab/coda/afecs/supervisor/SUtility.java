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

package org.jlab.coda.afecs.supervisor;

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.AfecsTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Class providing utility methods
 * for Supervisor agent.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/11/14 Time: 2:51 PM
 * @version 3.x
 */
public class SUtility {

    private SupervisorAgent owner;

    public SUtility(SupervisorAgent owner) {
        this.owner = owner;
    }

    /**
     * <p>
     * Supervisor local register (maps) cleanup
     * Stops active service execution thread.
     * Stops agent states watching thread.
     * Clears all internal registration storage.
     * </p>
     */
    public void clearLocalRegister() {

        if (owner.sortedComponentList != null &&
                !owner.sortedComponentList.isEmpty()) {
            owner.sortedComponentList.clear();
        }

        if (owner.myServices != null &&
                !owner.myServices.isEmpty()) {
            owner.myServices.clear();
        }

        if (owner.myServiceConditions != null &&
                !owner.myServiceConditions.isEmpty()) {
            owner.myServiceConditions.clear();
        }

        if (owner.myComponents != null &&
                !owner.myComponents.isEmpty()) {
            owner.myComponents.clear();
        }

        if (owner.myCompReportingTimes != null &&
                !owner.myCompReportingTimes.isEmpty()) {
            owner.myCompReportingTimes.clear();
        }
    }


    /**
     * <p>
     * Block and wait until components reset
     * </p>
     */
    public synchronized void blockingWait4Reset(boolean send, int sec) {
        boolean allSet = true;
        int trial = 0;
        owner.isResetting.set(true);
        while (trial < sec * 10) {
            allSet = true;
            for (AComponent c : owner.myComponents.values()) {
                if (c.getState().equals(AConstants.disconnected)) {
                    owner.me.setState(AConstants.booted);
                    owner.send(AConstants.GUI,
                            owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                            owner.me.getRunTimeDataAsPayload());
                    owner.isResetting.set(false);
                    return;
                } else if (c.getState().equalsIgnoreCase(AConstants.failed)) {

                    owner.me.setState(AConstants.failed);
                    owner.send(AConstants.GUI,
                            owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                            owner.me.getRunTimeDataAsPayload());
                    owner.isResetting.set(false);
                    return;
                } else if (c.getState().contains("ing")) {
                    allSet = false;
                    break;
                }
            }
            trial++;
            AfecsTool.sleep(100);
        }

        if (allSet) {
            owner.me.setState(AConstants.configured);
            if (send)
                owner.send(AConstants.GUI,
                        owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                        owner.me.getRunTimeDataAsPayload());
        } else {
            owner.me.setState(AConstants.failed);
            owner.send(AConstants.GUI,
                    owner.me.getSession() + "_" + owner.me.getRunType() + "/supervisor",
                    owner.me.getRunTimeDataAsPayload());
        }
        owner.isResetting.set(false);
    }


    /**
     * Checks reporting components map to see if
     * any of the components have a required state.
     * </p>
     *
     * @param state required state
     * @return true if found
     */
    public boolean containsState(String state) {
        for (AComponent c : owner.getMyComponents().values()) {
            if (c.getState().equals(state)) return true;
        }
        return false;
    }

    /**
     * Checks reporting components map to see if
     * any of the components have a required state.
     * </p>
     *
     * @param states required states
     * @return true if found
     */
    public boolean containsStates(List<String> states) {
        String state;
        for (AComponent c : owner.getMyComponents().values()) {
            state = c.getState();
            for (String s : states) {
                if (s.equals(state)) return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Returns the component that is the master roc of the configuration,
     * i.e. is the trigger source. Note that compareTo method of the
     * AComponent class is designed to sort lists in ascending order. So,
     * to get highest priority roc we have to get the last roc in the sorted list.
     * </p>
     *
     * @return AComponent object reference
     */
    public AComponent getTriggerSourceComponent() {

        AComponent gtComponent = null;
        List<AComponent> rocComponents = new ArrayList<>();

        for (AComponent com : owner.myComponents.values()) {
            if (com.getType().equals(ACodaType.TS.name())) {
                return com;
            } else if (com.getType().equals(ACodaType.GT.name())) {
                gtComponent = com;
            } else if (com.getType().equals(ACodaType.ROC.name())) {
                rocComponents.add(com);
            }
        }
        if (gtComponent != null) {
            return gtComponent;
        }

        if (!rocComponents.isEmpty()) {
            Collections.sort(rocComponents);
            return rocComponents.get(rocComponents.size() - 1);
        }

        // No trigger source
        // component was found
        return null;
    }

    /**
     * <p>
     * Gets the component that outputs data to a file.
     * Note that compareTo method of the AComponent class
     * is designed to sort lists in ascending order.
     * </p>
     *
     * @return AComponent object reference
     */
    AComponent getPersistencyComponent() {

        if(owner.sortedComponentList.containsKey("ER_class")){
            return owner.sortedComponentList.get("ER_class");
        } else if(owner.sortedComponentList.containsKey("PEB_class")){
            return owner.sortedComponentList.get("PEB_class");
        } else if(owner.sortedComponentList.containsKey("SEB_class")){
            return owner.sortedComponentList.get("SEB_class");
        }
        List<AComponent> l = new ArrayList<>(owner.myComponents.values());
        Collections.sort(l);
        for (AComponent comp : l) {
            if (comp.getType().equals(ACodaType.ER.name())) return comp;
            else if (comp.getType().equals(ACodaType.PEB.name())) return comp;
            else if (comp.getType().equals(ACodaType.SEB.name())) return comp;
            else if (comp.getType().equals(ACodaType.EB.name())) return comp;
            else if (comp.getType().equals(ACodaType.CDEB.name())) return comp;
            else if (comp.getType().equals(ACodaType.DC.name())) return comp;
        }
        return null;
    }

}
