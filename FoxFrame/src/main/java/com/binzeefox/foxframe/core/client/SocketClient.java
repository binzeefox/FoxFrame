package com.binzeefox.foxframe.core.client;

import android.Manifest;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.binzeefox.foxframe.core.client.parts.SocketInterface;
import com.binzeefox.foxframe.tools.dev.LogUtil;

/**
 * socket连接类
 * TODO 没有测试，不知道能不能正常使用。用法参见MainActivity中的示例
 * @author binze
 * 2020/1/2 10:20
 */
public class SocketClient implements SocketInterface {
    private static final String TAG = "SocketClient";
    private OnReceiveListener onReceiveListener = null; //监听器
    private final SocketBuilder mSocketBuilder;    //用于构造Socket的接口

    private final SocketHandler mHandler;   //工作线程
    private final Handler mUiHandler; //主线程

    /**
     * 实现的工作Handler
     * @author binze 2020/1/2 10:19
     */
    private class SocketHandler extends Handler {
        private PrintWriter writer;

        /**
         * 构造器
         * @param looper    提供looper
         */
        private SocketHandler(@NonNull Looper looper) {
            super(looper);
        }

        /**
         * 发送
         * @author binze 2020/1/2 10:33
         */
        private void send(final String msg){
            post(new Runnable() {
                @Override
                public void run() {
                    writer.println(msg);
                }
            });
        }

        /**
         * 预实现连接方法
         * @author binze 2020/1/2 10:20
         */
        private void connect() {
            post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = mSocketBuilder.createSocket();
                        mSocketBuilder.configSocket(socket);
                        writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter
                                (socket.getOutputStream(), StandardCharsets.UTF_8)), true);
                        BufferedReader reader = new BufferedReader(new InputStreamReader
                                (socket.getInputStream(), StandardCharsets.UTF_8));
                        LogUtil.d(TAG, "run: 已连接");
                        while (true) {
                            final String receiveMsg;
                            //姑且用的是readLine
                            try {
                                if ((receiveMsg = reader.readLine()) != null) {
                                    mUiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (onReceiveListener != null)
                                                onReceiveListener.onReceive(receiveMsg);
                                        }
                                    });
                                }
                            } catch (IOException e){
                                LogUtil.e(TAG, "run: 连接失败", e);
                                break;
                            }
                        }
                    } catch (IOException e){
                        LogUtil.e(TAG, "run: 连接失败", e);
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 建造者模式
     *
     * @author binze 2020/1/2 9:55
     */
    public static class Builder {
        private Integer soTimeout;
        private Integer receiveBufferSize;
        private Integer sendBufferSize;
        private Boolean keepAlive;

        //私有化构造器
        public Builder() {

        }

        /**
         * @see Socket#setSoTimeout(int)
         * @author binze 2019/12/31 15:24
         */
        public Builder soTimeout(int timeout) {
            this.soTimeout = timeout;
            return this;
        }

        /**
         * @see Socket#setKeepAlive(boolean)
         * @author binze 2019/12/31 15:26
         */
        public Builder keepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        /**
         * @see Socket#setReceiveBufferSize(int)
         * @author binze 2019/12/31 15:27
         */
        public Builder receiveBufferSize(int bufferSize) {
            this.receiveBufferSize = bufferSize;
            return this;
        }

        /**
         * @see Socket#setSendBufferSize(int)
         * @author binze 2019/12/31 15:29
         */
        public Builder sendBufferSize(int bufferSize) {
            this.sendBufferSize = bufferSize;
            return this;
        }

        //直接提供socket
        public SocketClient build(final Socket socket){
            return new SocketClient(new _SocketBuilder() {
                @Override
                public Socket createSocket() throws IOException {
                    return socket;
                }
            });
        }

        //提供proxy
        public SocketClient build(final Proxy proxy){
            return new SocketClient(new _SocketBuilder() {
                @Override
                public Socket createSocket() throws IOException {
                    return new Socket(proxy);
                }
            });
        }

        //提供host、端口号
        public SocketClient build(final String host, final int port){
            return new SocketClient(new _SocketBuilder() {
                @Override
                public Socket createSocket() throws IOException {
                    return new Socket(host, port);
                }
            });
        }

        //提供地址、端口号
        public SocketClient build(final InetAddress address, final int port){
            return new SocketClient(new _SocketBuilder() {
                @Override
                public Socket createSocket() throws IOException {
                    return new Socket(address, port);
                }
            });
        }

        //提供Host，端口号，本地地址、本地端口号
        public SocketClient build(final String host, final int port, final InetAddress localAddr,
                                  final int localPort){
            return new SocketClient(new _SocketBuilder() {
                @Override
                public Socket createSocket() throws IOException {
                    return new Socket(host, port, localAddr, localPort);
                }
            });
        }

        //提供地址，端口号，本地地址、本地端口号
        public SocketClient build(final InetAddress address, final int port, final InetAddress localAddr,
                                  final int localPort){
            return new SocketClient(new _SocketBuilder() {
                @Override
                public Socket createSocket() throws IOException {
                    return new Socket(address, port, localAddr, localPort);
                }
            });
        }

        //统一方法
        private abstract class _SocketBuilder implements SocketBuilder {
            @Override
            public void configSocket(Socket socket) throws IOException{
                if (soTimeout != null) socket.setSoTimeout(soTimeout);
                if (receiveBufferSize != null) socket.setReceiveBufferSize(receiveBufferSize);
                if (sendBufferSize != null) socket.setSendBufferSize(sendBufferSize);
                if (keepAlive != null) socket.setKeepAlive(keepAlive);
            }
        }
    }

    /**
     * 构造器
     *
     * @param builder 用于生成Socket的构造器
     * @author binze 2020/1/2 9:32
     */
    private SocketClient(SocketBuilder builder) {
        mSocketBuilder = builder;
        HandlerThread handlerThread = new HandlerThread("socket_handler_thread");
        handlerThread.start();
        mHandler = new SocketHandler(handlerThread.getLooper());
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 发送信息
     *
     * @author binze 2020/1/2 9:53
     */
    @Override
    public void send(String message) {
        if (mHandler.writer != null) {
            mHandler.send(message);
        } else {
            LogUtil.e(TAG, "send: 尚未连接");
        }
    }

    /**
     * 连接
     *
     * @author binze 2020/1/2 9:54
     */
    @Override
    @RequiresPermission(allOf = Manifest.permission.INTERNET)
    public void connect() {
        mHandler.connect();
    }

    /**
     * 设置接受监听器
     *
     * @author binze 2020/1/2 9:51
     */
    public void setOnReceiveListener(OnReceiveListener listener) {
        onReceiveListener = listener;
    }

    /**
     * 用于构造Socket的接口
     *
     * @author binze 2020/1/2 9:29
     */
    private interface SocketBuilder {
        Socket createSocket() throws IOException;

        void configSocket(Socket socket) throws IOException;
    }
}
