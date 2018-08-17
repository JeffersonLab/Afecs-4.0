package org.jlab.coda.afecs.container;

/**
 * Client platform join request packet structure
 * <p>
 *
 * @author gurjyan
 *         Date 11/8/16
 * @version 3.x
 */
public class ClientJoinRequestPacket {
    private int packetCount;
    private int senderId;

    public ClientJoinRequestPacket(int id, int packetNumber){
        setSenderId(id);
        setPacketCount(packetNumber);
    }

    public int getPacketCount() {
        return packetCount;
    }

    public void setPacketCount(int packetCount) {
        this.packetCount = packetCount;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
}
