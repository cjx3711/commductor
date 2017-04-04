package nus.cs4347.commductor;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.InputStream;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init Audio Processor
        audioProcessor = new AudioProcessor();
    }
}
