package ie.omk.smpp.message;

/**
 * Generic negative acknowledgment. Used if the short message entity, either
 * ESME or SMSC, does not understand a message transmitted to it or if a
 * transmitted protocol message is badly formed.
 * 
 * @version $Id$
 */
public class GenericNack extends SMPPPacket {
    private static final long serialVersionUID = 1L;

    /**
     * Construct a new GenericNack.
     */
    public GenericNack() {
        super(GENERIC_NACK);
    }

    public GenericNack(SMPPPacket request) {
        super(request);
    }
}

