package nus.cs4347.commductor;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import nus.cs4347.commductor.display.Piano;
import nus.cs4347.commductor.synthesizer.SynthThreadManager;

public class InstrumentPianoActivity extends AppCompatActivity {
    private static final String TAG = "InstrumentPianoActivity";
    Piano piano;

    private SoundPool soundPool;
    private int soundID;
    boolean loaded = false;

    private SynthThreadManager synthThreadManager;

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

        synthThreadManager = new SynthThreadManager();
        synthThreadManager.init();

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        soundID = soundPool.load(this, R.raw.fart, 1);

        piano = (Piano) findViewById(R.id.view_piano);
        piano.setPianoKeyListener(new Piano.PianoKeyListener() {
            @Override
            public void keyPressed(int id, int action) {
                if (action == 1) {
                    synthThreadManager.stopNote (id);
                    return;
                }
                Log.d(TAG, id + " " + action);
                if (loaded) {
                    synthThreadManager.playNote(id);
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    float volume = actualVolume / maxVolume;

                    //soundPool.play(soundID, volume, volume, 1, 0, 1f);
                }
            }
        });
    }

    /*
    @Override
    public void onDestroy() {
        super.onDestroy();
        synthThreadManager.destroy();
    }
    */
}
