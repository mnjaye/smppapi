package ie.omk.smpp.message;

import ie.omk.smpp.Address;
import ie.omk.smpp.util.SMPPIO;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

// TODO document
// TODO: I removed iterator methods - how to make this class useful again?
public class DestinationTable implements Cloneable {
    private List<Address> addresses = new ArrayList<Address>();
    private List<String> distributionLists = new ArrayList<String>();

    private int length;

    public DestinationTable() {
    }

    public void add(Address addr) {
        addresses.add(addr);
        // Plus 1 for the dest type flag.
        length += addr.getLength() + 1;
    }

    public void add(String distributionList) {
        distributionLists.add(distributionList);
        // nul byte plus dest type flag
        length += distributionList.length() + 2;
    }

    public void remove(Address addr) {
        int i = addresses.indexOf(addr);
        if (i > -1) {
            length -= addresses.remove(i).getLength() + 1;
        }
    }

    public void remove(String distributionList) {
        int i = distributionLists.indexOf(distributionList);
        if (i > -1) {
            length -= distributionLists.remove(i).length() + 2;
        }
    }

    public synchronized int getLength() {
        return length;
    }

    public int size() {
        return addresses.size() + distributionLists.size();
    }

    public synchronized void writeTo(OutputStream out) throws java.io.IOException {
        for (Address address : addresses) {
            SMPPIO.writeInt(1, 1, out);
            address.writeTo(out);
        }
        for (String list : distributionLists) {
            SMPPIO.writeInt(2, 1, out);
            SMPPIO.writeCString(list, out);
        }
    }

    public synchronized void readFrom(byte[] table, int offset, int count) {
        for (int i = 0; i < count; i++) {
            int type = SMPPIO.bytesToInt(table, offset++, 1);
            if (type == 1) {
                // SME address..
                Address a = new Address();
                a.readFrom(table, offset);
                offset += a.getLength();
                addresses.add(a);
            } else if (type == 2) {
                // Distribution list name
                String d = SMPPIO.readCString(table, offset);
                offset += d.length() + 1;
                distributionLists.add(d);
            } else {
                LoggerFactory.getLogger(DestinationTable.class).warn(
                        "Unidentified destination type on input.");
            }
        }
        calculateLength();
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException x) {
            throw new RuntimeException("Clone not supported", x);
        }
    }

    public String toString() {
        List<Object> list = new ArrayList<Object>();
        list.addAll(addresses);
        list.addAll(distributionLists);
        return list.toString();
    }
    
    private void calculateLength() {
        // One byte for all type flags, plus 1 (null) byte for each distribution
        // list string
        length = addresses.size() + (distributionLists.size() * 2);
        for (Address address : addresses) {
            // For the destination type flag
            length += address.getLength();
        }
        for (String list : distributionLists) {
            length += list.length();
        }
    }
}