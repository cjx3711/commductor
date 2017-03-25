package nus.cs4347.commductor;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Basic soundboard for an instrument.
 */

public class InstrumentDrumkitActivity extends AppCompatActivity {
    Button [] drumButtons;
    TextView volumeTV;

    private SoundPool soundPool;
    private int [] soundIDs;
    boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_drumkit);

        volumeTV = (TextView)findViewById(R.id.textview_volume);
        drumButtons = new Button[8];

        // Load the sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }

        final String [] drumNames = {
                "Kick", "Conga", "High hat", "Cowbell", "Snare", "Tom Tom 1", "Tom Tom 2", "Tom Tom 3"
        };
        final int [] drumMap = {
                R.raw.drum_bdrum,
                R.raw.drum_conga_mid,
                R.raw.drum_hihat_close,
                R.raw.drum_cowbell,
                R.raw.drum_snare1,
                R.raw.drum_tom1,
                R.raw.drum_tom2,
                R.raw.drum_tom3,
        };

        drumButtons[0] = (Button)findViewById(R.id.button_drum_1);
        drumButtons[1] = (Button)findViewById(R.id.button_drum_2);
        drumButtons[2] = (Button)findViewById(R.id.button_drum_3);
        drumButtons[3] = (Button)findViewById(R.id.button_drum_4);
        drumButtons[4] = (Button)findViewById(R.id.button_drum_5);
        drumButtons[5] = (Button)findViewById(R.id.button_drum_6);
        drumButtons[6] = (Button)findViewById(R.id.button_drum_7);
        drumButtons[7] = (Button)findViewById(R.id.button_drum_8);

        soundIDs = new int[8];
        for ( int i = 0; i < 8; i ++ ) {
            soundIDs[i] = soundPool.load(this, drumMap[i], 1);
        }

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        View.OnTouchListener drumTouch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
                    for ( int i = 0; i < 8; i++ ) {
                        if ( v == drumButtons[i] ) {
                            playSound(soundIDs[i]);
                            break;
                        }
                    }
                }
                return false;
            }
        };
        for ( int i = 0; i < 8; i++ ) {
            drumButtons[i].setOnTouchListener(drumTouch);
            drumButtons[i].setText(drumNames[i]);
        }

    }

    private void playSound(int soundID) {
        // Getting the user sound settings
        // This will be changed later on, when controlling it
        // I'm leaving it here for reference's sake
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        float volume = actualVolume / maxVolume;

        if (loaded) {
            soundPool.play(soundID, volume, volume, 1, 0, 1f);

        }
    }

}
