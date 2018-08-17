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

import org.jlab.coda.afecs.system.AConfig;
import org.jlab.coda.afecs.system.AConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 *     Class that manages user specific applications
 * </p>
 *
 * @author gurjyan
 *         Date: 11/20/14 Time: 2:51 PM
 * @version 3.x
 */public class UserAppManager {
    private AConfig sysConfig = AConfig.getInstance();

    // List of user defined applications
    private CopyOnWriteArrayList<CUserApp>
            userApps = new CopyOnWriteArrayList<>();

    // Default user applications describing file path
    private String dFile = sysConfig.getCoolHome() +
            File.separator + sysConfig.getPlatformExpid() +
            File.separator + "user" +
            File.separator + "userApplications.xml";

    public boolean writeFile() {
        boolean stat = true;
        try{
            FileWriter fw = new FileWriter(dFile);
            BufferedWriter out = new BufferedWriter(fw);
            out.write("<user>\n");
            for(CUserApp up: userApps){
                out.write("   <application>\n");
                out.write("      <name>"+up.getName()+"</name>\n");
                out.write("      <command>");
                for(String s:up.getCommand()){
                    out.write(s+ " ");
                }
                out.write("</command>\n");
                out.write("      <description>"+up.getDescription()+"</description>\n");
                out.write("   </application>\n");
            }
            out.write("</user>\n");
            out.close();
            fw.close();
        } catch(IOException e){
            stat = false;
            if(AConstants.debug.get()) e.printStackTrace();
        }
        return stat;
    }

    public boolean readFile(){
        boolean stat = true;
        userApps.clear();
        try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            // Create the builder and parse the file
            Document doc = factory.newDocumentBuilder().parse(new File(dFile));
            doc.getDocumentElement().normalize();

            NodeList nClient = doc.getElementsByTagName("application");
            for(int i=0; i<nClient.getLength();i++){
                NodeList nl = nClient.item(i).getChildNodes();
                CUserApp map = new CUserApp();
                for(int j=0;j<nl.getLength();j++){
                    Node n = nl.item(j);
                    if(n.getNodeType() == Node.ELEMENT_NODE){
                        Element ne= (Element)n;
                        NodeList nnl;

                        if(n.getNodeName().equals("name")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                map.setName(nnl.item(0).getNodeValue().trim());
                            }

                        } else if(n.getNodeName().equals("command")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                ArrayList<String> l = new ArrayList<>();
                                StringTokenizer st = new StringTokenizer(nnl.item(0).getNodeValue().trim());
                                while(st.hasMoreElements()){
                                  l.add(st.nextToken());
                                }
                                map.setCommand(l);
                            }

                        } else if(n.getNodeName().equals("description")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                map.setDescription(nnl.item(0).getNodeValue().trim());
                            }
                        }
                    }
                }
                userApps.add(map);
            }
        } catch (SAXException e) {
            if(AConstants.debug.get())
                System.out.println("Error: Not valid xml input.");
            stat = false;
        } catch (ParserConfigurationException e) {
            if(AConstants.debug.get())
                System.out.println("Error: DOM parser configuration exception.");
            stat = false;
        } catch (IOException e) {
            if(AConstants.debug.get())
                System.out.println("Warning: Can not find userApplications file.");
            stat = false;
        }
        return stat;
    }

    public void addApp(CUserApp app){
        userApps.add(app);
    }

    public void removeApp(String name){
        for(CUserApp app:userApps){
            if(app.getName().equals(name)){
                userApps.remove(app);
            }
        }
    }

    public CopyOnWriteArrayList<CUserApp> getUserApps() {
        return userApps;
    }

}
