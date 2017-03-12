package nus.cs4347.commductor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Set;

import nus.cs4347.commductor.bluetooth.BTClientConnector;
import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.PlayerConnectCallback;

public class ClientLobbyActivity extends AppCompatActivity {

    private static final String TAG = "ClientLobbyActivity";
    Button triangleButton;

    private PlayerConnectCallback playerConnectCallback;

    // Containers
    LinearLayout connectedInfoLayout;
    TextView connectedTextView;
    ListView pairedListview;
    Button disconnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_lobby);

        triangleButton = (Button) findViewById(R.id.button_triangle);
        pairedListview = (ListView)findViewById(R.id.listview_paired);
        connectedInfoLayout = (LinearLayout)findViewById(R.id.layout_connected_info);
        connectedTextView = (TextView)findViewById(R.id.textview_connected_device);
        disconnectButton = (Button)findViewById(R.id.button_disconnect);

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



        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, pairedStrings);
        pairedListview.setAdapter(adapter);
        pairedListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, ""+position);
                connectTo(pairedDevices[position]);
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideConnected();
            }
        });


        triangleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTClientManager.getInstance().initBluetoothService();
                Intent intent = new Intent(getApplicationContext(), InstrumentTriangleActivity.class);
                startActivity(intent);
            }
        });
        playerConnectCallback = new PlayerConnectCallback() {
            @Override
            public void playerConnected(final BluetoothSocket s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BTClientManager.getInstance().setSocket(s);
                        showConnected(s);
                    }
                });
            }
        };
    }

    protected void showConnected(BluetoothSocket socket) {
        pairedListview.setVisibility(View.GONE);
        connectedInfoLayout.setVisibility(View.VISIBLE);
        connectedTextView.setText(socket.getRemoteDevice().getName());
    }
    protected void hideConnected() {
        pairedListview.setVisibility(View.VISIBLE);
        connectedInfoLayout.setVisibility(View.GONE);
    }

    protected void connectTo(BluetoothDevice bluetoothDevice) {
        BTClientConnector btClientConnector = new BTClientConnector(bluetoothDevice, playerConnectCallback);
        btClientConnector.start();
    }


}
