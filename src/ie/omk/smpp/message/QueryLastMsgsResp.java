/*
 * Java SMPP API
 * Copyright (C) 1998 - 2001 by Oran Kelly
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
 * Java SMPP API author: oran.kelly@ireland.com
 * Java SMPP API Homepage: http://smppapi.sourceforge.net/
 */
package ie.omk.smpp.message;

import java.io.*;
import java.util.*;
import ie.omk.smpp.SMPPException;
import ie.omk.smpp.util.SMPPIO;
import ie.omk.debug.Debug;

/** SMSC response to a QueryLastMsgs request.
  * @author Oran Kelly
  * @version 1.0
  */
public class QueryLastMsgsResp
    extends ie.omk.smpp.message.SMPPResponse
{
    /** The table of messages returned */
    private Vector messageTable;

    /** Construct a new QueryLastMsgsResp with specified sequence number.
      * @param seqNum The sequence number to use
      */
    public QueryLastMsgsResp(int seqNum)
    {
	super(ESME_QUERY_LAST_MSGS_RESP, seqNum);
	messageTable = null;
    }

    /** Read in a QueryLastMsgsResp from an InputStream.  A full packet,
      * including the header fields must exist in the stream.
      * @param in The InputStream to read from
      * @exception java.io.IOException if there's an error reading from the
      * input stream.
      */
    public QueryLastMsgsResp(InputStream in)
	throws java.io.IOException, ie.omk.smpp.SMPPException
    {
	super(in);

	if(commandStatus != 0)
	    return;

	int msgCount = 0;
	long id = 0;
	msgCount = SMPPIO.readInt(in, 1);

	messageTable = new Vector(msgCount);

	for(int loop = 0; loop < msgCount; loop++) {
	    String s = SMPPIO.readCString(in);
	    try {
		id = Long.parseLong(s, 16);
		messageTable.addElement(new Integer((int)id));
		Debug.d(this, "<init>", "Adding " + id
			+ " to destinations", Debug.DBG_3);
	    } catch(NumberFormatException nx) {
		/* Just don't add it to the table! */
		Debug.d(this, "<init>", "Not added: " + id, Debug.DBG_2);
	    }
	}
    }

    /** Create a new QueryLastMsgsResp packet in response to a BindReceiver.
      * This constructor will set the sequence number to it's expected value.
      * @param r The Request packet the response is to
      */
    public QueryLastMsgsResp(QueryLastMsgs r)
    {
	super(r);
    }

    /** Add a message Id to the message table.
      * @param id The message Id to add to the table (0h - ffffffffh)
      * @return The current number of message Ids in the message table
      * @exception ie.omk.smpp.SMPPException If the message Id is invalid
      */
    public int addMessageId(int id)
	throws ie.omk.smpp.SMPPException
    {
	if(messageTable == null)
	    messageTable = new Vector();

	String s = Integer.toHexString(id);
	if(s.length() > 8)
	    throw new SMPPException("Message Id must be < 9 chars");

	messageTable.addElement(new Integer(id));
	return (messageTable.size());
    }

    /** Get the number of messages in the message table */
    public int getMsgCount()
    {
	return (messageTable != null) ? messageTable.size() : 0;
    }

    /** Get an array of the message Ids.
      * @return An int array of all the message Ids
      */
    public int[] getMessageIds()
    {
	int ids[];
	int loop;

	if(messageTable == null || messageTable.size() == 0)
	    return null;

	ids = new int[messageTable.size()];
	for(loop=0; loop<messageTable.size(); loop++)
	    ids[loop] = ((Integer)messageTable.elementAt(loop)).intValue();

	return ids;
    }

    /** Return the number of bytes this packet would be encoded as to an
      * OutputStream.
      */
    public int getCommandLen()
    {
	int id;
	String s = null;

	// 1 1-byte integer!
	int size = getHeaderLen() + 1;
	Enumeration e = messageTable.elements();
	while(e.hasMoreElements()) {
	    id = ((Integer)e.nextElement()).intValue();
	    s = Integer.toHexString(id);

	    // String length plus nul-terminator..
	    size += s.length() + 1;
	}
	return (size);
    }

    /** Write a byte representation of this packet to an OutputStream
      * @param out The OutputStream to write to
      * @exception java.io.IOException if there's an error writing to the
      * output stream.
      */
    protected void encodeBody(OutputStream out)
	throws java.io.IOException, ie.omk.smpp.SMPPException
    {
	String s = null;
	SMPPIO.writeInt(messageTable.size(), 1, out);
	Enumeration e = messageTable.elements();
	while(e.hasMoreElements()) {
	    s = Integer.toHexString(((Integer)e.nextElement()).intValue());
	    SMPPIO.writeCString(s, out);
	}
    }

    /** Convert this packet to a String. Not to be interpreted programmatically,
      * it's just dead handy for debugging!
      */
    public String toString()
    {
	return new String("query_last_msgs_resp");
    }
}