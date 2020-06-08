package com.binzeefox.androidtemplete;

//import com.binzeefox.foxtemplate.core.FoxCore;
import com.binzeefox.foxframe.core.base.FoxApplication;

public class BaseApplication extends FoxApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // or call this method below in your own Application instead
        // FoxCore.init(this);
    }
}
