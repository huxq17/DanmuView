package com.huxq17.danmuview.utils;


import android.util.Log;

import com.huxq17.danmuview.listener.OnFPSChangedListener;

import java.lang.ref.WeakReference;

public class DrawStatistics {
    public static final DrawStatistics instance = new DrawStatistics();
    private static final String TAG = DrawStatistics.class.getSimpleName();
    private int FPS;
    private long startCountT;
    private long lastT;
    private boolean mEnableLog = false;
    private long mDrawTime;
    private boolean mIsEnd = false;
    private WeakReference<OnFPSChangedListener> mFPSListenerweakRef;
    private long lastCountFPSTime;
    private int FPSCount;

    public void setEnableLog(boolean enableLog) {
        mEnableLog = enableLog;
    }

    public void startCount() {
        mIsEnd = false;
        lastT = System.currentTimeMillis();
        if (startCountT == 0) {
            startCountT = lastT;
            lastCountFPSTime = lastT;
            FPSCount = 0;
        }
    }

    public void finishCount(int danmuNum) {
        if (mIsEnd) return;
        long currentT = System.currentTimeMillis();
        mDrawTime = currentT - lastT;
        if (mDrawTime != 0 && danmuNum > 0) {
            FPSCount++;
            if (currentT - lastCountFPSTime >= 1000) {
                final int newFPS = FPSCount;
                if (newFPS != FPS) {
                    FPS = newFPS;
                    notifyFPSChanged();
                }
                FPSCount = 0;
                lastCountFPSTime = currentT;
                if (mEnableLog) {
                    Log.d(TAG, "renderFrame took=" + mDrawTime + ";fps=" + FPS);
                }
            }
        }
    }

    private void notifyFPSChanged() {
        if (mFPSListenerweakRef != null) {
            OnFPSChangedListener fpsListener = mFPSListenerweakRef.get();
            if (fpsListener != null) {
                fpsListener.onFPSChanged(FPS);
            }
        }
    }

    public void stop() {
        mIsEnd = true;
        startCountT = 0;
        lastT = 0;
        FPS = 0;
        FPSCount = 0;
    }

    public long getDrawTime() {
        return mDrawTime;
    }

    public void setFPSListener(OnFPSChangedListener listener) {
        this.mFPSListenerweakRef = new WeakReference<>(listener);
    }
}
