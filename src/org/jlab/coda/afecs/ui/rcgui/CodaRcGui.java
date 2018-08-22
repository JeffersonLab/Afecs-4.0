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

package org.jlab.coda.afecs.ui.rcgui;

import com.cosylab.gui.components.spikechart.ChartUpdateRequest;
import com.cosylab.gui.components.spikechart.DefaultChartDecorator;
import com.cosylab.gui.components.spikechart.FancyTimeTrendFlavor;
import com.cosylab.gui.components.spikechart.Interval;
import org.jlab.coda.afecs.platform.ASessionInfo;
import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.afecs.ui.pconsole.PConsole;
import org.jlab.coda.afecs.ui.rcgui.factory.ATableFactory;
import org.jlab.coda.afecs.ui.rcgui.util.*;
import org.jlab.coda.afecs.ui.rcgui.util.chart.ACosiTimeChart;
import org.jlab.coda.afecs.ui.rcgui.util.chart.ATimeChartDataModel;
import org.jlab.coda.afecs.ui.rcgui.util.chart.fx.AFxChart;
import org.jlab.coda.afecs.ui.rcgui.util.chart.fx.FxAnimatedBarChart;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;
import org.jlab.coda.cMsg.cMsgPayloadItem;
import org.jlab.coda.cMsg.cMsgSubscriptionHandle;

import javax.swing.*;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CODA run control graphical interface
 * </p>
 *
 * @author gurjyan
 *         Date: 11/18/14 Time: 2:51 PM
 * @version 4.x
 */
public class CodaRcGui extends JFrame implements ListSelectionListener {

    /********************************************
     * Object references
     */
    // cMsg managing methods, including
    // platform connection object, etc.
    public ABase base;
    private CodaRcGui iam;
    CommandLine cl;
    public GuiDriver gDriver;
    public DbDriver dbDriver;
    RcMsgHeartBeat cMsgConHB;

    // This is a thread that periodically
    // looks graphingComponentList map and
    // updates the ui
    RcGraphUpdate gUpdateThread;
    private UserAppManager userAppManager;

    /********************************************
     * Subscription handlers
     */
    private cMsgSubscriptionHandle uiControl;
    private cMsgSubscriptionHandle daLogAgents;
    private cMsgSubscriptionHandle statusAgents;

    private ATableFactory
            daLogTableFactory = new ATableFactory();
    ATableFactory
            dataTableFactory = new ATableFactory();
    // When selecting a component in the component table
    // info table will be shown in the "client data" tab
    ATableFactory
            individualCompDataTableFactory = new ATableFactory();

    /********************************************
     * Time graph and bar-chart
     */
    // Event rate time graph object
    ACosiTimeChart EvtRateTG;
    // Data rate time graph object
    ACosiTimeChart DataRateTG;
    // Live-time time graph object
    ACosiTimeChart LiveTimeTG;

    // Event rate time graph object
    AFxChart FxEvtRateTG;
    List<String> FxEvtRateTG_Series = new ArrayList<>();

    // Data rate time graph object
    AFxChart FxDataRateTG;
    private List<String> FxDataRateTG_Series = new ArrayList<>();

    // Live-time time graph object
    AFxChart FxLiveTimeTG;
    private List<String> FxLiveTimeTG_Series = new ArrayList<>();

    // Data rate bar graph object
    FxAnimatedBarChart DrBg;
    HashMap<String, Double> bGdata = new HashMap<>();

    // Input buffers bar graph object
    FxAnimatedBarChart InBuffersBg;
    LinkedHashMap<String, Double> ibData = new LinkedHashMap<>();

    // Output buffers bar graph object
    FxAnimatedBarChart OutBuffersBg;
    LinkedHashMap<String, Double> obData = new LinkedHashMap<>();

    // Default time graph averaging
    // interval (smoothing factor) is 1
    volatile int AIV = 1;

    /********************************************
     * Control flags
     */
    AtomicBoolean ResetRequest = new AtomicBoolean();
    // DiaLog box is up prevents the other same
    // type dialog box coming up. For e.g. unresponsive
    // component reset confirmation popup.
    AtomicBoolean
            dialog_is_up = new AtomicBoolean();
    boolean isMulticast;
    public boolean help = true;
    private boolean enable_fx = true;

    /********************************************
     * Selection and configuration
     */
    // Reporting TS component
    AAgentData tsComponent;
    // Component tree selection result.
    volatile String selectedComponent = AConstants.udf;
    // Currently selected component. EventNumber
    // and FileName will be shown on the main UI.
    volatile String monitoredComponent = AConstants.udf;
    // String keeps previous state
    // necessary for abort transition
    String previousState = AConstants.udf;
    public String _runState;
    String _runType;
    String _session;
    String _supervisorName;
    int _runNumber;
    String _runStartTime;
    String _runEndTime;
    String _autoStart = AConstants.setoff;
    public int _eventLimit;
    public long _dataLimit;
    public long _timeLimit;
    int _nScheduledRunsRemaining;
    public int _nScheduledRuns;
    private String userRunConfig = "unset";
    private String userDir = "unset";
    String codaHome;
    String UIMulticastUDL = AConstants.udf;
    String agentsSubRType;
    String supervisorSubRType;

    /********************************************
     * Local database, Maps and containers
     */
    // Components who's data must be graphed in the time graph
    Map<String, AAgentData>
            graphingCompList = Collections.synchronizedMap(
            new LinkedHashMap<String, AAgentData>());
    // Components who are reporting to this UI
    Map<String, AAgentData>
            reportingCompDataMap = Collections.synchronizedMap(
            new LinkedHashMap<String, AAgentData>());
    // Contains the name of the active runType in
    // the session with values = 1 or 0
    // 1 indicates that the messages of that runType
    // components will be shown in the message board.
    Map<String, Integer> msgReportingRuTypes =
            Collections.synchronizedMap(
                    new HashMap<String, Integer>());
    // Map of all platform registered runType
    // supervisors colored red (int > 0) for a
    // specific session set by the rcGui
    Map<String, Integer>
            _sessionConfigsColored = new HashMap<>();
    // Platform registered sessions
    ConcurrentHashMap<String, ASessionInfo> platformSessions;
    //Platform registered help docs
    ConcurrentHashMap<String, String> platformDocs;
    private Map<String, String>
            userRTVMap = new HashMap<>();

    /********************************************
     * Date formats
     */
    NumberFormat formatter = new DecimalFormat("#0.0");
    SimpleDateFormat startEndFormatter =
            new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    // Main Status callback object
    private StatusCB mainStatusCB;

    // go and pause icons
    ImageIcon goIcon, pauseIcon;

    /**********************************************
     * Bar cgarts creation
     */
    AtomicBoolean isDrBgCreated = new AtomicBoolean(false);
    AtomicInteger inBufReportSize = new AtomicInteger(0);
    AtomicInteger outBufReportSize = new AtomicInteger(0);

    Color bgColor;

    boolean isMonitorOnly = false;

    /**
     * Constructor
     *
     * @param c {@link CommandLine} object reference, contains
     *          command-line/xml defined variables.
     */
    public CodaRcGui(CommandLine c) {
        String plHost;
        String startTime;

        isMonitorOnly = c.isMonitorOnly();
//        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        cl = c;
        if (cl.isNoFx()) enable_fx = false;
        codaHome = System.getenv("CODA");
        if (codaHome == null) {
            System.out.println("Warning: CODA distribution dir is not defined. " +
                    "Using AFECS distribution instead.");
            codaHome = System.getenv("AFECS_HOME");
            if (codaHome == null) {
                System.out.println("Severe: can not run. $CODA or " +
                        "$AFECS_HOME are not set. exiting...");
                System.exit(1);
            }
        }

        initComponents();
        goIcon = new ImageIcon(codaHome + "/common/images/afecs/go.png");
        pauseIcon = new ImageIcon(codaHome + "/common/images/afecs/pause.png");

        addWindowListener(new WindowListener() {
                              public void windowOpened(WindowEvent e) {
                              }

                              public void windowClosing(WindowEvent e) {
                                  localExit();
                              }

                              public void windowClosed(WindowEvent e) {
                                  localExit();
                              }

                              public void windowIconified(WindowEvent e) {
                              }

                              public void windowDeiconified(WindowEvent e) {
                              }

                              public void windowActivated(WindowEvent e) {
                              }

                              public void windowDeactivated(WindowEvent e) {
                              }
                          }
        );

        AConstants.debug.set(cl.getDebug());

        // Create an object of the Afecs base class
        base = new ABase(cl.getPlatformUdl(),
                cl.getPlatformMulticastUdl(),
                "Afecs user",
                cl.getExpid());

        // If UI name is not defined at the
        // command line, create one.
        if (!cl.getName().equals(AConstants.udf)) {
            base.myName = cl.getName();
        } else {
            base.myName = "rcGui-" + new Random().nextInt(100);
        }

        // If connection is not successful use
        // rc multicast to find a platform
        if (!base.isPlatformConnected()) {

            // Connect to the rc domain multicast
            // server and request platform host name
            cMsgMessage m = null;
            for (int i = 0; i < 100; i++) {
                m = base.rcMonitor(300);
                if (m != null) break;
            }
            if (m != null) {
                plHost = m.getSenderHost();
                cMsgPayloadItem item = m.getPayloadItem("IpAddresses");
                try {
                    String[] plHosts = item.getStringArray();
                    // connect with the hostname
                    System.out.println("Info: Please wait... connecting to the platform host = " + plHost);
                    String UIMulticastUDL = base.updateHostUdl(plHost, cl.getPlatformTcpPort());
                    try {
                        base.myPlatformConnection = base.platformConnect(UIMulticastUDL);
                    } catch (cMsgException e) {
                        System.out.println("Failed to connect to IP address = " + plHost);
                    }
                    if (!base.isPlatformConnected()) {

                        // update platform udl and connect
                        if (plHosts.length > 0) {
                            for (String ph : plHosts) {
                                System.out.println("Info: Please wait... connecting to the platform host = " + ph);
                                UIMulticastUDL = base.updateHostUdl(ph, cl.getPlatformTcpPort());
                                try {
                                    base.myPlatformConnection = base.platformConnect(UIMulticastUDL);
                                } catch (cMsgException e) {
                                    System.out.println("Failed to connect to IP address = " + ph);
                                    continue;
                                }
                                break;
                            }
                        }
                    }

                } catch (cMsgException e) {
                    e.printStackTrace();
                }
                if (!base.isPlatformConnected()) {
                    System.out.println(" Can not connect to the " + base.getPlEXPID() + " platform.");
                    System.exit(1);
                }
            } else {
                System.out.println(" Can not find a platform for EXPID = " + base.getPlEXPID());
                System.exit(1);
            }
        }

        startTime = AfecsTool.getCurrentTime();
        System.out.println("**************************************************");
        System.out.println("*             Afecs-3   RunControl GUI           *");
        System.out.println("**************************************************");
        System.out.println("- Name                  = " + base.myName);
        if (isMulticast) {
            System.out.println("- UDL                   = " + UIMulticastUDL);
        } else {
            System.out.println("- UDL                   = " + base.getPlUDL());
        }
        System.out.println("- Start time            = " + startTime);
        System.out.println("- Connected to:");
        System.out.println("- Platform              = " + base.getPlEXPID());
        System.out.println("**************************************************");
        setTitle("Run Control " + base.myName + "                    ");


        // Reference to this UI
        iam = this;

        // Defines which buttons should be
        // enabled/disabled based on the
        // state of the run control
        gDriver = new GuiDriver(iam);

        // Communication with the platform
        // registrar services
        dbDriver = new DbDriver(iam);

        // Create cMsg connection heartbeat
        // checking timer. Timer will started at
        // RcInit (see below)
        cMsgConHB = new RcMsgHeartBeat(iam);

        // Create graphics updating timer object.
        // After successful connect this timer will
        // be started by RcConnect
        gUpdateThread = new RcGraphUpdate(iam);

        // Set the EXPID text field
        ExpidTextField.setText(base.getPlEXPID());


        // Gui initialization (access db's,
        // creation menus, etc.)
        new RcInit(iam).execute();

        // Create user specific application menu
        userAppManager = new UserAppManager();
        userAppManager.readFile();
        for (CUserApp app : userAppManager.getUserApps()) {
            addNewUserAppMenuItem(app);
            addNewUserAppEditMenuItem(app);
        }

        bgColor = outputFileComboBox.getBackground();

        AfecsTool.sleep(2000);
        if (c.isAutoConnect()) {
            new RcConnect(iam).execute();
        }

    }

    void dissableControl() {
        ConfigureButton.setEnabled(false);

        ConnectMenuItem.setEnabled(false);
        DisconnectMenuItem.setEnabled(false);
        releaseComponentMenuItem.setEnabled(false);
        SessionMenu.setEnabled(false);
        CoolMenuItem.setEnabled(false);
        ConfigMenu.setEnabled(false);

        DownloadButton.setEnabled(false);
        PrestartButton.setEnabled(false);
        GoButton.setEnabled(false);
        EndButton.setEnabled(false);
        StartButton.setEnabled(false);
        ResetButton.setEnabled(false);
        PauseButton.setEnabled(false);
        serRunNumberMenuItem.setEnabled(false);

        dissableRTVMenu();
        ConfigurationTextField.setBackground(Color.LIGHT_GRAY);
        disableFileOutputMenuItem.setEnabled(false);
        dalogMenu.setEnabled(false);
        OptionsMenu.setEnabled(false);
        expertmenu.setEnabled(false);

    }

    /**
     * <p>
     * RcUI basic subscriptions
     * </p>
     *
     * @return status of the operation.
     */
    boolean doSubscriptions() {
        boolean status;
        String SRType = _session + "_" + _runType + "/*";
        agentsSubRType = _session + "_" + _runType + "/agents";
        supervisorSubRType = _session + "_" + _runType + "/supervisor";
        String supervisorSubRSubject = _session + "/" + _runType;

        if (status = un_Subscribe()) {
            try {
                // Subscribe messages from the control supervisor
                uiControl = base.myPlatformConnection.subscribe(supervisorSubRSubject,
                        AConstants.UIControlRequest,
                        new ControlCB(iam),
                        null);
                mainStatusCB = new StatusCB(iam);
                statusAgents = base.myPlatformConnection.subscribe(AConstants.GUI,
                        SRType,
                        mainStatusCB,
                        null);

                // Subscribe daLog messages from agents
                daLogAgents = base.myPlatformConnection.subscribe(supervisorSubRSubject,
                        AConstants.AgentReportAlarm,
                        new DaLogCB(iam),
                        null);

                msgReportingRuTypes.put(_runType, 11);
            } catch (cMsgException e) {
                status = false;
                e.printStackTrace();
            }
        }
        return status;
    }

    /**
     * <p>
     * Un-subscribes UI messages
     * </p>
     *
     * @return status of the operation
     */
    boolean un_Subscribe() {
        boolean status = true;
        try {
            if (base.isPlatformConnected()) {
                if (uiControl != null)
                    base.myPlatformConnection.unsubscribe(uiControl);
                if (statusAgents != null) {
                    base.myPlatformConnection.unsubscribe(statusAgents);
                    if (mainStatusCB != null) mainStatusCB.exit();
                }
                if (daLogAgents != null) {
                    base.myPlatformConnection.unsubscribe(daLogAgents);
                    msgReportingRuTypes.clear();
                }
            }
        } catch (cMsgException e) {
            status = false;
            e.printStackTrace();
        }
        return status;
    }

    /**
     * <p>
     * Method that recreates user application
     * menu. This method is used by the
     * UserAppDefGui interface.
     * </p>
     */
    public void recreateUserAppMenu() {
        userApplicationsEditMenu.removeAll();
        userApplicationsMenu.removeAll();
        userApplicationsMenu.add(menuItem10);
        userApplicationsMenu.addSeparator();

        // create user specific application menu
        userAppManager = new UserAppManager();
        userAppManager.readFile();
        for (CUserApp app : userAppManager.getUserApps()) {
            addNewUserAppMenuItem(app);
            addNewUserAppEditMenuItem(app);
        }
    }

    /**
     * <p>
     * Tells supervisor agent to program scheduler
     * </p>
     *
     * @param numberOfRuns number of scheduled runs
     * @param eventLimit   event limit
     * @param dataLimit    data limit in bytes
     * @param timeLimit    time limit in minutes
     */
    public void setAutoMode(int numberOfRuns,
                            int eventLimit,
                            long dataLimit,
                            int timeLimit) {
        if (_runType != null) {
            if (numberOfRuns > 0) {
                _nScheduledRuns = numberOfRuns;
                base.send("sms_" + _runType.trim(),
                        AConstants.SupervisorControlRequestSetNumberOfRuns,
                        Integer.toString(numberOfRuns));
            } else {
                base.send("sms_" + _runType.trim(),
                        AConstants.SupervisorControlRequestResetNumberOfRuns,
                        Integer.toString(0));
                _nScheduledRuns = numberOfRuns;
            }
            if (timeLimit > 0) {
                _timeLimit = timeLimit * 60 * 1000;
                base.send("sms_" + _runType.trim(),
                        AConstants.SupervisorControlRequestSetTimeLimit,
                        Integer.toString(timeLimit));
            } else {
                base.send("sms_" + _runType.trim(),
                        AConstants.SupervisorControlRequestResetTimeLimit,
                        Integer.toString(0));
                _timeLimit = timeLimit;
            }
            if (eventLimit > 0) {
                _eventLimit = eventLimit;
                base.send("sms_" + _runType.trim(),
                        AConstants.SupervisorControlRequestSetEventLimit,
                        Integer.toString(eventLimit));
            } else {
                base.send("sms_" + _runType.trim(),
                        AConstants.SupervisorControlRequestResetEventLimit,
                        Integer.toString(0));
                _eventLimit = eventLimit;
            }
            if (dataLimit > 0) {
                _dataLimit = (dataLimit * 1000000L) / 4L;
                base.send("sms_" + _runType.trim(),
                        AConstants.SupervisorControlRequestSetDataLimit,
                        Long.toString(_dataLimit));
            } else {
                base.send("sms_" + _runType.trim(),
                        AConstants.SupervisorControlRequestResetDataLimit,
                        Long.toString(0));
                _dataLimit = dataLimit;
            }
        }
        _nScheduledRunsRemaining = _nScheduledRuns;
        schedulerVis();
    }

    private void clearGraphTabs() {
        while (Graph1.getTabCount() > 0) {
            Graph1.remove(0);
        }
    }

    /**
     * <p>
     * Recreates COSY graphs
     * </p>
     */
    void _createNewGraphs_cosy() {

        clearGraphTabs();

        // Event rate chart
        EvtRateTG =
                new ACosiTimeChart();
        DefaultChartDecorator dec =
                new DefaultChartDecorator();
        dec.applyFlavor(EvtRateTG.getBaseChart(),
                new FancyTimeTrendFlavor());
        EvtRateTG.getBaseChart().getViewManager().setXScale(
                new Interval(0.0, 30.0, false));
        EvtRateTG.getBaseChart().getViewManager().setYScale(
                new Interval(EvtRateTG.getYmin(), EvtRateTG.getYmax(), false));

        // Data rate chart
        DataRateTG =
                new ACosiTimeChart();
        dec =
                new DefaultChartDecorator();
        dec.applyFlavor(DataRateTG.getBaseChart(),
                new FancyTimeTrendFlavor());
        DataRateTG.getBaseChart().getViewManager().setXScale(
                new Interval(0.0, 30.0, false));
        DataRateTG.getBaseChart().getViewManager().setYScale(
                new Interval(DataRateTG.getYmin(), DataRateTG.getYmax(), false));

        // Live time chart
        LiveTimeTG =
                new ACosiTimeChart();
        dec =
                new DefaultChartDecorator();
        dec.applyFlavor(LiveTimeTG.getBaseChart(),
                new FancyTimeTrendFlavor());
        LiveTimeTG.getBaseChart().getViewManager().setXScale(
                new Interval(0.0, 30.0, false));
        LiveTimeTG.getBaseChart().getViewManager().setYScale(
                new Interval(LiveTimeTG.getYmin(), LiveTimeTG.getYmax(), false));

        // Selected component ( from component data table) individual table
        JScrollPane ICDataTablePane =
                new JScrollPane();
        ICDataTablePane.setViewportView(
                individualCompDataTableFactory.createIndividualCompTable("data"));
        individualCompDataTableFactory.addRowsIndCTable();

        // Add charts to the tabbed chart area
        Graph1.addTab("Event Rate", EvtRateTG.getJFrameContentPane());
        Graph1.addTab("Data Rate", DataRateTG.getJFrameContentPane());

        Graph1.addTab("Client Data", ICDataTablePane);
        Graph1.addTab("Live Time", LiveTimeTG.getJFrameContentPane());

        Graph1.addTab("DRs", null);

    }

    /**
     * <p>
     * Creates FX charts and adds to the tabbed panel
     * </p>
     */
    void _prepareGraphs_fx() {
        // Clear graph area
        clearGraphTabs();

        // Selected component ( from component data table) individual table
        JScrollPane ICDataTablePane =
                new JScrollPane();
        ICDataTablePane.setViewportView(
                individualCompDataTableFactory.createIndividualCompTable("data"));
        individualCompDataTableFactory.addRowsIndCTable();

        // create fx charts
        createFxCharts();

        // Add charts to the tabbed chart area
        Graph1.addTab("Event Rate", FxEvtRateTG.getContentPane());
        Graph1.addTab("Data Rate", FxDataRateTG.getContentPane());
        Graph1.addTab("Client Data", ICDataTablePane);
        Graph1.addTab("Live Time", FxLiveTimeTG.getContentPane());
        Graph1.addTab("LDRs", DrBg.getContentPane());
        Graph1.addTab("InB", InBuffersBg.getContentPane());
        Graph1.addTab("OutB", OutBuffersBg.getContentPane());
//        Graph1.addTab("DRs", null);
//        Graph1.addTab("InB", null);
//        Graph1.addTab("OutB", null);
    }

    /**
     * <p>
     * Creates FX charts. Note if graphing component
     * list or monitored component is not yet defined
     * charts will be created with a single
     * series  = undefined.
     * </p>
     */
    private void createFxCharts() {
        if (!graphingCompList.isEmpty()) {
            for (String s : graphingCompList.keySet()) {
                FxEvtRateTG_Series.add(s);
                FxDataRateTG_Series.add(s);
            }
        } else {
            FxEvtRateTG_Series.add(monitoredComponent);
            FxDataRateTG_Series.add(monitoredComponent);
        }
        FxEvtRateTG = new AFxChart("Event Rate", "Hz", FxEvtRateTG_Series);

        FxDataRateTG = new AFxChart("Data Rate", "KByte/sec", FxDataRateTG_Series);

        if (tsComponent != null) {
            FxLiveTimeTG_Series.add(tsComponent.getName());
        } else {
            FxLiveTimeTG_Series.add(AConstants.udf);
        }
        FxLiveTimeTG = new AFxChart("Live Time", "%", FxLiveTimeTG_Series);

        // Create fx bar chart
        DrBg = new FxAnimatedBarChart("Data Rates", bGdata,
                400,
                200,
                "Readout Controller ID",
                "KByte",
                500, false, 0.0);

        // creating input buffer bar
        InBuffersBg = new FxAnimatedBarChart("Input Buffers", ibData,
                400,
                200,
                "Component ID",
                "Buffer Depth [%]",
                500, true, 100.0);

        // creating output buffer bar
        OutBuffersBg = new FxAnimatedBarChart("Output Buffers", obData,
                400,
                200,
                "Component ID",
                "Buffer Depth [%]",
                500, true, 100.0);


    }

    /**
     * <p>
     * Updates FX charts by checking the
     * graphing components map, monitored
     * component and ts_component settings.
     * </p>
     */
    void updateFxCharts() {
        FxEvtRateTG_Series.clear();
        FxDataRateTG_Series.clear();
        FxLiveTimeTG_Series.clear();

        if (!graphingCompList.isEmpty()) {
            for (String s : graphingCompList.keySet()) {
                FxEvtRateTG_Series.add(s);
                FxDataRateTG_Series.add(s);
            }
        }

        if (!monitoredComponent.equals(AConstants.udf)) {
            FxEvtRateTG_Series.add(monitoredComponent);
            FxDataRateTG_Series.add(monitoredComponent);
        }
        if (tsComponent != null) {
            FxLiveTimeTG_Series.add(tsComponent.getName());
        }


        if (!FxEvtRateTG_Series.contains(AConstants.udf)) {
            FxEvtRateTG.reset();
            FxEvtRateTG.create(FxEvtRateTG_Series);
        }
        if (!FxDataRateTG_Series.contains(AConstants.udf)) {
            FxDataRateTG.reset();
            FxDataRateTG.create(FxDataRateTG_Series);
        }
        if (!FxLiveTimeTG_Series.contains(AConstants.udf)) {
            FxLiveTimeTG.reset();
            FxLiveTimeTG.create(FxLiveTimeTG_Series);
        }
    }

    /**
     * <p>
     * Reset FX charts by removing all series
     * and setting a single series = undefined.
     * </p>
     */
    void resetCharts_fx() {
        bGdata.clear();

        if (FxEvtRateTG != null) {
            FxEvtRateTG.reset();
            FxEvtRateTG_Series.clear();
            FxEvtRateTG_Series.add(AConstants.udf);
        }
        if (FxDataRateTG != null) {
            FxDataRateTG.reset();
            FxDataRateTG_Series.clear();
            FxDataRateTG_Series.add(AConstants.udf);
        }
        if (FxLiveTimeTG != null) {
            FxLiveTimeTG.reset();
            FxLiveTimeTG_Series.clear();
            FxLiveTimeTG_Series.add(AConstants.udf);
        }
    }

    /**
     * <p>
     * Clears all maps and tables.
     * Recreates graphs in the charts,
     * by removing and adding graphs
     * to the appropriate charts.
     * </p>
     */
    void hardResetUi(boolean clearDalog) {
        _clearLocalDB();
        _clearTables(clearDalog);

        if (!isEnable_fx()) {
            _tgHardClear_cosy();
            _tgReset_cosy();
        } else {
            _bgRemove();
        }

        addRemoveSelected2Graph(true);
    }

    /**
     * <p>
     * Recreates graphs in the charts,
     * by removing and adding graphs to
     * the appropriate charts.
     * </p>
     */
    void softResetCharts_cosy() {
        _tgSoftClear_cosy();
        _tgReset_cosy();
        _tgCreateDM_cosy();
    }

    /**
     * <p>
     * Create and add data models for graphing components.
     * Selected components from the components data table
     * will be stored in graphingCompList map.
     * Note. this method is for COSY charts only.
     * </p>
     */
    private void _tgCreateDM_cosy() {
        int i;
        if (graphingCompList != null &&
                !graphingCompList.isEmpty()) {

            // Models for event rate charts
            ATimeChartDataModel m[] =
                    new ATimeChartDataModel[graphingCompList.values().size()];
            i = 0;
            for (AAgentData ad : graphingCompList.values()) {
                m[i] = new ATimeChartDataModel(ad.getName());
                EvtRateTG.getBaseChart().getChartArea().addDataSource(m[i]);
                i++;
            }
            EvtRateTG.models = m;

            // Models for data rate charts
            ATimeChartDataModel dm[] =
                    new ATimeChartDataModel[graphingCompList.values().size()];
            i = 0;
            for (AAgentData ad : graphingCompList.values()) {
                dm[i] = new ATimeChartDataModel(ad.getName());
                DataRateTG.getBaseChart().getChartArea().addDataSource(dm[i]);
                i++;
            }
            DataRateTG.models = dm;
        }

        // Live time model for reporting ts component only
        if (tsComponent != null) {
            ATimeChartDataModel tm[] =
                    new ATimeChartDataModel[1];
            tm[0] = new ATimeChartDataModel(tsComponent.getName());
            LiveTimeTG.getBaseChart().getChartArea().addDataSource(tm[0]);
            LiveTimeTG.models = tm;
        }
    }

    /**
     * <p>
     * Clears time graphs.
     * Note: for COSI charts only
     * </p>
     */
    private void _tgSoftClear_cosy() {

        if (EvtRateTG != null) {
            EvtRateTG.getBaseChart().getChartArea().clear();
        }
        if (DataRateTG != null) {
            DataRateTG.getBaseChart().getChartArea().clear();
        }
        if (LiveTimeTG != null) {
            LiveTimeTG.getBaseChart().getChartArea().clear();
        }
    }

    /**
     * <p>
     * Clears time graphs and removes data sources
     * Note: for COSI charts
     * </p>
     */
    private void _tgHardClear_cosy() {
        if (EvtRateTG != null) {
            cosyTgHardClear(EvtRateTG);
        }
        if (DataRateTG != null) {
            cosyTgHardClear(DataRateTG);
        }
        if (LiveTimeTG != null) {
            cosyTgHardClear(LiveTimeTG);
        }
    }

    private void cosyTgHardClear(ACosiTimeChart tg) {
        tg.getBaseChart().getChartArea().clear();
        int dc = tg.getBaseChart().getChartArea().getDataSourceCount();
        for (int i = 0; i > dc; i++) {
            tg.getBaseChart().getChartArea().removeDataSource(i);
        }

    }

    /**
     * <p>
     * Resets charts.
     * Sets default Y scale
     * Note: for COSI charts only
     * </p>
     */
    private void _tgReset_cosy() {
        for (AAgentData ad : graphingCompList.values()) {

            // event rate update
            if (EvtRateTG != null &&
                    EvtRateTG.getModel(ad.getName()) != null) {
                cosyTgUpdate(EvtRateTG, ad);
            }

            // data rate update
            if (DataRateTG != null &&
                    DataRateTG.getModel(ad.getName()) != null) {
                cosyTgUpdate(DataRateTG, ad);
            }
        }
    }

    private void cosyTgUpdate(ACosiTimeChart tg, AAgentData ad) {
        tg.setYmax(10.0);
        tg.setYmin(0.0);
        tg.getBaseChart().getViewManager().setYScale(
                new Interval(tg.getYmin(), tg.getYmax(), false));
        tg.getModel(ad.getName()).generatePoint(0.0);
        tg.getBaseChart().updateChart(ChartUpdateRequest.UPDATE_ALL);
    }

    /**
     * <p>
     * Removes FX bar graph and removes
     * chart area dedicated tab also.
     * </p>
     */
    private void _bgRemove() {
        if (DrBg != null) {
            bGdata.clear();
            DrBg.destroy();
            DrBg = null;
            System.gc();

            if (isDrBgCreated.get()) {
                DrBg = new FxAnimatedBarChart("Data Rates", bGdata,
                        400,
                        200,
                        "Readout Controller ID",
                        "KByte",
                        500, false, 0.0);
                Graph1.setComponentAt(4, DrBg.getContentPane());
            }
            isDrBgCreated.set(false);
        }
        if (InBuffersBg != null) {
            ibData.clear();
            InBuffersBg.destroy();
            InBuffersBg = null;
            System.gc();

            if (inBufReportSize.get() > 0) {
                InBuffersBg = new FxAnimatedBarChart("Input Buffers", ibData,
                        400,
                        200,
                        "Component ID",
                        "Buffer Depth [%]",
                        500, true, 100.0);
                Graph1.setComponentAt(5, InBuffersBg.getContentPane());
            }
            inBufReportSize.set(0);
        }
        if (OutBuffersBg != null) {
            obData.clear();
            OutBuffersBg.destroy();
            OutBuffersBg = null;
            System.gc();

            if (outBufReportSize.get() > 0) {
                OutBuffersBg = new FxAnimatedBarChart("Output Buffers", obData,
                        400,
                        200,
                        "Component ID",
                        "Buffer Depth [%]",
                        500, true, 100.0);
                Graph1.setComponentAt(6, OutBuffersBg.getContentPane());

            }
            outBufReportSize.set(0);

        }
    }

    void setupDataRateBGTab(Container nC) {

        Graph1.setComponentAt(4, nC);
    }

    void setupInputBufferBGTab(Container nC) {

        Graph1.setComponentAt(5, nC);
    }

    void setupOutputBufferBGTab(Container nC) {

        Graph1.setComponentAt(6, nC);
    }

    /**
     * <p>
     * Clears local databases
     * </p>
     */
    private void _clearLocalDB() {
        reportingCompDataMap.clear();
        graphingCompList.clear();
        tsComponent = null;
    }

    /**
     * <p>
     * Clears UI tables
     * </p>
     */
    void _clearTables(boolean clearDalog) {
        dataTableFactory.clearTable();
        if (clearDalog) daLogTableFactory.clearTable();
    }


    /**
     * <p>
     * Update of the daLogTable, or message board of the rcGUI
     * </p>
     *
     * @param name       the name of the agent/client
     * @param txt        of the daLog message
     * @param severity   of the message
     * @param severityId of the message
     */
    void updateDaLogTable(String name,
                          String txt,
                          String severity,
                          int severityId) {
        String[] dl = new String[5];
        dl[0] = name;
        dl[1] = txt;
        dl[2] = new SimpleDateFormat("HH:mm:ss MM/dd").format(new Date());
        dl[3] = severity;
        dl[4] = Integer.toString(severityId);

        // update daLog table
        try {
            DefaultTableModel tm = daLogTableFactory.getTableModel();
            JTable tb = daLogTableFactory.getTable();
            if (tm != null && tb != null) {
                tm.addRow(dl);
                tb.scrollRectToVisible(tb.getCellRect(tm.getRowCount() - 1, 0, true));
                 //UI Tables and Tree
                int daLogTableHistory = 30000;
                if (tm.getRowCount() > daLogTableHistory) {
                    tm.removeRow(0);
                }
            }
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
//            System.out.println("DDD ArrayIndexOutOfBoundsException "+e.getMessage());
        }

    }

    /**
     * <p>
     * Adds/removes the selected component to
     * the graphingCompList map for graphing
     * in the charts area
     * </p>
     *
     * @param add if true adds the graph to the
     *            chart, false removes it from
     *            the chart.
     */
    synchronized void addRemoveSelected2Graph(boolean add) {
        if (!selectedComponent.equals(AConstants.udf)) {
            if (!graphingCompList.containsKey(selectedComponent) &&
                    add) {
                if (reportingCompDataMap.containsKey(selectedComponent)) {
                    graphingCompList.put(selectedComponent,
                            reportingCompDataMap.get(selectedComponent));
                }

            } else if (graphingCompList.containsKey(selectedComponent) &&
                    !add) {
                graphingCompList.remove(selectedComponent);
                selectedComponent = monitoredComponent;
            }
        }
    }


    /**
     * <p>
     * Sends message to the control supervisor agent asking to
     * disable/deactivate component before the next configure.
     * </p>
     */
    private synchronized void disableSelectedComponent() {
        if (!selectedComponent.equals(AConstants.udf)) {
            if (reportingCompDataMap.containsKey(selectedComponent)) {
                base.send(_supervisorName,
                        AConstants.SupervisorControlRequestDisableAgent,
                        selectedComponent);
            }
        }
    }

    /**
     * <p>
     * Sends message to the control supervisor agent asking to
     * disable/deactivate all components having state=disconnected.
     * </p>
     */
    private synchronized void disableAllDisconnectedComponents() {
        base.send(_supervisorName,
                AConstants.SupervisorControlRequestDisableAllDisconnects,
                "");
    }


    /**
     * <p>
     * Asks supervisor agent to report the persistency agent,
     * i.e. the tail agent of the configuration,
     * the agent who possibly will writes data to the file
     * </p>
     */
    void defineDefaultWatch() {
        cMsgMessage msg = null;
        try {
            msg = base.p2pSend(_supervisorName,
                    AConstants.SupervisorReportPersistencyComponent,
                    "",
                    AConstants.TIMEOUT);
        } catch (AException e) {
            System.out.println(e.getMessage());
        }

        if (msg != null) {
            String cm = msg.getText();
            if (reportingCompDataMap.containsKey(cm)) {
                monitoredComponent = cm;
                selectedComponent = cm;
                addRemoveSelected2Graph(true);
            }
        }
    }

    /**
     * <p>
     * This is called when component name on the table is clicked
     * </p>
     *
     * @param e List Selection event
     */
    public void valueChanged(ListSelectionEvent e) {
        Object o = dataTableFactory.getSelectedName();
        if (o != null) {
            if (reportingCompDataMap.containsKey(o.toString())) {
                selectedComponent = o.toString();
            }
        }
    }

    /**
     * <p>
     * Adds the rtv name:value
     * to the local rtv map.
     * </p>
     *
     * @param rtv   rtv (i.e. %(xyz))
     * @param value value of the rtv
     */
    public void addRTV(String rtv, String value) {
        userRTVMap.put(rtv, value);
    }

    /**
     * <p>
     * returns the list of all rtv in
     * the local rv map that are not set.
     * </p>
     *
     * @return list of rtv (i.e. %(xyz))
     */
    public List<String> getUnsetRTVs() {
        List<String> l = new ArrayList<>();
        for (String s : userRTVMap.keySet()) {
            if (userRTVMap.get(s).equals("unset")) {
                l.add(s);
            }
        }
        return l;
    }

    /**
     * <p>
     * Sets the values for
     * %(config) and %(dir) rtv
     * </p>
     */
    void updateRTVGui() {
        for (String s : userRTVMap.keySet()) {
            if (s.equals("%(config)")) {
                userRunConfigTextField.setText(userRTVMap.get(s));
                userRunConfig = userRTVMap.get(s);
            } else if (s.equals("%(dir)")) {
                userDirTestField.setText(userRTVMap.get(s));
                userDir = userRTVMap.get(s);
            }
        }
    }

    /**
     * <p>
     * Pops up a dialog box with the message
     * </p>
     *
     * @param message text
     */
    void popupInfoDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    void popupWarningDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * <p>
     * Pops up a dialog box with the a question.
     * </p>
     *
     * @param message     text
     * @param buttonTexts texts of buttons
     * @return int = JOptionPane.YES_OPTION
     * or JOptionPane.NO_OPTION
     */
    public int popupQuestionDialog(String message,
                                   String[] buttonTexts) {
        return JOptionPane.showOptionDialog(
                this
                , message
                , "Confirm"
                , JOptionPane.YES_NO_OPTION
                , JOptionPane.PLAIN_MESSAGE
                , null
                , buttonTexts
                , buttonTexts[buttonTexts.length - 1]
        );
    }

    /**
     * <p>
     * Pops up a dialog box with three
     * buttons: reset, abort, cancel
     * </p>
     *
     * @return int indicating button selection
     */
    private int popupResetDialog() {
        String[] choices = {"Reset", "Cancel"};
        return JOptionPane.showOptionDialog(
                this
                , "Are you sure you want to reset control system?"
                , "Reset"
                , JOptionPane.YES_NO_OPTION
                , JOptionPane.PLAIN_MESSAGE
                , null
                , choices
                , "Cancel"
        );
    }

    /**
     * <p>
     * Shows programmed scheduler data
     * </p>
     */
    void schedulerVis() {

        // event limit
        if (_eventLimit > 0) {
            EventLimitTextField.setText(Integer.toString(_eventLimit));
            EventLimitTextField.setEnabled(true);
            EventLimitTextField.setBackground(Color.YELLOW);
        } else {
            EventLimitTextField.setText("0");
            EventLimitTextField.setEnabled(false);
            EventLimitTextField.setBackground(Color.WHITE);
            _eventLimit = 0;

        }

        // data limit
        if (_dataLimit > 0) {
            DataLimitTextField.setText(Long.toString((_dataLimit * 4L) / 1000000L));
            DataLimitTextField.setEnabled(true);
            DataLimitTextField.setBackground(Color.YELLOW);
        } else {
            DataLimitTextField.setText("0");
            DataLimitTextField.setEnabled(false);
            DataLimitTextField.setBackground(Color.WHITE);
            _dataLimit = 0;

        }

        // time limit
        if (_timeLimit > 0) {
            TimeLimitTextField.setText(Long.toString(_timeLimit / 60000L));
            TimeLimitTextField.setBackground(Color.YELLOW);

            if (_autoStart.equals(AConstants.seton)) {
                if (_nScheduledRunsRemaining > 0) {
                    setTitle("Run Control " +
                            base.myName +
                            "           auto-mode with time limit = " +
                            _timeLimit / 60000 + " min. Scheduled runs = " +
                            _nScheduledRunsRemaining);
                } else {
                    setTitle("Run Control " +
                            base.myName +
                            "           auto-mode with time limit = " +
                            _timeLimit / 60000 +
                            " min.");
                }
            } else {
                setTitle("Run Control " +
                        base.myName +
                        "                    Time limit = " +
                        _timeLimit / 60000 +
                        " min.");
            }

        } else {
            TimeLimitTextField.setText("0");
            TimeLimitTextField.setBackground(Color.WHITE);

            if (_autoStart.equals(AConstants.seton)) {
                if (_nScheduledRunsRemaining > 0) {
                    setTitle("Run Control " +
                            base.myName +
                            "           auto-mode. Scheduled runs = " +
                            _nScheduledRunsRemaining);
                } else {
                    setTitle("Run Control " +
                            base.myName +
                            "           auto-mode");
                }
            } else {
                setTitle("Run Control " +
                        base.myName +
                        "                    ");
            }
        }
        repaint();
    }


    /**
     * <p>
     * Shows on the UI run-start and run-end times
     * </p>
     */
    void startEndRunTimeVis() {
        StartTimeTextField.setText(_runStartTime);
        EndTimeTextField.setText((_runEndTime));
    }

    /**
     * <p>
     * Adds a new user application menu item
     * with an action listener
     * </p>
     *
     * @param app CUserApp object reference
     */
    public void addNewUserAppMenuItem(CUserApp app) {
        // create and add menu items
        JMenuItem mi = new JMenuItem();
        mi.setText(app.getName());
        mi.addActionListener(
                e -> {
                    JMenuItem m = (JMenuItem) e.getSource();
                    for (CUserApp app1 : userAppManager.getUserApps()) {
                        if (app1.getName().equals(m.getText().trim())) {
                            List<String> l = new ArrayList<>();
                            for (String s : app1.getCommand()) {
                                if (AfecsTool.containsRTV(s)) {
                                    l.add(AfecsTool.replaceRtvsWithRunNumber(s, getRTVMap(),
                                            getRunNumberTextField().getText()));
                                } else {
                                    l.add(s);
                                }
                            }
                            // fork user process async
                            try {
                                AfecsTool.fork(l, false);
                            } catch (AException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
        );
        userApplicationsMenu.add(mi);
        userApplicationsMenu.addSeparator();
    }

    /**
     * <p>
     * Adds user application edit menu item
     * </p>
     *
     * @param app CUserApp object reference
     */
    public void addNewUserAppEditMenuItem(CUserApp app) {
        // create and add menu items
        JMenuItem mi = new JMenuItem();
        mi.setText(app.getName());
        mi.addActionListener(
                e -> {
                    JMenuItem m = (JMenuItem) e.getSource();
                    for (CUserApp app1 : userAppManager.getUserApps()) {
                        if (app1.getName().equals(m.getText().trim())) {
                            new UserAppDefGui(iam, app1).setVisible(true);
                        }
                    }
                }
        );
        userApplicationsEditMenu.add(mi);
        userApplicationsEditMenu.addSeparator();
    }

    /**
     * <p>
     * Clears entire UI
     * </p>
     */
    void _clear_gui() {
        // reset component table
        reportingCompDataMap.clear();
        dataTableFactory.clearTable();
        daLogTableFactory.clearTable();

        // reset charts
        hardResetUi(true);

        // reset total events and component watch fields
        getMonitorCompTextField().setText("");
        getEventNumberTextField().setText("0");
        getOutputFileComboBox().removeAllItems();

    }

    /**
     * <p>
     * Exit gracefully
     * </p>
     */
    private void localExit() {
        un_Subscribe();
        setVisible(false);
        dispose();
        System.exit(0);
    }

    private void schedulerMenuitemActionPerformed(ActionEvent e) {
        new AutoModeForm(iam).setVisible(true);
    }

    public UserAppManager getUserAppManager() {
        return userAppManager;
    }

    boolean isEnable_fx() {
        return enable_fx;
    }

    JTextField getConfigurationTextField() {
        return ConfigurationTextField;
    }

    JTextField getRunStateTextField() {
        return RunStateTextField;
    }

    JLabel getStatusLabel() {
        return StatusLabel;
    }

    JTextField getStartTimeTextField() {
        return StartTimeTextField;
    }

    JTextField getEndTimeTextField() {
        return EndTimeTextField;
    }

    JTextField getSessionTextField() {
        return SessionTextField;
    }

    JButton getConfigureButton() {
        return ConfigureButton;
    }

    void enableRTVMenu() {
        rtvConfigMenuItem.setEnabled(true);
        rtvDirMMenuItem.setEnabled(true);
    }

    void dissableRTVMenu() {
        rtvConfigMenuItem.setEnabled(false);
        rtvDirMMenuItem.setEnabled(false);
    }

    Map<String, String> getRTVMap() {
        return userRTVMap;
    }

    JMenuItem getConnectMenuItem() {
        return ConnectMenuItem;
    }

    JMenuItem getDisableFileOutputMenuItem() {
        return disableFileOutputMenuItem;
    }

    public void setDisableFileOutputMenuItem(JMenuItem disableFileOutputMenuItem) {
        this.disableFileOutputMenuItem = disableFileOutputMenuItem;
    }

    JMenuItem getEnableFileOutputMenuItem() {
        return enableFileOutputMenuItem;
    }

    public void setEnableFileOutputMenuItem(JMenuItem enableFileOutputMenuItem) {
        this.enableFileOutputMenuItem = enableFileOutputMenuItem;
    }

    JMenuItem getDisconnectMenuItem() {
        return DisconnectMenuItem;
    }

     JMenuItem getReleaseComponentMenuItem() {
        return releaseComponentMenuItem;
    }

     JMenu getSessionMenu() {
        return SessionMenu;
    }

     JMenuItem getCoolMenuItem() {
        return CoolMenuItem;
    }

     JMenu getConfigMenu() {
        return ConfigMenu;
    }

     JButton getDownloadButton() {
        return DownloadButton;
    }

     JButton getPrestartButton() {
        return PrestartButton;
    }

     JButton getGoButton() {
        return GoButton;
    }

     JButton getEndButton() {
        return EndButton;
    }

     JButton getStartButton() {
        return StartButton;
    }

     JButton getPauseButton() {
        return PauseButton;
    }

     JButton getResetButton() {
        return ResetButton;
    }

     JMenuItem getRunNumberSetMenuItem() {
        return serRunNumberMenuItem;
    }

     JTextField getEventNumberTextField() {
        return EventNumberTextField;
    }

     JTextField getMonitorCompTextField() {
        return MonitorCompTextField;
    }

     JTextField getExpidTextField() {
        return ExpidTextField;
    }

     NewSessionAction getAction22() {
        return action22;
    }

     public JTextField getRunNumberTextField() {
        return RunNumberTextField;
    }


    JMenu getHelpMenu() {
        return HelpMenu;
    }

    JPanel getRunStatePanel() {
        return RunStatePanel;
    }

    JComboBox getOutputFileComboBox() {
        return outputFileComboBox;
    }

    String getAutoStart() {
        return _autoStart;
    }

    private JTextField getEventLimitTextField() {
        return EventLimitTextField;
    }

    private JTextField getDataLimitTextField() {
        return DataLimitTextField;
    }

    String getUserDir() {
        userDir = userDirTestField.getText().trim();
        return userDir;
    }

    String getUserRunConfig() {
        userRunConfig = userRunConfigTextField.getText().trim();
        return userRunConfig;
    }

    JTextField getUserRunConfigTextField() {
        return userRunConfigTextField;
    }

    JTextField getUserDirTestField() {
        return userDirTestField;
    }

    /**
     * <p>
     * Sends a messages to the supervisor
     * asking to reset scheduler, i.e. event,
     * time, and number of runs limits.
     * </p>
     */
    void resetScheduler() {
        _timeLimit = 0;
        _eventLimit = 0;
        _dataLimit = 0;
        _nScheduledRuns = 0;
        _autoStart = AConstants.setoff;
        base.send("sms_" + _runType.trim(),
                AConstants.SupervisorControlRequestResetTimeLimit, Integer.toString(0));

        base.send("sms_" + _runType.trim(),
                AConstants.SupervisorControlRequestResetEventLimit,
                Integer.toString(0));

        base.send("sms_" + _runType.trim(),
                AConstants.SupervisorControlRequestResetDataLimit,
                Long.toString(0));

        base.send("sms_" + _runType.trim(),
                AConstants.SupervisorControlRequestDisableAutoMode,
                Integer.toString(0));

        base.send("sms_" + _runType.trim(),
                AConstants.SupervisorControlRequestResetNumberOfRuns,
                Integer.toString(0));
        gDriver._updateControlBtNs(RcStates.getEnum(_runState));
        schedulerVis();
    }

    public void selectRunType(String runType) {
        hardResetUi(true);
        resetCharts_fx();
        monitoredComponent = AConstants.udf;
        getConfigurationTextField().setText(runType);
        getOutputFileComboBox().removeAllItems();
        getEventNumberTextField().setText("");
        getMonitorCompTextField().setText("");
        getEventLimitTextField().setText("0");
        getDataLimitTextField().setText("0.0");
        // disconnect action
        new RcDisconnect(iam).execute();

        // connect action
        new RcConnect(iam).execute();

    }

    public void showMBSRunType(String rts) {
        if (msgReportingRuTypes.containsKey(rts) &&
                !rts.equals(ConfigurationTextField.getText().trim())) {
            if (msgReportingRuTypes.get(rts) == 1) {
                msgReportingRuTypes.put(rts, 0);
            } else {
                msgReportingRuTypes.put(rts, 1);
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        menuBar1 = new JMenuBar();
        ControlMenu = new JMenu();
        ConnectMenuItem = new JMenuItem();
        DisconnectMenuItem = new JMenuItem();
        releaseComponentMenuItem = new JMenuItem();
        dalogMenu = new JMenu();
        menuItem12 = new JMenuItem();
        menuItem13 = new JMenuItem();
        menuItem1 = new JMenuItem();
        SessionMenu = new JMenu();
        menuItem9 = new JMenuItem();
        ConfigMenu = new JMenu();
        CoolMenuItem = new JMenuItem();
        OptionsMenu = new JMenu();
        UIMenu = new JMenu();
        messageBoardMenu = new JMenu();
        menuItem17 = new JMenuItem();
        msgBoardSubscriptionMenueItem = new JMenuItem();
        ChartsMenu = new JMenu();
        menuItem7 = new JMenuItem();
        menuItem8 = new JMenuItem();
        menuItem2 = new JMenuItem();
        menuItem4 = new JMenuItem();
        WatchMenuItem = new JMenuItem();
        menu2 = new JMenu();
        schedulerMenuitem = new JMenuItem();
        menuItem15 = new JMenuItem();
        menuItem3 = new JMenuItem();
        expertmenu = new JMenu();
        disableComponentMenuItem = new JMenuItem();
        DisableAllmenuItem = new JMenuItem();
        serRunNumberMenuItem = new JMenuItem();
        disableFileOutputMenuItem = new JMenuItem();
        enableFileOutputMenuItem = new JMenuItem();
        userMenu = new JMenu();
        menu3 = new JMenu();
        rtvConfigMenuItem = new JMenuItem();
        rtvDirMMenuItem = new JMenuItem();
        menuItem19 = new JMenuItem();
        menu5 = new JMenu();
        userApplicationsMenu = new JMenu();
        menuItem10 = new JMenuItem();
        userApplicationsEditMenu = new JMenu();
        HelpMenu = new JMenu();
        toolBar1 = new JToolBar();
        ConfigureButton = new JButton();
        DownloadButton = new JButton();
        PrestartButton = new JButton();
        GoButton = new JButton();
        EndButton = new JButton();
        StartButton = new JButton();
        ResetButton = new JButton();
        PauseButton = new JButton();
        panel11 = new JPanel();
        StartTimeTextField = new JTextField();
        panel12 = new JPanel();
        EndTimeTextField = new JTextField();
        StatusLabel = new JLabel();
        RunStatusPanel = new JPanel();
        panel8 = new JPanel();
        RunNumberTextField = new JTextField();
        RunStatePanel = new JPanel();
        RunStateTextField = new JTextField();
        panel10 = new JPanel();
        EventLimitTextField = new JTextField();
        panel13 = new JPanel();
        DataLimitTextField = new JTextField();
        panel2 = new JPanel();
        EventNumberTextField = new JTextField();
        panel17 = new JPanel();
        TimeLimitTextField = new JTextField();
        panel19 = new JPanel();
        MonitorCompTextField = new JTextField();
        RunParametersPanel = new JPanel();
        panel4 = new JPanel();
        ExpidTextField = new JTextField();
        panel5 = new JPanel();
        SessionTextField = new JTextField();
        panel6 = new JPanel();
        ConfigurationTextField = new JTextField();
        panel7 = new JPanel();
        outputFileComboBox = new JComboBox();
        panel16 = new JPanel();
        userRunConfigTextField = new JTextField();
        panel18 = new JPanel();
        userDirTestField = new JTextField();
        DataTablePane = new JScrollPane();
        Graph1 = new JTabbedPane();
        UIManager.put("TabbedPane.selected", new Color(255, 233, 123));
        SwingUtilities.updateComponentTreeUI(Graph1);
        DalogPane = new JScrollPane();
        action1 = new ConfigAction();
        action2 = new DownloadAction();
        action3 = new ExitAction();
        action4 = new PrestartAction();
        action5 = new GoAction();
        action6 = new EndAction();
        action7 = new PauseAction();
        action8 = new StartRunAction();
        action9 = new ResetAction();
        action10 = new ConnectSupervisorAction();
        action11 = new DisconnectSupervisorAction();
        action12 = new ChartsClearAction();
        action17 = new AddChartAction();
        action18 = new RemoveChartAction();
        action14 = new SmoothingAction();
        action19 = new ReleaseComponentsAction();
        action20 = new RunTypeMenuAction();
        action13 = new SetWatchAction();
        action22 = new NewSessionAction();
        action24 = new SetRunNumber();
        action25 = new StartDalogArchiverAction();
        action26 = new StopDalogArchiverAction();
        action28 = new ResetAutoModeAction();
        action29 = new DisableComponentAction();
        action30 = new DisableAllAction();
        action33 = new ClearDalogTableAction();
        action36 = new MsgBoardSubscribeAction();
        action37 = new SchedulerClearAction();
        action40 = new UAppNewAction();
        action41 = new RunConfigAction();
        action42 = new ScriptDirSetAction();
        action43 = new RtvListAction();
        action21 = new DisableOutputAction();
        action23 = new EnableOutputAction();

        //======== this ========
        setTitle("CODA Run Control");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();

        //======== menuBar1 ========
        {

            //======== ControlMenu ========
            {
                ControlMenu.setText("Control");

                //---- ConnectMenuItem ----
                ConnectMenuItem.setAction(action10);
                ControlMenu.add(ConnectMenuItem);
                ControlMenu.addSeparator();

                //---- DisconnectMenuItem ----
                DisconnectMenuItem.setAction(action11);
                DisconnectMenuItem.setEnabled(false);
                ControlMenu.add(DisconnectMenuItem);
                ControlMenu.addSeparator();

                //---- releaseComponentMenuItem ----
                releaseComponentMenuItem.setAction(action19);
                ControlMenu.add(releaseComponentMenuItem);
                ControlMenu.addSeparator();

                //======== dalogMenu ========
                {
                    dalogMenu.setText("dalogMsg");

                    //---- menuItem12 ----
                    menuItem12.setAction(action25);
                    menuItem12.setText("Start Archiver");
                    dalogMenu.add(menuItem12);
                    dalogMenu.addSeparator();

                    //---- menuItem13 ----
                    menuItem13.setAction(action26);
                    menuItem13.setText("Stop Archiver");
                    dalogMenu.add(menuItem13);
                }
                ControlMenu.add(dalogMenu);
                ControlMenu.addSeparator();

                //---- menuItem1 ----
                menuItem1.setAction(action3);
                ControlMenu.add(menuItem1);
            }
            menuBar1.add(ControlMenu);

            //======== SessionMenu ========
            {
                SessionMenu.setText("Sessions");

                //---- menuItem9 ----
                menuItem9.setAction(action22);
                menuItem9.setText("New...");
                SessionMenu.add(menuItem9);
                SessionMenu.addSeparator();
            }
            menuBar1.add(SessionMenu);

            //======== ConfigMenu ========
            {
                ConfigMenu.setText("Configurations");

                //---- CoolMenuItem ----
                CoolMenuItem.setText("Coda Config");
                CoolMenuItem.setAction(action20);
                ConfigMenu.add(CoolMenuItem);
            }
            menuBar1.add(ConfigMenu);

            //======== OptionsMenu ========
            {
                OptionsMenu.setText("Options");

                //======== UIMenu ========
                {
                    UIMenu.setText("User Interface");

                    //======== messageBoardMenu ========
                    {
                        messageBoardMenu.setText("Message Board");

                        //---- menuItem17 ----
                        menuItem17.setAction(action33);
                        menuItem17.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
                        menuItem17.setText("Clear");
                        messageBoardMenu.add(menuItem17);
                        messageBoardMenu.addSeparator();

                        //---- msgBoardSubscriptionMenueItem ----
                        msgBoardSubscriptionMenueItem.setAction(action36);
                        msgBoardSubscriptionMenueItem.setText("Subscription");
                        messageBoardMenu.add(msgBoardSubscriptionMenueItem);
                    }
                    UIMenu.add(messageBoardMenu);
                }
                OptionsMenu.add(UIMenu);
                OptionsMenu.addSeparator();

                //======== ChartsMenu ========
                {
                    ChartsMenu.setText("Charts");

                    //---- menuItem7 ----
                    menuItem7.setAction(action17);
                    menuItem7.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_MASK));
                    ChartsMenu.add(menuItem7);
                    ChartsMenu.addSeparator();

                    //---- menuItem8 ----
                    menuItem8.setAction(action18);
                    menuItem8.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_MASK));
                    ChartsMenu.add(menuItem8);
                    ChartsMenu.addSeparator();

                    //---- menuItem2 ----
                    menuItem2.setAction(action14);
                    menuItem2.setText("Smoothing...");
                    ChartsMenu.add(menuItem2);
                    ChartsMenu.addSeparator();

                    //---- menuItem4 ----
                    menuItem4.setAction(action12);
                    menuItem4.setText("Clear");
                    ChartsMenu.add(menuItem4);
                }
                OptionsMenu.add(ChartsMenu);
                OptionsMenu.addSeparator();

                //---- WatchMenuItem ----
                WatchMenuItem.setAction(action13);
                OptionsMenu.add(WatchMenuItem);
                OptionsMenu.addSeparator();

                //======== menu2 ========
                {
                    menu2.setText("Scheduler");

                    //---- schedulerMenuitem ----
                    schedulerMenuitem.setText("Program...");
                    schedulerMenuitem.addActionListener(e -> schedulerMenuitemActionPerformed(e));
                    menu2.add(schedulerMenuitem);
                    menu2.addSeparator();

                    //---- menuItem15 ----
                    menuItem15.setAction(action28);
                    menuItem15.setText("Disable");
                    menuItem15.setActionCommand("Disable");
                    menu2.add(menuItem15);
                    menu2.addSeparator();

                    //---- menuItem3 ----
                    menuItem3.setAction(action37);
                    menuItem3.setText("Clear Limits");
                    menu2.add(menuItem3);
                }
                OptionsMenu.add(menu2);
            }
            menuBar1.add(OptionsMenu);

            //======== expertmenu ========
            {
                expertmenu.setText("Expert");

                //---- disableComponentMenuItem ----
                disableComponentMenuItem.setAction(action29);
                expertmenu.add(disableComponentMenuItem);
                expertmenu.addSeparator();

                //---- DisableAllmenuItem ----
                DisableAllmenuItem.setAction(action30);
                DisableAllmenuItem.setText("Disable Disconnected/Error");
                expertmenu.add(DisableAllmenuItem);
                expertmenu.addSeparator();

                //---- serRunNumberMenuItem ----
                serRunNumberMenuItem.setAction(action24);
                serRunNumberMenuItem.setText("Set Run Number");
                expertmenu.add(serRunNumberMenuItem);
                expertmenu.addSeparator();

                //---- disableFileOutputMenuItem ----
                disableFileOutputMenuItem.setAction(action21);
                expertmenu.add(disableFileOutputMenuItem);
                expertmenu.addSeparator();

                //---- enableFileOutputMenuItem ----
                enableFileOutputMenuItem.setAction(action23);
                expertmenu.add(enableFileOutputMenuItem);
            }
            menuBar1.add(expertmenu);

            //======== userMenu ========
            {
                userMenu.setText("User");

                //======== menu3 ========
                {
                    menu3.setText("Runtime Variables");
                    menu3.addSeparator();

                    //---- rtvConfigMenuItem ----
                    rtvConfigMenuItem.setAction(action41);
                    rtvConfigMenuItem.setText("Config File: %(config)");
                    menu3.add(rtvConfigMenuItem);
                    menu3.addSeparator();

                    //---- rtvDirMMenuItem ----
                    rtvDirMMenuItem.setAction(action42);
                    rtvDirMMenuItem.setText("Directory: %(dir)");
                    menu3.add(rtvDirMMenuItem);
                    menu3.addSeparator();

                    //---- menuItem19 ----
                    menuItem19.setAction(action43);
                    menu3.add(menuItem19);
                }
                userMenu.add(menu3);
                userMenu.addSeparator();

                //======== menu5 ========
                {
                    menu5.setText("Applications");

                    //======== userApplicationsMenu ========
                    {
                        userApplicationsMenu.setText("Create/Execute");

                        //---- menuItem10 ----
                        menuItem10.setAction(action40);
                        userApplicationsMenu.add(menuItem10);
                        userApplicationsMenu.addSeparator();
                    }
                    menu5.add(userApplicationsMenu);
                    menu5.addSeparator();

                    //======== userApplicationsEditMenu ========
                    {
                        userApplicationsEditMenu.setText("Edit/Remove");
                    }
                    menu5.add(userApplicationsEditMenu);
                }
                userMenu.add(menu5);
            }
            menuBar1.add(userMenu);

            //======== HelpMenu ========
            {
                HelpMenu.setText("Help");
            }
            menuBar1.add(HelpMenu);
        }
        setJMenuBar(menuBar1);

        //======== toolBar1 ========
        {

            //---- ConfigureButton ----
            ConfigureButton.setAction(action1);
            ConfigureButton.setEnabled(false);
            ConfigureButton.setIcon(new ImageIcon(codaHome+"/common/images/afecs/configure.png"));
            toolBar1.add(ConfigureButton);

            //---- DownloadButton ----
            DownloadButton.setAction(action2);
            DownloadButton.setEnabled(false);
            DownloadButton.setIcon(new ImageIcon(codaHome+"/common/images/afecs/download.png"));
            toolBar1.add(DownloadButton);

            //---- PrestartButton ----
            PrestartButton.setAction(action4);
            PrestartButton.setEnabled(false);
            PrestartButton.setIcon(new ImageIcon(codaHome+"/common/images/afecs/prestart.png"));
            toolBar1.add(PrestartButton);

            //---- GoButton ----
            GoButton.setAction(action5);
            GoButton.setEnabled(false);
            GoButton.setIcon(new ImageIcon(codaHome+"/common/images/afecs/go.png"));
            toolBar1.add(GoButton);

            //---- EndButton ----
            EndButton.setAction(action6);
            EndButton.setEnabled(false);
            EndButton.setIcon(new ImageIcon(codaHome+"/common/images/afecs/end.png"));
            toolBar1.add(EndButton);

            //---- StartButton ----
            StartButton.setAction(action8);
            StartButton.setEnabled(false);
            StartButton.setIcon(new ImageIcon(codaHome+"/common/images/afecs/startrun.png"));
            toolBar1.add(StartButton);

            //---- ResetButton ----
            ResetButton.setAction(action9);
            ResetButton.setEnabled(false);
            ResetButton.setIcon(new ImageIcon(codaHome+"/common/images/afecs/reset.png"));
            toolBar1.add(ResetButton);

            //---- PauseButton ----
            PauseButton.setAction(action7);
            PauseButton.setEnabled(false);
            PauseButton.setIcon(new ImageIcon(codaHome+"/common/images/afecs/pause.png"));
            toolBar1.add(PauseButton);
            toolBar1.addSeparator();

            //======== panel11 ========
            {
                panel11.setBorder(new TitledBorder("Start Time"));

                //---- StartTimeTextField ----
                StartTimeTextField.setEditable(false);

                GroupLayout panel11Layout = new GroupLayout(panel11);
                panel11.setLayout(panel11Layout);
                panel11Layout.setHorizontalGroup(
                    panel11Layout.createParallelGroup()
                        .addGroup(panel11Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(StartTimeTextField, GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                            .addContainerGap())
                );
                panel11Layout.setVerticalGroup(
                    panel11Layout.createParallelGroup()
                        .addGroup(panel11Layout.createSequentialGroup()
                            .addComponent(StartTimeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, Short.MAX_VALUE))
                );
            }
            toolBar1.add(panel11);

            //======== panel12 ========
            {
                panel12.setBorder(new TitledBorder("End Time"));

                //---- EndTimeTextField ----
                EndTimeTextField.setEditable(false);

                GroupLayout panel12Layout = new GroupLayout(panel12);
                panel12.setLayout(panel12Layout);
                panel12Layout.setHorizontalGroup(
                    panel12Layout.createParallelGroup()
                        .addGroup(panel12Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(EndTimeTextField, GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                            .addContainerGap())
                );
                panel12Layout.setVerticalGroup(
                    panel12Layout.createParallelGroup()
                        .addGroup(panel12Layout.createSequentialGroup()
                            .addComponent(EndTimeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, Short.MAX_VALUE))
                );
            }
            toolBar1.add(panel12);
            toolBar1.addSeparator();

            //---- StatusLabel ----
            StatusLabel.setIcon(null);
            StatusLabel.setIcon(new ImageIcon(codaHome+"/common/images/afecs/afecs.png"));
            toolBar1.add(StatusLabel);
        }

        //======== RunStatusPanel ========
        {
            RunStatusPanel.setBorder(new TitledBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), "Run Status"));

            //======== panel8 ========
            {
                panel8.setBorder(new TitledBorder("Run Number"));

                //---- RunNumberTextField ----
                RunNumberTextField.setEditable(false);
                RunNumberTextField.setFont(new Font("Dialog", Font.BOLD, 12));
                RunNumberTextField.setForeground(new Color(153, 0, 51));

                GroupLayout panel8Layout = new GroupLayout(panel8);
                panel8.setLayout(panel8Layout);
                panel8Layout.setHorizontalGroup(
                    panel8Layout.createParallelGroup()
                        .addGroup(panel8Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(RunNumberTextField)
                            .addContainerGap())
                );
                panel8Layout.setVerticalGroup(
                    panel8Layout.createParallelGroup()
                        .addGroup(panel8Layout.createSequentialGroup()
                            .addComponent(RunNumberTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 14, Short.MAX_VALUE))
                );
            }

            //======== RunStatePanel ========
            {
                RunStatePanel.setBorder(new TitledBorder("Run State"));

                //---- RunStateTextField ----
                RunStateTextField.setEditable(false);
                RunStateTextField.setBackground(Color.white);
                RunStateTextField.setFont(new Font("Dialog", Font.BOLD, 12));

                GroupLayout RunStatePanelLayout = new GroupLayout(RunStatePanel);
                RunStatePanel.setLayout(RunStatePanelLayout);
                RunStatePanelLayout.setHorizontalGroup(
                    RunStatePanelLayout.createParallelGroup()
                        .addGroup(RunStatePanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(RunStateTextField, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                RunStatePanelLayout.setVerticalGroup(
                    RunStatePanelLayout.createParallelGroup()
                        .addGroup(RunStatePanelLayout.createSequentialGroup()
                            .addComponent(RunStateTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 14, Short.MAX_VALUE))
                );
            }

            //======== panel10 ========
            {
                panel10.setBorder(new TitledBorder("Event Limit"));

                //---- EventLimitTextField ----
                EventLimitTextField.setText("0");
                EventLimitTextField.setEditable(false);

                GroupLayout panel10Layout = new GroupLayout(panel10);
                panel10.setLayout(panel10Layout);
                panel10Layout.setHorizontalGroup(
                    panel10Layout.createParallelGroup()
                        .addGroup(panel10Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(EventLimitTextField, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(7, Short.MAX_VALUE))
                );
                panel10Layout.setVerticalGroup(
                    panel10Layout.createParallelGroup()
                        .addGroup(panel10Layout.createSequentialGroup()
                            .addComponent(EventLimitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 13, Short.MAX_VALUE))
                );
            }

            //======== panel13 ========
            {
                panel13.setBorder(new TitledBorder("Data Limit"));

                //---- DataLimitTextField ----
                DataLimitTextField.setText("0");
                DataLimitTextField.setEditable(false);

                GroupLayout panel13Layout = new GroupLayout(panel13);
                panel13.setLayout(panel13Layout);
                panel13Layout.setHorizontalGroup(
                    panel13Layout.createParallelGroup()
                        .addGroup(panel13Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(DataLimitTextField, GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(10, Short.MAX_VALUE))
                );
                panel13Layout.setVerticalGroup(
                    panel13Layout.createParallelGroup()
                        .addGroup(panel13Layout.createSequentialGroup()
                            .addComponent(DataLimitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 8, Short.MAX_VALUE))
                );
            }

            //======== panel2 ========
            {
                panel2.setBorder(new TitledBorder("Total Events"));

                //---- EventNumberTextField ----
                EventNumberTextField.setEditable(false);
                EventNumberTextField.setFont(new Font("Dialog", Font.BOLD, 18));
                EventNumberTextField.setForeground(new Color(153, 0, 51));

                GroupLayout panel2Layout = new GroupLayout(panel2);
                panel2.setLayout(panel2Layout);
                panel2Layout.setHorizontalGroup(
                    panel2Layout.createParallelGroup()
                        .addGroup(panel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(EventNumberTextField)
                            .addContainerGap())
                );
                panel2Layout.setVerticalGroup(
                    panel2Layout.createParallelGroup()
                        .addGroup(panel2Layout.createSequentialGroup()
                            .addComponent(EventNumberTextField, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 6, Short.MAX_VALUE))
                );
            }

            //======== panel17 ========
            {
                panel17.setBorder(new TitledBorder("Time Limit (min.)"));

                //---- TimeLimitTextField ----
                TimeLimitTextField.setText("0");
                TimeLimitTextField.setEditable(false);
                TimeLimitTextField.setEnabled(false);

                GroupLayout panel17Layout = new GroupLayout(panel17);
                panel17.setLayout(panel17Layout);
                panel17Layout.setHorizontalGroup(
                    panel17Layout.createParallelGroup()
                        .addGroup(panel17Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(TimeLimitTextField, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(12, Short.MAX_VALUE))
                );
                panel17Layout.setVerticalGroup(
                    panel17Layout.createParallelGroup()
                        .addGroup(panel17Layout.createSequentialGroup()
                            .addComponent(TimeLimitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 9, Short.MAX_VALUE))
                );
            }

            //======== panel19 ========
            {
                panel19.setBorder(new TitledBorder("Watch Component"));

                //---- MonitorCompTextField ----
                MonitorCompTextField.setEditable(false);

                GroupLayout panel19Layout = new GroupLayout(panel19);
                panel19.setLayout(panel19Layout);
                panel19Layout.setHorizontalGroup(
                    panel19Layout.createParallelGroup()
                        .addGroup(panel19Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(MonitorCompTextField, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(7, Short.MAX_VALUE))
                );
                panel19Layout.setVerticalGroup(
                    panel19Layout.createParallelGroup()
                        .addGroup(panel19Layout.createSequentialGroup()
                            .addComponent(MonitorCompTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 3, Short.MAX_VALUE))
                );
            }

            GroupLayout RunStatusPanelLayout = new GroupLayout(RunStatusPanel);
            RunStatusPanel.setLayout(RunStatusPanelLayout);
            RunStatusPanelLayout.setHorizontalGroup(
                RunStatusPanelLayout.createParallelGroup()
                    .addGroup(RunStatusPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(RunStatusPanelLayout.createParallelGroup()
                            .addGroup(RunStatusPanelLayout.createSequentialGroup()
                                .addComponent(panel8, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(RunStatePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addComponent(panel2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panel19, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(RunStatusPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                            .addComponent(panel17, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panel10, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panel13, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
            );
            RunStatusPanelLayout.setVerticalGroup(
                RunStatusPanelLayout.createParallelGroup()
                    .addGroup(RunStatusPanelLayout.createSequentialGroup()
                        .addGroup(RunStatusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(panel10, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(RunStatePanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panel8, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(RunStatusPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addGroup(RunStatusPanelLayout.createSequentialGroup()
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                                .addComponent(panel19, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGroup(RunStatusPanelLayout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(panel13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(panel17, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addGap(12, 12, 12))
            );
        }

        //======== RunParametersPanel ========
        {
            RunParametersPanel.setBorder(new TitledBorder(new SoftBevelBorder(SoftBevelBorder.RAISED), "Run Parameters"));

            //======== panel4 ========
            {
                panel4.setBorder(new TitledBorder("Expid"));

                //---- ExpidTextField ----
                ExpidTextField.setEditable(false);

                GroupLayout panel4Layout = new GroupLayout(panel4);
                panel4.setLayout(panel4Layout);
                panel4Layout.setHorizontalGroup(
                    panel4Layout.createParallelGroup()
                        .addGroup(panel4Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(ExpidTextField, GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                            .addContainerGap())
                );
                panel4Layout.setVerticalGroup(
                    panel4Layout.createParallelGroup()
                        .addGroup(panel4Layout.createSequentialGroup()
                            .addComponent(ExpidTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, Short.MAX_VALUE))
                );
            }

            //======== panel5 ========
            {
                panel5.setBorder(new TitledBorder("Session"));

                //---- SessionTextField ----
                SessionTextField.setEditable(false);
                SessionTextField.setText("undefined");

                GroupLayout panel5Layout = new GroupLayout(panel5);
                panel5.setLayout(panel5Layout);
                panel5Layout.setHorizontalGroup(
                    panel5Layout.createParallelGroup()
                        .addGroup(panel5Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(SessionTextField, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
                panel5Layout.setVerticalGroup(
                    panel5Layout.createParallelGroup()
                        .addGroup(panel5Layout.createSequentialGroup()
                            .addComponent(SessionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, Short.MAX_VALUE))
                );
            }

            //======== panel6 ========
            {
                panel6.setBorder(new TitledBorder("Configuration"));

                //---- ConfigurationTextField ----
                ConfigurationTextField.setEditable(false);
                ConfigurationTextField.setText("undefined");

                GroupLayout panel6Layout = new GroupLayout(panel6);
                panel6.setLayout(panel6Layout);
                panel6Layout.setHorizontalGroup(
                    panel6Layout.createParallelGroup()
                        .addGroup(panel6Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(ConfigurationTextField, GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                            .addContainerGap())
                );
                panel6Layout.setVerticalGroup(
                    panel6Layout.createParallelGroup()
                        .addGroup(panel6Layout.createSequentialGroup()
                            .addComponent(ConfigurationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 8, Short.MAX_VALUE))
                );
            }

            //======== panel7 ========
            {
                panel7.setBorder(new TitledBorder("Output File"));

                GroupLayout panel7Layout = new GroupLayout(panel7);
                panel7.setLayout(panel7Layout);
                panel7Layout.setHorizontalGroup(
                    panel7Layout.createParallelGroup()
                        .addGroup(panel7Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(outputFileComboBox, GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addContainerGap())
                );
                panel7Layout.setVerticalGroup(
                    panel7Layout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, panel7Layout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(outputFileComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                );
            }

            //======== panel16 ========
            {
                panel16.setBorder(new TitledBorder("User RTV  %(config)"));

                //---- userRunConfigTextField ----
                userRunConfigTextField.setText("unset");
                userRunConfigTextField.setEditable(false);

                GroupLayout panel16Layout = new GroupLayout(panel16);
                panel16.setLayout(panel16Layout);
                panel16Layout.setHorizontalGroup(
                    panel16Layout.createParallelGroup()
                        .addGroup(panel16Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(userRunConfigTextField, GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addContainerGap())
                );
                panel16Layout.setVerticalGroup(
                    panel16Layout.createParallelGroup()
                        .addGroup(panel16Layout.createSequentialGroup()
                            .addComponent(userRunConfigTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 8, Short.MAX_VALUE))
                );
            }

            //======== panel18 ========
            {
                panel18.setBorder(new TitledBorder("User RTV  %(dir)"));

                //---- userDirTestField ----
                userDirTestField.setEditable(false);
                userDirTestField.setText("unset");

                GroupLayout panel18Layout = new GroupLayout(panel18);
                panel18.setLayout(panel18Layout);
                panel18Layout.setHorizontalGroup(
                    panel18Layout.createParallelGroup()
                        .addGroup(panel18Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(userDirTestField, GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                            .addContainerGap())
                );
                panel18Layout.setVerticalGroup(
                    panel18Layout.createParallelGroup()
                        .addGroup(panel18Layout.createSequentialGroup()
                            .addComponent(userDirTestField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 9, Short.MAX_VALUE))
                );
            }

            GroupLayout RunParametersPanelLayout = new GroupLayout(RunParametersPanel);
            RunParametersPanel.setLayout(RunParametersPanelLayout);
            RunParametersPanelLayout.setHorizontalGroup(
                RunParametersPanelLayout.createParallelGroup()
                    .addGroup(GroupLayout.Alignment.TRAILING, RunParametersPanelLayout.createSequentialGroup()
                        .addGroup(RunParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(panel18, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(RunParametersPanelLayout.createSequentialGroup()
                                .addComponent(panel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(panel16, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panel7, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
            );
            RunParametersPanelLayout.setVerticalGroup(
                RunParametersPanelLayout.createParallelGroup()
                    .addGroup(RunParametersPanelLayout.createSequentialGroup()
                        .addGroup(RunParametersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(panel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panel5, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panel4, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panel7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panel16, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panel18, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            );
        }

        //======== DataTablePane ========
        {
            DataTablePane.setViewportView(dataTableFactory.createDataTable("data",this));
        }

        //======== Graph1 ========
        {
            Graph1.setForeground(new Color(0, 77, 77));
            Graph1.setBackground(Color.orange);
        }

        //======== DalogPane ========
        {
                    // create and add daLogBrowser specific table
                    DalogPane.setViewportView(daLogTableFactory.createDaLogTable("daLog"));
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addGap(1, 1, 1)
                    .addComponent(toolBar1, GroupLayout.DEFAULT_SIZE, 847, Short.MAX_VALUE))
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addComponent(DataTablePane, GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
                        .addComponent(RunParametersPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addComponent(RunStatusPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Graph1, GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)))
                .addComponent(DalogPane, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 848, Short.MAX_VALUE)
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addComponent(toolBar1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addComponent(RunStatusPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addGap(8, 8, 8)
                            .addComponent(RunParametersPanel, GroupLayout.PREFERRED_SIZE, 254, GroupLayout.PREFERRED_SIZE)))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addComponent(Graph1, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                        .addComponent(DataTablePane, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(DalogPane, GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }


    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JMenuBar menuBar1;
    private JMenu ControlMenu;
    private JMenuItem ConnectMenuItem;
    private JMenuItem DisconnectMenuItem;
    private JMenuItem releaseComponentMenuItem;
    private JMenu dalogMenu;
    private JMenuItem menuItem12;
    private JMenuItem menuItem13;
    private JMenuItem menuItem1;
    private JMenu SessionMenu;
    private JMenuItem menuItem9;
    private JMenu ConfigMenu;
    private JMenuItem CoolMenuItem;
    private JMenu OptionsMenu;
    private JMenu UIMenu;
    private JMenu messageBoardMenu;
    private JMenuItem menuItem17;
    private JMenuItem msgBoardSubscriptionMenueItem;
    private JMenu ChartsMenu;
    private JMenuItem menuItem7;
    private JMenuItem menuItem8;
    private JMenuItem menuItem2;
    private JMenuItem menuItem4;
    private JMenuItem WatchMenuItem;
    private JMenu menu2;
    private JMenuItem schedulerMenuitem;
    private JMenuItem menuItem15;
    private JMenuItem menuItem3;
    private JMenu expertmenu;
    private JMenuItem disableComponentMenuItem;
    private JMenuItem DisableAllmenuItem;
    private JMenuItem serRunNumberMenuItem;
    private JMenuItem disableFileOutputMenuItem;
    private JMenuItem enableFileOutputMenuItem;
    private JMenu userMenu;
    private JMenu menu3;
    private JMenuItem rtvConfigMenuItem;
    private JMenuItem rtvDirMMenuItem;
    private JMenuItem menuItem19;
    private JMenu menu5;
    private JMenu userApplicationsMenu;
    private JMenuItem menuItem10;
    private JMenu userApplicationsEditMenu;
    private JMenu HelpMenu;
    private JToolBar toolBar1;
    private JButton ConfigureButton;
    private JButton DownloadButton;
    private JButton PrestartButton;
    private JButton GoButton;
    private JButton EndButton;
    private JButton StartButton;
    private JButton ResetButton;
    private JButton PauseButton;
    private JPanel panel11;
    private JTextField StartTimeTextField;
    private JPanel panel12;
    private JTextField EndTimeTextField;
    private JLabel StatusLabel;
    private JPanel RunStatusPanel;
    private JPanel panel8;
    private JTextField RunNumberTextField;
    private JPanel RunStatePanel;
    private JTextField RunStateTextField;
    private JPanel panel10;
    private JTextField EventLimitTextField;
    private JPanel panel13;
    private JTextField DataLimitTextField;
    private JPanel panel2;
    private JTextField EventNumberTextField;
    private JPanel panel17;
    private JTextField TimeLimitTextField;
    private JPanel panel19;
    private JTextField MonitorCompTextField;
    private JPanel RunParametersPanel;
    private JPanel panel4;
    private JTextField ExpidTextField;
    private JPanel panel5;
    private JTextField SessionTextField;
    private JPanel panel6;
    private JTextField ConfigurationTextField;
    private JPanel panel7;
    private JComboBox outputFileComboBox;
    private JPanel panel16;
    private JTextField userRunConfigTextField;
    private JPanel panel18;
    private JTextField userDirTestField;
    private JScrollPane DataTablePane;
    private JTabbedPane Graph1;
    private JScrollPane DalogPane;
    private ConfigAction action1;
    private DownloadAction action2;
    private ExitAction action3;
    private PrestartAction action4;
    private GoAction action5;
    private EndAction action6;
    private PauseAction action7;
    private StartRunAction action8;
    private ResetAction action9;
    private ConnectSupervisorAction action10;
    private DisconnectSupervisorAction action11;
    private ChartsClearAction action12;
    private AddChartAction action17;
    private RemoveChartAction action18;
    private SmoothingAction action14;
    private ReleaseComponentsAction action19;
    private RunTypeMenuAction action20;
    private SetWatchAction action13;
    private NewSessionAction action22;
    private SetRunNumber action24;
    private StartDalogArchiverAction action25;
    private StopDalogArchiverAction action26;
    private ResetAutoModeAction action28;
    private DisableComponentAction action29;
    private DisableAllAction action30;
    private ClearDalogTableAction action33;
    private MsgBoardSubscribeAction action36;
    private SchedulerClearAction action37;
    private UAppNewAction action40;
    private RunConfigAction action41;
    private ScriptDirSetAction action42;
    private RtvListAction action43;
    private DisableOutputAction action21;
    private EnableOutputAction action23;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class ExitAction extends AbstractAction {
        private ExitAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Exit");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            localExit();
        }
    }


    /**
     * <p>
     * Download button action
     * </p>
     */
    private class DownloadAction extends AbstractAction {
        private DownloadAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(SHORT_DESCRIPTION, "Download");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            new RcCodaSMCmd(iam, CodaSM.DOWNLOAD).execute();
        }
    }

    /**
     * <p>
     * Prestart button action
     * </p>
     */
    private class PrestartAction extends AbstractAction {
        private PrestartAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(SHORT_DESCRIPTION, "Prestart");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnable_fx()) {
                softResetCharts_cosy();
            } else {
                resetCharts_fx();
            }
            new RcCodaSMCmd(iam, CodaSM.PRESTART).execute();
        }
    }

    /**
     * <p>
     * Go button action
     * </p>
     */
    private class GoAction extends AbstractAction {
        private GoAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(SHORT_DESCRIPTION, "Go");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            userRTVMap.put("%(rn)", getRunNumberTextField().getText());
            new RcCodaSMCmd(iam, CodaSM.GO).execute();
        }
    }

    /**
     * <p>
     * End button action
     * </p>
     */
    private class EndAction extends AbstractAction {
        private EndAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(SHORT_DESCRIPTION, "End");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {

            _autoStart = AConstants.setoff;

            PauseButton.setIcon(new ImageIcon(codaHome + "/common/images/afecs/pause.png"));

            base.send(_supervisorName,
                    AConstants.SupervisorControlRequestDisableAutoMode,
                    0);

            if (_timeLimit > 0) {
                setTitle("Run Control " +
                        base.myName +
                        "                    Time limit = " +
                        _timeLimit / 60000 +
                        " min.");
            } else {
                setTitle("Run Control " +
                        base.myName +
                        "                    ");
            }
            repaint();
            new RcCodaSMCmd(iam, CodaSM.END).execute();
        }
    }

    /**
     * <p>
     * Pause/resume button action
     * </p>
     */
    private class PauseAction extends AbstractAction {
        private PauseAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(SHORT_DESCRIPTION, "Pause");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (RunStateTextField.getText().trim().equals(AConstants.active)) {
                base.send(_supervisorName, AConstants.SupervisorControlRequestPause, 0);
                RunStateTextField.setBackground(new Color(204, 204, 0));
                PauseButton.setIcon(goIcon);

            } else if (RunStateTextField.getText().trim().equals(AConstants.paused)) {
                base.send(_supervisorName, AConstants.SupervisorControlRequestResume, 0);
            }
        }
    }

    /**
     * <p>
     * start-run button action
     * </p>
     */
    private class StartRunAction extends AbstractAction {
        private StartRunAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(SHORT_DESCRIPTION, "Start");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            _autoStart = AConstants.seton;
            base.send(_supervisorName,
                    AConstants.SupervisorControlRequestEnableAutoMode,
                    Integer.toString(0));
            if (!isEnable_fx()) {
                softResetCharts_cosy();
            } else {
                resetCharts_fx();
            }
            new RcCodaSMCmd(iam, CodaSM.STARTRUN).execute();
        }
    }

    /**
     * <p>
     * reset button action
     * </p>
     */
    private class ResetAction extends AbstractAction {
        private ResetAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(SHORT_DESCRIPTION, "Reset");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            int answ = popupResetDialog();
            if (answ == JOptionPane.YES_OPTION) {
                _autoStart = AConstants.setoff;
                base.send(_supervisorName,
                        AConstants.SupervisorControlRequestDisableAutoMode,
                        0);

                gDriver._updateControlBtNs(RcStates.CONNECTED);
                new RcCodaSMCmd(iam, CodaSM.RESET).execute();
            }
        }
    }

    /**
     * <p>
     * connect button action
     * </p>
     */
    private class ConnectSupervisorAction extends AbstractAction {
        private ConnectSupervisorAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Connect");
            putValue(SHORT_DESCRIPTION, "Connect to a control supervisor");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            new RcConnect(iam).execute();
        }
    }


    /**
     * <p>
     * disconnect button action
     * </p>
     */
    private class DisconnectSupervisorAction extends AbstractAction {
        private DisconnectSupervisorAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Disconnect");
            putValue(SHORT_DESCRIPTION, "Disconnect from the control supervisor");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.SHIFT_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            new RcDisconnect(iam).execute();
        }
    }


    /**
     * <p>
     * Chart reset menu action
     * </p>
     */
    private class ChartsClearAction extends AbstractAction {
        private ChartsClearAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Clear");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (isEnable_fx()) {
                updateFxCharts();
            } else {
                softResetCharts_cosy();
            }
        }
    }

    /**
     * <p>
     * Add chart menu action
     * </p>
     */
    private class AddChartAction extends AbstractAction {
        private AddChartAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Add");
            putValue(SHORT_DESCRIPTION, "Add selected component data to the chart");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            addRemoveSelected2Graph(true);
            if (isEnable_fx()) {
                updateFxCharts();
            } else {
                softResetCharts_cosy();
            }
        }
    }

    /**
     * <p>
     * Remove chart menu action
     * </p>
     */
    private class RemoveChartAction extends AbstractAction {
        private RemoveChartAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Remove");
            putValue(SHORT_DESCRIPTION, "Remove selected item from the chart");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.ALT_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            addRemoveSelected2Graph(false);
            if (isEnable_fx()) {
                updateFxCharts();
            } else {
                softResetCharts_cosy();
            }
        }
    }

    /**
     * <p>
     * Smoothing menu action
     * </p>
     */
    private class SmoothingAction extends AbstractAction {
        private SmoothingAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Smoothing");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            // Start dialog with the slider (AIV: Averaging Interval Value)
            ASliderDialog dialog = new ASliderDialog(AConstants.AVERAGING_SIZE);
            dialog.showDialog(iam, AIV, "Smoothing", "Chart");
            if (dialog.getReturnStatus() == ASliderDialog.RET_OK) {
                int i = Integer.parseInt(dialog.getSliderValue());
                if (i > 0) AIV = i;
            }
        }
    }

    /**
     * <p>
     * Release component menu action
     * </p>
     */
    private class ReleaseComponentsAction extends AbstractAction {
        private ReleaseComponentsAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Release Components");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            new RcReleaseAgents(iam).execute();
        }
    }

    /**
     * <p>
     * This will pop up scrollable list showing the cool
     * configuration runType ($COOL_HOME/config/control dir)
     * and will set the forGround color to red to those runTypes
     * that are configured in the selected session.
     * </p>
     */
    private class RunTypeMenuAction extends AbstractAction {
        private RunTypeMenuAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Coda Config");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            new RcClRunTypes(iam).execute();
        }
    }

    /**
     * <p>
     * Set watch menu action
     * </p>
     */
    private class SetWatchAction extends AbstractAction {
        private SetWatchAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Set Watch");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.ALT_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (!selectedComponent.equals(AConstants.udf)) {
                monitoredComponent = selectedComponent;
                MonitorCompTextField.setText(monitoredComponent);
            }
        }
    }


    /**
     * <p>
     * Creating a new session action
     * </p>
     */
    private class NewSessionAction extends AbstractAction {
        private NewSessionAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "New...");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            String response = JOptionPane.showInputDialog(null,
                    "Session Name",
                    "New Session",
                    JOptionPane.QUESTION_MESSAGE);
            if (response != null) {
                final ASessionInfo si = new ASessionInfo();
                si.setName(response);
                si.setRunNumber(1);
                // ask platform to add the new session
                cMsgMessage msg = null;
                try {
                    msg = base.p2pSend(base.getPlEXPID(),
                            AConstants.PlatformInfoRequestAddNewSession,
                            si,
                            AConstants.TIMEOUT);
                } catch (AException e1) {
                    System.out.println(e1.getMessage());
                }
                if (msg != null && !msg.getText().equalsIgnoreCase(AConstants.failed)) {
                    // create and add menu items
                    JMenuItem mi = new JMenuItem();
                    mi.setText(si.getName());
                    mi.addActionListener(
                            e12 -> {
                                JMenuItem m = (JMenuItem) e12.getSource();
                                SessionTextField.setText(m.getText());
                                ConfigurationTextField.setText(si.getConfigName());
                                RunNumberTextField.setText(Integer.toString(si.getRunNumber()));
                                CoolMenuItem.setEnabled(true);
                                ConfigMenu.setEnabled(true);
                                ConnectMenuItem.setEnabled(true);
                                DisconnectMenuItem.setEnabled(false);
                                releaseComponentMenuItem.setEnabled(false);
                            }
                    );
                    SessionMenu.add(mi);
                    SessionMenu.addSeparator();
                } else {
                    popupInfoDialog("  Request failed!");
                }
            }
        }
    }


    /**
     * <p>
     * Set run number action
     * </p>
     */
    private class SetRunNumber extends AbstractAction {
        private SetRunNumber() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Set Run Number");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            new RcSetRunNumber(iam).execute();
        }
    }

    /**
     * <p>
     * Start daLog archiver action
     * </p>
     */
    private class StartDalogArchiverAction extends AbstractAction {
        private StartDalogArchiverAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Start dalogMsg Archiver");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            // ask platform to send all registered agents names
            cMsgMessage msg = null;
            try {
                msg = base.p2pSend(base.getPlEXPID(),
                        AConstants.PlatformInfoRequestIsArchiving,
                        "",
                        AConstants.TIMEOUT);
            } catch (AException e1) {
                System.out.println(e1.getMessage());
            }
            if (msg != null) {
                if (msg.getText().equals(AConstants.yes)) {
                    popupInfoDialog("  Platform daLogMsgArchiver is active.");
                } else if (msg.getText().equals(AConstants.no)) {
                    String message = "  Are you sure to start archiving daLog messages?";
                    String[] buttons = {"Archive", "Cancel"};
                    int answ = popupQuestionDialog(message, buttons);
                    if (answ == JOptionPane.YES_OPTION) {
                        base.send(base.getPlEXPID(), AConstants.PlatformControlArchiverStart, "");
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Stop daLog archiver action
     * </p>
     */
    private class StopDalogArchiverAction extends AbstractAction {
        private StopDalogArchiverAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Stop dalogMsg Archiver");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            // ask platform to send all registered agents names
            cMsgMessage msg = null;
            try {
                msg = base.p2pSend(base.getPlEXPID(),
                        AConstants.PlatformInfoRequestIsArchiving,
                        "",
                        AConstants.TIMEOUT);
            } catch (AException e1) {
                System.out.println(e1.getMessage());
            }
            if (msg != null) {
                if (msg.getText().equals(AConstants.no)) {
                    popupInfoDialog("  Platform daLogMsgArchiver is not active.");
                } else if (msg.getText().equals(AConstants.yes)) {
                    String message = "  Are you sure to stop archiving daLog messages?";
                    String[] buttons = {"Stop", "Cancel"};
                    int answ = popupQuestionDialog(message, buttons);
                    if (answ == JOptionPane.YES_OPTION) {
                        base.send(base.getPlEXPID(), AConstants.PlatformControlArchiverStop, "");
                    }
                }
            }
        }
    }


    /**
     * <p>
     * Reset auto-mod action
     * </p>
     */
    private class ResetAutoModeAction extends AbstractAction {
        private ResetAutoModeAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Disable");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            _autoStart = AConstants.setoff;

            // send supervisor a message to set autoRun on
            base.send(_supervisorName,
                    AConstants.SupervisorControlRequestDisableAutoMode,
                    0);

            if (_timeLimit > 0) {
                setTitle("Run Control " +
                        base.myName +
                        "                    Time limit = " + _timeLimit / 60000 + " min.");
            } else {
                setTitle("Run Control " +
                        base.myName +
                        "                    ");
            }
            gDriver._updateControlBtNs(RcStates.getEnum(_runState));
            repaint();
        }
    }

    /**
     * <p>
     * Disable a component action
     * </p>
     */
    private class DisableComponentAction extends AbstractAction {
        private DisableComponentAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Disable Component");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            String message = "  Disable component = " +
                    selectedComponent + "?\n" +
                    "  Attention! \n" +
                    "  The component will be disabled until the next Configure transition.";
            String[] buttons = {"Disable", "Cancel"};
            int a = popupQuestionDialog(message, buttons);
            if (a == JOptionPane.OK_OPTION) {
                disableSelectedComponent();
                RunStatePanel.setBackground(Color.YELLOW);
            }
        }
    }

    /**
     * <p>
     * Disable all disconnected components action
     * </p>
     */
    private class DisableAllAction extends AbstractAction {
        private DisableAllAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Disable Disconnected");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            String message = "  Disable disconnected components ?\n" +
                    "  Attention! \n" +
                    "  Components will be disabled until the next Configure transition.";
            String[] buttons = {"Disable", "Cancel"};
            int a = popupQuestionDialog(message, buttons);
            if (a == JOptionPane.OK_OPTION) {
                disableAllDisconnectedComponents();
                RunStatePanel.setBackground(Color.YELLOW);
            }
        }
    }


    /**
     * <p>
     * Clear daLog table action
     * </p>
     */
    private class ClearDalogTableAction extends AbstractAction {
        private ClearDalogTableAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Clear daLog Table");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            daLogTableFactory.clearTable();
        }
    }


    /**
     * <p>
     * Shows in a list what runType
     * data is shown in the message board.
     * </p>
     */
    private class MsgBoardSubscribeAction extends AbstractAction {
        private MsgBoardSubscribeAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Subscribe");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            ArrayList<String> activeconfs = new ArrayList<>();

            // first define the list of runtypes/configurations
            // that are running in the current session
            try {
                dbDriver.getPlatformRegisteredRunTypes(AConstants.TIMEOUT);
            } catch (AException e1) {
                updateDaLogTable(base.myName,
                        e1.getMessage(),
                        AConstants.WARN,
                        5);
            }

            if (_sessionConfigsColored != null) {
                for (String s : _sessionConfigsColored.keySet()) {
                    if (_sessionConfigsColored.get(s) > 0) {
                        activeconfs.add(s);
                    }
                }

                if (!activeconfs.isEmpty()) {
                    ArrayList<String> n = new ArrayList<>();
                    ArrayList<Integer> c = new ArrayList<>();

                    for (String s : activeconfs) {
                        if (!msgReportingRuTypes.containsKey(s)) {
                            msgReportingRuTypes.put(s, 0);
                        }
                    }
                    for (String s : msgReportingRuTypes.keySet()) {
                        n.add(s);
                        c.add(msgReportingRuTypes.get(s));
                    }

//                    AListDDialog rd = new AListDDialog();
//                    rd.showDialog(iam,
//                            n.toArray(new String[n.size()]),
//                            c.toArray(new Integer[c.size()]),"Configuration","MB Subscribe",null);

                    new ADialogList(iam,
                            n.toArray(new String[n.size()]),
                            c.toArray(new Integer[c.size()]), "Configuration", "MB Subscribe", null);
                }
            }
        }
    }

    /**
     * <p>
     * Configure action
     * </p>
     */
    private class ConfigAction extends AbstractAction {
        private ConfigAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(SHORT_DESCRIPTION, "Configure");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (_runState.equals(AConstants.booted) ||
                    _runState.equals(AConstants.configured) ||
                    _runState.equals(AConstants.connected) ||
                    _runState.equals(AConstants.reseted) ||
                    _runState.equalsIgnoreCase(AConstants.failed) ||
                    _runState.equals(AConstants.disconnected)
                    ) {

                new RcConfigure(iam).execute();

//                xyz();

            } else {
                popupInfoDialog("  Reset first if you need to " +
                        "configure a new run control !\n");
            }
        }

    }

    /**
     * <p>
     * Scheduler clear action
     * </p>
     */
    private class SchedulerClearAction extends AbstractAction {
        private SchedulerClearAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Clear Limits");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            resetScheduler();
        }
    }


    /**
     * <p>
     * Start user application definition UI action
     * </p>
     */
    private class UAppNewAction extends AbstractAction {
        private UAppNewAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "New...");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            new UserAppDefGui(iam, null).setVisible(true);
        }
    }


    /** DISABLED
     * <p>
     * Starts COOL configuration viewer
     * </p>
     */
    private class ConfigViewAction extends AbstractAction {
        private ConfigViewAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Configuration View");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {


//            ArrayList<String> l = new ArrayList<>();
//
//            // start a new viewer
//            l.add(System.getenv("CODA") +
//                    File.separator + "Linux" +
//                    File.separator + "bin" +
//                    File.separator + "jcedit_view");
//            l.add(_runType);
//            try {
//                AfecsTool.fork(l, false);
//            } catch (AException e1) {
//                if (AConstants.debug.get()) e1.printStackTrace();
//            }
        }
    }

    /**
     * <p>
     * Starts a file chooser to choose user
     * specified run configuration file.
     * Opens a default $COOL_HOME/EXPID/user
     * directory. Selected file is assigned to
     * %(config) system rtv.
     * </p>
     */
    private class RunConfigAction extends AbstractAction {
        private RunConfigAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Config File: %(config)");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            String runConfDir = base.myConfig.getCoolHome() +
                    File.separator + base.myConfig.getPlatformExpid() +
                    File.separator + "user";
            chooser.setCurrentDirectory(new File(runConfDir));
            chooser.setMultiSelectionEnabled(false);
            int option = chooser.showOpenDialog(CodaRcGui.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File sf = chooser.getSelectedFile();
                userRunConfig = sf.getAbsolutePath();
                userRunConfigTextField.setText(userRunConfig);
                addRTV("%(config)", userRunConfig);
            }
        }
    }

    /**
     * <p>
     * Starts a file chooser to choose user
     * specified directory where from scripts
     * will be executed..
     * Opens a default $COOL_HOME/EXPID/user
     * directory. Selected dir is assigned to
     * %(dir) system rtv.
     * </p>
     */
    private class ScriptDirSetAction extends AbstractAction {
        private ScriptDirSetAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Directory: %(dir)");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            String scriptDir = base.myConfig.getCoolHome() +
                    File.separator + base.myConfig.getPlatformExpid() +
                    File.separator + "user";
            chooser.setCurrentDirectory(new File(scriptDir));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = chooser.showOpenDialog(CodaRcGui.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String scriptSetName = chooser.getSelectedFile().getAbsolutePath();
                userDirTestField.setText(scriptSetName);
                addRTV("%(dir)", scriptSetName);
            }
        }
    }

    /**
     * <p>
     * Lists all defined rtvs
     * </p>
     */
    private class RtvListAction extends AbstractAction {
        private RtvListAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "List...");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            Object[][] data = new Object[getRTVMap().size()][getRTVMap().size()];
            int ii = 0;
            for (Map.Entry<String, String> entry : getRTVMap().entrySet()) {
                data[ii++] = new String[]{entry.getKey(), entry.getValue()};
            }
            RtvTable rt = new RtvTable(iam, data);
            rt.setVisible(true);
            for (String s : userRTVMap.keySet()) {
                rt.update(s, userRTVMap.get(s));
            }
        }
    }


    private class DisableOutputAction extends AbstractAction {
        private DisableOutputAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Disable Data File Output");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            base.send(_supervisorName, AConstants.SupervisorControlRequestNoFileOutput, 0);
        }
    }

    private class EnableOutputAction extends AbstractAction {
        private EnableOutputAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Enable Data File Output");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            base.send(_supervisorName, AConstants.SupervisorControlRequestResumeFileOutput, 0);
        }
    }


    private class PlatformConsoleAction extends AbstractAction {
        private PlatformConsoleAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Platform Console");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            // TODO add your code here
        }
    }
}


