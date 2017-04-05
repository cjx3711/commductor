package nus.cs4347.commductor;

import android.bluetooth.BluetoothSocket;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;
import nus.cs4347.commductor.enums.InstrumentType;
import nus.cs4347.commductor.gestures.GesturesTapCallback;
import nus.cs4347.commductor.gestures.GesturesProcessor;
import nus.cs4347.commductor.audioProcessor.AudioProcessor;

import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;


public class InstrumentTriangleActivity extends InstrumentPreRecordedActivity {

    Button holdButton;

    TextView volumeText;
    TextView bandpassText;
    TextView filterText;

    boolean isHold = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_instrument_triangle);

        holdButton = (Button) findViewById(R.id.button_hold);

        volumeText = (TextView) findViewById(R.id.text_volume);
        bandpassText = (TextView) findViewById(R.id.text_bandpass);
        filterText = (TextView) findViewById(R.id.text_filter);

        View.OnTouchListener holdDown = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isHold = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    isHold = false;
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
                            if (!isHold) {
                                playSound();
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
            isHold = false;
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


    public void playSound() throws IOException {

        AudioTrack audioTrack = null;

        try {
            // Init Audio processor
            if (BTClientManager.getInstance().getInstrumentalist().getType() == InstrumentType.TRIANGLE) {
                inputStream = getResources().openRawResource(R.raw.triangle16);
            } else {
                inputStream = getResources().openRawResource(R.raw.coconut);
            }
            audioProcessor.updateHeaderData(inputStream);

            int buffsize = AudioTrack.getMinBufferSize(audioProcessor.getSampleRate(), AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

            Log.e("byte - buffer size", String.valueOf(buffsize));

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioProcessor.getSampleRate(),
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffsize,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();
            byte[] sound = new byte[buffsize];
            int count = 0;

            while (!isHold && ((count = inputStream.read(sound, 0, buffsize)) > -1)) {

                float[] audio = AudioProcessor.byteToFloat(sound);


                float limitFreq = AudioProcessor.getLimitFreq(bandPassCoeff);
                if(bandPassCoeff > POSITIVE_FLOOR){
                    audioProcessor.processBPAudio(audio, AudioProcessor.HIGH_PASS_FILTER, limitFreq);
                    Log.d("audioprocess", "Bandpasscoeff: " + Float.toString(bandPassCoeff));
                    Log.d("audioprocess", "Limitfreq: " + Float.toString(limitFreq));
                }
                else if (bandPassCoeff < NEGATIVE_CEIL) {
                    audioProcessor.processBPAudio(audio, AudioProcessor.LOW_PASS_FILTER, limitFreq);
                    Log.d("audioprocess", "Bandpasscoeff: " + Float.toString(bandPassCoeff));
                    Log.d("audioprocess", "Limitfreq: " + Float.toString(limitFreq));
                }
                else {
                    // bandPassCoeff is within POSITIVE_FLOOR and NEGATIVE_CEIL -> do nothing
                    Log.d("bandpasscoeff", "within range");
                }

                audioProcessor.processVolAudio(audio, volumeCoeff);


                short[] shordio = AudioProcessor.floatToShort(audio);

                // recombine signals for playback
                // Load the sound
                Log.e("byte - shordio", Arrays.toString(shordio));
                Log.e("byte count", String.valueOf(count/2));
                Log.e("byte-end", "=======================");

                audioTrack.write(shordio, 0, count/2);

            }

        } catch (IOException e) {

        }

        audioTrack.stop();
        audioTrack.release();
    }


    public void onDestroy(){
        super.onDestroy();

        if (t != null) {
            t.interrupt();
            t = null;
        }
    }

}
