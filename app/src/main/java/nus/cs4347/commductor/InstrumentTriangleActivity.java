package nus.cs4347.commductor;


import android.bluetooth.BluetoothSocket;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.Toast;

import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;
import nus.cs4347.commductor.bluetooth.BTPacketHeader;
import nus.cs4347.commductor.enums.InstrumentType;
import nus.cs4347.commductor.gestures.GesturesTapCallback;
import nus.cs4347.commductor.gestures.GesturesProcessor;
import nus.cs4347.commductor.audioProcessor.AudioProcessor;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


public class InstrumentTriangleActivity extends AppCompatActivity {

    Button playButton;
    Button playButtonModified;
    Button holdButton;

    TextView volumeText;
    TextView bandpassText;
    TextView titleText;
    TextView filterText;

    boolean isHold = false;

    AudioProcessor audioProcessor;
    InputStream inputStream;

    float volumeCoeff;
    float bandPassCoeff;

    private final float POSITIVE_FLOOR = 10.0f;
    private final float NEGATIVE_CEIL = -10.0f;





    Thread t;
//    boolean isRunning = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init Audio Processor
        audioProcessor = new AudioProcessor();

        setContentView(R.layout.activity_instrument_triangle);

        playButton = (Button) findViewById(R.id.button_play_triangle);
        holdButton = (Button) findViewById(R.id.button_hold);
        playButtonModified = (Button) findViewById(R.id.button_play_triangle_modified);


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t = new Thread() {
                    public void run() {
                        // set process priority
                        setPriority(Thread.MAX_PRIORITY);
                        try {
                            if (!isHold) {
                                playSound(false);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        });
        playButtonModified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t = new Thread() {
                    public void run() {
                        // set process priority
                        setPriority(Thread.MAX_PRIORITY);
                        try {
                            if (!isHold) {
                                playSound(true);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };

                t.start();
            }
        });

        volumeText = (TextView) findViewById(R.id.text_volume);
        bandpassText = (TextView) findViewById(R.id.text_bandpass);
        titleText = (TextView) findViewById(R.id.text_title);
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
                                playSound(false);
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

        final Runnable updateTextRunnable = new Runnable() {
            @Override
            public void run() {
                updateText();
            }
        };
        BTPacketCallback packetCallback = new BTPacketCallback() {
            @Override
            public void packetReceived(BluetoothSocket socket, BTDataPacket packet) {
                runOnUiThread(updateTextRunnable);
            }
        };
        BTClientManager.getInstance().setCallback(packetCallback);
        updateText();

        if (BTClientManager.getInstance().getInstrumentalist().getType() == InstrumentType.COCONUT) {
            titleText.setText("Coconut Tok Tok");

            holdButton.setVisibility(View.GONE);
            isHold = false;
        }

    }

    public void updateText() {
        volumeCoeff = BTClientManager.getInstance().getInstrumentalist().getModifier1() * 100;
        bandPassCoeff = BTClientManager.getInstance().getInstrumentalist().getModifier2() * 100 - 50;
        volumeText.setText((volumeCoeff)+ "");
        bandpassText.setText((bandPassCoeff)+ "");

        float limitFreq = AudioProcessor.getLimitFreq(bandPassCoeff);
        if(bandPassCoeff > POSITIVE_FLOOR){
            filterText.setText("High pass, " + Float.toString(limitFreq) + "Hz");
        }
        else if(bandPassCoeff < NEGATIVE_CEIL) {
            filterText.setText("Low pass, " + Float.toString(limitFreq) + "Hz");
        } else {
            filterText.setText("No filtering");
        }

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//    }



    public void playSound(Boolean isFilter) throws IOException {

        AudioTrack audioTrack = null;

        try {
            // Init Audio processor
            if (BTClientManager.getInstance().getInstrumentalist().getType() == InstrumentType.TRIANGLE) {
                inputStream = getResources().openRawResource(R.raw.triangle16);
            } else {
                inputStream = getResources().openRawResource(R.raw.coconut);
            }
            audioProcessor.setInputStream(inputStream);
            audioProcessor.updateHeaderData();

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
                Log.e("byte count", Arrays.toString(audio));

                if (isFilter) {
                    float limitFreq = AudioProcessor.getLimitFreq(bandPassCoeff);
                    if(bandPassCoeff > POSITIVE_FLOOR){
                        audioProcessor.processAudio(audio, AudioProcessor.HIGH_PASS_FILTER, limitFreq);
                        Log.d("audioprocess", "Bandpasscoeff: " + Float.toString(bandPassCoeff));
                        Log.d("audioprocess", "Limitfreq: " + Float.toString(limitFreq));
                    }
                    else if (bandPassCoeff < NEGATIVE_CEIL) {
                        audioProcessor.processAudio(audio, AudioProcessor.LOW_PASS_FILTER, limitFreq);
                        Log.d("audioprocess", "Bandpasscoeff: " + Float.toString(bandPassCoeff));
                        Log.d("audioprocess", "Limitfreq: " + Float.toString(limitFreq));
                    }
                    else {
                        // bandPassCoeff is within POSITIVE_FLOOR and NEGATIVE_CEIL -> do nothing
                        Log.d("bandpasscoeff", "within range");
                    }

                }
                short[] shordio = AudioProcessor.floatToShort(audio);

                // recombine signals for playback
                // Load the sound
                Log.e("byte - count", String.valueOf(count));
                Log.e("byte - shordio", Arrays.toString(shordio));
                Log.e("byte - audio len", String.valueOf(audio.length));
                Log.e("byte buffer", String.valueOf(buffsize));
                Log.e("byte shordio", String.valueOf(shordio.length));
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
//        isRunning = false;
        if (t != null) {
            t.interrupt();
            t = null;
        }
    }
    // Getting the user sound settings
    // This will be changed later on, when controlling it
    // I'm leaving it here for reference's sake
//        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
//
//        float actualVolume = (float) audioManager
//                .getStreamVolume(AudioManager.STREAM_MUSIC);
//
//        float maxVolume = (float) audioManager
//                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//
//        float volume = actualVolume / maxVolume;

}
