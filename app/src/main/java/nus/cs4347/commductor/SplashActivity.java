package nus.cs4347.commductor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppData.getInstance().init(getApplicationContext());

        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }
}