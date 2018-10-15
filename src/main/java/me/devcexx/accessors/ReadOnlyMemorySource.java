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
 * Represents a read-only view of a source.
 */
public class ReadOnlyMemorySource extends SlicedSource {
    public ReadOnlyMemorySource(RandomAccessSource source, long off, long length) {
        super(source, off, length);
    }

    private void raiseReadOnlySource() {
        throw new UnsupportedOperationException("This source is read only and cannot be modified");
    }

    @Override
    public ByteBuffer byteBuffer(long off, long length) {
        return super.byteBuffer(off, length).asReadOnlyBuffer();
    }

    @Override
    public void clear(byte x, long off, long length) {
        raiseReadOnlySource();
    }

    @Override
    public void dealloc() {

    }

    @Override
    public void unsafePut(long off, byte value) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, short value, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, char value, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, int value, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, long value, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, float value, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, double value, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, byte[] buffer, int srcOff, int len) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, ByteBuffer buf) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, short[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, char[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, int[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, long[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, float[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }

    @Override
    public void unsafePut(long off, double[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        raiseReadOnlySource();
    }
}
