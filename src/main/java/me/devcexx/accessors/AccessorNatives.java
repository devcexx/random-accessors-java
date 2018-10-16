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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class that gives access to the native code for raw write in direct buffers.
 */
public class AccessorNatives {

    /**
     * Holds the native IPC_CREAT constant value.
     */
    public static final int IPC_CREAT;

    /**
     * Holds the native IPC_RMID constant value.
     */
    public static final int IPC_RMID;

    private static final int CONSTANT_ID_IPC_CREAT = 1;
    private static final int CONSTANT_ID_IPC_RMID = 2;

    /**
     * Holds the value returned by {@link #copyMemory(Object, long, long, Object, long, long, int, long, int, int)}
     * when the copy has been performed successfully.
     */
    public static final int CPYMEM_ERRNO_OK = 0;

    /**
     * Holds the value returned by {@link #copyMemory(Object, long, long, Object, long, long, int, long, int, int)}
     * when the given data size is invalid.
     */
    public static final int CPYMEM_ERRNO_INVALID_DATASIZE = 1;

    /**
     * Copies a memory block.
     * @param arrayIn The java array where the data will be read. If null, the data will be copied from the specified addressIn.
     * @param addressIn the address of the data to copy.
     * @param offsetIn the offset from the beginning of the data where the copy will begin.
     * @param arrayOut the java array where the data will be placed. if null, the data will be copied from the specified addressOut.
     * @param addressOut the address of the output buffer.
     * @param offsetOut the offset from the beginning of the output buffer where the data will be copied.
     * @param dataSize the size of each item of the buffer (1 for bytes, 2 for short and char, 4 for int, etc)
     * @param count the number of items to copy. Both the input buffer and the output buffer must have at least
     *              count * dataSize of available space. The native code do not check for the size of the buffer and,
     *              if this is not correctly set, the JVM may crash.
     * @param srcEndianness the endianness of the input data. See {@link DataOrder#id}
     * @param dstEndianness the endianness of the output data. See {@link DataOrder#id}
     */
    public static native int copyMemory(Object arrayIn, long addressIn, long offsetIn, Object arrayOut,
                                         long addressOut, long offsetOut, int dataSize, long count,
                                         int srcEndianness, int dstEndianness);

    private static native long native_ftok(String path, int id);
    private static native int native_shmget(long key, long size, int flags);
    private static native long native_shmat(int id, long addr, int flags);
    private static native long native_shmdt(long addr);
    private static native int native_shmctl(int id, int cmd, long ds);

    /**
     * Get the last error code issued by the system for the current process.
     * @return the last error code of the system.
     */
    public static native int errno();

    /**
     * Get an human readable description of a system error with the specified
     * error code.
     * @param error the system error code.
     * @return a string describing the given error code.
     */
    public static native String strerror(int error);
    private static native int constantValueOf(int constantId);

    private static String exLastError() {
        int errno = errno();
        return "Error: " + errno + ". " + strerror(errno);
    }

    /**
     * Managed call to the function ftok of the system.
     *
     * Convert a pathname and a project identifier to a System V IPC key
     * @param path the path name.
     * @param id the project identifier.
     * @return an unique System V IPC Key, expressed as a 64-bit value.
     * @throws NativeException if the function has returned with an error.
     */
    public static long ftok(String path, int id) {
        long r = native_ftok(path, id);
        if (r == -1) {
            throw new NativeException(errno(), "Failed to get key for file " + path + ". " + exLastError());
        }
        return r;
    }

    /**
     * Managed call to the function shmget of the system.
     *
     * Allocateds a System V shared memory segment.
     * @param key the key of the new segment.
     * @param size the size, in bytes, of the segment.
     * @param flags the flags for the segment creation.
     * @return a valid shared memory identifier.
     * @throws NativeException if the function has returned with an error.
     */
    public static int shmget(long key, long size, int flags) {
        int r = native_shmget(key, size, flags);
        if (r == -1) {
            throw new SharedMemoryException(errno(), "Failed to get shared buffer. " + exLastError());
        }
        return r;
    }

    /**
     * Managed call to the function shmat of the system.
     *
     * Attaches the shared memory segment associated with the shared memory identifier
     * specified by id to the address space of the calling process
     * @param id a valid shared memory identifier.
     * @param addr the attaching address.
     * @param flags the flags of the operation.
     * @return the base address of the attached segment.
     * @throws NativeException if the function has returned with an error.
     */
    public static long shmat(int id, long addr, int flags) {
        long r = native_shmat(id, addr, flags);
        if (r == -1) {
            throw new SharedMemoryException(errno(), "Failed to attach shared buffer. " + exLastError());
        }
        return r;
    }

    /**
     * Managed call to the function shmdt of the system.
     *
     * Detaches a shared memory segment from the process.
     * @param addr the address that will be detached.
     * @return the value 0.
     * @throws NativeException if the function has returned with an error.
     */
    public static long shmdt(long addr) {
        long r = native_shmdt(addr);
        if (r == -1) {
            throw new SharedMemoryException(errno(), "Failed to detach shared buffer. " + exLastError());
        }
        return r;
    }

    /**
     * Managed call to the function shmctl of the system.
     *
     * Changes the configuration of some shared memory region of the system.
     * @param id the id of the region.
     * @param cmd the command to execute.
     * @param ds the param of the operation.
     * @return the result of the operation.
     */
    public static long shmctl(int id, int cmd, long ds) {
        long r = native_shmctl(id, cmd, ds);
        if (r == -1) {
            throw new SharedMemoryException(errno(), "Failed to call shared memory control function. " + exLastError());
        }
        return r;
    }

    private static String LIB_NAME = "accessors";
    private static void loadNatives() {
        //Attempt to load most specific library for this system
        //(with no arch specifier)
        try {
            System.loadLibrary(LIB_NAME);
            return;
        } catch (UnsatisfiedLinkError ex) { }
        int arch = Unsafe.wordSize() * 8;

        //Attempt to load CPU generic library (with arch specifier)
        try {
            System.loadLibrary(LIB_NAME + arch);
            return;
        } catch (UnsatisfiedLinkError ex) { }

        //Library is not present on the java.library.path. Try to
        //load it for the resources.
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String tmpFolder = System.getProperty("java.io.tmpdir");
        File outFile;
        try {
            outFile = File.createTempFile(System.mapLibraryName(LIB_NAME + "-" + format.format(new Date())), "");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        String[] libraryNames = { System.mapLibraryName(LIB_NAME),
                System.mapLibraryName(LIB_NAME + arch) };

        for (String libraryName : libraryNames) {
            InputStream in = AccessorNatives.class.getResourceAsStream("/" + libraryName);
            if (in != null){
                OutputStream out = null;

                try {
                    out = new FileOutputStream(outFile, false);

                    byte[] buf = new byte[2048];
                    int read;

                    while ((read = in.read(buf)) != -1) {
                        out.write(buf, 0, read);
                    }
                } catch (IOException ex2) {
                    throw new ExceptionInInitializerError(ex2);
                } finally {
                    if (out != null) try { out.close(); } catch (Exception ignored) {}
                    try { in.close(); } catch (Exception ignored) {}
                }
                outFile.deleteOnExit();
                System.load(outFile.toString());
                return;
            }
        }

        throw new ExceptionInInitializerError("Cannot load accessors native library");
    }

    public static native long arrayMemset(Object object, int offset, int length, byte value);

    static {
        loadNatives();

        IPC_CREAT = constantValueOf(CONSTANT_ID_IPC_CREAT);
        IPC_RMID = constantValueOf(CONSTANT_ID_IPC_RMID);
    }
}
