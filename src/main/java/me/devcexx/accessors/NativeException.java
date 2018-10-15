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

public class NativeException extends RuntimeException {
    private final int errno;

    public NativeException(int errno) {
        this(errno, "An error has been raised during a native operation. Errno: " + errno);
    }

    public NativeException(int errno, String message) {
        this(errno, message, null);
    }

    public NativeException(int errno, String message, Throwable cause) {
        super(message, cause);
        this.errno = errno;
    }

    public NativeException(int errno, Throwable cause) {
        super(cause);
        this.errno = errno;
    }

    public int getErrno() {
        return errno;
    }
}
