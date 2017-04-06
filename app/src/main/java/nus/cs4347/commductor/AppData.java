package nus.cs4347.commductor;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import java.util.UUID;

import nus.cs4347.commductor.synthesizer.LookUpTable;

/**
 * Singleton class that stores all the global variables in the app
 */


public class AppData {
    private AppData() {

    }
    private static AppData instance = new AppData();
    public static AppData getInstance() {
        return instance;
    }

    private Context appContext;
    private BluetoothAdapter bluetoothAdapter;
    private String uuidString;
    private UUID uuid;
    private LookUpTable lut;
    private Typeface russian_font;



    public void init(Context appContext) {
        this.appContext = appContext;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        uuidString = "493a221d-a5c4-44d3-82bc-bf961702a738";
        uuid = UUID.fromString(uuidString);
        lut = new LookUpTable(8000);
        russian_font = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/Russian.ttf");
    }

    public Context getApplicationContext() {
        return appContext;
    }
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public String getUuidString() {
        return uuidString;
    }

    public UUID getUuid() {
        return uuid;
    }

    public LookUpTable getLUT () {
        return lut;
    }

    public void setFont(TextView text) {
        text.setTypeface(russian_font);
    }

    public RotateAnimation getRotateAnimation() {
        RotateAnimation r = new RotateAnimation(0, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        r.setInterpolator(new LinearInterpolator());
        r.setDuration(20000);
        r.setRepeatCount(Animation.INFINITE);
        return r;
    }
}
