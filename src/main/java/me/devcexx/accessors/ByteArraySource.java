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

    /**
     * Returns the underlying array of this source.
     */
	public byte[] array() {
	    if (deallocated())
	        throw new IllegalStateException("The source was deallocated and the " +
                    "backing array cannot be retrieved");
	    return buf;
    }

    /**
     * Returns the offset from the beginning of the underlying array where
     * the current source starts from.
     */
    public int arrayOffset() {
	    return off;
    }

	@Override
	public ByteBuffer byteBuffer(long off, long length) {
		Validate.checkInRange(this.length, this.off + off, length);

		//The #wrap(byte[], int, int) method just return
        //an ByteBuffer initialized with a non-zero position and a limit set.
        //We are calling #slice so we can return a buffer
        //actually limited to the requested range.
		return ByteBuffer.wrap(buf, (int) (this.off + off), (int) length).slice();
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
	public short unsafeGetShort(long off, DataOrder order) {
		int pos = (int) (this.off + off);
		return Bits.decodeShort(buf[pos], buf[pos + 1], order);
	}

	@Override
	public char unsafeGetChar(long off, DataOrder order) {
		int pos = (int) (this.off + off);
		return Bits.decodeChar(buf[pos], buf[pos + 1], order);
	}

	@Override
	public int unsafeGetInt(long off, DataOrder order) {
        int pos = (int) (this.off + off);
        return Bits.decodeInt(buf[pos], buf[pos + 1], buf[pos + 2], buf[pos + 3], order);
	}

	@Override
	public long unsafeGetLong(long off, DataOrder order) {
        int pos = (int) (this.off + off);
        return Bits.decodeLong(buf[pos], buf[pos + 1], buf[pos + 2], buf[pos + 3],
                buf[pos + 4], buf[pos + 5], buf[pos + 6], buf[pos + 7], order);
	}

	@Override
	public float unsafeGetFloat(long off, DataOrder order) {
        int pos = (int) (this.off + off);
        return Bits.decodeFloat(buf[pos], buf[pos + 1], buf[pos + 2], buf[pos + 3], order);
	}

	@Override
	public double unsafeGetDouble(long off, DataOrder order) {
        int pos = (int) (this.off + off);
        return Bits.decodeDouble(buf[pos], buf[pos + 1], buf[pos + 2], buf[pos + 3],
                buf[pos + 4], buf[pos + 5], buf[pos + 6], buf[pos + 7], order);
	}

	@Override
	public void unsafeGet(long off, byte[] buffer, int dstOff, int len) {
        Unsafe.copyArrayToArray(buf, (int) (this.off + off), buffer,
                dstOff, 1, len, DataOrder.NATIVE_ORDER);
	}

	@Override
	public void unsafeGet(long off, ByteBuffer buf) {
        if (buf.isDirect()) {
            Unsafe.copyArrayToAddress(this.buf, (int) (this.off + off),
                    Unsafe.addressOfByteBuffer(buf) + buf.position(), 1,
                    buf.remaining(), DataOrder.NATIVE_ORDER);
        } else {
            Unsafe.copyArrayToArray(this.buf, (int) (this.off + off),
                    buf.array(), buf.arrayOffset() + buf.position(), 1,
                    buf.remaining(), DataOrder.NATIVE_ORDER);
        }
        buf.position(buf.position() + buf.remaining());
	}

	@Override
	public void unsafeGet(long off, char[] buffer, int dstOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buf, (int) (this.off + off), buffer,
                2 * dstOff, 2, len, order);
	}

	@Override
	public void unsafeGet(long off, short[] buffer, int dstOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buf, (int) (this.off + off), buffer,
                2 * dstOff, 2, len, order);
	}

	@Override
	public void unsafeGet(long off, int[] buffer, int dstOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buf, (int) (this.off + off), buffer,
                4 * dstOff, 4, len, order);
	}

	@Override
	public void unsafeGet(long off, long[] buffer, int dstOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buf, (int) (this.off + off), buffer,
                8 * dstOff, 8, len, order);
	}

	@Override
	public void unsafeGet(long off, float[] buffer, int dstOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buf, (int) (this.off + off), buffer,
                4 * dstOff, 4, len, order);
	}

	@Override
	public void unsafeGet(long off, double[] buffer, int dstOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buf, (int) (this.off + off), buffer,
                8 * dstOff, 8, len, order);
	}

	@Override
	public void unsafePut(long off, byte value) {
        buf[(int) (this.off + off)] = value;
	}

	@Override
	public void unsafePut(long off, short value, DataOrder order) {
        Bits.encodeShort(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, char value, DataOrder order) {
        Bits.encodeChar(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, int value, DataOrder order) {
        Bits.encodeInt(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, long value, DataOrder order) {
        Bits.encodeLong(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, float value, DataOrder order) {
        Bits.encodeFloat(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, double value, DataOrder order) {
        Bits.encodeDouble(value, buf, (int) (this.off + off), order);
	}

	@Override
	public void unsafePut(long off, byte[] buffer, int srcOff, int len) {
        Unsafe.copyArrayToArray(buffer, srcOff, this.buf, (int) (this.off + off),
                1, len, DataOrder.NATIVE_ORDER);
	}

	@Override
	public void unsafePut(long off, ByteBuffer buf) {
        if (buf.isDirect()) {
            Unsafe.copyMemBlockToArray(Unsafe.addressOfByteBuffer(buf) + buf.position(),
                    this.buf, (int) (this.off + off), 1, buf.remaining(),
                    DataOrder.NATIVE_ORDER);
        } else {
            Unsafe.copyArrayToArray(buf.array(), buf.arrayOffset() + buf.position(),
                    this.buf, (int) (this.off + off), 1, buf.remaining(),
                    DataOrder.NATIVE_ORDER);
        }
        buf.position(buf.position() + buf.remaining());
	}

	@Override
	public void unsafePut(long off, short[] buffer, int srcOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buffer, 2 * srcOff, this.buf, (int) (this.off + off),
                2, len, order);
	}

	@Override
	public void unsafePut(long off, char[] buffer, int srcOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buffer, 2 * srcOff, this.buf, (int) (this.off + off),
                2, len, order);
	}

	@Override
	public void unsafePut(long off, int[] buffer, int srcOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buffer, 4 * srcOff, this.buf, (int) (this.off + off),
                4, len, order);
	}

	@Override
	public void unsafePut(long off, long[] buffer, int srcOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buffer, 8 * srcOff, this.buf, (int) (this.off + off),
                8, len, order);
	}

	@Override
	public void unsafePut(long off, float[] buffer, int srcOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buffer, 4 * srcOff, this.buf, (int) (this.off + off),
                4, len, order);
	}

	@Override
	public void unsafePut(long off, double[] buffer, int srcOff, int len, DataOrder order) {
        Unsafe.copyArrayToArray(buffer, 8 * srcOff, this.buf, (int) (this.off + off),
                8, len, order);
	}
}
