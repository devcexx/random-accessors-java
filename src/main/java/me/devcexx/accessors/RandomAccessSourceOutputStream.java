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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An implementation of an {@link OutputStream} that is able to write data to an access source.
 */
public class RandomAccessSourceOutputStream extends OutputStream {
    private final RandomAccessSource source;
    private final long length;
    private final long startOff;
    private final long endOff;
    private long off;

    /**
     * Creates a new output stream for writing to the given source.
     * @param source the backing source.
     * @param startOffset the offset from the beginning of the source from where the output stream
     *                    will start writing.
     * @param length the total amount of bytes that this input stream will be able to write.
     */
    public RandomAccessSourceOutputStream(RandomAccessSource source, long startOffset, long length) {
        Validate.checkInRange(source.length(), startOffset, length);

        this.source = source;
        this.startOff = startOffset;
        this.length = length;
        this.endOff = startOff + length;
        this.off = startOff;
    }

    /**
     * Returns the underlying access source associated with this instance.
     */
    public RandomAccessSource underlyingSource() {
        return source;
    }

    /**
     * Returns max number of bytes from the begin offset that this instance
     * is able to write.
     */
    public long length() {
        return length;
    }

    /**
     * Returns the offset from the beginning of the underlying access source,
     * where this instance began writing.
     */
    public long beginOffset() {
        return startOff;
    }

    /**
     * Returns the position of the stream from the beginning of the underlying
     * access source.
     */
    public synchronized long offset() {
        return off;
    }

    /**
     * Changes the offset of the next byte that the current instance will write.
     * @param offset the desired new offset, from the beginning of the underlying source.
     *
     * @throws IllegalArgumentException if the offset is beyond the max length
     * this instance is allowed to write, or is less than the begin offset.
     */
    public void seek(long offset) {
        if (offset < this.startOff) throw new IllegalArgumentException("Cannot seek beyond the start offset");
        if (offset > this.endOff) throw new IllegalArgumentException("Cannot seek beyond the limit of the source");
        synchronized (this) {
            this.off = offset;
        }
    }

    protected long availableSpace() {
        return this.endOff - this.off;
    }

    private void raiseLimitReachedEx() throws IOException {
        throw new IOException("The limit for writing on the underlying source has been reached");
    }

    @Override
    public void write(int b) throws IOException {
        source.checkValid();
        long writingOff;
        synchronized (this) {
            if (this.off != this.endOff) {
                writingOff = this.off++;
            } else {
                raiseLimitReachedEx();
                return;
            }
        }
        source.unsafePut(writingOff, (byte) (b & 0xff));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        source.checkValid();
        Validate.checkInRange(b.length, off, len);
        long writingOff;
        int willWrite;

        synchronized (this) {
            long av = availableSpace();
            if (av < len) {
                raiseLimitReachedEx();
                return;
            } else {
                willWrite = len;
            }
            writingOff = this.off;
            this.off += willWrite;
        }

        source.unsafeGet(writingOff, b, off, willWrite);
    }

    @Override
    public void flush() { }

    @Override
    public void close() { }
}
