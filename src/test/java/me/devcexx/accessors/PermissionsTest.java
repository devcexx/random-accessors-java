package me.devcexx.accessors;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PermissionsTest {
    @Test
    public void testInitialPermissions() {
        int perms = Sources.fromArray(new byte[1]).getPermissions();

        assertTrue(AccessorPermissions.isReadable(perms));
        assertTrue(AccessorPermissions.isWritable(perms));
    }

    @Test
    public void testPermissionMasking() {
        RandomAccessSource src1 = Sources.fromArray(new byte[1]);
        RandomAccessSource readableNoWritable1 = src1.withPermissions(AccessorPermissions.READ);
        RandomAccessSource writableNoReadable1 = src1.withPermissions(AccessorPermissions.WRITE);

        RandomAccessSource readableNoWritable2 = readableNoWritable1.withPermissions(AccessorPermissions.FULL_ACCESS);
        RandomAccessSource writableNoReadable2 = writableNoReadable1.withPermissions(AccessorPermissions.FULL_ACCESS);

        RandomAccessSource noReadableNoWritable1 = readableNoWritable2.withPermissions(AccessorPermissions.WRITE);
        RandomAccessSource noReadableNoWritable2 = writableNoReadable2.withPermissions(AccessorPermissions.READ);

        RandomAccessSource src2 = Sources.fromArray(new byte[1]).withPermissions(AccessorPermissions.FULL_ACCESS);
        RandomAccessSource readableNoWritable3 = src1.withPermissions(AccessorPermissions.READ);
        RandomAccessSource writableNoReadable3 = src1.withPermissions(AccessorPermissions.WRITE);

        assertTrue(AccessorPermissions.isReadable(readableNoWritable1.getPermissions()));
        assertFalse(AccessorPermissions.isWritable(readableNoWritable1.getPermissions()));

        assertFalse(AccessorPermissions.isReadable(writableNoReadable1.getPermissions()));
        assertTrue(AccessorPermissions.isWritable(writableNoReadable1.getPermissions()));

        assertTrue(AccessorPermissions.isReadable(readableNoWritable2.getPermissions()));
        assertFalse(AccessorPermissions.isWritable(readableNoWritable2.getPermissions()));

        assertFalse(AccessorPermissions.isReadable(writableNoReadable2.getPermissions()));
        assertTrue(AccessorPermissions.isWritable(writableNoReadable2.getPermissions()));

        assertFalse(AccessorPermissions.isReadable(noReadableNoWritable1.getPermissions()));
        assertFalse(AccessorPermissions.isWritable(noReadableNoWritable1.getPermissions()));

        assertFalse(AccessorPermissions.isReadable(noReadableNoWritable2.getPermissions()));
        assertFalse(AccessorPermissions.isWritable(noReadableNoWritable2.getPermissions()));

        assertTrue(AccessorPermissions.isReadable(src2.getPermissions()));
        assertTrue(AccessorPermissions.isWritable(src2.getPermissions()));

        assertTrue(AccessorPermissions.isReadable(readableNoWritable3.getPermissions()));
        assertFalse(AccessorPermissions.isWritable(readableNoWritable3.getPermissions()));

        assertFalse(AccessorPermissions.isReadable(writableNoReadable3.getPermissions()));
        assertTrue(AccessorPermissions.isWritable(writableNoReadable3.getPermissions()));
    }

    @Test
    public void testPermissionAppliance() {
        byte[] array = new byte[16];
        RandomAccessSource srcRead = Sources.fromArray(array).withPermissions(AccessorPermissions.READ);
        RandomAccessSource srcWrite = Sources.fromArray(array).withPermissions(AccessorPermissions.WRITE);

        assertEquals("Cannot perform a write operation on this source",
                assertThrows(IllegalStateException.class, () -> srcRead.put(0, (byte) 5)).getMessage());
        assertEquals(0, srcRead.get(0));

        assertEquals("Cannot perform a read operation on this source",
                assertThrows(IllegalStateException.class, () -> srcWrite.get(0)).getMessage());

    }
}
