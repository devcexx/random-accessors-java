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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RandomAccessorTest {

    @Test
    public void testSlicing() {
        RandomAccessor accessor = new RandomAccessor(Sources.alloc(32));

        for (int i = 0; i < 32; i++) {
            accessor.put(i, (byte) i);
        }

        for (int i = 0; i < 32; i++) {
            accessor.position(i);
            RandomAccessor slice = accessor.slice();

            assertEquals(32 - i, slice.length());
            assertEquals(i, slice.get(0));
        }

        accessor.source().dealloc();
    }

    @Test
    public void testPositioning() {
        RandomAccessor acc1 = new RandomAccessor(Sources.alloc(5), 3);
        RandomAccessor acc2 = new RandomAccessor(Sources.alloc(5), 5);
        RandomAccessor acc3 = new RandomAccessor(Sources.alloc(5), 1, 2);

        assertEquals(3, acc1.position());
        assertEquals(5, acc2.position());
        assertEquals(1, acc3.position());

        assertThrows(IllegalArgumentException.class, () -> new RandomAccessor(Sources.alloc(5), 6));

        RandomAccessor accessor = new RandomAccessor(Sources.alloc(512));
        assertEquals(0, accessor.position());

        //As there are so many overloads for the put method
        //we are going to check the most part of them, because
        //there's a high chance of having a mistake in one
        //of them.

        //Check the calls of the style: #put(T)
        //(should increment the position of the accessor by the number of bytes read)

        accessor.put((byte) 0);
        assertEquals(1, accessor.position());

        accessor.put((char) 0);
        assertEquals(3, accessor.position());

        accessor.put((short) 0);
        assertEquals(5, accessor.position());

        accessor.put(0);
        assertEquals(9, accessor.position());

        accessor.put(0L);
        assertEquals(17, accessor.position());

        accessor.put(0.0f);
        assertEquals(21, accessor.position());

        accessor.put(0.0);
        assertEquals(29, accessor.position());

        //-----

        accessor.position(0);
        assertEquals(0, accessor.position());

        //-----

        //Check the calls of the style: #put(T, order)
        //(should increment the position of the accessor by the number of bytes read)

        accessor.put((char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(2, accessor.position());

        accessor.put((short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(4, accessor.position());

        accessor.put(0, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(8, accessor.position());

        accessor.put(0L, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(16, accessor.position());

        accessor.put(0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(20, accessor.position());

        accessor.put(0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(28, accessor.position());

        //-----

        accessor.position(0);
        assertEquals(0, accessor.position());

        //-----

        //Check the calls of the style: #put(offset, T)
        //(should NOT increment the position of the accessor)

        accessor.put(0, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.put(0, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.put(0, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.put(0, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.put(0, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.put(0, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        //-----

        accessor.position(0);
        assertEquals(0, accessor.position());

        //-----

        //Check the calls of the style: #get([T], srcOff, len)
        //(should increment the position of the accessor by the number of bytes read)

        accessor.put(new byte[16], 0, 16);
        assertEquals(16, accessor.position());

        accessor.put(new char[16], 0, 16);
        assertEquals(48, accessor.position());

        accessor.put(new short[16], 0, 16);
        assertEquals(80, accessor.position());

        accessor.put(new int[16], 0, 16);
        assertEquals(144, accessor.position());

        accessor.put(new long[16], 0, 16);
        assertEquals(272, accessor.position());

        accessor.put(new float[16], 0, 16);
        assertEquals(336, accessor.position());

        accessor.put(new double[16], 0, 16);
        assertEquals(464, accessor.position());

        //-----

        accessor.position(0);
        assertEquals(0, accessor.position());

        //-----

        //Check the calls of the style: #get(off, [T], srcOff, len)
        //(should NOT increment the position of the accessor)

        accessor.put(0, new byte[16], 0, 16);
        assertEquals(0, accessor.position());

        accessor.put(0, new char[16], 0, 16);
        assertEquals(0, accessor.position());

        accessor.put(0, new short[16], 0, 16);
        assertEquals(0, accessor.position());

        accessor.put(0, new int[16], 0, 16);
        assertEquals(0, accessor.position());

        accessor.put(0, new long[16], 0, 16);
        assertEquals(0, accessor.position());

        accessor.put(0, new float[16], 0, 16);
        assertEquals(0, accessor.position());

        accessor.put(0, new double[16], 0, 16);
        assertEquals(0, accessor.position());

        //-----

        accessor.position(0);
        assertEquals(0, accessor.position());

        //-----

        //Check the calls of the style: #get([T], srcOff, len, order)
        //(should increment the position of the accessor by the number of bytes read)

        accessor.put(new char[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(32, accessor.position());

        accessor.put(new short[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(64, accessor.position());

        accessor.put(new int[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(128, accessor.position());

        accessor.put(new long[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(256, accessor.position());

        accessor.put(new float[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(320, accessor.position());

        accessor.put(new double[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(448, accessor.position());

        //-----

        accessor.position(0);
        assertEquals(0, accessor.position());

        //-----

        //Check the calls of the style: #get(off, [T], srcOff, len, order)
        //(should NOT increment the position of the accessor)

        accessor.put(0, new char[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.put(0, new short[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.put(0, new int[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.put(0, new long[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.put(0, new float[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.put(0, new double[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS);
        assertEquals(0, accessor.position());

        accessor.source().dealloc();
    }

    @Test
    public void testLimits() {
        RandomAccessor acc1 = new RandomAccessor(Sources.alloc(5), 3);
        RandomAccessor acc2 = new RandomAccessor(Sources.alloc(5), 5);
        RandomAccessor acc3 = new RandomAccessor(Sources.alloc(5), 1, 2);

        assertEquals(5, acc1.limit());
        assertEquals(5, acc2.limit());
        assertEquals(2, acc3.limit());

        RandomAccessor accessor = new RandomAccessor(Sources.alloc(512));
        assertEquals(512, accessor.limit());

        accessor.limit(15);
        accessor.position(15);

        //Check the calls of the style: #put(T)
        //(should increment the position of the accessor by the number of bytes read)
        assertThrows(IllegalArgumentException.class, () -> accessor.put((byte) 0));
        assertThrows(IllegalArgumentException.class, () -> accessor.put((char) 0));
        assertThrows(IllegalArgumentException.class, () -> accessor.put((short) 0));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0L));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0.0f));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0.0));

        //-----

        assertEquals(15, accessor.position());

        //-----

        //Check the calls of the style: #put(T, order)
        //(should increment the position of the accessor by the number of bytes read)

        assertThrows(IllegalArgumentException.class, () -> accessor.put((char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put((short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        //-----

        assertEquals(15, accessor.position());
        accessor.limit(1);
        assertEquals(1, accessor.position());

        accessor.position(0);

        //-----

        //Check the calls of the style: #put(offset, T)
        //(should NOT increment the position of the accessor)

        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        //-----

        assertEquals(0, accessor.position());
        accessor.limit(15);
        assertEquals(0, accessor.position());

        //-----

        //Check the calls of the style: #get([T], srcOff, len)
        //(should increment the position of the accessor by the number of bytes read)

        assertThrows(IllegalArgumentException.class, () -> accessor.put(new byte[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new char[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new short[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new int[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new long[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new float[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new double[16], 0, 16));

        //-----

        assertEquals(0, accessor.position());

        //-----

        //Check the calls of the style: #get(off, [T], srcOff, len)
        //(should NOT increment the position of the accessor)

        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new byte[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new char[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new short[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new int[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new long[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new float[16], 0, 16));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new double[16], 0, 16));

        //-----

        assertEquals(0, accessor.position());

        //-----

        //Check the calls of the style: #get([T], srcOff, len, order)
        //(should increment the position of the accessor by the number of bytes read)

        assertThrows(IllegalArgumentException.class, () -> accessor.put(new char[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new short[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new int[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new long[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new float[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(new double[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        //-----

        assertEquals(0, accessor.position());

        //-----

        //Check the calls of the style: #get(off, [T], srcOff, len, order)
        //(should NOT increment the position of the accessor)

        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new char[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new short[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new int[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new long[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new float[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> accessor.put(0, new double[16], 0, 16, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertEquals(0, accessor.position());

        accessor.source().dealloc();
    }

    @Test
    public void paddingTest() {
        RandomAccessor accessor = new RandomAccessor(Sources.alloc(1024));

        assertEquals(0, accessor.padTo(4, (byte) 'x'));
        assertEquals(0, accessor.position());

        accessor.advance(1);

        assertEquals(3, accessor.padTo(4, (byte) 'x'));
        assertEquals(4, accessor.position());

        byte[] pad1 = new byte[3];
        Arrays.fill(pad1, (byte) 'x');
        assertArrayEquals(pad1, accessor.get(1, new byte[3]));

        accessor.advance(2);

        assertEquals(2, accessor.padTo(4, (byte) 'y'));
        assertEquals(8, accessor.position());

        byte[] pad2 = new byte[2];
        Arrays.fill(pad2, (byte) 'y');
        assertArrayEquals(pad2, accessor.get(6, new byte[2]));

        accessor.advance(1);

        assertEquals(7, accessor.padTo(8, (byte) 'w'));
        assertEquals(16, accessor.position());

        byte[] pad3 = new byte[7];
        Arrays.fill(pad3, (byte) 'w');
        assertArrayEquals(pad3, accessor.get(9, new byte[7]));
    }

    @Test
    public void alignTest() {
        RandomAccessor accessor = new RandomAccessor(Sources.alloc(1024));

        assertEquals(0, accessor.alignTo(4));
        assertEquals(0, accessor.position());

        accessor.advance(1);

        assertEquals(3, accessor.alignTo(4));
        assertEquals(4, accessor.position());

        accessor.advance(2);

        assertEquals(2, accessor.alignTo(4));
        assertEquals(8, accessor.position());

        accessor.advance(1);

        assertEquals(7, accessor.alignTo(8));
        assertEquals(16, accessor.position());
    }
}
