package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;

/**
 * Singleton that manages the single connection for the client.
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

    /**
     * Set the callback for when a packet arrives.
     * Setting it will replace the existing one.
     * @param callback Callback to set
     */
    public void setCallback(BTPacketCallback callback) {
        if ( bluetoothService != null ) {
            bluetoothService.setCallback(callback);
        }
    }

    public void sendPacket(BTDataPacket packet) {
        if ( bluetoothService != null ) {
            bluetoothService.write(packet);
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
