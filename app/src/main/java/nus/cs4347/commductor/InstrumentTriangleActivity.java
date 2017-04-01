package nus.cs4347.commductor;


import android.bluetooth.BluetoothSocket;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;
import nus.cs4347.commductor.bluetooth.BTPacketHeader;
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

import static android.media.AudioTrack.WRITE_NON_BLOCKING;


public class InstrumentTriangleActivity extends AppCompatActivity {

    Button playButton;
    Button playButton2;
    TextView message;

    private SoundPool soundPool;
    private int soundID;
    boolean loaded = false;

    TextView volumeText;
    TextView bandpassText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_triangle);

        // Load the sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }


        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

                loaded = true;
            }
        });
        soundID = soundPool.load(this, R.raw.fart, 1);

        playButton = (Button) findViewById(R.id.button_play_triangle);
        playButton2 = (Button) findViewById(R.id.button_play_triangle2);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    spamSound1();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        playButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    spamSound2();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        message = (TextView)findViewById(R.id.message);
        volumeText = (TextView)findViewById(R.id.text_volume);
        bandpassText = (TextView)findViewById(R.id.text_bandpass);

        // GesturesTapCallback
        GesturesTapCallback tapCallback = new GesturesTapCallback(){
            public void tapDetected(){
                try {
                    spamSound1();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
    }

    public void updateText() {
        volumeText.setText((BTClientManager.getInstance().getInstrumentalist().getModifier1() * 100 )+ "");
        bandpassText.setText((BTClientManager.getInstance().getInstrumentalist().getModifier2() * 100 )+ "");
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }



    public void spamSound1() throws IOException {

        int Fs = 44100;

        int header_size = 44;

        byte[] sound = null;
        int i = 0;
        InputStream is = getResources().openRawResource(R.raw.fartwav);

        ByteBuffer buffer = ByteBuffer.allocate(header_size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        is.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

        buffer.rewind();
        buffer.position(buffer.position() + 20);
        int format = buffer.getShort();

        int channels = buffer.getShort();

        int sample_rate = buffer.getInt();

        buffer.position(buffer.position() + 6);
        int bits_per_sample = buffer.getShort();

        int dataSize = 0;
        while (buffer.getInt() != 0x61746164) { // "data" marker
            int size = buffer.getInt();
            is.skip(size);
            Log.e("byte me size", String.valueOf(size));

            buffer.rewind();
            is.read(buffer.array(), buffer.arrayOffset(), 8);
            buffer.rewind();
        }
        dataSize = buffer.getInt();
        Log.e("byte me", String.valueOf(format));
        Log.e("byte me", String.valueOf(channels));
        Log.e("byte me", String.valueOf(sample_rate));
        Log.e("byte me", String.valueOf(bits_per_sample));
        Log.e("byte me", String.valueOf(dataSize));

        int buffsize = AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.e("I dont byte", String.valueOf(buffsize));

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffsize,
                AudioTrack.MODE_STREAM);

        try {
            BandPass bandpass = new BandPass(19000,2000,44100);
            audioTrack.play();
            sound = new byte[dataSize];
            int count = 0;
            while ((count = is.read(sound, 0, dataSize)) > -1) {

                float[] audio = byteToFloat(sound);
                Log.e("byte count", Arrays.toString(audio));


                bandpass.process(audio);
                short[] shordio = floatToShort(audio);

                // recombine signals for playback
                // Load the sound
                Log.e("byte count", String.valueOf(count));
                Log.e("byte count", Arrays.toString(shordio));
                Log.e("byte count", String.valueOf(audio.length));
                audioTrack.write(shordio, 0, shordio.length);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    audioTrack.write(audio, 0, audio.length, WRITE_NON_BLOCKING);
//                }
            }
            Log.e("test byte", "out of loop");


        } catch (IOException e) {

        }
        audioTrack.stop();
        audioTrack.release();

        // Is the sound loaded already?
//        if (loaded) {
//            soundPool.play(soundID, volume, volume, 1, 0, 1f);
//            Log.e("Test", "Played sound");
//
//        }
    }

    public void spamSound2() throws IOException {

        int Fs = 44100;

        int header_size = 44;

        byte[] sound = null;
        int i = 0;
        InputStream is = getResources().openRawResource(R.raw.fartwav);

        ByteBuffer buffer = ByteBuffer.allocate(header_size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        is.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

        buffer.rewind();
        buffer.position(buffer.position() + 20);
        int format = buffer.getShort();

        int channels = buffer.getShort();

        int sample_rate = buffer.getInt();

        buffer.position(buffer.position() + 6);
        int bits_per_sample = buffer.getShort();

        int dataSize = 0;
        while (buffer.getInt() != 0x61746164) { // "data" marker
            int size = buffer.getInt();
            is.skip(size);
            Log.e("byte me size2", String.valueOf(size));

            buffer.rewind();
            is.read(buffer.array(), buffer.arrayOffset(), 8);
            buffer.rewind();
        }
        dataSize = buffer.getInt();
        Log.e("byte me2", String.valueOf(format));
        Log.e("byte me2", String.valueOf(channels));
        Log.e("byte me2", String.valueOf(sample_rate));
        Log.e("byte me2", String.valueOf(bits_per_sample));
        Log.e("byte me2", String.valueOf(dataSize));

        int buffsize = AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.e("I dont byte2", String.valueOf(buffsize));

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffsize,
                AudioTrack.MODE_STREAM);

        try {
            BandPass bandpass = new BandPass(19000,2000,44100);
            audioTrack.play();
            sound = new byte[dataSize];
            int count = 0;
            while ((count = is.read(sound, 0, dataSize)) > -1) {

                float[] audio = byteToFloat(sound);
                Log.e("byte count2", Arrays.toString(audio));


//                bandpass.process(audio);
                short[] shordio = floatToShort(audio);

                // recombine signals for playback
                // Load the sound
                Log.e("byte count2", String.valueOf(count));
                Log.e("byte count2", Arrays.toString(shordio));
                Log.e("byte count2", String.valueOf(audio.length));
                audioTrack.write(shordio, 0, shordio.length);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    audioTrack.write(audio, 0, audio.length, WRITE_NON_BLOCKING);
//                }
            }
            Log.e("test byte2", "out of loop");


        } catch (IOException e) {

        }
        audioTrack.stop();
        audioTrack.release();

        // Is the sound loaded already?
//        if (loaded) {
//            soundPool.play(soundID, volume, volume, 1, 0, 1f);
//            Log.e("Test", "Played sound");
//
//        }
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
//        Log.d("SHORTTOFLOAT","INSIDE SHORTTOFLOAT");
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
