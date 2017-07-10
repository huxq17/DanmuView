package com.huxq17.danmuview.controller;

import android.text.TextPaint;

import com.huxq17.danmuview.danmu.Danmu;
import com.huxq17.danmuview.danmu.Route;

public class DanmuExt extends Danmu {
    public int curX;
    public int top;
    public int startX;
    public int y;
    public int distance;
    public Route route;
    public long startTime;
    public boolean showCompletely;
    public int width;
    public TextPaint textPaint;

    public TextPaint getTextPaint() {
        if (textPaint == null) {
            textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
        }
        return textPaint;
    }

    private static final int[] mLock = new int[]{};
    private static DanmuExt sPool;
    private static int sPoolSize = 0;
    private DanmuExt next;

    public static DanmuExt obtain() {
        synchronized (mLock) {
            if (sPool != null) {
                DanmuExt danmu = sPool;
                sPool = danmu.next;
                danmu.next = null;
                sPoolSize--;
                return danmu;
            }
        }
        return new DanmuExt();
    }

    public void recycle() {
        synchronized (mLock) {
            next = sPool;
            sPool = this;
            sPoolSize++;
        }
    }
}
