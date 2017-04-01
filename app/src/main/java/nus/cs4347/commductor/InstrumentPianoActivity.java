package nus.cs4347.commductor;

import android.bluetooth.BluetoothSocket;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;
import nus.cs4347.commductor.display.Piano;
import nus.cs4347.commductor.synthesizer.SynthThreadManager;

public class InstrumentPianoActivity extends AppCompatActivity {
    private static final String TAG = "InstrumentPianoActivity";
    Piano piano;

    private SynthThreadManager synthThreadManager;

    TextView volumeText;
    TextView bandpassText;

    Button addButton, removeButton;

    int keys = 13;
    int minKeys = 12;
    int maxKeys = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_piano);

        synthThreadManager = SynthThreadManager.getInstance();
        synthThreadManager.init();

        Log.d("Lifecycle", "Creating");

        piano = (Piano) findViewById(R.id.view_piano);
        piano.setPianoKeyListener(new Piano.PianoKeyListener() {
            @Override
            public void keyPressed(int id, int action) {
                if (action == MotionEvent.ACTION_UP) {
                    synthThreadManager.stopNote (id);
                    return;
                }
                Log.d(TAG, id + " " + action);
                synthThreadManager.playNote(id);
            }
        });

        View.OnClickListener keyAddingListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if ( v == addButton ) {
                    keys++;
                } else {
                    keys--;
                }
                if ( keys > maxKeys ) keys = maxKeys;
                if ( keys < minKeys ) keys = minKeys;
                piano.setKeys(keys);
            }
        };
        piano.setKeys(keys);


        addButton = (Button)findViewById(R.id.button_add_key);
        removeButton = (Button)findViewById(R.id.button_remove_key);
        addButton.setOnClickListener(keyAddingListener);
        removeButton.setOnClickListener(keyAddingListener);

        volumeText = (TextView)findViewById(R.id.text_volume);
        bandpassText = (TextView)findViewById(R.id.text_bandpass);

        final Runnable updateTextRunnable = new Runnable() {
            @Override
            public void run() {
                updateText();
            }
        };
        BTPacketCallback packetCallback = new BTPacketCallback() {
            @Override
            public void packetReceived(BluetoothSocket socket, BTDataPacket packet) {
                runOnUiThread(updateTextRunnable);
            }
        };
        BTClientManager.getInstance().setCallback(packetCallback);
        updateText();
    }

    public void updateText() {
        volumeText.setText((BTClientManager.getInstance().getInstrumentalist().getModifier1() * 100 )+ "");
        bandpassText.setText((BTClientManager.getInstance().getInstrumentalist().getModifier2() * 100 )+ "");
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        Log.d("Lifecycle", "Starting");
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        Log.d("Lifecycle", "Stopping");
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d("Lifecycle", "Resuming");
//    }
//
//    @Override
//    public void onRestart() {
//        super.onRestart();
//        Log.d("Lifecycle", "Restarting");
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", "Destroying");
        synthThreadManager.destroy();
    }

}
