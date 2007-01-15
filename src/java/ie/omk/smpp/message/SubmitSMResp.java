package ie.omk.smpp.message;

import java.util.List;


/**
 * Submit short message response.
 * 
 * @author Oran Kelly
 * @version 1.0
 */
public class SubmitSMResp extends SMPPPacket {
    private String messageId;
    
    /**
     * Construct a new SubmitSMResp.
     */
    public SubmitSMResp() {
        super(SUBMIT_SM_RESP);
    }

    /**
     * Create a new SubmitSMResp packet in response to a SubmitSM. This
     * constructor will set the sequence number to it's expected value.
     * 
     * @param request
     *            The Request packet the response is to
     */
    public SubmitSMResp(SMPPPacket request) {
        super(request);
    }

    /**
     * Convert this packet to a String. Not to be interpreted programmatically,
     * it's just dead handy for debugging!
     */
    public String toString() {
        return new String("submit_sm_resp");
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    @Override
    protected BodyDescriptor getBodyDescriptor() {
        return BodyDescriptor.ONE_CSTRING;
    }

    @Override
    protected Object[] getMandatoryParameters() {
        return new Object[] {
                messageId,
        };
    }
    
    @Override
    protected void setMandatoryParameters(List<Object> params) {
        messageId = (String) params.get(0);
    }
}