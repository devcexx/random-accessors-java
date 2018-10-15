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

/**
 * Represents a memory source that is backend in a System V shared memory region.
 */
public class SharedMemorySource extends DirectMemorySource {

    /**
     * Maps a shared memory block to the current process memory and returns a {@link SharedMemorySource}
     * able to read/write it.
     * @param key the key of the memory region.
     * @param size the size of the memory block that will be attached.
     * @param flags the system flags that will be used in the attach operation.
     * @return a {@link SharedMemorySource} attached to the memory region just attached to the process.
     */
    public static SharedMemorySource attach(long key, long size, int flags) {
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
    public static SharedMemorySource attach(String fkey, int kid, long size, int flags) {
        return attach(AccessorNatives.ftok(fkey, kid), size, flags);
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
    public static SharedMemorySource create(long key, long size, int flags, boolean persistant) {
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
    public static SharedMemorySource create(String fkey, int kid, long size, int flags, boolean persistant) {
        return create(AccessorNatives.ftok(fkey, kid), size, flags, !persistant);
    }

    private final int shmid;
    private final boolean destroyOnDealloc;
    public SharedMemorySource(int shmid, long address, long length, boolean destroyOnDealloc) {
        super(address, length);
        this.destroyOnDealloc = destroyOnDealloc;
        this.shmid = shmid;
    }

    /**
     * Detaches and destroys the current attached region from the system.
     */
    public void destroyRegion() {
        if (!deallocated) {
            detach();
            AccessorNatives.shmctl(shmid, AccessorNatives.IPC_RMID, 0);
            deallocated = true;
        }
    }

    /**
     * Detaches the current shared memory block from the process, without
     * removing it.
     */
    public void detach() {
        if (!deallocated) {
            AccessorNatives.shmdt(address());
            deallocated = true;
        }
    }

    @Override
    public void dealloc() {
        if (!deallocated) {
            if (destroyOnDealloc) {
                destroyRegion();
            } else {
                detach();
            }
            deallocated = true;
        }
    }
}
