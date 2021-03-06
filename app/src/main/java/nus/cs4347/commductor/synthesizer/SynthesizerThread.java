package nus.cs4347.commductor.synthesizer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import nus.cs4347.commductor.AppData;
import nus.cs4347.commductor.bluetooth.BTClientManager;


/**
 * Created by glutSolidSphere on 29/3/2017.
 */

public class SynthesizerThread extends Thread {
    private static final double TWOPI = 8.*Math.atan(1.);
    private static final int ADSR_ATTACK = 0;
    private static final int ADSR_DECAY = 1;
    private static final int ADSR_SUSTAIN = 2;
    private static final int ADSR_RELEASE = 3;
    private static final int ADSR_END = 4;

    private int currentADSRState = ADSR_ATTACK;

    public int numHarmonics = 5;
    public double weights[];

    private AudioTrack audioTrack;

    private int bufferSize;
    private short samples[];

    public int samplingRate = 16000;
    private int samplesPerMs = samplingRate / 1000;
    private int baseAmplitude = 10000;
    public int amplitude = baseAmplitude;

    private double currentADSRMultiplier = 0.f;
    public double ADSRSustainValue = 0.7f;
    public double ADSRAttackVelocity = 1.f / (10.f * samplesPerMs);
    public double ADSRDecayVelocity = -(1.f - ADSRSustainValue) / (10.f * samplesPerMs);
    public double ADSRReleaseVelocity = -(ADSRSustainValue) / (10.f * samplesPerMs);

    public double fundamentalFrequency = 440.f;
    private double wave[];

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

        weights = new double [] {
            5.0, 4.0, 3.0, 2.0, 1.0
        };
        wave = new double [] {
          0.0, 0.0, 0.0, 0.0, 0.0
        };

        audioTrack.play();
    }

    public void setFundamentalFrequency (int pitch) {
        fundamentalFrequency = (pitch == 0) ? 0.f : (440.f * (Math.pow (2.f, (pitch - 69) / 12.f)));
    }

    public void startSynthesizing() {
        isSynthesizing = true;
        currentADSRState = ADSR_ATTACK;
    }

    public void stopSynthesizing() {
        currentADSRState = ADSR_RELEASE;
    }

    public void run() {
        //Set process priority
        setPriority(Thread.MAX_PRIORITY);

        //Synthesis loop
        while (isRunning) {
            while (isSynthesizing) {
                generateNote();
                audioTrack.write(samples, 0, bufferSize);
                if (currentADSRState == ADSR_END) {
                    isSynthesizing = false;
                }
            }
        }
    }

    public void finish() {
        audioTrack.stop();
        audioTrack.release();
        isRunning = false;
    }

    private void generateNote () {
        float modifier = BTClientManager.getInstance().getInstrumentalist().getModifier2();
        float gradient = numHarmonics - (modifier * 4.f);
        double weightSum = 0;
        for ( int j = 0; j < numHarmonics; j++ ) {
            weights[j] = (-gradient * (j) + numHarmonics) < 0 ? 0 : (-gradient * (j) + numHarmonics);
            weightSum += weights[j];
        }

        amplitude = (int) (BTClientManager.getInstance().getInstrumentalist().getModifier1() * baseAmplitude);

        for (int i = 0; i < bufferSize; i++) {
            switch (currentADSRState) {
                case ADSR_ATTACK:
                    currentADSRMultiplier += ADSRAttackVelocity;
                    if (currentADSRMultiplier >= 1.f) {
                        currentADSRState = ADSR_DECAY;
                    }
                    break;

                case ADSR_DECAY:
                    currentADSRMultiplier += ADSRDecayVelocity;
                    if (currentADSRMultiplier <= ADSRSustainValue) {
                        currentADSRState = ADSR_SUSTAIN;
                    }
                    break;

                case ADSR_SUSTAIN:
                    break;

                case ADSR_RELEASE:
                    currentADSRMultiplier += ADSRReleaseVelocity;
                    if (currentADSRMultiplier <= 0.f) {
                        currentADSRMultiplier = 0.f;
                        currentADSRState = ADSR_END;
                    }
                    break;

                default:
                    break;
            }
            samples[i] = 0;
            for (int j = 1; j < numHarmonics + 1; j++) {
                double weight = weights[j-1] / weightSum;
                samples[i] += currentADSRMultiplier * amplitude * weight * Math.sin (wave[j -1]);
                wave[j - 1] += (TWOPI * j * fundamentalFrequency / samplingRate);
                wave[j - 1] = wave[j - 1] % TWOPI;
            }
        }
    }
}
