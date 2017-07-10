package com.huxq17.danmuview.utils;


import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.util.Log;

import com.andbase.tractor.utils.LogUtils;

import java.math.BigDecimal;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDetachShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

public class OpenGLUtil {
    private static final String TAG = "tag";

    static {
        System.loadLibrary("OpenGLJni");
    }

    public static native void glUpdateTexImage2D(boolean usePbo, boolean isFirstFrame, int renderPbo,
                                                 int transferPbo, int texture, Bitmap jbitmap,
                                                 int width, int height, int bitmapsize);

    public static native void glTexImage2D(int target,
                                           int level,
                                           int xoffset,
                                           int yoffset,
                                           int width,
                                           int height,
                                           int format,
                                           int type);

    public static native void glTexSubImage2D(int target,
                                              int level,
                                              int xoffset,
                                              int yoffset,
                                              int width,
                                              int height,
                                              int format,
                                              int type);

    public static int getSupportOpenGLVersion(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return info.reqGlEsVersion;
    }

    public static boolean isSupportOpenGLVersion(Context context, double targetVersion) {
        int supportVersion = getSupportOpenGLVersion(context);
        int lower16 = supportVersion & 65535;
        int upper16 = supportVersion >> 16;
        BigDecimal targetBigDecimal = new BigDecimal(Double.toString(targetVersion));
        String localVersion = upper16 + "." + lower16;
        BigDecimal localBigDecimal = new BigDecimal(localVersion);
        LogUtils.d("targetVersion=" + targetVersion + ";supportVersion=" + localVersion);
        return localBigDecimal.compareTo(targetBigDecimal) >= 0;
    }

    /**
     * /**
     * 编译，连接 ，返回 program 的 ID
     *
     * @param vertexShaderSource
     * @param fragmentShaderSource
     * @return
     */
    public static int buildProgram(String vertexShaderSource,
                                   String fragmentShaderSource) {
        int program;

        // Compile the shaders.
        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);
        // Link them into a shader program.
        program = linkProgram(vertexShader, fragmentShader);
        validateProgram(program);

        glDetachShader(program, vertexShader);
        glDetachShader(program, fragmentShader);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return program;
    }

    /**
     * 加载并编译着色器
     *
     * @param type
     * @param shaderCode
     * @return
     */
    private static int compileShader(int type, String shaderCode) {
        // 建立新的着色器对象
        final int shaderObjectId = glCreateShader(type);
        if (shaderObjectId == 0) {
            Log.w(TAG, "不能创建新的着色器.");
            return 0;
        }
        // 传递着色器资源代码.
        glShaderSource(shaderObjectId, shaderCode);
        //编译着色器
        glCompileShader(shaderObjectId);
        // 获取编译的状态
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS,
                compileStatus, 0);
        //打印log
        Log.v(TAG, "代码编译结果:" + "\n" + shaderCode
                + "\n:" + glGetShaderInfoLog(shaderObjectId));
        // 确认编译的状态
        if (compileStatus[0] == 0) {
            // 如果编译失败，则删除该对象
            glDeleteShader(shaderObjectId);
            Log.w(TAG, "编译失败!");
            return 0;
        }
        return shaderObjectId;
    }

    /**
     * 链接顶点着色器和片段着色器成一个program
     *
     * @param vertexShaderId
     * @param fragmentShaderId
     * @return
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {

        // 新建一个program对象
        final int programObjectId = glCreateProgram();

        if (programObjectId == 0) {
            Log.w(TAG, "不能新建一个 program");
            return 0;
        }

        // Attach the vertex shader to the program.
        glAttachShader(programObjectId, vertexShaderId);

        // Attach the fragment shader to the program.
        glAttachShader(programObjectId, fragmentShaderId);

        // 将两个着色器连接成一个program
        glLinkProgram(programObjectId);

        // 获取连接状态
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS,
                linkStatus, 0);

        // Print the program info log to the Android log output.
        Log.v(TAG, "Results of linking program:\n"
                + glGetProgramInfoLog(programObjectId));

        // 验证连接状态
        if (linkStatus[0] == 0) {
            // If it failed, delete the program object.
            glDeleteProgram(programObjectId);
            Log.w(TAG, "连接 program 失败!.");
            return 0;
        }
        // Return the program object ID.
        return programObjectId;
    }

    public static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS,
                validateStatus, 0);
        Log.v(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

}
