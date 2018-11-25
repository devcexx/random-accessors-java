package me.devcexx.accessors;

import java.nio.ByteBuffer;

/**
 * Helper class for creating sources
 */
public class Sources {
    /**
     * Allocates a new memory block with the specified size and attaches a new {@link DirectMemorySource}
     * to it.
     * @param size the block size as a non negative long value.
     * @return a new {@link DirectMemorySource} attached to the just allocated memory region.
     */
    public static DirectMemorySource alloc(long size) {
        return new DirectMemorySource(Unsafe.alloc(size), size);
    }

    /**
     * Allocates a new memory block with the specified size, filling it with zeroes,
     * and attaches a new {@link DirectMemorySource} to it.
     * @param size the block size as a non negative long value.
     * @return a new {@link DirectMemorySource} attached to the just allocated memory region.
     */
    public static DirectMemorySource calloc(long size) {
        return new DirectMemorySource(Unsafe.allocAndSet(size, (byte) 0), size);
    }

    /**
     * Allocates a new memory block with the specified size, filling it with the given data,
     * and attaches a new {@link DirectMemorySource} to it.
     * @param size the block size as a non negative long value.
     * @param data the data that will be used to fill the new memory region.
     * @return a new {@link DirectMemorySource} attached to the just allocated memory region.
     */
    public static DirectMemorySource allocAndSet(long size, byte data) {
        return new DirectMemorySource(Unsafe.allocAndSet(size, data), size);
    }

    /**
     * Creates a source from a whole byte buffer. This call is equivalent
     * to the call to {@code #fromByteBuffer(bf, false)}.
     * @param bf a byte buffer that can be, either a direct buffer or a byte
     *           array backed buffer.
     */
    public static RandomAccessSource fromByteBuffer(ByteBuffer bf) {
        return fromByteBuffer(bf, false);
    }

    /**
     * Creates a source from a whole byte buffer. This call is equivalent
     * to the call to {@code #fromByteBuffer(bf, false)}.
     * @param bf a byte buffer that can be, either a direct buffer or a byte
     *           array backed buffer.
     * @param clipToState if true, the returned source will be bounded to
     *                    the current position and limit of the byte buffer.
     *                    Otherwise, the returned source will fit the whole
     *                    byte buffer.
     */
    public static RandomAccessSource fromByteBuffer(ByteBuffer bf, boolean clipToState) {
        if (bf.isDirect()) {
            if (clipToState) {
                return new DirectNioBufferSource(bf).slice(bf.position(), bf.remaining());
            } else {
                return new DirectNioBufferSource(bf);
            }
        } else if (bf.hasArray()) {
            if (clipToState) {
                return new ByteArraySource(bf.array(), bf.arrayOffset()
                        + bf.position(), bf.remaining());
            } else {
                return new ByteArraySource(bf.array(), bf.arrayOffset(),
                        bf.array().length - bf.arrayOffset());
            }
        } else {
            throw new IllegalArgumentException("Unknown ByteBuffer type. Should be " +
                    "either a direct buffer or a byte array backed buffer");
        }
    }

    /**
     * Creates a source backed in a byte array.
     * @param buf the byte array that will be wrapped.
     */
    public static ByteArraySource fromArray(byte[] buf) {
        return new ByteArraySource(buf, 0, buf.length);
    }

    /**
     * Creates a source backed in a byte array.
     * @param buf the byte array that will be wrapped.
     * @param off the starting offset from the beginning of the array.
     * @param len the total length of the source.
     */
    public static ByteArraySource fromArray(byte[] buf, int off, int len) {
        return new ByteArraySource(buf, off, len);
    }

    /**
     * Creates a new byte array and wraps it into a byte array source.
     * @param size the length of the new array.
     */
    public static ByteArraySource fromNewArray(int size) {
        return new ByteArraySource(new byte[size], 0, size);
    }

    /**
     * Maps a shared memory block to the current process memory and returns a {@link SharedMemorySource}
     * able to read/write it.
     * @param key the key of the memory region.
     * @param size the size of the memory block that will be attached.
     * @param flags the system flags that will be used in the attach operation.
     * @return a {@link SharedMemorySource} attached to the memory region just attached to the process.
     */
    public static SharedMemorySource attachToSharedMemorySegment(long key, long size, int flags) {
        int shmid = AccessorNatives.shmget(key, size, flags);
        long address = AccessorNatives.shmat(shmid, 0, 0);
        return new SharedMemorySource(shmid, address, size, false);
    }

    /**
     * Maps a shared memory block to the current process memory and returns a {@link SharedMemorySource}
     * able to read it, using as region key the result of a {@code ftok} syscall.
     * @param fkey the path of a file that will be used as the argument of the {@code ftok} syscall.
     * @param kid a numeric identifier that will be used as the argument of the {@code ftok} syscall.
     * @param size the size of the memory block that will be attached.
     * @param flags the system flags that will be used in the attach operation.
     * @return a {@link SharedMemorySource} attached to the memory region just attached to the process.
     */
    public static SharedMemorySource attachToSharedMemorySegment(String fkey, int kid, long size, int flags) {
        return attachToSharedMemorySegment(AccessorNatives.ftok(fkey, kid), size, flags);
    }

    /**
     * Creates a new shared memory block in the system and returns a {@link SharedMemorySource}
     * able to read/write it.
     * @param key the key of the memory region.
     * @param size the size of the memory block that will be attached.
     * @param flags the system flags that will be used in the attach operation.
     * @param persistant a value indicating if the memory block should be destroyed after the
     *                   deallocation of the current accessor, or should keep existing after that.
     * @return a {@link SharedMemorySource} attached to the memory region just attached to the process.
     */
    public static SharedMemorySource createSharedMemorySegment(long key, long size, int flags, boolean persistant) {
        int shmid = AccessorNatives.shmget(key, size, flags | AccessorNatives.IPC_CREAT);
        long address = AccessorNatives.shmat(shmid, 0, 0);
        return new SharedMemorySource(shmid, address, size, !persistant);
    }

    /**
     * Creates a new shared memory block in the system and returns a {@link SharedMemorySource}
     * able to read/write it, using as region key the result of a {@code ftok} syscall.
     * @param fkey the path of a file that will be used as the argument of the {@code ftok} syscall.
     * @param kid a numeric identifier that will be used as the argument of the {@code ftok} syscall.
     * @param size the size of the memory block that will be attached.
     * @param flags the system flags that will be used in the attach operation.
     * @param persistant a value indicating if the memory block should be destroyed after the
     *                   deallocation of the current accessor, or should keep existing after that.
     * @return a {@link SharedMemorySource} attached to the memory region just attached to the process.
     */
    public static SharedMemorySource createSharedMemorySegment(String fkey, int kid, long size, int flags, boolean persistant) {
        return createSharedMemorySegment(AccessorNatives.ftok(fkey, kid), size, flags, !persistant);
    }
}
