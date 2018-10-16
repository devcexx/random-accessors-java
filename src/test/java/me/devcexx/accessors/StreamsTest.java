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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreamsTest {
    @Test
    public void testInputStream() throws IOException {
        DirectMemorySource source = Sources.alloc(150);
        byte[] data = new byte[150];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }

        source.put(0, data);

        try (RandomAccessSourceInputStream in = new RandomAccessSourceInputStream(source, 0, source.length())) {
            assertEquals(data.length, in.available());

            for (byte aData : data) {
                assertEquals(aData, (byte) in.read());
            }

            assertEquals(-1, in.read());

            assertEquals(0, in.available());
            in.seek(0);
            assertEquals(data.length, in.available());

            byte[] buf = new byte[10];
            byte[] expected = new byte[10];
            int read;
            int off = 0;

            while ((read = in.read(buf)) != 0) {
                System.arraycopy(data, off, expected, 0, 10);
                assertArrayEquals(expected, buf);
                off += read;
            }

            assertEquals(0, in.available());
        }
    }

    @Test
    public void testOutputStream() throws IOException {
        DirectMemorySource source = Sources.alloc(150);
        byte[] data = new byte[150];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }

        try (RandomAccessSourceOutputStream out = new RandomAccessSourceOutputStream(source, 0, source.length())) {
            for (byte d : data) {
                out.write(d);
            }
            assertArrayEquals(data, source.get(0, new byte[150]));
            out.seek(0);

            for (int i = 0; i < data.length; i += 10) {
                out.write(data, i, 10);
            }

            assertArrayEquals(data, source.get(0, new byte[150]));
        }
    }
}
