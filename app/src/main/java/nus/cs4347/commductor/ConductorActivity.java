package nus.cs4347.commductor;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import nus.cs4347.commductor.bluetooth.BTServerManager;
import nus.cs4347.commductor.display.PlayerPagerAdapter;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ConductorActivity extends AppCompatActivity {
    private final String TAG = "ConductorActivity";

    PlayerPagerAdapter playersPagerAdapter;
    ViewPager playersPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conductor);

        // Set up the pager stuff
        playersPager = (ViewPager) findViewById(R.id.myviewpager);
        playersPagerAdapter = new PlayerPagerAdapter(this, this.getSupportFragmentManager(), BTServerManager.getInstance().getInstrumentalistList());
        playersPager.setAdapter(playersPagerAdapter);
        playersPager.setPageTransformer(false, playersPagerAdapter);

        playersPager.setCurrentItem(playersPagerAdapter.getFirstPage());
        playersPager.setOffscreenPageLimit(3);
        playersPager.setPageMargin(100);

    }
}
