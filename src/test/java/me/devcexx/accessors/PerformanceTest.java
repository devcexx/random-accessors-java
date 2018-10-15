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
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;

public class PerformanceTest {

    private static final int MEGABYTE = (1 << 20);
    private static final int BUFFER_SIZE_IN_MB = 100;
    private static final int BUFFER_WRITE_TIMES = 100;

    private double performTest(WritableBuffer buf) {
        Random random = new Random();
        int[] buffer = new int[BUFFER_SIZE_IN_MB * MEGABYTE / 4];

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = random.nextInt();
        }

        long totalTime = 0;
        long totalToWrite = buffer.length * (long)BUFFER_WRITE_TIMES * 4L;

        for (int i = 0; i < BUFFER_WRITE_TIMES; i++) {
            long t = System.nanoTime();
            buf.writeAndReset(buffer);
            totalTime += System.nanoTime() - t;
        }
        return (totalToWrite / (double)MEGABYTE) / (totalTime / 1000000000.0f);
    }

    private void printResults(String schema, double result) {
        System.out.println(String.format("%s: %.4f MiB/s", schema, result));
    }

    @Test
    public void testOffHeapNativeEndiannessPerfomance() {
        RandomAccessor buf = new RandomAccessor(DirectMemorySource.alloc(BUFFER_SIZE_IN_MB * MEGABYTE), MemoryAccessorOrder.NATIVE_ENDIANNESS);
        double megasPerSecond = performTest(new OffHeapWritableBuffer(buf));
        printResults("Off Heap buffer with native endianness (" + buf.order() + ")", megasPerSecond);

        buf.source().dealloc();
    }


    @Test
    public void testOffHeapNonNativeEndiannessPerfomance() {
        RandomAccessor buf = new RandomAccessor(DirectMemorySource.alloc(BUFFER_SIZE_IN_MB * MEGABYTE), MemoryAccessorOrder.NATIVE_ENDIANNESS.opposite());
        double megasPerSecond = performTest(new OffHeapWritableBuffer(buf));
        printResults("Off Heap buffer with non-native endianness (" + buf.order() + ")", megasPerSecond);

        buf.source().dealloc();
    }


    @Test
    public void testDirectNativeEndiannessPerformance() {
        ByteBuffer buf = ByteBuffer.allocateDirect(BUFFER_SIZE_IN_MB * MEGABYTE);
        buf.order(MemoryAccessorOrder.NATIVE_ENDIANNESS.nioOrder);
        double megasPerSecond = performTest(new JavaNioWritableBuffer(buf));
        printResults("Direct buffer with native endianness", megasPerSecond);
    }


    @Test
    public void testDirectNonNativeEndiannessPerformance() {
        ByteBuffer buf = ByteBuffer.allocateDirect(BUFFER_SIZE_IN_MB * MEGABYTE);
        buf.order(MemoryAccessorOrder.NATIVE_ENDIANNESS.opposite().nioOrder);
        double megasPerSecond = performTest(new JavaNioWritableBuffer(buf));
        printResults("Direct buffer with native endianness", megasPerSecond);
    }

    @Test
    public void testOffHeapThroughDirectBufferNativeEndiannessPerformance() {
        RandomAccessSource buf = DirectMemorySource.alloc(BUFFER_SIZE_IN_MB * MEGABYTE);

        ByteBuffer accessor = buf.byteBuffer();
        accessor.order(MemoryAccessorOrder.NATIVE_ENDIANNESS.nioOrder);

        double megasPerSecond = performTest(new JavaNioWritableBuffer(accessor));
        printResults("Off heap buffer accessed through NIO ByteBuffer with native endianness", megasPerSecond);

        buf.dealloc();
    }

    @Test
    public void testOffHeapThroughDirectBufferNonNativeEndiannessPerformance() {
        RandomAccessSource buf = DirectMemorySource.alloc(BUFFER_SIZE_IN_MB * MEGABYTE);

        ByteBuffer accessor = buf.byteBuffer();
        accessor.order(MemoryAccessorOrder.NATIVE_ENDIANNESS.opposite().nioOrder);

        double megasPerSecond = performTest(new JavaNioWritableBuffer(accessor));
        printResults("Off heap buffer accessed through NIO ByteBuffer with non native endianness", megasPerSecond);

        buf.dealloc();
    }

    private interface WritableBuffer {
        void writeAndReset(int[] buf);
    }


    private class JavaNioWritableBuffer implements WritableBuffer {

        private final IntBuffer buf;

        private JavaNioWritableBuffer(ByteBuffer buf) {
            this.buf = buf.asIntBuffer();
        }

        @Override
        public void writeAndReset(int[] buf) {
            this.buf.put(buf);
            this.buf.position(0);
        }
    }

    private class OffHeapWritableBuffer implements WritableBuffer {

        private final RandomAccessor buf;

        private OffHeapWritableBuffer(RandomAccessor buf) {
            this.buf = buf;
        }

        @Override
        public void writeAndReset(int[] buf) {
            this.buf.put(buf);
            this.buf.position(0);
        }

        public void dealloc() {
            buf.source().dealloc();
        }
    }
}
