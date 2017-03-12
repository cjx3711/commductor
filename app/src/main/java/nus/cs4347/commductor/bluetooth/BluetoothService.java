package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Basic wrapper for a thread. Sends and receives bytes.
 */

public class BluetoothService {
    private final static String TAG = "BluetoothService";
    private Handler mHandler; // handler that gets info from Bluetooth service
    private ConnectedThread thread;
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    public BluetoothService( BluetoothSocket bluetoothSocket, Handler handler ) {
        thread = new ConnectedThread(bluetoothSocket);
        mHandler = handler;
        thread.start();
    }

    public void write(BTDataPacket packet ) {
        if ( thread != null ) {
            thread.write(packet);
        }
    }

    public void destroy() {
        if ( thread != null ) {
            thread.stopRunning();
            thread.cancel();
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean running = true;
        private byte[] mmBuffer; // mmBuffer store for the stream
        private final String TAG = "ConnectedThread";

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (running) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    BTDataPacket packet = BTDataPacket.convertFromBytes(mmBuffer);

                    // Send the obtained bytes to the UI activity.
                    if ( mHandler != null ) {
                        Message readMsg = mHandler.obtainMessage(
                                MessageConstants.MESSAGE_READ, packet);

                        readMsg.sendToTarget();
                    }
                    Log.d(TAG, numBytes + " bytes received");
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void stopRunning() {
            running = false;
        }

        // Call this from the main activity to send data to the remote device.
        public void write(BTDataPacket packet) {
            byte [] bytes = packet.convertToBytes();
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                if ( mHandler != null ) {
                    Message writtenMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                    writtenMsg.sendToTarget();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                if ( mHandler != null ) {
                    Message writeErrorMsg =
                            mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString("toast",
                            "Couldn't send data to the other device");
                    writeErrorMsg.setData(bundle);
                    mHandler.sendMessage(writeErrorMsg);
                }
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

    }

}
