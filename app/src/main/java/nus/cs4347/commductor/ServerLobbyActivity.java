package nus.cs4347.commductor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class ServerLobbyActivity extends AppCompatActivity {

    Button startButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_lobby);

        startButton = (Button) findViewById(R.id.button_start_game);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ConductorActivity.class);
                startActivity(intent);
            }
        });

    }
}
