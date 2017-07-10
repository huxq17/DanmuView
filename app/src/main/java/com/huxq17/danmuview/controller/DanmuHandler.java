package com.huxq17.danmuview.controller;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;

import com.huxq17.danmuview.danmu.Danmu;
import com.huxq17.danmuview.danmu.DanmuMode;
import com.huxq17.danmuview.listener.OnFPSChangedListener;
import com.huxq17.danmuview.objectpool.ObjectPool;
import com.huxq17.danmuview.render.IRender;
import com.huxq17.danmuview.utils.DanmuUtil;
import com.huxq17.danmuview.utils.DensityUtil;
import com.huxq17.danmuview.utils.DrawStatistics;

public class DanmuHandler implements OnFPSChangedListener {
    private static final int MSG_SEND_DANMU = 0x1;
    private static final int MSG_UPDATE_UI = 0x2;
    private static final int MSG_FPS_CHANGED = 0x3;
    private HandlerThread mThread;
    private Handler mHandler;
    private boolean mPrepared = false;
    private BaseDanmuView mDanmuView;
    private DanmuKu mDanmuKu;
    private Context mContext;
    private int textSize = 15;
    private DrawTask mDrawTask;
    private boolean mIsNewThread;
    private OnFPSChangedListener mFPSChangeListener;

    public DanmuHandler(BaseDanmuView danmuView, IRender render) {
        this(danmuView, render, false);
    }

    public DanmuHandler(BaseDanmuView danmuView, IRender render, boolean newThread) {
        mDanmuView = danmuView;
        mContext = mDanmuView.getContext();
        mIsNewThread = newThread;
        mDanmuKu = new DanmuKu(render);
        mDrawTask = new DrawTask(mDanmuView, this);
        DrawStatistics.instance.setFPSListener(this);
    }

    public void prepare() {
        if (mPrepared) return;
        mThread = new HandlerThread("DanmuThread");
        mThread.start();
        final Looper looper = mThread.getLooper();
        mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (!mPrepared) return;
                switch (msg.what) {
                    case MSG_SEND_DANMU:
                        if (msg.obj instanceof DanmuExt) {
                            mDrawTask.requestRender();
                            DanmuExt danmuExt = (DanmuExt) msg.obj;
                            if (mDanmuKu.addDanmu(mContext, danmuExt)) {
                                mDanmuView.onDanmuAdd(danmuExt);
                            } else {
                                mDanmuView.onDanmuAbandon(danmuExt);
                            }
                        }
                        break;
                    case MSG_FPS_CHANGED:
                        final int fps = msg.arg1;
                        if (mFPSChangeListener != null) {
                            mFPSChangeListener.onFPSChanged(fps);
                        }
                        break;
                    case MSG_UPDATE_UI:
                        break;
                }
            }
        };
        mPrepared = true;
        if (mIsNewThread) {
            mDrawTask.start();
        }
    }

    public int getDanmuCount() {
        if (mDanmuKu != null) {
            return mDanmuKu.getDanmuCount();
        }
        return 0;
    }

    public void sendDanmu(Danmu danmu) {
        if (!mPrepared) {
            ObjectPool.instance().recycleDanmu((DanmuExt) danmu);
            return;
        }
        danmu.textSize = textSize;
        Message msg = Message.obtain();
        msg.obj = danmu;
        msg.what = MSG_SEND_DANMU;
        mHandler.sendMessage(msg);
    }

    public void onWindowResize(int width, int height) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(DensityUtil.dip2px(mContext, textSize));
        mDanmuKu.changeConfiguration(width, height, DanmuUtil.getDanmuHeight(textPaint));
    }

    public void setDanmuMode(DanmuMode mode) {
        if (mPrepared) {
            mDanmuKu.setDanmuMode(mode);
        }
    }

    public void initRender() {
        if (mPrepared) {
            mDanmuKu.initRender();
        }
    }

    public void clearRender() {
        if (mPrepared) {
            mDanmuKu.clearRender();
        }
    }

    public void renderFrame() {
        if (mPrepared) {
            DrawStatistics.instance.startCount();
            int danmuNum = mDanmuKu.renderFrame();
            DrawStatistics.instance.finishCount(danmuNum);
        }
    }

    public void post(Runnable runnable) {
        if (mPrepared) {
            mHandler.post(runnable);
        }
    }

    public boolean isPrepared() {
        return mPrepared;
    }

    public void destroy() {
        if (mPrepared) {
            DrawStatistics.instance.stop();
            mPrepared = false;
            mHandler = null;
            mThread.quit();
            if (mDrawTask != null) {
                mDrawTask.requestRender();
                if (mIsNewThread) {
                    mDrawTask.stop();
                }
            }
        }
    }

    public void setOnFPSChangedListener(OnFPSChangedListener listener) {
        this.mFPSChangeListener = listener;
    }

    @Override
    public void onFPSChanged(int fps) {
        if (mPrepared) {
            Message msg = Message.obtain();
            msg.arg1 = fps;
            msg.what = MSG_FPS_CHANGED;
            mHandler.sendMessage(msg);
        }
    }
}
