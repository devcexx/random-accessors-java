package me.devcexx.accessors;

public class OffsetedByteArraySourceTest extends AbstractRandomAccessorTest {
    @Override
    protected RandomAccessSource mkSource(long size) {
        return new ByteArraySource(new byte[(int) (size + 512)], 512, (int) size);
    }
}
