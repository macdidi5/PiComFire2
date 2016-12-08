package net.macdidi5.picomfire;

import android.support.multidex.MultiDexApplication;

import com.firebase.client.Firebase;

//public class MyApplication extends Application {
// for API 16 ~ 19, add 'multiDexEnabled true' in build.gradle too
public class MyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }

}
