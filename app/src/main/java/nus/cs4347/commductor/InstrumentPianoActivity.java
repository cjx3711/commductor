package nus.cs4347.commductor;

import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import nus.cs4347.commductor.bluetooth.BTClientManager;
import nus.cs4347.commductor.display.Piano;
import nus.cs4347.commductor.synthesizer.SynthThreadManager;

public class InstrumentPianoActivity extends AppCompatActivity {
    private static final String TAG = "InstrumentPianoActivity";
    Piano piano;

    private SynthThreadManager synthThreadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrument_piano);

        synthThreadManager = SynthThreadManager.getInstance();
        synthThreadManager.init();

        Log.d("Lifecycle", "Creating");

        piano = (Piano) findViewById(R.id.view_piano);
        piano.setPianoKeyListener(new Piano.PianoKeyListener() {
            @Override
            public void keyPressed(int id, int action) {
                if (action == MotionEvent.ACTION_UP) {
                    synthThreadManager.stopNote (id);
                    return;
                }
                Log.d(TAG, id + " " + action);
                synthThreadManager.playNote(id);

                Log.d ( TAG, "Piano Activity Volume Modulator: " + BTClientManager.getInstance().getInstrumentalist().getModifier1() );
            }
        });
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        Log.d("Lifecycle", "Starting");
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        Log.d("Lifecycle", "Stopping");
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d("Lifecycle", "Resuming");
//    }
//
//    @Override
//    public void onRestart() {
//        super.onRestart();
//        Log.d("Lifecycle", "Restarting");
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", "Destroying");
        synthThreadManager.destroy();
    }

}
