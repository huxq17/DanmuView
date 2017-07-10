#include "GL3.h"

#include <GLES3/gl3.h>

void glBindUnpackBuffer(jint buffer) {
    glBindBuffer(GL_PIXEL_UNPACK_BUFFER, buffer);
}

void glBufferUnpackData(jint size, const void *data) {
    glBufferData(GL_PIXEL_UNPACK_BUFFER, size, data, GL_STREAM_DRAW);
}

void glUnmapUnpackBuffer() {
    glUnmapBuffer(GL_PIXEL_UNPACK_BUFFER);
}

void *glMapBufferUnpackRange(jint offset, jint length) {
    return glMapBufferRange(GL_PIXEL_UNPACK_BUFFER, offset, length, GL_MAP_WRITE_BIT |
                                                                    GL_MAP_INVALIDATE_BUFFER_BIT);
}
