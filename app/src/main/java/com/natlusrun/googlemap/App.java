package com.natlusrun.googlemap;

import android.app.Application;

import com.natlusrun.googlemap.preferences.PreferenceUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceUtils.init(this);
    }
}
