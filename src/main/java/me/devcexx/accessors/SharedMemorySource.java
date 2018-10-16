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
