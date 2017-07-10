package com.huxq17.danmuview;


import android.graphics.Color;
import android.os.SystemClock;

import com.andbase.tractor.task.Task;
import com.andbase.tractor.task.TaskPool;
import com.huxq17.danmuview.controller.IDanmuView;
import com.huxq17.danmuview.danmu.Danmu;
import com.huxq17.danmuview.objectpool.ObjectPool;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class DanmuReceiver {
    private Socket socket = null;
    private IDanmuView mDanmuView;

    public DanmuReceiver(IDanmuView danmuView) {
        mDanmuView = danmuView;
    }

    private boolean isDanmuSendTaskRunning = false;
    private boolean started = false;

    public void start() {
        if (started) {
            return;
        }
        started = true;
        if (mDanmuView != null) {
//            mDanmuView.show();
            mDanmuView.start();
        }
        startMockDanmuTask();
//        TaskPool.getInstance().execute(new Task() {
//            @Override
//            public void onRun() {
//                BufferedReader in = null;
//                BufferedWriter writer = null;
//                try {
//                    long startTime = System.currentTimeMillis();
//                    InetAddress address = InetAddress.getByName(host name);
//                    LogUtils.e("address=" + address.getHostAddress() + ";spend=" + (System.currentTimeMillis() - startTime));
//                    socket = new Socket(host name or ip address, port);
//                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                    String send = "{\"msg_type\": \"set_slug\",\"slug\": \"ssmtl\"}\r\n";
//                    writer.write(send);
//                    writer.flush();
//                    startHeartBeatTask();
//                    if (true) {
//                        return;
//                    }
//                    while (!socket.isClosed() && socket.isConnected() && !socket.isInputShutdown()) {
//                        long startTime = System.currentTimeMillis();
//                        String content = in.readLine();
//                        long spend = System.currentTimeMillis() - startTime;
//                        if (spend < 500) {
//                            SystemClock.sleep(500 - spend);
//                        }
//                        if (content != null) {
////                            Danmu danmu = ObjectPool.instance().obtainDanMu();
////                            DamuParse.parse(danmu, content);
//                            //{"msg_type":"danmaku","slug":["ssmtl","ygnlz","shbh"],"content":"123","font_color":"#00ff00","bg_color":"#00ff00","bg_image":1}
////                            LogUtils.e("tcontent=" + content);
//                            if (!TextUtils.isEmpty(content)) {
//                                if (content.contains("get_slug")) {
//                                    try {
//                                        JSONObject jsonObject = new JSONObject(content);
//                                        if (jsonObject.has("pattern")) {
//                                            String pattern = jsonObject.getString("pattern");
//                                            if ("top".equals(pattern)) {
//                                                mDanmuView.setDanmuMode(DanmuMode.TOP);
//                                            } else if ("bottom".equals(pattern)) {
//                                                mDanmuView.setDanmuMode(DanmuMode.BOTTOM);
//                                            } else if ("full".equals(pattern)) {
//                                                mDanmuView.setDanmuMode(DanmuMode.FULL);
//                                            }
//                                        }
//                                    } catch (Exception e) {
//                                    }
//
//                                } else if (content.contains("danmaku")) {
//                                    mDanmuView.sendDanmu(content);
//                                    break;
//                                }
//                            }
//                        } else {
//                            break;
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                    Util.closeQuietly(writer);
//                    Util.closeQuietly(in);
//                    Util.closeQuietly(socket);
//                }
//            }
//
//            @Override
//            public void cancelTask() {
//
//            }
//        });
    }

    private String[] danmuText = {"端午快乐", "我又回来了！", "我的剑就是你的剑",
            "一个能打的都没有", "有情况", "为不能作战的人而战", "想去哪里就去哪里", "我不会轻易的狗带", "66666666666", "这条弹幕最长长长长长长长长长长长长长长长长长长长"};
    private int[] danmuColors = new int[]{
            Color.parseColor("#e51c23"),
            Color.parseColor("#e91e63"),
            Color.parseColor("#9c27b0"),
            Color.parseColor("#673ab7"),
            Color.parseColor("#3f51b5"),
            Color.parseColor("#5677fc"),
            Color.parseColor("#ffc107"),
            Color.parseColor("#009688"),
            Color.parseColor("#259b24"),
    };
    private Task mDanmuSendTask;

    private void startMockDanmuTask() {
        isDanmuSendTaskRunning = true;
        mDanmuSendTask = new Task() {
            private boolean isStopped = false;
            int count = 0;

            @Override
            public void onRun() {
                SystemClock.sleep(1000);
                Random random = new Random();
                boolean limit = Boolean.TRUE.booleanValue();
                while (!isStopped) {
//                    count++;
//                    if (count > 1) isStopped = true;
                    long startTime = System.currentTimeMillis();
                    if (mDanmuView.getDanmuCount() <= 15 || limit) {
                        Danmu danmu = ObjectPool.instance().obtainDanMu();
                        danmu.text = danmuText[random.nextInt(danmuText.length)];
                        danmu.textColor = danmuColors[random.nextInt(danmuColors.length)];
                        mDanmuView.sendDanmu(danmu);
                    } else {
                        SystemClock.sleep(10);
                    }
//                String content = "{\"msg_type\":\"danmaku\",\"slug\":[\"ssmtl\",\"ygnlz\",\"shbh\"],\"content\":\"123\",\"font_color\":\"#00ff00\",\"bg_color\":\"#00ff00\",\"bg_image\":1}";
//                mDanmuView.sendDanmu(content);
                    long spend = System.currentTimeMillis() - startTime;
                    if (spend < 5) {
                        SystemClock.sleep(5 - spend);
                    }
                }
            }

            @Override
            public void cancelTask() {
                isStopped = true;
            }
        };
        TaskPool.getInstance().execute(mDanmuSendTask);
    }

    public void startHeartBeatTask() {
        TaskPool.getInstance().execute(new Task() {
            @Override
            public void onRun() {
                try {
                    while (socket != null && !socket.isClosed() && socket.isConnected() && !
                            socket.isOutputShutdown()) {
                        socket.getOutputStream().write("\r\n".getBytes());
                        SystemClock.sleep(30 * 1000);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void cancelTask() {

            }
        });
    }

    public void switchDanmuSendTaskStatus() {
        if (isDanmuSendTaskRunning()) {
            isDanmuSendTaskRunning = false;
            TaskPool.getInstance().cancelTask(mDanmuSendTask);
        } else {
            isDanmuSendTaskRunning = true;
            startMockDanmuTask();
        }
    }

    public boolean isDanmuSendTaskRunning() {
        return isDanmuSendTaskRunning;
    }

    public void stop() {
        started = false;
        if (mDanmuView != null) {
            TaskPool.getInstance().cancelTask(mDanmuSendTask);
            mDanmuSendTask = null;
            isDanmuSendTaskRunning = false;
            mDanmuView.stop();
        }
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
