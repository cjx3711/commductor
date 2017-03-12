package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;

/**
 * Manages the single connection for the client
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
            bluetoothService = new BluetoothService(bluetoothSocket, null);
        }
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
