/* ByteBufferHelper.java 2007/10/18
 *
 * Copyright 2006 Chimen Chen. All rights reserved.
 *
 */

package org.jchmlib.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * ByteBufferHelper provides some ByteBuffer-relating methods.
 */
public class ByteBufferHelper {

    public static void skip(ByteBuffer bb, int count) {
        bb.position(bb.position() + count);
    }

    /**
     * get a big-endian int from a little-endian ByteBuffer
     */
    public static int parseBigEndianInt(ByteBuffer bb) {
        ByteOrder origOrder = bb.order();
        bb.order(ByteOrder.BIG_ENDIAN);
        int result = bb.getInt();
        bb.order(origOrder);
        return result;
    }

    public static long getUInt32(ByteBuffer bb) {
        ByteOrder origOrder = bb.order();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int tmp = bb.getInt();
        long result = tmp & 0x00000000ffffffffL;
        bb.order(origOrder);
        return result;
    }

    /**
     * parse a compressed dword (a variant length integer)
     */
    public static long parseCWord(ByteBuffer bb) {
        long accum = 0;
        byte temp = bb.get();
        while (temp < 0) {  // if the most significant bit is 1
            accum <<= 7;
            accum += temp & 0x7f;
            temp = bb.get();
        }

        return (accum << 7) + temp;
    }

    public static String parseString(ByteBuffer bb, String codec) {
        int len = bb.remaining();
        return parseString(bb, len, codec);
    }

    /**
     * Parses a utf-8 string.
     */
    public static String parseUTF8(ByteBuffer bb, int strLen) throws IOException {
        return parseString(bb, strLen, "UTF-8");
    }

    /**
     * Parses a String using the named Charset Encoding.
     */
    public static String parseString(ByteBuffer bb, int strLen, String codec) {
        int length = Math.min(bb.remaining(), strLen);
        if (length <= 0) {
            return null;
        }

        byte[] buf = new byte[length];
        bb.get(buf);

        for (int j = 0; j < buf.length; j++) {
            if (buf[j] == 0) {
                length = j;
                break;
            }
        }

        return bytesToString(buf, 0, length, codec);
    }

    /**
     * Gets the remaining contents of the given java.nio.ByteBuffer
     * (i.e. the bytes between its position and limit) as a String.
     * Leaves the position of the ByteBuffer unchanged.
     */
    public static String dataToString(ByteBuffer buf, String encoding) {
        // First check to see if the input buffer has a backing array; 
        // if so, we can just use it, to save making a copy of the data
        byte[] bytes;
        if (buf.hasArray()) {
            bytes = buf.array();
            return bytesToString(bytes, buf.position(), buf.remaining(),
                    encoding);
        } else {
            // FIXME: synchronization on method parameter
            synchronized (buf) {
                // Remember the original position of the buffer
                int pos = buf.position();
                bytes = new byte[buf.remaining()];
                buf.get(bytes);
                // Reset the original position of the buffer
                buf.position(pos);
            }
            return bytesToString(bytes, encoding);
        }
    }

    private static String bytesToString(byte[] bytes, String encoding) {
        return bytesToString(bytes, 0, bytes.length, encoding);
    }

    public static String bytesToString(byte[] bytes, int offset,
            int length, String encoding) {
        String result;
        try {
            result = new String(bytes, offset, length, encoding);
        } catch (UnsupportedEncodingException ignored) {
            result = new String(bytes, offset, length);
        }
        return result;
    }

}

