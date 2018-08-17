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

package org.jlab.coda.afecs.ui.rcgui.util;

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.system.AConstants;

import java.util.ArrayList;
import java.util.HashMap;

public class TreeHashMap {
    // Components tree nested hashmap structure, needed for JTree creation.
    private HashMap<String,HashMap<String,HashMap<String, HashMap<String,ArrayList<String>>>>> componentTree =
            new HashMap<String,HashMap<String,HashMap<String,HashMap<String,ArrayList<String>>>>>();

    private AComponent comp;
    public TreeHashMap(AComponent c){
        comp = c;
    }

    public void update(){
        String expid   = comp.getExpid();
        String session = comp.getSession();
        String runtype = comp.getRunType();
        String type    = comp.getType();
        String name    = comp.getName();
        if(!expid.equals(AConstants.udf) && !session.equals(AConstants.udf) && !runtype.equals(AConstants.udf) && !type.equals(AConstants.udf)){
            if(!componentTree.containsKey(expid)){

                ArrayList<String>names = new ArrayList<String>();
                names.add(name);

                HashMap<String,ArrayList<String>> Mtypes = new HashMap<String,ArrayList<String>>();
                Mtypes.put(type, names);

                HashMap<String,HashMap<String,ArrayList<String>>> Mruntypes = new HashMap<String,HashMap<String,ArrayList<String>>>();
                Mruntypes.put(runtype, Mtypes);

                HashMap<String,HashMap<String,HashMap<String,ArrayList<String>>>> Msessions = new HashMap<String,HashMap<String,HashMap<String,ArrayList<String>>>>();
                Msessions.put(session, Mruntypes);

                componentTree.put(expid,Msessions);
            } else {
                if(componentTree.get(expid).containsKey(session)){
                    if(componentTree.get(expid).get(session).containsKey(runtype)){
                        if(componentTree.get(expid).get(session).get(runtype).containsKey(type)){
                            if(!componentTree.get(expid).get(session).get(runtype).get(type).contains(name)){
                                componentTree.get(expid).get(session).get(runtype).get(type).add(name);
                            }
                        } else {
                            // type does not exists
                            ArrayList<String>names = new ArrayList<String>();
                            names.add(name);
                            componentTree.get(expid).get(session).get(runtype).put(type,names);
                        }
                    } else {
                        // runtype does not exists
                        ArrayList<String>names = new ArrayList<String>();
                        names.add(name);

                        HashMap<String,ArrayList<String>> Mtypes = new HashMap<String,ArrayList<String>>();
                        Mtypes.put(type, names);
                        componentTree.get(expid).get(session).put(runtype,Mtypes);
                    }
                } else {
                    // session does not exists
                    ArrayList<String>names = new ArrayList<String>();
                    names.add(name);

                    HashMap<String,ArrayList<String>> Mtypes = new HashMap<String,ArrayList<String>>();
                    Mtypes.put(type, names);

                    HashMap<String,HashMap<String,ArrayList<String>>> Mruntypes = new HashMap<String,HashMap<String,ArrayList<String>>>();
                    Mruntypes.put(runtype, Mtypes);
                    componentTree.get(expid).put(session,Mruntypes);
                }
            }
        }
    }

    public HashMap getMap(){
        return componentTree;
    }
}
