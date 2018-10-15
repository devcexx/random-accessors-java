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

#include "me_devcexx_accessors_AccessorNatives.h"
#include <sys/ipc.h>
#include <sys/shm.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <string.h>
#include <stdint.h>

/*
 * Flips the bytes of an unsigned 16-bit value.
 */
static inline __attribute__ (( always_inline )) uint16_t flip16(uint16_t x) {
  return (((x & 0xff00) >> 8) | (x << 8));
}

/*
 * Flips the bytes of four unsigned 16-bit value,
 * passed as a 64-bit value.
 */
static inline __attribute__ (( always_inline )) uint64_t flip16_quad(uint64_t x) {
  return ((x & 0xff00ff00ff00ff00) >> 8) | ((x & 0x00ff00ff00ff00ff) << 8);
}

/*
 * Flips the bytes of an unsigned 32-bit value.
 */
static inline __attribute__ (( always_inline )) uint32_t flip32(uint32_t x) {
  return (x << 24) |
    ((x <<  8) & 0x00ff0000) |
    ((x >>  8) & 0x0000ff00) |
    ((x >> 24) & 0x000000ff);
}

/*
 * Flips the bytes of two unsigned 32-bit value,
 * passed as a 64-bit value.
 */
static inline __attribute__ (( always_inline )) uint64_t flip32_dual(uint64_t x)
{
  x = (x & 0x0000ffff0000ffff) << 16 | (x & 0xffff0000ffff0000) >> 16;
  x = (x & 0x00ff00ff00ff00ff) << 8  | (x & 0xff00ff00ff00ff00) >> 8;
  return x;
}

/*
 * Flips the bytes of an unsigned 64-bit value.
 */
static inline __attribute__ (( always_inline )) uint64_t flip64(uint64_t x) {
  x = (x & 0x00000000ffffffff) << 32 | (x & 0xffffffff00000000) >> 32;
  x = (x & 0x0000ffff0000ffff) << 16 | (x & 0xffff0000ffff0000) >> 16;
  x = (x & 0x00ff00ff00ff00ff) << 8  | (x & 0xff00ff00ff00ff00) >> 8;
  return x;
}

/*
 * Copy n 16-bit items from the in pointer to the out pointer
 * reversing the bytes of each item.
 */
void cpymem_flip16(char* out, char* in, uint64_t n) {
  uint64_t c = n / 4;
  uint64_t* ptr_in = (uint64_t*) in;
  uint64_t* ptr_out = (uint64_t*) out;

  while (c-- > 0) {
    *ptr_out++ = flip16_quad(*ptr_in++);
  }

  uint16_t rem = n % 4;

  if (rem > 0) {
    uint16_t *ptr16_in = (uint16_t*) ptr_in;
    uint16_t *ptr16_out = (uint16_t*) ptr_out;

    while (rem-- > 0) {
      *ptr16_out++ = flip16(*ptr16_in++);
    }
  }
}

/*
 * Copy n 32-bit items from the in pointer to the out pointer
 * reversing the bytes of each item.
 */
void cpymem_flip32(char* out, char* in, uint64_t n) {
  uint64_t c = n / 2;
  
  unsigned long* ptr_in = (unsigned long*) in;
  unsigned long* ptr_out = (unsigned long*) out;
  
  while (c-- > 0) {
    *ptr_out++ = flip32_dual(*ptr_in++);
  }

  if (n % 2 != 0) {
    unsigned int* ptr_in32 = (unsigned int*) ptr_in;
    unsigned int* ptr_out32 = (unsigned int*) ptr_out;
    *ptr_out32 = flip32(*ptr_in32);
  }
}

/*
 * Copy n 64-bit items from the in pointer to the out pointer
 * reversing the bytes of each item.
 */
void cpymem_flip64(char* out, char* in, uint64_t n) {
  uint64_t* ptr_in = (uint64_t*) in;
  uint64_t* ptr_out = (uint64_t*) out;

  while (n-- > 0) {
    *ptr_out++ = flip64(*ptr_in++);
  }
}

/*
 * Class:     me_devcexx_accessors_AccessorNatives
 * Method:    copyMemory
 * Signature: (Ljava/lang/Object;JJLjava/lang/Object;JJIJII)I
 */
JNIEXPORT jint JNICALL Java_me_devcexx_accessors_AccessorNatives_copyMemory
(JNIEnv * env, jclass clazz, jobject arrayIn, jlong addressIn, jlong offsetIn, jobject arrayOut, jlong addressOut,
 jlong offsetOut, jint dataSize, jlong count, jint srcEndianness, jint dstEndianness) {
  char* in;
  char* out;
  int c_in_off;
  int c_out_off;

  if (arrayIn == NULL) {
    in = (char *) (intptr_t) addressIn;
  } else {
    in = (char *) (*env)->GetPrimitiveArrayCritical(env, arrayIn, 0);
  }

  in += offsetIn;

  if (arrayOut == NULL) {
    out = (char *) (intptr_t) addressOut;
  } else {
    out = (char *) (*env)->GetPrimitiveArrayCritical(env, arrayOut, 0);
  }

  out += offsetOut;

  if (srcEndianness == dstEndianness || dataSize == 1) {
    memcpy(out, in, dataSize * count);
  } else if (dataSize == 2) {
    cpymem_flip16(out, in, count);
  } else if (dataSize == 4) {
    cpymem_flip32(out, in, count);
  } else if (dataSize == 8) {
    cpymem_flip64(out, in, count);
  } else {
    return me_devcexx_accessors_AccessorNatives_CPYMEM_ERRNO_INVALID_DATASIZE;
  }

  if (arrayIn != NULL) {
    in -= offsetIn;
    (*env)->ReleasePrimitiveArrayCritical(env, arrayIn, in, 0);
  }

  if (arrayOut != NULL) {
    out -= offsetOut;
    (*env)->ReleasePrimitiveArrayCritical(env, arrayOut, out, 0);
  }

  return me_devcexx_accessors_AccessorNatives_CPYMEM_ERRNO_OK;
}

/*
 * Class:     me_devcexx_accessors_AccessorNatives
 * Method:    native_ftok
 * Signature: (Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL Java_me_devcexx_accessors_AccessorNatives_native_1ftok
(JNIEnv * env, jclass clazz, jstring key, jint kid) {
  const char *keyStr = (*env)->GetStringUTFChars(env, key, 0);
  key_t k = ftok(keyStr, kid);
  (*env)->ReleaseStringUTFChars(env, key, keyStr);
  return k;
}

/*
 * Class:     me_devcexx_accessors_AccessorNatives
 * Method:    native_shmget
 * Signature: (JJI)I
 */
JNIEXPORT jint JNICALL Java_me_devcexx_accessors_AccessorNatives_native_1shmget
(JNIEnv * env, jclass clazz, jlong key, jlong size, jint flags) {
  return shmget(key, size, flags);
}

/*
 * Class:     me_devcexx_accessors_AccessorNatives
 * Method:    native_shmat
 * Signature: (IJI)J
 */
JNIEXPORT jlong JNICALL Java_me_devcexx_accessors_AccessorNatives_native_1shmat
(JNIEnv * env, jclass clazz, jint id, jlong address, jint flag) {
  return (jlong) (intptr_t) shmat(id, (const void*) (intptr_t) address, flag);
}

/*
 * Class:     me_devcexx_accessors_AccessorNatives
 * Method:    native_shmdt
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_me_devcexx_accessors_AccessorNatives_native_1shmdt
(JNIEnv * env, jclass clazz, jlong addr) {
  return shmdt((const void *) (intptr_t) addr);
}

/*
 * Class:     me_devcexx_accessors_AccessorNatives
 * Method:    native_shmctl
 * Signature: (IIJ)I
 */
JNIEXPORT jint JNICALL Java_me_devcexx_accessors_AccessorNatives_native_1shmctl
(JNIEnv * env, jclass clazz, jint id, jint cmd, jlong ds) {
  return shmctl(id, cmd, NULL);
}

/*
 * Class:     me_devcexx_accessors_AccessorNatives
 * Method:    errno
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_me_devcexx_accessors_AccessorNatives_errno
(JNIEnv * env, jclass clazz) {
  return errno;
}
/*
 * Class:     me_devcexx_accessors_AccessorNatives
 * Method:    strerror
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_me_devcexx_accessors_AccessorNatives_strerror
(JNIEnv * env, jclass clazz, jint err) {
  return (*env)->NewStringUTF(env, strerror(errno));
}
/*
 * Class:     me_devcexx_accessors_AccessorNatives
 * Method:    constantValueOf
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_me_devcexx_accessors_AccessorNatives_constantValueOf
(JNIEnv * env, jclass clazz, jint constant) {
  switch(constant) {
  case me_devcexx_accessors_AccessorNatives_CONSTANT_ID_IPC_CREAT:
    return IPC_CREAT;
  case me_devcexx_accessors_AccessorNatives_CONSTANT_ID_IPC_RMID:
    return IPC_RMID;
  default:
    return -1;
  }
}
