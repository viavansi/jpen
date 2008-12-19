/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class jpen_provider_xinput_XiBus */

#ifndef _Included_jpen_provider_xinput_XiBus
#define _Included_jpen_provider_xinput_XiBus
#ifdef __cplusplus
extern "C" {
#endif
/* Inaccessible static: bootTimeUtc */
/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    create
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_create
  (JNIEnv *, jclass);

/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    getError
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_provider_xinput_XiBus_getError
  (JNIEnv *, jclass);

/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    getDevicesSize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_getDevicesSize
  (JNIEnv *, jclass, jint);

/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    getDeviceName
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_provider_xinput_XiBus_getDeviceName
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    setDevice
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_setDevice
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    refreshDeviceInfo
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_refreshDeviceInfo
  (JNIEnv *, jclass, jint);

/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    getBootTimeUtc
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_jpen_provider_xinput_XiBus_getBootTimeUtc
  (JNIEnv *, jclass, jint);

/*
 * Class:     jpen_provider_xinput_XiBus
 * Method:    destroy
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiBus_destroy
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif