package ie.omk.smpp.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of an Smsc link over the tcp/ip protocol
 * 
 * @author Oran Kelly
 * @version $Id$
 */
public class TcpLink extends ie.omk.smpp.net.SmscLink {
    private static final String STACK_TRACE_ERR = "Stack trace:";

    private static final String SOCKET_NOT_OPEN_ERR = "Socket connection is not open";

    private static final Log LOGGER = LogFactory.getLog(TcpLink.class);

    /** Default IP port to use if none are specified */
    public static final int DEFAULT_PORT = 5016;

    /** The ip address of the SMSC */
    private InetAddress addr = null;

    /** The ip port to connect to */
    private int port = 0;

    /** The socket corresponding to the virtual connection */
    private Socket sock = null;

    /** Are we connected? */
    private boolean connected = false;

    /**
     * Create a new TcpLink
     * 
     * @param address
     *            IP address or hostname of SMSC
     * @throws java.net.UnknownHostException
     *             If the host is not found.
     */
    public TcpLink(String address) throws java.net.UnknownHostException {
        this(address, DEFAULT_PORT);
    }

    /**
     * Create a new TcpLink
     * 
     * @param address
     *            IP address or hostname of SMSC
     * @param port
     *            The port number to connect to
     * @throws java.net.UnknownHostException
     *             If the host is not found.
     */
    public TcpLink(String address, int port)
            throws java.net.UnknownHostException {
        this.addr = InetAddress.getByName(address);
        if (port < 1) {
            this.port = DEFAULT_PORT;
        } else {
            this.port = port;
        }
    }

    /**
     * Create a new TcpLink
     * 
     * @param address
     *            IP address SMSC
     * @throws java.net.UnknownHostException
     *             If the host is not found.
     */
    public TcpLink(InetAddress address) throws java.net.UnknownHostException {
        this(address, DEFAULT_PORT);
    }

    /**
     * Create a new TcpLink
     * 
     * @param address
     *            IP address of SMSC
     * @param port
     *            The port number to connect to
     * @throws java.net.UnknownHostException
     *             If the host is not found.
     */
    public TcpLink(InetAddress address, int port)
            throws java.net.UnknownHostException {
        this.addr = address;
        if (port < 1) {
            this.port = DEFAULT_PORT;
        } else {
            this.port = port;
        }
    }

    /**
     * Create a new Socket connection to the SMSC. This implementation creates a
     * new instance of a java.net.Socket with the host name and port supplied to
     * the constructor an instance of this class was created with.
     * 
     * @throws java.io.IOException
     *             If an error occurs while creating the socket connection to
     *             the SMSC.
     * @see java.net.Socket#Socket(java.net.InetAddress, int)
     */
    protected void implOpen() throws java.io.IOException {
        LOGGER.info("Opening TCP socket to " + addr + ":" + port);
        sock = new Socket(addr, port);
        connected = true;
    }

    /**
     * Close the Socket connection to the SMSC.
     * 
     * @throws java.io.IOException
     *             If an I/O error occurs closing the socket connection.
     * @see java.net.Socket#close
     */
    protected void implClose() throws java.io.IOException {
        if (connected && sock != null) {
            LOGGER.info("Shutting down socket connection");
            try {
                sock.close();
                sock = null;
                connected = false;
            } catch (IOException ix) {
                LOGGER.warn("I/O exception closing socket", ix);
                connected = false;
                ix.fillInStackTrace();
                throw ix;
            }
        }
    }

    /**
     * Get the output stream of the Socket connection to the SMSC.
     * 
     * @throws java.io.IOException
     *             If the socket connection is not open or an I/O error occurs
     *             when creating the output stream.
     * @see java.io.OutputStream
     * @see java.net.Socket#getOutputStream
     */
    protected OutputStream getOutputStream() throws java.io.IOException {
        if (sock == null) {
            throw new IOException(SOCKET_NOT_OPEN_ERR);
        } else {
            return sock.getOutputStream();
        }
    }

    /**
     * Get the input stream of the Socket connection to the SMSC.
     * 
     * @throws java.io.IOException
     *             If the socket connection is not open or an I/O error occurs
     *             when creating the input stream.
     * @see java.io.InputStream
     * @see java.net.Socket#getInputStream
     */
    protected InputStream getInputStream() throws java.io.IOException {
        if (sock == null) {
            throw new IOException(SOCKET_NOT_OPEN_ERR);
        } else {
            return sock.getInputStream();
        }
    }

    /**
     * Get the address we're connected (or connecting) to.
     * 
     * @return The address of the SMSC this link is connected to.
     */
    public InetAddress getAddress() {
        return addr;
    }

    /**
     * Get the service port to connect to at the SMSC to establish a TCP
     * connection.
     * 
     * @return The service port at the SMSC to connect to.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the port at the SMSC that this link is connected to. This is the
     * remote port that this link is connected to after a successful connection
     * has been made.
     * 
     * @return The remote port this link is connected to.
     * @throws java.io.IOException
     *             If the connection is not open.
     */
    public int getConnectedPort() throws java.io.IOException {
        if (sock == null) {
            throw new IOException(SOCKET_NOT_OPEN_ERR);
        } else {
            return sock.getPort();
        }
    }

    /**
     * Get the local port number this link is connected to.
     * 
     * @return The local port number this link is connected to.
     * @throws java.io.IOException
     *             If the connection is not open.
     */
    public int getLocalPort() throws java.io.IOException {
        if (sock == null) {
            throw new IOException(SOCKET_NOT_OPEN_ERR);
        } else {
            return sock.getLocalPort();
        }
    }

    /**
     * Check connection status.
     * 
     * @return false if unconnected, true if connected.
     */
    public boolean isConnected() {
        return connected;
    }

    public void setTimeout(long timeout) {
        try {
            sock.setSoTimeout((int) timeout);
        } catch (Throwable t) {
            LOGGER.error("Failed to set timeout on socket: " + t.getMessage());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(STACK_TRACE_ERR, t);
            }
        }
    }

    public long getTimeout() {
        try {
            return (long) sock.getSoTimeout();
        } catch (Throwable t) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(STACK_TRACE_ERR, t);
            }
        }

        return -1L;
    }
}