package nus.cs4347.commductor;

import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
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

        synthThreadManager = SynthThreadManager.getInstance();
        synthThreadManager.init();

        Log.d("OnCreate", "Creating Piano Activity");

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
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                float volume = actualVolume / maxVolume;

                //soundPool.play(soundID, volume, volume, 1, 0, 1f);
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
