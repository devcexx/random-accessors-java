/*
 *  This file is part of random-accessors-java.
 *  random-accessors-java is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  random-accessors-java is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with random-accessors-java.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.devcexx.accessors;

public class Bits {
    /**
     * Raised to tell the compiler that a point of the program code cannot
     * be actually reached, but the Java Compiler is too stupid to figure it out.
     */
    private static class FuckJavaException extends RuntimeException {}

    //The parameters that are on the left are the ones with lower memory positions.
    //The right ones, the ones in higher memory positions.

    /**
     * Decodes a short from its bytes.
     * @param b0 the byte in the lower memory position.
     * @param b1 the byte in the highest memory position.
     * @param order the order to use to decode the data.
     * @return the decoded value.
     */
    public static short decodeShort(byte b0, byte b1, DataOrder order) {
        switch (order) {
            case BIG_ENDIAN:
                return (short) (((b0 & 0xff) << 8) | (b1 & 0xff));
            case LITTLE_ENDIAN:
                return (short) (((b1 & 0xff) << 8) | (b0 & 0xff));
        }

        throw new FuckJavaException();
    }

    /**
     * Decodes a char from its bytes.
     * @param b0 the byte in the lower memory position.
     * @param b1 the byte in the highest memory position.
     * @param order the order to use to decode the data.
     * @return the decoded value.
     */
    public static char decodeChar(byte b0, byte b1, DataOrder order) {
        switch (order) {
            case BIG_ENDIAN:
                return (char) (((b0 & 0xff) << 8) | (b1 & 0xff));
            case LITTLE_ENDIAN:
                return (char) (((b1 & 0xff) << 8) | (b0 & 0xff));
        }

        throw new FuckJavaException();
    }

    /**
     * Decodes an int from its bytes.
     * @param b0 the byte in the lower memory position.
     * @param b1 the next byte.
     * @param b2 the next byte.
     * @param b3 the byte in the highest memory position.
     * @param order the order to use to decode the data.
     * @return the decoded value.
     */
    public static int decodeInt(byte b0, byte b1, byte b2, byte b3, DataOrder order) {
        switch (order) {
            case BIG_ENDIAN:
                return ((b0 & 0xff) << 24) | ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff);
            case LITTLE_ENDIAN:
                return ((b3 & 0xff) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | (b0 & 0xff);
        }

        throw new FuckJavaException();
    }

    /**
     * Decodes a long from its bytes.
     * @param b0 the byte in the lower memory position.
     * @param b1 the next byte.
     * @param b3 the next byte.
     * @param b4 the next byte.
     * @param b5 the next byte.
     * @param b6 the next byte.
     * @param b7 the byte in the highest memory position.
     * @param order the order to use to decode the data.
     * @return the decoded value.
     */
    public static long decodeLong(byte b0, byte b1, byte b2,
                                 byte b3, byte b4, byte b5,
                                 byte b6, byte b7, DataOrder order) {
        switch (order) {
            case BIG_ENDIAN:
                return (((long)(b0 & 0xff) << 56) |
                        ((long)(b1 & 0xff) << 48) |
                        ((long)(b2 & 0xff) << 40) |
                        ((long)(b3 & 0xff) << 32) |
                        ((long)(b4 & 0xff) << 24) |
                        ((b5 & 0xff) << 16) |
                        ((b6 & 0xff) <<  8) |
                        ((b7 & 0xff)));
            case LITTLE_ENDIAN:
                return (((long)(b7 & 0xff) << 56) |
                        ((long)(b6 & 0xff) << 48) |
                        ((long)(b5 & 0xff) << 40) |
                        ((long)(b4 & 0xff) << 32) |
                        ((long)(b3 & 0xff) << 24) |
                        ((b2 & 0xff) << 16) |
                        ((b1 & 0xff) <<  8) |
                        ((b0 & 0xff)));
        }

        throw new FuckJavaException();
    }

    /**
     * Decodes a float from its bytes.
     * @param b0 the byte in the lower memory position.
     * @param b1 the next byte.
     * @param b2 the next byte.
     * @param b3 the byte in the highest memory position.
     * @param order the order to use to decode the data.
     * @return the decoded value.
     */
    public static float decodeFloat(byte b0, byte b1, byte b2, byte b3, DataOrder order) {
        return Float.intBitsToFloat(decodeInt(b0, b1, b2, b3, order));
    }

    /**
     * Decodes a double from its bytes.
     * @param b0 the byte in the lower memory position.
     * @param b1 the next byte.
     * @param b3 the next byte.
     * @param b4 the next byte.
     * @param b5 the next byte.
     * @param b6 the next byte.
     * @param b7 the byte in the highest memory position.
     * @param order the order to use to decode the data.
     * @return the decoded value.
     */
    public static double decodeDouble(byte b0, byte b1, byte b2,
                                     byte b3, byte b4, byte b5,
                                     byte b6, byte b7, DataOrder order) {
        return Double.longBitsToDouble(decodeLong(b0, b1, b2, b3, b4, b5, b6, b7, order));
    }

    /**
     * Encodes a short to an array of bytes.
     * @param x the value to encode.
     * @param b the target array of bytes.
     * @param off the offset from the beginning of the byte array where the data
     *            will be written.
     * @param order the order that will be used to encode the data.
     */
    public static void encodeShort(short x, byte[] b, int off, DataOrder order) {
        switch (order) {
            case BIG_ENDIAN:
                b[off] = (byte) ((x >> 8) & 0xff);
                b[off + 1] = (byte) (x & 0xff);
                break;
            case LITTLE_ENDIAN:
                b[off + 1] = (byte) ((x >> 8) & 0xff);
                b[off] = (byte) (x & 0xff);
                break;
        }
    }

    /**
     * Encodes a char to an array of bytes.
     * @param x the value to encode.
     * @param b the target array of bytes.
     * @param off the offset from the beginning of the byte array where the data
     *            will be written.
     * @param order the order that will be used to encode the data.
     */
    public static void encodeChar(char x, byte[] b, int off, DataOrder order) {
        switch (order) {
            case BIG_ENDIAN:
                b[off] = (byte) ((x >> 8) & 0xff);
                b[off + 1] = (byte) (x & 0xff);
                break;
            case LITTLE_ENDIAN:
                b[off + 1] = (byte) ((x >> 8) & 0xff);
                b[off] = (byte) (x & 0xff);
                break;
        }
    }

    /**
     * Encodes an int to an array of bytes.
     * @param x the value to encode.
     * @param b the target array of bytes.
     * @param off the offset from the beginning of the byte array where the data
     *            will be written.
     * @param order the order that will be used to encode the data.
     */
    public static void encodeInt(int x, byte[] b, int off, DataOrder order) {
        switch (order) {
            case BIG_ENDIAN:
                b[off] = (byte) ((x >> 24) & 0xff);
                b[off + 1] = (byte) ((x >> 16) & 0xff);
                b[off + 2] = (byte) ((x >> 8) & 0xff);
                b[off + 3] = (byte) (x & 0xff);
                break;
            case LITTLE_ENDIAN:
                b[off + 3] = (byte) ((x >> 24) & 0xff);
                b[off + 2] = (byte) ((x >> 16) & 0xff);
                b[off + 1] = (byte) ((x >> 8) & 0xff);
                b[off] = (byte) (x & 0xff);
                break;
        }
    }

    /**
     * Encodes a long to an array of bytes.
     * @param x the value to encode.
     * @param b the target array of bytes.
     * @param off the offset from the beginning of the byte array where the data
     *            will be written.
     * @param order the order that will be used to encode the data.
     */
    public static void encodeLong(long x, byte[] b, int off, DataOrder order) {
        switch (order) {
            case BIG_ENDIAN:
                b[off] = (byte) ((x >> 56) & 0xff);
                b[off + 1] = (byte) ((x >> 48) & 0xff);
                b[off + 2] = (byte) ((x >> 40) & 0xff);
                b[off + 3] = (byte) ((x >> 32) & 0xff);
                b[off + 4] = (byte) ((x >> 24) & 0xff);
                b[off + 5] = (byte) ((x >> 16) & 0xff);
                b[off + 6] = (byte) ((x >> 8) & 0xff);
                b[off + 7] = (byte) (x & 0xff);
                break;
            case LITTLE_ENDIAN:
                b[off + 7] = (byte) ((x >> 56) & 0xff);
                b[off + 6] = (byte) ((x >> 48) & 0xff);
                b[off + 5] = (byte) ((x >> 40) & 0xff);
                b[off + 4] = (byte) ((x >> 32) & 0xff);
                b[off + 3] = (byte) ((x >> 24) & 0xff);
                b[off + 2] = (byte) ((x >> 16) & 0xff);
                b[off + 1] = (byte) ((x >> 8) & 0xff);
                b[off] = (byte) (x & 0xff);
                break;
        }
    }

    /**
     * Encodes a float to an array of bytes.
     * @param x the value to encode.
     * @param b the target array of bytes.
     * @param off the offset from the beginning of the byte array where the data
     *            will be written.
     * @param order the order that will be used to encode the data.
     */
    public static void encodeFloat(float x, byte[] b, int off, DataOrder order) {
        encodeInt(Float.floatToIntBits(x), b, off, order);
    }

    /**
     * Encodes a double to an array of bytes.
     * @param x the value to encode.
     * @param b the target array of bytes.
     * @param off the offset from the beginning of the byte array where the data
     *            will be written.
     * @param order the order that will be used to encode the data.
     */
    public static void encodeDouble(double x, byte[] b, int off, DataOrder order) {
        encodeLong(Double.doubleToLongBits(x), b, off, order);
    }
}
