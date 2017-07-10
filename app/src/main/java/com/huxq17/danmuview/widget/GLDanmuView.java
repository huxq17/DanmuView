package com.huxq17.danmuview.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.huxq17.danmuview.controller.BaseDanmuView;
import com.huxq17.danmuview.controller.DanmuHandler;
import com.huxq17.danmuview.danmu.Danmu;
import com.huxq17.danmuview.danmu.DanmuMode;
import com.huxq17.danmuview.listener.OnFPSChangedListener;
import com.huxq17.danmuview.render.GLRender;
import com.huxq17.danmuview.utils.OpenGLUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glViewport;


public class GLDanmuView extends GLSurfaceView implements GLSurfaceView.Renderer, BaseDanmuView {
    private DanmuHandler mDanmuHandler;
    private boolean STOP_FLAG = false;

    public GLDanmuView(Context context) {
        this(context, null);
    }

    public GLDanmuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        mDanmuHandler = new DanmuHandler(this, new GLRender(OpenGLUtil.isSupportOpenGLVersion(getContext(), 3.0)));
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    public void start() {
        if (!mDanmuHandler.isPrepared()) {
            STOP_FLAG = false;
            mDanmuHandler.prepare();
            onResume();
        }
    }

    public void stop() {
        STOP_FLAG = true;
    }

    private Runnable mDestroyRunnable = new Runnable() {
        @Override
        public void run() {
            onPause();
            if (mDanmuHandler != null) {
                mDanmuHandler.destroy();
            }
        }
    };

    public boolean isPrepared() {
        return mDanmuHandler.isPrepared();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mDanmuHandler.initRender();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDanmuHandler.onWindowResize(width, height);
        glViewport(0, 0, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        stop();
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (STOP_FLAG) {
            mDanmuHandler.clearRender();
            mDanmuHandler.post(mDestroyRunnable);
            return;
        }
        mDanmuHandler.renderFrame();
    }

    public void setDanmuMode(DanmuMode mode) {
        if (mDanmuHandler != null) {
            mDanmuHandler.setDanmuMode(mode);
        }
    }

    public void sendDanmu(Danmu danmu) {
        if (mDanmuHandler != null) {
            mDanmuHandler.sendDanmu(danmu);
        }
    }
    @Override
    public void setOnFPSChangedListener(OnFPSChangedListener listener) {
        mDanmuHandler.setOnFPSChangedListener(listener);
    }

    @Override
    public int getDanmuCount() {
        return mDanmuHandler.getDanmuCount();
    }
    @Override
    public void onDanmuAdd(Danmu danmu) {

    }

    @Override
    public void onDanmuAbandon(Danmu danmu) {

    }
}
