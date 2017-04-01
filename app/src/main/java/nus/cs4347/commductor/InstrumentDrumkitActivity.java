package nus.cs4347.commductor;

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

import nus.cs4347.commductor.bluetooth.BTClientManager;

/**
 * Basic soundboard for an instrument.
 */

public class InstrumentDrumkitActivity extends AppCompatActivity {
    int Fs = 44100; // sample rate, default
    Button [] drumButtons;
    TextView volumeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_drumkit);

        final int buffsize = AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        final Context context = getApplicationContext();

        volumeTV = (TextView)findViewById(R.id.textview_volume);
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
                                Thread t = new Thread() {
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
    }

    private void playDrum(int buffsize, Context context, int file) {
        int i = 0;
        InputStream is = context.getResources().openRawResource(file);
        byte[] header = new byte[44];
        try {
            is.read(header);
            ByteBuffer wrapped = ByteBuffer.wrap(header, 24, 4).order(ByteOrder.LITTLE_ENDIAN);
            Fs = wrapped.getInt();
        } catch (IOException e) {

        }

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Fs,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffsize,
                AudioTrack.MODE_STREAM);

        try {
            byte[] sound = new byte[buffsize*2];
            audioTrack.play();
            float volume = BTClientManager.getInstance().getInstrumentalist().getModifier1();
            while ((i = is.read(sound)) != -1) {
                short[] sample = new short[buffsize];
                ByteBuffer bb = ByteBuffer.wrap(sound);
                bb.order( ByteOrder.LITTLE_ENDIAN);
                int j = 0;
                while(bb.hasRemaining()) {
                    short v = bb.getShort();
                    sample[j++] = (short)( v * volume );
                }

                Log.d("Buffer", "J: " + j + " buffer: " + buffsize + " i : " + i);
                // audioTrack.setStereoVolume(volSliderVal, volSliderVal);
                // setStereoVolume is deprecated
                audioTrack.write(sample, 0, i/2);
            }
        } catch (IOException e) {

        }

        audioTrack.stop();
        audioTrack.release();
    }

}
