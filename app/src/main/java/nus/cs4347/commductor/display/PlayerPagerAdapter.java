package nus.cs4347.commductor.display;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;

import nus.cs4347.commductor.R;
import nus.cs4347.commductor.server.ServerInstrumentalist;

/**
 * Adapter for the view pager for the players
 */

public class PlayerPagerAdapter extends FragmentPagerAdapter implements ViewPager.PageTransformer {
    public final static float BIG_SCALE = 1.0f;
    public final static float SMALL_SCALE = 0.7f;
    public final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;

    private ScalingLinearLayout cur = null;
    private ScalingLinearLayout next = null;
    private Context context;
    private FragmentManager fm;
    private float scale;

    private int pages;
    private int loops = 1000;
    private int firstPage;
    private ArrayList<ServerInstrumentalist> serverInstrumentalists;
    public PlayerPagerAdapter(Context context, FragmentManager fm, ArrayList<ServerInstrumentalist> serverInstrumentalists) {
        super(fm);
        this.fm = fm;
        this.context = context;

        this.serverInstrumentalists = serverInstrumentalists;
        this.pages = serverInstrumentalists.size();
        this.firstPage = pages * loops / 2;
    }

    @Override
    public Fragment getItem(int position) {
        // make the first pager bigger than others
        if (position == firstPage)
            scale = BIG_SCALE;
        else
            scale = SMALL_SCALE;

        position = position % pages;
        ServerInstrumentalist si = serverInstrumentalists.get(position);
        return PlayerSelectFragment.newInstance(context, position, scale, si.getType());
    }

    @Override
    public int getCount() {
        return pages * loops;
    }

    public int getFirstPage() {
        return firstPage;
    }

    @Override
    public void transformPage(View page, float position) {
        ScalingLinearLayout scalingLinearLayout = (ScalingLinearLayout) page.findViewById(R.id.root);
        float scale = BIG_SCALE;
        if (position > 0) {
            scale = scale - position * DIFF_SCALE;
        } else {
            scale = scale + position * DIFF_SCALE;
        }
        if (scale < 0) scale = 0;
        scalingLinearLayout.setScaleBoth(scale);
    }
}
