package nus.cs4347.commductor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


public class MenuActivity extends AppCompatActivity implements SensorEventListener {

    ImageButton startServer;
    ImageButton startClient;
    TextView message;
    ImageView sunburst;
    AppCompatActivity activity;
    private SensorManager mSensorManager;
    private Sensor mAccelSensor;

    int times = 0;

    MediaPlayer mp;

    boolean accelerometerPresent = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        activity = this;

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

        Typeface russian_font = Typeface.createFromAsset(getAssets(), "fonts/Russian.ttf");

        startServer = (ImageButton)findViewById(R.id.button_start_server);
        startClient = (ImageButton)findViewById(R.id.button_start_client);
        message = (TextView)findViewById(R.id.message);
        sunburst = (ImageView)findViewById(R.id.sunburst);
        mp = MediaPlayer.create(activity.getApplicationContext(), R.raw.kick_mp3);


        sunburst.startAnimation(AppData.getInstance().getRotateAnimation());

        startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ServerLobbyActivity.class);
                startActivity(intent);
            }
        });

        startClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClientLobbyActivity.class);
                startActivity(intent);
            }
        });
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

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        float value = Math.abs(event.values[0]) + Math.abs(event.values[1]) + Math.abs(event.values[2]);
        // Do something with this sensor value.
        if ( value > 25 ) {
            times ++;
            try {
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        message.setText("Times: " + times + " Total: " + value);

    }

}
