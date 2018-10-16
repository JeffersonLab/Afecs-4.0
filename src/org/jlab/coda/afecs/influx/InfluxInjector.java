package org.jlab.coda.afecs.influx;

import org.influxdb.dto.Point;
import org.jlab.coda.afecs.platform.APlatform;
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
public class InfluxInjector {

    // JinFluxDriver object
    public JinFluxDriver jinFluxDriver;

    private static final String name = "afecswebmon";
    private static final String dbNode = "claraweb.jlab.org";
    private static final String dbName = "afecs";
    private SupervisorAgent owner;

    public InfluxInjector(SupervisorAgent sup, String dbNode, String dbName) throws JinFluxException {
        super();
        owner = sup;

        // connect to the influxDB and create JinFlux connection
        jinFluxDriver = new JinFluxDriver(dbNode, dbName, null);


        do {
            if (owner.myPlatform.influxDb) {
                // request influx-db injection
                if (owner.me.getState().equals(AConstants.active) &&
                        !owner.me.getRunStartTime().equals("0")) {
                    jinFluxDriver.push(owner);
                }
            }
            AfecsTool.sleep(5000);
        } while (true);

    }

    public InfluxInjector(SupervisorAgent sup, boolean isLocal) throws JinFluxException {
        this(sup, dbNode, dbName);
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

}
