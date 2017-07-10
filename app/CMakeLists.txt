cmake_minimum_required(VERSION 3.4.1)

#cpp文件
add_library(OpenGLJni  SHARED  src/main/jni/OpenGLJni.cpp src/main/jni/GL3.cpp)

#lib引用
find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)
find_library(GLESv2-lib GLESv2)
find_library(GLESv3-lib GLESv3)

#链接lib
target_link_libraries(OpenGLJni
                        ${log-lib}
                        ${jnigraphics-lib}
                        ${GLESv2-lib}
                        ${GLESv3-lib})