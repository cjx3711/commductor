package nus.cs4347.commductor.gestures;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.content.Context;
import android.util.Log;
import nus.cs4347.commductor.AppData;

import java.util.ArrayList;


import static android.content.Context.SENSOR_SERVICE;


/**
 * Created by Jonathan on 3/24/17.
 */

public class GesturesProcessor implements SensorEventListener{
    public static final int CONDUCTOR = 0;
    public static final int PLAYER = 1;

    public static final int REST = 0;
    public static final int ROLLING_LEFT = 1;
    public static final int ROLLING_RIGHT = 2;
    public static final int TILTING_DOWN = 3;
    public static final int TILTING_UP = 4;
    public static final int FLIP = 5;
    public static final int TAP = 6;

    private static final double PITCH_ANGLE_THRESHOLD = 15.0;
    private static final double ROLL_ANGLE_THRESHOLD = 15.0;
    private static final double FLIP_THRESHOLD = 19.6;
    private static final double TAP_THRESHOLD = 25.0;
    private static final int CONDUCTOR_WINDOW_SIZE = 20;
    private static final int PLAYER_WINDOW_SIZE = 20;

    private int processorMode;
    private GesturesTapCallback tapCallback;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private ArrayList<SensorData> dataList;

    private int currentGesture = 0;
    private double currentPitch = 0;
    private double currentRoll = 0;
    public float currentX = 0;
    public float currentY = 0;
    public float currentZ = 0;

    private GesturesProcessor() {}
    private static GesturesProcessor singleton = new GesturesProcessor();
    public static GesturesProcessor getInstance() {
        return singleton;
    }

    // PLAYER init will have to provide a tapCallBack that gets executed when a TAP is detected.
    public void init(GesturesTapCallback tapCallback) {
        init();
        this.tapCallback = tapCallback;
        this.processorMode = PLAYER;
    }

    // CONDUCTOR init is the default init
    public void init() {
        if ( mSensorManager != null )  {
            return;
        }
        this.processorMode = CONDUCTOR;
        dataList = new ArrayList<SensorData>();

        mSensorManager = (SensorManager) AppData.getInstance().getApplicationContext().getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

    }

    public void destroy(){
        mSensorManager.unregisterListener(this);
        mSensorManager = null;
        mAccelerometer = null;
    }

    public int getCurrentGesture(){
        return this.currentGesture;
    }

    public double getCurrentPitch(){
        return this.currentPitch;
    }

    public double getCurrentRoll(){
        return this.currentRoll;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        dataList.add(new SensorData(x, y, z));
        currentX = x;
        currentY = y;
        currentZ = z;
        this.currentPitch = Math.toDegrees(Math.atan(y/z));
        this.currentRoll = Math.toDegrees(Math.atan(x/z));
        detectGesture();

    }

    private void detectGesture() {
        // PLAYER gestures
        if(processorMode == PLAYER){
            if(this.dataList.size() < PLAYER_WINDOW_SIZE){
                return;
            }
            for(int i=0; i < PLAYER_WINDOW_SIZE; i++) {
                SensorData data = dataList.get(i);
                if (data.getX() > TAP_THRESHOLD) {
                    Log.d("Tap", Float.toString(data.getX()));
                    this.currentGesture = TAP;
                    this.tapCallback.tapDetected();
                    this.dataList.clear();
                    return;
                }
            }
            this.dataList.clear();
            this.currentGesture = REST;
        }
        // CONDUCTOR Gestures
        else {
            if(this.dataList.size() < CONDUCTOR_WINDOW_SIZE){
                return;
            }
            for(int i=0; i < CONDUCTOR_WINDOW_SIZE; i++){
                SensorData data = dataList.get(i);
                if(Math.abs(data.getZ()) > FLIP_THRESHOLD){
                    Log.d("Flip", "z: " + data.getZ());
                    this.currentGesture = FLIP;
                    this.dataList.clear();
                    return;
                }
            }

            this.dataList.clear();

            double rollAngle = this.currentRoll;
            double pitchAngle = this.currentPitch;

            if(Math.abs(rollAngle) > ROLL_ANGLE_THRESHOLD){
                if(rollAngle > 0){
                    this.currentGesture = ROLLING_LEFT;
                }
                else {
                    this.currentGesture = ROLLING_RIGHT;
                }
            }
            else if(Math.abs(pitchAngle) > PITCH_ANGLE_THRESHOLD){
                if(pitchAngle > 0){
                    this.currentGesture = TILTING_UP;
                }
                else{
                    this.currentGesture = TILTING_DOWN;
                }
            }
            // Random brownian motion
            else {
                this.currentGesture = REST;
            }
        }

    }

    public static String gestureTypeFromCode(int code){
        switch(code){
            case REST:
                return "Rest";
            case ROLLING_LEFT:
                return "Rolling left";
            case ROLLING_RIGHT:
                return "Rolling right";
            case TILTING_DOWN:
                return "Tilting down";
            case TILTING_UP:
                return "Tilting up";
            case FLIP:
                return "Flip";
            case TAP:
                return "Tap";
            default:
                return "Rest";
        }
    }


}
