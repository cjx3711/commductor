package nus.cs4347.commductor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.widget.TextView;

public class InstrumentTriangleActivity extends AppCompatActivity implements SensorEventListener {

    Button playButton;
    TextView message;

    private SoundPool soundPool;
    private int soundID;
    boolean loaded = false;

    private SensorManager mSensorManager;
    private Sensor mAccelSensor;

    boolean accelerometerPresent = false;
    int times = 0;

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
                spamSound();
            }
        });
        message = (TextView)findViewById(R.id.message);

        //Load sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            // Success! There's an accelerometer.
            accelerometerPresent = true;
        }

        if (mAccelSensor == null && accelerometerPresent){
            // Use the accelerometer.
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                mAccelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void spamSound() {

        // Getting the user sound settings
        // This will be changed later on, when controlling it
        // I'm leaving it here for reference's sake
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

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        float value = Math.abs(event.values[0]) + Math.abs(event.values[1]) + Math.abs(event.values[2]);
        // Do something with this sensor value.
        if ( value > 20 ) {
            times ++;
            try {
                spamSound();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        message.setText("Times: " + times + " Total: " + value);

    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }


}
