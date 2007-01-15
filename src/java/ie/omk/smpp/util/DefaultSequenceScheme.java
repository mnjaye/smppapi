package ie.omk.smpp.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The default sequence numbering scheme. This implementation starts at sequence
 * number 1 and increments by 1 for each number requested, resulting in the
 * sequence numbers <code>1..2..3..4..5..6..7..8..n</code>. If the sequence
 * number reaches as far as <code>Integer.MAX_VALUE</code>, it will wrap back
 * around to 1.
 * <p>
 * This implementation uses an {@link java.util.concurrent.atomic.AtomicInteger}
 * internally to track the next sequence number.
 * </p>
 * @author Oran Kelly
 * @version $Id$
 */
public class DefaultSequenceScheme implements SequenceNumberScheme {

    private int start = 1;
    private AtomicInteger sequence = new AtomicInteger(1);

    public DefaultSequenceScheme() {
    }

    /**
     * Construct a new DefaultSequenceScheme that starts the sequence from
     * <code>start</code>.
     */
    public DefaultSequenceScheme(int start) {
        this.start = start;
        sequence.set(start);
    }

    public int nextNumber() {
        int n = sequence.getAndIncrement();
        if (n == Integer.MAX_VALUE) {
            sequence.set(1);
        }
        return n;
    }

    public int peek() {
        return sequence.get();
    }

    public int peek(int nth) {
        return sequence.get() + nth;
    }

    public void reset() {
        sequence.set(start);
    }
}