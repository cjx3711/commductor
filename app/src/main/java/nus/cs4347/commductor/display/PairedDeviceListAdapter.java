package nus.cs4347.commductor.display;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import nus.cs4347.commductor.AppData;
import nus.cs4347.commductor.R;

public class PairedDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
    public PairedDeviceListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public PairedDeviceListAdapter(Context context, int resource, List<BluetoothDevice> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listitem_paireddevice, null);
        }

        BluetoothDevice p = getItem(position);

        if (p != null) {
            TextView name = (TextView) v.findViewById(R.id.text_name);
            AppData.getInstance().setFont(name);

            name.setText(p.getName() + " " + p.getAddress());
        }

        return v;
    }

};
