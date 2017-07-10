#ifndef _GL3_H_
#define _GL3_H_
#include <jni.h>

void glBindUnpackBuffer(jint buffer);

void glUnmapUnpackBuffer();

void glBufferUnpackData(jint size, const void *data);

void *glMapBufferUnpackRange(jint offset, jint length);

#endif //_GL3_H_
