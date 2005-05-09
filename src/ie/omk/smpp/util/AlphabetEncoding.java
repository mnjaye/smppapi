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

package ie.omk.smpp.util;

/**
 * SMS Alphabet to Java String mapping interface. Implementations of this
 * interface convert Java Unicode strings into a series of bytes representing
 * the String in a particular SMS alphabet.
 */
public abstract class AlphabetEncoding extends ie.omk.smpp.util.MessageEncoding {
    protected AlphabetEncoding(int dcs) {
        super(dcs);
    }

    /**
     * Convert SMS message text into a Java String. Implementations of this
     * method <b>must </b> support decoding <code>null</code>. In such cases,
     * the String "" will be returned.
     */
    public abstract String decodeString(byte[] b);

    /**
     * Convert a Java String into SMS message text. Implementations of this
     * method <b>must </b> support encoding a <code>null</code> string. In
     * such cases, a byte array of length 0 will be returned.
     */
    public abstract byte[] encodeString(String s);
}