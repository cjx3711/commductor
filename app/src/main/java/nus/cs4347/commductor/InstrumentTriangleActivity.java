package nus.cs4347.commductor;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import nus.cs4347.commductor.bluetooth.BTClientConnector;
import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;

public class InstrumentTriangleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_triangle);


        BTPacketCallback callback = new BTPacketCallback() {
            @Override
            public void packetReceived(BluetoothSocket socket, BTDataPacket packet) {
                Toast.makeText(AppData.getInstance().getApplicationContext(), packet.stringData, Toast.LENGTH_SHORT).show();
            }
        };
        BTClientManager.getInstance().setCallback(callback);
    }
}
