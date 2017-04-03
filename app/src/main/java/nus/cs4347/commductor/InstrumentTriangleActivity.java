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
import nus.cs4347.commductor_minim.ddf.minim.effects.BandPass;

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
    Button playButton2;
    Button holdButton;

    TextView volumeText;
    TextView bandpassText;
    TextView titleText;

    boolean isHold = false;

    int format;
    int channels;
    int sample_rate;
    int bits_per_sample;
    int dataSize;

    final int HEADER_SIZE = 44;

    Thread t;
//    boolean isRunning = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_triangle);

        playButton = (Button) findViewById(R.id.button_play_triangle);
        holdButton = (Button) findViewById(R.id.button_hold);
        playButton2 = (Button) findViewById(R.id.button_play_triangle2);

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
        playButton2.setOnClickListener(new View.OnClickListener() {
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
        volumeText.setText((BTClientManager.getInstance().getInstrumentalist().getModifier1() * 100 )+ "");
        bandpassText.setText((BTClientManager.getInstance().getInstrumentalist().getModifier2() * 100 )+ "");
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//    }



    public void playSound(Boolean isFilter) throws IOException {

        AudioTrack audioTrack = null;

        try {
            InputStream is = null;

            if (BTClientManager.getInstance().getInstrumentalist().getType() == InstrumentType.TRIANGLE) {
                is = getResources().openRawResource(R.raw.triangle16);
            } else {
                is = getResources().openRawResource(R.raw.coconut);
            }
            updateHeaderData(is);

            int buffsize = AudioTrack.getMinBufferSize(sample_rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

            Log.e("byte - buffer size", String.valueOf(buffsize));

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sample_rate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffsize,
                    AudioTrack.MODE_STREAM);

            BandPass bandpass = new BandPass(5000, 2000, sample_rate);
            audioTrack.play();
            byte[] sound = new byte[buffsize];
            int count = 0;

            while (!isHold && ((count = is.read(sound, 0, buffsize)) > -1)) {

                float[] audio = byteToFloat(sound);
                Log.e("byte count", Arrays.toString(audio));

                if (isFilter) {
                    bandpass.process(audio);
                }
                short[] shordio = floatToShort(audio);

                // recombine signals for playback
                // Load the sound
                Log.e("byte - count", String.valueOf(count));
                Log.e("byte - shordio", Arrays.toString(shordio));
                Log.e("byte - audio len", String.valueOf(audio.length));
                Log.e("byte-end", "=======================");

                audioTrack.write(shordio, 0, shordio.length);

            }

        } catch (IOException e) {

        }

        audioTrack.stop();
        audioTrack.release();
    }

    public void updateHeaderData(InputStream is) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        is.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

        buffer.rewind();
        buffer.position(buffer.position() + 20);
        format = buffer.getShort();

        channels = buffer.getShort();

        sample_rate = buffer.getInt();

        buffer.position(buffer.position() + 6);
        bits_per_sample = buffer.getShort();

        dataSize = 0;

        // not really important I think, but I"m leaving it here
//        while (buffer.getInt() != 0x61746164) { // "data" marker
//            int size = buffer.getInt();
//            is.skip(size);
//
//            buffer.rewind();
//            is.read(buffer.array(), buffer.arrayOffset(), 8);
//            buffer.rewind();
//        }

        dataSize = buffer.getInt();
        Log.e("byte - format", String.valueOf(format));
        Log.e("byte - channel", String.valueOf(channels));
        Log.e("byte - sample rate", String.valueOf(sample_rate));
        Log.e("byte - bps", String.valueOf(bits_per_sample));
        Log.e("byte - data size", String.valueOf(dataSize));
    }

    /**
     * Convert byte[] raw audio to 16 bit int format.
     * @param rawdata
     */
    private short[] byteToShort(byte[] rawdata) {
        short[] sample = new short[rawdata.length/2];
        ByteBuffer bb = ByteBuffer.wrap(rawdata);
        bb.order( ByteOrder.LITTLE_ENDIAN);
        int j = 0;
        while( bb.hasRemaining()) {
            short v = bb.getShort();
            sample[j++] = v;
        }
        return sample;
    }

    private float[] byteToFloat(byte[] audio) {
        return shortToFloat(byteToShort(audio));
    }


    /**
     * Convert int[] audio to 32 bit float format.
     * From [-32768,32768] to [-1,1]
     * @param audio
     */
    private float[] shortToFloat(short[] audio) {
        Log.e("short byte", Arrays.toString(audio));
        float[] converted = new float[audio.length];

        for (int i = 0; i < converted.length; i++) {
            // [-32768,32768] -> [-1,1]
            converted[i] = audio[i] / 32768f; /* default range for Android PCM audio buffers) */

        }

        return converted;
    }

    private short[] floatToShort(float[] buffer) {
        short[] shorts = new short[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            shorts[i] = (short) (buffer[i] * 32768f);
        }
        return shorts;
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
