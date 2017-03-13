package nus.cs4347.commductor.server;

import android.bluetooth.BluetoothSocket;

import nus.cs4347.commductor.bluetooth.BluetoothService;
import nus.cs4347.commductor.enums.InstrumentType;

/**
 * This object represents the state of a given instrument on the server
 */

public class ServerInstrumentalist {
    private BluetoothSocket socket;
    private BluetoothService service;
    private InstrumentType type;

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

    public InstrumentType getType() {
        return type;
    }

    public void setType(InstrumentType type) {
        this.type = type;
    }
}

