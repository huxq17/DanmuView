package com.huxq17.danmuview.danmu;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.huxq17.danmuview.controller.DanmuExt;
import com.huxq17.danmuview.objectpool.ObjectPool;
import com.huxq17.danmuview.utils.DanmuUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class Route {
    public ArrayList<DanmuExt> danmus = new ArrayList<>();
    public volatile boolean enable = true;
    public int line;
    public int left;
    public int top;
    public int width;
    public int height;
    public double speed;
    public Bitmap bitmap;
    public Canvas canvas;
    public FloatBuffer bPos;
    public Buffer indices;
    public boolean hasBitmapBindTexture;
    public boolean isFirstFrame = true;
    private boolean recycled = false;

    public int position = 0, nextPosition = 0;

    public Route(int line, int width, int height, int viewHeight) {
        set(line, width, height, viewHeight);
    }

    public void set(int line, int width, int height, int viewHeight) {
        recycled = false;
        isFirstFrame = true;
        this.line = line;
        this.top = line;
        this.left = 0;
        this.width = width;
        this.height = height;
        hasBitmapBindTexture = false;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        final float sPosTop = viewHeight - top;
        final float sPosBottom = sPosTop - height;
        float[] sPos = new float[]{
                0, sPosTop, 0.0f, 0.0f,
                0, sPosBottom, 0.0f, 1.0f,
                width, sPosBottom, 1.0f, 1.0f,
                width, sPosTop, 1.0f, 0.0f
        };
        bPos = ByteBuffer.allocateDirect(sPos.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(sPos);
        bPos.position(0);
        short[] index = {1, 2, 3, 4};

        indices = ByteBuffer.allocateDirect(index.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer().put(index).position(0);
    }

    public void change() {
        position = position ^ nextPosition;
        nextPosition = position ^ nextPosition;
        position = position ^ nextPosition;
        if (isFirstFrame) {
            isFirstFrame = false;
        }
    }

    public void addDanmu(DanmuExt danmu) {
        synchronized (danmus) {
            danmus.add(danmu);
        }
    }

    public void removeDanmu(DanmuExt danmu) {
        synchronized (danmus) {
            danmus.remove(danmu);
        }
    }

    private ArrayList<DanmuExt> mUselessDanmus = new ArrayList<>();

    public int drawDanmus() {
        if (recycled) return 0;
        synchronized (danmus) {
            DanmuUtil.clearCanvas(canvas);
            for (int i = 0; i < danmus.size(); i++) {
                DanmuExt danmu = danmus.get(i);
                drawDanmu(danmu);
            }
            //Find invalid index exception here,fix it later. fixed.
            int invalidNum = mUselessDanmus.size();
            for (int i = invalidNum - 1; i >= 0; i--) {
                DanmuExt danmu = danmus.get(i);
                removeDanmu(danmu);
                ObjectPool.instance().recycleDanmu(danmu);
            }
            if (mDanmuRemoveListener != null) {
                mDanmuRemoveListener.onDanmuRemoved(invalidNum);
            }
            mUselessDanmus.clear();
            return danmus.size();
        }
    }

    private void drawDanmu(DanmuExt danmu) {
        if (danmu.startTime == 0) {
            danmu.startTime = System.currentTimeMillis();
        }
        long runningTime = System.currentTimeMillis() - danmu.startTime;
        final int distance = danmu.distance;
        danmu.curX = (int) (danmu.startX + runningTime * speed);
        int bottomP = danmu.top;
        canvas.drawText(danmu.text, danmu.curX, 0 - bottomP, danmu.textPaint);
        //enable route
        if (!danmu.showCompletely && (danmu.startX - danmu.curX) >= danmu.width) {
            if (!enable) {
                danmu.showCompletely = true;
                enable = true;
            }
        }
        //remove danmu that is out of screen
        if ((danmu.startX - danmu.curX) >= distance) {
            mUselessDanmus.add(danmu);
        }
    }

    private OnDanmuRemoveListener mDanmuRemoveListener;

    public void setDanmuRemoveListener(OnDanmuRemoveListener listener) {
        mDanmuRemoveListener = listener;
    }

    public interface OnDanmuRemoveListener {
        void onDanmuRemoved(int count);
    }

    public void recycle() {
        synchronized (danmus) {
            recycled = true;
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (mDanmuRemoveListener != null) {
                mDanmuRemoveListener.onDanmuRemoved(danmus.size());
            }
            danmus.clear();
            canvas = null;
        }
    }

    public ArrayList<DanmuExt> getDanmus() {
        return danmus;
    }
}