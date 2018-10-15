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

/**
 * An implementation of an {@link InputStream} that is able to read data from an access source.
 */
public class RandomAccessSourceInputStream extends InputStream {
    private final RandomAccessSource source;
    private final long length;
    private final long startOff;
    private final long endOff;
    private long off;

    /**
     * Creates a new input stream for reading from the given source.
     * @param source the backing source.
     * @param startOffset the offset from the beginning of the source from where the input stream
     *                    will start reading.
     * @param length the total amount of bytes that this input stream will be able to read.
     */
    public RandomAccessSourceInputStream(RandomAccessSource source, long startOffset, long length) {
        Validate.checkInRange(source.length(), startOffset, length);

        this.source = source;
        this.startOff = startOffset;
        this.length = length;
        this.endOff = startOff + length;
        this.off = startOff;
    }

    /**
     * Returns the underlying source of this stream.
     */
    public RandomAccessSource underlyingSource() {
        return source;
    }

    /**
     * Returns the total length of this input stream.
     */
    public long length() {
        return length;
    }

    /**
     * The offset from the beginning of the source from where the input stream
     * will start reading.
     */
    public long beginOffset() {
        return startOff;
    }

    /**
     * The offset from the beginning of the source where the input stream
     * is currently placed.
     */
    public synchronized long offset() {
        return off;
    }

    /**
     * Seeks the input stream to the specified offset.
     * @param offset The offset from the beginning of the source that will
     *               be used as a new current offset.
     */
    public void seek(long offset) {
        if (offset < this.startOff) throw new IllegalArgumentException("Cannot seek beyond the start offset");
        if (offset > this.endOff) throw new IllegalArgumentException("Cannot seek beyond the limit of the source");
        synchronized (this) {
            this.off = offset;
        }
    }

    protected long realAvailable() {
        return this.endOff - this.off;
    }

    @Override
    public int read() {
        source.checkValid();
        long readingOff;
        synchronized (this) {
            if (this.off != this.endOff) {
                readingOff = this.off++;
            } else {
                return -1;
            }
        }
        return source.unsafeGet(readingOff);
    }

    @Override
    public int read(byte[] b, int off, int len) {
        source.checkValid();
        Validate.checkInRange(b.length, off, len);
        long readingOff;
        int willRead;

        synchronized (this) {
            long av = realAvailable();
            if (av < len) {
                willRead = (int) av;
            } else {
                willRead = len;
            }
            readingOff = this.off;
            this.off += willRead;
        }

        source.unsafeGet(readingOff, b, off, willRead);
        return willRead;
    }

    @Override
    public long skip(long n) {
        synchronized (this) {
            long av = realAvailable();
            if (av < n) {
                this.off = this.endOff;
                return av;
            }

            this.off += n;
            return n;
        }
    }

    @Override
    public int available() {
        long r;
        synchronized (this) {
            r = realAvailable();
        }
        if (r > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) r;
        }
    }

    @Override
    public void close() {
    }

    @Override
    public boolean markSupported() {
        return false;
    }
}
