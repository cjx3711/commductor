package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import nus.cs4347.commductor.AppData;

/**
 * Bluetooth Server that listens for incoming connections.
 * This is only used at the start to get the BluetoothSockets
 * for the client devices. It is not required for the actual networking.
 */

public class BTServerConnector extends Thread {
    private static final String TAG = "BTServerConnector";

    private final BluetoothServerSocket serverSocket;
    private BTConnectCallback BTConnectCallback;
    private boolean toStop = false;
    public BTServerConnector (BTConnectCallback BTConnectCallback) {
        Log.d(TAG, "Starting server connector");
        this.BTConnectCallback = BTConnectCallback;

        // Use a temp socket because socket is final
        BluetoothServerSocket tempSocket = null;

        try {
            tempSocket = AppData.getInstance().getBluetoothAdapter().listenUsingInsecureRfcommWithServiceRecord("Commductor", AppData.getInstance().getUuid());

        } catch ( IOException e ) {
            e.printStackTrace();
            Log.e(TAG, "Socket's listen() method failed", e);
        }

        serverSocket = tempSocket;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (!toStop) {
            Log.d(TAG, "Listening...");
            try {
                socket = serverSocket.accept(5000);
            } catch (IOException e) {
//                Log.e(TAG, "Socket's accept() method failed", e);
            }

            try {
                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    Log.d(TAG, "Socket accepted");
//                    connectedSockets.add(socket);
                    BTConnectCallback.playerConnected(socket);
                }
                if ( toStop ) {
                    serverSocket.close(); // No more acceptance
                    break;
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
                break;
            }
        }
    }

    public void stopListening() {
        toStop = true;
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
