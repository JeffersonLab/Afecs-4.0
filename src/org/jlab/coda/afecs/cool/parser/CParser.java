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

package org.jlab.coda.afecs.cool.parser;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdql.*;

import com.sun.xml.internal.rngom.digested.DOptionalPattern;
import org.jlab.coda.afecs.client.AClientInfo;
import org.jlab.coda.afecs.codarc.CodaRCAgent;
import org.jlab.coda.afecs.container.AContainer;
import org.jlab.coda.afecs.cool.ontology.*;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConfig;
import org.jlab.coda.afecs.system.AConstants;

import org.jlab.coda.afecs.system.util.AfecsTool;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

public class CParser {
    // Jena model
    private Model Gmodel;

    // List of the included cool configuration file jena models
    private HashMap<String, Model> includeModeles = new HashMap<>();

    // Singleton object. Get system const variables, (reads setup.xml).
    private AConfig myConfig = AConfig.getInstance();

    // Cool  top level configuration files directory
    private String _coolHome = myConfig.getCoolHome() + File.separator + myConfig.getPlatformExpid() + File.separator + "config" + File.separator;

    // Cool  top level configuration files directory
    private String _userConfigDir = myConfig.getCoolHome() + File.separator + myConfig.getPlatformExpid() + File.separator + "user" + File.separator;

    // default option directories for the control
    private Set<String> _opDirs = new HashSet<>();


    // this is passed by the rcGui rtv table set by the user at the run time.
    private Map<String, String> setRTVs;

    private int numberOfFileComponents;
    private int erId = -1, pebId = -1, sebId = -1, ebId = -1, cdebId = -1, dcId = -1, usrId = -1;

    private AContainer myContainer;


    public CParser(Map<String, String> usrSetRTVs, AContainer con) {
        if (usrSetRTVs != null) {
            this.setRTVs = usrSetRTVs;
        } else {
            setRTVs = new HashMap<>();
        }
        myContainer = con;
    }

    /**
     * Constructor
     *
     * @param fileName of the rdf/cool configuration file
     * @param debug    if true prints statements of the cool description
     * @return stat status of construction
     */
    public boolean openFile(String fileName, boolean debug) {
        boolean stat;
        stat = createModel(_coolHome + "Control" + File.separator + fileName) && generateFinalModel();
//        printStatements(Gmodel);
        return stat;
    }


    /**
     * parses Cool control concept and fills {@link org.jlab.coda.afecs.cool.ontology.AControl} objetc
     *
     * @param runType of the control
     * @return AControl object
     */
    public AControl parseControl(String runType, boolean onlyComponents) {
        numberOfFileComponents = 0;
        usrId = erId = pebId = sebId = ebId = cdebId = dcId = -1;

//        default directory for option files
        String fn = _coolHome + "Control" + File.separator + runType + File.separator + "Options";
        _opDirs.add(fn);

        AControl c = new AControl();
        c.setName(runType);
        ArrayList<AComponent> al = parseComponent(AConstants.COOL_HTTP_BASE + "Control" +
                File.separator + runType +
                File.separator + runType + "#" + runType, "hasComponent");
        c.setComponents(al);

        // update Container agentMap with new configuration data
        for (AComponent agent : al) {

            if (myContainer.getContainerAgents().containsKey(agent.getName())) {
                // update registered agent information on the container
                CodaRCAgent cAgent = myContainer.getContainerAgents().get(agent.getName());
                if (cAgent != null && cAgent.me.getClient() != null) {

                    // saving IP information
                    Map<String, String[]> lip = cAgent.me.getLinkedIp();
                    Map<String, String[]> lba = cAgent.me.getLinkedBa();
                    AClientInfo ai = cAgent.me.getClient();
                    // putting back IP information
                    cAgent.me.setClient(ai);
                    cAgent.me.setLinkedIp(lip);
                    cAgent.me.setLinkedBa(lba);
                    // update registration
                    myContainer.myPlatform.registrar.addAgent(cAgent.me);
                    myContainer.myPlatform.registrar.addClient(cAgent.me.getClient());

                    // update linked components IP information
                    for (String linkedCompName : cAgent.me.getLinkedComponentNames()) {
                        CodaRCAgent linkedAgent = cAgent.myContainer.getContainerAgents().get(linkedCompName);
                        linkedAgent.agentControlRequestNetworkDetails(
                                cAgent.myName,
                                cAgent.me.getClient().getHostIps(),
                                cAgent.me.getClient().getHostBroadcastAddresses()
                        );
                        // update registration
                        myContainer.myPlatform.registrar.addClient(linkedAgent.me.getClient());
                    }
                }
            }
        }

        if (!onlyComponents) {

            c.setGui(parseHmi(AConstants.COOL_HTTP_BASE + "Control" +
                    File.separator + runType +
                    File.separator + runType + "#" + runType, "hasHmi"));

            c.setOption(parseOption(AConstants.COOL_HTTP_BASE + "Control" +
                    File.separator + runType +
                    File.separator + runType + "#" + runType, "hasOption"));


            // create a supervisor agent for this control
            AComponent duper = new AComponent();
            duper.setName("sms_" + runType);
            duper.setType("SMS");
            duper.setPriority(ACodaType.SMS.priority());

            // add options to the supervisor agent
            if (c.getOption() != null) {
                if (c.getOption().getEventLimit() > 0) {
                    duper.setEventLimit(c.getOption().getEventLimit());
                }
                if (c.getOption().getDataLimit() > 0) {
                    //@todo is not implemented for AComponent
                }
            }

            // parse and add described processes for the control to the supervisor agent
            ArrayList<AProcess> processlist = parseProcess(AConstants.COOL_HTTP_BASE + "Control" +
                    File.separator + runType +
                    File.separator + runType + "#" + runType, "hasProcess");

            if (processlist != null && !processlist.isEmpty()) {
                duper.setProcesses(processlist);
            }

            //add supervisor agent to the control
            c.setSupervisor(duper);
        }
        defineStreams(c.getComponents());

        return c;
    }

    /**
     * Defines number of streams defined in the configuration
     *
     * @param comps List of control agents
     */
    private void defineStreams(List<AComponent> comps) {
//        System.out.println("DDD -----| Info: StreamCount = "+numberOfFileComponents);
        for (AComponent comp : comps) {
            comp.setStreamCount(numberOfFileComponents);
        }
    }

    /**
     * Creates Jena model of the rdf/cool file
     *
     * @param fileName of the rdf/cool configuration file direct path
     * @return stat status of the operation
     */
    private boolean createModel(String fileName) {
        boolean stat = true;
        FileInputStream fis;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
// create the jena model
        Model model = ModelFactory.createDefaultModel();

        try {
            model.read(fis, AConstants.COOL_HTTP_BASE, "RDF/XML");
        } catch (Exception ee) {
            System.out.println(AfecsTool.stack2str(ee));
            return false;
        }

        // add this model to the model list
        if (!includeModeles.containsKey(fileName)) {
            includeModeles.put(fileName, model);
        }

        // list the statements in the Model
        StmtIterator iter = model.listStatements();

        // print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            RDFNode node = stmt.getObject();

            if (node instanceof Resource) {
                if ((node.toString().endsWith(".rdf"))) {
                    String incName;
                    if (node.toString().contains(AConstants.COOL_HTTP_BASE)) {
                        incName = replace(node.toString(), AConstants.COOL_HTTP_BASE, _coolHome);
                    } else {
                        return false;
                    }
                    if (incName.contains("Option")) {
                        String opp = incName.substring(0, incName.lastIndexOf(File.separator));

                        _opDirs.add(opp);
                    }
                    stat = createModel(incName);
                    if (!stat) return false;
                }
            }
        }
        return stat;
    }


    /**
     * Creates one big jena model including all submodels.
     *
     * @return stat status of the operation
     */
    private boolean generateFinalModel() {
        boolean stat = false;
        if (!includeModeles.isEmpty()) {
            // create union of the jena models
            Gmodel = ModelFactory.createDefaultModel();
            for (String s : includeModeles.keySet()) {
                Gmodel = Gmodel.union(includeModeles.get(s));
            }
            stat = true;
        }
        return stat;
    }

    /**
     * Replaces the substring of the given string with the new string
     *
     * @param str     given string
     * @param pattern substring
     * @param replace the new string
     * @return result string
     */
    private String replace(String str, String pattern, String replace) {
        int s = 0;
        int e;
        StringBuilder result = new StringBuilder();

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }


    /**
     * Debugging method prints all the rdf/cool statements of the model
     *
     * @param model Jena model object
     */
    public void printStatements(Model model) {
        // list the statements in the Model
        StmtIterator iter = model.listStatements();

        // print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            Resource subject = stmt.getSubject();
            Property predicate = stmt.getPredicate();
            RDFNode node = stmt.getObject();
            System.out.println("subject   = " + subject.toString());
            System.out.println("predicate = " + predicate.toString());
            if (node instanceof Resource) {
                System.out.println("resource = " + node.toString());
            } else {
                System.out.println("literal = " + node.toString());
            }
            System.out.println();
        }
    }

    /**
     * Method creates a query from the string and executes it within the
     * jena model and returns the result as a string.
     *
     * @param subject   object of the cool concept
     * @param predicate String: cool predicate ( for example has timestamp, hasdatatype, etc.)
     * @return result           string
     */
    private String getValue(Object subject, String predicate) {
        Object x = null;
        String sq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                "> <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        try {
            Query query = new Query(sq);
            query.setSource(Gmodel);
            QueryExecution qe = new QueryEngine(query);

            QueryResults results = qe.exec();
            for (; results.hasNext(); ) {
                ResultBinding res = (ResultBinding) results.next();
                x = res.get("x");
            }
            results.close();
        } catch (Exception ex) {
            System.out.println(AfecsTool.stack2str(ex));
        }
        if (x == null) {
            return null;
        } else return x.toString();
    }


    /**
     * Method creates a query from the string and executes it within the
     * jena model and returns the result as a list of strings.
     *
     * @param subject   object of the cool concept
     * @param predicate String: cool predicate ( for example has timestamp, hasdatatype, etc.)
     * @return result           string
     */
    private ArrayList<String> getValueList(Object subject, String predicate) {
        Object x;
        ArrayList<String> l = new ArrayList<String>();

        String sq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                "> <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        try {
            Query query = new Query(sq);
            query.setSource(Gmodel);
            QueryExecution qe = new QueryEngine(query);

            QueryResults results = qe.exec();
            for (; results.hasNext(); ) {
                ResultBinding res = (ResultBinding) results.next();
                x = res.get("x");
                if (x != null) l.add(x.toString());
            }
            results.close();
        } catch (Exception ex) {
            System.out.println(AfecsTool.stack2str(ex));
        }
        return l;
    }


    /**
     * Parsing the component of the control.
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return cl               list of {@link org.jlab.coda.afecs.cool.ontology.AComponent} objects
     */

    private ArrayList<AComponent> parseComponent(Object subject, String predicate) {
        AComponent cmp;
        ArrayList<AProcess> processlist;
        ArrayList<AState> statelist;
        ArrayList<AService> services;
        HMI gui;
        APlugin plugin;
        AOption option;
        ArrayList<ALink> link;
        String tmps;

        ArrayList<AComponent> cl = new ArrayList<>();
        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;
            cmp = new AComponent();

            tmps = getValue(x, "hasName");
            if (tmps != null) {
                cmp.setName(tmps);
            } else {
                System.out.println("COOL: Component " + x + " does not have a name");
                return null;
            }

            tmps = getValue(x, "hasClassPath");
            if (tmps != null) {
                cmp.setClassPath(tmps);
            }

            tmps = getValue(x, "hasClassName");
            if (tmps != null) {
                cmp.setClassName(tmps);
            }

            tmps = getValue(x, "hasType");
            if (tmps != null) {
                if (tmps.equals(ACodaType.FILE.name())) {
                    continue;
                } else {
                    cmp.setType(tmps);
                    if (tmps.equals(ACodaType.ER.name())) {
                        erId++;
                        cmp.setStreamId(erId);
                    } else if (tmps.equals(ACodaType.PEB.name())) {
                        numberOfFileComponents++;
                        pebId++;
                        cmp.setStreamId(pebId);
                    } else if (tmps.equals(ACodaType.SEB.name())) {
                        numberOfFileComponents++;
                        sebId++;
                        cmp.setStreamId(sebId);
                    } else if (tmps.equals(ACodaType.EB.name())) {
                        ebId++;
                        cmp.setStreamId(ebId);
                    } else if (tmps.equals(ACodaType.EBER.name())) {
                        cdebId++;
                        cmp.setStreamId(cdebId);
                    } else if (tmps.equals(ACodaType.DC.name())) {
                        dcId++;
                        cmp.setStreamId(dcId);
                    } else if (tmps.equals(ACodaType.USR.name())) {
                        usrId++;
                        cmp.setStreamId(usrId);
                    }
                }
            }


            tmps = getValue(x, "hasID");
            if (tmps != null) {
                try {
                    cmp.setId(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "hasDescription");
            if (tmps != null) {
                cmp.setDescription(tmps);
            }

            tmps = getValue(x, "hasHost");
            if (tmps != null) {
                cmp.setHost(tmps);
            }

//            tmps = getValue(x, "hasCode");
//            if (tmps != null) {
//                if (AfecsTool.containsRTV(tmps)) {
//                    cmp.setCode(AfecsTool.checkRtvs(tmps, setRTVs));
//                } else {
//                    cmp.setCode(tmps);
//                }
//            }

            tmps = getValue(x, "hasPriority");
            if (tmps != null) {
                try {
                    cmp.setPriority(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                if (cmp.getType().equalsIgnoreCase(ACodaType.USR.name()))
                    cmp.setPriority(ACodaType.USR.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.SLC.name()))
                    cmp.setPriority(ACodaType.SLC.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.WNC.name()))
                    cmp.setPriority(ACodaType.WNC.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.ER.name()))
                    cmp.setPriority(ACodaType.ER.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.FCS.name()))
                    cmp.setPriority(ACodaType.FCS.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.PEB.name()))
                    cmp.setPriority(ACodaType.PEB.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.SEB.name()))
                    cmp.setPriority(ACodaType.SEB.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.EB.name()))
                    cmp.setPriority(ACodaType.EB.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.EBER.name()))
                    cmp.setPriority(ACodaType.EBER.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.DC.name()))
                    cmp.setPriority(ACodaType.DC.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.ROC.name()))
                    cmp.setPriority(ACodaType.ROC.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.GT.name()))
                    cmp.setPriority(ACodaType.GT.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.TS.name()))
                    cmp.setPriority(ACodaType.TS.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.SMS.name()))
                    cmp.setPriority(ACodaType.SMS.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.RCS.name()))
                    cmp.setPriority(ACodaType.RCS.priority());
                else if (cmp.getType().equalsIgnoreCase(ACodaType.FILE.name()))
                    cmp.setPriority(ACodaType.FILE.priority());
                else
                    System.err.println("Error: " + cmp.getType() + " type is not defined for the component " + " " + cmp.getName());
            }

            tmps = getValue(x, "hasUserConfig");
            if (tmps != null) {
                if (AfecsTool.containsRTV(tmps)) {
                    cmp.setUserConfig(AfecsTool.checkRtvs(tmps, setRTVs));
                } else if (tmps.startsWith("/")) {
                    cmp.setUserConfig(tmps);
                } else {
                    cmp.setUserConfig(_userConfigDir + tmps);
                }
            }

            tmps = getValue(x, "hasSession");
            if (tmps != null) {
                cmp.setSession(tmps);
            }

            tmps = getValue(x, "hasRunType");
            if (tmps != null) cmp.setRunType(tmps);

            option = parseOption(x, "hasOption");
            if (option != null) {
                cmp.setOption(option);
                for (String s : _opDirs) {
                    cmp.addDod(s);
                }
            }

            tmps = getValue(x, "representsCoda2Component");
            if (tmps != null) {
                cmp.setCoda2Component(tmps);
            }

            tmps = getValue(x, "isCodaComponent");
            if (tmps != null) {
                cmp.setCodaComponent(tmps);
            }


            link = parseLink(x, "usesLink");
            if (link != null) {
                cmp.setLinks(link);
            }

            tmps = getValue(x, "hasReportingInterval");
            if (tmps != null) {
                try {
                    cmp.setReportingInterval(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }


            statelist = parseStates(x, "hasState");

            if (statelist != null && !statelist.isEmpty()) {

                cmp.setStates(statelist);
            }
            processlist = parseProcess(x, "hasProcess");
            if (processlist != null && !processlist.isEmpty()) {
                cmp.setProcesses(processlist);
            }

            services = parseServices(x, "providesService");
            if (services != null && !services.isEmpty()) {
                cmp.setServices(services);
            }

            gui = parseHmi(x, "hasHMI");
            if (gui != null) {
                cmp.setGui(gui);
            }

            plugin = parsePlugin(x, "hasCommunicationPlugin");
            if (plugin != null) {
                cmp.setPlugin(plugin);
            }

            tmps = getValue(x, "XCo");
            if (tmps != null) {
                try {
                    cmp.setX(Double.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "YCo");
            if (tmps != null) {
                try {
                    cmp.setY(Double.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "WCo");
            if (tmps != null) {
                try {
                    cmp.setW(Double.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "HCo");
            if (tmps != null) {
                try {
                    cmp.setH(Double.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            ArrayList<String> tmL = getValueList(x, "linkedTo");
            if (tmL != null && !tmL.isEmpty()) {
                cmp.setLinkedComponentNames(tmL);
            }

            ArrayList<String> tmLt = getValueList(x, "linkedToType");
            if (tmLt != null && !tmLt.isEmpty()) {
                cmp.setLinkedComponentTypes(tmLt);
            }

            // If type is ROC or TS read component specific .dat configuration
            // file created by JcEdit to get the mastership of the ROC
            if (cmp.getType().equals(ACodaType.ROC.name()) ||
                    cmp.getType().equals(ACodaType.TS.name()) ||
                    cmp.getType().equals(ACodaType.GT.name())) {
                String configFileName = _coolHome +
                        "Control" + File.separator +
                        cmp.getRunType() + File.separator +
                        "Options" + File.separator +
                        cmp.getName() + ".dat";
                try {
                    BufferedReader br = new BufferedReader(new FileReader(configFileName));
                    Stream<String> l = br.lines();
                    try {
                        String s = l.filter(line -> line.contains("isMaster")).findFirst().get();
                        if (s.split("=")[1].equals("true")) {
                            cmp.setMaster(true);
                        }
                    } catch (NoSuchElementException e) {
                        e.printStackTrace();
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            cl.add(cmp);
        }
        results.close();
        return cl;
    }

    private void isMaster(String s) {
       ;
    }

    /**
     * Method parses the cool Plugin concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return states           The {@link org.jlab.coda.afecs.cool.ontology.APlugin} object
     */
    private APlugin parsePlugin(Object subject, String predicate) {
        APlugin plugin = null;
        String name;
        String description;
        String className;

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;
            plugin = new APlugin();

            name = getValue(x, "hasName");
            if (name != null) {
                plugin.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the Plugin of the" + x.toString());
                return null;
            }

            className = getValue(x, "hasClass");
            if (className != null) {
                plugin.setClassName(className);
            } else {
                System.out.println("COOL: ClassName is not defined for the Plugin of the" + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                plugin.setDescription(description);
            }

        }
        results.close();
        return plugin;
    }

    /**
     * Method parses the cool State concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return states           List of the {@link org.jlab.coda.afecs.cool.ontology.AState} objects
     */
    private ArrayList<AState> parseStates(Object subject, String predicate) {
        AState state;
        String name;
        String description;
        ArrayList<String> compNames;
        ArrayList<AProcess> processes;


        ArrayList<AState> states = new ArrayList<>();

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;

            state = new AState();

            name = getValue(x, "hasName");
            if (name != null) {
                state.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the State of the" + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                state.setDescription(description);
            }


            processes = parseProcess(x, "achievedThrough");
            if (processes != null && !processes.isEmpty()) {
                state.setProcesses(processes);
            }

            compNames = getValueList(x, "addressedTo");
            if (compNames != null && !compNames.isEmpty()) {
                state.setComponentNames(compNames);
            }
            states.add(state);
        }
        results.close();
        return states;
    }

    /**
     * parses Cool process concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return pl              List of {@link org.jlab.coda.afecs.cool.ontology.AProcess} objects
     */
    private ArrayList<AProcess> parseProcess(Object subject, String predicate) {
        AProcess process;
        String name, before, after, status, initiator, description, critical, sync;
        ArrayList<AScript> scripts;
        ArrayList<APackage> sendPackages;
        ArrayList<APackage> receivePackages;
        String tmps;
        ArrayList<AProcess> pl = new ArrayList<>();

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                "> <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;
            process = new AProcess();

            name = getValue(x, "hasName");
            if (name != null) {
                process.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the Process " + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                process.setDescription(description);
            }

            critical = getValue(x, "isCritical");
            if (critical != null) {
                process.setCritical(critical);
            }

            sync = getValue(x, "isSync");
            if (sync != null) {
                process.setSync(sync);
            }

            before = getValue(x, "before");
            if (before != null) {
                process.setBefore(before);
            }

            after = getValue(x, "after");
            if (after != null) {
                process.setAfter(after);
            }

            tmps = getValue(x, "hasPeriodicity");
            if (tmps != null) {
                process.setPeriodicity(Integer.parseInt(tmps));
            }

            tmps = getValue(x, "hasDelay");
            if (tmps != null) {
                process.setDelay(Integer.parseInt(tmps));
            }

            tmps = getValue(x, "hasTimeOut");
            // Note: time-outs are in seconds
            if (tmps != null) {
                process.setTimeout(Integer.parseInt(tmps));
            }

            tmps = getValue(x, "hasRequestId");
            if (tmps != null) {
                process.setRequestid(Integer.parseInt(tmps));
            }

            status = getValue(x, "status");
            if (status != null) {
                process.setStatus(status);
            }

            initiator = getValue(x, "isInitiator");
            if (initiator != null) {
                process.setInitiator(initiator);
            }
            scripts = parseScript(x, "runs");
            if (scripts != null && !scripts.isEmpty()) {
                process.setScripts(scripts);
            }

            sendPackages = parsePackage(x, "sends");
            if (sendPackages != null && !sendPackages.isEmpty()) {
                process.setSendPackages(sendPackages);
            }
            receivePackages = parsePackage(x, "receives");
            if (receivePackages != null && !receivePackages.isEmpty()) {
                process.setReceivePackages(receivePackages);
            }

            pl.add(process);
        }
        results.close();
        return pl;
    }

    /**
     * Parses Cool service concept (object of the rdf statement) and
     * fills arraylist of {@link org.jlab.coda.afecs.cool.ontology.AService} objects.
     *
     * @param subject   of the rdf statement
     * @param predicate of the rdf statement
     * @return AService objects.
     */
    private ArrayList<AService> parseServices(Object subject, String predicate) {
        AService service;
        ArrayList<AService> services = new ArrayList<AService>();
        String name;
        String description;
        String owner;
        ARule stateMachineRule;

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;
            service = new AService();

            name = getValue(x, "hasName");
            if (name != null) {
                service.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the Service " + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                service.setDescription(description);
            }

            owner = getValue(x, "hasOwner");
            if (owner != null) {
                service.setOwner(owner);
            }

            stateMachineRule = parseRule(x, "startsStatemachine");
            if (stateMachineRule != null) {
                service.setStateMachineRule(stateMachineRule);
            }
            services.add(service);
        }
        results.close();
        return services;
    }

    /**
     * Method parses the cool rule concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return rule             {@link org.jlab.coda.afecs.cool.ontology.ARule} object
     */
    private ARule parseRule(Object subject, String predicate) {
        ARule rule = null;
        String name, description, code;

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;

            rule = new ARule();

            name = getValue(x, "hasName");
            if (name != null) {
                rule.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the rule of the" + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                rule.setDescription(description);
            }

            code = getValue(x, "hasCode");
            if (code != null) {
                rule.setCode(code);
            } else {
                System.out.println("COOL: Code is not defined for the rule of the" + x.toString());
            }
        }
        results.close();
        return rule;
    }

    private ArrayList<ALink>


    parseLink(Object subject, String predicate) {
        ALink link;
        String tmps;
        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();

        ArrayList<ALink> al = new ArrayList<ALink>();

        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;

            link = new ALink();

            tmps = getValue(x, "hasName");
            if (tmps != null) {
                link.setName(tmps);
            } else {
                System.out.println("COOL: Name is not defined for the linkPoint " + x.toString());
                return null;
            }

            tmps = getValue(x, "hasDescription");
            if (tmps != null) {
                link.setDescription(tmps);
            }

            tmps = getValue(x, "hasTransport");
            if (tmps != null) {
                link.setTransport(tmps);
            }

            tmps = getValue(x, "sourceComponentName");
            if (tmps != null) {
                link.setSourceComponentName(tmps);
            }

            tmps = getValue(x, "sourceHost");
            if (tmps != null) {
                link.setSourceHost(tmps);
            }

            tmps = getValue(x, "sourcePort");
            if (tmps != null) {
                try {
                    link.setSourcePort(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "destinationComponentName");
            if (tmps != null) {
                link.setDestinationComponentName(tmps);
            }

            tmps = getValue(x, "destinationHost");
            if (tmps != null) {
                link.setDestinationHost(tmps);
            }

            tmps = getValue(x, "destinationPort");
            if (tmps != null) {
                try {
                    link.setDestinationPort(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "destinationETNumberEvents");
            if (tmps != null) {
                try {
                    link.setDestinationETNumberEvents(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "destinationETEventSize");
            if (tmps != null) {
                try {
                    link.setDestinationETEventSize(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "startX");
            if (tmps != null) {
                try {
                    link.setStartX(Double.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "startY");
            if (tmps != null) {
                try {
                    link.setStartY(Double.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "endX");
            if (tmps != null) {
                try {
                    link.setEndX(Double.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "endY");
            if (tmps != null) {
                try {
                    link.setEndY(Double.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }


            al.add(link);

        }
        results.close();
        return al;
    }

    /**
     * Method parses the cool option concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return option           {@link org.jlab.coda.afecs.cool.ontology.AOption} object
     */
    private AOption parseOption(Object subject, String predicate) {
        AOption option = null;
        String name, description, dataFile, dataFilePrefix, coda2Component,
                autoStart, startRun, configFile, configString, tmps;

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;

            option = new AOption();

            name = getValue(x, "hasName");
            if (name != null) {
                option.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the option " + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                option.setDescription(description);
            }

            dataFile = getValue(x, "hasDataFile");
            if (dataFile != null) {
                option.setDataFile(dataFile);
            }

            dataFilePrefix = getValue(x, "hasDataFilePrefix");
            if (dataFilePrefix != null) {
                option.setDataFilePrefix(dataFilePrefix);
            }

            coda2Component = getValue(x, "hasCoda2Component");
            if (coda2Component != null) {
                option.setCoda2Component(coda2Component);
            }

            autoStart = getValue(x, "hasAutoStart");
            if (autoStart != null) {
                option.setAutoStart(autoStart);
            }

            startRun = getValue(x, "hasStartRun");
            if (startRun != null) {
                option.setStartRun(startRun);
            }

            configFile = getValue(x, "hasConfigFile");
            if (configFile != null) {
                option.setConfigFile(configFile);
            }

            configString = getValue(x, "hasConfigString");
            if (configString != null) {
                option.setConfigString(configString);
            }

            tmps = getValue(x, "hasDownloadString");
            if (tmps != null) {
                option.setDownloadString(tmps);
            }

            tmps = getValue(x, "hasPrestartString");
            if (tmps != null) {
                option.setPrestartString(tmps);
            }

            tmps = getValue(x, "hasGoString");
            if (tmps != null) {
                option.setGoString(tmps);
            }

            tmps = getValue(x, "hasEndString");
            if (tmps != null) {
                option.setEndString(tmps);
            }


            tmps = getValue(x, "hasEventLimit");
            if (tmps != null) {
                try {
                    option.setEventLimit(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "hasDataLimit");
            if (tmps != null) {
                try {
                    option.setDataLimit(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

        }
        results.close();
        return option;
    }


    /**
     * Method parses the cool package concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return list of          {@link org.jlab.coda.afecs.cool.ontology.APackage} objects
     */
    private ArrayList<APackage> parsePackage(Object subject, String predicate) {
        APackage pk;
        String name;
        String description;
        String tmp;
        String subj;
        String type;
        String text;
        ArrayList<String> receivedTexts;
        ArrayList<AChannel> channels;

        ArrayList<APackage> al = new ArrayList<APackage>();

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;
            pk = new APackage();

            name = getValue(x, "hasName");
            if (name != null) {
                pk.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the package " + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                pk.setDescription(description);
            }

            subj = getValue(x, "hasSendSubject");
            if (subj != null) {
                pk.setSendSubject(subj);
            }

            type = getValue(x, "hasSendType");
            if (type != null) {
                pk.setSendType(type);
            }

            text = getValue(x, "hasSendText");
            if (text != null) {
                pk.setSendText(text);
            }

            subj = getValue(x, "hasReceivedSubject");
            if (subj != null) {
                pk.setReceivedSubject(subj);
            }

            type = getValue(x, "hasReceivedType");
            if (type != null) {
                pk.setReceivedType(type);
            }

            receivedTexts = getValueList(x, "hasReceivedText");
            if (receivedTexts != null && !receivedTexts.isEmpty()) {
                pk.setReceivedText(receivedTexts);
            }

            tmp = getValue(x, "isForRcClient");
            if (tmp != null) {
                pk.setForRcClient(tmp);
            }

            tmp = getValue(x, "isForNativecMsg");
            if (tmp != null) {
                pk.setForNativecMsg(tmp);
            }

            tmp = getValue(x, "isForShellClient");
            if (tmp != null) {
                pk.setForShellClient(tmp);
            }

            channels = parseChannel(x, "hasChannel");
            if (channels != null && !channels.isEmpty()) {
                pk.setChannels(channels);
            }

            al.add(pk);
        }
        results.close();
        return al;
    }

    /**
     * Method parses the cool channel concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return list of          {@link org.jlab.coda.afecs.cool.ontology.AChannel} objects
     */
    private ArrayList<AChannel> parseChannel(Object subject, String predicate) {
        AChannel ch;
        String name;
        String value;
        String minValue;
        String maxValue;
        String setValue;
        String tmps;
        String unit;
        String description;

        ArrayList<AChannel> al = new ArrayList<AChannel>();

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;
            ch = new AChannel();

            name = getValue(x, "hasName");
            if (name != null) {
                ch.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the channel " + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                ch.setDescription(description);
            }

            tmps = getValue(x, "hasValueType");
            if (tmps != null) {
                try {
                    ch.setValueType(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            value = getValue(x, "hasValue");
            if (value != null) {
                ch.setValue(value);
            }

            minValue = getValue(x, "hasMinValue");
            if (minValue != null) {
                ch.setMinValue(minValue);
            }

            maxValue = getValue(x, "hasMaxValue");
            if (maxValue != null) {
                ch.setMaxValue(maxValue);
            }

            setValue = getValue(x, "hasSetValue");
            if (setValue != null) {
                ch.setSetValue(setValue);
            }

            unit = getValue(x, "hasUnit");
            if (unit != null) {
                ch.setUnit(unit);
            }

            al.add(ch);
        }
        results.close();
        return al;
    }

    /**
     * Method parses the cool script concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return list of          {@link org.jlab.coda.afecs.cool.ontology.AScript} objects
     */
    private ArrayList<AScript> parseScript(Object subject, String predicate) {
        AScript sc;
        String name;
        String description;
        String commandString;
        String sync;
        String tmp;
        ArrayList<AScript> al = new ArrayList<>();

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;
            sc = new AScript();

            name = getValue(x, "hasName");
            if (name != null) {
                sc.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the script " + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                sc.setDescription(description);
            }

            commandString = getValue(x, "hasCommandString");
            if (commandString != null) {
                if (AfecsTool.containsRTV(commandString)) {
                    sc.setCommandString(AfecsTool.checkRtvs(commandString, setRTVs));
                } else {
                    sc.setCommandString(commandString);
                }
            }

            tmp = getValue(x, "hasExitCode");
            if (tmp != null) {
                try {
                    sc.setExitCode(Integer.valueOf(tmp));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            sync = getValue(x, "isSynchronous");
            if (sync != null) {
                sc.setSync(sync);
            }

            tmp = getValue(x, "hasTimeout");
            if (tmp != null) {
                try {
                    sc.setTimeout(Integer.valueOf(tmp));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            al.add(sc);
        }
        results.close();
        return al;
    }

    /**
     * Method parses the cool hmi concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return gui             {@link org.jlab.coda.afecs.cool.ontology.HMI} objects
     */
    private HMI parseHmi(Object subject, String predicate) {
        HMI h = null;
        String name;
        String description;
        ArrayList<APanel> panels;
        String isWebBased;
        String tmps;

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;
            h = new HMI();

            name = getValue(x, "hasName");
            if (name != null) {
                h.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the hmi " + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                h.setDescription(description);
            }

            isWebBased = getValue(x, "isWebBased");
            if (isWebBased != null) {
                h.setWebBased(isWebBased);
            }

            tmps = getValue(x, "hasRows");
            if (tmps != null) {
                try {
                    h.setRowsNumber(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            tmps = getValue(x, "hasColumns");
            if (tmps != null) {
                try {
                    h.setColumnsNumber(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            panels = parsePanel(x, "hasPanel");
            if (panels != null && !panels.isEmpty()) {
                h.setPanels(panels);
            }

        }
        results.close();
        return h;
    }

    /**
     * Method parses the cool panel concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return list of          {@link APanel} objects
     */
    private ArrayList<APanel> parsePanel(Object subject, String predicate) {
        APanel p;
        String name;
        String description;
        String color;
        String title;
        ArrayList<AWidget> widgets;
        String tmps;
        ArrayList<APanel> al = new ArrayList<APanel>();

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;
            p = new APanel();

            name = getValue(x, "hasName");
            if (name != null) {
                p.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the panel " + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                p.setDescription(description);
            }

            color = getValue(x, "hasColor");
            if (color != null) {
                p.setColor(color);
            }

            title = getValue(x, "hasTitle");
            if (title != null) {
                p.setTitle(title);
            }

            tmps = getValue(x, "hasNumber");
            if (tmps != null) {
                try {
                    p.setNumber(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            widgets = parseWidget(x, "hasWidget");
            if (widgets != null && !widgets.isEmpty()) {
                p.setWidgets(widgets);
            }

            al.add(p);
        }
        results.close();
        return al;
    }

    /**
     * Method parses the cool widget concept
     *
     * @param subject   object of the cool concept
     * @param predicate String of the cool predicate name
     * @return list of          {@link org.jlab.coda.afecs.cool.ontology.AWidget} objects
     */
    private ArrayList<AWidget> parseWidget(Object subject, String predicate) {
        AWidget w;
        String name;
        String description;
        String title;
        String tmps;
        String type;
        String subType;
        String icon;
        String editable;
        ArrayList<AService> actionServices;
        ArrayList<AProcess> actionProcesses;
        ArrayList<AState> actionStates;
        ArrayList<AWidget> al = new ArrayList<AWidget>();

        String tq = "SELECT ?x " + "WHERE(<" + subject.toString() +
                ">, <" + AConstants.COOL_CORE + "" + predicate + ">,?x )";
        Query query = new Query(tq);
        query.setSource(Gmodel);
        QueryExecution qe = new QueryEngine(query);

        QueryResults results = qe.exec();
        for (; results.hasNext(); ) {
            ResultBinding res = (ResultBinding) results.next();
            Object x = res.get("x");
            if (x == null) break;
            w = new AWidget();

            name = getValue(x, "hasName");
            if (name != null) {
                w.setName(name);
            } else {
                System.out.println("COOL: Name is not defined for the widget " + x.toString());
                return null;
            }

            description = getValue(x, "hasDescription");
            if (description != null) {
                w.setDescription(description);
            }

            title = getValue(x, "hasTitle");
            if (title != null) {
                w.setTitle(title);
            }

            tmps = getValue(x, "hasNumber");
            if (tmps != null) {
                try {
                    w.setNumber(Integer.valueOf(tmps));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            type = getValue(x, "hasType");
            if (type != null) {
                w.setType(type);
            }

            subType = getValue(x, "hasSubType");
            if (subType != null) {
                w.setSubType(subType);
            }

            editable = getValue(x, "isEditable");
            if (editable != null) {
                w.setEditable(editable);
            }

            icon = getValue(x, "hasIcon");
            if (icon != null) {
                w.setIcon(icon);
            }

            actionStates = parseStates(x, "moveToState");
            if (actionStates != null && !actionStates.isEmpty()) {
                w.setActionStates(actionStates);
            }

            actionProcesses = parseProcess(x, "startsProcess");
            if (actionProcesses != null && !actionProcesses.isEmpty()) {
                w.setActionProcesses(actionProcesses);
            }

            actionServices = parseServices(x, "startsService");
            if (actionServices != null && !actionServices.isEmpty()) {
                w.setActionServices(actionServices);
            }

            al.add(w);
        }
        results.close();
        return al;
    }


}