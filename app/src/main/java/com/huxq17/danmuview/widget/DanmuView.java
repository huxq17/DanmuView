package com.huxq17.danmuview.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.huxq17.danmuview.controller.BaseDanmuView;
import com.huxq17.danmuview.controller.DanmuHandler;
import com.huxq17.danmuview.danmu.Danmu;
import com.huxq17.danmuview.danmu.DanmuMode;
import com.huxq17.danmuview.listener.OnFPSChangedListener;
import com.huxq17.danmuview.render.NormalRender;

public class DanmuView extends SurfaceView implements SurfaceHolder.Callback, BaseDanmuView {
    private SurfaceHolder surfaceHolder;
    private DanmuHandler mDanmuHandler;

    public DanmuView(Context context) {
        this(context, null);
    }

    public DanmuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DanmuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotCacheDrawing(true);
        setDrawingCacheEnabled(false);
        setWillNotDraw(true);
        surfaceHolder = getHolder();

        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);

        mDanmuHandler = new DanmuHandler(this, new NormalRender(surfaceHolder), true);
//        GraphicBuffer
    }

    public void start() {
        mDanmuHandler.prepare();
    }

    public void stop() {
        mDanmuHandler.destroy();
    }

    public boolean isPrepared() {
        return mDanmuHandler.isPrepared();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mDanmuHandler.onWindowResize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    public void setDanmuMode(DanmuMode mode) {
        if (mDanmuHandler != null) {
            mDanmuHandler.setDanmuMode(mode);
        }
    }

    @Override
    public void onDanmuAdd(Danmu danmu) {
    }

    @Override
    public void onDanmuAbandon(Danmu danmu) {
    }

    public void sendDanmu(Danmu danmu) {
        mDanmuHandler.sendDanmu(danmu);
    }

    @Override
    public void setOnFPSChangedListener(OnFPSChangedListener listener) {
        mDanmuHandler.setOnFPSChangedListener(listener);
    }

    @Override
    public int getDanmuCount() {
        return mDanmuHandler.getDanmuCount();
    }
}
