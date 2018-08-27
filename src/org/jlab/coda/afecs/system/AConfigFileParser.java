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

package org.jlab.coda.afecs.system;


import org.jlab.coda.afecs.system.util.AfecsTool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * <p>
 *     Generic Afecs configuration file parser
 *</p>
 *
 * @author gurjyan
 *         Date: 11/12/13 Time: 1:51 PM
 * @version 4.x
 */


public class AConfigFileParser {
    private String             platformExpid = AConstants.udf;

    // platform cMsg domain server
    private String             platformDescription = AConstants.udf;
    private String             platformHost = AConstants.udf;
    private int                platformTcpPort;
    private int                platformUdpPort;

    // platform rc domain server
    private String             platformRcDomainServerDescription = AConstants.udf;
    private int                platformRcDomainUdpPort;

    // control session
    private String session = AConstants.udf;

    // sql database
    private String dbUrl    = AConstants.udf;
    private String dbDriver = AConstants.udf;
    private String dbUser   = AConstants.udf;
    private String dbPasswd = AConstants.udf;

    public AConfigFileParser(String fileName){
        NodeList nl;
        Element ne;

        try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            // Create the builder and parse the file
            Document doc = factory.newDocumentBuilder().parse(new File(fileName));
            doc.getDocumentElement().normalize();

            nl = doc.getElementsByTagName("expid");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                platformExpid = nl.item(0).getNodeValue().trim();
            }

            nl = doc.getElementsByTagName("session");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                session = nl.item(0).getNodeValue().trim();
            }

            nl = doc.getElementsByTagName("description");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                platformDescription = nl.item(0).getNodeValue().trim();
            }

            nl = doc.getElementsByTagName("tcpPort");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                try{
                    platformTcpPort = Integer.parseInt(nl.item(0).getNodeValue().trim());
                } catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }

            // This tag is usually used by containers and guis.
            nl = doc.getElementsByTagName(AConstants.PLATFORMHOST);
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                platformHost = nl.item(0).getNodeValue().trim();
            }

            // cMsg server UDP port
            nl = doc.getElementsByTagName("udpPort");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                try{
                    platformUdpPort = Integer.parseInt(nl.item(0).getNodeValue().trim());
                } catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }

            // rc domain multicast server udp port
            nl = doc.getElementsByTagName("rcUdpPort");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                try{
                    platformRcDomainUdpPort = Integer.parseInt(nl.item(0).getNodeValue().trim());
                } catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }

            nl = doc.getElementsByTagName("rcDescription");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                platformRcDomainServerDescription = nl.item(0).getNodeValue().trim();
            }

            // sql database parameters
            nl = doc.getElementsByTagName("dbUrl");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                dbUrl = nl.item(0).getNodeValue().trim();
            }

            nl = doc.getElementsByTagName("dbDriver");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                dbDriver = nl.item(0).getNodeValue().trim();
            }

            nl = doc.getElementsByTagName("dbUser");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                dbUser = nl.item(0).getNodeValue().trim();
            }

            nl = doc.getElementsByTagName("dbPasswd");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                dbPasswd = nl.item(0).getNodeValue().trim();
            }

        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }

    }

    public String getPlatformExpid() {
        return platformExpid;
    }

    public String getPlatformHost() {
        return platformHost;
    }

    public int getPlatformTcpPort() {
        return platformTcpPort;
    }

    public int getPlatformUdpPort() {
        return platformUdpPort;
    }

    public String getSession() {
        return session;
    }

}
