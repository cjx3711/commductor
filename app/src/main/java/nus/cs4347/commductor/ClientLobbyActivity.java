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
import android.widget.TextView;
import android.widget.Toast;

import nus.cs4347.commductor.bluetooth.BTClientConnector;
import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;
import nus.cs4347.commductor.bluetooth.BTPacketHeader;
import nus.cs4347.commductor.bluetooth.BTConnectCallback;
import nus.cs4347.commductor.enums.InstrumentType;

public class ClientLobbyActivity extends AppCompatActivity {

    private static final String TAG = "ClientLobbyActivity";

    private BTConnectCallback BTConnectCallback;

    // Containers
    LinearLayout connectedInfoLayout;
    ListView pairedListview;

    // Buttons
    Button disconnectButton;
    Button triangleButton;
    Button coconutButton;
    Button pianoButton;
    Button devStartButton;

    // Feedback text views
    TextView connectedTextView;
    TextView selectedTextView;

    InstrumentType selectedInstrument = null;

    BTPacketCallback startActivityCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_lobby);

        triangleButton = (Button) findViewById(R.id.button_triangle);
        coconutButton = (Button) findViewById(R.id.button_coconut);
        pianoButton = (Button) findViewById(R.id.button_piano);
        devStartButton = (Button) findViewById(R.id.button_dev_start);

        pairedListview = (ListView)findViewById(R.id.listview_paired);
        connectedInfoLayout = (LinearLayout)findViewById(R.id.layout_connected_info);
        connectedTextView = (TextView)findViewById(R.id.textview_connected_device);
        disconnectButton = (Button)findViewById(R.id.button_disconnect);

        selectedTextView = (TextView)findViewById(R.id.textview_selected_instrument);

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

        View.OnClickListener instrumentSelect = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( v == triangleButton ) {
                    selectedInstrument = InstrumentType.TRIANGLE;
                    selectedTextView.setText("Selected: Triangle");
                }
                if ( v == coconutButton ) {
                    selectedInstrument = InstrumentType.COCONUT;
                    selectedTextView.setText("Selected: Coconut");
                }
                if ( v == pianoButton ) {
                    selectedInstrument = InstrumentType.PIANO;
                    selectedTextView.setText("Selected: Piamo");
                }
                sendInstrumentPacket();

            }
        };
        triangleButton.setOnClickListener(instrumentSelect);
        coconutButton.setOnClickListener(instrumentSelect);
        pianoButton.setOnClickListener(instrumentSelect);

        devStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInstrumentActivity();
            }
        });
        startActivityCallback = new BTPacketCallback() {
            @Override
            public void packetReceived(BluetoothSocket socket, BTDataPacket packet) {
                if ( packet.getHeader() == BTPacketHeader.SERVER_START_GAME ) {
                    startInstrumentActivity();
                }
            }
        };
        BTConnectCallback = new BTConnectCallback() {
            @Override
            public void playerConnected(final BluetoothSocket s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BTClientManager.getInstance().setSocket(s);
                        showConnected(s);
                        BTClientManager.getInstance().setCallback(startActivityCallback);
                        sendInstrumentPacket();
                    }
                });
            }
        };
    }

    protected void sendInstrumentPacket() {
        if ( selectedInstrument != null ) {
            BTDataPacket packet = new BTDataPacket(BTPacketHeader.CLIENT_INSTRUMENT_TYPE);
            packet.intData = selectedInstrument.getInt();
            BTClientManager.getInstance().sendPacket(packet);
        }
    }

    protected void startInstrumentActivity() {
        if ( selectedInstrument != null ) {
            Intent intent = new Intent(getApplicationContext(), InstrumentTriangleActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(AppData.getInstance().getApplicationContext(), "Can't start without selecting an instrument", Toast.LENGTH_SHORT).show();
        }
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
        BTClientConnector btClientConnector = new BTClientConnector(bluetoothDevice, BTConnectCallback);
        btClientConnector.start();
    }


}
