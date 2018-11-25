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

/**
 * Represents a access sourced backend on another access source that is able to operate
 * just in a specific of the underlying one.
 */
public class SlicedSource extends RandomAccessSource {
    private final RandomAccessSource source;
    private final long pf;
    private final int permissions;

    /**
     * Creates a sliced source that is able to operate in the given source, from
     * the given offset with the specified length.
     * @param source the underlying source.
     * @param offset the offset from the beginning of the given source that will be the
     *               zero position of this source.
     * @param length the length of this new source.
     */
    public SlicedSource(RandomAccessSource source, long offset, long length) {
        this(source, offset, length, -1);
    }

    public SlicedSource(RandomAccessSource source, long offset, long length, int permMask) {
        super(length);
        Validate.checkInRange(source.length(), offset, length);

        this.pf = offset;
        this.source = source;
        this.permissions = source.getPermissions() & permMask;
    }

    @Override
    public final int getPermissions() {
        return permissions;
    }

    private void checkPermissions(int type) {
        if ((permissions & type) == 0) {
            throw new IllegalStateException("Cannot perform a "
                    + (type == AccessorPermissions.READ ? "read" : "write")
                    + " operation on this source");
        }
    }

    @Override
    public RandomAccessSource withPermissions(int mask) {
        return new SlicedSource(source, 0, length(), permissions & mask);
    }

    @Override
    public RandomAccessSource slice(long off, long length) {
        return source.slice(pf + off, length);
    }

    @Override
    public ByteBuffer byteBuffer(long off, long length) {
        return source.byteBuffer(pf + off, length);
    }

    @Override
    public void clear(byte x, long off, long length) {
        source.clear(x, pf + off, length);
    }

    @Override
    public void dealloc() {

    }

    @Override
    public boolean deallocated() {
        return source.deallocated();
    }

    @Override
    public final byte unsafeGet(long off) {
        checkPermissions(AccessorPermissions.READ);
        return source.unsafeGet(pf + off);
    }

    @Override
    public final short unsafeGetShort(long off, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        return source.unsafeGetShort(pf + off, order);
    }

    @Override
    public final char unsafeGetChar(long off, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        return source.unsafeGetChar(pf + off, order);
    }

    @Override
    public final int unsafeGetInt(long off, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        return source.unsafeGetInt(pf + off, order);
    }

    @Override
    public final long unsafeGetLong(long off, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        return source.unsafeGetLong(pf + off, order);
    }

    @Override
    public final float unsafeGetFloat(long off, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        return source.unsafeGetFloat(pf + off, order);
    }

    @Override
    public final double unsafeGetDouble(long off, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        return source.unsafeGetDouble(pf + off, order);
    }

    @Override
    public final void unsafeGet(long off, byte[] buffer, int dstOff, int len) {
        checkPermissions(AccessorPermissions.READ);
        source.unsafeGet(pf + off, buffer, dstOff, len);
    }

    @Override
    public final void unsafeGet(long off, ByteBuffer buf) {
        checkPermissions(AccessorPermissions.READ);
        source.unsafeGet(pf + off, buf);
    }

    @Override
    public final void unsafeGet(long off, char[] buffer, int dstOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        source.unsafeGet(pf + off, buffer, dstOff, len, order);
    }

    @Override
    public final void unsafeGet(long off, short[] buffer, int dstOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        source.unsafeGet(pf + off, buffer, dstOff, len, order);
    }

    @Override
    public final void unsafeGet(long off, int[] buffer, int dstOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        source.unsafeGet(pf + off, buffer, dstOff, len, order);
    }

    @Override
    public final void unsafeGet(long off, long[] buffer, int dstOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        source.unsafeGet(pf + off, buffer, dstOff, len, order);
    }

    @Override
    public final void unsafeGet(long off, float[] buffer, int dstOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        source.unsafeGet(pf + off, buffer, dstOff, len, order);
    }

    @Override
    public final void unsafeGet(long off, double[] buffer, int dstOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.READ);
        source.unsafeGet(pf + off, buffer, dstOff, len, order);
    }

    @Override
    public final void unsafePut(long off, byte value) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, value);
    }

    @Override
    public final void unsafePut(long off, short value, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, value, order);
    }

    @Override
    public final void unsafePut(long off, char value, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, value, order);
    }

    @Override
    public final void unsafePut(long off, int value, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, value, order);
    }

    @Override
    public final void unsafePut(long off, long value, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, value, order);
    }

    @Override
    public final void unsafePut(long off, float value, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, value, order);
    }

    @Override
    public final void unsafePut(long off, double value, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, value, order);
    }

    @Override
    public final void unsafePut(long off, byte[] buffer, int srcOff, int len) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, buffer, srcOff, len);
    }

    @Override
    public final void unsafePut(long off, ByteBuffer buf) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, buf);
    }

    @Override
    public final void unsafePut(long off, short[] buffer, int srcOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, buffer, srcOff, len, order);
    }

    @Override
    public final void unsafePut(long off, char[] buffer, int srcOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, buffer, srcOff, len, order);
    }

    @Override
    public final void unsafePut(long off, int[] buffer, int srcOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, buffer, srcOff, len, order);
    }

    @Override
    public final void unsafePut(long off, long[] buffer, int srcOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, buffer, srcOff, len, order);
    }

    @Override
    public final void unsafePut(long off, float[] buffer, int srcOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, buffer, srcOff, len, order);
    }

    @Override
    public final void unsafePut(long off, double[] buffer, int srcOff, int len, DataOrder order) {
        checkPermissions(AccessorPermissions.WRITE);
        source.unsafePut(pf + off, buffer, srcOff, len, order);
    }
}
