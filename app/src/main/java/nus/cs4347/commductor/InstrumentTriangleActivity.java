package nus.cs4347.commductor;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;
import nus.cs4347.commductor.enums.InstrumentType;
import nus.cs4347.commductor.gestures.GesturesTapCallback;
import nus.cs4347.commductor.gestures.GesturesProcessor;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;


public class InstrumentTriangleActivity extends InstrumentPreRecordedActivity {

    Button holdButton;

    TextView volumeText;
    TextView bandpassText;
    TextView filterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_instrument_triangle);

        holdButton = (Button) findViewById(R.id.button_hold);

        volumeText = (TextView) findViewById(R.id.text_volume);
        bandpassText = (TextView) findViewById(R.id.text_bandpass);
        filterText = (TextView) findViewById(R.id.text_filter);

        final View.OnTouchListener holdDown = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    canPlay = true;
                    holdButton.setText("Release to Stop");

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    canPlay = false;
                    holdButton.setText("Hold to Play");
                }
                return false;
            }
        };
        holdButton.setOnTouchListener(holdDown);

        // GesturesTapCallback
        GesturesTapCallback tapCallback = new GesturesTapCallback() {
            public void tapDetected() {
                t = new Thread() {
                    public void run() {
                        // set process priority
                        setPriority(Thread.MAX_PRIORITY);
                        try {
                            if (canPlay) {
                                // Init Audio processor
                                if (BTClientManager.getInstance().getInstrumentalist().getType() == InstrumentType.TRIANGLE) {
                                    playSound(R.raw.triangle16, false);
                                } else {
                                    playSound(R.raw.coconut, false);
                                }

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }

        };

        // Init Gesture Processor with callback
        GesturesProcessor.getInstance().init(tapCallback);

        // Check if coconut or triangle
        if (BTClientManager.getInstance().getInstrumentalist().getType() == InstrumentType.COCONUT) {
//            titleText.setText("Coconut Tok Tok");
            holdButton.setVisibility(View.GONE);
        } else {
            canPlay = false;
        }

        final Runnable updateTextRunnable = new Runnable() {
            @Override
            public void run() {
                updateText(volumeText, bandpassText, filterText);
            }
        };
        BTPacketCallback packetCallback = new BTPacketCallback() {
            @Override
            public void packetReceived(BluetoothSocket socket, BTDataPacket packet) {
                runOnUiThread(updateTextRunnable);
            }
        };

        BTClientManager.getInstance().setCallback(packetCallback);
        updateText(volumeText, bandpassText, filterText);

    }

    public void onDestroy(){
        super.onDestroy();

        if (t != null) {
            t.interrupt();
            t = null;
        }
    }

}
