package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;

public interface PlayerConnectCallback {
    void playerConnected(BluetoothSocket s);
}