package nus.cs4347.commductor;

import android.os.Bundle;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.lang.Math;

public class GesturesActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TextView dataText;
    private TextView gestureText;
    private TextView pitchRollText;

    private final double PITCH_ANGLE_THRESHOLD = 15.0;
    private final double ROLL_ANGLE_THRESHOLD = 15.0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestures);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        dataText = (TextView)findViewById(R.id.data_text);
        gestureText = (TextView)findViewById(R.id.gesture_text);
        pitchRollText = (TextView)findViewById(R.id.pitch_roll_text);
    }
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        double pitchAngle = Math.toDegrees(Math.atan(y/z));
        double rollAngle = Math.toDegrees(Math.atan(x/z));
        pitchRollText.setText("Pitch: " + pitchAngle + "\n" + "Roll: " + rollAngle);
        gestureText.setText("Gesture: " + detectGesture(pitchAngle, rollAngle));
        dataText.setText("Data: \n" + "x: " + x + "\n" + "y: " + y + "\n" + "z: " + z);
    }

    public String detectGesture(double pitchAngle, double rollAngle) {
        if(Math.abs(rollAngle) > ROLL_ANGLE_THRESHOLD){
            if(rollAngle > 0){
                return "Rolling Left";
            }
            else {
                return "Rolling Right";
            }
        }
        else if(Math.abs(pitchAngle) > PITCH_ANGLE_THRESHOLD){
            if(pitchAngle > 0){
                return "Tilting Up";
            }
            else{
                return "Tilting Down";
            }
        }
        // Random brownian motion
        else {
            return "Rest";
        }

    }

}
