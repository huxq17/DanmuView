package com.huxq17.danmuview.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.text.Layout;
import android.text.TextPaint;

public class DanmuUtil {
    /**
     * 计算字符串的长度
     *
     * @param text 要计算的字符串
     * @return TextView中字符串的长度
     */
    public static float getTextWidth(TextPaint textPaint, String text) {
        return Layout.getDesiredWidth(text, textPaint);
    }

    /**
     * 获取弹幕的高度
     *
     * @return
     */
    public static int getDanmuHeight(TextPaint textPaint) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        return (int) (fontMetrics.bottom - fontMetrics.top + .5f);
    }

    public static void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
}
