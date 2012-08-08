#include <stdio.h>
#include <string.h>
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif
	
/*
 * Class:     org_openmdx_kernel_natives_Native
 * Method:    jniConvertToCharSequence
 * Signature: ([B[CIZ)Z
 */
JNIEXPORT jboolean JNICALL 
_Java_org_openmdx_kernel_natives_Native_jniConvertToCharSequence(
    JNIEnv *env, 
    jclass cl, 
    jbyteArray srcArr, 
    jcharArray dstArr, 
    jint nChars, 
    jboolean isBigEndian
) {
	  jboolean srcIsCopy;
	  jboolean dstIsCopy;
	  jbyte* src = (jbyte*)(*env)->GetPrimitiveArrayCritical(env, srcArr, &srcIsCopy);
	  jchar* dst = (jchar*)(*env)->GetPrimitiveArrayCritical(env, dstArr, &dstIsCopy);
	  if(isBigEndian == JNI_TRUE) {
	      memcpy(dst, src, 2 * nChars);
	  }
	  else {
	      jbyte* isrc = src;
        jchar* idst = dst;
        int i;
        for(i = 0; i < nChars; i++) {
        	  char c = *isrc++ << 8;
        	  c += *isrc++;
	          *idst++ = c;
	      }
	  }
    (*env)->ReleasePrimitiveArrayCritical(env, srcArr, src, 0);
    (*env)->ReleasePrimitiveArrayCritical(env, dstArr, dst, 0);
    return srcIsCopy || dstIsCopy ? JNI_FALSE : JNI_TRUE;
}

/*
 * Class:     org_openmdx_kernel_natives_Native
 * Method:    jniConvertToByteSequence
 * Signature: ([C[BIZ)Z
 */
JNIEXPORT jboolean JNICALL 
_Java_org_openmdx_kernel_natives_Native_jniConvertToByteSequence(
    JNIEnv *env, 
    jclass cl, 
    jcharArray srcArr, 
    jbyteArray dstArr, 
    jint nBytes,
    jboolean isBigEndian
) {
	  jboolean srcIsCopy;
	  jboolean dstIsCopy;
	  jchar* src = (jchar*)(*env)->GetPrimitiveArrayCritical(env, srcArr, &srcIsCopy);
	  jbyte* dst = (jbyte*)(*env)->GetPrimitiveArrayCritical(env, dstArr, &dstIsCopy);
	  if(isBigEndian == JNI_TRUE) {
	      memcpy(dst, src, nBytes);
	  }
	  else {
	      jchar* isrc = src;
        jbyte* idst = dst;
        int i;
	      for(i = 0; i < nBytes; i+=2) {
	          jchar c = *isrc++;
	          *idst++ = (c >> 8) & 0xFF;
	          *idst++ = c & 0xFF;
	      }
	  }	
    (*env)->ReleasePrimitiveArrayCritical(env, srcArr, src, 0);
    (*env)->ReleasePrimitiveArrayCritical(env, dstArr, dst, 0);
    return srcIsCopy || dstIsCopy ? JNI_FALSE : JNI_TRUE;
}

#ifdef __cplusplus
}
#endif
