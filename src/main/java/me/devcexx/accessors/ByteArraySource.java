package me.devcexx.accessors;

import java.nio.ByteBuffer;

public class ByteArraySource extends RandomAccessSource {
	private byte[] buf;
	private int off;

	protected ByteArraySource(byte[] buf, int off, int len) {
		super(len);

		Validate.checkInRange(buf.length, off, len);
		this.buf = buf;
		this.off = off;
	}

	@Override
	public ByteBuffer byteBuffer(long off, long length) {
		Validate.checkInRange(this.length, this.off + off, length);
		return ByteBuffer.wrap(buf, (int) (this.off + off), (int) length);
	}

	@Override
	public void clear(byte x, long off, long length) {
	    Validate.checkInRange(this.length, off, length);
		AccessorNatives.arrayMemset(buf, (int) (this.off + off), (int) length, x);
	}

	@Override
	public void dealloc() {
		buf = null;
		deallocated = true;
	}

	@Override
	public byte unsafeGet(long off) {
		return buf[(int) (this.off + off)];
	}

	@Override
	public short unsafeGetShort(long off, MemoryAccessorOrder order) {
		int pos = (int) (this.off + off);
		return Bits.decodeShort(buf[pos], buf[pos + 1], order);
	}

	@Override
	public char unsafeGetChar(long off, MemoryAccessorOrder order) {
		int pos = (int) (this.off + off);
		return Bits.decodeChar(buf[pos], buf[pos + 1], order);
	}

	@Override
	public int unsafeGetInt(long off, MemoryAccessorOrder order) {
        int pos = (int) (this.off + off);
        return Bits.decodeInt(buf[pos], buf[pos + 1], buf[pos + 2], buf[pos + 3], order);
	}

	@Override
	public long unsafeGetLong(long off, MemoryAccessorOrder order) {
        int pos = (int) (this.off + off);
        return Bits.decodeLong(buf[pos], buf[pos + 1], buf[pos + 2], buf[pos + 3],
                buf[pos + 4], buf[pos + 5], buf[pos + 6], buf[pos + 7], order);
	}

	@Override
	public float unsafeGetFloat(long off, MemoryAccessorOrder order) {
        int pos = (int) (this.off + off);
        return Bits.decodeFloat(buf[pos], buf[pos + 1], buf[pos + 2], buf[pos + 3], order);
	}

	@Override
	public double unsafeGetDouble(long off, MemoryAccessorOrder order) {
        int pos = (int) (this.off + off);
        return Bits.decodeDouble(buf[pos], buf[pos + 1], buf[pos + 2], buf[pos + 3],
                buf[pos + 4], buf[pos + 5], buf[pos + 6], buf[pos + 7], order);
	}

	@Override
	public void unsafeGet(long off, byte[] buffer, int dstOff, int len) {
        UnsafeMemory.copyArrayToArray(buf, (int) (this.off + off), buffer,
                dstOff, 1, len, MemoryAccessorOrder.NATIVE_ENDIANNESS);
	}

	@Override
	public void unsafeGet(long off, ByteBuffer buf) {
        if (buf.isDirect()) {
            UnsafeMemory.copyArrayToAddress(this.buf, (int) (this.off + off),
                    UnsafeMemory.addressOfByteBuffer(buf), 1, buf.remaining(),
                    MemoryAccessorOrder.NATIVE_ENDIANNESS);
        } else {
            UnsafeMemory.copyArrayToArray(this.buf, (int) (this.off + off),
                    buf.array(), buf.arrayOffset() + buf.position(), 1,
                    buf.remaining(), MemoryAccessorOrder.NATIVE_ENDIANNESS);
        }
	}

	@Override
	public void unsafeGet(long off, char[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buf, (int) (this.off + off), buffer,
                2 * dstOff, 2, len, order);
	}

	@Override
	public void unsafeGet(long off, short[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buf, (int) (this.off + off), buffer,
                2 * dstOff, 2, len, order);
	}

	@Override
	public void unsafeGet(long off, int[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buf, (int) (this.off + off), buffer,
                4 * dstOff, 4, len, order);
	}

	@Override
	public void unsafeGet(long off, long[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buf, (int) (this.off + off), buffer,
                8 * dstOff, 8, len, order);
	}

	@Override
	public void unsafeGet(long off, float[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buf, (int) (this.off + off), buffer,
                4 * dstOff, 4, len, order);
	}

	@Override
	public void unsafeGet(long off, double[] buffer, int dstOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buf, (int) (this.off + off), buffer,
                8 * dstOff, 8, len, order);
	}

	@Override
	public void unsafePut(long off, byte value) {
        buf[(int) (this.off + off)] = value;
	}

	@Override
	public void unsafePut(long off, short value, MemoryAccessorOrder order) {
        Bits.encodeShort(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, char value, MemoryAccessorOrder order) {
        Bits.encodeChar(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, int value, MemoryAccessorOrder order) {
        Bits.encodeInt(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, long value, MemoryAccessorOrder order) {
        Bits.encodeLong(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, float value, MemoryAccessorOrder order) {
        Bits.encodeFloat(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, double value, MemoryAccessorOrder order) {
        Bits.encodeDouble(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, byte[] buffer, int srcOff, int len) {
        UnsafeMemory.copyArrayToArray(buffer, srcOff, this.buf, (int) (this.off + off),
                1, len, MemoryAccessorOrder.NATIVE_ENDIANNESS);
	}

	@Override
	public void unsafePut(long off, ByteBuffer buf) {

	}

	@Override
	public void unsafePut(long off, short[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buffer, 2 * srcOff, this.buf, (int) (this.off + off),
                2, len, order);
	}

	@Override
	public void unsafePut(long off, char[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buffer, 2 * srcOff, this.buf, (int) (this.off + off),
                2, len, order);
	}

	@Override
	public void unsafePut(long off, int[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buffer, 4 * srcOff, this.buf, (int) (this.off + off),
                4, len, order);
	}

	@Override
	public void unsafePut(long off, long[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buffer, 8 * srcOff, this.buf, (int) (this.off + off),
                8, len, order);
	}

	@Override
	public void unsafePut(long off, float[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buffer, 4 * srcOff, this.buf, (int) (this.off + off),
                4, len, order);
	}

	@Override
	public void unsafePut(long off, double[] buffer, int srcOff, int len, MemoryAccessorOrder order) {
        UnsafeMemory.copyArrayToArray(buffer, 8 * srcOff, this.buf, (int) (this.off + off),
                8, len, order);
	}
}
