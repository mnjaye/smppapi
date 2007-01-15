package ie.omk.smpp.util;

import ie.omk.smpp.Connection;
import ie.omk.smpp.ConnectionType;
import ie.omk.smpp.event.ConnectionObserver;
import ie.omk.smpp.event.SMPPEvent;
import ie.omk.smpp.message.Bind;
import ie.omk.smpp.message.BindReceiver;
import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.BindTransceiver;
import ie.omk.smpp.message.BindTransmitter;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.Unbind;
import ie.omk.smpp.message.UnbindResp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection observer which mimics synchronous communications. This observer
 * provides methods which block the caller until the desired response packet
 * is available to be returned.
 * @version $Id:$
 */
public class SyncWrapper implements ConnectionObserver {
    private static final Logger LOG = LoggerFactory.getLogger(SyncWrapper.class);
    
    private Connection connection;
    private Map<Integer, SMPPPacket> blockers = new HashMap<Integer, SMPPPacket>();
    private List<SMPPPacket> packetQueue = new ArrayList<SMPPPacket>();
    private long packetTimeout;
    
    private ConnectionCaller bindCaller = new ConnectionCaller() {
        public void execute(Connection connection, SMPPPacket packet) throws IOException {
            connection.bind((Bind) packet);
        }
    };
    private ConnectionCaller packetCaller = new ConnectionCaller() {
        public void execute(Connection connection, SMPPPacket packet) throws IOException {
            connection.sendPacket(packet);
        }
    };
    private ConnectionCaller unbindCaller = new ConnectionCaller() {
        public void execute(Connection connection, SMPPPacket packet) throws IOException {
            connection.unbind((Unbind) packet);
        }
    };

    public SyncWrapper(Connection connection) {
        this.connection = connection;
    }

    public BindResp bind(ConnectionType type,
            String systemID,
            String password,
            String systemType) throws IOException {
        return bind(type, systemID, password, systemType, 0, 0, null);
    }
    
    public BindResp bind(ConnectionType type,
            String systemID,
            String password,
            String systemType,
            int typeOfNumber,
            int numberPlanIndicator,
            String addressRange) throws IOException {
        Bind bindRequest;
        if (type == ConnectionType.TRANSMITTER) {
            bindRequest = new BindTransmitter();
        } else if (type == ConnectionType.RECEIVER) {
            bindRequest = new BindReceiver();
        } else {
            bindRequest = new BindTransceiver();
        }
        bindRequest.setVersion(connection.getVersion());
        bindRequest.setSystemId(systemID);
        bindRequest.setPassword(password);
        bindRequest.setSystemType(systemType);
        bindRequest.setAddressTon(typeOfNumber);
        bindRequest.setAddressNpi(numberPlanIndicator);
        bindRequest.setAddressRange(addressRange);
        return bind(bindRequest);
    }
    
    public BindResp bind(Bind bindRequest) throws IOException {
        long timeout = getBindTimeout();
        return (BindResp) sendAndWait(bindRequest, bindCaller, timeout);
    }

    public UnbindResp unbind() throws IOException {
        return (UnbindResp) sendAndWait(new Unbind(), unbindCaller, packetTimeout);
    }
    
    /**
     * Send a packet to the SMSC and wait for its response.
     * @param packet The request packet to send to the SMSC.
     * @return The response packet received from the SMSC. This may be
     * <code>null</code> if the call timed out waiting on the packet, or if
     * the thread was instructed to give up the wait.
     * @throws IOException If there was a problem writing to, or reading
     * from, the connection.
     */
    public SMPPPacket sendPacket(SMPPPacket packet) throws IOException {
        return sendAndWait(packet, packetCaller, packetTimeout);
    }
    
    public void packetReceived(Connection source, SMPPPacket packet) {
        if (packet.isResponse()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Response received: there are {} threads blocked.",
                        blockers.size());
            }
            Integer seq = null;
            synchronized (blockers) {
                seq = new Integer(packet.getSequenceNum());
                seq = getKeyObject(seq);
                if (seq != null) {
                    blockers.put(seq, packet);
                    synchronized (seq) {
                        seq.notify();
                    }
                } else {
                    LOG.warn("No blocker thread waiting on packet {}", seq);
                    addToQueue(packet);
                }
            }
        } else {
            addToQueue(packet);
        }
    }

    public void update(Connection source, SMPPEvent event) {
    }

    /**
     * Check if there are any packets available to be read.
     * @return <code>true</code> if {@link #readNextPacket} will return a
     * packet without blocking, <code>false</code> if it would block waiting
     * on a packet.
     */
    public boolean isPacketAvailable() {
        return packetQueue.size() > 0;
    }

    /**
     * Read the next packet from the connection.
     * @param block <code>true</code> to block waiting on the next packet
     * to arrive, <code>false</code> to return whether or not a packet was
     * available. Note that calling this method will not return a response
     * packet to the calling thread if there is already another thread blocked
     * waiting on that response. That is, if thread 1 called <code>sendPacket
     * </code> and is waiting on a response and then thread 2 calls this method,
     * when the response packet arrives, thread 1 will be given the response
     * and thread 2 will continue to block in this method until the <b>next</b>
     * packet arrives. However, if thread 1 stops waiting for some reason
     * (for example, it times out waiting for its response), then thread 2
     * will receive the response packet.
     * @return The next packet from the SMSC, or <code>null</code> if no
     * packet was available and the caller requested non-blocking operation.
     * @throws IOException If there was a problem communicating with the 
     * connection.
     */
    public SMPPPacket readNextPacket(boolean block) throws IOException {
        SMPPPacket packet = null;
        try {
            synchronized (packetQueue) {
                if (packetQueue.size() < 1 && block) {
                    packetQueue.wait();
                } else {
                    packet = packetQueue.remove(0);
                }
            }
        } catch (InterruptedException x) {
            LOG.info("Thread interrupted while blocked waiting on a packet.");
        }
        return packet;
    }
    
    /**
     * Get the current packet timeout setting.
     * @return The packet timeout setting.
     * @see #setPacketTimeout(long)
     */
    public long getPacketTimeout() {
        return packetTimeout;
    }

    /**
     * Set the timeout, in milliseconds, to block waiting for a packet. The
     * default is <code>0</code>, meaning wait forever for the packet.
     * @param packetTimeout
     */
    public void setPacketTimeout(long packetTimeout) {
        this.packetTimeout = packetTimeout;
    }

    /**
     * Notify any threads that are currently blocked waiting on a response
     * packet to give up waiting on the response and return. 
     */
    public void interruptAllBlocked() {
        synchronized (blockers) {
            for (Iterator<Integer> iter = blockers.keySet().iterator(); iter.hasNext();) {
                Integer seq = iter.next();
                synchronized (seq) {
                    seq.notify();
                }
            }
        }
    }
    
    private Integer getKeyObject(Integer seq) {
        for (Iterator<Integer> iter = blockers.keySet().iterator(); iter.hasNext();) {
            Integer integer = iter.next();
            if (integer.equals(seq)) {
                return integer;
            }
        }
        return null;
    }
    
    private void addToQueue(SMPPPacket packet) {
        synchronized (packetQueue) {
            packetQueue.add(packet);
            packetQueue.notify();
        }
    }
    
    private long getBindTimeout() {
        return APIConfig.getInstance().getLong(APIConfig.BIND_TIMEOUT, 0L);
    }
    
    private SMPPPacket sendAndWait(SMPPPacket packet,
            ConnectionCaller caller,
            long timeout) throws IOException {
        Integer seq;
        if (packet.getSequenceNum() < 0) {
            int nextSeq = connection.getSequenceNumberScheme().nextNumber();
            seq = new Integer(nextSeq);
        } else {
            seq = new Integer(packet.getSequenceNum());
        }
        synchronized (blockers) {
            if (blockers.containsKey(seq)) {
                throw new IllegalStateException("Got a duplicate sequence number!");
            }
            blockers.put(seq, null);
        }
        SMPPPacket response = null;
        try {
            synchronized (seq) {
                caller.execute(connection, packet);
                seq.wait(timeout);
            }
        } catch (InterruptedException x) {
            LOG.debug("Thread interrupted while waiting on response packet {}.",
                    seq);
        } finally {
            // Done in finally because the sequence number should be removed
            // from the map whether or not a response was received.
            synchronized (blockers) {
                response = blockers.remove(seq);
            }
        }
        return response;
    }
    
    private interface ConnectionCaller {
        void execute(Connection connection, SMPPPacket packet) throws IOException;
    }
}