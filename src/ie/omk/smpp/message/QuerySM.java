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

import java.io.IOException;
import java.io.OutputStream;

import ie.omk.smpp.Address;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.SMPPException;

import ie.omk.smpp.util.GSMConstants;
import ie.omk.smpp.util.SMPPIO;

import org.apache.log4j.Logger;

/** Query the state of a message.
  * Relevant inherited fields from SMPPPacket:<br>
  * <ul>
  *   messageId<br>
  *   source<br>
  * </ul>
  * @author Oran Kelly
  * @version 1.0
  */
public class QuerySM
    extends ie.omk.smpp.message.SMPPRequest
{
    /** Construct a new QuerySM.
      */
    public QuerySM()
    {
	super(QUERY_SM);
    }

    /** Construct a new QuerySM with specified sequence number.
      * @param seqNum The sequence number to use
      * @deprecated
      */
    public QuerySM(int seqNum)
    {
	super(QUERY_SM, seqNum);
    }

    public int getBodyLength()
    {
	int len = (((messageId != null) ? messageId.length() : 0)
		+ ((source != null) ? source.getLength() : 3));

	// 1 c-string
	return (len + 1);
    }

    /** Write a byte representation of this packet to an OutputStream
      * @param out The OutputStream to write to
      * @throws java.io.IOException if there's an error writing to the
      * output stream.
      */
    protected void encodeBody(OutputStream out)
	throws java.io.IOException
    {
	SMPPIO.writeCString(getMessageId(), out);
	if(source != null) {
	    source.writeTo(out);
	} else {
	    // Write ton=0(null), npi=0(null), address=\0(nul)
	    new Address(GSMConstants.GSM_TON_UNKNOWN,
		    GSMConstants.GSM_NPI_UNKNOWN, "").writeTo(out);
	}
    }

    public void readBodyFrom(byte[] body, int offset) throws SMPPProtocolException {
	messageId = SMPPIO.readCString(body, offset);
	offset += messageId.length() + 1;

	source = new Address();
	source.readFrom(body, offset);
    }

    /** Convert this packet to a String. Not to be interpreted programmatically,
      * it's just dead handy for debugging!
      */
    public String toString()
    {
	return new String("query_sm");
    }
}