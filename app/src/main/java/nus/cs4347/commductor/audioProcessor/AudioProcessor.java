package nus.cs4347.commductor.audioProcessor;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nus.cs4347.commductor_minim.ddf.minim.effects.LowPassSP;
import nus.cs4347.commductor_minim.ddf.minim.effects.HighPassSP;

/**
 * Created by Jonathan on 4/3/17.
 */


// Note:  Piano: 261.6Hz to 1046.5Hz. Triangle: 1174.6Hz

public class AudioProcessor {

    final int HEADER_SIZE = 44;
    final static float MAX_FREQ = 10000f;
    final static float MIN_FREQ = 500f;

    public final static int LOW_PASS_FILTER = 0;
    public final static int HIGH_PASS_FILTER = 1;

    private int format;
    private int channels;
    private int sampleRate;
    private int bitsPerSample;
    private int dataSize;
    private InputStream inputStream;
    private LowPassSP lowPassFilter;
    private HighPassSP highPassFilter;

    public AudioProcessor(){}

    public void setInputStream(InputStream is){
        this.inputStream = is;
    }

    public int getSampleRate(){ return this.sampleRate; }

    public void processAudio(float[] audio, int filterType, float limitFreq){
        if(filterType == LOW_PASS_FILTER){
            lowPassFilter = new LowPassSP(limitFreq, this.sampleRate);
            lowPassFilter.process(audio);

        } else if(filterType == HIGH_PASS_FILTER){
            highPassFilter = new HighPassSP(limitFreq, this.sampleRate);
            highPassFilter.process(audio);
        } else { return; }
    }
    public void updateHeaderData() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        inputStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

        buffer.rewind();
        buffer.position(buffer.position() + 20);
        this.format = buffer.getShort();

        this.channels = buffer.getShort();

        this.sampleRate = buffer.getInt();

        buffer.position(buffer.position() + 6);
        this.bitsPerSample = buffer.getShort();

        this.dataSize = 0;

        // not really important I think, but I"m leaving it here
//        while (buffer.getInt() != 0x61746164) { // "data" marker
//            int size = buffer.getInt();
//            inputStream.skip(size);
//
//            buffer.rewind();
//            inputStream.read(buffer.array(), buffer.arrayOffset(), 8);
//            buffer.rewind();
//        }

        this.dataSize = buffer.getInt();
        Log.e("byte - format", String.valueOf(format));
        Log.e("byte - channel", String.valueOf(channels));
        Log.e("byte - sample rate", String.valueOf(sampleRate));
        Log.d("sample rate", String.valueOf(sampleRate));
        Log.e("byte - bps", String.valueOf(bitsPerSample));
        Log.e("byte - data size", String.valueOf(dataSize));
    }

    // Gives the limit freq for a filter.
    public static float getLimitFreq(float bandPassCoeff){
        if(bandPassCoeff == 50.0){
            return MAX_FREQ;
        }
        if(bandPassCoeff == -50.0){
            return MIN_FREQ;
        }

        double min = Math.log10(MIN_FREQ);
        double max = Math.log10(MAX_FREQ);
        double range = max - min;
        double frac;
        double level;
        double freq;

        if(bandPassCoeff > 0){
            // Positive: Return a lower limit for High-pass filter
            frac = Math.abs(bandPassCoeff) / 50.0 * range;
            level = frac + min;
            freq = Math.pow(10, level);

        } else {
            // Negative: Return an upper limit for Low-pass filter
            frac = Math.abs(bandPassCoeff) / 50.0 * range;
            level = max - frac;
            freq = Math.pow(10, level);
        }
        return (float) freq;
    }

    public static float[] byteToFloat(byte[] audio) {
        return shortToFloat(byteToShort(audio));
    }

    public static short[] floatToShort(float[] buffer) {
        short[] shorts = new short[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            shorts[i] = (short) buffer[i];
        }
        return shorts;
    }
    public static short[] byteToShort (byte[] byteArray){
        short[] shortOut = new short[byteArray.length / 2];
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortOut);
        return shortOut;
    }
    public static float[] shortToFloat (short[] shortArray){
        float[] floatOut = new float[shortArray.length];
        for (int i = 0; i < shortArray.length; i++) {
            floatOut[i] = shortArray[i];
        }
        return floatOut;
    }
}


// I've found some methods that were slightly cleaner.

//    private int[] byteToShort(byte[] rawdata) {
//        int[] converted = new int[rawdata.length / 2];
//
//        for (int i = 0; i < converted.length; i++) {
//            // Wave file data are stored in little-endian order
//            int lo = rawdata[2*i];
//            int hi = rawdata[2*i+1];
//            converted[i] = ((hi&0xFF)<<8) | (lo&0xFF);
//        }
//        return converted;
//    }


//    /**
//     * Convert int[] audio to 32 bit float format.
//     * From [-32768,32768] to [-1,1]
//     * @param audio
//     */
//    private float[] shortToFloat(short[] audio) {
//        Log.e("short byte", Arrays.toString(audio));
//        float[] converted = new float[audio.length];
//
//        for (int i = 0; i < converted.length; i++) {
//            // [-32768,32768] -> [-1,1]
//            converted[i] = audio[i] / 32768f; /* default range for Android PCM audio buffers) */
//            converted[i] = (float)audio[i]; /* default range for Android PCM audio buffers) */
//
//        }
//
//        return converted;
//    }
