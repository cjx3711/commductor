package nus.cs4347.commductor;

import android.bluetooth.BluetoothSocket;
import android.media.AudioManager;
import android.media.SoundPool;
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

public class InstrumentPianoActivity extends AppCompatActivity {
    private static final String TAG = "InstrumentPianoActivity";
    Piano piano;

    TextView volumeText;
    TextView bandpassText;

    Button addButton, removeButton;

    int keys = 13;
    int minKeys = 12;
    int maxKeys = 25;

    private SoundPool soundPool;
    private int soundID;
    boolean loaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_piano);

        // Load the sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {loaded = true;
            }
        });
        soundID = soundPool.load(this, R.raw.fart, 1);

        piano = (Piano) findViewById(R.id.view_piano);
        piano.setPianoKeyListener(new Piano.PianoKeyListener() {
            @Override
            public void keyPressed(int id, int action) {
                if (action == 1) {
                    return;
                }
                Log.d(TAG, id + " " + action);
                if (loaded) {
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    float volume = actualVolume / maxVolume;

                    soundPool.play(soundID, volume, volume, 1, 0, 1f);
                }
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
}
