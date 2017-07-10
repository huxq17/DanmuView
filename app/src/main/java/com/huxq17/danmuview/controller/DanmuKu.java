package com.huxq17.danmuview.controller;

import android.content.Context;
import android.text.TextPaint;
import android.util.SparseArray;

import com.huxq17.danmuview.danmu.DanmuMode;
import com.huxq17.danmuview.danmu.Route;
import com.huxq17.danmuview.listener.DataChangedListener;
import com.huxq17.danmuview.objectpool.ObjectPool;
import com.huxq17.danmuview.render.IRender;
import com.huxq17.danmuview.utils.DanmuUtil;
import com.huxq17.danmuview.utils.DensityUtil;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DanmuKu implements Route.OnDanmuRemoveListener {
    private int mRouteHeight = 0;
    private int totalLine = 0;
    private Random random = new Random(System.currentTimeMillis());
    private SparseArray<Route> mRoutes = new SparseArray<>();
    private DanmuMode danmuMode = DanmuMode.TOP;
    private int mViewHeight, mViewWidth;
    private boolean mPrepared;
    private IRender mRender;
    private boolean hasRenderInited;
    private AtomicInteger mDanmuCount = new AtomicInteger();

    private ArrayList<DataChangedListener<DanmuKuStatus>> mRouteChangeListeners = new ArrayList<>();

    public DanmuKu(IRender canvas) {
        mRender = canvas;
    }

    public void changeRenderConfiguration(int width, int height) {
        hasDanmuModeChanged = false;
        if (mRender == null) return;
        mRender.onConfigurationChanged(mRoutes, width, height);
    }

    public int renderFrame() {
        if (!mPrepared || mRender == null) return 0;
        if (hasDanmuModeChanged) {
            changeRenderConfiguration(mViewWidth, mViewHeight);
            hasDanmuModeChanged = false;
        }
        int danmuCount = mDanmuCount.get();
        int drawCount = 0;
        if (danmuCount > 0) {
            drawCount = mRender.drawFrame();
        }
        return danmuCount;
    }

    public void clearRender() {
        if (mRender == null) return;
        mRender.clear();
        hasRenderInited = false;
    }

    public int getRouteHeight() {
        return mRouteHeight;
    }

    public int getTotalLine() {
        return totalLine;
    }

    public void changeConfiguration(int width, int height, int routeHeight) {
        if (width != mViewWidth) {
            synchronized (mRoutes) {
                mRoutes.clear();
                notifyDanmuKuChanged();
            }
        }
        mViewWidth = width;
        mViewHeight = height;
        mRouteHeight = routeHeight;
        totalLine = mViewHeight / mRouteHeight;
        setDanmuMode(true, danmuMode);
        mPrepared = true;
        changeRenderConfiguration(width, height);
    }

    public void initRender() {
        if (!hasRenderInited) {
            mRender.init();
            hasRenderInited = true;
        }
    }

    public void stop() {
        mPrepared = false;
    }

    public void setDanmuMode(DanmuMode mode) {
        setDanmuMode(false, mode);
    }

    private void setDanmuMode(boolean prepare, DanmuMode mode) {
        if (prepare || mPrepared) {
            mPrepared = false;
            if (mode != danmuMode) {
                hasDanmuModeChanged = true;
                notifyDanmuKuChanged(true);
                danmuMode = mode;
            }
            int start = 0;
            int end = 0;

            switch (danmuMode) {
                case TOP:
                    start = 0;
                    end = (int) (totalLine * 0.5) - 1;
                    break;
                case BOTTOM:
                    start = (int) (totalLine * 0.5);
                    end = totalLine - 1;
                    break;
                case FULL:
                    start = 0;
                    end = totalLine - 1;
                    break;
            }
            buildRouteIfNeed(start, end);
            mPrepared = true;
        }
    }

    private void buildRouteIfNeed(int start, int end) {
        SparseArray<Route> newRouteArray = new SparseArray<>();
        synchronized (mRoutes) {
            int newRouteEndIndex = -1;
            int routSize = mRoutes.size();
            for (int i = start; i <= end; i++) {
                int line = mRouteHeight * i;
                int index = i - start;
                Route oldRoute = null;
                newRouteEndIndex = index;
                if (index < routSize) {
                    oldRoute = mRoutes.valueAt(index);
                    if (oldRoute != null) {
                        oldRoute.set(line, mViewWidth, mRouteHeight, mViewHeight);
                        oldRoute.setDanmuRemoveListener(this);
                        newRouteArray.put(line, oldRoute);
                    }
                }
                if (oldRoute == null) {
                    Route route = new Route(line, mViewWidth, mRouteHeight, mViewHeight);
                    route.setDanmuRemoveListener(this);
                    newRouteArray.put(line, route);
                }
            }
            if (newRouteEndIndex < routSize) {
                for (int i = newRouteEndIndex + 1; i < routSize; i++) {
                    Route route = mRoutes.valueAt(i);
                    route.recycle();
                }
            }
            mRoutes = newRouteArray;
            notifyDanmuKuChanged();
        }
    }

    public Route generateRoute() {
        if (!mPrepared) {
            return null;
        }
        int line = -1;
        int start = 0;
        int end = 0;
        switch (danmuMode) {
            case TOP:
                line = random.nextInt((int) (totalLine * 0.5) - 1) * mRouteHeight;
                break;
            case BOTTOM:
                start = (int) (totalLine * 0.5);
                end = totalLine - 1;
                line = (start + random.nextInt(end - start + 1)) * mRouteHeight;
                break;
            case FULL:
                line = random.nextInt(totalLine - 1) * mRouteHeight;
                break;
        }
        if (!isRoutEnable(line)) {
            synchronized (mRoutes) {
                int routesize = mRoutes.size();
                for (int i = 0; i < routesize; i++) {
//                    int tempKey = mRoutes.keyAt(i);
                    Route tempRoute = mRoutes.valueAt(i);
                    if (tempRoute.enable) {
                        return tempRoute;
                    }
                }
            }
            return null;
        }
        Route route = mRoutes.get(line);
        if (route == null) {
            route = new Route(line, mViewWidth, mRouteHeight, mViewHeight);
            route.setDanmuRemoveListener(this);
            mRoutes.put(line, route);
        }
        return route;
    }

    public boolean addDanmu(Context context, DanmuExt danmu) {
        TextPaint textPaint = danmu.getTextPaint();
        textPaint.setTextSize(DensityUtil.dip2px(context, danmu.textSize));
        Route route = generateRoute();
        if (route != null) {
            mDanmuCount.incrementAndGet();
            int width = route.width;
            danmu.textPaint = textPaint;
            danmu.textPaint.setColor(danmu.textColor);
            danmu.width = (int) DanmuUtil.getTextWidth(danmu.textPaint, danmu.text);
            danmu.y = route.line;
            danmu.curX = width;
            danmu.top = (int) danmu.textPaint.getFontMetrics().ascent;
            danmu.startX = width;
            route.enable = false;
            final int distance = danmu.width + width;
            if (route.speed == 0) {
                int duration = 4600 + random.nextInt(2000);
                route.speed = -1.0 * 2000 / duration;
            }
            danmu.route = route;
            danmu.distance = distance;
            danmu.startTime = 0;
            danmu.showCompletely = false;
            route.addDanmu(danmu);
            return true;
        } else {
            ObjectPool.instance().recycleDanmu(danmu);
            return false;
        }
    }

    public void enableRoute(int rout, boolean enable) {
        Route route = mRoutes.get(rout);
        if (route != null)
            route.enable = enable;
    }

    public void enableAllRoutes() {
        synchronized (mRoutes) {
            int routesize = mRoutes.size();
            for (int i = 0; i < routesize; i++) {
                Route tempRoute = mRoutes.valueAt(i);
                tempRoute.enable = true;
            }
        }
    }

    public boolean isRoutEnable(int rout) {
        Route route = mRoutes.get(rout);
        if (route == null) {
            return true;
        }
        return route.enable;
    }

    public void removeDanmu(int line, DanmuExt danmu) {
        ObjectPool.instance().recycleDanmu(danmu);
        Route route = mRoutes.get(line);
        if (route != null) {
            route.removeDanmu(danmu);
        }
    }

    public void addRouteChangeListener(DataChangedListener<DanmuKuStatus> listener) {
        synchronized (mRouteChangeListeners) {
            if (!mRouteChangeListeners.contains(listener)) {
                mRouteChangeListeners.add(listener);
            }
        }
    }

    public void removeRouteChangeListener(DataChangedListener<SparseArray<Route>> listener) {
        synchronized (mRouteChangeListeners) {
            mRouteChangeListeners.remove(listener);
        }
    }

    public void clearRouteChangeListener() {
        synchronized (mRouteChangeListeners) {
            mRouteChangeListeners.clear();
        }
    }

    private void notifyDanmuKuChanged() {
        notifyDanmuKuChanged(false);
    }

    private boolean hasDanmuModeChanged;

    private void notifyDanmuKuChanged(boolean clear) {
        if (Boolean.TRUE.booleanValue()) return;
        synchronized (mRouteChangeListeners) {
            DanmuKuStatus status = new DanmuKuStatus();
            status.routes = clear ? null : mRoutes;
            status.hasDanmuModeChanged = hasDanmuModeChanged;
            for (DataChangedListener<DanmuKuStatus> listener : mRouteChangeListeners) {
                listener.onChanged(status);
            }
            hasDanmuModeChanged = false;
        }
    }

    @Override
    public void onDanmuRemoved(int num) {
        mDanmuCount.getAndAdd(-num);
    }

    public int getDanmuCount() {
        return mDanmuCount.get();
    }

    public class DanmuKuStatus {
        public SparseArray<Route> routes;
        public boolean hasDanmuModeChanged;
    }
}
