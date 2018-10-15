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

import java.nio.ByteOrder;

/**
 * Represents the order of the bytes in a byte buffer.
 */
public enum MemoryAccessorOrder {

    /**
     * Represents the Big Endian order, where the most significant bytes of a number are placed at the left.
     */
    BIG_ENDIAN(1, ByteOrder.BIG_ENDIAN),

    /**
     * Represents the Little Endian order, where the most significant bytes of a number are placed at the right.
     */
    LITTLE_ENDIAN(2, ByteOrder.LITTLE_ENDIAN);

    /**
     * The native endianness of the current machine.
     */
    public static final MemoryAccessorOrder NATIVE_ENDIANNESS =
            ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ? BIG_ENDIAN : LITTLE_ENDIAN;

    /**
     * The id of the order
     */
    public final int id;

    /**
     * The Java Nio instance linked to the current endianness.
     */
    public final ByteOrder nioOrder;

    MemoryAccessorOrder(int id, ByteOrder order) {
        this.id = id;
        this.nioOrder = order;
    }

    /**
     * Returns the opposite byte order to the actual.
     */
    public MemoryAccessorOrder opposite() {
        return this == BIG_ENDIAN ? LITTLE_ENDIAN : BIG_ENDIAN;
    }

    public boolean isNative() {
        return this == NATIVE_ENDIANNESS;
    }

    /**
     * Returns the buffer order that are linked to the specified java nio byte order.
     */
    public static MemoryAccessorOrder fromNioOrder(ByteOrder order) {
        if (order == null) throw new NullPointerException();

        if (order == ByteOrder.BIG_ENDIAN) return BIG_ENDIAN;
        else return LITTLE_ENDIAN;
    }
}
