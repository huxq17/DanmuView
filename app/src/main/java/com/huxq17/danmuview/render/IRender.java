package com.huxq17.danmuview.render;

import android.util.SparseArray;

import com.huxq17.danmuview.danmu.Route;

public interface IRender {
    void init();

    void onConfigurationChanged(SparseArray<Route> routes, int width, int height);

    int drawFrame();

    void clear();
}
