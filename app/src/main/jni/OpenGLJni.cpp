#include <jni.h>
#include <GLES2/gl2.h>
#include<Android/log.h>
#include <android/bitmap.h>
#include <string.h>
#include "GL3.h"

#define TAG "danmuview-jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_huxq17_danmuview_utils_OpenGLUtil_glTexImage2D(
        JNIEnv *env, jclass cls, jint target, jint level, jint internalformat, jint width,
        jint height, jint border, jint format, jint type) {
    glTexImage2D(target, level, internalformat, width, height, border, format, type, 0);
    LOGD("glTexImage2D");
}
JNIEXPORT void JNICALL Java_com_huxq17_danmuview_utils_OpenGLUtil_glTexSubImage2D(
        JNIEnv *env, jclass cls, jint target, jint level, jint xoffset, jint yoffset, jint width,
        jint height,
        jint format, jint type) {
    glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, 0);
}
void checkError(JNIEnv *env, jclass cls, char *line) {
    if (glGetError() != GL_NO_ERROR) {
        LOGD("checkError,%s", line);
    }
}
void transferPixelWithPbo(jint transferPbo, jint bitmapsize, void *dataPtr) {
    glBindUnpackBuffer(transferPbo);
    glBufferUnpackData(bitmapsize, NULL);
    GLvoid *pixelUnpackBuffer = glMapBufferUnpackRange(0, bitmapsize);
    memcpy(pixelUnpackBuffer, dataPtr, bitmapsize);
    glUnmapUnpackBuffer();
    glBindUnpackBuffer(0);
}
void renderPixelWithPbo(jint renderPbo, jint width, jint height) {
    glBindUnpackBuffer(renderPbo);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, 0);
}
JNIEXPORT void JNICALL Java_com_huxq17_danmuview_utils_OpenGLUtil_glUpdateTexImage2D(
        JNIEnv *env, jclass cls, jboolean usePbo, jboolean isFirstFrame, jint renderPbo,
        jint transferPbo, jint texture, jobject jbitmap, jint width, jint height, jint bitmapsize) {
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    void *dataPtr;
    AndroidBitmap_lockPixels(env, jbitmap, &dataPtr);
    if (isFirstFrame) {
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                     dataPtr);
        if (usePbo) {
            transferPixelWithPbo(transferPbo, bitmapsize, dataPtr);
        }
    } else {
        if (usePbo) {
            renderPixelWithPbo(renderPbo, width, height);
            transferPixelWithPbo(transferPbo, bitmapsize, dataPtr);
        } else {
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE,
                            dataPtr);
        }
    }
    glGenerateMipmap(GL_TEXTURE_2D);
    checkError(env, cls, "");
    AndroidBitmap_unlockPixels(env, jbitmap);
}
#ifdef __cplusplus
}
#endif
