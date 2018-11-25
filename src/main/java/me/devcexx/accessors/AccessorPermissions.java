package me.devcexx.accessors;

public class AccessorPermissions {
    public static final int READ = 1;
    public static final int WRITE = 2;
    public static final int FULL_ACCESS = READ | WRITE;

    public static boolean isReadable(int flags) {
        return (flags & READ) > 0;
    }

    public static boolean isWritable(int flags) {
        return (flags & WRITE) > 0;
    }
}
