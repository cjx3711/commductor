package nus.cs4347.commductor;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.Set;

import nus.cs4347.commductor.bluetooth.BTClientConnector;

public class ClientLobbyActivity extends AppCompatActivity {

    private static final String TAG = "ClientLobbyActivity";
    Button triangleButton;
    ListView pairedListview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_lobby);

        triangleButton = (Button) findViewById(R.id.button_triangle);
        triangleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InstrumentTriangleActivity.class);
                startActivity(intent);
            }
        });

        // Get paired devices
        String[] pairedStrings;
        BluetoothDevice [] pairedDevicesSomething = new BluetoothDevice[0];
        pairedDevicesSomething = AppData.getInstance().getBluetoothAdapter().getBondedDevices().toArray(pairedDevicesSomething);

        final BluetoothDevice [] pairedDevices = pairedDevicesSomething;

        int index = 0;

        Log.d(TAG, "There are " + pairedDevices.length + " devices paired");

        pairedStrings = new String[pairedDevices.length];
        if (pairedDevices.length > 0) {

            // There are paired devices. Get the name and address of each paired device.
            index = 0;
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                pairedStrings[index] = deviceName + " - " + deviceHardwareAddress;
                index++;
            }
        }


        pairedListview = (ListView)findViewById(R.id.listview_paired);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, pairedStrings);
        pairedListview.setAdapter(adapter);
        pairedListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, ""+position);
                connectTo(pairedDevices[position]);
            }
        });

    }

    protected void connectTo(BluetoothDevice bluetoothDevice) {
        BTClientConnector btClientConnector = new BTClientConnector(bluetoothDevice);
        btClientConnector.start();
    }


}
