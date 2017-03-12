package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;

import nus.cs4347.commductor.AppData;


/**
 * The class to connect to the bluetooth server
 */

public class BTClientConnector extends Thread {
    private static final String TAG = "BTClientConnector";
    private BluetoothDevice device;
    private final BluetoothSocket socket;
    private PlayerConnectCallback playerConnectCallback;

    public BTClientConnector(BluetoothDevice device, PlayerConnectCallback playerConnectCallback) {
        this.device = device;
        this.playerConnectCallback = playerConnectCallback;

        // Use a temp socket because socket is final
        BluetoothSocket tempSocket = null;

        try {
            tempSocket = device.createRfcommSocketToServiceRecord(AppData.getInstance().getUuid());
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Socket's create() method failed", e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        socket = tempSocket;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        AppData.getInstance().getBluetoothAdapter().cancelDiscovery();

        Log.d(TAG, "Connecting to bluetooth device: " + device.getName());

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            socket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                socket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            Log.d(TAG, "Could not connect to bluetooth");
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        Log.d(TAG, "Connection attempt succeeded");
        playerConnectCallback.playerConnected(socket);

    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

}
