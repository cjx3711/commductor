package nus.cs4347.commductor;

import android.content.Context;

/**
 * Singleton class that stores all the global variables in the app
 */


public class AppData {
    private AppData() {

    }
    private static AppData instance = new AppData();
    public static AppData getInstance() {
        return instance;
    }

    private Context appContext;
    public void init(Context appContext) {
        this.appContext = appContext;
    }

    public Context getApplicationContext() {
        return appContext;
    }

}
