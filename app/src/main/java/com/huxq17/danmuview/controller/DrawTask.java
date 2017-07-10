package com.huxq17.danmuview.controller;

import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.huxq17.danmuview.utils.DrawStatistics;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DrawTask extends Task {
    private WeakReference<IDanmuView> mDanmuViewWeakRef;
    private WeakReference<DanmuHandler> mDanmuHandlerRef;
    private byte[] mLock = new byte[0];
    final Lock lock = new ReentrantLock();
    final Condition condition = lock.newCondition();

    public DrawTask(IDanmuView danmuView, DanmuHandler danmuHandler) {
//        super("draw danmu task");
        mDanmuViewWeakRef = new WeakReference<>(danmuView);
        mDanmuHandlerRef = new WeakReference<>(danmuHandler);
    }

    public void requestRender() {
        synchronized (mLock) {
            mLock.notify();
        }
    }

    @Override
    public void onRun() {
        while (isRunning()) {
            synchronized (mLock) {
                try {
                    if (!isDanmuViewPrepared()) {
                        mLock.wait();
                        continue;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                drawFrame();
            }
        }
    }

    @Override
    public void cancelTask() {
        synchronized (mLock) {
            mLock.notify();
        }
    }

    private boolean isDanmuViewPrepared() {
        IDanmuView danmuView = mDanmuViewWeakRef.get();
        if (danmuView == null) {
            TaskPool.getInstance().cancelTask(this);
            return false;
        } else {
            return danmuView.isPrepared();
        }
    }

    private void drawFrame() {
        DanmuHandler danmuHandler = mDanmuHandlerRef.get();
        if (danmuHandler != null) {
            danmuHandler.renderFrame();
        }
        final long dt = DrawStatistics.instance.getDrawTime();
        synchronized (mLock) {
            try {
                mLock.wait(dt < 16 ? 16 - dt : 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        TaskPool.getInstance().execute(this);
    }

    public void stop() {
        TaskPool.getInstance().cancelTask(this);
    }

}
