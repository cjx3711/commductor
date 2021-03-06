package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * The meat of the Bluetooth networking service.
 * This class is a wrapper for a thread that sends and receives bytes
 * to and from a given BluetoothSocket.
 * It wraps all bytes into BTDataPacket objects for convenience.
 */

public class BluetoothService {
    private final static String TAG = "BluetoothService";
    private Handler mHandler; // handler that gets info from Bluetooth service
    private final BluetoothSocket bluetoothSocket;
    private ConnectedThread thread;
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_FAIL = 2;
    }

    public BluetoothService(final BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
        thread = new ConnectedThread(bluetoothSocket);
        thread.start();
    }

    public void setCallback(final BTPacketCallback callback) {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                // Gets the packet
                BTDataPacket packet = (BTDataPacket)inputMessage.obj;
                if ( callback != null ) {
                    callback.packetReceived(bluetoothSocket, packet);
                }
            }
        };
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
                    BTDataPacket successPacket = new BTDataPacket(BTPacketHeader.SEND_SUCCESS);
                    successPacket.intData = bytes.length;
                    Message writtenMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_WRITE, successPacket);
                    writtenMsg.sendToTarget();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                if ( mHandler != null ) {
                    BTDataPacket failurePacket = new BTDataPacket(BTPacketHeader.SEND_FAILURE);
                    Message writeErrorMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_FAIL, failurePacket);
                    writeErrorMsg.sendToTarget();
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
