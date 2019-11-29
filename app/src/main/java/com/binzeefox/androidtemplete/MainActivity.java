package com.binzeefox.androidtemplete;

import android.os.Bundle;

import com.binzeefox.foxtemplate.base.FoxActivity;

public class MainActivity extends FoxActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected int onInflateLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void create(Bundle savedInstanceState) {

    }
}
