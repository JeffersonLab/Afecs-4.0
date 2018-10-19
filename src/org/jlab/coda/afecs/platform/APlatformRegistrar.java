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

package org.jlab.coda.afecs.platform;

import org.jlab.coda.afecs.client.AClientInfo;
import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.system.AConfig;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;

import org.jlab.coda.afecs.system.util.AfecsTool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *     This is the class, responsible for creating and maintaining
 *     physical client registration information {@link AClientInfo}
 *     database, as well as maintaining sessions {@link ASessionInfo}
 *     database including run-numbers for each session. Information
 *     will be stored and retrieved from the xml files.
 * </p>
 *
 * <font size = 1 >
 *     Note, that SQL database option is not supported for this version.
 *     There a few methods for coda2 support though.
 * </font>
 *
 *     Following methods are provided to support:
 * <ul>
 *     <li>
 *         Afecs user authentication information encryption
 *     </li>
 *     <li>
 *         Management of the clients COOL database
 *     </li>
 *     <li>
 *         Management of the sessions COOL database
 *     </li>
 *     <li>
 *         Management of the agents memory based database
 *     </li>
 *     <li>
 *         Creation of the run-log files,
 *         i.e. current_run.log and previous_run.log
 *     </li>
 * </ul>
 *
 * @author gurjyan
 *         Date: 11/16/14 Time: 2:51 PM
 * @version 4.x
 */
public class APlatformRegistrar {
    private ConcurrentHashMap<String, AClientInfo>  ClientDir  = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ASessionInfo> SessionDir = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> RunTypeDir = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AComponent>   AgentDir   = new ConcurrentHashMap<>();

    // Get hold of AConfig singleton object.
    AConfig sysConfig = AConfig.getInstance();


    /**
     * Constructor
     */
    public APlatformRegistrar(){
        readSessionDatabase();
    }

    /**
     * <p>
     *     Encrypts and saves the authentication information
     *     into binary file located in the $COOL_HOME/$EXPID/.afecs_
     *     account directory
     * </p>
     * @param node     Node name where the account is opened
     * @param user     User name
     * @param password Password
     */
    public void encryptAndSave(String node,
                               String user,
                               String password){

        try {
            // Generate secret encryption key
            KeyGenerator desGen = KeyGenerator.getInstance("DES");
            SecretKey desKey = desGen.generateKey();

            // Convert secret key to a byte array
            // to be able to transfer to a requester
            SecretKeyFactory desFactory = SecretKeyFactory.getInstance("DES");
            DESKeySpec desSpec = (DESKeySpec)desFactory.getKeySpec(desKey,
                    javax.crypto.spec.DESKeySpec.class);
            byte[] secKey = desSpec.getKey();

            byte[] plainPassword = password.getBytes();

            // Get object for encryption
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, desKey);
            byte[] encryptedPassword = cipher.doFinal(plainPassword);

            DataOutputStream os = new DataOutputStream(
                    new FileOutputStream(sysConfig.getCoolHome() +
                            File.separator + sysConfig.getPlatformExpid() +
                            File.separator+ ".afecs_account" +
                            File.separator + user + "_" + node + ".pswd"));
            os.write(encryptedPassword);
            os.close();
            os = new DataOutputStream(
                    new FileOutputStream(sysConfig.getCoolHome() +
                            File.separator + sysConfig.getPlatformExpid() +
                            File.separator + ".afecs_account" +
                            File.separator + user + "_" + node + ".key"));
            os.write(secKey);
            os.close();

        } catch (NoSuchAlgorithmException |
                InvalidKeySpecException |
                NoSuchPaddingException |
                InvalidKeyException |
                IllegalBlockSizeException |
                BadPaddingException |
                IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     *     Reads the hidden secret key file and returns
     *     binary form of the password encryption secret key.
     * </p>
     *
     * @param n node name
     * @param u user name
     * @return byte array representing the secret key
     */
    public byte[] getSavedSecretKey(String n, String u){
        byte[] keyBytes = null;
        File kf = new File(sysConfig.getCoolHome() +
                File.separator + sysConfig.getPlatformExpid() +
                File.separator + ".afecs_account" +
                File.separator + u + "_" + n + ".key");

        if(kf.exists()){
            try {
                DataInputStream kis = new DataInputStream(new FileInputStream(kf));
                keyBytes = new byte[(int)kf.length()];
                kis.readFully(keyBytes);
                kis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return keyBytes;
    }

    /**
     * <p>
     *      Reads the hidden password file and
     *      returns binary form of the encrypted password.
     * </p>
     * @param n node name
     * @param u user name
     * @return byte array representing the password
     */
    public byte[] getSavedPassword(String n, String u){
        byte[] passwordBytes = null;
        File pf = new File(sysConfig.getCoolHome()+
                File.separator + sysConfig.getPlatformExpid() +
                File.separator + ".afecs_account" +
                File.separator + u + "_" + n + ".pswd");

        if(pf.exists()){
            try {
                DataInputStream pis = new DataInputStream(new FileInputStream(pf));
                passwordBytes = new byte[(int)pf.length()];
                pis.readFully(passwordBytes);
                pis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return passwordBytes;
    }

    /**
     * <p>
     *     Gets the map of all registered physical clients
     * </p>
     * @return map (key = client name, value = {@link AClientInfo} object)
     */
    public ConcurrentHashMap<String, AClientInfo> getClientDir() {
        return ClientDir;
    }

    /**
     * <p>
     *     Gets the specified client from the clients hashmap
     * </p>
     *
     * @param n the name of the client
     * @return AClientInfo object
     */
    public AClientInfo getClient(String n){
        return ClientDir.get(n);
    }

    /**
     * <p>
     *    Adds new client to the map
     * </p>
     *
     * @param a {@link AClientInfo} object
     */
    public synchronized void addClient(AClientInfo a) {
        ClientDir.put(a.getName(), a);
    }

    /**
     * <p>
     *     Removes client from the map
     * </p>
     * @param a the name of the agent
     */
    public synchronized void removeClient(String a) {
        if(ClientDir.containsKey(a)){
            ClientDir.remove(a);
        }
    }

    /**
     * <p>
     *     Check to if we have a client in the client's map with
     *     the same host and port, delete the client if exists.
     * </p>
     */
    public void checkDeleteClientDB(String name, String host, int port){
        for(AClientInfo ci:ClientDir.values()){
            if(!ci.getName().equals(name)) {
                if((ci.getHostName().equals(host)) &&
                        (ci.getPortNumber()==port)){
                    removeClient(ci.getName());
                }
            }
        }
    }

    /**
     * <p>
     *     Dumps the content of the map of the registered
     *     clients to the disk as an xml file. The file will
     *     be stored in COOL_HOME/expid/clientRegistration.xml file
     * </p>
     * @return status of the operation
     */
    public boolean dumpClientDatabase(){
        boolean stat = true;
        try{
            FileWriter fw = new FileWriter(sysConfig.getCoolHome() +
                    File.separator + sysConfig.getPlatformExpid() +
                    File.separator + "ddb" +
                    File.separator + "clientRegistration.xml");
            BufferedWriter out = new BufferedWriter(fw);
            out.write("<registration>\n");
            for(AClientInfo ci: ClientDir.values()){
                out.write("   <client>\n");
                out.write("      <name>"+ci.getName()+"</name>\n");
                out.write("      <hostname>"+ci.getHostName()+"</hostname>\n");
                out.write("      <containerhost>"+ci.getContainerHost()+"</containerhost>\n");
                out.write("      <portnumber>"+ci.getPortNumber()+"</portnumber>\n");
                out.write("   </client>\n");
            }
            out.write("</registration>\n");
            out.close();
            fw.close();

        } catch(IOException e){
            stat = false;
            e.printStackTrace();
        }
        return stat;
    }

    /**
     * <p>
     *     Reads the COOL_HOME/expid/clientRegistration.xml file and
     *     fills the internal map of the registered physical clients.
     * </p>
     * @return status of the operation
     */
    public boolean readClientDatabase(){
        boolean stat = true;
        try {

            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            // Create the builder and parse the file
            Document doc = factory.newDocumentBuilder().parse(
                    new File(sysConfig.getCoolHome()+
                            File.separator + sysConfig.getPlatformExpid() +
                            File.separator + "ddb" +
                            File.separator + "clientRegistration.xml"));
            doc.getDocumentElement().normalize();

            NodeList nClient = doc.getElementsByTagName("client");
            for(int i=0; i<nClient.getLength();i++){
                NodeList nl = nClient.item(i).getChildNodes();
                AClientInfo ci = new AClientInfo();
                for(int j=0;j<nl.getLength();j++){
                    Node n = nl.item(j);
                    if(n.getNodeType() == Node.ELEMENT_NODE){
                        Element ne= (Element)n;
                        NodeList nnl;
                        if(n.getNodeName().equals("name")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                ci.setName(nnl.item(0).getNodeValue().trim());
                            }
                        } else if(n.getNodeName().equals("hostname")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                ci.setHostName(nnl.item(0).getNodeValue().trim());
                            }
                        } else if(n.getNodeName().equals("containerhost")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                ci.setContainerHost(nnl.item(0).getNodeValue().trim());
                            }
                        } else if(n.getNodeName().equals("portnumber")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                try{
                                    ci.setPortNumber(Integer.parseInt(nnl.item(0).getNodeValue().trim()));
                                } catch(NumberFormatException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                ClientDir.put(ci.getName(),ci);
            }
        } catch (SAXException |
                ParserConfigurationException |
                IOException e) {
            e.printStackTrace();
            stat = false;
        }
        return stat;
    }

    /**
     * <p>
     *     Gets the stored map of all registered sessions of the control
     * </p>
     * @return map (key = session name, value = {@link ASessionInfo} object)
     */
    public ConcurrentHashMap<String, ASessionInfo> getSessionDir() {
        return SessionDir;
    }

    /**
     * <p>
     *     Gets the specified session from the sessions map
     * </p>
     * @param n the name of the session
     * @return  {@link ASessionInfo} object
     */
    public ASessionInfo getSession(String n){
        return SessionDir.get(n);
    }

    /**
     * <p>
     *     Adds the new session to the sessions registration map
     * </p>
     * @param a {@link ASessionInfo} object
     */
    public synchronized void addSession(ASessionInfo a) {
        SessionDir.put(a.getName(), a);
    }

    /**
     * <p>
     *     Set the run number for the specified session
     * </p>
     * @param s the name of the session
     * @param r run number
     * @return -1 if unsuccessful and 0 if successful
     */
    public synchronized int setSessionRunNumber(String s, int r){
        if(SessionDir.containsKey(s)){
            SessionDir.get(s).setRunNumber(r);
            int rn = SessionDir.get(s).getRunNumber();
            if(rn!=r) return -1;
        }
        return 0;
    }

    /**
     * <p>
     *     Gets the run number for the specified session
     *     from the registered sessions map,
     *     increments it and puts it back.
     * </p>
     * @param s the name of the session
     * @return rn run number or -1 if failed
     */
    public synchronized int incrementSessionRunNumber(String s){
        int rn=-1;
        int previous;
        if(SessionDir.containsKey(s)){
            rn = SessionDir.get(s).getRunNumber();
            previous = rn;
            rn++;
            SessionDir.get(s).setRunNumber(rn);
            rn = SessionDir.get(s).getRunNumber();
            if((rn-previous)!=1) rn = -1;
        }
        return rn;
    }

    /**
     * <p>
     *     Update the config name for the session
     * </p>
     * @param session the name of the session
     * @param runType the name of the new config
     */
    public synchronized void updateSessionRunType(String session,
                                                  String runType){
        if(SessionDir.containsKey(session)){
            SessionDir.get(session).setConfigName(runType);
        }
    }

    /**
     * <p>
     *     Returns the run number of the specified session
     * </p>
     * @param s the name of the session
     * @return run number
     */
    public int getSessionRunNumber(String s){
        readSessionDatabase();
        int rn = 0;
        if(SessionDir.containsKey(s)){
            rn = SessionDir.get(s).getRunNumber();
        }
        return rn;
    }

    /**
     * <p>
     *     Dumps the content of the sessions hash map to
     *     the disk in the form of xml file. The content
     *     will be stored in the COOL_HOME/expid/controlSessions.xml file
     * </p>
     * @return status of the operation
     */
    public boolean dumpSessionsDatabase(){
        boolean stat = true;
        if(sysConfig.getCoolHome().equals(AConstants.udf)){
            System.out.println("System constant coolHome is not defined.");
            return false;
        }
        try{
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(sysConfig.getCoolHome()+
                            File.separator + sysConfig.getPlatformExpid() +
                            File.separator + "ddb" +
                            File.separator + "controlSessions.xml"));
            out.write("<control>\n");
            for(String cn: SessionDir.keySet()){
                out.write("   <session>\n");
                out.write("      <name>"+SessionDir.get(cn).getName()+"</name>\n");
                out.write("      <config>"+SessionDir.get(cn).getConfigName()+"</config>\n");
                out.write("      <runnumber>"+SessionDir.get(cn).getRunNumber()+"</runnumber>\n");
                out.write("   </session>\n");
            }
            out.write("</control>\n");
            out.close();

        } catch(IOException e){
            stat = false;
            e.printStackTrace();
        }
        return stat;
    }

    /**
     * <p>
     *     Parses COOL_HOME/expid/conmtrolSessions.xml file
     *     and creates sessions registration internal map.
     * </p>
     * @return status of the operation
     */
    public boolean readSessionDatabase(){
        boolean stat = true;
        SessionDir.clear();
        if(sysConfig.getCoolHome().equals(AConstants.udf)){
            System.out.println("System constant codaHome is not defined.");
            return false;
        }
        try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            // Create the builder and parse the file
            Document doc = factory.newDocumentBuilder().parse(
                    new File(sysConfig.getCoolHome() +
                            File.separator + sysConfig.getPlatformExpid() +
                            File.separator + "ddb" +
                            File.separator + "controlSessions.xml"));
            doc.getDocumentElement().normalize();

            NodeList nSession = doc.getElementsByTagName("session");
            for(int i=0; i<nSession.getLength();i++){
                NodeList nl = nSession.item(i).getChildNodes();
                ASessionInfo ci = new ASessionInfo();
                for(int j=0;j<nl.getLength();j++){
                    Node n = nl.item(j);
                    if(n.getNodeType() == Node.ELEMENT_NODE){
                        Element ne= (Element)n;
                        NodeList nnl;
                        if(n.getNodeName().equals("name")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                ci.setName(nnl.item(0).getNodeValue().trim());
                            }
                        } else if(n.getNodeName().equals("config")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                ci.setConfigName(nnl.item(0).getNodeValue().trim());
                            }
                        } else if(n.getNodeName().equals("runnumber")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){

                                try{
                                    ci.setRunNumber(Integer.parseInt(nnl.item(0).getNodeValue().trim()));
                                } catch(NumberFormatException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                SessionDir.put(ci.getName(),ci);
            }
        } catch (SAXException |
                ParserConfigurationException |
                IOException e) {
            e.printStackTrace();
            stat = false;
        }
        return stat;
    }

    /**
     * <p>
     *     Creating physical runType or configID database file.
     * </p>
     * @return result of the creation
     */
    public boolean dumpRunTypeDatabase(){
        boolean stat = true;
        if(sysConfig.getCoolHome().equals(AConstants.udf)){
            System.out.println("System constant codaHome is not defined.");
            return false;
        }
        try{
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(sysConfig.getCoolHome() +
                            File.separator + sysConfig.getPlatformExpid() +
                            File.separator + "ddb" +
                            File.separator + "configID.xml"));
            out.write("<control>\n");
            for(String cn: RunTypeDir.keySet()){
                out.write("   <runtype>\n");
                out.write("      <name>"+cn+"</name>\n");
                out.write("      <id>"+ RunTypeDir.get(cn)+"</id>\n");
                out.write("   </runtype>\n");
            }
            out.write("</control>\n");
            out.close();
        } catch(IOException e){
            stat = false;
            e.printStackTrace();
        }
        return stat;
    }

    /**
     * Returns the config ID based on the config name
     * @param confName configuration name
     * @return int representing configuration
     */
    public synchronized int addConfigId(String confName){
        int id = -1;
        if(!RunTypeDir.containsKey(confName)){
            int i = 0;
            while(i<Integer.MAX_VALUE){
                i++;
                if(!RunTypeDir.containsValue(i)) {
                    RunTypeDir.put(confName,i);
                    dumpRunTypeDatabase();
                    id = i;
                    break;
                }
            }
        } else {
            id = RunTypeDir.get(confName);
        }
        return id;
    }

    /**
     * <p>
     *     Get the map of registered sessions
     * </p>
     * @return  the names of all registered sessions
     */
    public ArrayList<String> getSessionNames(){
        ArrayList<String> al = new ArrayList<>();
        for(String s:SessionDir.keySet()){
            al.add(s);
        }
        return al;
    }

    /**
     * <p>
     *     Gets the map of registered agents
     * </p>
     * @return map of {@link AComponent} objects
     */
    public ConcurrentHashMap<String, AComponent> getAgentDir() {
        return AgentDir;
    }

    /**
     * <p>
     *     Gets the specific agent information form the map
     * </p>
     * @param n the name of the agent
     * @return AComponent object
     */
    public AComponent getAgent(String n){
        return AgentDir.get(n);
    }

    /**
     * <p>
     *    Adds an agent to the agents map
     * </p>
     * @param a {@link AComponent} object
     */
    public synchronized void addAgent(AComponent a){
        String containerName = a.getHost()+"_admin";
        if(a.getName().equals(containerName)){
            // This is a container requesting to register.
            // This means that container was restarted,
            // All the agents running on that container
            // must be removed from the registration maps.
            for(String s: AgentDir.keySet()){
                if(!s.equals(containerName)){
                    if(AgentDir.get(s).getHost().equals(a.getHost())){
                        AgentDir.remove(s);
                    }
                }
            }
        }
        AgentDir.put(a.getName(),a);
    }

    /**
     * <p>
     *    Removes agent from the agents map
     * </p>
     * @param n the name of the agent
     */
    public synchronized void removeAgent(String n){
        if(AgentDir.containsKey(n)){
            AgentDir.remove(n);
        }
    }

    /**
     * @return status of the operation
     */
    /**
     * <p>
     *     Creates Run-Log file, containing
     *     information about the run.
     * </p>
     *
     * @param xml String containing information
     * @param session session name
     * @param cp2Previous indicates is run is ended
     * @return status of the operation
     */
    public boolean createRunLog(String xml,
                                String session,
                                boolean cp2Previous) {
        boolean stat = true;
        if(sysConfig.getCoolHome().equals(AConstants.udf)){
            System.out.println("Error: System constant codaHome is not defined.");
            System.out.println("System constant codaHome is not defined.");
            stat =  false;
        } else {
            String logDirName = sysConfig.getCoolHome()+File.separator+
                    sysConfig.getPlatformExpid()+File.separator+
                    "ddb"+File.separator+
                    "run-log"+File.separator+session;

            // If directory does not exist create it
            File d = new File(logDirName);
            d.mkdirs();

            String logFileName = logDirName+File.separator+"current_run.log";
            String logPrevFileName = logDirName+File.separator+"previous_run.log";

            if(cp2Previous){
                File f = new File(logFileName);
                if(f.exists()){
                    try {
                        AfecsTool.fork("cp "+logFileName+" "+logPrevFileName,false);
                    } catch (AException e) {
                        stat = false;
                        System.out.println("Error: "+e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            try{
                BufferedWriter out = new BufferedWriter(new FileWriter(logFileName));
                out.write(xml);
                out.close();
            } catch(IOException e){
                stat = false;
                System.out.println("Error: "+e.getMessage());
                e.printStackTrace();
            }
        }
        return stat;
    }

    /**
     * <p>
     *     Goes over the agents database and extracts the
     *     agent host names (where agents are physically running).
     * </p>
     * @return  returns set of the host_admin
     *         (the name of the container administrator agent).
     */
    public SortedSet<String> getContainerAdmins(){
        SortedSet<String> st = Collections.synchronizedSortedSet(new TreeSet<String>());
        for(AComponent ai:AgentDir.values()){
            st.add(ai.getHost()+"_admin");
        }
        return st;
    }


    /**
     * <p>
     *     Gets run number from the sql database
     *     and then from the COOL database
     *     User to make sure run numbers recorded
     *     both in sql and COOL databases.
     *     Coda2 only.
     * </p>
     * @param session the name of the session
     * @return status of the operation
     */
    public int[] getRunNumbers(String session){
        int[] res = new int[2];


        // now cool db
        res[1] = SessionDir.get(session).getRunNumber();
        return res;
    }

    /**
     * <p>
     *     Updates event limit setting in the
     *     runType/Options/runType_options.rdf file.
     * </p>
     * @param runType the name of the configuration
     * @param eventLimit event limit value
     * @return status of the operation
     */
    public synchronized boolean updateOptionsEventLimit(String runType,
                                                        int eventLimit){
        boolean stat = true;
        String optionsFileName = sysConfig.getCoolHome()+
                File.separator + sysConfig.getPlatformExpid()+
                File.separator + "config" +
                File.separator + "Control" +
                File.separator + runType +
                File.separator + "Options " +
                File.separator + runType + "_option.rdf";
        try{

            File opt = new File(optionsFileName);
            if(!opt.exists()) return false;

            FileReader fr = new FileReader(opt);
            BufferedReader in = new BufferedReader(fr);
            ArrayList<String> tr = new ArrayList<>();
            String l;
            while((l = in.readLine())!=null){
                tr.add(l);
            }
            in.close();
            fr.close();

            for(int i=0; i<tr.size();i++){
                if(tr.get(i).trim().startsWith("<cool:hasEventLimit>")){
                    tr.set(i,"   <cool:hasEventLimit>"+eventLimit+"</cool:hasEventLimit>");
                    break;
                }
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(optionsFileName));

            for(String s:tr){
                out.write(s+"\n");
            }
            out.close();

        } catch(IOException e){
            stat = false;
            e.printStackTrace();
        }
        return stat;
    }

}
