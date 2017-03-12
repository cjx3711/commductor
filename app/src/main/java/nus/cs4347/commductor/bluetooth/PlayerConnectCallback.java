package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;

/**
 * The function that gets called when a client is connected to the server
 */
public interface PlayerConnectCallback {
    void playerConnected(BluetoothSocket s);
}