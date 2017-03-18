package nus.cs4347.commductor;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;

public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppData.getInstance().init(getApplicationContext());

        if ( !AppData.getInstance().getBluetoothAdapter().isEnabled() ) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            proceed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                proceed();
            } else if (resultCode == RESULT_CANCELED) {
                new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme))
                    .setTitle("Bluetooth Required")
                    .setMessage("Bluetooth could not be enabled. It is required for this application to run.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
            }
        }

    }

    private void proceed() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }
}