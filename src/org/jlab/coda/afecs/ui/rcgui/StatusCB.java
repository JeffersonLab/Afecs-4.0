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

import org.jlab.coda.afecs.cool.ontology.AComponent;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.ATimerExecutor;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.afecs.system.util.LinkedConcurrentHashMap;
import org.jlab.coda.afecs.ui.rcgui.util.AAgentData;
import org.jlab.coda.afecs.ui.rcgui.util.chart.fx.FxAnimatedBarChart;
import org.jlab.coda.cMsg.cMsgCallbackAdapter;
import org.jlab.coda.cMsg.cMsgException;
import org.jlab.coda.cMsg.cMsgMessage;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * Callback for the subscription:
 * <ul>
 * <li>subject = "GUI"</li>
 * <li>type = "session_runType/*"</li>
 * </ul>
 * This is the main class that furnishes the UI
 * Starts a swing worker class to update the UI elements.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */
class StatusCB extends cMsgCallbackAdapter {

    private boolean tableUpdateFlag = true;

    private CodaRcGui owner;

    // run-control specific data, subset of the agents
    // reported data, shown on the rcGUI component table
    private LinkedConcurrentHashMap<String, String[]> _data_m = new LinkedConcurrentHashMap<>();

    // individual component data
    private String[] _id = new String[17];

    private long _evtNumber;
    private ArrayList<String> _outputFile = new ArrayList<>();

    private String _outputFileStatus = "enabled";

    private long prevSupReportTime;
    private AtomicLong deltaSupReportTime = new AtomicLong(0);


    private PeriodicEx atx;

    StatusCB(CodaRcGui owner) {
        this.owner = owner;
        atx = new PeriodicEx(10000);
        atx.start();
    }

    public void callback(cMsgMessage msg, Object userObject) {
        if (msg != null) {
            String type = msg.getType();

            if (type != null) {
                new AAction(msg).execute();
            }
        }
    }

    public void exit() {
        atx.stop();
    }

    /**
     * Method for visualization,
     * updates gui.
     * </p>
     *
     * @param i int
     */
    private void visualization(int i) {
        switch (i) {
            case 1:
                gui_update();
                break;
            case 3:
                owner.getRunStateTextField().setText(owner._runState);

                // update buttons according the the state reporting from the supervisor agent
                owner.gDriver._updateControlBtNs(owner.gDriver.getRcState(owner._runState));

                // visualize event limit, time, limit, auto-mode and scheduled runs
                owner.schedulerVis();
                // visualize run start/end times
                owner.startEndRunTimeVis();

                break;
        }

    }

    /**
     * <p>
     * Updates gui table, tree, etc.
     * </p>
     */
    private void gui_update() {
        if (!owner.getMonitorCompTextField().getText().equals(owner.monitoredComponent)) {
            owner.getMonitorCompTextField().setText(owner.monitoredComponent);
        }
        if (!owner.getEventNumberTextField().getText().equals(NumberFormat.getNumberInstance(Locale.US).format(_evtNumber))) {
            owner.getEventNumberTextField().setText(NumberFormat.getNumberInstance(Locale.US).format(_evtNumber));
        }
        if (_outputFileStatus.equals("disabled")) {
            //@todo write on the main gui "Attention: Data Output is disabled ..."
        }
        for (String s[] : _data_m.values()) {
            String[] d1 = new String[6];
            d1[0] = s[0];
            d1[1] = s[1];
            d1[2] = s[2];
            d1[3] = s[3];
            d1[4] = s[4];
            d1[5] = s[5];
            owner.dataTableFactory.addRcDataTable(d1);
        }

        if (_id[0] != null && owner.selectedComponent != null) {
            if (_id[0].equals(owner.selectedComponent)) {
                owner.individualCompDataTableFactory.updateIndividualCompTable(_id);
            }
        }
        // update input/output buffers in the component individual table
        owner.individualCompDataTableFactory.updateIndComTInputBuffers(owner.selectedComponent, owner.ibData);
        owner.individualCompDataTableFactory.updateIndComTOutputBuffers(owner.selectedComponent, owner.obData);
    }

    /**
     * <p>
     * This method runs in the background.
     * Updates visualization component data,
     * creates fx charts , i.e. prepares for visualization
     * </p>
     *
     * @param msg received message
     * @return int needed by the visualization method
     */
    @SuppressWarnings("unchecked")
    private int bgProcessing(cMsgMessage msg) {
        int out = -1;
        String receivedType = msg.getType();


        // This is agents data reported by
        // the supervisor of the runType
        if (receivedType.equals(owner.agentsSubRType)) {
            //cMsg text defines if data output is disabled
            String fileWriting = msg.getText();

            if (fileWriting.equals("disabled")) {
                owner.getOutputFileComboBox().setBackground(Color.RED);

            } else if (fileWriting.equals("enabled")) {
                owner.getOutputFileComboBox().setBackground(owner.bgColor);
            }

            Map<String, AComponent> cmpU = null;
            try {
                if (msg.getByteArray() != null) {
                    cmpU = (Map<String, AComponent>) AfecsTool.B2O(msg.getByteArray());
                }
            } catch (IOException | ClassNotFoundException e) {

                e.printStackTrace();
                return -1;
            }

            // sort according to their types
            Map<String, AComponent> cmp = AfecsTool.getSortedByType(cmpU);

            if (msg.getSender().contains("sms_")) {
                if (cmp.isEmpty()) {
                    cmp = cmpU;
                }
            } else {
                cmp = cmpU;
            }


            if (!AfecsTool.containsState(cmp, AConstants.checking) &&
                    !AfecsTool.containsState(cmp, AConstants.connected) &&
                    !AfecsTool.containsState(cmp, AConstants.booted) &&
                    !AfecsTool.containsState(cmp, AConstants.udf)) {

                if (owner.monitoredComponent.equals(AConstants.udf)) {

                    if (msg.getSender().contains("sms_")) {
                        owner.defineDefaultWatch();
                    }
                    if (owner.isEnable_fx()) {
                        owner.updateFxCharts();
                    } else {
                        owner.softResetCharts_cosy();
                    }
                }
            }

            if (msg.getSender().contains("sms_")) {
                // Check to see if javaFX is supported by the OS
                if (owner.isEnable_fx()) {
                    // create data rate bar graph if not existed
                    if (!owner.isDrBgCreated.get()) {
                        createDataRateBGFx(cmp);
                    }

                    if (owner.FxEvtRateTG_Series.contains(AConstants.udf)) {
                        owner.updateFxCharts();
                    }
                }
            }

            out = 1;

            updateGuiComponentsData(cmp);

            // message coming from the supervisor agent, reporting its own parameters
        } else if (receivedType.equals(owner.supervisorSubRType)) {

            // measure supervisor reporting time
            long curSupReportTime = AfecsTool.getCurrentTimeInMs();
            if (prevSupReportTime == 0) {
                prevSupReportTime = curSupReportTime;
            }
            deltaSupReportTime.set(curSupReportTime - prevSupReportTime);
            prevSupReportTime = curSupReportTime;

            if (msg.getText() != null) _outputFileStatus = msg.getText();
            out = 3;
            try {

                // supervisor reports its state
                if (msg.getPayloadItem(AConstants.STATE) != null) {

                    owner._runState = msg.getPayloadItem(AConstants.STATE).getString();
                    if (!owner._runState.equals(AConstants.connected) &&
                            !owner._runState.equals(AConstants.booted) &&
                            tableUpdateFlag) {
//                    if (owner._runState.equals(AConstants.downloaded) && tableUpdateFlag) {
                        _data_m.clear();
                        owner._clearTables(false);
                        tableUpdateFlag = false;
                    }

                    if (owner._runState.equals(AConstants.connected) ||
                            owner._runState.equals(AConstants.disconnected) ||
                            owner._runState.equalsIgnoreCase(AConstants.failed)
                            ) {

                        owner.ResetRequest.set(true);
                    }

                    if (msg.getPayloadItem(AConstants.RUNNUMBER) != null &&
                            msg.getPayloadItem(AConstants.RUNNUMBER).getInt() > 0) {
                        owner._runNumber = msg.getPayloadItem(AConstants.RUNNUMBER).getInt();
                    }

                    // get supervisor run start and times, event and time limits, as well as autoStart
                    owner._runStartTime = msg.getPayloadItem(AConstants.RUNSTARTTIME).getString();
                    owner._runEndTime = msg.getPayloadItem(AConstants.RUNENDTIME).getString();
                    owner._autoStart = msg.getPayloadItem(AConstants.AUTOSTART).getString();
                    owner._eventLimit = msg.getPayloadItem(AConstants.EVENTLIMIT).getInt();
                    owner._dataLimit = msg.getPayloadItem(AConstants.DATALIMIT).getLong();
                    owner._timeLimit = msg.getPayloadItem(AConstants.TIMELIMIT).getLong();
                    owner._nScheduledRunsRemaining = msg.getPayloadItem(AConstants.SCHEDULEDRUNS).getInt() - 1;
                }
            } catch (cMsgException e) {
                e.printStackTrace();
            }
        }

        if (msg.isGetRequest()) {
            try {
                cMsgMessage mr = msg.response();
                mr.setSubject(AConstants.udf);
                mr.setType(AConstants.udf);
                owner.base.myPlatformConnection.send(mr);
            } catch (cMsgException e) {
                if (AConstants.debug.get()) e.printStackTrace();
            }
        }

        return out;
    }

    private void createDataRateBGFx(Map<String, AComponent> cmp) {

        //--------- FX Bar graph
        // Create barChart data map, storing data rates
        for (AComponent s : cmp.values()) {
            if (s.getType().equals(ACodaType.ROC.name()) ||
                    s.getType().equals(ACodaType.TS.name()) ||
                    s.getType().equals(ACodaType.GT.name())) {
                owner.bGdata.put(s.getName(), 0.1);
            }
        }
        // Create fx bar chart
        owner.DrBg = new FxAnimatedBarChart("Data Rates", owner.bGdata,
                400,
                200,
                "Readout Controller ID",
                "KByte",
                500, false, 0.0);

        owner.setupDataRateBGTab(owner.DrBg.getContentPane());
        owner.isDrBgCreated.set(true);
    }

    private void createInBufferBGFx(HashMap<String, Double> ibData) {
        // Create fx bar chart
        owner.InBuffersBg = new FxAnimatedBarChart("Input Buffers", ibData,
                400,
                200,
                "Component ID",
                "Buffer Depth [%]",
                500, true, 100.0);

        owner.setupInputBufferBGTab(owner.InBuffersBg.getContentPane());
        owner.inBufReportSize.set(ibData.size());
    }

    private void createOutBufferBGFx(HashMap<String, Double> obData) {
        // Create fx bar chart
        owner.OutBuffersBg = new FxAnimatedBarChart("Output Buffers", obData,
                400,
                200,
                "Component ID",
                "Buffer Depth [%]",
                500, true, 100.0);

        owner.setupOutputBufferBGTab(owner.OutBuffersBg.getContentPane());
        owner.outBufReportSize.set(obData.size());
    }

    /**
     * <p>
     * Updates gui component data, prepares arrays for UI
     * tables visualization, including component main table
     * as well as individual data table data.
     * </p>
     *
     * @param map reference to the map received
     *            as part of the supervisor reporting.
     */
    private void updateGuiComponentsData(Map<String, AComponent> map) {

        for (AComponent comp : map.values()) {

            // update/add new reporting component's subset of the data
            // to the local reporting components map,
            // by creating AAgentData object
            AAgentData _ad;

            if (!comp.getRunType().equals(owner._runType) &&
                    !comp.getRunType().equals(AConstants.udf) &&
                    !comp.getState().equals(AConstants.udf)) {
                comp.setState(AConstants.busy);
            }

            if (owner.reportingCompDataMap.containsKey(comp.getName())) {
                _ad = owner.reportingCompDataMap.get(comp.getName());
                _ad.setType(comp.getType());
                _ad.setState(comp.getState());
                _ad.setEvtNumber(comp.getEventNumber());
                _ad.setEvtRate(comp.getEventRate());
                _ad.setDataRate(comp.getDataRate());
                _ad.setLiveTime(comp.getLiveTime());
                _ad.setEvtRateA(comp.getEventRateAverage());
                _ad.setDataRateA(comp.getDataRateAverage());
            } else {
                _ad = new AAgentData();
                _ad.setName(comp.getName());
                _ad.setType(comp.getType());
                _ad.setState(comp.getState());
                _ad.setEvtNumber(comp.getEventNumber());
                _ad.setEvtRate(comp.getEventRate());
                _ad.setDataRate(comp.getDataRate());
                _ad.setLiveTime(comp.getLiveTime());
                _ad.setEvtRateA(comp.getEventRateAverage());
                _ad.setDataRateA(comp.getDataRateAverage());
                _ad.setMaster(comp.isMaster());
                owner.reportingCompDataMap.put(_ad.getName(), _ad);
            }

            updateComponentIndividualData(comp);

            updateTableTreeData(comp);

            // define reporting ts-component
//            if (comp.getType().equals(ACodaType.TS.name())) {
//                owner.tsComponent = _ad;
//            }
            // ts component, i.e. trigger source component is
            // defined as a ROC that is master defined by JcEdit. 02.17 VG
            System.out.println("DDDD ============================ " + comp.getName()+" "+comp.isMaster());
            if (comp.isMaster()) {
                owner.tsComponent = _ad;
            }

            // show currently monitored component event number and file name
            if (owner.monitoredComponent.equals(comp.getName())) {
                _evtNumber = comp.getEventNumber();

                if (comp.getDestinationNames() != null) {
                    ArrayList<String> fNames = new ArrayList<>(Arrays.asList(comp.getDestinationNames()));
                    boolean isChanged = false;
                    for (String s : fNames) {
                        if (!_outputFile.contains(s)) {
                            isChanged = true;
                        }
                    }

                    if (isChanged) {
                        _outputFile = fNames;
                        owner.getOutputFileComboBox().removeAllItems();
                        for (String ss : fNames) {
                            owner.getOutputFileComboBox().addItem(ss);
                        }
                    }
                }

            }

            // data rate bar graph data update only at
            // active state and only for ROC data
            // 12.16.2020 added also paused state
            if (owner.isEnable_fx() &&
                    (owner._runState.equals(AConstants.active) ||
                            owner._runState.equals(AConstants.paused))
            ) {
                if (_ad.getDataRate(owner.AIV) >= 0) {

                    if (owner.DrBg != null) {

                        if (_ad.getType().equals(ACodaType.ROC.name()) ||
                                _ad.getType().equals(ACodaType.TS.name()) ||
                                _ad.getType().equals(ACodaType.GT.name())
                                ) {
                            owner.bGdata.put(_ad.getName(), _ad.getDataRate(owner.AIV));
                        }
                    }
                    if (owner.FxEvtRateTG != null) {
                        float evtr = _ad.getEvtRate(owner.AIV);
                        if (evtr <= 0.1) evtr = 0.1F;
                        owner.FxEvtRateTG.update(_ad.getName(), evtr);
                    }
                    if (owner.FxDataRateTG != null) {
                        owner.FxDataRateTG.update(_ad.getName(), _ad.getDataRate(owner.AIV));
                    }
                }
                if (_ad.getType().equals(ACodaType.DC.name()) ||
                        _ad.getType().equals(ACodaType.SEB.name()) ||
                        _ad.getType().equals(ACodaType.PEB.name()) ||
                        _ad.getType().equals(ACodaType.EB.name()) ||
                        _ad.getType().equals(ACodaType.EBER.name()) ||
                        _ad.getType().equals(ACodaType.ROC.name()) ||
                        _ad.getType().equals(ACodaType.TS.name()) ||
                        _ad.getType().equals(ACodaType.GT.name()) ||
                        _ad.getType().equals(ACodaType.ER.name())) {

                    // Input buffers
                    for (String s : comp.getInBuffers().keySet()) {
                        owner.ibData.put(s, (double) comp.getInBuffers().get(s));
                    }
                    // create input buffers bar graph if we get more
                    // reporting from different components.
                    if (owner.inBufReportSize.get() != owner.ibData.size()) createInBufferBGFx(owner.ibData);

                    // Output buffers
                    for (String s : comp.getOutBuffers().keySet()) {
                        owner.obData.put(s, (double) comp.getOutBuffers().get(s));
                    }
                    // create input buffers bar graph if we get more
                    // reporting from different components.
                    if (owner.outBufReportSize.get() != owner.obData.size()) createOutBufferBGFx(owner.obData);
                }
            }
        }
        if (owner.FxLiveTimeTG != null &&
                owner.tsComponent != null && owner._runState.equals(AConstants.active)) {
            owner.FxLiveTimeTG.update(owner.tsComponent.getName(), owner.tsComponent.getLiveTime());
        }
    }

    /**
     * <p>
     * Updates individual component data array
     * </p>
     *
     * @param comp AComponent object
     */

    private void updateComponentIndividualData(AComponent comp) {
        if (comp.getName().equals(owner.selectedComponent)) {
            // update individual component data
            _id[0] = comp.getName();
            _id[1] = comp.getType();
            _id[2] = comp.getObjectType();
            _id[3] = comp.getState();
            _id[4] = Long.toString(comp.getEventNumber());
            _id[5] = owner.formatter.format(comp.getEventRate());
            _id[6] = owner.formatter.format(comp.getDataRate());
            _id[7] = owner.formatter.format(comp.getNumberOfLongs());
            _id[8] = owner.formatter.format(comp.getLiveTime());
            if (comp.getDestinationNames() != null && comp.getDestinationNames().length > 0) {
                StringBuilder sb = new StringBuilder();
                for (String s : comp.getDestinationNames()) {
                    sb.append(s).append(" ;");
                }
                sb.deleteCharAt(sb.length() - 1);
                _id[9] = sb.toString();
            } else {
                _id[9] = "";
            }
            _id[10] = Integer.toString(comp.getMinEventSize());
            _id[11] = Integer.toString(comp.getMaxEventSize());
            _id[12] = Integer.toString(comp.getAvgEventSize());
            _id[13] = Integer.toString(comp.getMinTimeToBuild());
            _id[14] = Integer.toString(comp.getMaxTimeToBuild());
            _id[15] = Integer.toString(comp.getMeanTimeToBuild());
            _id[16] = Integer.toString(comp.getChunkXEtBuffer());

        }
    }

    /**
     * <p>
     * Updates component specific array
     * with information that is going to
     * be used during the table visualization
     * </p>
     *
     * @param comp AComponent object
     */
    private void updateTableTreeData(AComponent comp) {
        String[] _d = new String[11];
        // create data for the row in the component table
        _d[0] = comp.getName();
        _d[1] = comp.getState();
        _d[2] = owner.formatter.format(comp.getEventRate());
        _d[3] = owner.formatter.format(comp.getDataRate());
        _d[4] = owner.formatter.format(comp.getEventRateAverage());
        _d[5] = owner.formatter.format(comp.getDataRateAverage());
        // update data for component tree
        _d[6] = comp.getExpid();
        _d[7] = comp.getSession();
        _d[8] = comp.getRunType();
        _d[9] = comp.getType();
        _d[10] = comp.getName();

        try {
            _data_m.put(comp.getName(), _d);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Swing worker inner class
     */
    private class AAction extends SwingWorker<Integer, Void> {

        private cMsgMessage msgR;

        AAction(cMsgMessage messageReceived) {
            this.msgR = messageReceived;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            return bgProcessing(msgR);
        }

        @Override
        protected void done() {
            super.done();
            try {
                visualization(get());
            } catch (InterruptedException | ExecutionException e) {
                if (AConstants.debug.get()) e.printStackTrace();
            }
        }
    }


    private class PeriodicEx extends ATimerExecutor {

        int count = 0;

        PeriodicEx(int period) {
            super(period);
        }

        @Override
        public void doSomeWork() {
            if (deltaSupReportTime.get() > 60000) { // warning messages every 60sec.
                count++;
            } else {
                count = 0;
            }
            if (count > 30) { // 4 minute is elapsed
                // ping supervisor agent to see if it is up and can end the run
                try {
                    cMsgMessage msgB = owner.base.p2pSend(owner.base.getPlEXPID(),
                            AConstants.SupervisorControlRequestReportReady,
                            "areYouHome", 10000);
                    if (msgB != null && msgB.getText().equals("IamAlive")) {
                        owner.updateDaLogTable(owner.getName(), "You can end the run if you choose to do so.", AConstants.WARN, 7);
                        //reset
                        count = 0;
                    } else {
                        owner.updateDaLogTable(owner.getName(), "Lost connection to the Supervisor", AConstants.ERROR, 11);
                        new RcDisconnect(owner).execute();
                        atx.stop();
                    }
                } catch (AException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}
