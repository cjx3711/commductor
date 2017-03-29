package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;

import nus.cs4347.commductor.client.Instrumentalist;

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

    private BTPacketCallback interceptCallback;
    private BTPacketCallback externalCallback;

    private Instrumentalist instrumentalist;

    private BTClientManager() {
        instrumentalist = new Instrumentalist();
        // Allows the client manager to
        interceptCallback = new BTPacketCallback() {
            @Override
            public void packetReceived(BluetoothSocket socket, BTDataPacket packet) {
                BTPacketHeader header = packet.getHeader();
                if ( header == BTPacketHeader.SERVER_UPDATE_MODIFIER_1 ) {
                    instrumentalist.setModifier1(packet.floatData);
                } else if ( header == BTPacketHeader.SERVER_UPDATE_MODIFIER_2 ) {
                    instrumentalist.setModifier2(packet.floatData);
                }
                if ( externalCallback != null ) {
                    externalCallback.packetReceived(socket, packet);
                }
            }
        };
    }

    public void setSocket(BluetoothSocket socket) {
        if ( bluetoothSocket != null ) {
            reset();
        }
        bluetoothSocket = socket;
        bluetoothService = new BluetoothService(bluetoothSocket);
        bluetoothService.setCallback(interceptCallback);
    }

//    public void initBluetoothService() {
//        if ( bluetoothSocket != null ) {
//            bluetoothService = new BluetoothService(bluetoothSocket);
//        }
//    }

    /**
     * Set the callback for when a packet arrives.
     * Setting it will replace the existing one.
     * @param callback Callback to set
     */
    public void setCallback(BTPacketCallback callback) {
        externalCallback = callback;
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

    public Instrumentalist getInstrumentalist() {
        return instrumentalist;
    }

}
