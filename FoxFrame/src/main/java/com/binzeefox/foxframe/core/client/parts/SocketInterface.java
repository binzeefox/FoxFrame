package com.binzeefox.foxframe.core.client.parts;

import java.io.IOException;

public interface SocketInterface {
    interface OnReceiveListener{
        void onReceive(String message);
    }

//    Socket getSocket();

    void send(String message);

    void connect() throws IOException;
}
