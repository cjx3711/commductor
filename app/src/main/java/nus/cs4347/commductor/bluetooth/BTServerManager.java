package nus.cs4347.commductor.bluetooth;

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
    private ArrayList<BluetoothService> bluetoothServices;

    private BTServerManager() {
        bluetoothSockets = new ArrayList<>();
    }

    public void addSocket(BluetoothSocket socket) {
        bluetoothSockets.add(socket);
    }


    public void reset() {


        bluetoothSockets.clear();
        if ( bluetoothServices != null ) {
            for (BluetoothService service : bluetoothServices) {
                service.destroy();
            }
            bluetoothServices.clear();
            bluetoothServices = null;
        } else {
            for ( BluetoothSocket socket : bluetoothSockets ) {
                try {
                    socket.close();
                } catch ( Exception e ) {

                }
            }
        }
    }

    public void sendMessage(String message) {
        for ( BluetoothService service : bluetoothServices ) {
            service.write(message.getBytes());
        }
    }

    /**
     * Creates a bluetooth service for every
     * connected socket to send and receive data
     */
    public void initBluetoothServices() {
        bluetoothServices = new ArrayList<>(bluetoothSockets.size());
        for ( BluetoothSocket socket : bluetoothSockets ) {
            bluetoothServices.add(new BluetoothService(socket, null));
        }
    }

    public ArrayList<BluetoothSocket> getBluetoothSockets() {
        return bluetoothSockets;
    }

}
