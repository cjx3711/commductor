package nus.cs4347.commductor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import nus.cs4347.commductor.display.Piano;

public class InstrumentPianoActivity extends AppCompatActivity {
    private static final String TAG = "InstrumentPianoActivity";
    Piano piano;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_piano);

        piano = (Piano) findViewById(R.id.view_piano);
        piano.setPianoKeyListener(new Piano.PianoKeyListener() {
            @Override
            public void keyPressed(int id, int action) {
                Log.d(TAG, id + " " + action);
            }
        });
    }
}
