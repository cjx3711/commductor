package nus.cs4347.commductor;

import android.os.Handler;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


import java.util.ArrayList;

import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketHeader;
import nus.cs4347.commductor.bluetooth.BTServerManager;
import nus.cs4347.commductor.display.PlayerPagerAdapter;
import nus.cs4347.commductor.server.ServerInstrumentalist;
import nus.cs4347.commductor.gestures.GesturesProcessor;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ConductorActivity extends AppCompatActivity {
    private final String TAG = "ConductorActivity";

    private int gestureType = 0;
    private final int GESTURE_PACKET_DELAY_MILLIS = 200;

    private final View.OnTouchListener mDetectGestureButtonTouchListener = new View.OnTouchListener() {
        private Handler mHandler = new Handler();
        @Override
        public boolean onTouch(View view, MotionEvent event) {
//            if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS);
//            }
            // When User holds onto button
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "Button pressed");
                mHandler.postDelayed(sendGesturePackets, GESTURE_PACKET_DELAY_MILLIS);
            }
            // When User releases button
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.d(TAG, "Button released");
                // Stop sending packets
                mHandler.removeCallbacks(sendGesturePackets);
            }

            BTServerManager.getInstance().sendMessage("Test packet message");
            return false;
        }

        private Runnable sendGesturePackets = new Runnable() {
            @Override
            public void run() {
                gestureType = GesturesProcessor.getInstance().getCurrentGesture();
//                Log.d(TAG, "Sending packets for gestures. Gesture: " + GesturesProcessor.gestureTypeFromCode(gestureType));
                Log.d(TAG, "Pitch: " + GesturesProcessor.getInstance().getCurrentPitch() + " Roll: " + GesturesProcessor.getInstance().getCurrentRoll());
                Log.d(TAG, GesturesProcessor.gestureTypeFromCode(gestureType));
//                Log.d(TAG, "x: " + GesturesProcessor.getInstance().currentX + "\n" + "y: " + GesturesProcessor.getInstance().currentY + "\n" + "z: " + GesturesProcessor.getInstance().currentZ);

                switch ( gestureType ) {
                    case GesturesProcessor.TILTING_DOWN:
                    case GesturesProcessor.TILTING_UP:
                        double pitch = GesturesProcessor.getInstance().getCurrentPitch();
                        // Convert pitch from (-50 to 50) to (0 to 1)
                        pitch = (pitch + 50) / 100.0;
                        Log.d(TAG, "Modifier: " + pitch);
                        if ( selectedInstrumentalist != null ) {
                            selectedInstrumentalist.setModifier1((float)pitch);
                            selectedInstrumentalist.sendModifier1();
                        }
                        break;

                    case GesturesProcessor.ROLLING_RIGHT:
                    case GesturesProcessor.ROLLING_LEFT:
                        double roll = GesturesProcessor.getInstance().getCurrentRoll();
                        // Convert roll from (50 to -50) to (0 to 1)
                        roll = 1 - ((roll + 50 ) / 100.0);
                        Log.d(TAG, "Modifier: " + roll);

                        if ( selectedInstrumentalist != null ) {
                            selectedInstrumentalist.setModifier2((float)roll);
                            selectedInstrumentalist.sendModifier2();
                        }
                        break;
                }
                // Post itself to handler again
                mHandler.postDelayed(this, GESTURE_PACKET_DELAY_MILLIS);
            }
        };
    };

    Button detectGestureButton;

    PlayerPagerAdapter playersPagerAdapter;
    ViewPager playersPager;

    Button sendMessageButton;

    ServerInstrumentalist selectedInstrumentalist;
    ArrayList<ServerInstrumentalist> serverInstrumentalists;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conductor);

        serverInstrumentalists = BTServerManager.getInstance().getInstrumentalistList();
        detectGestureButton = (Button)findViewById(R.id.detect_gesture_button);

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        detectGestureButton.setOnTouchListener(mDetectGestureButtonTouchListener);

//        AppData.getInstance().init(getApplicationContext());
        GesturesProcessor.getInstance().init();

        // Set up the pager stuff
        playersPager = (ViewPager) findViewById(R.id.myviewpager);
        playersPagerAdapter = new PlayerPagerAdapter(this, this.getSupportFragmentManager(), BTServerManager.getInstance().getInstrumentalistList());
        playersPager.setAdapter(playersPagerAdapter);
        playersPager.setPageTransformer(false, playersPagerAdapter);

        playersPager.setCurrentItem(playersPagerAdapter.getFirstPage());
        playersPager.setOffscreenPageLimit(3);
        playersPager.setPageMargin(100);

        playersPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int selectedIndex = position % serverInstrumentalists.size();
                selectedInstrumentalist = serverInstrumentalists.get(selectedIndex);
                Log.d(TAG, "Selected:" + selectedIndex);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        sendMessageButton = (Button)findViewById(R.id.button_send_message);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( selectedInstrumentalist != null ) {
                    BTDataPacket packet = new BTDataPacket(BTPacketHeader.STRING_DATA);
                    packet.stringData = "I choose you!";
                    selectedInstrumentalist.getService().write(packet);
                }
            }
        });

    }

}
