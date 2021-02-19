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

import org.jlab.coda.afecs.client.AClientInfo;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgPayloadItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AComponent extends AOntologyConcept implements Serializable, Comparable<AComponent> {
    // rdf/cool defined fields
    private String name = AConstants.udf;
    private String classPath = AConstants.udf;
    private String className = AConstants.udf;
    private int id;
    private String description = AConstants.udf;
    private String type = AConstants.udf;
    private String role = AConstants.udf;
    private int priority = 0;
    private String session = AConstants.udf;
    private String runType = AConstants.udf;
    private AOption option;
    private String coda2Component = AConstants.udf;
    private String codaComponent = AConstants.seton;
    private ArrayList<ALink> link = new ArrayList<>();
    private int reportingInterval = 1000;
    private ArrayList<AState> states = new ArrayList<>();
    private ArrayList<AProcess> processes = new ArrayList<>();
    private ArrayList<AService> services = new ArrayList<>();
    private APlugin plugin;
    private String userConfig = AConstants.udf;
    private boolean isMaster = false;

    private String[] destinationNames;
    // runtime field parameters
    private String expid = AConstants.udf;
    private String host = AConstants.udf;
    private String startTime = AConstants.udf;

    private String state = AConstants.checking;
    private String fileName = AConstants.udf;
    private String objectType = AConstants.udf;
    private float liveTime;
    private float eventRate;
    private float eventRateAverage;
    private double dataRate;
    private double dataRateAverage;
    private long numberOfLongs;
    private int maxEventSize;
    private int minEventSize;
    private int avgEventSize;
    private int minTimeToBuild;
    private int maxTimeToBuild;
    private int meanTimeToBuild;
    private int chunkXEtBuffer;
    private int fileWriting = 1;

    private String runStartTime = "0";
    private String runEndTime = "0";
    private boolean autoStart = false;
    private int eventLimit = 0;
    private long dataLimit = 0;
    private long timeLimit = 0;
    private long eventNumber;
    private int runNumber = -1371;
    private AClientInfo client;
    private int nScheduledRuns = 0;
    private String supervisor = AConstants.udf;

    private Map<String, String[]> linkedIp = new HashMap<>();
    private Map<String, String[]> linkedBa = new HashMap<>();

    // hashMap containing names and cMsgPayloadItem objects for all described monitored, periodic data
    private ConcurrentHashMap<String, cMsgPayloadItem> MonitoredData = new ConcurrentHashMap<>();

    // arrayList of default option file directory locations set by include concept of the cool
    private ArrayList<String> dod = new ArrayList<>();

    private int configID;

    private ArrayList<String> linkedComponentNames = new ArrayList<>();
    private ArrayList<String> linkedComponentTypes = new ArrayList<>();

    // input and output buffers
    private Map<String, Integer> inBuffers = new ConcurrentHashMap<>();
    private Map<String, Integer> outBuffers = new ConcurrentHashMap<>();

    private int streamCount;
    private int streamId;


    public AComponent() {
        setOntology("afecs");
        setConceptName("Component");
        addPrimitiveSlot("hasName", 1, false, "String");
        addPrimitiveSlot("hasClassPath", 1, false, "String");
        addPrimitiveSlot("hasClassName", 1, false, "String");
        addConceptSlot("hasID", 1, false, "Integer");
        addPrimitiveSlot("hasDescription", 1, true, "String");
        addPrimitiveSlot("hasType", 1, true, "String");
        addPrimitiveSlot("hasCode", 1, true, "String");
        addPrimitiveSlot("hasPriority", 1, true, "Integer");
        addPrimitiveSlot("hasSession", 1, true, "String");
        addPrimitiveSlot("hasRunType", 1, true, "String");
        addConceptSlot("hasOption", 1, true, "Option");
        addPrimitiveSlot("representsCoda2Component", 1, true, "String");
        addPrimitiveSlot("isCodaComponent", 1, true, "String");
        addPrimitiveSlot("hasReportingInterval", 1, true, "Integer");
        addConceptSlot("hasState", 2, true, "AState");
        addConceptSlot("hasProcess", 2, true, "AProcess");
        addConceptSlot("providesService", 2, true, "AService");
        addConceptSlot("hasHmi", 1, true, "HMI");
        addConceptSlot("hasCommunicationPlugin", 1, true, "APlugin");
        addConceptSlot("XCo", 1, true, "Double");
        addConceptSlot("YCo", 1, true, "Double");
        addConceptSlot("WCo", 1, true, "Double");
        addConceptSlot("HCo", 1, true, "Double");
        addConceptSlot("linkedTo", 2, true, "String");
        addConceptSlot("linkedToType", 2, true, "String");
        addConceptSlot("hasUserConfig", 2, true, "String");
        addConceptSlot("usesLink", 2, true, "ALink");
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public AClientInfo getClient() {
        return client;
    }

    public void setClient(AClientInfo client) {
        this.client = client;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
    }

    public String getExpid() {
        return expid;
    }

    public void setExpid(String expid) {
        this.expid = expid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public float getEventRate() {
        return eventRate;
    }

    public void setEventRate(float eventRate) {
        this.eventRate = eventRate;
    }

    public double getDataRate() {
        return dataRate;
    }

    public void setDataRate(double dataRate) {
        this.dataRate = dataRate;
    }

    public long getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(long eventNumber) {
        this.eventNumber = eventNumber;
    }

    public int getRunNumber() {
        return runNumber;
    }

    public void setRunNumber(int runNumber) {
        this.runNumber = runNumber;
    }

    public void setPrevious_runNumber(int runNumber) {
        int previous_runNumber = runNumber;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getReportingInterval() {
        return reportingInterval;
    }

    public void setReportingInterval(int reportingInterval) {
        this.reportingInterval = reportingInterval;
    }

    public float getEventRateAverage() {
        return eventRateAverage;
    }

    public void setEventRateAverage(float eventRateAverage) {
        this.eventRateAverage = eventRateAverage;
    }

    public double getDataRateAverage() {
        return dataRateAverage;
    }

    public void setDataRateAverage(double dataRateAverage) {
        this.dataRateAverage = dataRateAverage;
    }

    public long getNumberOfLongs() {
        return numberOfLongs;
    }

    public void setNumberOfLongs(long numberOfLongs) {
        this.numberOfLongs = numberOfLongs;
    }

    public int getMaxEventSize() {
        return maxEventSize;
    }

    public void setMaxEventSize(int maxEventSize) {
        this.maxEventSize = maxEventSize;
    }

    public int getMinEventSize() {
        return minEventSize;
    }

    public void setMinEventSize(int minEventSize) {
        this.minEventSize = minEventSize;
    }

    public int getAvgEventSize() {
        return avgEventSize;
    }

    public void setAvgEventSize(int avgEventSize) {
        this.avgEventSize = avgEventSize;
    }

    public int getMeanTimeToBuild() {
        return meanTimeToBuild;
    }

    public void setMeanTimeToBuild(int meanTimeToBuild) {
        this.meanTimeToBuild = meanTimeToBuild;
    }

    public int getMinTimeToBuild() {
        return minTimeToBuild;
    }

    public void setMinTimeToBuild(int minTimeToBuild) {
        this.minTimeToBuild = minTimeToBuild;
    }

    public int getMaxTimeToBuild() {
        return maxTimeToBuild;
    }

    public void setMaxTimeToBuild(int maxTimeToBuild) {
        this.maxTimeToBuild = maxTimeToBuild;
    }

    public int getChunkXEtBuffer() {
        return chunkXEtBuffer;
    }

    public void setChunkXEtBuffer(int chunkXEtBuffer) {
        this.chunkXEtBuffer = chunkXEtBuffer;
    }

    public String getRunStartTime() {
        return runStartTime;
    }

    public void setRunStartTime(String runStartTime) {
        this.runStartTime = runStartTime;
    }

    public String getRunEndTime() {
        return runEndTime;
    }

    public void setRunEndTime(String runEndTime) {
        this.runEndTime = runEndTime;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public int getEventLimit() {
        return eventLimit;
    }

    public void setEventLimit(int eventLimit) {
        this.eventLimit = eventLimit;
    }

    public long getDataLimit() {
        return dataLimit;
    }

    public void setDataLimit(long dataLimit) {
        this.dataLimit = dataLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public ArrayList<AState> getStates() {
        return states;
    }

    public void setStates(ArrayList<AState> states) {
        this.states = states;
    }

    public void addState(AState s) {
        this.states.add(s);
    }

    public String[] getStateNames() {
        String st[] = new String[states.size()];
        int i = 0;
        for (AState s : states) {
            st[i] = s.getName();
            i++;
        }
        return st;
    }

    public String[] getLinkNames() {
        String st[] = new String[linkedComponentNames.size()];
        int i = 0;
        for (String s : linkedComponentNames) {
            st[i] = s;
            i++;
        }
        return st;
    }

    public ArrayList<String> getLinkedComponentNames() {
        return linkedComponentNames;
    }

    public ArrayList<AProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(ArrayList<AProcess> processes) {
        this.processes = processes;
    }

    public String[] getProcessNames() {
        String st[] = new String[processes.size()];
        int i = 0;
        for (AProcess p : processes) {
            st[i] = p.getName();
            i++;
        }
        return st;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AOption getOption() {
        return option;
    }

    public void setOption(AOption option) {
        this.option = option;
    }

    public String getCoda2Component() {
        return coda2Component;
    }

    public void setCoda2Component(String coda2Component) {
        this.coda2Component = coda2Component;
    }

    public String getCodaComponent() {
        return codaComponent;
    }

    public void setCodaComponent(String codaComponent) {
        this.codaComponent = codaComponent;
    }

    public void setGui(HMI gui) {
        HMI gui1 = gui;
    }


    public APlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(APlugin plugin) {
        this.plugin = plugin;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        if (destinationNames == null) {
            destinationNames = new String[1];
        }
        this.destinationNames[0] = fileName;
        this.fileName = fileName;
    }

    public String[] getDestinationNames() {
        return destinationNames;
    }

    public void setDestinationNames(String[] destinationNames) {
        this.destinationNames = destinationNames;
    }

    public ArrayList<AService> getServices() {
        return services;
    }

    public void setServices(ArrayList<AService> services) {
        this.services = services;
    }

    public void addService(AService service) {
        this.services.add(service);
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public float getLiveTime() {
        return liveTime;
    }

    public void setLiveTime(float liveTime) {
        this.liveTime = liveTime;
    }

    public int getConfigID() {
        return configID;
    }

    public void setConfigID(int configID) {
        this.configID = configID;
    }

    public ArrayList<String> getDod() {
        return dod;
    }

    public void addDod(String d) {
        dod.add(d);
    }

    public void setW(double w) {
        double w1 = w;
    }

    public void setX(double x) {
        double x1 = x;
    }

    public void setY(double y) {
        double y1 = y;
    }

    public void setH(double h) {
        double h1 = h;
    }

    public void setLinkedComponentNames(ArrayList<String> linkedComponentNames) {
        this.linkedComponentNames = linkedComponentNames;
    }

    public void setLinkedComponentTypes(ArrayList<String> linkedComponentTypes) {
        this.linkedComponentTypes = linkedComponentTypes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLinks(ArrayList<ALink> link) {
        this.link = link;
    }

    public void setnScheduledRuns(int nScheduledRuns) {
        this.nScheduledRuns = nScheduledRuns;
    }

    public String getUserConfig() {
        return userConfig;
    }

    public void setUserConfig(String userConfig) {
        this.userConfig = userConfig;
    }

    public int getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(int streamCount) {
        this.streamCount = streamCount;
    }

    public int getStreamId() {
        return streamId;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    public int getFileWriting() {
        return fileWriting;
    }

    public void setFileWriting(int fileWriting) {
        this.fileWriting = fileWriting;
    }

    /**
     * Returns the configuration data of this class as an ArrayList of payloadItems
     *
     * @return array list of payload items.
     */
    public ArrayList<cMsgPayloadItem> getConfigurationDataAsPayload() {
        defineRole(linkedComponentTypes);
        ArrayList<cMsgPayloadItem> al = new ArrayList<>();
        try {
            al.add(new cMsgPayloadItem(AConstants.CODANAME, name));
            al.add(new cMsgPayloadItem(AConstants.TIMESTAMP, AfecsTool.getCurrentTimeInH()));
            al.add(new cMsgPayloadItem(AConstants.DESCRIPTION, description));
            al.add(new cMsgPayloadItem(AConstants.TYPE, type));
            al.add(new cMsgPayloadItem(AConstants.ROLE, role));
            al.add(new cMsgPayloadItem(AConstants.HOST, host));
            al.add(new cMsgPayloadItem(AConstants.EXPID, expid));
            al.add(new cMsgPayloadItem(AConstants.SESSION, session));
            al.add(new cMsgPayloadItem(AConstants.RUNTYPE, runType));
            al.add(new cMsgPayloadItem(AConstants.SUPERVISOR, supervisor));
            al.add(new cMsgPayloadItem(AConstants.STARTTIME, startTime));
            al.add(new cMsgPayloadItem(AConstants.REPORTINGINTERVAL, reportingInterval));
            al.add(new cMsgPayloadItem(AConstants.STATE, state));
            al.add(new cMsgPayloadItem(AConstants.PRIORITY, priority));
            al.add(new cMsgPayloadItem(AConstants.USERCONFIG, userConfig));
            al.add(new cMsgPayloadItem(AConstants.STREAMCOUNT, streamCount));
            al.add(new cMsgPayloadItem(AConstants.STREAMID, streamId));
            al.add(new cMsgPayloadItem(AConstants.MASTERSHIP, String.valueOf(isMaster)));
            if (linkedComponentNames != null && !linkedComponentNames.isEmpty())
                al.add(new cMsgPayloadItem(AConstants.INPUTLINKS, getLinkNames()));

//            al.add(new cMsgPayloadItem(AConstants.CLIENTIPLIST,getClient().getHostIps()));
//            al.add(new cMsgPayloadItem(AConstants.CLIENTBALIST,getClient().getHostBroadcastAddresses()));

            if (client != null) al.add(new cMsgPayloadItem(AConstants.CLIENTHOST, client.getHostName()));
            if (client != null) al.add(new cMsgPayloadItem(AConstants.CLIENTPORT, client.getPortNumber()));
            if (getStateNames() != null) al.add(new cMsgPayloadItem(AConstants.STATES, getStateNames()));
            if (getProcessNames() != null) al.add(new cMsgPayloadItem(AConstants.PROCESSES, getProcessNames()));
        } catch (cMsgException e) {
            if (AConstants.debug.get()) e.printStackTrace();
        }
        return al;
    }

    /**
     * Returns the dynamic data of this class as an ArrayList of payloadItems
     *
     * @return array list of payload items.
     */
    public ArrayList<cMsgPayloadItem> getRunTimeDataAsPayload() {
        ArrayList<cMsgPayloadItem> al = new ArrayList<>();
        try {
            al.add(new cMsgPayloadItem(AConstants.CODANAME, name));
            al.add(new cMsgPayloadItem(AConstants.TIMESTAMP, AfecsTool.getCurrentTimeInH()));
            al.add(new cMsgPayloadItem(AConstants.TYPE, type));
            al.add(new cMsgPayloadItem(AConstants.EXPID, expid));
            al.add(new cMsgPayloadItem(AConstants.SESSION, session));
            al.add(new cMsgPayloadItem(AConstants.RUNTYPE, runType));
            al.add(new cMsgPayloadItem(AConstants.STATE, state));
            al.add(new cMsgPayloadItem(AConstants.EVENTRATE, eventRate));
            al.add(new cMsgPayloadItem(AConstants.EVENTRATEAVERAGE, eventRateAverage));
            al.add(new cMsgPayloadItem(AConstants.DATARATE, dataRate));
            al.add(new cMsgPayloadItem(AConstants.DATARATEAVERAGE, dataRateAverage));
            al.add(new cMsgPayloadItem(AConstants.NUMBEROFLONGS, numberOfLongs));
            al.add(new cMsgPayloadItem(AConstants.EVENTNUMBER, eventNumber));
            al.add(new cMsgPayloadItem(AConstants.RUNNUMBER, runNumber));
            al.add(new cMsgPayloadItem(AConstants.FILENAME, fileName));
            al.add(new cMsgPayloadItem(AConstants.OBJECTTYPE, objectType));
            al.add(new cMsgPayloadItem(AConstants.LIVETIME, liveTime));
            al.add(new cMsgPayloadItem(AConstants.RUNSTARTTIME, runStartTime));
            al.add(new cMsgPayloadItem(AConstants.RUNENDTIME, runEndTime));
            al.add(new cMsgPayloadItem(AConstants.MASTERSHIP, String.valueOf(isMaster)));
            if (autoStart) {
                al.add(new cMsgPayloadItem(AConstants.AUTOSTART, AConstants.seton));
            } else {
                al.add(new cMsgPayloadItem(AConstants.AUTOSTART, AConstants.setoff));
            }
            al.add(new cMsgPayloadItem(AConstants.EVENTLIMIT, eventLimit));
            al.add(new cMsgPayloadItem(AConstants.DATALIMIT, dataLimit));
            al.add(new cMsgPayloadItem(AConstants.TIMELIMIT, timeLimit));
            al.add(new cMsgPayloadItem(AConstants.SCHEDULEDRUNS, nScheduledRuns));

            al.add(new cMsgPayloadItem(AConstants.SUPERVISOR, supervisor));

            for (cMsgPayloadItem s : MonitoredData.values()) {
                al.add(s);
            }
        } catch (cMsgException e) {
            if (AConstants.debug.get()) e.printStackTrace();
        }
        return al;
    }

    public Map<String, String[]> getLinkedIp() {
        return linkedIp;
    }

    public void addLinkedIp(String name, String[] linkedIpa) {
        if (linkedIpa.length > 0 && !linkedIpa[0].equals("")) {
            this.linkedIp.put(name, linkedIpa);
        }
    }

    public Map<String, String[]> getLinkedBa() {
        return linkedBa;
    }

    public void addLinkedBa(String name, String[] linkedBaa) {
        this.linkedBa.put(name, linkedBaa);
    }

    @Override
    public String toString() {
        String s = "\n Agent_Data " +
                "  \n-------------" +
                "  \nname              = " + name +
                ", \ntype              = " + type +
                ", \nsession           = " + session +
                ", \nrunType           = " + runType +
                ", \nexpid             = " + expid +
                ", \nstate             = " + state +
                ", \neventRate         = " + eventRate +
                ", \neventRateAverage  = " + eventRateAverage +
                ", \ndataRate          = " + dataRate +
                ", \ndataRateAverage   = " + dataRateAverage +
                ", \nnumberOfLongs     = " + numberOfLongs +
                ", \neventNumber       = " + eventNumber +
                ", \nrunNumber         = " + runNumber +
                ", \nreportingInterval = " + reportingInterval +
                ", \npriority          = " + priority +
                ", \nuserConfig        = " + userConfig +
                "\n";
        StringBuilder sb = new StringBuilder(s);
        for (String ip : client.getHostIps()) {
            sb.append("IP = " + ip).append("\n");
        }
        for (String ba : client.getHostBroadcastAddresses()) {
            sb.append("BA = " + ba).append("\n");
        }
        for (ALink link : link) {
            sb.append("source comp = " + link.getSourceComponentName()).
                    append(" destination comp = " + link.getDestinationComponentName()).
                    append("\n");
        }
        for (String l : linkedComponentNames) {
            sb.append("linked comp = " + l).append("\n");
        }
        if (!linkedIp.isEmpty()) {
            for (String c : linkedIp.keySet()) {
                sb.append("LinkedComponent IP from comp = " + c).append("\n");

                String[] ips = linkedIp.get(c);
                if (ips != null) {
                    for (String ip : ips) {
                        sb.append("LinkedComponent IP = " + ip).append("\n");
                    }
                }
            }
        }
        if (!linkedBa.isEmpty()) {
            for (String c : linkedBa.keySet()) {
                sb.append("LinkedComponent BA from comp = " + c).append("\n");

                String[] bas = linkedBa.get(c);
                if (bas != null) {
                    for (String ba : bas) {
                        sb.append("LinkedComponent BA = " + ba).append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    @Override
    /**
     * <p>
     *     Ascending ordering in case we sort arrays of AComponents
     *     For descending ordering simply do:
     *     comparedPriority-priority
     * </p>
     */
    public int compareTo(AComponent comp) {
        int comparedPriority = comp.getPriority();
        return priority - comparedPriority;
    }

    public Map<String, Integer> getInBuffers() {
        return inBuffers;
    }

    public Map<String, Integer> getOutBuffers() {
        return outBuffers;
    }

    public void setLinkedIp(Map<String, String[]> linkedIp) {
        this.linkedIp = linkedIp;
    }

    public void setLinkedBa(Map<String, String[]> linkedBa) {
        this.linkedBa = linkedBa;
    }

    private void defineRole(ArrayList<String> ar) {
        if (type.equals("EBER")) {
            if (ar.contains("ROC")) {
                role = "PEBER";
            } else {
                role = "SEBER";
            }
        } else {
            role = type;
        }
    }
}
