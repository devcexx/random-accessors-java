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

import sun.nio.ch.DirectBuffer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class UnsafeMemory {
    public static final sun.misc.Unsafe UNSAFE;
    public static final Constructor<? extends ByteBuffer> BYTE_BUF_CTOR;

    static {
        try {
            Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (sun.misc.Unsafe) theUnsafe.get(null);
            BYTE_BUF_CTOR = (Constructor<? extends ByteBuffer>) Class.forName("java.nio.DirectByteBuffer")
                    .getDeclaredConstructor(long.class, int.class, Object.class);
            BYTE_BUF_CTOR.setAccessible(true);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Cannot access AccessorsUtils");
        }
    }

    public static ByteBuffer createDirectBuffer(long addr, long length, Object obj) {
        try {
            if (length > Integer.MAX_VALUE) {
                throw new BufferOverflowException();
            }

            return BYTE_BUF_CTOR.newInstance(addr, (int) length, obj);
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate DirectByteBuffer: " + e);
        }
    }

    public static long addressOfByteBuffer(ByteBuffer buf) {
        if (!(buf instanceof DirectBuffer)) {
            throw new IllegalArgumentException("Given ByteBuffer is not a direct memory buffer");
        }
        return ((DirectBuffer) buf).address();
    }

    public static int wordSize() {
        return UNSAFE.addressSize();
    }

    public static boolean getBoolean(long address) {
        return UNSAFE.getByte(address) == 1;
    }

    public static byte getByte(long address) {
        return UNSAFE.getByte(address);
    }

    public static short getShort(long address, MemoryAccessorOrder order) {
        short s = UNSAFE.getShort(address);
        if (!order.isNative()) {
            s = Short.reverseBytes(s);
        }
        return s;
    }

    public static char getChar(long address, MemoryAccessorOrder order) {
        char c = UNSAFE.getChar(address);
        if (!order.isNative()) {
            c = Character.reverseBytes(c);
        }
        return c;
    }

    public static int getInt(long address, MemoryAccessorOrder order) {
        int i = UNSAFE.getInt(address);
        if (!order.isNative()) {
            i = Integer.reverseBytes(i);
        }
        return i;
    }

    public static long getLong(long address, MemoryAccessorOrder order) {
        long l = UNSAFE.getLong(address);
        if (!order.isNative()) {
            l = Long.reverseBytes(l);
        }
        return l;
    }

    public static float getFloat(long address, MemoryAccessorOrder order) {
        if (order.isNative()) {
            return UNSAFE.getFloat(address);
        } else {
            int in = UNSAFE.getInt(address);
            in = Integer.reverseBytes(in);
            return Float.intBitsToFloat(in);
        }
    }

    public static double getDouble(long address, MemoryAccessorOrder order) {
        if (order.isNative()) {
            return UNSAFE.getDouble(address);
        } else {
            long in = UNSAFE.getLong(address);
            in = Long.reverseBytes(in);
            return Double.longBitsToDouble(in);
        }
    }

    public static void putBoolean(long address, boolean value) {
        UNSAFE.putByte(address, (byte) (value ? 1 : 0));
    }

    public static void putByte(long address, byte value) {
        UNSAFE.putByte(address, value);
    }

    public static void putShort(long address, short value, MemoryAccessorOrder order) {
        if (!order.isNative()) {
            value = Short.reverseBytes(value);
        }
        UNSAFE.putShort(address, value);
    }

    public static void putChar(long address, char value, MemoryAccessorOrder order) {
        if (!order.isNative()) {
            value = Character.reverseBytes(value);
        }
        UNSAFE.putChar(address, value);
    }

    public static void putInt(long address, int value, MemoryAccessorOrder order) {
        if (!order.isNative()) {
            value = Integer.reverseBytes(value);
        }
        UNSAFE.putInt(address, value);
    }

    public static void putLong(long address, long value, MemoryAccessorOrder order) {
        if (!order.isNative()) {
            value = Long.reverseBytes(value);
        }
        UNSAFE.putLong(address, value);
    }

    public static void putFloat(long address, float value, MemoryAccessorOrder order) {
        if (order.isNative()) {
            UNSAFE.putFloat(address, value);
        } else {
            UNSAFE.putInt(address, Integer.reverseBytes(Float.floatToRawIntBits(value)));
        }
    }

    public static void putDouble(long address, double value, MemoryAccessorOrder order) {
        if (order.isNative()) {
            UNSAFE.putDouble(address, value);
        } else {
            UNSAFE.putLong(address, Long.reverseBytes(Double.doubleToRawLongBits(value)));
        }
    }

    public static void copyMemBlockToArray(long srcAddr, Object dstArray, int dstOff, int dataSize, long count, MemoryAccessorOrder order) {
        AccessorNatives.copyMemory(null, srcAddr, 0, dstArray, 0, dstOff * dataSize, dataSize, count, order.id, MemoryAccessorOrder.NATIVE_ENDIANNESS.id);
    }

    public static void copyMemBlockToAddress(long srcAddr, long dstAddr, long count) {
        AccessorNatives.copyMemory(null, srcAddr, 0, null, dstAddr, 0, 1, count, 0, 0);
    }

    public static void copyArrayToAddress(Object srcArray, int srcOff, long dstAddress, int dataSize, long count, MemoryAccessorOrder order) {
        AccessorNatives.copyMemory(srcArray, 0, srcOff, null, dstAddress, 0, dataSize, count, MemoryAccessorOrder.NATIVE_ENDIANNESS.id, order.id);
    }

    public static long alloc(long size) {
        return UNSAFE.allocateMemory(size);
    }

    public static long allocAndSet(long size, byte data) {
        long addr = UNSAFE.allocateMemory(size);
        UNSAFE.setMemory(addr, size, data);
        return addr;
    }

    public static void memset(long address, long length, byte x) {
        UNSAFE.setMemory(address, length, x);
    }

    public static void dealloc(long addr) {
        if (addr == 0)
            return;

        UNSAFE.freeMemory(addr);
    }
}
