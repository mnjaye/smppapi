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
import java.io.InputStream;

import java.net.SocketException;

import ie.omk.smpp.Address;
import ie.omk.smpp.SMPPException;
import ie.omk.smpp.InvalidMessageIDException;
import ie.omk.smpp.StringTooLongException;

import ie.omk.smpp.message.tlv.Tag;
import ie.omk.smpp.message.tlv.TLVTable;

import ie.omk.smpp.util.AlphabetEncoding;
import ie.omk.smpp.util.AlphabetFactory;
import ie.omk.smpp.util.BinaryEncoding;
import ie.omk.smpp.util.GSMConstants;
import ie.omk.smpp.util.MessageEncoding;
import ie.omk.smpp.util.SMPPDate;
import ie.omk.smpp.util.SMPPIO;

/** This is the abstract class that all SMPP messages are inherited from.
 *  @author Oran Kelly
 *  @version 1.0
 */
public abstract class SMPPPacket
{
    /** Command Id: Negative Acknowledgement */
    public static final int GENERIC_NACK                = 0x80000000;
    /** Command Id: Bind Receiver */
    public static final int BIND_RECEIVER               = 0x00000001;
    /** Command Id: Bind Receiver Response */
    public static final int BIND_RECEIVER_RESP          = 0x80000001;
    /** Command Id: Bind transmitter */
    public static final int BIND_TRANSMITTER            = 0x00000002;
    /** Command Id: Bind transmitter response */
    public static final int BIND_TRANSMITTER_RESP       = 0x80000002;
    /** Command Id: Query message */
    public static final int QUERY_SM                    = 0x00000003;
    /** Command Id: Query message response */
    public static final int QUERY_SM_RESP               = 0x80000003;
    /** Command Id: Submit message */
    public static final int SUBMIT_SM                   = 0x00000004;
    /** Command Id: Submit message response */
    public static final int SUBMIT_SM_RESP              = 0x80000004;
    /** Command Id: Deliver Short message */
    public static final int DELIVER_SM                  = 0x00000005;
    /** Command Id: Deliver message response */
    public static final int DELIVER_SM_RESP             = 0x80000005;
    /** Command Id: Unbind */
    public static final int UNBIND                      = 0x00000006;
    /** Command Id: Unbind response */
    public static final int UNBIND_RESP                 = 0x80000006;
    /** Command Id: Replace message */
    public static final int REPLACE_SM                  = 0x00000007;
    /** Command Id: replace message response */
    public static final int REPLACE_SM_RESP             = 0x80000007;
    /** Command Id: Cancel message */
    public static final int CANCEL_SM                   = 0x00000008;
    /** Command Id: Cancel message response */
    public static final int CANCEL_SM_RESP              = 0x80000008;
    /** Command Id: Bind transceiver */
    public static final int BIND_TRANSCEIVER            = 0x00000009;
    /** Command Id: Bind transceiever response. */
    public static final int BIND_TRANSCEIVER_RESP       = 0x80000009;
    /** Command Id: Outbind. */
    public static final int OUTBIND                     = 0x0000000b;
    /** Command Id: Enquire Link */
    public static final int ENQUIRE_LINK                = 0x00000015;
    /** Command Id: Enquire link respinse */
    public static final int ENQUIRE_LINK_RESP           = 0x80000015;
    /** Command Id: Submit multiple messages */
    public static final int SUBMIT_MULTI                = 0x00000021;
    /** Command Id: Submit multi response */
    public static final int SUBMIT_MULTI_RESP           = 0x80000021;
    /** Command Id: Parameter retrieve */
    public static final int PARAM_RETRIEVE              = 0x00000022;
    /** Command Id: Paramater retrieve response */
    public static final int PARAM_RETRIEVE_RESP         = 0x80000022;
    /** Command Id: Query last messages */
    public static final int QUERY_LAST_MSGS             = 0x00000023;
    /** Command Id: Query last messages response */
    public static final int QUERY_LAST_MSGS_RESP        = 0x80000023;
    /** Command Id: Query message details */
    public static final int QUERY_MSG_DETAILS           = 0x00000024;
    /** Command Id: Query message details response */
    public static final int QUERY_MSG_DETAILS_RESP	= 0x80000024;
    /** Command Id: alert notification. */
    public static final int ALERT_NOTIFICATION          = 0x00000102;
    /** Command Id: Data message. */
    public static final int DATA_SM                     = 0x00000103;
    /** Command Id: Data message response. */
    public static final int DATA_SM_RESP                = 0x80000103;

    /** Message state at Smsc: En route */
    public static final int SM_STATE_EN_ROUTE		= 1;
    /** Message state at Smsc: Delivered (final) */
    public static final int SM_STATE_DELIVERED		= 2;
    /** Message state at Smsc: Expired (final) */
    public static final int SM_STATE_EXPIRED		= 3;
    /** Message state at Smsc: Deleted (final) */
    public static final int SM_STATE_DELETED		= 4;
    /** Message state at Smsc: Undeliverable (final) */
    public static final int SM_STATE_UNDELIVERABLE	= 5;
    /** Message state at Smsc: Accepted */
    public static final int SM_STATE_ACCEPTED		= 6;
    /** Message state at Smsc: Invalid message (final) */
    public static final int SM_STATE_INVALID		= 7;

    /** Esm class: Mobile Terminated; Normal delivery, no address swapping */
    public static final int SMC_MT			= 1;
    /** Esm class: Mobile originated */
    public static final int SMC_MO			= 2;
    /** Esm class: Mobile Originated / Terminated */
    public static final int SMC_MOMT			= 3;
    /** Esm class: Delivery receipt, no address swapping */
    public static final int SMC_RECEIPT						= 4;
    /** Esm class: Predefined message */
    public static final int SMC_DEFMSG			= 8;
    /** Esm class: Normal delivery , address swapping on */
    public static final int SMC_LOOPBACK_RECEIPT	= 16;
    /** Esm class: Delivery receipt, address swapping on */
    public static final int SMC_RECEIPT_SWAP		= 20;
    /** Esm class: Store message, do not send to Kernel */
    public static final int SMC_STORE			= 32;
    /** Esm class: Store message and send to kernel */
    public static final int SMC_STORE_FORWARD		= 36;
    /** Esm class: Distribution submission */
    public static final int SMC_DLIST			= 64;
    /** Esm class: Multiple recipient submission */
    public static final int SMC_MULTI			= 128;
    /** Esm class: Distribution list and multiple recipient submission */
    public static final int SMC_CAS_DL			= 256;
    /** Esm class: Escalated message FFU */
    public static final int SMC_ESCALATED		= 512;
    /** Esm class: Submit with replace message */
    public static final int SMC_SUBMIT_REPLACE		= 1024;
    /** Esm class: Memory capacity error */
    public static final int SMC_MCE			= 2048;

    /** Esme error code: No error */
    public static final int ESME_ROK			= 0;


    /** Command ID. */
    protected int commandId = 0;

    /** Command status. */
    protected int commandStatus = 0;

    /** Packet sequence number. */
    protected int sequenceNum = 0;

    /* Almost all packets use one or more of these.
     * These attributes were all stuck in here for easier maintenance...
     * instead of altering 5 different packets, just alter it here!!
     * Special cases like SubmitMulti and QueryMsgDetailsResp maintain
     * their own destination tables.  Any packets that wish to use
     * these attribs should override the appropriate methods defined
     * below to be public and just call super.method()
     */

    /** Source address */
    protected Address	source = null;
    
    /** Destination address */
    protected Address	destination = null;
    
    /** The short message data */
    protected byte[]		message = null;
    
    /** Service type for this msg */
    protected String		serviceType = null;
    
    /** Scheduled delivery time */
    protected SMPPDate		deliveryTime = null;
    
    /** Scheduled expiry time */
    protected SMPPDate		expiryTime = null;
    
    /** Date of reaching final state */
    protected SMPPDate		finalDate = null;
    
    /** Smsc allocated message Id */
    protected String		messageId = null;
    
    /** Status of message */
    protected int		messageStatus = 0;
    
    /** Error associated with message */
    protected int		errorCode = 0;
    
    /** Message priority. In v3.3, this is boolean, either '1' or '0' */
    protected int		priority = 0;

    /** Registered delivery. */
    protected boolean		registered = false;

    /** Replace if present. */
    protected boolean		replaceIfPresent = false;

    /** ESM class. */
    protected int		esmClass = 0;

    /** GSM protocol ID. */
    protected int		protocolID = 0;

    /** GSM data coding (see GSM 03.38). */
    public int			dataCoding = 0;

    /** Default message number. */
    protected int		defaultMsg = 0;

    /** Optional parameter table. */
    protected TLVTable		tlvTable = new TLVTable();

    /** Alphabet to use to encode this message's text. */
    private AlphabetEncoding	alphabet = AlphabetFactory.getDefaultAlphabet();

    /** Create a new SMPPPacket with specified Id.
      * @param id Command Id value
      */
    protected SMPPPacket(int id)
    {
	this(id, 0);
    }

    /** Create a new SMPPPacket with specified Id and sequence number.
      * @param id Command Id value
      * @param seqNum Command Sequence number
      */
    protected SMPPPacket(int id, int seqNum)
    {
	this.commandId = id;
	this.sequenceNum = seqNum;
    }

    /** Read an SMPPPacket header from an InputStream
      * @param in InputStream to read from
      * @throws IOException if there's an error reading from the input
      * stream.
      */
    /*protected SMPPPacket(InputStream in)
	throws java.io.IOException, ie.omk.smpp.SMPPException
    {
	int cmdLen = SMPPIO.readInt(in, 4);
	this.commandId = SMPPIO.readInt(in, 4);
	this.commandStatus = SMPPIO.readInt(in, 4);
	this.sequenceNum = SMPPIO.readInt(in, 4);
    }*/

    /** Return the number of bytes this packet would be encoded as to an
      * OutputStream.
      * @return The size in bytes of the packet
      * @deprecated
      */
    public final int getCommandLen()
    {
	// stop overriding this deprecated method.
	return (getLength());
    }

    /** Get the number of bytes this packet would be encoded as. This returns
     * the sum of the size of the header (always 16), the packet's body and all
     * optional parameters.
     * @return the number of bytes this packet would encode as.
     */
    public final int getLength()
    {
	return (16 + getBodyLength() + tlvTable.getLength());
    }

    /** Get the number of bytes the body of this packet would encode as. This
     * method should only return the number of bytes the fields in the mandatory
     * parameters section of the packet would encode as. The total size of the
     * packet then is 16 (header length) + getBodyLength() + SUM(foreach
     * optionalParameter: getLength()).
     */
    public abstract int getBodyLength();

    /** Get the Command Id of this SMPP packet.
      * @return The Command Id of this packet
      */
    public int getCommandId()
    {
	return (commandId);
    }

    /** Get the status of this packet.
      * @return The error status of this packet (only relevent to Response
      * packets)
      */
    public int getCommandStatus()
    {
	return (this.commandStatus);
    }

    /** Get the sequence number of this packet.
      * @return The sequence number of this SMPP packet
      */
    public int getSequenceNum()
    {
	return (this.sequenceNum);
    }

    /** Set the sequence number of this packet.
      */
    public void setSequenceNum(int sequenceNum)
    {
	this.sequenceNum = sequenceNum;
    }


    /** Set the source address.
      * Not used by all SMPP Packet types.
      * @see ie.omk.smpp.message.SubmitSM
      * @see ie.omk.smpp.message.DeliverSM
      * XXX Add other packets.
      */
    public void setSource(Address s)
    {
	if(s != null) {
	    this.source = s;
	}
    }

    /** Get the source address.
      * Not used by all SMPP Packet types.
      * @return The source address or null if it is not set.
      */
    public Address getSource()
    {
	if(source != null)
	    return (source);
	else
	    return (null);
    }

    /** Set the destination address.
      * Not used by all SMPP Packet types.
      */
    public void setDestination(Address s)
    {
	if(s != null) {
	    this.destination = s;
	}
    }

    /** Get the destination address.
      * Not used by all SMPP Packet types.
      * @return The destination address or null if it is not set.
      */
    public Address getDestination()
    {
	if(destination != null) {
	    return (destination);
	} else {
	    return (null);
	}
    }

    /** Set the message flags.
      * Not used by all SMPP Packet types.
      * @see ie.omk.smpp.message.MsgFlags
      * @deprecated
      */
    public void setMessageFlags(MsgFlags f)
    {
	if(f != null) {
	    this.priority = (f.priority) ? 1 : 0;
	    this.registered = f.registered;
	    this.replaceIfPresent = f.replace_if_present;
	    this.esmClass = f.esm_class;
	    this.protocolID = f.protocol;
	    this.dataCoding = f.data_coding;
	    this.defaultMsg = f.default_msg;
	}
    }

    /** Set the 'priority' flag.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public void setPriority(boolean b)
    {
	this.priority = (b) ? 1 : 0;
    }

    /** Set 'registered delivery' flag.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public void setRegistered(boolean b)
    {
	this.registered = b;
    }

    /** Set 'replace if present'.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public void setReplaceIfPresent(boolean b)
    {
	this.replaceIfPresent = b;
    }
    
    /** Set the esm class of the message.
      * @see ie.omk.smpp.message.MsgFlags
      * @see ie.omk.smpp.util.GSMConstants
      */
    public void setEsmClass(int c)
    {
	this.esmClass = c;
    }

    /** Set the protocol Id in the message flags.
      * @see ie.omk.smpp.message.MsgFlags
      * @see ie.omk.smpp.util.GSMConstants
      * @deprecated ie.omk.smpp.message.SMPPPacket#setProtocolID
      */
    public void setProtocolId(int id)
    {
	this.protocolID = id;
    }

    /** Set the GSM protocol ID.
      * @see ie.omk.smpp.message.MsgFlags
      * @see ie.omk.smpp.util.GSMConstants
      */
    public void setProtocolID(int id)
    {
	this.protocolID = id;
    }

    /** Set the GSM data coding of the message.
      * @see ie.omk.smpp.message.MsgFlags
      * @see ie.omk.smpp.util.GSMConstants
      */
    public void setDataCoding(int dc)
    {
	this.dataCoding = dc;
    }

    /** Set the default message id in the message flags.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public void setDefaultMsg(int id)
    {
	this.defaultMsg = id;
    }

    /** Get the message flags.
      * @return The ie.omk.smpp.message.MsgFlags object. Never null.
      * @deprecated
      */
    public MsgFlags getMessageFlags()
    {
	MsgFlags flags = new MsgFlags();
	flags.priority  = (this.priority == 0) ? false : true;
	flags.registered = this.registered;
	flags.replace_if_present = this.replaceIfPresent;
	flags.esm_class = this.esmClass;
	flags.protocol = this.protocolID;
	flags.data_coding = this.dataCoding;
	flags.default_msg = this.defaultMsg;

	return (flags);
    }

    /** Check is the message registered.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public boolean isRegistered()
    {
	return (this.registered);
    }

    /** Check is the message submitted as priority.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public boolean isPriority()
    {
	return ((this.priority == 0) ? false : true);
    }

    /** Check if the message should be replaced if it is already present.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public boolean isReplaceIfPresent()
    {
	return (this.replaceIfPresent);
    }
    /** Get the ESM class of the message.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public int getEsmClass()
    {
	return (this.esmClass);
    }
    /** Get the GSM protocol Id of the message.
      * @see ie.omk.smpp.message.MsgFlags
      * @deprecated getProtocolID
      */
    public int getProtocolId()
    {
	return (this.protocolID);
    }

    /** Get the GSM protocol ID of the message.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public int getProtocolID()
    {
	return (this.protocolID);
    }

    /** Get the data coding.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public int getDataCoding()
    {
	return (this.dataCoding);
    }
    /** Get the default message to use.
      * @see ie.omk.smpp.message.MsgFlags
      */
    public int getDefaultMsgId()
    {
	return (this.defaultMsg);
    }

    /** Set the text of the message (max 160 characters).
      * This method sets the message text encoded using the default alphabet (as
      * returned by AlphabetFactory.getDefaultAlphabet).  The maximum length of
      * the string is 160 octets. Calling this method sets the data_coding value
      * using AlphabetEncoding.getDataCoding.
      * @param text The short message text.
      * @throws ie.omk.smpp.StringTooLongException if the message is too
      * long.
      * @see ie.omk.smpp.util.AlphabetEncoding
      * @see ie.omk.smpp.util.AlphabetEncoding#getDataCoding
      * @see ie.omk.smpp.util.AlphabetFactory
      * @see ie.omk.smpp.util.DefaultAlphabetEncoding
      */
    public void setMessageText(String text)
	throws StringTooLongException
    {
	this.setMessageText(text, this.alphabet);
    }

    /** Set the text of the message (max 160 characters).
      * This method sets the message text encoded using the SMS alphabet
      * <i>alphabet</i>. The AlphabetEncoding.getDataCoding value will be used
      * to set the data_coding field.
      * @param text The short message text.
      * @param alphabet The SMS alphabet to use.
      * @throws ie.omk.smpp.StringTooLongException if the message is too
      * long.
      * @see ie.omk.smpp.util.AlphabetEncoding
      * @see ie.omk.smpp.util.AlphabetEncoding#getDataCoding
      * @see ie.omk.smpp.util.DefaultAlphabetEncoding
      */
    public void setMessageText(String text, AlphabetEncoding alphabet)
	throws StringTooLongException
    {
	if(text == null) {
	    this.message = null;
	    return;
	}

	byte[] bytes = alphabet.encodeString(text);
	this.setMessage(bytes, alphabet);
    }

    /** Set the message data (max 140 octets).
      * Maximum data length is 140 octets. The data will be copied from the
      * supplied byte array into a newly created internal one.
      * @param message The byte array to take message data from.
      * @throws ie.omk.smpp.StringTooLongException if the message is too long
      */
    public void setMessage(byte[] message)
	throws StringTooLongException
    {
	this.setMessage(message, 0, message.length, null);
    }

    /** Set the message data (max 140 octets).
      * Maximum data length is 140 octets. The data will be copied from the
      * supplied byte array into a newly created internal one.
      * @param message The byte array to take message data from.
      * @throws ie.omk.smpp.StringTooLongException if the message is too long
      */
    public void setMessage(byte[] message, MessageEncoding encoding)
	throws StringTooLongException
    {
	this.setMessage(message, 0, message.length, encoding);
    }

    /** Set the message data (max 140 octets).
      * Maximum data length is 140 octets. The data will be copied from the
      * supplied byte array into a newly created internal one. If
      * <i>encoding</i> is not null, the data_coding field will be set using the
      * value returned by MessageEncoding.getDataCoding.
      * @param message The byte array to take message data from.
      * @param start The index the message data begins at.
      * @param len The length of the message data.
      * @param encoding The encoding object representing the type of data in the
      * message. If null, uses ie.omk.smpp.util.BinaryEncoding.
      * @throws ie.omk.smpp.StringTooLongException if the message is too long
      */
    public void setMessage(byte[] message, int start, int len,
	    MessageEncoding encoding)
	throws StringTooLongException
    {
	int maxLen = 0;
	int dcs = -1;

	if (encoding == null) {
	    // use unspecified (ie binary) encoding type..
	    maxLen = new BinaryEncoding().getMaxLength();
	} else {
	    maxLen = encoding.getMaxLength();
	    dcs = encoding.getDataCoding();
	}

	if (message != null) {
	    if ((start < 0) || (len < 0) || message.length < (start + len))
		throw new ArrayIndexOutOfBoundsException();

	    if (len > maxLen) {
		throw new StringTooLongException(maxLen);
	    }

	    this.message = new byte[len];
	    System.arraycopy(message, start, this.message, 0, len);
	    if (dcs != -1)
		this.setDataCoding(encoding.getDataCoding());
	} else {
	    this.message = null;
	}
    }

    /** Get the message data.
      * This method returns a <i>copy</i> of the binary message data.
      * @return A byte array copy of the message data. May be null.
      */
    public byte[] getMessage()
    {
	byte[] b = null;
	if (this.message != null) {
	    b = new byte[this.message.length];
	    System.arraycopy(this.message, 0, b, 0, b.length);
	}
	return (b);
    }

    /** Get the text of the message.
      * The message will be decoded according to  the data_coding field. If the
      * API has no registered encoding for a data_coding value, the default
      * alphabet (as returned by AlphabetFactory.getDefaultAlphabet) will be
      * used.
      * @see ie.omk.smpp.util.AlphabetFactory#getDefaultAlphabet
      */
    public String getMessageText()
    {
	AlphabetEncoding enc = AlphabetEncoding.getEncoding(this.dataCoding);
	if (enc == null)
	    enc = this.alphabet;

	return (enc.decodeString(this.message));
    }


    /** Get the text of the message.
      * @param enc The text encoding of the message bytes.
      * @see ie.omk.smpp.util.AlphabetEncoding
      */
    public String getMessageText(AlphabetEncoding enc)
    {
	return (enc.decodeString(this.message));
    }

    /** Get the number of octets in the message payload.
      * @return The number of octets (bytes) in the message payload.
      */
    public int getMessageLen()
    {
	return (message == null) ? 0 : message.length;
    }

    /** Set the service type.
      * @param type The service type.
      * @throws ie.omk.smpp.StringTooLongException if the service type is too
      * long.
      */
    public void setServiceType(String type)
	throws StringTooLongException
    {
	if(type == null) {
	    serviceType = null;
	    return;
	}

	if(type.length() < 6) {
	    this.serviceType = type;
	} else {
	    throw new StringTooLongException(5);
	}
    }

    /** Get the service type. */
    public String getServiceType()
    {
	return (serviceType);
    }

    /** Set the scheduled delivery time for the short message.
      * @param d The date and time the message should be delivered.
      */
    public void setDeliveryTime(SMPPDate d)
    {
	this.deliveryTime = d;
    }

    /** Get the current value of the scheduled delivery time for the short
      * message.
      */
    public SMPPDate getDeliveryTime()
    {
	return (deliveryTime);
    }

    /** Set the expiry time of the message.
      * If the message is not delivered by time 'd', it will be cancelled and
      * never delivered to it's destination.
      * @param d the date and time the message should expire.
      */
    public void setExpiryTime(SMPPDate d)
    {
	expiryTime = d;
    }

    /** Get the current expiry time of the message.
      */
    public SMPPDate getExpiryTime()
    {
	return (expiryTime);
    }

    /** Set the final date of the message.
      * The final date is the date and time that the message reached it's final
      * destination.
      * @param d the date the message was delivered.
      */
    public void setFinalDate(SMPPDate d)
    {
	finalDate = d;
    }

    /** Get the final date of the message.
      */
    public SMPPDate getFinalDate()
    {
	return (finalDate);
    }

    /** Set the message Id.
      * Each submitted short message is assigned an Id by the SMSC which is used
      * to uniquely identify it. SMPP v3.3 message Ids are hexadecimal numbers
      * up to 9 characters long. This gives them a range of 0x0 - 0xffffffff.
      * <p>SMPP v3.4 Ids, on the other hand, are opaque objects represented as
      * C-Strings assigned by the SMSC and can be up to 64 characters (plus 1
      * nul-terminator).
      * @param id The message's id.
      * @throws ie.omk.smpp.StringTooLongException If the message ID is too long
      * for for the interface version in use.
      */
    public void setMessageId(String id)
	throws StringTooLongException
    {
	if (id == null) {
	    this.messageId = null;
	} else {
	    // XXX need a version length check!
	    this.messageId = id;
	}
    }
    
    /** Get the message id.
      */
    public String getMessageId()
    {
	return (this.messageId);
    }

    /** Set the message status. This is different to the command status field.
      * XXX describe the message status.
      * @param st The message status.
      */
    public void setMessageStatus(int st)
    {
	this.messageStatus = st;
    }

    /** Get the message status.
      */
    public int getMessageStatus()
    {
	return (this.messageStatus);
    }

    /** Set the error code.
      * @param code The error code.
      */
    public void setErrorCode(int code)
    {
	errorCode = code; 
    }

    /** Get the error code.
      */
    public int getErrorCode()
    {
	return (errorCode);
    }

    /** Get the optional parameter (TLV) table.
     * @see ie.omk.smpp.message.tlv.TLVTable
     */
    public TLVTable getTLVTable() {
	return (tlvTable);
    }

    /** Set the optional parameter (TLV) table. This method discards the entire
     * optional paramter table and replaces it with <code>table</code>. The
     * discarded table is returned. If <code>null</code> is passed in, a new,
     * empty TLVTable object will be created.
     * @see ie.omk.smpp.message.tlv.TLVTable
     * @return the old tlvTable.
     */
    public TLVTable setTLVTable(TLVTable table) {
	TLVTable t = this.tlvTable;
	if (table == null)
	    this.tlvTable = new TLVTable();
	else
	    this.tlvTable = table;

	return (t);
    }

    /** Set an optional parameter. This is a convenience method and merely calls
     * {@link ie.omk.smpp.message.tlv.TLVTable#set} on this message's optional
     * parameter table.
     * @param tag the tag of the parameter to set.
     * @param value the value object to set.
     * @throws ie.omk.smpp.message.tlv.BadValueTypeException if the type of
     * <code>value</code> is incorrect for the <code>tag</code>.
     * @return the previous value of the parameter, or null if it was unset.
     */
    public Object setOptionalParameter(Tag tag, Object value) {
	return (tlvTable.set(tag, value));
    }

    /** Get an optional parameter. This is a convenience method and merely calls
     * {@link ie.omk.smpp.message.tlv.TLVTable#set} on this message's optional
     * parameter table.
     * @param tag the tag of the parameter value to get.
     */
    public Object getOptionalParameter(Tag tag) {
	return (tlvTable.get(tag));
    }

    /** Check if a particular optional parameter is set. This is a convenience
     * method and merely calls {@link ie.omk.smpp.message.tlv.TLVTable#isSet} on
     * this message's optional parameter table.
     * @param tag the tag of the parameter to check.
     * @return true if the parameter is set, false if it is not.
     */
    public boolean isSet(Tag tag) {
	return (tlvTable.isSet(tag));
    }

    /** Set the alphabet encoding for this message.
     * @param enc The alphabet to use. If null, use DefaultAlphabetEncoding.
     * @see ie.omk.smpp.util.AlphabetEncoding
     * @see ie.omk.smpp.util.DefaultAlphabetEncoding
     */
    public void setAlphabet(AlphabetEncoding enc)
    {
	if (enc == null)
	    this.alphabet = new ie.omk.smpp.util.DefaultAlphabetEncoding();
	else
	    this.alphabet = enc;
    }

    /** Return a String representation of this packet. This method does not
     * return any value which is useful programatically...it returns a
     * description of the packet's header as follows:<br>
     * <code>"SMPP(l:[len], c:[commandId], s:[status], n:[sequence])"</code>
     */
    public String toString()
    {
	return (new StringBuffer("SMPP(l:")
		.append(Integer.toString(getLength()))
		.append(", c:0x").append(Integer.toHexString(commandId))
		.append(", s:").append(Integer.toString(commandStatus))
		.append(", n:").append(Integer.toString(sequenceNum))
		.append(")").toString());
    }

    /** Encode the body of the SMPP Packet to the output stream. Sub classes
      * should override this method to output their packet-specific fields. This
      * method is called from SMPPPacket.writeTo(java.io.OutputStream) to
      * encode the message.
      * @param out The output stream to write to.
      * @throws java.io.IOException if there's an error writing to the output
      * stream.
      */
    protected void encodeBody(OutputStream out)
	throws java.io.IOException
    {
	// some packets ain't got a body...provide a default adapter instead of
	// making it abstract.
    }

    /** Write the byte representation of this SMPP packet to an OutputStream
      * @param out The OutputStream to use
      * @throws java.io.IOException if there's an error writing to the
      * output stream.
      */
    public final void writeTo(OutputStream out)
	throws java.io.IOException
    {
	int commandLen = getLength();

	SMPPIO.writeInt(commandLen, 4, out);
	SMPPIO.writeInt(commandId, 4, out);
	SMPPIO.writeInt(commandStatus, 4, out);
	SMPPIO.writeInt(sequenceNum, 4, out);

	encodeBody(out);
	tlvTable.writeTo(out);

    }

    // XXX javadoc
    public void readFrom(byte[] b, int offset)
    {
	// Clear out the TLVTable..
	tlvTable.clear();

	if (b.length < (offset + 16)) {
	    // XXX exception??
	    throw new RuntimeException("no header present in bytes (not enuf)");
	}

	int len = SMPPIO.bytesToInt(b, offset, 4);
	int id = SMPPIO.bytesToInt(b, offset + 4, 4);

	if (id != commandId) {
	    // Command type mismatch...ye can't do that, lad!
	    // XXX exception
	    throw new RuntimeException("Command ID mismatch.");
	}
	if (b.length < (offset + len)) {
	    // not enough bytes there for me to read in, buddy!
	    // XXX exception
	    throw new RuntimeException("not enough bytes in array.");
	}

	commandStatus = SMPPIO.bytesToInt(b, offset + 8, 4);
	sequenceNum = SMPPIO.bytesToInt(b, offset + 12, 4);

	if (commandStatus == 0) {
	    // Read the mandatory body parameters..
	    int ptr = 16 + offset;
	    readBodyFrom(b, ptr);

	    // Read the optional parameters..
	    ptr += getBodyLength();
	    tlvTable.readFrom(b, offset, (offset + len) - ptr);
	}
    }

    // XXX javadoc
    public abstract void readBodyFrom(byte[] b, int offset);
}
