package org.jlab.coda.afecs.platform.thread;

import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.cMsg.cMsg;

/**
 * Platform cMsg server ping.
 * <p>
 *
 * @author gurjyan
 *         Date 11/10/16
 * @version 3.x
 */
public class cMsgPingT extends Thread {

    private cMsg connection;

    public cMsgPingT(cMsg connection){
        this.connection = connection;
    }
    @Override
    public void run() {

        if(! connection.isConnected()){
            System.out.println("Sever-Error: ==============  Platform cMsg server is not responding. ");
        }
        AfecsTool.sleep(10000);

    }
}
