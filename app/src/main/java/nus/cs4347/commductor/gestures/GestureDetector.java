package nus.cs4347.commductor.gestures;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import nus.cs4347.commductor.AppData;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by chaij on 26/03/2017.
 */

public class GestureDetector {
    private static GestureDetector singleton = new GestureDetector();
    public static GestureDetector getInstance() {
        return singleton;
    }

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private GestureDetector() {

    }

    public void init() {
        if ( mSensorManager != null )  {
            return;
        }
        mSensorManager = (SensorManager) AppData.getInstance().getApplicationContext().getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }
}
