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

class Validate {
    public static void checkInRange(long totalLength, long off, long n) {
        if (off < 0) {
            throw new IllegalArgumentException("Offset cannot be less than 0");
        }

        if (n < 0) {
            throw new IllegalArgumentException("Length cannot be less than 0");
        }

        if (totalLength - off < n) throw new IllegalArgumentException(
                "Cannot operate with a byte buffer out of its bounds. Offset: "
                        + off + "; Operation totalLength: "
                        + n + " bytes; buffer totalLength: "
                        + totalLength);
    }
}
