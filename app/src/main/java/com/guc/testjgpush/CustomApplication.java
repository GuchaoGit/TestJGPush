package com.guc.testjgpush;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by guc on 2018/11/15.
 * 描述：
 */
public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }
}
