package nus.cs4347.commductor;

import android.bluetooth.BluetoothAdapter;
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
    private BluetoothAdapter bluetoothAdapter;

    public void init(Context appContext) {
        this.appContext = appContext;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public Context getApplicationContext() {
        return appContext;
    }
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }


}
