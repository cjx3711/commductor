package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import nus.cs4347.commductor.AppData;

/**
 * Singleton that manages the single connection for the client
 */

public class BTClientManager {
    private static BTClientManager singleton = new BTClientManager();
    public static BTClientManager getInstance() {
        return singleton;
    }

    private BluetoothSocket bluetoothSocket;
    private BluetoothService bluetoothService;

    public void setSocket(BluetoothSocket socket) {
        bluetoothSocket = socket;
    }

    public void initBluetoothService() {
        if ( bluetoothSocket != null ) {
            bluetoothService = new BluetoothService(bluetoothSocket);
        }
    }

    public void setCallback(BTPacketCallback callback) {
        bluetoothService.setCallback(callback);
    }

    public void reset() {
        if ( bluetoothService != null ) {
            bluetoothService.destroy();
            bluetoothService = null;
        } else {
            try {
                bluetoothSocket.close();
            } catch ( Exception e ) {

            }
        }
    }

}
