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

import org.jlab.coda.cMsg.cMsgNetworkConstants;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * Constants used in the Afecs
 * </p>
 *
 * @author gurjyan
 *         Date: 11/12/13 Time: 1:51 PM
 * @version 4.x
 */

public class AConstants {

    public static AtomicBoolean debug = new AtomicBoolean();
    public static final String GUI = "GUI";

    // array size for calculating partial averages for evt and data rates.
    public static final int AVERAGING_SIZE = 20;

    public static final int OK = 0;
    public static final int RESET = 1;

    // max component priority
    public static final int maxPriority = 1411;

    public static int defaultTcpPort = cMsgNetworkConstants.nameServerTcpPort;
    public static int defaultUdpPort = cMsgNetworkConstants.nameServerUdpPort;

    // coda states/actions
    public static final String unattached = "unattached";
    public static final String checking = "checking";
    public static final String removed = "removed";
    public static final String busy = "busy";
    public static final String disabled = "disabled";
    public static final String disconnected = "disconnected";
    public static final String connected = "connected";
    public static final String booted = "booted";
    public static final String configured = "configured";
    public static final String downloaded = "downloaded";
    public static final String prestarted = "prestarted";
    public static final String active = "active";
    public static final String paused = "paused";
    public static final String active_paused = "active_paused";

    // supervisor specific states/actions
    public static final String ended = "ended";
    public static final String reseted = "reseted";
    public static final String emergencyreset = "emergencyreset";
    public static final String aborted = "aborted";

    // generic
    public static final String udf = "undefined";
    public static final String seton = "true";
    public static final String setoff = "false";
    public static final String yes = "yes";
    public static final String no = "no";
    public static final String INFO = "INFO";
    public static final String WARN = "WARN";
    public static final String ERROR = "ERROR";
    public static final String SERROR = "SERROR";
    public static final String failed = "failed";
    public static final String FAILED = "FAILED";
    public static final String error = "error";


    // COOL language definition (taxonomy) cool.rdfs file url
    public static final String COOL_HTTP_BASE = "http://COOLHOME/";
    public static final String AFECSDB_HTTP_BASE = "http://AFECSHOMEDB/";
    public static final String COOL_CORE = AFECSDB_HTTP_BASE + "schema/cool#";


    // payload type enumerations
    public final static int BYTE = 201;
    public final static int SHORT = 202;
    public final static int INT = 203;
    public final static int FLOAT = 204;
    public final static int DOUBLE = 205;
    public final static int STRING = 206;
    public final static int BYTE_ARRAY = 207;
    public final static int SHORT_ARRAY = 208;
    public final static int INT_ARRAY = 209;
    public final static int FLOAT_ARRAY = 210;
    public final static int DOUBLE_ARRAY = 211;
    public final static int STRING_ARRAY = 212;

    // name of the controlDesigner normative agent of the platform
    public static final String CONTROLDESIGNER = "ControlDesigner";
    public static final String ORPHANAGENTMONITOR = "OrphanAgentMonitor";

    // name of the dalogMsg archive normative agent of the platform
    public static final String PLATFORMDALOGARCHIVER = "PlDalogMsgArchiver";
    public static final String SERVERMONITOR = "ServerMonitor";

    // static fields for payload item names
    public static final String INTERVAL = "interval";
    public static final String CODANAME = "codaName";
    public static final String CODACLASS = "codaClass";
    public static final String STATE = "state";
    public static final String EVENTNUMBER = "eventCount";
    public static final String EVENTNUMBER64 = "eventCount64";
    public static final String EVENTRATE = "eventRate";
    public static final String DATARATE = "dataRate";
    public static final String NUMBEROFLONGS = "dataCount";
    public static final String LIVETIME = "liveTime";
    public static final String FILENAME = "fileName";
    public static final String DESTINATIONNAMES = "destinationNames";
    public static final String FILEDATE = "fileDate";
    public static final String FILECONTENT = "fileContent";
    public static final String ISCHANGED = "fileChanged";
    public static final String USRFILENAME = "userFileName";
    public static final String USRFILECONTENT = "userFileContent";
    public static final String USRFILEISCHANGED = "userFileChanged";
    public static final String EMUROCCONFIG = "emuRocConfig";
    public static final String CONFSTRING = "configString";
    public static final String DOWNLOADSTRING = "downloadString";
    public static final String PRESTARTSTRING = "prestartString";
    public static final String GOSTRING = "goString";
    public static final String ENDSTRING = "endString";
    public static final String OBJECTTYPE = "objectType";
    public static final String RUNSTARTTIME = "runStartTime";
    public static final String RUNENDTIME = "runEndTime";
    public static final String AUTOSTART = "autoStart";
    public static final String EVENTLIMIT = "eventLimit";
    public static final String DATALIMIT = "dataLimit";
    public static final String TIMELIMIT = "timeLimit";
    public static final String SCHEDULEDRUNS = "scheduledRuns";

    public static final String DESCRIPTION = "description";
    public static final String TYPE = "type";
    public static final String ROLE = "role";
    public static final String SUPERVISOR = "supervisor";
    public static final String HOST = "host";
    public static final String CLIENTHOST = "hostName";
    public static final String CLIENTPORT = "clientPort";
    public static final String SESSION = "session";
    public static final String RUNTYPE = "config";
    public static final String CONFIGID = "configId";
    public static final String CODAID = "codaId";
    public static final String EXPID = "EXPID";
    public static final String STARTTIME = "myStartTime";
    public static final String REPORTINGINTERVAL = "reportingInterval";
    public static final String EVENTRATEAVERAGE = "eventRateAverage";
    public static final String DATARATEAVERAGE = "dataRateAverage";
    public static final String PRIORITY = "priority";
    public static final String RUNNUMBER = "runNumber";
    public static final String STATES = "states";
    public static final String PROCESSES = "processes";
    public static final String DALOGSEVERITY = "severity";
    public static final String DALOGSEVERITYID = "severityId";
    public static final String DALOGTEXT = "dalogText";
    public static final String TIMESTAMP = "timeStamp";
    public static final String AGENT = "agent";
    public static final String CONTAINER = "container";
    public static final String CODA2 = setoff;
    public static final String USERCONFIG = "userConfig";
    public static final String STREAMCOUNT = "nStreams";
    public static final String STREAMID = "streamID";
    public static final String INPUTLINKS = "inputLinks";
    public static final String BROADCASTADDRESSLIST = "baList";
    public static final String IPADDRESSLIST = "ipList";
    public static final String DEFAULTOPTIONDIRS = "defaultOptionsDir";
    public static final String PLATFORMHOST = "platformHost";
    public static final String PLATFORMPORT = "platformPort";
    public static final String CP_PREVIOUS_LOG = "cp_previous";
    public static final String FILEWRITING = "fileWriting";
    public static final String MASTERSHIP = "mastership";

    // user account specific
    public static final String NODE = "node";
    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";
    public static final int TIMEOUT = 30000;


    /***********************************************************************
     *
     *                            Afecs Platform
     *
     * *********************************************************************/
    public static final String DalogMsgType = "rc/report/dalog";

    public static final String PlatformJoinRequest = "platform/join/Request";


    public static final String PlatformControl = "platform/control/*";
    public static final String PlatformControlGetAgentsDir = "platform/control/getAgentsDir";
    public static final String PlatformControlGetClientsDir = "platform/control/getClientsDir";
    public static final String PlatformControlRegisterClient = "platform/control/registerClient";
    public static final String PlatformControlArchiverStart = "platform/control/archiverStart";
    public static final String PlatformControlArchiverStop = "platform/control/archiverStop";
    public static final String PlatformControlArchiverState = "platform/control/archiverState";
    public static final String PlatformControlRegisterAccount = "platform/control/registerAccount";
    public static final String PlatformControlGetSecretKey = "platform/control/getSecretKey";
    public static final String PlatformControlGetPassword = "platform/control/getPassword";
    public static final String PlatformControlUpdateOptions = "platform/control/updateOptions";
    public static final String PlatformControlRunLogBegin = "platform/control/runLogBegin";
    public static final String PlatformControlRunLogEnd = "platform/control/runLogEnd";

    public static final String PlatformInfoResponseName = "platform/info/response/name";
    public static final String PlatformInfoResponseHost = "platform/info/response/host";
    public static final String PlatformInfoResponsePort = "platform/info/response/port";
    public static final String PlatformInfoResponseStartTime = "platform/info/response/myStartTime";
    public static final String PlatformInfoResponseAgentsDir = "platform/info/response/agentsDir";
    public static final String PlatformInfoResponseClientsDir = "platform/info/response/clientsDir";
    public static final String PlatformInfoResponseContainers = "platform/info/response/containers";
    public static final String PlatformInfoResponseAgent = "platform/info/response/agent";

    public static final String PlatformInfoRequest = "platform/info/request/*";
    public static final String PlatformInfoRequestActiveRunType = "platform/info/request/activeRunType";
    public static final String PlatformInfoRequestActiveRunState = "platform/info/request/activeRunState";
    public static final String PlatformInfoRequestActiveRunTypes = "platform/info/request/activeRunTypes";
    public static final String PlatformInfoRequestAgents = "platform/info/request/agents";
    public static final String PlatformInfoRequestAgents_p = "platform/info/request/agents/pl";
    public static final String PlatformInfoRequestContainers = "platform/info/request/containers";
    public static final String PlatformInfoRequestAgent = "platform/info/request/agent";
    public static final String PlatformInfoRequestisRegistered = "platform/info/request/isregistered";
    public static final String PlatformInfoRequestName = "platform/info/request/name";
    public static final String PlatformInfoRequestHost = "platform/info/request/host";
    public static final String PlatformInfoRequestPort = "platform/info/request/port";
    public static final String PlatformInfoRequestStartTime = "platform/info/request/myStartTime";
    public static final String PlatformInfoRequestSessions = "platform/info/request/sessions";
    public static final String PlatformInfoRequestSessions_p = "platform/info/request/sessions/pl";
    public static final String PlatformInfoRequestSessionNames = "platform/info/request/sessionNames";
    public static final String PlatformInfoRequestDocs = "platform/info/request/docs";
    public static final String PlatformInfoRequestDocs_p = "platform/info/request/docs/pl";
    public static final String PlatformInfoRequestDocument = "platform/info/request/document";
    public static final String PlatformInfoRequestAddNewSession = "platform/info/request/addNewSession";
    public static final String PlatformInfoRequestIsArchiving = "platform/info/request/isArchiving";
    public static final String PlatformInfoRequestRunNumbers = "platform/info/request/runNumbers";
    public static final String PlatformInfoRequestReadConfgiFile = "platform/info/request/readConfigFile";
    public static final String PlatformInfoRequestGetRocDataLink = "platform/info/request/getRocDataLink";

    public static final String InfluxDBInjectRequest = "influx/inject/request/*";
    public static final String InfluxDBInjectRequestUserData = "influx/inject/request/user";
    public static final String InfluxDBInjectRequestPlatformData = "influx/inject/request/user/platform";


    /***********************************************************************
     *
     *                            Afecs Platform Control Designer
     *
     * *********************************************************************/

    public static final String DesignerControlRequest = "designer/control/request/*";
    public static final String DesignerControlRequestConfigureControl = "designer/control/request/configure/control";
    public static final String DesignerControlRequestConfigureControl_RCAPI = "designer/control/request/configure/control_rcapi";
    public static final String DesignerControlRequestConfigureAgent = "designer/control/request/configure/agent";

    public static final String DesignerInfoRequest = "designer/info/request/*";
    public static final String DesignerInfoRequestGetConfigs = "designer/info/request/getConfigFileNames";
    public static final String DesignerInfoRequestGetDefinedRTVs = "designer/info/request/getDefinedRTVs";
    public static final String DesignerInfoRequestControlAgents = "designer/info/request/agents";


    /***********************************************************************
     *
     *                            Afecs Agent
     *
     * *********************************************************************/

    public static final String AgentControlRequest = "agent/control/request/*";
    public static final String AgentControlRequestSetRunNumber = "agent/control/request/setRunNumber";
    public static final String AgentControlRequestStartReporting = "agent/control/request/startReporting/status";
    public static final String AgentControlRequestStopReporting = "agent/control/request/stopReporting/status";
    public static final String AgentControlRequestMoveToState = "agent/control/request/moveToState";
    public static final String AgentControlRequestExecuteProcess = "agent/control/request/executeProcess";
    public static final String AgentControlRequestReset = "agent/control/request/reset";
    public static final String AgentControlRequestSetup = "agent/control/request/setup";
    public static final String AgentControlRequestClientReconnect = "agent/control/request/clientreconnect";
    public static final String AgentControlRequestClientState = "agent/control/request/clientstate";
    public static final String AgentControlRequestPlatformDisconnect = "agent/control/request/platformdisconnect";
    public static final String AgentControlRequestReleaseAgent = "agent/control/request/release";
    public static final String AgentControlRequestSetFileWriting = "agent/control/request/setFileWriting";
    public static final String SupervisorControlRequestSetup = "agent/control/request/supervisor/setup";
    public static final String SupervisorControlRequestReleaseAgents = "agent/control/request/supervisor/releaseAgents";
    public static final String AgentControlRequestNetworkDetails = "agent/control/request/linkedNetworkDetails";

    public static final String AgentInfoRequest = "agent/info/request/*";
    public static final String AgentInfoRequestStatus = "agent/info/request/status";
    public static final String AgentInfoRequestState = "agent/info/request/state";
    public static final String AgentInfoRequestStartTime = "agent/info/request/startTime";
    public static final String AgentInfoRequestHost = "agent/info/request/host";
    public static final String AgentInfoRequestDescription = "agent/info/request/description";
    public static final String AgentInfoRequestType = "agent/info/request/type";
    public static final String AgentInfoRequestSession = "agent/info/request/session";
    public static final String AgentInfoRequestRuntype = "agent/info/request/runtype";

    public static final String AgentInfoResponseStatus = "agent/info/response/status";
    public static final String AgentInfoResponseState = "agent/info/response/state";
    public static final String AgentInfoResponseStartTime = "agent/info/response/startTime";
    public static final String AgentInfoResponseHost = "agent/info/response/host";
    public static final String AgentInfoResponseDescription = "agent/info/response/description";
    public static final String AgentInfoResponseType = "agent/info/response/type";
    public static final String AgentInfoResponseSession = "agent/info/response/session";
    public static final String AgentInfoResponseRuntype = "agent/info/response/runtype";

    public static final String AgentReportAlarm = "agent/report/alarm";
    public static final String AgentReportPopUpAlarm = "agent/report/popupalarm";


    /***********************************************************************
     *
     *                            Afecs CRCAgent Agent
     *
     * *********************************************************************/
    // agent subscribes to messages from client
    public static final String RcReportStatus = "rc/report/status";
    public static final String RcReportDalog = "rc/report/dalog";

    // message types coming from the client at the rc/response callback
    public static final String RcResponse = "rc/response/*";
    public static final String RcResponseGetState = "rc/response/getState";
    public static final String RcResponseGetCodaClass = "rc/response/getCodaClass";
    public static final String RcResponseGetSession = "rc/response/getSession";
    public static final String RcResponseGetRunNumber = "rc/response/getRunNumber";
    public static final String RcResponseGetRunType = "rc/response/getRunType";
    public static final String RcResponseGetConfigId = "rc/response/getConfigId";

    public static final String RunControlSetRocBufferLevel = "run/control/setRocBufferLevel";
    public static final String RunControlGetRocBufferLevel = "run/control/getRocBufferLevel";
    public static final String RunControlGetRunNumber = "run/control/getRunNumber";
    public static final String RunControlGetRunType = "run/control/getRunType";

    public static final String CodaInfoGetObjectType = "coda/info/getObjectType";
    public static final String CodaInfoGetState = "coda/info/getState";
    public static final String CodaInfoGetStatus = "coda/info/getStatus";
    public static final String CodaInfoGetCodaClass = "coda/info/getCodaClass";

    public static final String SessionControlStartReporting = "session/control/startReporting";
    public static final String SessionControlStopReporting = "session/control/stopReporting";
    public static final String SessionControlSetInterval = "session/control/setInterval";
    public static final String SessionControlExit = "session/control/exit";
    public static final String SessionControlSetState = "session/control/setState";
    public static final String SessionControlSetSession = "session/control/setSession";
    public static final String SessionControlGetSession = "session/control/getSession";
    public static final String SessionControlSetRunNumber = "session/control/setRunNumber";
    public static final String SessionControlSetRunType = "session/control/setRunType";
    public static final String SessionControlPreGo = "session/control/preGo";
    public static final String SessionControlPreEnd = "session/control/preEnd";


    /***********************************************************************
     *
     *                            Afecs Supervisor Agent
     *
     * *********************************************************************/


    public static final String SupervisorControlRequest = "supervisor/control/request/*";
    public static final String SupervisorControlRequestStartService = "supervisor/control/request/startService";
    public static final String SupervisorControlRequestSopService = "supervisor/control/request/stopService";
    public static final String SupervisorControlRequestReleaseAgent = "supervisor/control/request/releaseAgent";
    public static final String SupervisorControlRequestDisableAgent = "supervisor/control/request/disableAgent";
    public static final String SupervisorControlRequestDisableAllDisconnects = "supervisor/control/request/disableAllDisconnects";
    public static final String SupervisorControlRequestSetRunNumber = "supervisor/control/request/setRunNumber";
    public static final String SupervisorControlRequestReleaseRunType = "supervisor/control/request/releaseRunType";

    public static final String SupervisorControlRequestReportAgents = "supervisor/control/request/reportAgents";
    public static final String SupervisorControlRequestFailTransition = "supervisor/control/request/failTransition";

    public static final String SupervisorControlRequestSetEventLimit = "supervisor/control/request/setEventLimit";
    public static final String SupervisorControlRequestResetEventLimit = "supervisor/control/request/resetEventLimit";
    public static final String SupervisorControlRequestSetDataLimit = "supervisor/control/request/setDataLimit";
    public static final String SupervisorControlRequestResetDataLimit = "supervisor/control/request/resetDataLimit";
    public static final String SupervisorControlRequestSetTimeLimit = "supervisor/control/request/setTimeLimit";
    public static final String SupervisorControlRequestResetTimeLimit = "supervisor/control/request/resetTimeLimit";
    public static final String SupervisorControlRequestEnableAutoMode = "supervisor/control/request/enableAutoMode";
    public static final String SupervisorControlRequestDisableAutoMode = "supervisor/control/request/disableAutoMode";
    public static final String SupervisorControlRequestSetNumberOfRuns = "supervisor/control/request/setNumberOfRuns";
    public static final String SupervisorControlRequestResetNumberOfRuns = "supervisor/control/request/resetNumberOfRuns";

    public static final String SupervisorControlRequestPause = "supervisor/control/request/pause";
    public static final String SupervisorControlRequestResume = "supervisor/control/request/resume";
    public static final String SupervisorControlRequestNoFileOutput = "supervisor/control/request/noFileOutput";
    public static final String SupervisorControlRequestResumeFileOutput = "supervisor/control/request/resumeFileOutput";

    public static final String SupervisorControlRequestInformDeadAgent = "supervisor/control/request/inform/deadAgent";
    public static final String SupervisorControlRequestReportReady = "supervisor/control/request/report/ready";

    public static final String SupervisorUserRequest = "supervisor/user/request/*";
    public static final String SupervisorReportRunNumber = "supervisor/user/request/runNumber";
    public static final String SupervisorReportRunNumber_p = "supervisor/user/request/runNumber/pl";
    public static final String SupervisorReportRunState = "supervisor/user/request/runState";
    public static final String SupervisorReportRunState_p = "supervisor/user/request/runState/pl";
    public static final String SupervisorReportSession = "supervisor/user/request/runSession";
    public static final String SupervisorReportSession_p = "supervisor/user/request/runSession/pl";
    public static final String SupervisorReportRunType = "supervisor/user/request/runType";
    public static final String SupervisorReportSchedulerStatus = "supervisor/user/request/schedulerStatus";
    public static final String SupervisorReportSchedulerStatus_p = "supervisor/user/request/schedulerStatus/pl";
    public static final String SupervisorReportComponentStates = "supervisor/user/request/componentStates";
    public static final String SupervisorReportComponentStates_p = "supervisor/user/request/componentStates/pl";
    public static final String SupervisorReportComponentEventNumber = "supervisor/user/request/componentEventNumber";
    public static final String SupervisorReportComponentEventNumber_p = "supervisor/user/request/componentEventNumber/pl";
    public static final String SupervisorReportComponentOutputFile = "supervisor/user/request/componentOutputFile";
    public static final String SupervisorReportComponentOutputFile_p = "supervisor/user/request/componentOutputFile/pl";
    public static final String SupervisorReportComponentEventRate = "supervisor/user/request/componentEventRate";
    public static final String SupervisorReportComponentEventRate_p = "supervisor/user/request/componentEventRate/pl";
    public static final String SupervisorReportComponentDataRate = "supervisor/user/request/componentDataRate";
    public static final String SupervisorReportComponentDataRate_p = "supervisor/user/request/componentDataRate/pl";
    public static final String SupervisorReportComponentLiveTime_p = "supervisor/user/request/componentLiveTime/pl";
    public static final String SupervisorReportComponentState = "supervisor/user/request/componentState";
    public static final String SupervisorReportComponentState_p = "supervisor/user/request/componentState/pl";
    public static final String SupervisorReportRunStartTime = "supervisor/user/request/runStartTime";
    public static final String SupervisorReportRunStartTime_p = "supervisor/user/request/runStartTime/pl";
    public static final String SupervisorReportRunEndTime = "supervisor/user/request/runEndTime";
    public static final String SupervisorReportRunEndTime_p = "supervisor/user/request/runEndTime/pl";
    public static final String SupervisorReportRtvs_p = "supervisor/user/request/rtvs/pl";
    public static final String SupervisorReportPersistencyComponent = "supervisor/user/request/persistency/component";
    public static final String SupervisorReportTriggerSourceComponent = "supervisor/user/request/trigger/component";


    /***********************************************************************
     *
     *                            Afecs User Interface
     *
     * *********************************************************************/


    public static final String UIControlRequest = "ui/control/request/*";
    public static final String UIControlRequestReset = "ui/control/request/reset";
    public static final String UIControlRequestSetRunNumber = "ui/control/request/setRunNumber";
    public static final String UIControlPopupInfo = "ui/control/request/popupInfo";
    public static final String UIControlContainerDead = "ui/control/request/containerDead";
    public static final String UIControlRequestDisableDisconnected = "ui/control/request/disabledisconnecteds";

}
