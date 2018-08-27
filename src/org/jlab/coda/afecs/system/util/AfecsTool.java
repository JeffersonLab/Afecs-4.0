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

package org.jlab.coda.afecs.system.util;

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.fcs.FCSConfigData;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConfig;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.cMsg.cMsgConstants;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgPayloadItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Useful methods (tools) used in the project.
 *
 * @author gurjyan
 *         Date: 11/12/13 Time: 1:51 PM
 * @version 4.x
 */
public class AfecsTool {

    private static AConfig myConfig = AConfig.getInstance();

    /**
     * parser for the XML having a structure:
     * <containerTag1>
     *   <containerTag2>
     *       <tag>value</tag>
     *       .....
     *       <tag>value</tag>
     *   </containerTag2>
     * </containerTag1>
     * @param fileName the name of the XML file
     * @param containerTag first container tag
     * @param tags tag names
     * @return list of list of tag value pairs
     */
    public static List<XMLContainer> parseXML(String fileName,
                                              String containerTag,
                                              String[] tags){
        List<XMLContainer> result = new ArrayList<>();

        Document doc;

        File fXmlFile = new File(fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName(containerTag);

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    XMLContainer container = new XMLContainer();
                    Element eElement = (Element) nNode;
                    for(String tag:tags){
                        if(eElement.getElementsByTagName(tag)!=null && eElement.getElementsByTagName(tag).item(0)!=null){
                            String value =  eElement.getElementsByTagName(tag).item(0).getTextContent();
                            XMLTagValue tv = new XMLTagValue(tag,value);
                            container.addTagValue(tv);
                        }
                    }
                    result.add(container);
                }
            }
        } catch (SAXException |
                IOException |
                ParserConfigurationException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * <p>
     *     Parses FCS specific configuration file
     * </p>
     * @param content of the FCS config file
     * @return FCSConfigData object
     */
    public static FCSConfigData parseFcsSystemConfigXml(String content){
        FCSConfigData fcd = null;
        NodeList nl;
        Element ne;

        // get input stream from the string, needed by the xml document builder.
        ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes());

        try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(stream);

            doc.getDocumentElement().normalize();

            fcd = new FCSConfigData();

            nl = doc.getElementsByTagName("command");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                fcd.setCommandTemplate(nl.item(0).getNodeValue().trim());
            }

            nl = doc.getElementsByTagName("inputEt");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                for(int j=0;j<nl.getLength();j++){
                    Node n = nl.item(j);
                    if(n.getNodeType() == Node.ELEMENT_NODE){
                        ne= (Element)n;
                        NodeList nnl;
                        if(n.getNodeName().equals("name")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                String itName =  nnl.item(0).getNodeValue().trim();
                                if(!itName.equals(AConstants.udf)) fcd.setInputEtName(itName);
                            }
                        } else if(n.getNodeName().equals("host")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                String itHost =  nnl.item(0).getNodeValue().trim();
                                if(!itHost.equals(AConstants.udf)) fcd.setInputEtHost(itHost);
                            }
                        } else if(n.getNodeName().equals("port")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                try{
                                    fcd.setInputEtPort(Integer.parseInt(nnl.item(0).getNodeValue().trim()));
                                } catch(NumberFormatException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            nl = doc.getElementsByTagName("outputEt");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                for(int j=0;j<nl.getLength();j++){
                    Node n = nl.item(j);
                    if(n.getNodeType() == Node.ELEMENT_NODE){
                        ne= (Element)n;
                        NodeList nnl;
                        if(n.getNodeName().equals("name")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                String itName =  nnl.item(0).getNodeValue().trim();
                                if(!itName.equals(AConstants.udf)) fcd.setOutputEtName(itName);
                            }
                        } else if(n.getNodeName().equals("host")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                String itHost =  nnl.item(0).getNodeValue().trim();
                                if(!itHost.equals(AConstants.udf)) fcd.setOutputEtHost(itHost);
                            }
                        } else if(n.getNodeName().equals("port")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                try{
                                    fcd.setOutputEtPort(Integer.parseInt(nnl.item(0).getNodeValue().trim()));
                                } catch(NumberFormatException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            nl = doc.getElementsByTagName("nodes");
            if(nl.item(0)!=null){
                ne = (Element)nl.item(0);
                nl = ne.getChildNodes();
                for(int j=0;j<nl.getLength();j++){
                    Node n = nl.item(j);
                    if(n.getNodeType() == Node.ELEMENT_NODE){
                        ne= (Element)n;
                        NodeList nnl;
                        if(n.getNodeName().equals("node")){
                            nnl = ne.getChildNodes();
                            if(nnl!=null && nnl.getLength()>0){
                                String itName =  nnl.item(0).getNodeValue().trim();
                                fcd.addNode(itName);
                            }
                        }
                    }
                }
            }

        } catch (SAXException |
                ParserConfigurationException |
                IOException e) {
            e.printStackTrace();
        }

        return fcd;
    }


    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    public static boolean isPortAvailable(int port) {

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            System.out.println("Opening server socket...");
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            System.out.println("Opening datagram socket...");
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * <p>
     *     Gets stdInput, stdOutput and stdError from the
     *     shell process object.
     *     If the process is async we sleep
     *     for a second to allow process io to become ready.
     * </p>
     * @param p shell process
     * @param isSync true if process is sync
     * @return {@link StdOutput} object
     * @throws AException
     */
    public static StdOutput analyzeShellProcess(Process p,
                                                boolean isSync)
            throws AException {
        StdOutput po = new StdOutput();
        StringBuilder sb;
        String s;
        BufferedReader stdInput;
        BufferedReader stdError;


        try {
            if(isSync){
                // synchronous request
                // stdInput
                if(p.getInputStream()!=null){
                    stdInput = new BufferedReader(new
                            InputStreamReader(p.getInputStream()));
                    sb = new StringBuilder();
                    if(stdInput.ready()){
                        while ((s = stdInput.readLine()) != null) {
                            sb.append(s).append("\n");
                        }
                    }
                    po.setStdio(sb.toString());
                    stdInput.close();
                }

                // stdError
                if(p.getErrorStream()!=null){
                    stdError = new BufferedReader(new
                            InputStreamReader(p.getErrorStream()));
                    sb = new StringBuilder();
                    if(stdError.ready()) {
                        while ((s = stdError.readLine()) != null) {
                            sb.append(s).append("\n");
                        }
                    }
                    po.setStdErr(sb.toString());
                    stdError.close();
                }
                // set exit value
                po.setExitValue(p.waitFor());

            } else {
                // asynchronous request
                AfecsTool.sleep(500);
                if(p.getInputStream()!=null){
                    stdInput = new BufferedReader(new
                            InputStreamReader(p.getInputStream()));
                    sb = new StringBuilder();
                    if(stdInput.ready()){
                        String ss = stdInput.readLine();
                        if((ss != null)) {
                            sb.append(ss).append("\n");
                        }
                    }
                    po.setStdio(sb.toString());
                    stdInput.close();
                }
                // stdError
                if(p.getErrorStream()!=null){
                    stdError = new BufferedReader(new
                            InputStreamReader(p.getErrorStream()));
                    sb = new StringBuilder();
                    if(stdError.ready()) {
                        String se = stdError.readLine();
                        if(se != null) {
                            sb.append(se).append("\n");
                        }
                    }
                    po.setStdErr(sb.toString());
                    stdError.close();
                }

                // 09.20.15 vg process might be permanent, hence we do not check exitValue()
            }

        } catch ( IOException |
                InterruptedException |
                IllegalThreadStateException e) {
            throw new AException(e.getMessage());
        }

        return po;

    }

    /**
     *  <p>
     *      Pipes data of a process to
     *      the next process in the chain.
     * </p>
     *
     * @param processes array of processes
     * @return {@link StdOutput} object reference
     * @throws AException
     */
    private static StdOutput fork_pipe(Process[] processes)
            throws AException {

        // Start Piper between all processes
        java.lang.Process p1;
        java.lang.Process p2;
        for (int i = 0; i < processes.length; i++) {
            p1 = processes[i];

            // If there's one more process
            if (i + 1 < processes.length) {
                p2 = processes[i + 1];
                // Start piper
                new Thread(new APiper(p1.getInputStream(),
                        p2.getOutputStream())).start();
            }
        }
        Process last = processes[processes.length - 1];
        // Wait for last process in chain;
        // may throw InterruptedException
        try {
            last.waitFor();
        } catch (InterruptedException e) {
            throw new AException(e.getMessage());
        }
        // Return its InputStream
        return analyzeShellProcess(last, false);
    }


    /**
     * <p>
     *     Forks an external shell process
     * </p>
     * @param cmdL list containing command name,
     *             followed by number of parameters
     * @param isSync if true thread will
     *               wait until process is completed
     * @return {@link StdOutput} object
     */
    public static StdOutput fork (List<String> cmdL,
                                  boolean isSync)
            throws AException {
        StdOutput out;
        Process p;


        try {
            p = new ProcessBuilder(cmdL).start();
            out = analyzeShellProcess(p, isSync);
        } catch (IOException | IllegalThreadStateException e) {
            throw new AException(e.getMessage());
        }
        String err = out.getStdErr();
        if(err!=null && !err.equals("")){
            System.out.println(err);
        }
        return out;
    }

    /**
     * <p>
     *     Overloaded {@link #fork(java.util.List, boolean)} method
     *     that takes string as an input parameter.
     *     It handles process piping ( '|' operator, unix only),
     *     multiprocessing (';' operator). In case multi-process
     *     request stdOutput object of the last process in a chain
     *     will be returned. Note: mixing '|' and ';' is not allowed.
     * </p>
     *
     * @param cmd command line string
     * @param isSync see {@link #fork(java.util.List, boolean)}
     * @return {@link StdOutput} object
     */
    public static StdOutput fork (String cmd,
                                  boolean isSync)
            throws AException {

        StdOutput out = new StdOutput();

        if(cmd.contains("|")){
            StringTokenizer st2 = new StringTokenizer(cmd,"|");
            Process[] pp = new Process[st2.countTokens()];
            for(int i=0; i<=st2.countTokens();i++) {
                String p = st2.nextToken();
                ArrayList<String> l = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(p.trim());
                while(st.hasMoreTokens()){
                    l.add(st.nextToken());
                }
                try {
                    pp[i] = new ProcessBuilder(l).start();
                } catch (IOException e) {
                    throw new AException(e.getMessage());
                }
            }
            out = fork_pipe(pp);

        } else if(cmd.contains(";")){
            StringTokenizer st1 = new StringTokenizer(cmd,";");
            while(st1.hasMoreTokens()){
                String p = st1.nextToken();
                ArrayList<String> l = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(p.trim());
                while(st.hasMoreTokens()){
                    l.add(st.nextToken());
                }
                out = fork(l,isSync);
            }

        } else {
            ArrayList<String> l = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(cmd.trim());
            while(st.hasMoreTokens()){
                l.add(st.nextToken());
            }
            out = fork(l,isSync);
        }
        if(out!=null){
            String err = out.getStdErr();
            if(err!=null && !err.equals("")){
                System.out.println(err);
            }
        }
        return out;
    }

    /**
     * <p>
     *     Converts object into a byte array
     * </p>
     * @param object to be converted. Probably it must be serializable
     * @return byte array
     * @throws IOException in case there is an io problem.
     */
    public static byte[] O2B(Object object) throws IOException {
        java.io.ByteArrayOutputStream bs = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream out = new java.io.ObjectOutputStream (bs);
        out.writeObject(object);
        out.flush();
        out.close();
        return bs.toByteArray();
    }

    /**
     * <p>
     *      Converts byte array into an Object, that
     *      can be cast into pre known class object.
     * </p>
     * @param bytes byte array
     * @return Object
     * @throws IOException case will return null
     * @throws ClassNotFoundException case will return null
     */
    public static Object B2O(byte bytes[]) throws IOException, ClassNotFoundException{

        Object object;
        java.io.ObjectInputStream in;
        java.io.ByteArrayInputStream bs;
        bs = new java.io.ByteArrayInputStream (bytes);
        in = new java.io.ObjectInputStream(bs);
        object = in.readObject();
        in.close ();
        bs.close ();
        return object;
    }

    /**
     * <p>
     *      Converts cMSgMessage into a xml string
     * </p>
     * @param m cMsgMessage object
     * @return  xml representation of the message
     */
    public static String msg2xml(cMsgMessage m){

        StringBuilder sb = new StringBuilder();
        sb.append("<msg>\n");
        sb.append("  <sender>").append(m.getSender()).append("</sender>\n");
        sb.append("  <subject>").append(m.getSubject()).append("</subject>\n");
        sb.append("  <type>").append(m.getType()).append("</type>\n");
        sb.append("  <text>").append(m.getText()).append("</text>\n");
        if(m.getByteArray()!=null){
            sb.append("  <bytearray>").append(m.getByteArray().length).append("</bytearray>\n");
        }
        for(cMsgPayloadItem pi:m.getPayloadItems().values()){
            if(pi!=null && !pi.getName().contains("cMsg")){
                try{
                    sb.append("  <payloadItem>\n");
                    sb.append("    <name>").append(pi.getName()).append("</name>\n");
                    sb.append("    <type>").append(pi.getType()).append("</type>\n");
                    switch(pi.getType()) {
                        case cMsgConstants.payloadStr:
                            sb.append("    <value>").append(pi.getString()).append("</value>\n");
                            break;
                        case cMsgConstants.payloadInt16:
                            sb.append("    <value>").append(pi.getShort()).append("</value>\n");
                            break;
                        case cMsgConstants.payloadInt32:
                            sb.append("    <value>").append(pi.getInt()).append("</value>\n");
                            break;
                        case cMsgConstants.payloadInt64:
                            sb.append("    <value>").append(pi.getLong()).append("</value>\n");
                            break;
                        case cMsgConstants.payloadFlt:
                            sb.append("    <value>").append(pi.getFloat()).append("</value>\n");
                            break;
                        case cMsgConstants.payloadDbl:
                            sb.append("    <value>").append(pi.getDouble()).append("</value>\n");
                            break;
                        case cMsgConstants.payloadStrA:
                            sb.append("    <value>\n");
                            for(String ss:pi.getStringArray()){
                                sb.append("     ").append(ss).append("\n");
                            }
                            sb.append("    </value>\n");
                            break;
                        case cMsgConstants.payloadInt16A:
                            sb.append("    <value>\n");
                            for(int ss:pi.getShortArray()){
                                sb.append("     ").append(ss).append("\n");
                            }
                            sb.append("    </value>\n");
                            break;
                        case cMsgConstants.payloadInt32A:
                            sb.append("    <value>\n");
                            for(int ss:pi.getIntArray()){
                                sb.append("     ").append(ss).append("\n");
                            }
                            sb.append("    </value>\n");
                            break;
                        case cMsgConstants.payloadInt64A:
                            sb.append("    <value>\n");
                            for(long ss:pi.getLongArray()){
                                sb.append("     ").append(ss).append("\n");
                            }
                            sb.append("    </value>\n");
                            break;
                        case cMsgConstants.payloadFltA:
                            sb.append("    <value>\n");
                            for(float ss:pi.getFloatArray()){
                                sb.append("     ").append(ss).append("\n");
                            }
                            sb.append("    </value>\n");
                            break;
                        case cMsgConstants.payloadDblA:
                            sb.append("    <value>\n");
                            for(double ss:pi.getDoubleArray()){
                                sb.append("     ").append(ss).append("\n");
                            }
                            sb.append("    </value>\n");
                            break;
                        case cMsgConstants.payloadBin:
                            sb.append("    <value> binary data </value>\n");
                            break;
                        case cMsgConstants.payloadBinA:
                            sb.append("    <value> binary array </value>\n");
                            break;
                        default:
                            break;
                    }
                    sb.append("  </payloadItem>\n");
                } catch (cMsgException e) {
                    e.printStackTrace();
                }
            }
        }
        sb.append("/<msg>\n");
        return sb.toString();
    }

    /**
     * Convenient method to force main thread to sleep.
     * @param msec sleep parameter is in msecods.
     */
    public static void sleep(int msec){
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>
     *     If the string passed is a number
     *     this will return integer umber,
     *     otherwise -1
     * </p>
     * @param s String representation of the number
     * @return integer number
     */
    public static int isNumber(String s){
        int r;
        try {
            r = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
        return r;
    }

    /**
     * <p>
     *     Generates a unique name.
     * </p>
     * @param prefix used with the rundom number
     * @return name
     */
    public static String generateName(String prefix){
        return prefix+"-"+new Random().nextInt(100);
    }

    /**
     * This method is for unix os only.
     * Takes the name of the process. Runs a shell process to find
     * the ID of the process with that name. Then starts another
     * unix process to kill the process with that id.
     * @param name of the process
     */
    public void removeUnixShellProcess(String name){
        ArrayList<String> cl = new ArrayList<>();
        cl.add("ps -C");
        cl.add(name);
        cl.add(" -o pid=");

        StdOutput sp = null;
        try {
            sp = fork(cl, true);
        } catch (AException e) {
            e.printStackTrace();
        }
        if(sp!=null) {
            Scanner sc = new Scanner(sp.getStdio());
            while (sc.hasNext()) {
                cl = new ArrayList<>();
                cl.add("kill -9 ");
                cl.add(sc.next());
                try {
                    fork(cl, true);
                } catch (AException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the current time and returns string representation of it.
     * @return string representing the current time.
     */
    public static String getCurrentTimeInH(){
        Format formatter = new SimpleDateFormat("HH:mm:ss MM/dd");
        return formatter.format(new Date());
    }

    /**
     * Gets the current time and returns string representation of it.
     * @return string representing the current time.
     */
    public static String getCurrentTime(){
        Format formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return formatter.format(new Date());
    }

    /**
     * Gets the current time and returns string representation of it.
     * @return string representing the current time.
     */
    public static String getCurrentTime(String format){
        Format formatter = new SimpleDateFormat(format);
        return formatter.format(new Date());
    }

    /**
     * Current time in milli-seconds.
     * @return current time in ms.
     */
    public static long getCurrentTimeInMs(){
        return new GregorianCalendar().getTimeInMillis();
    }

    /**
     * <p>
     * Sorts components according to their types
     * </p>

     * @param cps  Unsorted map of components
     * @return  sorted linked hash map of components
     */
    public static LinkedHashMap<String, AComponent> getSortedByType(Map<String, AComponent> cps){
        LinkedHashMap<String, AComponent> res = new LinkedHashMap<>();
        String cc = null;
        AComponent ccomp = null;
        ArrayList<AComponent> l = new ArrayList<>();
        for(ACodaType type:ACodaType.values()){
            l.clear();
            for(AComponent cmp:cps.values()){
                if(cmp.getType().equalsIgnoreCase(type.name())) {
                    if(cmp.getName().endsWith("_class")){
                       ccomp = cmp;
                       cc = cmp.getName();
                    } else {
                      l.add(cmp);
                    }
                }
            }
            if(ccomp!=null) {
                res.put(cc, ccomp);
            }
            for(AComponent aComponent:l){
                res.put(aComponent.getName(), aComponent);
            }
        }
        return res;
    }

    /**
     * String replacement
     * @param input original input string
     * @param rtv  String within the original string to be replaced
     * @param value a new string value to be replaced instead
     * @return resulting string
     */
    public static String rtvReplace(String input, String rtv, String value){
        String result = input;
        if(input.contains(rtv)){
            result = input.replace(rtv, value);
        }
        return result;

    }

    /**
     * Checks existence of undefined rtv in the string
     * (i.e. checks for existence of "%(" in teh string)
     * @param input original input string
     * @return true or false
     */
    public static boolean containsRTV(String input){
        return input.contains("%(");
    }

    /**
     * <p>
     *     Returns Afecs rtv's found in a string
     * </p>
     * @param input string
     * @return array of rtvs in the string
     */
    public static String[] getRTVsInAString(String input){
        List<String> l = new ArrayList<>();
        if(containsRTV(input)){
            StringTokenizer st = new StringTokenizer(input,"%(");

            while(st.hasMoreTokens()){
                String tmp = st.nextToken();
                if(tmp.contains(")")){
                    String tmp2 = tmp.substring(0, tmp.indexOf(")"));
                    String rtv = "%("+tmp2+")";
                    if(!l.contains(rtv)) l.add(rtv);
                }
            }
        }
        return l.toArray(new String[l.size()]);
    }

    /**
     * <p>
     *     Analyses the rtv, i.e. %(xyz), and returns the name of it, i.e.
     *     in the provided example this method will return the string xyz
     * </p>
     * @param rtv rtv syntax
     * @return rtv name
     */
    public static String getRTVName(String rtv){
        String res = null;
        if(rtv.startsWith("%(env.")){
            res = rtv.substring(rtv.indexOf(".") + 1, rtv.lastIndexOf(")"));
        } else if(rtv.startsWith("%(")){
            res = rtv.substring(rtv.indexOf("(")+1,rtv.lastIndexOf(")"));
        }
        return res;
    }

    /**
     * <p>
     *     Parses rtv xml file created by the
     *     JCedit and updated by the run-control
     * </p>
     * @param f rtv xml file object
     * @return map of key = rtv-syntax and value
     * @throws Exception
     */
    public static Map<String, String> parseRTVs(File f) throws Exception {
        Map<String,String> result = new HashMap<>();

        //Get the DOM Builder Factory
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        //Get the DOM Builder
        DocumentBuilder builder = factory.newDocumentBuilder();


        FileInputStream is = new FileInputStream(f);
        Document document = builder.parse(is);

        //Iterating through the nodes and extracting the data.
        NodeList nodeList = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            //We have encountered an <component> tag.
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                NodeList childNodes = node.getChildNodes();
                String tag = AConstants.udf;
                String val = "unset";
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node cNode = childNodes.item(j);
                    //Identifying the child tag of component encountered.
                    if (cNode instanceof Element) {
                        String content = cNode.getLastChild().getTextContent().trim();
                        if(cNode.getNodeName().equals("name")) {
                            tag = content;
                        } else if(cNode.getNodeName().equals("value")) {
                            val = content;
                        }
                    }
                    if(!tag.equals(AConstants.udf)){
                        result.put(tag,val);
                    }
                }
            }
        }
        return result;
    }

    /**
     * <p>
     *     Adds to the passed rtv map
     *     run-control system rtvs, such as:
     *     %(session), %(rt), %(ddb), %(udl), and %(env.xyz)
     * </p>
     * @return map of key = rtv-syntax and value
     * @param session run-control session
     * @param runType run-control run type
     */
    public static void updateRTV(Map<String, String> rtvMap,
                                 String session,
                                 String runType){

        if(rtvMap.containsKey("%(session)")){
            rtvMap.put("%(session)",session);
        }
        if (rtvMap.containsKey("%(rt)")){
            rtvMap.put("%(rt)",runType);
        }
        if(rtvMap.containsKey("%(ddb)")){
            rtvMap.put("%(ddb)",myConfig.getCoolHome()+File.separator+
                    myConfig.getPlatformExpid()+File.separator+"ddb");
        }
        if(rtvMap.containsKey("%(udl)")){
            rtvMap.put("%(udl)",myConfig.getPlatformUdl());
        }

        for(String s: rtvMap.keySet()){
            if(s.startsWith("%(env.")){
                String res = System.getenv(getRTVName(s));
                if(res!=null){
                    rtvMap.put(s,res);
                }
            }
        }
    }

    /**
     * <p>
     *     Dumps the rtv map into a file
     * </p>
     * @param fileName the name of a file
     * @param map rtv map
     */
    public static void writeRTVFile(String fileName,
                                    Map<String,String> map){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bw.write("<rtvs>\n");
            for(String r:map.keySet()){
                bw.write("  <rtv>\n");
                bw.write("    <name>"+r+"</name>\n");
                String v = map.get(r);
                if(v.contains("&")) v = v.replaceAll("&", "&amp;");
                bw.write("    <value>"+v+"</value>\n");
                bw.write("  </rtv>\n");
            }
            bw.write("</rtvs>\n");
            bw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.out.println(AfecsTool.stack2str(e1));
        }

    }

    /**
     * <p>
     *     Reads rtv files in the COOL database directory,
     *     namely:
     *     <ul>
     *         <li>
     *             $COOL_HOME/EXPID/user/rtv/runType/session_rtv.xml
     *         </li>
     *         <li>
     *             $COOL_HOME/EXPID/user/rtv/runType/undefined_rtv.xml
     *         </li>
     *     </ul>
     *     Parses rtv xml files and returns defined rtv map.
     * </p>
     * @param runType run control run-type
     * @param session run control session
     * @return rtv map
     */
    public static Map<String,String> readRTVFile(String runType,
                                                 String session){
        Map<String,String> definedRTVs = null;
        File f1= new File(myConfig.getCoolHome()+File.separator+
                myConfig.getPlatformExpid()+File.separator+
                "user"+File.separator+
                "rtv"+File.separator+
                runType+File.separator+
                session+"_rtv.xml");
        File f2= new File(myConfig.getCoolHome()+File.separator+
                myConfig.getPlatformExpid()+File.separator+
                "user"+File.separator+
                "rtv"+File.separator+
                runType+File.separator+
                "undefined_rtv.xml");

        try {
            if(f1.exists()){
                // parse cool defined RTV file for a specific configuration
                // file is found in  $COOL_HOME/expid/user/rtv/runtype_rtv.xml
                definedRTVs = AfecsTool.parseRTVs(f1);
            } else if(f2.exists()){
                definedRTVs = AfecsTool.parseRTVs(f2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return definedRTVs;
    }

    /**
     * <p>
     *     Checks rtvs in a input string and does
     *     appropriate substitution with defined
     *     rtv values.
     * </p>
     * @param tmps input string
     * @param setRTVs map containing rtvs and their values
     * @return substituted string
     */
    public static String checkRtvs(String tmps,
                                   Map<String,String> setRTVs){

        String res = "unset-rtv";

        // define all rtv's in the user config string
        // replace those we know how to replace
        String[] _rtvs = AfecsTool.getRTVsInAString(tmps);

        String result = tmps;
        for(String s: _rtvs){

            if(s.trim().equals("%(rn)")){
                res = result;
            } else {
                if(setRTVs.containsKey(s)){
                    if(setRTVs.get(s).trim().startsWith("unset")){
                        res = "unset-"+AfecsTool.getRTVName(s);
                    } else {
                        result = AfecsTool.rtvReplace(result,s,setRTVs.get(s));
                        res = result;
                    }
                }
            }
        }
        return res;
    }

    /**
     /**
     * <p>
     *     Checks rtvs in a input string and does
     *     appropriate substitution with defined
     *     rtv values.
     * </p>
     * @param tmps input string
     * @param setRTVs map containing rtvs and their values
     * @param runnumber run control run number to be substituted
     * @return substituted string
     */
    public static String replaceRtvsWithRunNumber(String tmps,
                                                  Map<String, String> setRTVs,
                                                  String runnumber){

        String res = "unset-rtv";

        // define all rtv's in the user config string
        // replace those we know how to replace
        String[] _rtvs = AfecsTool.getRTVsInAString(tmps);

        String result = tmps;
        for(String s: _rtvs){
            if(setRTVs.containsKey(s)){
                if(setRTVs.get(s).startsWith("unset")){
                    res = "unset-"+AfecsTool.getRTVName(s);
                } else {
                    if(s.equals("%(rn)")){
                        result = AfecsTool.rtvReplace(result,s,runnumber);
                    } else {
                        result = AfecsTool.rtvReplace(result,s,setRTVs.get(s));
                    }
                    res = result;
                }
            }
        }
        return res;
    }

    /**
     * <p>
     *     Converts exception stack trace to a string
     * </p>
     * @param e exception
     * @return String of the stack trace
     */
    public static String stack2str(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
        catch(Exception e2) {
            return "bad stack";
        }
    }


    /**
     * <p>
     *     Defines if AComponent map contains a
     *     component with a given state.
     * </p>
     * @param map AComponent map
     * @param state given state
     * @return true/false
     */
    public static boolean containsState(Map<String, AComponent> map, String state){
        for(AComponent c:map.values()){
            if(c.getState().equals(state)) return true;
        }
        return false;
    }

    public static String getLocalHostIP(){
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getHostFromIP(String ip){
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return addr.getHostName();
    }

    public static void main(String[] args) {
        String fileName = "/daqfs/home/gurjyan/db/afecs/experiment_7/user/parms/farm_p.xml" ;
        String[] tags = {"node","command"};
        List<XMLContainer> fcd = AfecsTool.parseXML(fileName,
                "process",tags);

        for(XMLContainer x:fcd){
            System.out.print(" " + x);
            System.out.println("\n");
        }
    }
}
