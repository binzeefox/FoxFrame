package com.binzeefox.androidtemplete;

import android.os.Bundle;

import com.binzeefox.foxframe.core.base.FoxActivity;
import com.binzeefox.foxframe.views.cameraview.TextureCameraView;

public class SecondActivity extends FoxActivity {


    @Override
    protected void create(Bundle savedInstanceState) {
        TextureCameraView cameraView = findViewById(R.id.texture_view);
//        cameraView.initCamera();
        cameraView.previewOn();
    }
}
