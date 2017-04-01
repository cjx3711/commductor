package nus.cs4347.commductor.synthesizer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import nus.cs4347.commductor.AppData;


/**
 * Created by glutSolidSphere on 29/3/2017.
 */

public class SynthesizerThread extends Thread {
    private static final double TWOPI = 8.*Math.atan(1.);

    public int numHarmonics = 5;
    public double ratios[];

    private AudioTrack audioTrack;

    private int bufferSize;
    private short samples[];

    public int samplingRate = 8000;
    public int amplitude = 1000;
    private double currentADSRMultiplier = 0.f;
    public double ADSRAttackVelocity = 0.f;
    public double fundamentalFrequency = 440.f;
    private int wave = 0;

    private boolean isRunning = true;
    private boolean isSynthesizing = false;

    SynthesizerThread() {
        //Set the buffer size
        bufferSize = AudioTrack.getMinBufferSize (
            samplingRate,
            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        );
        //Initialize audiotrack object
        audioTrack = new AudioTrack (
            AudioManager.STREAM_MUSIC,
            samplingRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize * 2,
            AudioTrack.MODE_STREAM
        );

        samples = new short[bufferSize];

        ratios = new double [] {
            5.0/15.0, 4.0/15.0, 3.0/15.0, 2.0/15.0, 1.0/15.0
        };
    }

    public void setFundamentalFrequency (int pitch) {
        fundamentalFrequency = (pitch == 0) ? 0.f : (440.f * (Math.pow (2.f, (pitch - 69) / 12.f)));
    }

    public void startSynthesizing() {
        isSynthesizing = true;
        audioTrack.play();
    }

    public void stopSythnesizing() {
        isSynthesizing = false;
        audioTrack.flush();
        audioTrack.stop();
    }

    public void run() {
        //Set process priority
        setPriority(Thread.MAX_PRIORITY);

        //Synthesis loop
        while (isRunning) {
            while (isSynthesizing) {
                generateNote();
                audioTrack.write(samples, 0, bufferSize);
            }
        }
    }

    public void finish() {
        audioTrack.stop();
        audioTrack.release();
    }

    private void generateNote () {
        for (int i = 0; i < bufferSize; i++) {
            samples[i] = 0;
            for (int j = 1; j < numHarmonics + 1; j++) {
                samples[i] += amplitude * ratios[j-1] * AppData.getInstance().getLUT().getValAt(wave, j * fundamentalFrequency);
            }
            wave += 1;
            wave = wave % samplingRate;
        }
    }
}
