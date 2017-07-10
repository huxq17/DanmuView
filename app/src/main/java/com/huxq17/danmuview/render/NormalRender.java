package com.huxq17.danmuview.render;


import android.graphics.Canvas;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.huxq17.danmuview.danmu.Route;
import com.huxq17.danmuview.utils.DanmuUtil;

public class NormalRender implements IRender {
    private SurfaceHolder mSurfaceHolder;
    private SparseArray<Route> mRoutes;

    @Override
    public void init() {

    }

    public NormalRender(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void onConfigurationChanged(SparseArray<Route> routes, int width, int height) {
        if (routes == null) return;
        mRoutes = routes;
    }

    @Override
    public int drawFrame() {
        if (mRoutes == null) return 0;
        Canvas canvas = null;
        Surface surface = mSurfaceHolder.getSurface();
        if (surface.isValid()) {
            canvas = mSurfaceHolder.lockCanvas();
        }
        if (canvas == null) return 0;
        DanmuUtil.clearCanvas(canvas);
        int routesize = mRoutes.size();
        int drawCount = 0;
        for (int i = 0; i < routesize; i++) {
            Route route = mRoutes.valueAt(i);
            drawCount += drawRoute(route, canvas);
        }
        if (surface.isValid()) {
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
        return drawCount;
    }

    private int drawRoute(Route route, Canvas canvas) {
        if (route == null) return 0;
        int count = route.drawDanmus();
        if (count > 0) {
            canvas.drawBitmap(route.bitmap, route.left, route.top, null);
        }
        return count;
    }

    @Override
    public void clear() {

    }
}
