package nus.cs4347.commductor;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import nus.cs4347.commductor.bluetooth.BTDataPacket;
import nus.cs4347.commductor.bluetooth.BTPacketHeader;
import nus.cs4347.commductor.bluetooth.BTServerManager;
import nus.cs4347.commductor.display.PlayerPagerAdapter;
import nus.cs4347.commductor.server.ServerInstrumentalist;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ConductorActivity extends AppCompatActivity {
    private final String TAG = "ConductorActivity";

    PlayerPagerAdapter playersPagerAdapter;
    ViewPager playersPager;

    Button sendMessageButton;

    ServerInstrumentalist selectedInstrumentalist;
    ArrayList<ServerInstrumentalist> serverInstrumentalists;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conductor);

        serverInstrumentalists = BTServerManager.getInstance().getInstrumentalistList();

        // Set up the pager stuff
        playersPager = (ViewPager) findViewById(R.id.myviewpager);
        playersPagerAdapter = new PlayerPagerAdapter(this, this.getSupportFragmentManager(), BTServerManager.getInstance().getInstrumentalistList());
        playersPager.setAdapter(playersPagerAdapter);
        playersPager.setPageTransformer(false, playersPagerAdapter);

        playersPager.setCurrentItem(playersPagerAdapter.getFirstPage());
        playersPager.setOffscreenPageLimit(3);
        playersPager.setPageMargin(100);

        playersPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int selectedIndex = position % serverInstrumentalists.size();
                selectedInstrumentalist = serverInstrumentalists.get(selectedIndex);
                Log.d(TAG, "Selected:" + selectedIndex);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        sendMessageButton = (Button)findViewById(R.id.button_send_message);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( selectedInstrumentalist != null ) {
                    BTDataPacket packet = new BTDataPacket(BTPacketHeader.STRING_DATA);
                    packet.stringData = "I choose you!";
                    selectedInstrumentalist.getService().write(packet);
                }
            }
        });

    }
}
