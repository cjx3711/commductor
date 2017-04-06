package nus.cs4347.commductor.display;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import nus.cs4347.commductor.AppData;
import nus.cs4347.commductor.R;
import nus.cs4347.commductor.enums.InstrumentType;
import nus.cs4347.commductor.server.ServerInstrumentalist;


public class PlayerAdapter extends ArrayAdapter<ServerInstrumentalist> {
    public PlayerAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public PlayerAdapter(Context context, int resource, List<ServerInstrumentalist> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listitem_player, null);
        }

        ServerInstrumentalist p = getItem(position);

        if (p != null) {
            ImageView image = (ImageView) v.findViewById(R.id.image_instrument);
            TextView player = (TextView) v.findViewById(R.id.text_player);
            TextView name = (TextView) v.findViewById(R.id.text_name);

            int imageResource = 0;
            InstrumentType type = p.getType();
            if ( type != null ) {
                switch (type) {
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
            }
            image.setImageResource(imageResource);

            player.setText("Player " + (position + 1));
            name.setText(p.getSocket().getRemoteDevice().getName() + " - " + p.getSocket().getRemoteDevice().getAddress());
            AppData.getInstance().setFont(player);
            AppData.getInstance().setFont(name);
        }

        return v;
    }

};
