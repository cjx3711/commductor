package nus.cs4347.commductor.display;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import nus.cs4347.commductor.R;
import nus.cs4347.commductor.enums.InstrumentType;


public class InstrumentSelectFragment extends Fragment {
    public static Fragment newInstance(Context context, int pos, float scale, InstrumentType instrument) {
        Bundle b = new Bundle();
        b.putInt("pos", pos);
        b.putFloat("scale", scale);
        b.putInt("instrument", instrument.getInt());
        return Fragment.instantiate(context, InstrumentSelectFragment.class.getName(), b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        LinearLayout l = (LinearLayout)
                inflater.inflate(R.layout.fragment_client_instrument, container, false);

        int pos = this.getArguments().getInt("pos");
        int instrument = this.getArguments().getInt("instrument");
        InstrumentType instrumentType = InstrumentType.valueOf(instrument);

        ImageView instrumentImage = (ImageView) l.findViewById(R.id.image_instrument);

        int imageResource = 0;
        switch ( instrumentType ) {
            case PIANO:
                imageResource = R.drawable.piano;
                break;
            case DRUMS:
                imageResource = R.drawable.drum;
                break;
            case TRIANGLE:
                imageResource = R.drawable.triangle;
                break;
            case COCONUT:
                imageResource = R.drawable.coconut_right;
                break;
        }
        instrumentImage.setImageResource(imageResource);

        ScalingLinearLayout root = (ScalingLinearLayout) l.findViewById(R.id.root);
        float scale = this.getArguments().getFloat("scale");
        root.setScaleBoth(scale);

        return l;
    }
}
