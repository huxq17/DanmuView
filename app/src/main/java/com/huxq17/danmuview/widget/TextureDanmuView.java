package com.huxq17.danmuview.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

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


public class TextureDanmuView extends EGLTextureView implements GLSurfaceView.Renderer, BaseDanmuView {
    private DanmuHandler mDanmuHandler;
    private boolean STOP_FLAG = false;
    private boolean isSurfaceDestroyed = false;

    public TextureDanmuView(Context context) {
        this(context, null);
    }

    public TextureDanmuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mDanmuHandler = new DanmuHandler(this, new GLRender(OpenGLUtil.isSupportOpenGLVersion(getContext(), 3.0)));
        setRenderer(this);
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
            mDanmuHandler.destroy();
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
    public void onSurfaceDestroyed() {
        isSurfaceDestroyed = true;
        mDanmuHandler.clearRender();
        mDanmuHandler.post(mDestroyRunnable);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (STOP_FLAG) {
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
        mDanmuHandler.sendDanmu(danmu);
    }

    @Override
    public int getDanmuCount() {
        return mDanmuHandler.getDanmuCount();
    }

    @Override
    public void setOnFPSChangedListener(OnFPSChangedListener listener) {
        mDanmuHandler.setOnFPSChangedListener(listener);
    }

    @Override
    public void onDanmuAdd(Danmu danmu) {

    }

    @Override
    public void onDanmuAbandon(Danmu danmu) {

    }
}
