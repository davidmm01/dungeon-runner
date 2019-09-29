package fit3037.dmmic2.dungeonrunner;

import android.app.Application;

/*
* Idea to do this from:
* https://stackoverflow.com/questions/21818905/get-application-context-from-non-activity-singleton-class/21819009
* This allows me to access context (i.e. use db helper) in classes such as Equipment where there is
* no getApplicationContext that can be called.
* */

public class MyApplication extends Application {

    private static MyApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static MyApplication getContext() { return mContext; }
}