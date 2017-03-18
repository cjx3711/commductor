package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;

/**
 * Simple callback method to wrap the handlers.
 * Gets called on bluetooth packet data receipt
 */

public interface BTPacketCallback {
    void packetReceived(BluetoothSocket socket, BTDataPacket packet);
}
