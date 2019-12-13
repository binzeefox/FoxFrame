package com.binzeefox.androidtemplete;

//import com.binzeefox.foxtemplate.core.FoxCore;
import com.binzeefox.foxtemplate.core.base.FoxApplication;

public class BaseApplication extends FoxApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // or call this method in your Application instead
        // FoxCore.init(this);
    }
}
