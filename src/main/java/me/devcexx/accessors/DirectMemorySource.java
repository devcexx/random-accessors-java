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
import java.nio.channels.Channel;

/**
 * Represents a memory block that can be directly read or written.
 */
public class DirectMemorySource extends RandomAccessSource {

    /**
     * Allocates a new memory block with the specified size and attaches a new {@link DirectMemorySource}
     * to it.
     * @param size the block size as a non negative long value.
     * @return a new {@link DirectMemorySource} attached to the just allocated memory region.
     *
     *
     */
    public static DirectMemorySource alloc(long size) {
        return new DirectMemorySource(UnsafeMemory.alloc(size), size);
    }

    /**
     * Allocates a new memory block with the specified size, filling it with zeroes,
     * and attaches a new {@link DirectMemorySource} to it.
     * @param size the block size as a non negative long value.
     * @return a new {@link DirectMemorySource} attached to the just allocated memory region.
     */
    public static DirectMemorySource calloc(long size) {
        return new DirectMemorySource(UnsafeMemory.allocAndSet(size, (byte) 0), size);
    }

    /**
     * Allocates a new memory block with the specified size, filling it with the given data,
     * and attaches a new {@link DirectMemorySource} to it.
     * @param size the block size as a non negative long value.
     * @param data the data that will be used to fill the new memory region.
     * @return a new {@link DirectMemorySource} attached to the just allocated memory region.
     */
    public static DirectMemorySource allocAndSet(long size, byte data) {
        return new DirectMemorySource(UnsafeMemory.allocAndSet(size, data), size);
    }

    protected final long address;

    /**
     * Creates an unsafe {@link DirectMemorySource} starting at the given address
     * with the given length.
     * @param address the base address of the memory block.
     * @param length the length of the memory block.
     */
    public DirectMemorySource(long address, long length) {
        super(length);
        this.address = address;
    }


    /**
     * The address of the underlying memory block in memory.
     */
    public long address() {
        return this.address;
    }

    @Override
    public ByteBuffer byteBuffer(long off, long length) {
        if (length > Integer.MAX_VALUE) {
            throw new IllegalStateException("The requested length for the byte buffer is too high. " +
                    "Java ByteBuffer API only allows lengths representable in 32-bit signed values.");
        }

        checkAbleToIO(off, length);

        //Create an association with the new byte buffer and thhe current source, so it is not deallocated
        //until all the byte buffers that is using it has been destroyed too.
        return UnsafeMemory.createDirectBuffer(address + off, length, this);
    }

    @Override
    public void clear(byte x, long off, long length) {
        Validate.checkInRange(this.length(), off, length);
        UnsafeMemory.memset(address + off, length, x);
    }

    @Override
    public byte unsafeGet(long off) {
        return UnsafeMemory.getByte(address + off);
    }

    @Override
    public short unsafeGetShort(long off, MemoryAccessorOrder order) {
        return UnsafeMemory.getShort(address + off, order);
    }

    @Override
    public char unsafeGetChar(long off, MemoryAccessorOrder order) {
        return UnsafeMemory.getChar(address + off, order);
    }

    @Override
    public int unsafeGetInt(long off, MemoryAccessorOrder order) {
        return UnsafeMemory.getInt(address + off, order);
    }

    @Override
    public long unsafeGetLong(long off, MemoryAccessorOrder order) {
        return UnsafeMemory.getLong(address + off, order);
    }

    @Override
    public float unsafeGetFloat(long off, MemoryAccessorOrder order) {
        return UnsafeMemory.getFloat(address + off, order);
    }

    @Override
    public double unsafeGetDouble(long off, MemoryAccessorOrder order) {
        return UnsafeMemory.getDouble(address + off, order);
    }

    @Override
    public void unsafeGet(long off, byte[] buffer, int dstOff, int len) {
        readToArray(off, buffer, 1, dstOff, len, MemoryAccessorOrder.NATIVE_ENDIANNESS);
    }

    @Override
    public void unsafeGet(long off, ByteBuffer buf) {
        if (buf.isDirect()) {
            UnsafeMemory.copyMemBlockToAddress(address + off,
                    UnsafeMemory.addressOfByteBuffer(buf) + buf.position(),
                    buf.remaining());
        } else {
            get(off, buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
        }
        buf.position(buf.position() + buf.remaining());
    }

    @Override
    public void unsafeGet(long off, char[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        readToArray(off, buffer, 2, dstOff, len, order);
    }

    @Override
    public void unsafeGet(long off, short[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        readToArray(off, buffer, 2, dstOff, len, order);
    }

    @Override
    public void unsafeGet(long off, int[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        readToArray(off, buffer, 4, dstOff, len, order);
    }

    @Override
    public void unsafeGet(long off, long[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        readToArray(off, buffer, 8, dstOff, len, order);
    }

    @Override
    public void unsafeGet(long off, float[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        readToArray(off, buffer, 4, dstOff, len, order);
    }

    @Override
    public void unsafeGet(long off, double[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        readToArray(off, buffer, 8, dstOff, len, order);
    }

    @Override
    public void unsafePut(long off, byte value) {
        UnsafeMemory.putByte(address + off, value);
    }

    @Override
    public void unsafePut(long off, short value, MemoryAccessorOrder order) {
        UnsafeMemory.putShort(address + off, value, order);
    }

    @Override
    public void unsafePut(long off, char value, MemoryAccessorOrder order) {
        UnsafeMemory.putChar(address + off, value, order);
    }

    @Override
    public void unsafePut(long off, int value, MemoryAccessorOrder order) {
        UnsafeMemory.putInt(address + off, value, order);
    }

    @Override
    public void unsafePut(long off, long value, MemoryAccessorOrder order) {
        UnsafeMemory.putLong(address + off, value, order);
    }

    @Override
    public void unsafePut(long off, float value, MemoryAccessorOrder order) {
        UnsafeMemory.putFloat(address + off, value, order);
    }

    @Override
    public void unsafePut(long off, double value, MemoryAccessorOrder order) {
        UnsafeMemory.putDouble(address + off, value, order);
    }

    @Override
    public void unsafePut(long off, byte[] buffer, int srcOff, int len) {
        writeFromArray(srcOff, buffer, 1, off, len, MemoryAccessorOrder.NATIVE_ENDIANNESS);
    }

    @Override
    public void unsafePut(long off, ByteBuffer buf) {
        if (buf.isDirect()) {
            UnsafeMemory.copyMemBlockToAddress(UnsafeMemory.addressOfByteBuffer(buf) + buf.position(),
                    address + off,
                    buf.remaining());
        } else {
            put(off, buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
        }
        buf.position(buf.position() + buf.remaining());
    }

    @Override
    public void unsafePut(long off, short[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        writeFromArray(srcOff, buffer, 2, off, len, order);
    }

    @Override
    public void unsafePut(long off, char[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        writeFromArray(srcOff, buffer, 2, off, len, order);
    }

    @Override
    public void unsafePut(long off, int[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        writeFromArray(srcOff, buffer, 4, off, len, order);
    }

    @Override
    public void unsafePut(long off, long[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        writeFromArray(srcOff, buffer, 8, off, len, order);
    }

    @Override
    public void unsafePut(long off, float[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        writeFromArray(srcOff, buffer, 4, off, len, order);
    }

    @Override
    public void unsafePut(long off, double[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        writeFromArray(srcOff, buffer, 8, off, len, order);
    }

    @Override
    public void dealloc() {
        if (!deallocated) {
            UnsafeMemory.dealloc(address);
            deallocated = true;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        dealloc();
        super.finalize();
    }

    protected void readToArray(long srcOff, Object buf, int dataSize, int dstOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyMemBlockToArray(address + srcOff, buf, dstOff, dataSize, len, order);
    }

    protected void writeFromArray(int srcOff, Object buf, int dataSize, long dstOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToAddress(buf, srcOff, address + dstOff, dataSize, len, order);
    }
}
