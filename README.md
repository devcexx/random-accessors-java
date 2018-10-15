# RANDOM ACCESSORS FOR JAVA

This library is an utility that exposes abstract classes and utilities
to read and write data from random access sources. Currently, this
library includes classes that allows to read and write data directly
from memory blocks allocated inside and outside the JVM, and System V
shared memory segments. This I/O operations have been optimized to
have a good performance in reading and writing huge amount of data,
using any byte order. It is expected in the future to add more data
sources to work with, and complete the existing ones.

*WARNING*: This library has only been tested in x86 and x64
architectures with unaligned memory access. It might fail in other
scenarios.

*WARNING*: This library is *not* compatible with Java 9+
(yet), as it uses reflection operations restricted by this version of
Java.

## NATIVES

Since this library performs direct memory access, it includes some
code in C that must be compiled and loaded in order to make the
library work. The repository includes some pre-compiled binaries for
Linux/macOS with generic x86 and x64 processors. Nevertheless, it is
recommended to compile the binaries by yourself in the machine that
will run the library, since it may take advantage of the instructions
sets of its processor to improve a little bit the performance of the
library, especially in I/O operations that requires changing the byte
order of the input data (this task might be faster in processors with
vectorized instructions sets, for example). Binaries for macOS x86 are
not included in the repository because the new macOS systems won't support 
x86 binaries.

### Operating system support

The library has been tested in macOS and Linux, but should work in any
other POSIX-compliant operating system. I don't give a cat about
Windows.

### Natives load

The library will recognize three different filenames. Supposing that
we're on a Linux system, the recognized filenames are:

1. libaccessors.so: It is expected to contain the most native binary
   for the current platform.
2. libaccessors32.so or libaccessors64.so: They are expected to
   contain the most generic binaries for x86 and x64 architectures,
   respectively.

The library will try to load the first available binary, using the
priority defined in the list above. First of all will try to find the
binaries in the folders specified in the `java.library.path`. If
they're not present there, it will try to load them from the java
classpath, copying them to a temporary location during the execution
of the JVM. This mean that you can install the libraries on your
system, or bundle it in your jar or classpath, without changing any
other environment variable or JVM configuration.

### Compiling the natives

The repository includes different scripts to compile the natives by
yourself:

* `build_generic_natives_*.sh` will allow you to compile generic
  natives for x86 and x64 platforms (the ones included in the
  repository).
* `build_natives_*.sh` will allow you to compile a native library
  targeted to your current platform.

Both scripts requires a compiler that satisfies the GCC dependency
(for example, clang in macOS). Also, both scripts will output the
results to the src/main/resources folder.

## License

This software is under the GNU GPL v3 license. This mean, in a
nutshell, that you can freely use and distribute open source software
that uses this library, but you cannot use it for privative projects
that you are intending to distribute. For more details about this
license, please refer to the file LICENSE present in this repository.
