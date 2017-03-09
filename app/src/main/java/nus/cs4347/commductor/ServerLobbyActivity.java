package nus.cs4347.commductor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import nus.cs4347.commductor.bluetooth.BTServerConnector;


public class ServerLobbyActivity extends AppCompatActivity {

    private static final String TAG = "ServerLobbyActivity";
    Button startButton;
    BTServerConnector btServerConnector;
    TextView deviceInfoTextview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);

        deviceInfoTextview = (TextView) findViewById(R.id.textview_device_info);
        startButton = (Button) findViewById(R.id.button_start_game);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ConductorActivity.class);
                startActivity(intent);
            }
        });

        String bluetoothDeviceInfo = AppData.getInstance().getBluetoothAdapter().getName() + " - " + AppData.getInstance().getBluetoothAdapter().getAddress();
        deviceInfoTextview.setText(bluetoothDeviceInfo);
        Log.d(TAG, "Scan Mode: " + AppData.getInstance().getBluetoothAdapter().getScanMode());

        btServerConnector = new BTServerConnector();
        btServerConnector.run();
    }
}
