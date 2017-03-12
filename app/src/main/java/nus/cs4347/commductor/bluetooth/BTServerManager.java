package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import java.util.ArrayList;

import nus.cs4347.commductor.AppData;

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

    public void setCallback(BTPacketCallback callback) {
        for (BluetoothService service : bluetoothServices) {
            service.setCallback(callback);
        }
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
        BTDataPacket packet = new BTDataPacket(BTPacketHeader.STRING_DATA);
        packet.stringData = message;
        for ( BluetoothService service : bluetoothServices ) {
            service.write(packet);
        }
    }

    /**
     * Creates a bluetooth service for every
     * connected socket to send and receive data
     */
    public void initBluetoothServices() {
        bluetoothServices = new ArrayList<>(bluetoothSockets.size());
        for ( BluetoothSocket socket : bluetoothSockets ) {
            bluetoothServices.add(new BluetoothService(socket));
        }
    }

    public ArrayList<BluetoothSocket> getBluetoothSockets() {
        return bluetoothSockets;
    }

}
