package com.huxq17.danmuview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.huxq17.danmuview.controller.IDanmuView;
import com.huxq17.danmuview.danmu.DanmuMode;
import com.huxq17.danmuview.listener.OnFPSChangedListener;
import com.huxq17.danmuview.widget.DanmuView;
import com.huxq17.danmuview.widget.GLDanmuView;
import com.huxq17.danmuview.widget.TextureDanmuView;

import java.lang.ref.WeakReference;

public class MainActivity extends Activity {

    public static final int FLAG_SURFACE_VIEW = 0x1;
    public static final int FLAG_GL_SURFACE_VIEW = 0x2;
    public static final int FLAG_TEXTURE_VIEW = 0x3;
    public static final String FLAG = "flag";
    public static final String TAG = MainActivity.class.getSimpleName();

    private DanmuReceiver danmuReceiver;
    private IDanmuView danmuView;
    private Button mBtDanmuSwitch;
    private TextView mTvFPS;
    private WeakReference<MainActivity> mainActivityWeakRef = new WeakReference<>(this);
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int fps = msg.arg1;
            MainActivity mainActivity = mainActivityWeakRef.get();
            if (mainActivity != null) {
                mainActivity.mTvFPS.setText(fps + " fps");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup layout = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        int flag = getIntent().getIntExtra(FLAG, FLAG_SURFACE_VIEW);
        switch (flag) {
            case FLAG_SURFACE_VIEW:
                danmuView = new DanmuView(this);
                break;
            case FLAG_GL_SURFACE_VIEW:
                danmuView = new GLDanmuView(this);
                break;
            case FLAG_TEXTURE_VIEW:
                danmuView = new TextureDanmuView(this);
                break;
        }
        layout.addView((View) danmuView);

        setContentView(layout);
        danmuReceiver = new DanmuReceiver(danmuView);
        danmuReceiver.start();
        mBtDanmuSwitch = (Button) findViewById(R.id.bt_danmu_switch);
        mTvFPS = (TextView) findViewById(R.id.bt_danmu_fps);
        danmuView.setOnFPSChangedListener(new OnFPSChangedListener() {
            @Override
            public void onFPSChanged(int fps) {
                Message message = mHandler.obtainMessage();
                message.arg1 = fps;
                mHandler.sendMessage(message);
            }
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.bt_top:
                danmuView.setDanmuMode(DanmuMode.TOP);
                break;
            case R.id.bt_bottom:
                danmuView.setDanmuMode(DanmuMode.BOTTOM);
                break;
            case R.id.bt_full:
                danmuView.setDanmuMode(DanmuMode.FULL);
                break;
            case R.id.bt_danmu_switch:
                danmuReceiver.switchDanmuSendTaskStatus();
                setSwitchText();
                break;
        }
    }

    private void setSwitchText() {
        mBtDanmuSwitch.setText(danmuReceiver.isDanmuSendTaskRunning() ? "停止" : "开始");
    }

    @Override
    protected void onResume() {
        super.onResume();
        danmuReceiver.start();
        setSwitchText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        danmuReceiver.stop();
    }

}
