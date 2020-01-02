package com.binzeefox.androidtemplete;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.binzeefox.foxtemplate.core.base.FoxActivity;
import com.binzeefox.foxtemplate.core.client.SocketClient;
import com.binzeefox.foxtemplate.core.client.SocketInterface;
import com.binzeefox.foxtemplate.tools.phone.NoticeUtil;

import java.io.IOException;


public class MainActivity extends FoxActivity {
    private static final String TAG = "MainActivity";
    private SocketClient client;

    @Override
    protected int onSetLayoutResource() {
        return R.layout.activity_main;
    }

//    @Override
//    protected int onInflateLayout() {
//        return R.layout.activity_main;
//    }

    @Override
    protected void create(Bundle savedInstanceState) {

        findViewById(R.id.btn_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client = new SocketClient.Builder()
                        .soTimeout(60000)
                        .build("121.40.165.18", 8800);
                client.setOnReceiveListener(message -> {
                    NoticeUtil.get().showToast(message);
                    Log.d(TAG, "onClick: " + message);
                });
                client.connect();
            }
        });

        findViewById(R.id.send_btn).setOnClickListener(v -> {
            EditText text = findViewById(R.id.edit_field);
            client.send(text.getText().toString());
        });
    }
}
