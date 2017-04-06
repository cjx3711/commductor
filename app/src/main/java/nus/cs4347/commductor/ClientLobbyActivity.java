package nus.cs4347.commductor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
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

import java.util.ArrayList;
import java.util.Set;

import nus.cs4347.commductor.bluetooth.BTClientConnector;
import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;
import nus.cs4347.commductor.bluetooth.BTPacketHeader;
import nus.cs4347.commductor.bluetooth.BTConnectCallback;
import nus.cs4347.commductor.bluetooth.BTServerManager;
import nus.cs4347.commductor.client.Instrumentalist;
import nus.cs4347.commductor.display.InstrumentPagerAdapter;
import nus.cs4347.commductor.display.PlayerPagerAdapter;
import nus.cs4347.commductor.enums.InstrumentType;

public class ClientLobbyActivity extends AppCompatActivity {

    private static final String TAG = "ClientLobbyActivity";

    private BTConnectCallback BTConnectCallback;

    // Containers
    ListView pairedListview;

    // Buttons
    Button devStartButton;

    // Feedback text views
    TextView connectedToTextView;
    TextView connectedTextView;

    TextView selectedTextView;

    // Instrument pager
    InstrumentPagerAdapter instrumentPagerAdapter;
    ViewPager instrumentPager;


    BTPacketCallback startActivityCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_lobby);

        final Instrumentalist instrumentalist = BTClientManager.getInstance().getInstrumentalist();

        devStartButton = (Button) findViewById(R.id.button_dev_start);

        pairedListview = (ListView)findViewById(R.id.listview_paired);
        connectedTextView = (TextView)findViewById(R.id.textview_connected_device);
        connectedToTextView = (TextView)findViewById(R.id.textview_connected_to);
        selectedTextView = (TextView)findViewById(R.id.textview_selected_instrument);
        AppData.getInstance().setFont(connectedTextView);
        AppData.getInstance().setFont(connectedToTextView);
        AppData.getInstance().setFont(selectedTextView);


        instrumentPager = (ViewPager) findViewById(R.id.pager_instrument_select);
        instrumentPagerAdapter = new InstrumentPagerAdapter(this, this.getSupportFragmentManager());
        instrumentPager.setAdapter(instrumentPagerAdapter);
        instrumentPager.setPageTransformer(false, instrumentPagerAdapter);

        instrumentalist.setType(InstrumentType.valueOf(instrumentPagerAdapter.getFirstPage() % 4));
        instrumentPager.setCurrentItem(instrumentPagerAdapter.getFirstPage());
        instrumentPager.setOffscreenPageLimit(3);
        instrumentPager.setPageMargin(-100);
        if ( instrumentalist.getType() != null ) {
            selectedTextView.setText(instrumentalist.getType().toString());
        }


        instrumentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                instrumentalist.setType(InstrumentType.valueOf(position % 4));
                if ( instrumentalist.getType() != null ) {
                    selectedTextView.setText(instrumentalist.getType().toString());
                }
                sendInstrumentPacket();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Get paired devices
        String[] pairedStrings;
        BluetoothDevice [] pairedDevicesTemp = new BluetoothDevice[0];
        Set<BluetoothDevice> pairedDevicesSet = AppData.getInstance().getBluetoothAdapter().getBondedDevices();
        if ( pairedDevicesSet != null ) {
            pairedDevicesTemp = pairedDevicesSet.toArray(pairedDevicesTemp);
        }

        final BluetoothDevice [] pairedDevices = pairedDevicesTemp;

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
        connectedTextView.setText(socket.getRemoteDevice().getName());
    }
    protected void hideConnected() {
    }

    protected void connectTo(BluetoothDevice bluetoothDevice) {
        BTClientConnector btClientConnector = new BTClientConnector(bluetoothDevice, BTConnectCallback);
        btClientConnector.start();
    }


}
