package com.binzeefox.androidtemplete;

import android.os.Bundle;

import com.binzeefox.foxframe.core.base.FoxActivity;

public class SecondActivity extends FoxActivity {

    @Override
    protected int onSetLayoutResource() {
        return R.layout.activity_second;
    }

    @Override
    protected void create(Bundle savedInstanceState) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
