package nus.cs4347.commductor;

import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;
import nus.cs4347.commductor.bluetooth.BTPacketHeader;
import nus.cs4347.commductor.bluetooth.BTServerConnector;
import nus.cs4347.commductor.bluetooth.BTServerManager;
import nus.cs4347.commductor.bluetooth.BTConnectCallback;
import nus.cs4347.commductor.enums.InstrumentType;


public class ServerLobbyActivity extends AppCompatActivity {

    private static final String TAG = "ServerLobbyActivity";
    Button startButton, refreshButton;
    BTServerConnector btServerConnector;
    TextView deviceInfoTextview;
    ListView connectedListview;
    ArrayAdapter<String> connectedAdapter;

    final BTServerManager btServerManager = BTServerManager.getInstance();

    BTConnectCallback BTConnectCallback;
    BTPacketCallback instrumentChooseCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);

        connectedListview = (ListView)findViewById(R.id.listview_connected);
        deviceInfoTextview = (TextView) findViewById(R.id.textview_device_info);
        startButton = (Button) findViewById(R.id.button_start_game);
        refreshButton = (Button) findViewById(R.id.button_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshListView();
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTDataPacket packet = new BTDataPacket(BTPacketHeader.SERVER_START_GAME);
                BTServerManager.getInstance().sendPacket(packet);
                Intent intent = new Intent(getApplicationContext(), ConductorActivity.class);
                startActivity(intent);
            }
        });

        String bluetoothDeviceInfo = AppData.getInstance().getBluetoothAdapter().getName() + " - " + AppData.getInstance().getBluetoothAdapter().getAddress();
        deviceInfoTextview.setText(bluetoothDeviceInfo);

        int scanMode = AppData.getInstance().getBluetoothAdapter().getScanMode();
        Log.d(TAG, "Scan Mode: " + scanMode);


        BTConnectCallback = new BTConnectCallback() {
            @Override
            public void playerConnected(final BluetoothSocket s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btServerManager.addSocket(s);
                        refreshListView();
                    }
                });
            }
        };

        instrumentChooseCallback = new BTPacketCallback() {
            @Override
            public void packetReceived(BluetoothSocket socket, BTDataPacket packet) {
                String deviceName = socket.getRemoteDevice().getName();
                if ( packet.getHeader() == BTPacketHeader.CLIENT_INSTRUMENT_TYPE ) {
                    InstrumentType type = InstrumentType.valueOf(packet.intData);
                    switch (type) {
                        case TRIANGLE:
                            Toast.makeText(AppData.getInstance().getApplicationContext(),
                                    deviceName + " chose Triangle", Toast.LENGTH_SHORT).show();
                            break;
                        case COCONUT:
                            Toast.makeText(AppData.getInstance().getApplicationContext(),
                                    deviceName + " chose Coconut", Toast.LENGTH_SHORT).show();
                            break;
                        case PIANO:
                            Toast.makeText(AppData.getInstance().getApplicationContext(),
                                    deviceName + " chose Piano", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        };

        BTServerManager.getInstance().setCallback(instrumentChooseCallback);
        if ( scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ) {
            Log.d(TAG, "Bluetooth Already Discoverable");

            btServerConnector = new BTServerConnector(BTConnectCallback);
            btServerConnector.start();
        } else {
            Intent discoverableIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    protected void refreshListView() {
        ArrayList<BluetoothSocket> connectedSockets = btServerManager.getBluetoothSockets();
        Log.d(TAG, "Refreshing list view with " + connectedSockets.size() + " players");
        String [] connected = new String[connectedSockets.size()];
        for ( int i = 0 ; i < connectedSockets.size(); i++ ) {
            connected[i] = connectedSockets.get(i).getRemoteDevice().getName();
        }
        connectedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, connected);

        connectedListview.setAdapter(connectedAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy");
        if ( btServerConnector != null ) {
            btServerConnector.stopListening();
            btServerConnector.cancel();
            btServerConnector = null;
            btServerManager.reset();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Result " + requestCode + " " + resultCode);
        if ( resultCode == 300 ) {
            Log.d(TAG, "Bluetooth Discoverable");
//            btServerConnector = new BTServerConnector();
//            btServerConnector.start();
        } else {
            Log.d(TAG, "Fail");

        }
    }


}
