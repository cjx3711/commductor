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
import nus.cs4347.commductor.client.Instrumentalist;
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
    Button drumsButton;
    Button devStartButton;

    // Feedback text views
    TextView connectedTextView;
    TextView selectedTextView;

    BTPacketCallback startActivityCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_lobby);

        triangleButton = (Button) findViewById(R.id.button_triangle);
        coconutButton = (Button) findViewById(R.id.button_coconut);
        pianoButton = (Button) findViewById(R.id.button_piano);
        drumsButton = (Button) findViewById(R.id.button_drums);
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
                Instrumentalist instrumentalist = BTClientManager.getInstance().getInstrumentalist();
                if ( v == triangleButton ) {
                    instrumentalist.setType(InstrumentType.TRIANGLE);
                }
                if ( v == coconutButton ) {
                    instrumentalist.setType(InstrumentType.COCONUT);
                }
                if ( v == pianoButton ) {
                    instrumentalist.setType(InstrumentType.PIANO);
                }
                if ( v == drumsButton ) {
                    instrumentalist.setType(InstrumentType.DRUMS);
                }

                if ( instrumentalist.getType() != null ) {
                    selectedTextView.setText("Selected: " + instrumentalist.getType().toString());
                }

                sendInstrumentPacket();

            }
        };
        triangleButton.setOnClickListener(instrumentSelect);
        coconutButton.setOnClickListener(instrumentSelect);
        pianoButton.setOnClickListener(instrumentSelect);
        drumsButton.setOnClickListener(instrumentSelect);

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
        if ( BTClientManager.getInstance().getInstrumentalist().getType() != null ) {
            BTDataPacket packet = new BTDataPacket(BTPacketHeader.CLIENT_INSTRUMENT_TYPE);
            packet.intData = BTClientManager.getInstance().getInstrumentalist().getType().getInt();
            BTClientManager.getInstance().sendPacket(packet);
        }
    }

    protected void startInstrumentActivity() {

        if ( BTClientManager.getInstance().getInstrumentalist().getType() != null ) {
            Intent intent;
            switch (BTClientManager.getInstance().getInstrumentalist().getType()) {
                case DRUMS:
                    intent = new Intent(getApplicationContext(), InstrumentDrumkitActivity.class);
                    break;
                case PIANO:
                    intent = new Intent(getApplicationContext(), InstrumentPianoActivity.class);
                    break;
                default:
                    intent = new Intent(getApplicationContext(), InstrumentTriangleActivity.class);
                    break;
            }
            if ( intent != null ) startActivity(intent);

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
