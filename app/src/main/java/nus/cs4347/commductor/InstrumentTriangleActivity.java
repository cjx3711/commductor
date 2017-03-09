package nus.cs4347.commductor;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class InstrumentTriangleActivity extends AppCompatActivity {

    Button playButton;
    private SoundPool soundPool;
    private int soundID;
    boolean loaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_triangle);

        // Load the sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }


        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

                loaded = true;
            }
        });
        soundID = soundPool.load(this, R.raw.fart, 1);

        playButton = (Button) findViewById(R.id.button_play_triangle);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spamSound(v);
            }
        });

    }

    public void spamSound(View v) {

        // Getting the user sound settings
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        float actualVolume = (float) audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);

        float maxVolume = (float) audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        float volume = actualVolume / maxVolume;

        // Is the sound loaded already?
        if (loaded) {
            soundPool.play(soundID, volume, volume, 1, 0, 1f);
            Log.e("Test", "Played sound");

        }
    }



}
