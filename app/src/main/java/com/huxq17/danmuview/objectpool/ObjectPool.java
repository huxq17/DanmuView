package com.huxq17.danmuview.objectpool;


import com.huxq17.danmuview.controller.DanmuExt;
import com.huxq17.danmuview.danmu.Danmu;

import java.util.LinkedList;

public class ObjectPool {
    public static class InstanceHolder {
        private static ObjectPool instance = new ObjectPool();
    }

    private ObjectPool() {
    }

    public static ObjectPool instance() {
        return InstanceHolder.instance;
    }

    private LinkedList<DanmuExt> pool = new LinkedList<>();

    public Danmu obtainDanMu() {
        return DanmuExt.obtain();
    }


    public void recycleDanmu(DanmuExt danmu) {
        danmu.recycle();
    }

    public void clear() {
        pool.clear();
    }
}
