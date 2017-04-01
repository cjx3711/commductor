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

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    spamSound();
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
                    spamSound();
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

    }

    public void updateText() {
        volumeText.setText((BTClientManager.getInstance().getInstrumentalist().getModifier1() * 100 )+ "");
        bandpassText.setText((BTClientManager.getInstance().getInstrumentalist().getModifier2() * 100 )+ "");
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }



    public void spamSound() throws IOException {

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
                (dataSize/2)-1,
                AudioTrack.MODE_STREAM);

        try {
            sound = new byte[dataSize]; // 2 bytes per audio frame, 16 bits
            audioTrack.play();
            while ((i = is.read(sound)) != -1) {
                short[] sample = new short[dataSize/2];
                ByteBuffer bb = ByteBuffer.wrap(sound);
                bb.order( ByteOrder.LITTLE_ENDIAN);
                int j = 0;
                while( bb.hasRemaining()) {
                    short v = bb.getShort();
                    sample[j++] = v;
                }

                audioTrack.write(sample, 0, sample.length);
                Log.e("byte size", Arrays.toString(sound));
                Log.e("byte size", Arrays.toString(sample));
            }
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



}
