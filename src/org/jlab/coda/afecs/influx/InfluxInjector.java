package org.jlab.coda.afecs.influx;

import org.influxdb.dto.Point;
import org.jlab.coda.afecs.supervisor.SupervisorAgent;
import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.*;
import org.jlab.coda.jinflux.JinFluxException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Influx DB injector class
 *
 * @author gurjyan on 3/9/18.
 */
public class InfluxInjector extends ABase {

    // JinFluxDriver object
    public JinFluxDriver jinFluxDriver;

    private static final String name   = "afecswebmon";
    private static final String dbNode = "claraweb.jlab.org";
    private static final String dbName = "afecs";

    public InfluxInjector(String name, String dbNode, String dbName, boolean isLocal) throws JinFluxException {
        super();
        myName = name+"_"+myConfig.getPlatformExpid();

        // connect to the influxDB and create JinFlux connection
        jinFluxDriver = new JinFluxDriver(dbNode, dbName, null);

        // Connect to the platform cMsg domain server
        if(isLocal){
            // Connect to the platform cMsg domain server
            try {
                myPlatformConnection = platformConnect("cMsg://localhost" +
                        ":" + myConfig.getPlatformTcpPort() +
                        "/cMsg/" + myConfig.getPlatformName() + "?cmsgpassword=" + myConfig.getPlatformExpid());
            } catch (cMsgException e) {
                e.printStackTrace();
            }
        } else {
            remoteConnect();
        }
        if (isPlatformConnected()) {
            // Subscribe messages asking to inject data into influxDB
            try {
                myPlatformConnection.subscribe(myName,
                        AConstants.InfluxDBInjectRequest,
                        new InjectRequestCB(),
                        null);
            } catch (cMsgException e) {
                e.printStackTrace();
            }
        }
    }

    public InfluxInjector(boolean isLocal) throws JinFluxException {
        this(name, dbNode, dbName, isLocal);
    }

    private void remoteConnect() {
        String plHost;
        if (!isPlatformConnected()) {
            // Connect to the rc domain multicast
            // server and request platform host name
            cMsgMessage m = null;
            for (int i = 0; i < 100; i++) {
                m = rcMonitor(300);
                if (m != null) break;
            }
            if (m != null) {
                plHost = m.getSenderHost();
                cMsgPayloadItem item = m.getPayloadItem("IpAddresses");
                try {
                    String[] plHosts = item.getStringArray();
                    // connect with the hostname
                    System.out.println("Info: Please wait... connecting to the platform host = " + plHost);
                    String UIMulticastUDL = updateHostUdl(plHost, myConfig.getPlatformTcpPort());
                    try {
                        myPlatformConnection = platformConnect(UIMulticastUDL);
                    } catch (cMsgException e) {
                        System.out.println("Failed to connect to IP address = " + plHost);
                    }
                    if (!isPlatformConnected()) {

                        // update platform udl and connect
                        if (plHosts.length > 0) {
                            for (String ph : plHosts) {
                                System.out.println("Info: Please wait... connecting to the platform host = " + ph);
                                UIMulticastUDL = updateHostUdl(ph, myConfig.getPlatformTcpPort());
                                try {
                                    myPlatformConnection = platformConnect(UIMulticastUDL);
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
                    System.exit(1);
                }
                if (!isPlatformConnected()) {
                    System.out.println(" Can not connect to the " + getPlEXPID() + " platform.");
                    System.exit(1);
                } else {
                    System.out.println("Info:Connected to the " + getPlEXPID() + " platform.");
                }
            } else {
                System.out.println(" Can not find a platform for EXPID = " + getPlEXPID());
                System.exit(1);
            }
        }
    }

    /**
     * Method that injects user defined message into influxDB
     *
     * @param msg uer defined message
     */
    private void userRequestJinFluxInject(cMsgMessage msg) {
        String user_table = msg.getText();

        if (jinFluxDriver.isConnected()) {
            Map<String, String> tags = new HashMap<>();
            try {
                // adding tags
                for (String pI_name : msg.getPayloadItems().keySet()) {
                    if (pI_name.startsWith("tag")) {
                        tags.put(pI_name, msg.getPayloadItem(pI_name).getString());
                    }
                }
                Point.Builder p = jinFluxDriver.openTB(user_table, tags);

                // adding points
                for (String pI_name : msg.getPayloadItems().keySet()) {
                    if (!pI_name.startsWith("tag")) {

                        switch (msg.getPayloadItem(pI_name).getType()) {
                            case cMsgConstants.payloadInt8:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getByte());
                                break;
                            case cMsgConstants.payloadInt16:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getShort());
                                break;
                            case cMsgConstants.payloadInt32:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getInt());
                                break;
                            case cMsgConstants.payloadInt64:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getLong());
                                break;
                            case cMsgConstants.payloadUint8:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getByte());
                                break;
                            case cMsgConstants.payloadUint16:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getShort());
                                break;
                            case cMsgConstants.payloadUint32:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getInt());
                                break;
                            case cMsgConstants.payloadUint64:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getLong());
                                break;
                            case cMsgConstants.payloadDbl:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getDouble());
                                break;
                            case cMsgConstants.payloadFlt:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getFloat());
                                break;
                            case cMsgConstants.payloadStr:
                                jinFluxDriver.addDP(p, pI_name, msg.getPayloadItem(pI_name).getString());
                                break;
                        }
                    }
                }
            } catch (cMsgException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>
     * Private inner class for responding
     * to the platform info request messages
     * </p>
     */
    private class InjectRequestCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {

                String type = msg.getType();
                switch (type) {
                    case AConstants.InfluxDBInjectRequestUserData:
                        userRequestJinFluxInject(msg);
                        break;
                    case AConstants.InfluxDBInjectRequestPlatformData:
                        try {
                            if(msg.getByteArray()!=null) {
                                SupervisorAgent ac = (SupervisorAgent) AfecsTool.B2O(msg.getByteArray());
                                if (ac != null) {
                                    System.out.println("DDD -----| Info: push data to InfluxDB");
                                    jinFluxDriver.push(ac);
                                }
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            new InfluxInjector(false);
        } catch (JinFluxException e) {
            e.printStackTrace();
        }
    }
}
