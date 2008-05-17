/* % [{
% (C) Copyright 2008 Nicolas Carranza and individual contributors.
% See the jpen-copyright.txt file in the jpen distribution for a full
% listing of individual contributors.
%
% This file is part of jpen.
%
% jpen is free software: you can redistribute it and/or modify
% it under the terms of the GNU Lesser General Public License as published by
% the Free Software Foundation, either version 3 of the License,
% or (at your option) any later version.
%
% jpen is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU Lesser General Public License for more details.
%
% You should have received a copy of the GNU Lesser General Public License
% along with jpen.  If not, see <http://www.gnu.org/licenses/>.
% }] */
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class jpen_provider_xinput_XiDevice */

#ifndef _Included_jpen_provider_xinput_XiDevice
#define _Included_jpen_provider_xinput_XiDevice
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    getIsListening
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_jpen_provider_xinput_XiDevice_getIsListening
  (JNIEnv *, jclass, jint);

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    setIsListening
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_jpen_provider_xinput_XiDevice_setIsListening
  (JNIEnv *, jclass, jint, jboolean);

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    getLevelRangeMin
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_getLevelRangeMin
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    getLevelRangeMax
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_getLevelRangeMax
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    getValue
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_getValue
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    destroy
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_destroy
  (JNIEnv *, jclass, jint);

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    getError
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jpen_provider_xinput_XiDevice_getError
  (JNIEnv *, jclass);

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    nextEvent
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_jpen_provider_xinput_XiDevice_nextEvent
  (JNIEnv *, jclass, jint);

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    getLastEventType
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_getLastEventType
  (JNIEnv *, jclass, jint);

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    getLastEventButton
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_jpen_provider_xinput_XiDevice_getLastEventButton
  (JNIEnv *, jclass, jint);

/*
 * Class:     jpen_provider_xinput_XiDevice
 * Method:    refreshLevelRanges
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_jpen_provider_xinput_XiDevice_refreshLevelRanges
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
