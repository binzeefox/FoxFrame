package com.binzeefox.foxtemplate.core.client;

import java.io.IOException;
import java.net.Socket;

public interface SocketInterface {
    interface OnReceiveListener{
        void onReceive(String message);
    }

//    Socket getSocket();

    void send(String message);

    void connect() throws IOException;
}
