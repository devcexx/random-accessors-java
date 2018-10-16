package me.devcexx.accessors;

public class ByteArraySourceTest extends AbstractRandomAccessorTest {
    @Override
    protected RandomAccessSource mkSource(long size) {
        return new ByteArraySource(new byte[(int) size], 0, (int) size);
    }
}
