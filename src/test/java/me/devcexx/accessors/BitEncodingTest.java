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

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BitEncodingTest {
    private void testShortEncode(short x, MemoryAccessorOrder order) {
        byte[] b = new byte[2];
        Bits.encodeShort(x, b, 0, order);

        byte[] b1 = new byte[2];
        ByteBuffer.wrap(b1).order(order.nioOrder).putShort(x);

        assertArrayEquals(b1, b);
    }

    private void testCharEncode(char x, MemoryAccessorOrder order) {
        byte[] b = new byte[2];
        Bits.encodeChar(x, b, 0, order);

        byte[] b1 = new byte[2];
        ByteBuffer.wrap(b1).order(order.nioOrder).putChar(x);

        assertArrayEquals(b1, b);
    }

    private void testIntEncode(int x, MemoryAccessorOrder order) {
        byte[] b = new byte[4];
        Bits.encodeInt(x, b, 0, order);

        byte[] b1 = new byte[4];
        ByteBuffer.wrap(b1).order(order.nioOrder).putInt(x);

        assertArrayEquals(b1, b);
    }

    private void testLongEncode(long x, MemoryAccessorOrder order) {
        byte[] b = new byte[8];
        Bits.encodeLong(x, b, 0, order);

        byte[] b1 = new byte[8];
        ByteBuffer.wrap(b1).order(order.nioOrder).putLong(x);

        assertArrayEquals(b1, b);
    }

    private void testFloatEncode(float x, MemoryAccessorOrder order) {
        byte[] b = new byte[4];
        Bits.encodeFloat(x, b, 0, order);

        byte[] b1 = new byte[4];
        ByteBuffer.wrap(b1).order(order.nioOrder).putFloat(x);

        assertArrayEquals(b1, b);
    }

    private void testDoubleEncode(double x, MemoryAccessorOrder order) {
        byte[] b = new byte[8];
        Bits.encodeDouble(x, b, 0, order);

        byte[] b1 = new byte[8];
        ByteBuffer.wrap(b1).order(order.nioOrder).putDouble(x);

        assertArrayEquals(b1, b);
    }

    private void testShortDecode(byte[] data, MemoryAccessorOrder order) {
        short x = Bits.decodeShort(data[0], data[1], order);
        short x1 = ByteBuffer.wrap(data).order(order.nioOrder).getShort();

        assertEquals(x1, x);
    }

    private void testCharDecode(byte[] data, MemoryAccessorOrder order) {
        char x = Bits.decodeChar(data[0], data[1], order);
        char x1 = ByteBuffer.wrap(data).order(order.nioOrder).getChar();

        assertEquals(x1, x);
    }

    private void testIntDecode(byte[] data, MemoryAccessorOrder order) {
        int x = Bits.decodeInt(data[0], data[1], data[2], data[3], order);
        int x1 = ByteBuffer.wrap(data).order(order.nioOrder).getInt();

        assertEquals(x1, x);
    }

    private void testLongDecode(byte[] data, MemoryAccessorOrder order) {
        long x = Bits.decodeShort(data[0], data[1], order);
        long x1 = ByteBuffer.wrap(data).order(order.nioOrder).getShort();

        assertEquals(x1, x);
    }

    private void testFloatDecode(byte[] data, MemoryAccessorOrder order) {
        float x = Bits.decodeFloat(data[0], data[1], data[2], data[3], order);
        float x1 = ByteBuffer.wrap(data).order(order.nioOrder).getFloat();

        assertEquals(x1, x, 0.1);
    }

    private void testDoubleDecode(byte[] data, MemoryAccessorOrder order) {
        double x = Bits.decodeDouble(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], order);
        double x1 = ByteBuffer.wrap(data).order(order.nioOrder).getDouble();

        assertEquals(x1, x, 0.1);
    }

    @Test
    public void testEncodingShort() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing short encoding in " + order);
            testShortEncode((short) 0, order);
            testShortEncode((short) -2000, order);
            testShortEncode((short) 2000, order);
            testShortEncode((short) -100, order);
            testShortEncode((short) 100, order);
            testShortEncode(Short.MAX_VALUE, order);
            testShortEncode(Short.MIN_VALUE, order);
            testShortEncode((short) (Short.MAX_VALUE - 1), order);
            testShortEncode((short) (Short.MIN_VALUE + 1), order);
        }
    }

    @Test
    public void testEncodingChar() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing char encoding in " + order);
            testCharEncode((char) 0, order);
            testCharEncode((char) 2000, order);
            testCharEncode((char) 100, order);
            testCharEncode((char) 32768, order);
            testCharEncode(Character.MAX_VALUE, order);
            testCharEncode((char) (Character.MAX_VALUE - 1), order);
        }
    }

    @Test
    public void testEncodingInt() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing int encoding in " + order);
            testIntEncode(0, order);
            testIntEncode(100, order);
            testIntEncode(-100, order);
            testIntEncode(12345678, order);
            testIntEncode(-12345678, order);
            testIntEncode(1234567890, order);
            testIntEncode(-1234567890, order);
            testIntEncode(Integer.MIN_VALUE, order);
            testIntEncode(Integer.MAX_VALUE, order);
            testIntEncode(Integer.MIN_VALUE + 1, order);
            testIntEncode(Integer.MAX_VALUE - 1, order);
        }
    }

    @Test
    public void testEncodingLong() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing long encoding in " + order);
            testLongEncode(0, order);
            testLongEncode(100, order);
            testLongEncode(-100, order);
            testLongEncode(12345678, order);
            testLongEncode(-12345678, order);
            testLongEncode(1234567890, order);
            testLongEncode(-1234567890, order);
            testLongEncode(1234567890123456789L, order);
            testLongEncode(-1234567890123456789L, order);
            testLongEncode(Long.MIN_VALUE, order);
            testLongEncode(Long.MAX_VALUE, order);
            testLongEncode(Long.MIN_VALUE + 1, order);
            testLongEncode(Long.MAX_VALUE - 1, order);
        }
    }

    @Test
    public void testEncodingFloat() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing float encoding in " + order);
            testFloatEncode(0.0f, order);
            testFloatEncode(123.45678f, order);
            testFloatEncode(-123.45678f, order);
            testFloatEncode(12345678.9012345f, order);
            testFloatEncode(-12345678.9012345f, order);
            testFloatEncode(Float.MIN_VALUE, order);
            testFloatEncode(Float.MAX_VALUE, order);
            testFloatEncode(Float.POSITIVE_INFINITY, order);
            testFloatEncode(Float.NEGATIVE_INFINITY, order);
            testFloatEncode(Float.NaN, order);
        }
    }

    @Test
    public void testEncodingDouble() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing double encoding in " + order);
            testDoubleEncode(0.0f, order);
            testDoubleEncode(123.45678f, order);
            testDoubleEncode(-123.45678f, order);
            testDoubleEncode(12345678.9012345f, order);
            testDoubleEncode(-12345678.9012345f, order);
            testDoubleEncode(Double.MIN_VALUE, order);
            testDoubleEncode(Double.MAX_VALUE, order);
            testDoubleEncode(Double.POSITIVE_INFINITY, order);
            testDoubleEncode(Double.NEGATIVE_INFINITY, order);
            testDoubleEncode(Double.NaN, order);
        }
    }

    @Test
    public void testDecodingShort() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing short decoding in " + order);
            testShortDecode(new byte[] {0, 0}, order);
            testShortDecode(new byte[] {0, -1}, order);
            testShortDecode(new byte[] {5, 60}, order);
            testShortDecode(new byte[] {-1, -1}, order);
            testShortDecode(new byte[] {-128, 0}, order);
        }
    }

    @Test
    public void testDecodingChar() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing short decoding in " + order);
            testCharDecode(new byte[] {0, 0}, order);
            testCharDecode(new byte[] {0, -1}, order);
            testCharDecode(new byte[] {5, 60}, order);
            testCharDecode(new byte[] {-1, -1}, order);
            testCharDecode(new byte[] {-128, 0}, order);
        }
    }

    @Test
    public void testDecodingInt() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing int decoding in " + order);
            testIntDecode(new byte[] {0, 0, 0, 0}, order);
            testIntDecode(new byte[] {0, -1, -1, -1}, order);
            testIntDecode(new byte[] {-1, 0, 0, 0}, order);
            testIntDecode(new byte[] {-128, 0, 0, 0}, order);
            testIntDecode(new byte[] {60, 25, 7, 1}, order);
        }
    }

    @Test
    public void testDecodingLong() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing long decoding in " + order);
            testLongDecode(new byte[] {0, 0, 0, 0, 0, 0, 0}, order);
            testLongDecode(new byte[] {0, -1, -1, -1, -1, 56, 6, 1}, order);
            testLongDecode(new byte[] {-1, 0, 0, 0, 0, 0, 0, 0}, order);
            testLongDecode(new byte[] {-128, 0, 0, 0, 0, 0, 0, 0}, order);
            testLongDecode(new byte[] {-5, 25, 7, 1, 8, 1, 67, 99}, order);
        }
    }

    @Test
    public void testDecodingFloat() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing float decoding in " + order);
            testFloatDecode(new byte[] {0, 0, 0, 0}, order);
            testFloatDecode(new byte[] {0, -1, -1, -1}, order);
            testFloatDecode(new byte[] {-1, 0, 0, 0}, order);
            testFloatDecode(new byte[] {-128, 0, 0, 0}, order);
            testFloatDecode(new byte[] {60, 25, 7, 1}, order);
        }
    }

    @Test
    public void testDecodingDouble() {
        for (MemoryAccessorOrder order : MemoryAccessorOrder.values()) {
            System.out.println("Testing double decoding in " + order);
            testDoubleDecode(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}, order);
            testDoubleDecode(new byte[] {0, -1, -1, -1, -1, 56, 6, 1}, order);
            testDoubleDecode(new byte[] {-1, 0, 0, 0, 0, 0, 0, 0}, order);
            testDoubleDecode(new byte[] {-128, 0, 0, 0, 0, 0, 0, 0}, order);
            testDoubleDecode(new byte[] {-5, 25, 7, 1, 8, 1, 67, 99}, order);
        }
    }
}
