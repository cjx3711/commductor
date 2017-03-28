package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;
import java.util.HashMap;

import nus.cs4347.commductor.enums.InstrumentType;
import nus.cs4347.commductor.server.ServerInstrumentalist;


/**
 * Singleton that manages all the clients connected to the phone.
 *
 */

public class BTServerManager {
    private static BTServerManager singleton = new BTServerManager();
    public static BTServerManager getInstance() {
        return singleton;
    }

    private ArrayList<BluetoothSocket> bluetoothSockets;
    private ArrayList<BluetoothService> bluetoothServices;
    private HashMap<BluetoothSocket, ServerInstrumentalist> instrumentalistMap;
    private ArrayList<ServerInstrumentalist> instrumentalistList;

    private BTPacketCallback callback;

    private BTServerManager() {
        bluetoothSockets = new ArrayList<>();
        bluetoothServices = new ArrayList<>();
        instrumentalistMap = new HashMap<>();
        instrumentalistList = new ArrayList<>();
    }

    public void addSocket(BluetoothSocket socket) {
        bluetoothSockets.add(socket);
        BluetoothService newService = new BluetoothService(socket);
        bluetoothServices.add(newService);
        ServerInstrumentalist instrument = new ServerInstrumentalist(socket, newService);
        instrumentalistList.add(instrument);
        instrumentalistMap.put(socket, instrument);
        if ( callback != null ) {
            newService.setCallback(callback);
        }
    }

    /**
     * Set the callback for when a packet arrives.
     * Setting it will replace the existing one.
     * @param callback Callback to set
     */
    public void setCallback(BTPacketCallback callback) {
        this.callback = callback;
        for (BluetoothService service : bluetoothServices) {
            service.setCallback(callback);
        }
    }

    public void setInstrument(BluetoothSocket socket, InstrumentType type){
        if ( instrumentalistMap.containsKey(socket) ) {
            instrumentalistMap.get(socket).setType(type);
        }
    }


    public void reset() {
        bluetoothSockets.clear();
        if ( bluetoothServices != null ) {
            for (BluetoothService service : bluetoothServices) {
                service.destroy();
            }
            bluetoothServices.clear();
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

    public void sendPacket(BTDataPacket packet) {
        for ( BluetoothService service : bluetoothServices ) {
            service.write(packet);
        }
    }

    /**
     * Creates a bluetooth service for every
     * connected socket to send and receive data
     */
//    public void initBluetoothServices() {
//
//        for ( BluetoothSocket socket : bluetoothSockets ) {
//            bluetoothServices.add(new BluetoothService(socket));
//        }
//    }

    public ArrayList<BluetoothSocket> getBluetoothSockets() {
        return bluetoothSockets;
    }

    public int getInstrumentalistCount() {
        return instrumentalistList.size();
    }

    public ArrayList<ServerInstrumentalist> getInstrumentalistList() {
        return instrumentalistList;
    }

}
