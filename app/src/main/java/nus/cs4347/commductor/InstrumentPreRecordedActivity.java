package nus.cs4347.commductor;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import nus.cs4347.commductor.audioProcessor.AudioProcessor;
import nus.cs4347.commductor.bluetooth.BTClientManager;

/**
 * Created by haritha on 4/4/17.
 */

public class InstrumentPreRecordedActivity extends AppCompatActivity {

    AudioProcessor audioProcessor;
    InputStream inputStream;

    float volumeCoeff;
    float bandPassCoeff;
    boolean canPlay = true;

    protected final float POSITIVE_FLOOR = 10.0f;
    protected final float NEGATIVE_CEIL = -10.0f;

    Thread t;

    public void updateText(TextView volumeText, TextView bandpassText, TextView filterText ) {
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

    protected void playSound(int file) throws IOException {
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

            while (canPlay && (count = inputStream.read(sound, 0, buffsize)) > -1) {
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
                Log.e("byte count", String.valueOf(count / 2));
                Log.e("byte-end", "=======================");

                audioTrack.write(shordio, 0, count / 2);

            }

        } catch (IOException e) {

        }
        audioTrack.stop();
        audioTrack.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init Audio Processor
        audioProcessor = new AudioProcessor();
    }
}
