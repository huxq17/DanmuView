package com.huxq17.danmuview.render;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.SparseArray;

import com.huxq17.danmuview.danmu.Route;
import com.huxq17.danmuview.utils.OpenGLUtil;

import java.nio.IntBuffer;

public class GLRender implements IRender {
    private static final String TAG = "tag";
    private static final String simple_vertex_shader = "attribute vec4 a_Position;     \n" +
            "attribute vec2 a_TextureCoordinates;  \n" +
            "varying vec2 v_TextureCoordinates; \n" +
            "uniform mat4 u_Matrix;" +
            "void main()                      \n" +
            "{" +
            "  v_TextureCoordinates = a_TextureCoordinates;\n" +
            "    gl_Position = u_Matrix * a_Position;  \n" +
            "}";
    private static final String simple_fragment_shader = "precision mediump float;   \n" +
            "                                          \n" +
            "uniform sampler2D u_TextureUnit; \n" +
            "varying vec2 v_TextureCoordinates;\n" +
            "    \n" +
            "void main()                           \n" +
            "{                                 \n" +
//            "vec3 Color_RGB = texture2D(u_TextureUnit, v_TextureCoordinates);\n" +
//            "gl_FragColor = vec4(Color_RGB, 1.0);" +
//            "    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);\n" +
            "    vec4 Color_RGBA = texture2D(u_TextureUnit, v_TextureCoordinates);\n" +
            "    gl_FragColor = Color_RGBA; \n" +
            "}";
    private int program;

    //------------第一步 : 定义两个标签，分别于着色器代码中的变量名相同,
    //------------第一个是顶点着色器的变量名，第二个是片段着色器的变量名
    private static final String A_POSITION = "a_Position";
    private static final String U_COLOR = "u_Color";
    //------------第二步: 定义两个ID,我们就是通ID来实现数据的传递的,这个与前面
    //------------获得program的ID的含义类似的
    private int uColorLocation;
    private int aPositionLocation;
    private final float[] projectionMatrix = new float[16];
    private static final String U_MATRIX = "u_Matrix";
    private int uMatrixLocation;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int VERTEX_COUNTS = 4;//顶点坐标数
    private static final int POSITION_COMPONENT_COUNT = 2;

    // 数组中每个顶点的坐标数
    static final int COORDS_PER_VERTEX = 2;
    //因为我们的顶点数据和纹理坐标数据放在了一起 ，所以在使用glVertexAttribPointer等函数时，其中的stride参数就需要传入了，
    //用于高速着色器应该如何读取坐标值 ，比如这里我们的着色器读取坐标时，设置从位置 0开始读，读取x , y后就会跳过 s t 接着读取 curX y
    //这就是通过传入stride参数实现的
    private static final int TEXTURE_COORDIANTES_COMPONENT_COUNT = 2; //一个纹理坐标含有的元素个数
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDIANTES_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";//纹理
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";//纹理

    private int uTextureUnitLocation;
    private int aTextureCoordinates;
    private int[] textures;
    private IntBuffer pboIds;
    private boolean mSupportGL30 = true;
    private boolean hasConfigurationChanged = false;

    public GLRender(boolean supportGL30) {
        mSupportGL30 = supportGL30;
    }

    private SparseArray<Route> mRoutes;

    public void setRoutes(SparseArray<Route> routes) {
        mRoutes = routes;
    }


    public void init() {
        program = OpenGLUtil.buildProgram(simple_vertex_shader, simple_fragment_shader);
        GLES20.glUseProgram(program);
        uColorLocation = GLES20.glGetUniformLocation(program, U_COLOR);
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        aTextureCoordinates = GLES20.glGetAttribLocation(program, A_TEXTURE_COORDINATES);
        uTextureUnitLocation = GLES20.glGetAttribLocation(program, U_TEXTURE_UNIT);
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
    }

    @Override
    public void onConfigurationChanged(SparseArray<Route> routes, int width, int height) {
        if (routes == null) return;
        hasConfigurationChanged = true;
        clear(false);
        int size = routes.size();
        textures = new int[size];
//        pboIds = ByteBuffer.allocateDirect(size * 2).asIntBuffer();
        pboIds = IntBuffer.allocate(size * 2);
        GLES20.glGenTextures(size, textures, 0);
        GLES20.glGenBuffers(size * 2, pboIds);
        this.mRoutes = routes;
        Matrix.orthoM(projectionMatrix, 0, 0, width, 0, height, -1f, 1f);
    }

    @Override
    public int drawFrame() {
        if (mRoutes == null) return 0;
        int drawDanmuCount = 0;
        GLES20.glUniform1i(uTextureUnitLocation, 0);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glEnableVertexAttribArray(aTextureCoordinates);
//        GLES20.glDisableVertexAttribArray();
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
        int length = textures.length;
        for (int i = 0; i < length; i++) {
            Route route = mRoutes.valueAt(i);
            drawDanmuCount += drawRoute(route, i, hasConfigurationChanged);
        }
        hasConfigurationChanged = false;
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aTextureCoordinates);
        return drawDanmuCount;
    }

    @Override
    public void clear() {
        clear(true);
    }

    private void clear(boolean isCompletely) {
        if (textures != null) {
            GLES20.glDeleteTextures(textures.length, IntBuffer.wrap(textures));
            GLES20.glDeleteBuffers(pboIds.array().length, pboIds);
            if (isCompletely) {
                GLES20.glUseProgram(0);
                GLES20.glDeleteProgram(program);
            }
            textures = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private int drawRoute(Route route, int index, boolean resetFirstFrame) {
        if (route == null) return 0;
        final Bitmap bitmap = route.bitmap;
        int count = route.drawDanmus();
        if (count <= 0) {
            return 0;
        }
        if (route.position == 0 && route.nextPosition == 0) {
            route.position = index * 2;
            route.nextPosition = route.position + 1;
        }
        if (resetFirstFrame) route.isFirstFrame = true;
        int bitmapsize = bitmap.getRowBytes() * bitmap.getHeight();
        final boolean usePbo = mSupportGL30;
        OpenGLUtil.glUpdateTexImage2D(usePbo, route.isFirstFrame, pboIds.get(route.position), pboIds.get(route.nextPosition), textures[index], bitmap,
                bitmap.getWidth(), bitmap.getHeight(), bitmapsize);
//        if (!route.isFirstFrame) {
//            GLES20.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, pboIds.get(route.position));
//        }
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[index]);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//        if (route.isFirstFrame) {
//            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, route.bitmap, 0);
//        } else {
//            OpenGLUtil.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, route.bitmap.getWidth(), route.bitmap.getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE);
//        }
//        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
//        if (!route.isFirstFrame) {
//            GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, pboIds.get(route.nextPosition));
//            int bitmapsize = route.bitmap.getRowBytes() * route.bitmap.getHeight();
//            GLES30.glBufferData(GLES30.GL_PIXEL_UNPACK_BUFFER, bitmapsize, null, GLES30.GL_DYNAMIC_DRAW);
//            ByteBuffer byteBuffer = (ByteBuffer) GLES30.glMapBufferRange(GLES30.GL_PIXEL_UNPACK_BUFFER, 0, bitmapsize, GLES30.GL_MAP_WRITE_BIT | GLES30.GL_MAP_INVALIDATE_BUFFER_BIT);
//            route.bitmap.copyPixelsToBuffer(byteBuffer);
////            byteBuffer.position(0);
//            GLES30.glUnmapBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER);
//            GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, 0);
//        }
//        TextureHelper.checkGlError("");
        route.bPos.position(0);
        //传入顶点坐标和纹理坐标
        GLES20.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT,
                GLES20.GL_FLOAT, false, STRIDE, route.bPos);
        //设置从第二个元素开始读取，因为从第二个元素开始才是纹理坐标
        route.bPos.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aTextureCoordinates, TEXTURE_COORDIANTES_COMPONENT_COUNT,
                GLES20.GL_FLOAT, false, STRIDE, route.bPos);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_COUNTS);
//        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 4, GLES20.GL_UNSIGNED_SHORT, route.indices);
        if (!route.isFirstFrame && !route.hasBitmapBindTexture) {
            route.hasBitmapBindTexture = true;
        }
        route.change();
        return count;
    }
}
