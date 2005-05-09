/*
 * Java SMPP API
 * Copyright (C) 1998 - 2002 by Oran Kelly
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * A copy of the LGPL can be viewed at http://www.gnu.org/copyleft/lesser.html
 * Java SMPP API author: orank@users.sf.net
 * Java SMPP API Homepage: http://smppapi.sourceforge.net/
 * $Id$
 */
package ie.omk.smpp.message;

import ie.omk.smpp.util.InvalidDateFormatException;
import ie.omk.smpp.util.SMPPDate;
import ie.omk.smpp.util.SMPPIO;

import java.io.OutputStream;

/**
 * SMSC response to a QuerySM request. Contains the current state of a short
 * message at the SMSC.
 * 
 * @author Oran Kelly
 * @version 1.0
 */
public class QuerySMResp extends ie.omk.smpp.message.SMPPResponse {
    /**
     * Construct a new QuerySMResp.
     */
    public QuerySMResp() {
        super(QUERY_SM_RESP);
    }

    /**
     * Construct a new QuerySMResp with specified sequence number.
     * 
     * @param seqNum
     *            The sequence number to use
     * @deprecated
     */
    public QuerySMResp(int seqNum) {
        super(QUERY_SM_RESP, seqNum);
    }

    /**
     * Create a new QuerySMResp packet in response to a BindReceiver. This
     * constructor will set the sequence number to it's expected value.
     * 
     * @param r
     *            The Request packet the response is to
     */
    public QuerySMResp(QuerySM r) {
        super(r);

        messageId = r.getMessageId();
        finalDate = null;
        messageStatus = 0;
        errorCode = 0;
    }

    /**
     * Return the number of bytes this packet would be encoded as to an
     * OutputStream.
     * 
     * @return the number of bytes this packet would encode as.
     */
    public int getBodyLength() {
        int len = (((messageId != null) ? messageId.length() : 0) + ((finalDate != null) ? finalDate
                .toString().length()
                : 0));

        // 2 1-byte integers, 2 c-strings
        return (len + 2 + 2);
    }

    /**
     * Write a byte representation of this packet to an OutputStream
     * 
     * @param out
     *            The OutputStream to write to
     * @throws java.io.IOException
     *             if there's an error writing to the output stream.
     */
    protected void encodeBody(OutputStream out) throws java.io.IOException {
        String fdate = (finalDate == null) ? null : finalDate.toString();
        SMPPIO.writeCString(getMessageId(), out);
        SMPPIO.writeCString(fdate, out);
        SMPPIO.writeInt(messageStatus, 1, out);
        SMPPIO.writeInt(errorCode, 1, out);
    }

    public void readBodyFrom(byte[] body, int offset)
            throws SMPPProtocolException {
        try {
            messageId = SMPPIO.readCString(body, offset);
            offset += messageId.length() + 1;

            String finald = SMPPIO.readCString(body, offset);
            offset += finald.length() + 1;
            if (finald.length() > 0)
                finalDate = SMPPDate.parseSMPPDate(finald);

            messageStatus = SMPPIO.bytesToInt(body, offset++, 1);
            errorCode = SMPPIO.bytesToInt(body, offset++, 1);
        } catch (InvalidDateFormatException x) {
            throw new SMPPProtocolException("Unrecognized date format", x);
        }
    }

    /**
     * Convert this packet to a String. Not to be interpreted programmatically,
     * it's just dead handy for debugging!
     */
    public String toString() {
        return new String("query_sm_resp");
    }
}