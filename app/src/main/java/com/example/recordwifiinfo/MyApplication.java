package com.example.recordwifiinfo;

import android.app.Application;

import com.huaweisoft.ihvc.IHTts;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        IHTts.getInstance().init(this,true);
    }
}
