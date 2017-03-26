package org.broad.igv.util;

import java.util.Arrays;

/**
 * Created by jrobinso on 3/24/17.
 */
public class ByteArray {

    private byte[] bytes;
    private int offset;
    private int length;

    public ByteArray(byte[] bytes) {
        this.bytes = bytes;
        this.offset = 0;
        this.length = bytes.length;
    }

    public ByteArray(byte[] bytes, int offset, int length) {

        try {
            if (offset < 0 || offset + length > bytes.length) {
                throw new IndexOutOfBoundsException();
            }
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            throw e;
        }
    }


    public byte get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index + "  valid range: " + offset + "-" + (offset + length - 1));
        }
        return bytes[index + offset];
    }

    public int length() {
        return length;
    }

    public String getString() {
        return new String(bytes, offset, length);
    }

    public String getAbbrevString(int len) {
        if (len <= (length - offset)) {
            return getString();
        } else {
            int l1 = length / 2;
            int l2 = length - l1;
            int s2 = offset + (length - l2);
            return new String(Arrays.copyOfRange(bytes, offset, l1)) + "..." +
                    new String(Arrays.copyOfRange(bytes, s2, s2 + l2));
        }
    }

    public ByteArray subArray(int from, int to) {
        if (from < 0 || to > length || from >= to) {
            throw new IndexOutOfBoundsException();
        }
        int len = to - from;
        return new ByteArray(bytes, offset + from, len);

    }

    public ByteArray subArray(int from) {

        if (from < 0 || from >= length) {
            throw new IndexOutOfBoundsException();
        }
        int len = length - from;
        return new ByteArray(bytes, offset + from, len);

    }

    /**
     * Provided for backward compatibility but
     *
     * @return
     */
    public byte[] getBytes() {
        if (offset == 0 && length == bytes.length) {
            return bytes;
        } else {
            return Arrays.copyOfRange(bytes, offset, offset + length);
        }
    }
}
