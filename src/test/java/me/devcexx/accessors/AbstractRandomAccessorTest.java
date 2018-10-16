package me.devcexx.accessors;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractRandomAccessorTest {

    protected abstract RandomAccessSource mkSource(long size);

    public void testScalars(MemoryAccessorOrder order) {
        RandomAccessSource source = mkSource(29);
        Random random = new Random();

        byte byteVal = (byte) random.nextInt();
        char charVal = (char) random.nextInt();
        short shortVal = (short) random.nextInt();
        int intVal = random.nextInt();
        long longVal = random.nextLong();
        float floatVal = random.nextFloat();
        double doubleVal = random.nextDouble();

        source.put(0, byteVal);
        source.put(1, charVal, order);
        source.put(3, shortVal, order);
        source.put(5, intVal, order);
        source.put(9, longVal, order);
        source.put(17, floatVal, order);
        source.put(21, doubleVal, order);

        assertEquals(byteVal, source.get(0));
        assertEquals(charVal, source.getChar(1, order));
        assertEquals(shortVal, source.getShort(3, order));
        assertEquals(intVal, source.getInt(5, order));
        assertEquals(longVal, source.getLong(9, order));
        assertEquals(Float.floatToRawIntBits(floatVal), Float.floatToRawIntBits(source.getFloat(17, order)));
        assertEquals(Double.doubleToRawLongBits(doubleVal), Double.doubleToRawLongBits(source.getDouble(21, order)));

        source.dealloc();
    }


    private void testBuffers(MemoryAccessorOrder order) {
        RandomAccessSource source = mkSource(145);

        Random random = new Random();

        byte[] byteVals = new byte[5];
        random.nextBytes(byteVals);
        char[] charVals = { (char) random.nextInt(),  (char) random.nextInt(),  (char) random.nextInt(),  (char) random.nextInt(),  (char) random.nextInt()};
        short[] shortVals = { (short) random.nextInt(), (short) random.nextInt(), (short) random.nextInt(), (short) random.nextInt(), (short) random.nextInt()};
        int[] intVals = { random.nextInt(), random.nextInt(), random.nextInt(), random.nextInt(), random.nextInt()};
        long[] longVals = { random.nextLong(), random.nextLong(), random.nextLong(), random.nextLong(), random.nextLong()};
        float[] floatVals = { random.nextFloat(), random.nextFloat(), random.nextFloat(),
                random.nextFloat(), random.nextFloat()};
        double[] doubleVals = { random.nextDouble(), random.nextDouble(),
                random.nextDouble(), random.nextDouble(), random.nextDouble()};

        source.put(0, byteVals);
        source.put(5, charVals, order);
        source.put(15, shortVals, order);
        source.put(25, intVals, order);
        source.put(45, longVals, order);
        source.put(85, floatVals, order);
        source.put(105, doubleVals, order);

        assertArrayEquals(byteVals, source.get(0, new byte[5]));
        assertArrayEquals(charVals, source.get(5, new char[5], order));
        assertArrayEquals(shortVals, source.get(15, new short[5], order));
        assertArrayEquals(intVals, source.get(25, new int[5], order));
        assertArrayEquals(longVals, source.get(45, new long[5], order));
        assertArrayEquals(floatVals, source.get(85, new float[5], order));
        assertArrayEquals(doubleVals, source.get(105, new double[5], order));

        source.dealloc();

        source = mkSource(145);

        source.put(0, byteVals, 3, 2);
        source.put(5, charVals, 4, 1, order);
        source.put(15, shortVals, 3, 2, order);
        source.put(25, intVals, 4, 1, order);
        source.put(45, longVals, 3, 2, order);
        source.put(85, floatVals, 4, 1, order);
        source.put(105, doubleVals, 3, 2, order);

        byte[] byteExpected = new byte[5];
        char[] charExpected = new char[5];
        short[] shortExpected = new short[5];
        int[] intExpected = new int[5];
        long[] longExpected = new long[5];
        float[] floatExpected = new float[5];
        double[] doubleExpected = new double[5];

        System.arraycopy(byteVals, 3, byteExpected, 3, 2);
        System.arraycopy(charVals, 4, charExpected, 4, 1);
        System.arraycopy(shortVals, 3, shortExpected, 3, 2);
        System.arraycopy(intVals, 4, intExpected, 4, 1);
        System.arraycopy(longVals, 3, longExpected, 3, 2);
        System.arraycopy(floatVals, 4, floatExpected, 4, 1);
        System.arraycopy(doubleVals, 3, doubleExpected, 3, 2);


        assertArrayEquals(byteExpected, source.get(0, new byte[5], 3, 2));
        assertArrayEquals(charExpected, source.get(5, new char[5], 4, 1, order));
        assertArrayEquals(shortExpected, source.get(15, new short[5], 3, 2, order));
        assertArrayEquals(intExpected, source.get(25, new int[5], 4, 1, order));
        assertArrayEquals(longExpected, source.get(45, new long[5], 3, 2, order));
        assertArrayEquals(floatExpected, source.get(85, new float[5], 4, 1, order));
        assertArrayEquals(doubleExpected, source.get(105, new double[5], 3, 2, order));

    }

    @Test
    public void testNativeEndiannessScalars() {
        testScalars(MemoryAccessorOrder.NATIVE_ENDIANNESS);
    }

    @Test
    public void testNonNativeEndiannessScalars() {
        testScalars(MemoryAccessorOrder.NATIVE_ENDIANNESS.opposite());
    }

    @Test
    public void testNativeEndiannessBuffers() {
        testBuffers(MemoryAccessorOrder.NATIVE_ENDIANNESS);
    }
    @Test
    public void testNonNativeEndiannessBuffers() {
        testBuffers(MemoryAccessorOrder.NATIVE_ENDIANNESS.opposite());
    }

    @Test
    public void testSlicing() {
        RandomAccessSource source = mkSource(32);
        for (int i = 0; i < 32; i++) {
            source.put(i, (byte) i);
        }

        for (int i = 0; i < 32; i++) {
            RandomAccessSource slice = source.slice(i);

            assertEquals(32 - i, slice.length());
            assertEquals(i, slice.get(0));
        }

        source.dealloc();
    }

    @Test
    public void testOverflow() {
        RandomAccessSource source = mkSource(128);

        assertThrows(IllegalArgumentException.class, () -> source.put(129, (byte) 0));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, (byte) 0));
        assertDoesNotThrow(() -> source.put(127, (byte) 0));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(126, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(126, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(126, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(125, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(124, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(126, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(125, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(124, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(123, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(122, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(121, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(120, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(126, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(125, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(124, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));

        assertThrows(IllegalArgumentException.class, () -> source.put(129, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(128, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(127, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(126, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(125, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(124, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(123, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(122, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalArgumentException.class, () -> source.put(121, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertDoesNotThrow(() -> source.put(120, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
    }

    @Test
    public void testDeallocationState() {
        RandomAccessSource source = mkSource(128);

        source.dealloc();

        assertThrows(IllegalStateException.class, () -> source.put(5, (byte) 0));
        assertThrows(IllegalStateException.class, () -> source.put(5, (byte) 0));
        assertThrows(IllegalStateException.class, () -> source.put(5, (char) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalStateException.class, () -> source.put(5, (short) 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalStateException.class, () -> source.put(5, 0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalStateException.class, () -> source.put(5, 0L, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalStateException.class, () -> source.put(5, 0.0f, MemoryAccessorOrder.NATIVE_ENDIANNESS));
        assertThrows(IllegalStateException.class, () -> source.put(5, 0.0, MemoryAccessorOrder.NATIVE_ENDIANNESS));
    }

    private void testReadByteBuffer(Function<Integer, ByteBuffer> f) {
        Random random = new Random();

        byte[] data = new byte[1024];
        ByteBuffer bb = f.apply(1024);
        random.nextBytes(data);

        RandomAccessSource source = mkSource(1024);

        // #1: Test for data reading when offset is 0
        source.put(0, data);
        source.get(0, bb);

        assertEquals(1024, bb.position());

        byte[] finalData = new byte[1024];
        bb.position(0);
        bb.get(finalData);

        assertArrayEquals(data, finalData);

        bb.position(250);

        data = new byte[10];
        random.nextBytes(data);

        // #2: Test for data reading when offset is > 0
        source.put(50, data);
        source.get(50, bb);

        finalData = new byte[10];

        bb.position(250);
        bb.get(finalData);

        assertArrayEquals(data, finalData);
    }

    private void testWriteByteBuffer(Function<Integer, ByteBuffer> f) {
        Random random = new Random();

        byte[] data = new byte[1024];
        ByteBuffer bb = f.apply(1024);
        random.nextBytes(data);

        RandomAccessSource source = mkSource(1024);

        // #1: Test for data reading when offset is 0
        bb.mark();
        bb.put(data);
        bb.reset();

        source.put(0, bb);

        assertEquals(1024, bb.position());

        byte[] finalData = new byte[1024];
        source.get(0, finalData);

        assertArrayEquals(data, finalData);

        bb.position(250);

        data = new byte[10];
        random.nextBytes(data);

        // #2: Test for data reading when offset is > 0
        bb.mark();
        bb.put(data);
        bb.reset();

        source.put(50, bb);

        finalData = new byte[10];
        source.get(50, finalData);

        assertArrayEquals(data, finalData);
    }

    @Test
    public void testReadDirectByteBuffer() {
        testReadByteBuffer(ByteBuffer::allocateDirect);
    }

    @Test
    public void testReadNonDirectByteBuffer() {
        testReadByteBuffer(ByteBuffer::allocate);
    }

    @Test
    public void testReadNonDirectOffsetedByteBuffer() {
        testReadByteBuffer(integer -> {
            byte[] b = new byte[integer + 512];
            return ByteBuffer.wrap(b, 128, integer).slice();
        });
    }

    @Test
    public void testWriteDirectByteBuffer() {
        testWriteByteBuffer(ByteBuffer::allocateDirect);
    }

    @Test
    public void testWriteNonDirectByteBuffer() {
        testWriteByteBuffer(ByteBuffer::allocate);
    }

    @Test
    public void testWriteNonDirectOffsetedByteBuffer() {
        testWriteByteBuffer(integer -> {
            byte[] b = new byte[integer + 512];
            return ByteBuffer.wrap(b, 128, integer).slice();
        });
    }
}
