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
import java.util.LinkedList;

/**
 * Represents a class able to safely read and write from and to a {@link RandomAccessSource},
 * giving access to a cursor, to keep track of the last read/written position, and a limiter, that
 * disallows any I/O operation beyond some point of the source, among others.
 */
public class RandomAccessor {
    public interface AccessorState {
        long limit();
        long position();
        MemoryAccessorOrder order();
    }

    private static class AccessorStateImpl implements AccessorState {
        private long limit;
        private long position;
        private MemoryAccessorOrder order;

        public AccessorStateImpl(long limit, long position, MemoryAccessorOrder order) {
            this.limit = limit;
            this.position = position;
            this.order = order;
        }

        @Override
        public long limit() {
            return this.limit;
        }

        @Override
        public long position() {
            return this.position;
        }

        @Override
        public MemoryAccessorOrder order() {
            return this.order;
        }

        public AccessorStateImpl copy() {
            return new AccessorStateImpl(limit, position, order);
        }
    }

    private final RandomAccessSource source;
    private final AccessorStateImpl state;
    private LinkedList<AccessorState> stateStack;

    /**
     * Creates a random accessor for the specified source.
     * @param source the source to use.
     */
    public RandomAccessor(RandomAccessSource source) {
        this(source, 0);
    }

    /**
     * Creates a random accessor for the specified source.
     * @param source the source to use.
     * @param order the default order for the accessor.
     */
    public RandomAccessor(RandomAccessSource source, MemoryAccessorOrder order) {
        this(source, 0, source.length(), order);
    }

    /**
     * Creates a random accessor for the specified source.
     * @param source the source to use.
     * @param position the initial position of the random accessor.
     */
    public RandomAccessor(RandomAccessSource source, long position) {
        this(source, position, source.length());
    }

    /**
     * Creates a random accessor for the specified source.
     * @param source the source to use.
     * @param position the initial position of the random accessor.
     */
    public RandomAccessor(RandomAccessSource source, long position, long limit) {
        this(source, position, limit, MemoryAccessorOrder.NATIVE_ENDIANNESS);
    }

    /**
     * Creates a random accessor for the specified source.
     * @param source the source to use.
     * @param position the initial position of the random accessor.
     * @param limit the initial limit of the accessor.
     * @param order the default order for the accessor.
     */
    public RandomAccessor(RandomAccessSource source, long position, long limit, MemoryAccessorOrder order) {
        this.state = new AccessorStateImpl(source.length(), 0, order);
        this.source = source;

        limit(limit);
        position(position);
    }

    protected void checkLimit(long off, long n) {
        if (off + n > this.limit()) {
            throw new IllegalArgumentException("Cannot read " + n + " bytes from buffer "
                    + this + ", starting at offset " + off + " and with a limit set at " + this.limit() +
                    " (just " + (this.limit() - off) + " bytes left on the operable area of the buffer)");
        }
    }

    /**
     * Gets the current position (the offset of the next byte to read). The value
     * of the position is included in the range [0, length]
     */
    public long position() {
        return this.state.position;
    }

    /**
     * Sets the current position of the accessor.
     * @param pos the new position.
     */
    public void position(long pos) {
        if (pos > limit()) {
            throw new IllegalArgumentException("The limit cannot be greater than the source length");
        }

        this.state.position = pos;
    }

    /**
     * Creates an sliced accessor from the current one, limited
     * to the specified range.
     * @param from the offset from the beginning of the current accessor that specifies
     *             the beginning of the new sliced accessor.
     * @param to the offset from the beginning of the current accessor that specifies
     *           the end of the new sliced accessor.
     */
    public RandomAccessor sliceRange(long from, long to) {
        return slice(from, from + to);
    }


    /**
     * Creates an sliced accessor from the current one, beginning in
     * the specified offset and ending at the end of the current accessor.
     * @param off the offset from the beginning of the current accessor that specifies
     *            the beginning of the new sliced accessor.
     */
    public RandomAccessor slice(long off) {
        return slice(off, this.length() - off);
    }

    /**
     * Creates an sliced accessor from the current one, beginning in
     * the specified offset with the given length.
     * @param off the offset from the beginning of the current accessor that specifies
     *            the beginning of the new sliced accessor.
     * @param length the length of the new sliced accessor.
     */
    public RandomAccessor slice(long off, long length) {
        Validate.checkInRange(this.length(), off, length);
        return new RandomAccessor(this.source.slice(off, length));
    }

    /**
     * Creates an sliced accessor from the current one, beginning in
     * the current position of the accessor and ending at the end of the current accessor.
     */
    public RandomAccessor slice() {
        return slice(this.position(), this.length() - this.position());
    }

    /**
     * Increments the current position of the accessor by n.
     * @param n the delta of the position
     * @return the new position.
     */
    public long advance(long n) {
        position(this.state.position + n);
        return this.state.position;
    }

    private void unsafeAdvance(long n) {
        this.state.position += n;
    }

    /**
     * Returns the limit currently set on this accessor.
     */
    public long limit() {
        return this.state.limit;
    }

    /**
     * Sets the limit of the current accessor. This limit is a number between 0 and length,
     * inclusive, and no operations can be performed beyond the offset specified by the limit.
     * To remove the limit of this accessor, set the limit to the length of this accessor.
     * @param limit the new limit.
     */
    public void limit(long limit) {
        if (limit > source.length()) {
            throw new IllegalArgumentException("The limit cannot be greater than the source length");
        }

        if (position() > limit) {
            this.state.position = limit;
        }

        this.state.limit = limit;
    }

    /**
     * Returns the default byte order that will be used to
     * encode or decode data in this accessor.
     */
    public MemoryAccessorOrder order() {
        return this.state.order;
    }

    /**
     * Sets the new default byte order of the current accessor.
     * @param order the new byte order.
     */
    public void order(MemoryAccessorOrder order) {
        this.state.order = order;
    }

    /**
     * Returns the underlying source that is being used by this accessor.
     */
    public RandomAccessSource source() {
        return this.source;
    }

    /**
     * Pushes the current state (defined by the triple (limit, position, order)), onto a
     * LIFO structure, allowing to recover it in the future. It is expected from you
     * to perform a call to {@link #popState()} for each call to this function.
     * @return the saved state.
     */
    public AccessorState pushState() {
        if (stateStack == null) {
            stateStack = new LinkedList<>();
        }

        AccessorState state = this.state.copy();
        this.stateStack.push(state);
        return this.state.copy();
    }

    /**
     * Pops a previous state saved by the method {@link #pushState()} and set it as
     * the current state for this accessor.
     * @return the popped state, or null if the state stack is empty.
     */
    public AccessorState popState() {
        if (stateStack == null)
            return null;

        AccessorState state = this.stateStack.pop();
        if (state != null) {
            this.state.position = state.position();
            this.state.limit = state.limit();
        }
        return state;
    }

    /**
     * The length of the underlying source.
     */
    public long length() {
        return this.source.length();
    }

    /**
     * The remaining bytes that are able to be read or written in the current accessor,
     * taking into account the current position and the limit of the current accessor.
     */
    public long remaining() {
        return this.limit() - this.position();
    }

    /**
     * Increases the current position as necessary to align it to the word size of this computer, filling with zeroes
     * the skipped positions.
     * @return the number of bytes skipped and zero-ed.
     */
    public long padToWord() {
        return padTo(UnsafeMemory.wordSize());
    }

    /**
     * Increases the current position as necessary to align it to the word size of this computer, filling with the
     * specified byte the skipped positions.
     * @param fill the byte that will be used to fill the skipped positions.
     * @return the number of bytes skipped and filled.
     */
    public long padToWord(byte fill) {
        return padTo(UnsafeMemory.wordSize(), fill);
    }

    /**
     * Increases the current position as necessary to align it to the specified data size, filling with zeroes
     * the skipped positions.
     * @param dataSize the data size used to align the current position.
     * @return the number of bytes skipped and filled.
     */
    public long padTo(int dataSize) {
        return padTo(dataSize, (byte) 0);
    }

    /**
     * Increases the current position as necessary to align it to the specified data size, filling with the
     * specified byte the skipped positions.
     * @param dataSize the data size used to align the current position.
     * @param fill the byte that will be used to fill the skipped positions.
     * @return the number of bytes skipped and filled.
     */
    public long padTo(int dataSize, byte fill) {
        long mod = (int) (position() % dataSize);
        if (mod != 0) {
            long adv = dataSize - mod;
            long off = position();
            advance(adv);
            source().clear(fill, off, adv);
            return adv;
        }
        return 0;
    }

    /**
     * Increases the current position as necessary to align it to the word size of this computer.
     * @return the number of bytes skipped.
     */
    public long alignToWord() {
        return alignTo(UnsafeMemory.wordSize());
    }

    /**
     * Increases the current position as necessary to align it to the specified data size.
     * @return the number of bytes skipped.
     */
    public long alignTo(int dataSize) {
        long mod = (int) (position() % dataSize);
        if (mod != 0) {
            long adv = dataSize - mod;
            advance(adv);
            return adv;
        }
        return 0;
    }


    // GET get

    public byte get() {
        byte x = get(position());
        unsafeAdvance(1);
        return x;
    }

    public byte get(long off) {
        checkLimit(off, 1);
        return source.get(off);
    }

    // GET unsigned byte

    public int getUnsignedByte() {
        int x = getUnsignedByte(position());
        unsafeAdvance(1);
        return x;
    }

    public int getUnsignedByte(long off) {
        checkLimit(off, 1);
        return source.getUnsignedByte(off);
    }

    // GET short

    public short getShort() {
        short x = getShort(position(), order());
        unsafeAdvance(2);
        return x;
    }

    public short getShort(long off) {
        return getShort(off, order());
    }

    public short getShort(MemoryAccessorOrder order) {
        short x = getShort(position(), order);
        unsafeAdvance(2);
        return x;
    }

    public short getShort(long off, MemoryAccessorOrder order) {
        checkLimit(off, 2);
        return source.getShort(off, order);
    }

    // GET char

    public char getChar() {
        char x = getChar(position(), order());
        unsafeAdvance(2);
        return x;
    }

    public char getChar(long off) {
        return getChar(off, order());
    }

    public char getChar(MemoryAccessorOrder order) {
        char x = getChar(position(), order);
        unsafeAdvance(2);
        return x;
    }

    public char getChar(long off, MemoryAccessorOrder order) {
        checkLimit(off, 2);
        return source.getChar(off, order);
    }

    // GET unsigned short

    public int getUnsignedShort() {
        int x = getUnsignedShort(position(), order());
        unsafeAdvance(2);
        return x;
    }

    public int getUnsignedShort(long off) {
        return getUnsignedShort(off, order());
    }

    public int getUnsignedShort(MemoryAccessorOrder order) {
        int x = getUnsignedShort(position(), order);
        unsafeAdvance(2);
        return x;
    }

    public int getUnsignedShort(long off, MemoryAccessorOrder order) {
        checkLimit(off, 2);
        return source.getUnsignedShort(off, order);
    }

    // GET int

    public int getInt() {
        int x = getInt(position(), order());
        unsafeAdvance(4);
        return x;
    }

    public int getInt(long off) {
        return getInt(off, order());
    }

    public int getInt(MemoryAccessorOrder order) {
        int x = getInt(position(), order);
        unsafeAdvance(4);
        return x;
    }

    public int getInt(long off, MemoryAccessorOrder order) {
        checkLimit(off, 4);
        return source.getInt(off, order);
    }

    // GET unsigned int

    public long getUnsignedInt() {
        long x = getUnsignedInt(position(), order());
        unsafeAdvance(4);
        return x;
    }

    public long getUnsignedInt(long off) {
        return getUnsignedInt(off, order());
    }

    public long getUnsignedInt(MemoryAccessorOrder order) {
        long x = getUnsignedInt(position(), order);
        unsafeAdvance(4);
        return x;
    }

    public long getUnsignedInt(long off, MemoryAccessorOrder order) {
        checkLimit(off, 4);
        return source.getUnsignedInt(off, order);
    }

    // GET long

    public long getLong() {
        long x = getLong(position(), order());
        unsafeAdvance(8);
        return x;
    }

    public long getLong(long off) {
        return getLong(off, order());
    }

    public long getLong(MemoryAccessorOrder order) {
        long x = getLong(position(), order());
        unsafeAdvance(8);
        return x;
    }

    public long getLong(long off, MemoryAccessorOrder order) {
        checkLimit(off, 8);
        return source.getLong(off, order);
    }

    // GET float

    public float getFloat() {
        float x = getFloat(position(), order());
        unsafeAdvance(4);
        return x;
    }

    public float getFloat(long off) {
        return getFloat(off, order());
    }

    public float getFloat(MemoryAccessorOrder order) {
        float x = getFloat(position(), order);
        unsafeAdvance(4);
        return x;
    }

    public float getFloat(long off, MemoryAccessorOrder order) {
        checkLimit(off, 4);
        return source.getFloat(off, order);
    }

    // GET double

    public double getDouble() {
        double x = getDouble(position(), order());
        unsafeAdvance(8);
        return x;
    }

    public double getDouble(long off) {
        return getDouble(off, order());
    }

    public double getDouble(MemoryAccessorOrder order) {
        double x = getDouble(position(), order);
        unsafeAdvance(8);
        return x;
    }

    public double getDouble(long off, MemoryAccessorOrder order) {
        checkLimit(off, 8);
        return source.getDouble(off, order);
    }

    // GET byte[]

    public byte[] get(byte[] buf) {
        return get(buf, 0, buf.length);
    }

    public byte[] get(long off, byte[] buf) {
        return get(off, buf, 0, buf.length);
    }

    public byte[] get(byte[] buf, int dstOff, int len) {
        get(position(), buf, dstOff, len);
        unsafeAdvance(len);
        return buf;
    }

    public byte[] get(long off, byte[] buf, int dstOff, int len) {
        checkLimit(off, len);
        return source.get(off, buf, dstOff, len);
    }

    // GET ByteBuffer

    public ByteBuffer get(ByteBuffer buf) {
        int rem = buf.remaining();
        get(position(), buf);
        unsafeAdvance(rem);
        return buf;
    }

    public ByteBuffer get(long off, ByteBuffer buf) {
        checkLimit(off, buf.remaining());
        return source.get(off, buf);
    }

    // GET short[]

    public short[] get(short[] buf) {
        return get(buf, 0, buf.length);
    }

    public short[] get(short[] buf, MemoryAccessorOrder order) {
        return get(buf, 0, buf.length, order);
    }

    public short[] get(long off, short[] buf) {
        return get(off, buf, 0, buf.length);
    }

    public short[] get(long off, short[] buf, MemoryAccessorOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public short[] get(short[] buf, int dstOff, int len) {
        get(position(), buf, dstOff, len, order());
        unsafeAdvance(len * 2);
        return buf;
    }

    public short[] get(short[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        get(position(), buf, dstOff, len, order);
        unsafeAdvance(len * 2);
        return buf;
    }

    public short[] get(long off, short[] buf, int dstOff, int len) {
        return get(off, buf, dstOff, len, order());
    }

    public short[] get(long off, short[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        checkLimit(off, len);
        return source.get(off, buf, dstOff, len, order);
    }

    // GET char[]

    public char[] get(char[] buf) {
        return get(buf, 0, buf.length);
    }

    public char[] get(char[] buf, MemoryAccessorOrder order) {
        return get(buf, 0, buf.length, order);
    }

    public char[] get(long off, char[] buf) {
        return get(off, buf, 0, buf.length);
    }

    public char[] get(long off, char[] buf, MemoryAccessorOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public char[] get(char[] buf, int dstOff, int len) {
        get(position(), buf, dstOff, len, order());
        unsafeAdvance(len * 2);
        return buf;
    }

    public char[] get(char[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        get(position(), buf, dstOff, len, order);
        unsafeAdvance(len * 2);
        return buf;
    }

    public char[] get(long off, char[] buf, int dstOff, int len) {
        return get(off, buf, dstOff, len, order());
    }

    public char[] get(long off, char[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        checkLimit(off, len);
        return source.get(off, buf, dstOff, len, order);
    }

    // GET int[]

    public int[] get(int[] buf) {
        return get(buf, 0, buf.length);
    }

    public int[] get(int[] buf, MemoryAccessorOrder order) {
        return get(buf, 0, buf.length, order);
    }

    public int[] get(long off, int[] buf) {
        return get(off, buf, 0, buf.length);
    }

    public int[] get(long off, int[] buf, MemoryAccessorOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public int[] get(int[] buf, int dstOff, int len) {
        get(position(), buf, dstOff, len, order());
        unsafeAdvance(len * 4);
        return buf;
    }

    public int[] get(int[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        get(position(), buf, dstOff, len, order);
        unsafeAdvance(len * 4);
        return buf;
    }

    public int[] get(long off, int[] buf, int dstOff, int len) {
        return get(off, buf, dstOff, len, order());
    }

    public int[] get(long off, int[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        checkLimit(off, len);
        return source.get(off, buf, dstOff, len, order);
    }

    // GET long[]

    public long[] get(long[] buf) {
        return get(buf, 0, buf.length);
    }

    public long[] get(long[] buf, MemoryAccessorOrder order) {
        return get(buf, 0, buf.length, order);
    }

    public long[] get(long off, long[] buf) {
        return get(off, buf, 0, buf.length);
    }

    public long[] get(long off, long[] buf, MemoryAccessorOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public long[] get(long[] buf, int dstOff, int len) {
        get(position(), buf, dstOff, len, order());
        unsafeAdvance(len * 8);
        return buf;
    }

    public long[] get(long[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        get(position(), buf, dstOff, len, order);
        unsafeAdvance(len * 8);
        return buf;
    }

    public long[] get(long off, long[] buf, int dstOff, int len) {
        return get(off, buf, dstOff, len, order());
    }

    public long[] get(long off, long[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        checkLimit(off, len);
        return source.get(off, buf, dstOff, len, order);
    }

    // GET float[]

    public float[] get(float[] buf) {
        return get(buf, 0, buf.length);
    }

    public float[] get(float[] buf, MemoryAccessorOrder order) {
        return get(buf, 0, buf.length, order);
    }

    public float[] get(long off, float[] buf) {
        return get(off, buf, 0, buf.length);
    }

    public float[] get(long off, float[] buf, MemoryAccessorOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public float[] get(float[] buf, int dstOff, int len) {
        get(position(), buf, dstOff, len, order());
        unsafeAdvance(len * 4);
        return buf;
    }

    public float[] get(float[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        get(position(), buf, dstOff, len, order);
        unsafeAdvance(len * 4);
        return buf;
    }

    public float[] get(long off, float[] buf, int dstOff, int len) {
        return get(off, buf, dstOff, len, order());
    }

    public float[] get(long off, float[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        checkLimit(off, len);
        return source.get(off, buf, dstOff, len, order);
    }

    // GET double[]

    public double[] get(double[] buf) {
        return get(buf, 0, buf.length);
    }

    public double[] get(double[] buf, MemoryAccessorOrder order) {
        return get(buf, 0, buf.length, order);
    }

    public double[] get(long off, double[] buf) {
        return get(off, buf, 0, buf.length);
    }

    public double[] get(long off, double[] buf, MemoryAccessorOrder order) {
        return get(off, buf, 0, buf.length, order);
    }

    public double[] get(double[] buf, int dstOff, int len) {
        get(position(), buf, dstOff, len, order());
        unsafeAdvance(len * 8);
        return buf;
    }

    public double[] get(double[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        get(position(), buf, dstOff, len, order);
        unsafeAdvance(len * 8);
        return buf;
    }

    public double[] get(long off, double[] buf, int dstOff, int len) {
        return get(off, buf, dstOff, len, order());
    }

    public double[] get(long off, double[] buf, int dstOff, int len, MemoryAccessorOrder order) {
        checkLimit(off, len);
        return source.get(off, buf, dstOff, len, order);
    }

    // PUT byte

    public void put(byte x) {
        put(position(), x);
        unsafeAdvance(1);
    }

    public void put(long off, byte x) {
        checkLimit(off, 1);
        source.put(off, x);
    }

    // PUT short

    public void put(short x) {
        put(position(), x, order());
        unsafeAdvance(2);
    }

    public void put(short x, MemoryAccessorOrder order) {
        put(position(), x, order);
        unsafeAdvance(2);
    }

    public void put(long off, short x) {
        put(position(), x, order());
    }

    public void put(long off, short x, MemoryAccessorOrder order) {
        checkLimit(off, 2);
        source.put(off, x, order);
    }

    // PUT char

    public void put(char x) {
        put(position(), x, order());
        unsafeAdvance(2);
    }

    public void put(char x, MemoryAccessorOrder order) {
        put(position(), x, order);
        unsafeAdvance(2);
    }

    public void put(long off, char x) {
        put(position(), x, order());
    }

    public void put(long off, char x, MemoryAccessorOrder order) {
        checkLimit(off, 2);
        source.put(off, x, order);
    }

    // PUT int

    public void put(int x) {
        put(position(), x, order());
        unsafeAdvance(4);
    }

    public void put(int x, MemoryAccessorOrder order) {
        put(position(), x, order);
        unsafeAdvance(4);
    }

    public void put(long off, int x) {
        put(position(), x, order());
    }

    public void put(long off, int x, MemoryAccessorOrder order) {
        checkLimit(off, 4);
        source.put(off, x, order);
    }

    // PUT long

    public void put(long x) {
        put(position(), x, order());
        unsafeAdvance(8);
    }

    public void put(long x, MemoryAccessorOrder order) {
        put(position(), x, order);
        unsafeAdvance(8);
    }

    public void put(long off, long x) {
        put(position(), x, order());
    }

    public void put(long off, long x, MemoryAccessorOrder order) {
        checkLimit(off, 8);
        source.put(off, x, order);
    }

    // PUT float

    public void put(float x) {
        put(position(), x, order());
        unsafeAdvance(4);
    }

    public void put(float x, MemoryAccessorOrder order) {
        put(position(), x, order);
        unsafeAdvance(4);
    }

    public void put(long off, float x) {
        put(position(), x, order());
    }

    public void put(long off, float x, MemoryAccessorOrder order) {
        checkLimit(off, 4);
        source.put(off, x, order);
    }

    // PUT double

    public void put(double x) {
        put(position(), x, order());
        unsafeAdvance(8);
    }

    public void put(double x, MemoryAccessorOrder order) {
        put(position(), x, order);
        unsafeAdvance(8);
    }

    public void put(long off, double x) {
        put(position(), x, order());
    }

    public void put(long off, double x, MemoryAccessorOrder order) {
        checkLimit(off, 8);
        source.put(off, x, order);
    }

    // PUT byte[]

    public void put(byte[] buf) {
        put(buf, 0, buf.length);
    }

    public void put(byte[] buf, int srcOff, int length) {
        put(position(), buf, srcOff, length);
        unsafeAdvance(length);
    }

    public void put(long off, byte[] buf) {
        put(off, buf, 0, buf.length);
    }

    public void put(long off, byte[] buf, int srcOff, int length) {
        checkLimit(off, length);
        source.put(off, buf, srcOff, length);
    }

    // PUT ByteBuffer

    public void put(ByteBuffer buf) {
        int rem = buf.remaining();
        put(position(), buf);
        unsafeAdvance(rem);
    }

    public void put(long off, ByteBuffer buf) {
        checkLimit(off, buf.remaining());
        source.put(off, buf);
    }

    // PUT short[]

    public void put(short[] buf) {
        put(buf, 0, buf.length);
    }

    public void put(short[] buf, MemoryAccessorOrder order) {
        put(buf, 0, buf.length, order);
    }

    public void put(long off, short[] buf) {
        put(off, buf, 0, buf.length);
    }

    public void put(long off, short[] buf, MemoryAccessorOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(short[] buf, int srcOff, int length) {
        put(position(), buf, srcOff, length, order());
        unsafeAdvance(length * 2);
    }

    public void put(long off, short[] buf, int srcOff, int length) {
        put(off, buf, srcOff, length, order());
    }

    public void put(short[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        put(position(), buf, srcOff, length, order);
        unsafeAdvance(length * 2);
    }

    public void put(long off, short[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        checkLimit(off, length);
        source.put(off, buf, srcOff, length, order);
    }

    // PUT char[]

    public void put(char[] buf) {
        put(buf, 0, buf.length);
    }

    public void put(char[] buf, MemoryAccessorOrder order) {
        put(buf, 0, buf.length, order);
    }

    public void put(long off, char[] buf) {
        put(off, buf, 0, buf.length);
    }

    public void put(long off, char[] buf, MemoryAccessorOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(char[] buf, int srcOff, int length) {
        put(position(), buf, srcOff, length, order());
        unsafeAdvance(length * 2);
    }

    public void put(long off, char[] buf, int srcOff, int length) {
        put(off, buf, srcOff, length, order());
    }

    public void put(char[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        put(position(), buf, srcOff, length, order);
        unsafeAdvance(length * 2);
    }

    public void put(long off, char[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        checkLimit(off, length);
        source.put(off, buf, srcOff, length, order);
    }

    // PUT int[]

    public void put(int[] buf) {
        put(buf, 0, buf.length);
    }

    public void put(int[] buf, MemoryAccessorOrder order) {
        put(buf, 0, buf.length, order);
    }

    public void put(long off, int[] buf) {
        put(off, buf, 0, buf.length);
    }

    public void put(long off, int[] buf, MemoryAccessorOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(int[] buf, int srcOff, int length) {
        put(position(), buf, srcOff, length, order());
        unsafeAdvance(length * 4);
    }

    public void put(long off, int[] buf, int srcOff, int length) {
        put(off, buf, srcOff, length, order());
    }

    public void put(int[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        put(position(), buf, srcOff, length, order);
        unsafeAdvance(length * 4);
    }

    public void put(long off, int[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        checkLimit(off, length);
        source.put(off, buf, srcOff, length, order);
    }

    // PUT long[]

    public void put(long[] buf) {
        put(buf, 0, buf.length);
    }

    public void put(long[] buf, MemoryAccessorOrder order) {
        put(buf, 0, buf.length, order);
    }

    public void put(long off, long[] buf) {
        put(off, buf, 0, buf.length);
    }

    public void put(long off, long[] buf, MemoryAccessorOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(long[] buf, int srcOff, int length) {
        put(position(), buf, srcOff, length, order());
        unsafeAdvance(length * 8);
    }

    public void put(long off, long[] buf, int srcOff, int length) {
        put(off, buf, srcOff, length, order());
    }

    public void put(long[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        put(position(), buf, srcOff, length, order);
        unsafeAdvance(length * 8);
    }

    public void put(long off, long[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        checkLimit(off, length);
        source.put(off, buf, srcOff, length, order);
    }

    // PUT float[]

    public void put(float[] buf) {
        put(buf, 0, buf.length);
    }

    public void put(float[] buf, MemoryAccessorOrder order) {
        put(buf, 0, buf.length, order);
    }

    public void put(long off, float[] buf) {
        put(off, buf, 0, buf.length);
    }

    public void put(long off, float[] buf, MemoryAccessorOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(float[] buf, int srcOff, int length) {
        put(position(), buf, srcOff, length, order());
        unsafeAdvance(length * 4);
    }

    public void put(long off, float[] buf, int srcOff, int length) {
        put(off, buf, srcOff, length, order());
    }

    public void put(float[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        put(position(), buf, srcOff, length, order);
        unsafeAdvance(length * 4);
    }

    public void put(long off, float[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        checkLimit(off, length);
        source.put(off, buf, srcOff, length, order);
    }

    // PUT double[]

    public void put(double[] buf) {
        put(buf, 0, buf.length);
    }

    public void put(double[] buf, MemoryAccessorOrder order) {
        put(buf, 0, buf.length, order);
    }

    public void put(long off, double[] buf) {
        put(off, buf, 0, buf.length);
    }

    public void put(long off, double[] buf, MemoryAccessorOrder order) {
        put(off, buf, 0, buf.length, order);
    }

    public void put(double[] buf, int srcOff, int length) {
        put(position(), buf, srcOff, length, order());
        unsafeAdvance(length * 8);
    }

    public void put(long off, double[] buf, int srcOff, int length) {
        put(off, buf, srcOff, length, order());
    }

    public void put(double[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        put(position(), buf, srcOff, length, order);
        unsafeAdvance(length * 8);
    }

    public void put(long off, double[] buf, int srcOff, int length, MemoryAccessorOrder order) {
        checkLimit(off, length);
        source.put(off, buf, srcOff, length, order);
    }
}
