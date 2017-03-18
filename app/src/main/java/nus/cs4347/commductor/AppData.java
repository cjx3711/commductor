package nus.cs4347.commductor;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import java.util.UUID;

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
    private String uuidString;
    private UUID uuid;


    public void init(Context appContext) {
        this.appContext = appContext;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        uuidString = "493a221d-a5c4-44d3-82bc-bf961702a738";
        uuid = UUID.fromString(uuidString);
    }

    public Context getApplicationContext() {
        return appContext;
    }
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public String getUuidString() {
        return uuidString;
    }

    public UUID getUuid() {
        return uuid;
    }
}
