package com.binzeefox.foxframe.core.client;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;

/**
 * 一个关于socket的服务
 * 尝试用来快速解决socket连接问题，经测试貌似发送和心跳包是没问题了
 * 不过读取信息还是有点问题。
 *
 * TODO 读取问题和编码问题
 *
 * @author binze
 * 2020/6/8 10:24
 */
public class SocketService extends Service {
    public static final String INTENT_IP = "intent_ip";
    public static final String INTENT_PORT = "intent_port";
    public static final String CONNECT_SUCCESS = "CONNECT_SUCCESS";

    private static final String TAG = "SocketService";
    private Socket socket;
    private Thread connectThread;
    private Timer timer = new Timer();
    private OutputStream outS;
    private SocketBinder binder = new SocketBinder();
    private String ip;
    private String port;
    private TimerTask task;
    private boolean isReconnect = true; //重连
    private Handler mainHandler = new Handler(Looper.getMainLooper());  //UI线程

    private SocketListener listener = null;

    private BufferedReader in;
    private String receiveMsg;
    private ListenerThread listenerThread;

    /**
     * 获取服务
     *
     * @author binze 2020/5/14 14:10
     */
    public class SocketBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    /**
     * 接收监听
     *
     * @author binze 2020/5/14 15:07
     */
    public interface SocketListener {
        void onListen(String msg);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        ip = intent.getStringExtra(INTENT_IP);
        port = intent.getStringExtra(INTENT_PORT);
        Log.d(TAG, "onBind: ip = " + ip + "; port = " + port);

        initSocket();
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        ip = intent.getStringExtra(INTENT_IP);
        port = intent.getStringExtra(INTENT_PORT);
        Log.d(TAG, "onStartCommand: ip = " + ip + "; port = " + port);

        initSocket();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化
     *
     * @author binze 2020/5/14 14:16
     */
    private void initSocket() {
        Log.d(TAG, "initSocket: ");
        if (socket != null) return;
        connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                socket = new Socket();
                try {
                    //2s超时
                    socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 2000);
                    if (!socket.isConnected()) throw new IOException("Socket连接失败");
                    showToast("socket 已连接");

                    EventMsg msg = new EventMsg(CONNECT_SUCCESS);
                    //TODO 原文这里用了EventBus讲连接成功的信息发送出去，我懒得发

                    sendMessage("连接成功");
                    socket.setSoTimeout(6000);
                    listen();   //监听
//                    sendBeatData();
                } catch (IOException e) {
                    Log.e(TAG, "run: 生成socket失败", e);
                }
            }
        });

        connectThread.start();
    }

    /**
     * 设置接受监听
     *
     * @author binze 2020/5/14 15:27
     */
    public void setSocketListener(SocketListener listener) {
        this.listener = listener;
    }

    /**
     * 开始心跳包
     *
     * @author binze 2020/5/14 14:28
     */
    private void sendBeatData() {
        final String data = "beat data";
        if (timer == null) timer = new Timer();
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        outS = socket.getOutputStream();
                        outS.write(data.getBytes(StandardCharsets.UTF_8));
                        outS.flush();
                    } catch (Exception e) {
                        Log.e(TAG, "run: 心跳包发送失败", e);
                        showToast("连接断开，正在重连");
                        releaseSocket();
                    }
                }
            };

            timer.schedule(task, 0, 2000);
        }
    }

    /**
     * 发送信息
     *
     * @author binze 2020/5/14 14:45
     */
    public void sendMessage(final String msg) {
        if (socket == null || !socket.isConnected()) {
            showToast("连接错误，请重试");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outS = socket.getOutputStream();
                    if (outS == null) return;
                    outS.write(msg.getBytes(StandardCharsets.UTF_8));
                    outS.flush();
                } catch (IOException e) {
                    Log.e(TAG, "run: ", e);
                }
            }
        }).start();
    }

    /**
     * 监听
     *
     * @author binze 2020/5/14 15:06
     */
    private void listen() {
//        try {
//            socket.setSoTimeout(6000); //超时
//            in = new BufferedReader(new InputStreamReader
//                    (socket.getInputStream(), StandardCharsets.UTF_8));
//            while (true) {
//                if ((receiveMsg = in.readLine()) != null) {
//                    Log.d(TAG, "startReceiving: " + receiveMsg);
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (listener != null)
//                                listener.onListen(receiveMsg);
//                        }
//                    });
//                }
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "run: 客户端线程异常", e);
//        }
        Log.d(TAG, "listen: ");
        if (listenerThread == null) {
            listenerThread = new ListenerThread();
            listenerThread.start();
        }
    }

    /**
     * 释放资源
     *
     * @author binze 2020/5/14 14:34
     */
    private void releaseSocket() {
        try {
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (timer != null) {
                timer.purge();
                timer.cancel();
                timer = null;
            }
            if (outS != null) {
                outS.close();
                outS = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
            connectThread = null;
            //重连
            if (isReconnect) initSocket();
        } catch (Exception e) {
            Log.e(TAG, "releaseSocket: 回收出错", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: destroy");
        isReconnect = false;
        releaseSocket();
    }

    private void showToast(final String msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 数据类
     *
     * @author binze 2020/5/14 14:25
     */
    public static class EventMsg {
        private String tag;

        public EventMsg() {
        }

        public EventMsg(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    /**
     * 继承的监听线程
     *
     * @author binze 2020/5/14 15:49
     */
    private class ListenerThread extends Thread {
        @Override
        public void run() {
            while (!socket.isClosed()) {
                Log.d(TAG, "run: reading");
                try (DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                     ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
                    if (socket == null) continue;
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = in.read(buffer)) != -1) {
                        outStream.write(buffer, 0, len);
                    }
                    final byte[] data = outStream.toByteArray();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) listener.onListen(new String(data));
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "run: ", e);
                }
            }
        }
    }
}
