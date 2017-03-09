package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

import nus.cs4347.commductor.AppData;

/**
 * Bluetooth Server that listens for incoming connections
 */

public class BTServerConnector extends Thread {
    private static final String TAG = "BTServerConnector";

    private final BluetoothServerSocket serverSocket;

    public BTServerConnector () {
        // Use a temp socket because socket is final
        BluetoothServerSocket tempSocket = null;
        try {
            tempSocket = AppData.getInstance().getBluetoothAdapter().listenUsingRfcommWithServiceRecord("Commductor", AppData.getInstance().getUuid());

        } catch ( IOException e ) {
            e.printStackTrace();
            Log.e(TAG, "Socket's listen() method failed", e);
        }

        serverSocket = tempSocket;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            Log.d(TAG, "Listening...");
            try {
                socket = serverSocket.accept(1000);
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            try {
                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    Log.d(TAG, "Socket accepted");
                    serverSocket.close(); // No more acceptance
                    break;
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
                break;
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
