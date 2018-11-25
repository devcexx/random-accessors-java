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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Represents an access source that can read or write data from anywhere of the
 * underlying data storage at any time.
 */
public abstract class RandomAccessSource {
    protected final long length;
    protected boolean deallocated;

    protected RandomAccessSource(long length) {
        this.length = length;
        if (length < 0) {
            throw new IllegalArgumentException("Length cannot be less than 0");
        }
    }

    /**
     * Gets the address of the memory block accessed by this instance.
     * @return a long value indicating the address of the memory block.
     */
    public final long length() {
        return length;
    }

    /**
     * Gets whether the current source has been deallocated or not
     */
    public boolean deallocated() {
        return deallocated;
    }

    /**
     * Gets whether the current source is still valid to be accessed.
     * The default implementation of the method returns that the buffer
     * is valid if and only if it has not been deallocated from the memory.
     */
    public boolean isValid() {
        return !deallocated;
    }

    /**
     * Returns the permissions that are being applied to all the operations over
     * this accessor.
     * @see AccessorPermissions
     */
    public int getPermissions() {
        return AccessorPermissions.READ | AccessorPermissions.WRITE;
    }

    /**
     * Checks whether the user is able to read from this accessor.
     * @return true if so, false otherwise.
     */
    public final boolean isReadable() {
        return AccessorPermissions.isReadable(getPermissions());
    }

    /**
     * Checks whether the user is able to write from this accessor.
     * @return true if so, false otherwise.
     */
    public final boolean isWritable() {
        return AccessorPermissions.isWritable(getPermissions());
    }

    /**
     * Creates a new source backed on the current one, updating its
     * access permissions.
     *
     * @param mask The permission mask that will be used to compute the
     *             final permissions of the new accessor. The final permissions
     *             are computed performing an AND operation between the
     *             permissions of the current accessor and the given mask.
     *             This implies that the built accessor can only have the same,
     *             or less, permissions than the current one.
     */
    public RandomAccessSource withPermissions(int mask) {
        return new SlicedSource(this, 0, length(), mask);
    }

    /**
     * Creates a sliced view from the current source.
     * The returned source can only safely access to the data
     * from the specified start offset to the specified end offset.
     * @param from The offset from the current source beginning that will be the
     *             position 0 on the sliced source, inclusive.
     * @param to The offset from the current source beginning that will be
     *           the end position of the sliced source, exclusive.
     */
    public RandomAccessSource sliceRange(long from, long to) {
        return slice(from, to - from);
    }

    /**
     * Creates a sliced view from the current source.
     * The returned source can only safely access to the data
     * from the specified offset to the end of the current source.
     * @param off The offset from the current source beginning that will be the
     *            position 0 on the sliced source, inclusive.
     */
    public RandomAccessSource slice(long off) {
        return slice(off, length - off);
    }

    /**
     * Creates a sliced view from the current source.
     * The returned source can only safely access to the data
     * from the specified offset with the specified length.
     * @param off The offset from the current source beginning that will be the
     *            position 0 on the sliced source, inclusive.
     * @param length the length of the new source.
     */
    public RandomAccessSource slice(long off, long length) {
        checkAbleToIO(off, length);
        return new SlicedSource(this, off, length);
    }

    /**
     * Returns the current source as a Java NIO Byte Buffer.
     */
    public ByteBuffer byteBuffer() {
        return byteBuffer(0, length());
    }

    /**
     * When overriden, returns, if possible, a Byte Buffer that is able
     * to directly read and write from the current source.
     * @param off the offset from the beginning of the current source where
     *            the byte buffer will start, inclusive.
     * @param length the length of the byte buffer.
     */
    public abstract ByteBuffer byteBuffer(long off, long length);

    /**
     * Creates an input stream backed on the current source.
     */
    public InputStream inputStream() {
        return inputStream(0, length);
    }

    /**
     * Creates an input stream backed on the current source.
     * @param offset the offset from the beginning of the current source where
     *               the input stream will start to read, inclusive.
     * @param length the total amount of bytes that the input stream will be able to read.
     */
    public InputStream inputStream(long offset, long length) {
        checkAbleToIO(offset, length);
        return new RandomAccessSourceInputStream(this, offset, length);
    }

    /**
     * Creates an output stream backed on the current source.
     */
    public OutputStream outputStream() {
        return outputStream(0, length);
    }

    /**
     * Creates an output stream backed on the current source.
     * @param offset the offset from the beginning of the current source where
     *               the output stream will start to write, inclusive.
     * @param length the total amount of bytes that the output stream will be able to write.
     */
    public OutputStream outputStream(long offset, long length) {
        checkAbleToIO(offset, length);
        return new RandomAccessSourceOutputStream(this, offset, length);
    }


    /**
     * Sets the memory of the whole source to the specified value.
     * @param x the value that will be used to fill the memory of the source.
     */
    public void clear(byte x) {
        clear(x, 0, length);
    }

    /**
     * Sets the memory of the source to the specified value.
     * @param x the value that will be used to fill the memory of the source.
     * @param off the offset, inclusive, where the fill operation will start.
     * @param length the total amount of bytes that will be cleared.
     */
    public abstract void clear(byte x, long off, long length);

    /**
     * Sets the memory of the source to the specified value within the specified
     * range in the source.
     * @param x the value that will be used to fill the memory of the source.
     * @param from the offset, inclusive, where the fill operation will start.
     * @param to the offset, exclusive, where the fill operation will end.
     */
    public void clearRange(byte x, long from, long to) {
        clear(x, from, to - from);
    }

    /**
     * When overriden, deallocates the underlying source from the memory.
     */
    public abstract void dealloc();

    public abstract byte     unsafeGet(long off);
    public abstract short    unsafeGetShort(long off, DataOrder order);
    public abstract char     unsafeGetChar(long off, DataOrder order);
    public abstract int      unsafeGetInt(long off, DataOrder order);
    public abstract long     unsafeGetLong(long off, DataOrder order);
    public abstract float    unsafeGetFloat(long off, DataOrder order);
    public abstract double   unsafeGetDouble(long off, DataOrder order);

    public abstract void     unsafeGet(long off, byte[] buffer, int dstOff, int len);
    public abstract void     unsafeGet(long off, ByteBuffer buf);
    public abstract void     unsafeGet(long off, char[] buffer, int dstOff, int len, DataOrder order);
    public abstract void     unsafeGet(long off, short[] buffer, int dstOff, int len, DataOrder order);
    public abstract void     unsafeGet(long off, int[] buffer, int dstOff, int len, DataOrder order);
    public abstract void     unsafeGet(long off, long[] buffer, int dstOff, int len, DataOrder order);
    public abstract void     unsafeGet(long off, float[] buffer, int dstOff, int len, DataOrder order);
    public abstract void     unsafeGet(long off, double[] buffer, int dstOff, int len, DataOrder order);

    public abstract void     unsafePut(long off, byte value);
    public abstract void     unsafePut(long off, short value, DataOrder order);
    public abstract void     unsafePut(long off, char value, DataOrder order);
    public abstract void     unsafePut(long off, int value, DataOrder order);
    public abstract void     unsafePut(long off, long value, DataOrder order);
    public abstract void     unsafePut(long off, float value, DataOrder order);
    public abstract void     unsafePut(long off, double value, DataOrder order);

    public abstract void     unsafePut(long off, byte[] buffer, int srcOff, int len);
    public abstract void     unsafePut(long off, ByteBuffer buf);
    public abstract void     unsafePut(long off, short[] buffer, int srcOff, int len, DataOrder order);
    public abstract void     unsafePut(long off, char[] buffer, int srcOff, int len, DataOrder order);
    public abstract void     unsafePut(long off, int[] buffer, int srcOff, int len, DataOrder order);
    public abstract void     unsafePut(long off, long[] buffer, int srcOff, int len, DataOrder order);
    public abstract void     unsafePut(long off, float[] buffer, int srcOff, int len, DataOrder order);
    public abstract void     unsafePut(long off, double[] buffer, int srcOff, int len, DataOrder order);

    // Safe & convenience methods

    public byte get(long off) {
        checkAbleToIO(off, 1);
        return unsafeGet(off);
    }

    public int getUnsignedByte(long off) {
        return get(off) & 0xff;
    }

    public char getChar(long off, DataOrder order) {
        checkAbleToIO(off, 2);
        return unsafeGetChar(off, order);
    }

    public short getShort(long off, DataOrder order) {
        checkAbleToIO(off, 2);
        return unsafeGetShort(off, order);
    }

    public int getUnsignedShort(long off, DataOrder order) {
        return getShort(off, order) & 0xffff;
    }

    public int getInt(long off, DataOrder order) {
        checkAbleToIO(off, 4);
        return unsafeGetInt(off, order);
    }

    public long getUnsignedInt(long off, DataOrder order) {
        return getInt(off, order) & 0xffffffffL;
    }

    public long getLong(long off, DataOrder order) {
        checkAbleToIO(off, 8);
        return unsafeGetLong(off, order);
    }

    public float getFloat(long off, DataOrder order) {
        checkAbleToIO(off, 4);
        return unsafeGetFloat(off, order);
    }

    public double getDouble(long off, DataOrder order) {
        checkAbleToIO(off, 8);
        return unsafeGetDouble(off, order);
    }

    public byte[] get(long off, byte[] buf) {
        return get(off, buf, 0, buf.length);
    }

    public byte[] get(long off, byte[] buf, int dstOff, int len) {
        checkAbleToIO(off, len);
        unsafeGet(off, buf, dstOff, len);
        return buf;
    }

    public ByteBuffer get(long off, ByteBuffer buf) {
        checkAbleToIO(off, buf.remaining());
        unsafeGet(off, buf);
        return buf;
    }

    public short[] get(long off, short[] buf, DataOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public short[] get(long off, short[] buf, int dstOff, int len, DataOrder order) {
        Validate.checkInRange(buf.length, dstOff, len);
        checkAbleToIO(off, len * 2);
        unsafeGet(off, buf, dstOff, len, order);
        return buf;
    }

    public char[] get(long off, char[] buf, DataOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public char[] get(long off, char[] buf, int dstOff, int len, DataOrder order) {
        Validate.checkInRange(buf.length, dstOff, len);
        checkAbleToIO(off, len * 2);
        unsafeGet(off, buf, dstOff, len, order);
        return buf;
    }

    public int[] get(long off, int[] buf, DataOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public int[] get(long off, int[] buf, int dstOff, int len, DataOrder order) {
        Validate.checkInRange(buf.length, dstOff, len);
        checkAbleToIO(off, len * 4);
        unsafeGet(off, buf, dstOff, len, order);
        return buf;
    }

    public long[] get(long off, long[] buf, DataOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public long[] get(long off, long[] buf, int dstOff, int len, DataOrder order) {
        Validate.checkInRange(buf.length, dstOff, len);
        checkAbleToIO(off, len * 8);
        unsafeGet(off, buf, dstOff, len, order);
        return buf;
    }

    public float[] get(long off, float[] buf, DataOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public float[] get(long off, float[] buf, int dstOff, int len, DataOrder order) {
        Validate.checkInRange(buf.length, dstOff, len);
        checkAbleToIO(off, len * 4);
        unsafeGet(off, buf, dstOff, len, order);
        return buf;
    }

    public double[] get(long off, double[] buf, DataOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public double[] get(long off, double[] buf, int dstOff, int len, DataOrder order) {
        Validate.checkInRange(buf.length, dstOff, len);
        checkAbleToIO(off, len * 8);
        unsafeGet(off, buf, dstOff, len, order);
        return buf;
    }

    public void put(long off, byte x) {
        checkAbleToIO(off, 1);
        unsafePut(off, x);
    }

    public void put(long off, short x, DataOrder order) {
        checkAbleToIO(off, 2);
        unsafePut(off, x, order);
    }

    public void put(long off, char x, DataOrder order) {
        checkAbleToIO(off, 2);
        unsafePut(off, x, order);
    }

    public void put(long off, int x, DataOrder order) {
        checkAbleToIO(off, 4);
        unsafePut(off, x, order);
    }

    public void put(long off, long x, DataOrder order) {
        checkAbleToIO(off, 8);
        unsafePut(off, x, order);
    }

    public void put(long off, float x, DataOrder order) {
        checkAbleToIO(off, 4);
        unsafePut(off, x, order);
    }

    public void put(long off, double x, DataOrder order) {
        checkAbleToIO(off, 8);
        unsafePut(off, x, order);
    }

    public void put(long off, byte[] buf) {
        put(off, buf, 0, buf.length);
    }

    public void put(long off, byte[] buf, int srcOff, int length) {
        Validate.checkInRange(buf.length, srcOff, length);
        checkAbleToIO(off, length);
        unsafePut(off, buf, srcOff, length);
    }

    public void put(long off, ByteBuffer buf) {
        checkAbleToIO(off, buf.remaining());
        unsafePut(off, buf);
    }

    public void put(long off, short[] buf, DataOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(long off, short[] buf, int srcOff, int length, DataOrder order) {
        Validate.checkInRange(buf.length, srcOff, length);
        checkAbleToIO(off, 2 * length);
        unsafePut(off, buf, srcOff, length, order);
    }

    public void put(long off, char[] buf, DataOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(long off, char[] buf, int srcOff, int length, DataOrder order) {
        Validate.checkInRange(buf.length, srcOff, length);
        checkAbleToIO(off, 2 * length);
        unsafePut(off, buf, srcOff, length, order);
    }

    public void put(long off, int[] buf, DataOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(long off, int[] buf, int srcOff, int length, DataOrder order) {
        Validate.checkInRange(buf.length, srcOff, length);
        checkAbleToIO(off, 4 * length);
        unsafePut(off, buf, srcOff, length, order);
    }

    public void put(long off, long[] buf, DataOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(long off, long[] buf, int srcOff, int length, DataOrder order) {
        Validate.checkInRange(buf.length, srcOff, length);
        checkAbleToIO(off, 8 * length);
        unsafePut(off, buf, srcOff, length, order);
    }

    public void put(long off, float[] buf, DataOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(long off, float[] buf, int srcOff, int length, DataOrder order) {
        Validate.checkInRange(buf.length, srcOff, length);
        checkAbleToIO(off, 4 * length);
        unsafePut(off, buf, srcOff, length, order);
    }

    public void put(long off, double[] buf, DataOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(long off, double[] buf, int srcOff, int length, DataOrder order) {
        Validate.checkInRange(buf.length, srcOff, length);
        checkAbleToIO(off, 8 * length);
        unsafePut(off, buf, srcOff, length, order);
    }

    protected void checkAbleToIO(long off, long n) {
        checkNotDeallocated();
        Validate.checkInRange(length, off, n);
    }

    protected final void checkNotDeallocated() {
        if (deallocated()) throw new IllegalStateException(
                "The underlying buffer of this source has been deallocated and is not longer accessible");
    }

    protected final void checkValid() {
        if (!isValid()) throw new IllegalStateException(
                "The underlying buffer of this source is not valid anymore");
    }
}
