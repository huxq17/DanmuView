package com.huxq17.danmuview.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.TextureView;

import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.huxq17.danmuview.utils.DrawStatistics;
import com.huxq17.danmuview.utils.EGLHelper;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import javax.microedition.khronos.opengles.GL10;

public class EGLTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    private GLSurfaceView.Renderer mRenderer;
    private EGLThread mEGLThread;
    private final WeakReference<EGLTextureView> mThisWeakRef =
            new WeakReference<>(this);
    private static final Object mLock = new Object();
    private SurfaceTexture mSurface;
    private static final int SIZE_MASK = 0xffff0000;

    public EGLTextureView(Context context) {
        super(context);
        init();
    }

    public EGLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOpaque(false);
        setWillNotCacheDrawing(true);
        setDrawingCacheEnabled(false);
        setSurfaceTextureListener(this);
    }

    public void setRenderer(GLSurfaceView.Renderer renderer) {
        mRenderer = renderer;
        mEGLThread = new EGLThread(mThisWeakRef);
        TaskPool.getInstance().execute(mEGLThread);
    }

    public void requestRender() {
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = surface;
        mEGLThread.post(new Message(Message.MSG_SURFACE_CREATE, surface));
        notifyWindowSizeChanged(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        notifyWindowSizeChanged(width, height);
    }

    private void notifyWindowSizeChanged(int width, int height) {
        int sizeSpec = (width << 16 & SIZE_MASK) | (height & ~SIZE_MASK);
        Message message = new Message(Message.MSG_SURFACE_CHANGED);
        message.args = sizeSpec;
        mEGLThread.post(message);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mEGLThread.post(new Message(Message.MSG_SURFACE_DESTROYED));
        TaskPool.getInstance().cancelTask(mEGLThread);
        return true;
    }

    public void onSurfaceDestroyed() {
    }

    public void onResume() {
        mEGLThread.post(new Message(Message.MSG_REQUEST_RESUME));
    }

    public void onPause() {
        mEGLThread.post(new Message(Message.MSG_REQUEST_PAUSE));
    }

    class Message {
        private static final int MSG_SURFACE_CREATE = 0x1;
        private static final int MSG_SURFACE_CHANGED = 0x2;
        private static final int MSG_SURFACE_DESTROYED = 0x3;
        private static final int MSG_REQUEST_RESUME = 0x4;
        private static final int MSG_REQUEST_PAUSE = 0x5;
        public int what;
        public Object obj;
        public int args;

        public Message(int what, Object obj) {
            this.what = what;
            this.obj = obj;
        }

        public Message(int what) {
            this(what, null);
        }
    }

    static class EGLThread extends Task {

        private WeakReference<EGLTextureView> mEGLTextureViewWeakRef;
        private LinkedList<Message> mMessageQueue = new LinkedList<>();
        private int mWidth;
        private int mHeight;
        private boolean mHasSurface = false;
        private boolean mPausing = false;
        private GL10 mGL10;
        private EGLHelper mEGLHelper;

        EGLThread(WeakReference<EGLTextureView> eGLTextureViewWeakRef) {
            mEGLTextureViewWeakRef = eGLTextureViewWeakRef;
            mEGLHelper = new EGLHelper();
        }

        @Override
        public void onRun() {
            while (isRunning()) {
                synchronized (mLock) {
                    while (!mMessageQueue.isEmpty()) {
                        Message msg = mMessageQueue.poll();
                        resolveMessage(msg);
                    }
                    if (!readyToDraw()) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
                EGLTextureView view = mEGLTextureViewWeakRef.get();
                if (view != null) {
                    view.mRenderer.onDrawFrame(mGL10);
                }
                mEGLHelper.swap();
                final long dt = DrawStatistics.instance.getDrawTime();
                if (dt < 16) {
                    synchronized (mLock) {
                        try {
                            mLock.wait(16 - dt);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private boolean readyToDraw() {
            return mHasSurface && mWidth > 0 && mHeight > 0 && !mPausing;
        }

        private void resolveMessage(Message msg) {
            EGLTextureView view = null;
            switch (msg.what) {
                case Message.MSG_SURFACE_CREATE:
                    SurfaceTexture surface = (SurfaceTexture) msg.obj;
                    if (surface != null) {
                        if (mHasSurface) return;
                        if (mEGLHelper.eglInit(surface)) {
                            mHasSurface = true;
                            view = mEGLTextureViewWeakRef.get();
                            mGL10 = mEGLHelper.getGL10();
                            if (view != null) {
                                view.mRenderer.onSurfaceCreated(mGL10, mEGLHelper.mEglConfig);
                            }
                        }
                        mGL10 = mEGLHelper.getGL10();
                    }
                    break;
                case Message.MSG_SURFACE_CHANGED:
                    mWidth = (msg.args & SIZE_MASK) >> 16;
                    mHeight = msg.args & ~SIZE_MASK;
                    view = mEGLTextureViewWeakRef.get();
                    if (view != null) {
                        view.mRenderer.onSurfaceChanged(mGL10, mWidth, mHeight);
                    }
                    break;
                case Message.MSG_SURFACE_DESTROYED:
                    mHasSurface = false;
                    view = mEGLTextureViewWeakRef.get();
                    if (view != null) {
                        view.onSurfaceDestroyed();
                    }
                    mEGLHelper.destroy();
                    TaskPool.getInstance().cancelTask(this);
                    break;
                case Message.MSG_REQUEST_RESUME:
                    mPausing = false;
                    break;
                case Message.MSG_REQUEST_PAUSE:
                    mPausing = true;
                    break;
            }
        }


        public void post(Message msg) {
            synchronized (mLock) {
                mMessageQueue.add(msg);
                mLock.notify();
            }
        }

        @Override
        public void cancelTask() {

        }
    }
}
