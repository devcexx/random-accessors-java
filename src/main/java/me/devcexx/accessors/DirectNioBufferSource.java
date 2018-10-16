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
 * Represents a source that is able to read and write directly from
 * a memory block allocated for a Java NIO Direct Buffer.
 */
public class DirectNioBufferSource extends DirectMemorySource {

    //It's imperative to save a reference to the byte buffer
    //so the underlying memory block are not freed if the GC decides
    //to remove the ByteBuffer instance.
    private final ByteBuffer byteBuffer;

    /**
     * Creates a new source backed in the memory of a given Direct Buffer.
     * @param buffer a Java NIO Direct Buffer.
     */
    public DirectNioBufferSource(ByteBuffer buffer) {
        super(Unsafe.addressOfByteBuffer(buffer), buffer.capacity());
        this.byteBuffer = buffer.duplicate();
    }

    @Override
    public ByteBuffer byteBuffer(long off, long length) {
        Validate.checkInRange(length(), off, length);

        byteBuffer.position((int) off);
        byteBuffer.limit((int) length);
        return byteBuffer.slice();
    }

    @Override
    public void dealloc() {
        //This must be overriden so the direct buffer should be cleaned through its cleaner,
        //not a free statement, but, should I keep this empty, so the deallocation
        //can only be performed from the byte source?
    }

    @Override
    protected void finalize() throws Throwable {
        //Override this method so the deallocation process depends on the
        //byte buffer, not on this source.
    }
}
