package com.huxq17.danmuview.utils;

import android.graphics.SurfaceTexture;
import android.opengl.GLUtils;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class EGLHelper {

    public EGL10 mEgl;
    public EGLDisplay mEglDisplay;
    public EGLConfig mEglConfig;
    public EGLSurface mEglSurface;
    public EGLContext mEglContext;
    public GL10 mGL;

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final String TAG = EGLHelper.class.getSimpleName();
    private int red = 8;
    private int green = 8;
    private int blue = 8;
    private int alpha = 8;
    private int depth = 16;
    private int renderType = 4;
    private int bufferType = EGL10.EGL_SINGLE_BUFFER;
    private EGLContext shareContext = EGL10.EGL_NO_CONTEXT;


    public void config(int red, int green, int blue, int alpha, int depth, int renderType) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.depth = depth;
        this.renderType = renderType;
    }

    public boolean eglInit(SurfaceTexture surface) {
        int[] attributes = new int[]{
                EGL10.EGL_RED_SIZE, red,  //指定RGB中的R大小（bits）
                EGL10.EGL_GREEN_SIZE, green, //指定G大小
                EGL10.EGL_BLUE_SIZE, blue,  //指定B大小
                EGL10.EGL_ALPHA_SIZE, alpha, //指定Alpha大小，以上四项实际上指定了像素格式
                EGL10.EGL_DEPTH_SIZE, depth, //指定深度缓存(Z Buffer)大小
                EGL10.EGL_RENDERABLE_TYPE, renderType, //指定渲染api版本, EGL14.EGL_OPENGL_ES2_BIT
                EGL10.EGL_NONE};  //总是以EGL10.EGL_NONE结尾

        //获取Display
        mEgl = (EGL10) EGLContext.getEGL();
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int[] version = new int[2];    //主版本号和副版本号
        mEgl.eglInitialize(mEglDisplay, version);
        //选择Config
        int[] configNum = new int[1];
        mEgl.eglChooseConfig(mEglDisplay, attributes, null, 0, configNum);
        if (configNum[0] == 0) {
            Log.w(TAG, "config not support");
            return false;
        }
        EGLConfig[] c = new EGLConfig[configNum[0]];
        mEgl.eglChooseConfig(mEglDisplay, attributes, c, configNum[0], configNum);
        mEglConfig = c[0];
        //创建Surface
//        int[] surAttr = new int[]{
//                EGL10.EGL_WIDTH, width,
//                EGL10.EGL_HEIGHT, height,
//                EGL10.EGL_NONE
//        };
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, null);
        //创建Context
        int[] contextAttr = new int[]{
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };
        mEglContext = mEgl.eglCreateContext(mEglDisplay, mEglConfig, shareContext, contextAttr);
        makeCurrent();
        mGL = (GL10) mEglContext.getGL();
        return true;
    }

    public void makeCurrent() {
        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {// 设置为当前的渲染环境
            Log.w(TAG, "eglMakeCurrent failed : " + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
    }

    public int swap() {
        if (!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {
            Log.w(TAG, "eglSwapBuffers failed : " + GLUtils.getEGLErrorString(mEgl.eglGetError()));
            return mEgl.eglGetError();
        }
        return EGL10.EGL_SUCCESS;
    }

    public GL10 getGL10() {
        return mGL;
    }

    public void destroy() {
        mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
        mEgl.eglDestroyContext(mEglDisplay, mEglContext);
        mEgl.eglTerminate(mEglDisplay);
    }

}
