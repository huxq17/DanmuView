package com.huxq17.danmuview.controller;

import com.huxq17.danmuview.danmu.Danmu;
import com.huxq17.danmuview.danmu.DanmuMode;
import com.huxq17.danmuview.listener.OnFPSChangedListener;

public interface IDanmuView {
    void start();

    void setDanmuMode(DanmuMode danmuMode);

    void sendDanmu(Danmu danmu);

    int getDanmuCount();

    void setOnFPSChangedListener(OnFPSChangedListener listener);

    void stop();

    boolean isPrepared();
}
