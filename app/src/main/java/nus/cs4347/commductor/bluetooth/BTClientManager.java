package nus.cs4347.commductor.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.util.Arrays;

import nus.cs4347.commductor.AppData;

/**
 * Manages the single connection for the client
 */

public class BTClientManager {
    private static BTClientManager singleton = new BTClientManager();
    public static BTClientManager getInstance() {
        return singleton;
    }

    private BluetoothSocket bluetoothSocket;
    private BluetoothService bluetoothService;

    public void setSocket(BluetoothSocket socket) {
        bluetoothSocket = socket;
    }

    public void initBluetoothService() {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                // Gets the image task from the incoming Message object.
                int numBytes = inputMessage.arg1;
                byte [] rawData = (byte[])inputMessage.obj;
                byte [] stringData = Arrays.copyOfRange(rawData, 0, numBytes);
                Toast.makeText(AppData.getInstance().getApplicationContext(), new String(stringData), Toast.LENGTH_SHORT).show();
            }
        };
        if ( bluetoothSocket != null ) {
            bluetoothService = new BluetoothService(bluetoothSocket, handler);
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

}
