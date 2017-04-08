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

import nus.cs4347.commductor.AppData;
import nus.cs4347.commductor.R;
import nus.cs4347.commductor.enums.InstrumentType;
import nus.cs4347.commductor.server.ServerInstrumentalist;

public class PlayerSelectFragment extends Fragment {

    public static Fragment newInstance(Context context, int pos, float scale, ServerInstrumentalist instrument) {
        Bundle b = new Bundle();
        b.putInt("pos", pos);
        b.putFloat("scale", scale);
        b.putInt("instrument", instrument.getType().getInt());
        b.putString("name", instrument.getSocket().getRemoteDevice().getName());
        return Fragment.instantiate(context, PlayerSelectFragment.class.getName(), b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        LinearLayout l = (LinearLayout)
                inflater.inflate(R.layout.fragment_server_instrumentalist, container, false);

        int pos = this.getArguments().getInt("pos");
        int instrument = this.getArguments().getInt("instrument");
        String playerName = this.getArguments().getString("name");
        InstrumentType instrumentType = InstrumentType.valueOf(instrument);

        TextView playerTV = (TextView) l.findViewById(R.id.text_player_name);
        playerTV.setText("P" + (pos +1) + ": " + playerName);

        TextView instrumentTV = (TextView) l.findViewById(R.id.text_player_instrument);
        instrumentTV.setText(instrumentType.toString() );

        ImageView instrumentImage = (ImageView) l.findViewById(R.id.image_instrument);

        AppData.getInstance().setFont(instrumentTV);
        AppData.getInstance().setFont(playerTV);

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
