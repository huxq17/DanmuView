package com.huxq17.danmuview.controller;

import android.content.Context;

import com.huxq17.danmuview.danmu.Danmu;

public interface BaseDanmuView extends IDanmuView {
    /**
     * 弹幕已被发送到屏幕上
     *
     * @param danmu 被发送的弹幕
     */
    void onDanmuAdd(Danmu danmu);

    /**
     * 弹幕被丢弃了
     *
     * @param danmu 被丢弃的弹幕
     */
    void onDanmuAbandon(Danmu danmu);

    Context getContext();
}
