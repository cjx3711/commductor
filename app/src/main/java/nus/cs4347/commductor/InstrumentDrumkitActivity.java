package nus.cs4347.commductor;

import android.bluetooth.BluetoothSocket;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;

import nus.cs4347.commductor.audioProcessor.AudioProcessor;
import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketCallback;

/**
 * Basic soundboard for an instrument.
 */

public class InstrumentDrumkitActivity extends InstrumentPreRecordedActivity {

    Button[] drumButtons;
    TextView volumeText;
    TextView bandpassText;
    TextView filterText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // Init Audio Processor
//        audioProcessor = new AudioProcessor();

        setContentView(R.layout.activity_instrument_drumkit);

        volumeText = (TextView) findViewById(R.id.text_volume);
        bandpassText = (TextView) findViewById(R.id.text_bandpass);
        filterText = (TextView) findViewById(R.id.text_filter);

        drumButtons = new Button[8];

        final String[] drumNames = {
                "Kick", "Conga", "High hat", "Cowbell", "Snare", "Tom Tom 1", "Tom Tom 2", "Tom Tom 3"
        };
        final int[] drumMap = {
                R.raw.drum_bdrum_new,
                R.raw.drum_conga_mid_new,
                R.raw.drum_hihat_close_new,
                R.raw.drum_cowbell_new,
                R.raw.drum_snare116_new,
                R.raw.drum_tom1_new,
                R.raw.drum_tom2_new,
                R.raw.drum_tom3_new,
        };

        drumButtons[0] = (Button) findViewById(R.id.button_drum_1);
        drumButtons[1] = (Button) findViewById(R.id.button_drum_2);
        drumButtons[2] = (Button) findViewById(R.id.button_drum_3);
        drumButtons[3] = (Button) findViewById(R.id.button_drum_4);
        drumButtons[4] = (Button) findViewById(R.id.button_drum_5);
        drumButtons[5] = (Button) findViewById(R.id.button_drum_6);
        drumButtons[6] = (Button) findViewById(R.id.button_drum_7);
        drumButtons[7] = (Button) findViewById(R.id.button_drum_8);

        for (int i = 0; i < 8; i++) {
            final int index = i;
            drumButtons[i].setText(drumNames[i]);

            // new code that plays from AudioTrack
            View.OnTouchListener drumTouch = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        for (int j = 0; j < 8; j++) {
                            if (v == drumButtons[j]) {
                                t = new Thread() {
                                    public void run() {
                                        try {
                                            playDrum(drumMap[index]);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
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

    private void playDrum(int file) throws IOException {
        AudioTrack audioTrack = null;

        try {
            inputStream = getResources().openRawResource(file);
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


            while ((count = inputStream.read(sound, 0, buffsize)) > -1) {
                float[] audio = AudioProcessor.byteToFloat(sound);

                float limitFreq = AudioProcessor.getLimitFreq(bandPassCoeff);
                if (bandPassCoeff > POSITIVE_FLOOR) {
                    audioProcessor.processBPAudio(audio, AudioProcessor.HIGH_PASS_FILTER, limitFreq);
                    Log.d("audioprocess", "Bandpasscoeff: " + Float.toString(bandPassCoeff));
                    Log.d("audioprocess", "Limitfreq: " + Float.toString(limitFreq));
                } else if (bandPassCoeff < NEGATIVE_CEIL) {
                    audioProcessor.processBPAudio(audio, AudioProcessor.LOW_PASS_FILTER, limitFreq);
                    Log.d("audioprocess", "Bandpasscoeff: " + Float.toString(bandPassCoeff));
                    Log.d("audioprocess", "Limitfreq: " + Float.toString(limitFreq));
                } else {
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

    public void onDestroy() {
        super.onDestroy();

        if (t != null) {
            t.interrupt();
            t = null;
        }
    }
}