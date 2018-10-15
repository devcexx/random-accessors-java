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

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class DirectMemorySourceTest {
    public void testScalars(MemoryAccessorOrder order) {
        DirectMemorySource source = DirectMemorySource.alloc(29);
        Random random = new Random();

        byte byteVal = (byte) random.nextInt();
        char charVal = (char) random.nextInt();
        short shortVal = (short) random.nextInt();
        int intVal = random.nextInt();
        long longVal = random.nextLong();
        float floatVal = random.nextFloat();
        double doubleVal = random.nextDouble();

        source.put(0, byteVal);
        source.put(1, charVal, order);
        source.put(3, shortVal, order);
        source.put(5, intVal, order);
        source.put(9, longVal, order);
        source.put(17, floatVal, order);
        source.put(21, doubleVal, order);

        assertEquals(byteVal, source.get(0));
        assertEquals(charVal, source.getChar(1, order));
        assertEquals(shortVal, source.getShort(3, order));
        assertEquals(intVal, source.getInt(5, order));
        assertEquals(longVal, source.getLong(9, order));
        assertEquals(Float.floatToRawIntBits(floatVal), Float.floatToRawIntBits(source.getFloat(17, order)));
        assertEquals(Double.doubleToRawLongBits(doubleVal), Double.doubleToRawLongBits(source.getDouble(21, order)));

        source.dealloc();
    }


    private void testBuffers(MemoryAccessorOrder order) {
        DirectMemorySource source = DirectMemorySource.alloc(145);

        Random random = new Random();

        byte[] byteVals = new byte[5];
        random.nextBytes(byteVals);
        char[] charVals = { (char) random.nextInt(),  (char) random.nextInt(),  (char) random.nextInt(),  (char) random.nextInt(),  (char) random.nextInt()};
        short[] shortVals = { (short) random.nextInt(), (short) random.nextInt(), (short) random.nextInt(), (short) random.nextInt(), (short) random.nextInt()};
        int[] intVals = { random.nextInt(), random.nextInt(), random.nextInt(), random.nextInt(), random.nextInt()};
        long[] longVals = { random.nextLong(), random.nextLong(), random.nextLong(), random.nextLong(), random.nextLong()};
        int[] floatVals = { Float.floatToRawIntBits(random.nextFloat()), Float.floatToRawIntBits(random.nextFloat()),
                Float.floatToRawIntBits(random.nextFloat()), Float.floatToRawIntBits(random.nextFloat()), Float.floatToRawIntBits(random.nextFloat())};
        long[] doubleVals = { Double.doubleToRawLongBits(random.nextDouble()), Double.doubleToRawLongBits(random.nextDouble()),
                Double.doubleToRawLongBits(random.nextDouble()), Double.doubleToRawLongBits(random.nextDouble()), Double.doubleToRawLongBits(random.nextDouble())};

        source.put(0, byteVals);
        source.put(5, charVals, order);
        source.put(15, shortVals, order);
        source.put(25, intVals, order);
        source.put(45, longVals, order);
        source.put(85, floatVals, order);
        source.put(105, doubleVals, order);

        assertArrayEquals(byteVals, source.get(0, new byte[5]));
        assertArrayEquals(charVals, source.get(5, new char[5], order));
        assertArrayEquals(shortVals, source.get(15, new short[5], order));
        assertArrayEquals(intVals, source.get(25, new int[5], order));
        assertArrayEquals(longVals, source.get(45, new long[5], order));
        assertArrayEquals(floatVals, source.get(85, new int[5], order));
        assertArrayEquals(doubleVals, source.get(105, new long[5], order));

        source.dealloc();
    }

    @Test
    public void testNativeEndiannessScalars() {
        testScalars(MemoryAccessorOrder.NATIVE_ENDIANNESS);
    }

    @Test
    public void testNonNativeEndiannessScalars() {
        testScalars(MemoryAccessorOrder.NATIVE_ENDIANNESS.opposite());
    }

    @Test
    public void testNativeEndiannessBuffers() {
        testBuffers(MemoryAccessorOrder.NATIVE_ENDIANNESS);
    }
    @Test
    public void testNonNativeEndiannessBuffers() {
        testBuffers(MemoryAccessorOrder.NATIVE_ENDIANNESS.opposite());
    }

    @Test
    public void testSlicing() {
        DirectMemorySource source = DirectMemorySource.alloc(32);
        for (int i = 0; i < 32; i++) {
            source.put(i, (byte) i);
        }

        for (int i = 0; i < 32; i++) {
            RandomAccessSource slice = source.slice(i);

            assertEquals(32 - i, slice.length());
            assertEquals(i, slice.get(0));
        }

        source.dealloc();
    }

    @Test
    public void testOverflow() {
        DirectMemorySource source = DirectMemorySource.alloc(128);

        assertThrows(IllegalArgumentException.class, () -> source.put(129, (byte) 0));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, (byte) 0));
        assertDoesNotThrow(() -> source.put(127, (byte) 0));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(126, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(126, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(126, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(125, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(124, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(126, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(125, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(124, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(123, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(122, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(121, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(120, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(126, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(125, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(124, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(126, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(125, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(124, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(123, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(122, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(121, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(120, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
    }

    @Test
    public void testDeallocationState() {
        DirectMemorySource source = DirectMemorySource.alloc(128);

        source.dealloc();

        assertThrows(IllegalStateException.class, () -> source.put(5, (byte) 0));
        assertThrows(IllegalStateException.class, () -> source.put(5, (byte) 0));
        assertThrows(IllegalStateException.class, () -> source.put(5, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalStateException.class, () -> source.put(5, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalStateException.class, () -> source.put(5, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalStateException.class, () -> source.put(5, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalStateException.class, () -> source.put(5, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalStateException.class, () -> source.put(5, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
    }

}
