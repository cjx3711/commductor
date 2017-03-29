package nus.cs4347.commductor.server;

import android.bluetooth.BluetoothSocket;

import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketHeader;
import nus.cs4347.commductor.bluetooth.BluetoothService;
import nus.cs4347.commductor.client.Instrumentalist;


/**
 * This object represents the state of a given instrument on the server
 */

public class ServerInstrumentalist extends Instrumentalist {
    private BluetoothSocket socket;
    private BluetoothService service;

    public ServerInstrumentalist(BluetoothSocket socket, BluetoothService service) {
        this.socket = socket;
        this.service = service;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public BluetoothService getService() {
        return service;
    }

    public void updateModifier1() {
        if ( service != null ) {
            BTDataPacket packet = new BTDataPacket(BTPacketHeader.SERVER_UPDATE_MODIFIER_1);
            packet.floatData = modifier1;
            service.write(packet);
        }
    }
    public void updateModifier2() {
        if ( service != null ) {
            BTDataPacket packet = new BTDataPacket(BTPacketHeader.SERVER_UPDATE_MODIFIER_2);
            packet.floatData = modifier2;
            service.write(packet);
        }
    }
}

