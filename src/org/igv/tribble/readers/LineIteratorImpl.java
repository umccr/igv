package org.igv.tribble.readers;

import htsjdk.samtools.util.AbstractIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.RuntimeIOException;

import java.io.Closeable;
import java.io.IOException;

/**
 * A simple iterator over the elements in LineReader.
 */
public class LineIteratorImpl extends AbstractIterator<String> implements org.igv.tribble.readers.LineIterator, Closeable {
    private final org.igv.tribble.readers.LineReader lineReader;

    /**
     * @param lineReader The line reader whose elements are to be iterated over.
     */
    public LineIteratorImpl(final LineReader lineReader) {
        this.lineReader = lineReader;
    }

    @Override
    protected String advance() {
        try {
            return lineReader.readLine();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        CloserUtil.close(lineReader);
    }
}
