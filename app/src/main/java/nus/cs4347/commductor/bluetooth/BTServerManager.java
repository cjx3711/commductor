package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;

/**
 * Singleton that manages all the clients connected to the phone
 */

public class BTServerManager {
    private static BTServerManager singleton = new BTServerManager();
    public static BTServerManager getInstance() {
        return singleton;
    }

    private ArrayList<BluetoothSocket> bluetoothSockets;

    private BTServerManager() {
        bluetoothSockets = new ArrayList<>();
    }

    public void addSocket(BluetoothSocket socket) {
        bluetoothSockets.add(socket);
    }


    public void reset() {
        for ( BluetoothSocket socket : bluetoothSockets ) {
            try {
                socket.close();
            } catch ( Exception e ) {

            }
        }
        bluetoothSockets.clear();
    }


    public ArrayList<BluetoothSocket> getBluetoothSockets() {
        return bluetoothSockets;
    }

}
