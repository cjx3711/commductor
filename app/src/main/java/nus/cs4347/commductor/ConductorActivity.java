package nus.cs4347.commductor;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.lang.Math;

import nus.cs4347.commductor.bluetooth.BTServerManager;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ConductorActivity extends AppCompatActivity implements SensorEventListener{
    private final String TAG = "ConductorActivity";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private int gestureType = 0;
    private final int GESTURE_PACKET_DELAY_MILLIS = 200;

    private final View.OnTouchListener mDetectGestureButtonTouchListener = new View.OnTouchListener() {
        private Handler mHandler = null;
        @Override
        public boolean onTouch(View view, MotionEvent event) {
//            if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS);
//            }

            // When User holds onto button
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d("Pressed", "Button pressed");
                if (mHandler != null) return true;
                mHandler = new Handler();
                mHandler.postDelayed(sendGesturePackets, GESTURE_PACKET_DELAY_MILLIS);
            }
            // When User releases button
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.d("Released", "Button released");
                // Stop sending packets
                mHandler.removeCallbacks(sendGesturePackets);
                mHandler = null;
            }

            BTServerManager.getInstance().sendMessage("Test packet message");
            return false;
        }

        private Runnable sendGesturePackets = new Runnable() {
            @Override
            public void run() {
                Log.d("Sending Packets", "Sending packets for gestures. Gesture: " + GesturesProcessor.gestureTypeFromCode(gestureType));

                // Post itself to handler again
                mHandler.postDelayed(this, GESTURE_PACKET_DELAY_MILLIS);
            }
        };
    };

    MediaPlayer kickMP;

    Button detectGestureButton;

    //TODO: Remove these
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TextView dataText;
    private TextView gestureText;
    private TextView pitchRollText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conductor);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        detectGestureButton = (Button)findViewById(R.id.detect_gesture_button);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        detectGestureButton.setOnTouchListener(mDetectGestureButtonTouchListener);

        // Sensor Data
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        dataText = (TextView)findViewById(R.id.data_text);
        gestureText = (TextView)findViewById(R.id.gesture_text);
        pitchRollText = (TextView)findViewById(R.id.pitch_roll_text);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void playSound(View view) {
        kickMP = MediaPlayer.create(getApplicationContext(), R.raw.kick_mp3);

        if(kickMP != null) {
//            kickMP.stop();
//            kickMP.release();
            Log.d(TAG, "Playing sound");

            kickMP.start();
        } else {
            Log.d(TAG, "Still NULL");
        }

    }

    /*
        All sensor-related methods
     */
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

        gestureType = GesturesProcessor.detectGesture(pitchAngle, rollAngle);
        // Just for debugging purposes
        gestureText.setText("Gesture: " + GesturesProcessor.gestureTypeFromCode(gestureType));
        dataText.setText("Data: \n" + "x: " + x + "\n" + "y: " + y + "\n" + "z: " + z);
    }

}
