package nus.cs4347.commductor;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;

/**
 * Basic soundboard for an instrument.
 */

public class InstrumentDrumkitActivity extends AppCompatActivity {
    Thread t;
    int Fs = 44100; // sample rate, default
    Button [] drumButtons;

    TextView volumeText;
    TextView bandpassText;
    final int HEADER_SIZE = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_drumkit);

        final int buffsize = AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        final Context context = getApplicationContext();

        volumeText = (TextView)findViewById(R.id.text_volume);
        bandpassText = (TextView)findViewById(R.id.text_bandpass);

        drumButtons = new Button[8];

        final String [] drumNames = {
                "Kick", "Conga", "High hat", "Cowbell", "Snare", "Tom Tom 1", "Tom Tom 2", "Tom Tom 3"
        };
        final int [] drumMap = {
                R.raw.drum_bdrum,
                R.raw.drum_conga_mid,
                R.raw.drum_hihat_close,
                R.raw.drum_cowbell,
                R.raw.drum_snare1,
                R.raw.drum_tom1,
                R.raw.drum_tom2,
                R.raw.drum_tom3,
        };

        drumButtons[0] = (Button)findViewById(R.id.button_drum_1);
        drumButtons[1] = (Button)findViewById(R.id.button_drum_2);
        drumButtons[2] = (Button)findViewById(R.id.button_drum_3);
        drumButtons[3] = (Button)findViewById(R.id.button_drum_4);
        drumButtons[4] = (Button)findViewById(R.id.button_drum_5);
        drumButtons[5] = (Button)findViewById(R.id.button_drum_6);
        drumButtons[6] = (Button)findViewById(R.id.button_drum_7);
        drumButtons[7] = (Button)findViewById(R.id.button_drum_8);

        for ( int i = 0; i < 8; i++ ) {
            final int index = i;
            drumButtons[i].setText(drumNames[i]);

            // new code that plays from AudioTrack
            View.OnTouchListener drumTouch = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
                        for ( int j = 0; j < 8; j++ ) {
                            if ( v == drumButtons[j] ) {
                                t = new Thread() {
                                    public void run() {
                                        playDrum(buffsize, context, drumMap[index]);
                                    }
                                };
                                t.start();
                                break;
                            }
                        }
                    }
                    return false;
                }
            };
            drumButtons[i].setOnTouchListener(drumTouch);
        }


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

    }

    private void playDrum(int buffsize, Context context, int file) {

        InputStream is = context.getResources().openRawResource(file);
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);


        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffsize,
                AudioTrack.MODE_STREAM);

        try {
            // read away the header whoosh~
            is.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

            audioTrack.play();
            byte[] sound = new byte[buffsize];
            int count = 0;

            float volume = BTClientManager.getInstance().getInstrumentalist().getModifier1();
            // audioTrack.setStereoVolume(volSliderVal, volSliderVal);
            // setStereoVolume is deprecated
            while ((count = is.read(sound, 0, buffsize)) > -1) {

                float[] audio = byteToFloat(sound);

                short[] shordio = floatToShort(audio);
//                sample[j++] = (short)( (float)v * volume );

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

    public void updateText() {
        volumeText.setText((BTClientManager.getInstance().getInstrumentalist().getModifier1() * 100 )+ "");
        bandpassText.setText((BTClientManager.getInstance().getInstrumentalist().getModifier2() * 100 )+ "");
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

}