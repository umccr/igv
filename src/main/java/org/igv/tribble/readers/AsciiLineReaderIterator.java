package org.igv.tribble.readers;

import htsjdk.samtools.util.*;

import java.io.Closeable;
import java.io.IOException;

/**
 * A class that iterates over the lines and line positions in an {@link org.igv.tribble.readers.AsciiLineReader}.
 * <p>
 * This class is slower than other {@link org.igv.tribble.readers.LineIterator}s because it is driven by {@link org.igv.tribble.readers.AsciiLineReader}, but offers the benefit of
 * implementing {@link LocationAware}, which is required for indexing.  If you do not require {@link LocationAware}, consider using
 * {@link org.igv.tribble.readers.LineIteratorImpl} as an alternative to this class.
 * <p>
 * Note an important distinction in the way this class and its inner iterator differ: in the inner iterator, the position stored with
 * a line is the position at the start of that line.  However, {@link #getPosition()} of the outer class must return the position at the
 * end of the most-recently-returned line (or the start of the underlying {@link org.igv.tribble.readers.AsciiLineReader}, if no line has been read).  The latter
 * bit of logic here is required to conform with the interface described by {@link LocationAware#getPosition()}.
 *
 * @author mccowan
 */
public class AsciiLineReaderIterator implements LocationAware, LineIterator, Closeable {
    private final org.igv.tribble.readers.AsciiLineReader asciiLineReader;
    private final TupleIterator i;
    private Tuple<String, Long> current = null;

    public AsciiLineReaderIterator(final org.igv.tribble.readers.AsciiLineReader asciiLineReader) {
        this.asciiLineReader = asciiLineReader;
        this.i = new TupleIterator();
    }

    @Override
    public void close() throws IOException {
        CloserUtil.close(asciiLineReader);
    }

    @Override
    public boolean hasNext() {
        return i.hasNext();
    }

    @Override
    public String next() {
        current = i.next();
        return current.a;
    }

    @Override
    public void remove() {
        i.remove();
    }

    /**
     * Returns the byte position at the end of the most-recently-read line (a.k.a., the beginning of the next line) from {@link #next()} in
     * the underlying {@link AsciiLineReader}.
     */
    @Override
    public long getPosition() {
        return i.getPosition();
    }

    @Override
    public String peek() {
        return i.peek().a;
    }

    /**
     * This is stored internally since it iterates over {@link Tuple}, not {@link String} (and the outer
     * class can't do both).
     */
    private class TupleIterator extends AbstractIterator<Tuple<String, Long>> implements LocationAware {

        public TupleIterator() {
            hasNext(); // Initialize the iterator, which appears to be a requirement of the parent class.  TODO: Really?
        }

        @Override
        protected Tuple<String, Long> advance() {
            final String line;
            final long position = asciiLineReader.getPosition(); // A line's position is where it starts, so get it before reading the line.
            try {
                line = asciiLineReader.readLine();
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
            return line == null ? null : new Tuple<String, Long>(line, position);
        }

        /**
         * Returns the byte position at the beginning of the next line.
         */
        @Override
        public long getPosition() {
            final Tuple<String, Long> peek = peek();
            // Be careful: peek will be null at the end of the stream.
            return peek != null ? peek.b : asciiLineReader.getPosition();
        }
    }
}
