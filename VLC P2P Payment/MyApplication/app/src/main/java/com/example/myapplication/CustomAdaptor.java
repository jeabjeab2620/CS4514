package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CustomAdaptor extends BaseAdapter {

    Context context;
    ArrayList<JSONObject> arr;
    LayoutInflater inflter;
    String uid;

    public CustomAdaptor(Context applicationContext, String uid, ArrayList<JSONObject>  arr) {
        this.context = context;
        this.uid = uid;
        this.arr = arr;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return arr.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
    public static String formatDecimal(double number) {
        double epsilon = 0.004f; // 4 tenths of a cent
        if (Math.abs(Math.round(number) - number) < epsilon) {
            return "$"+String.format("%,.2f", number); // sdb
        } else {
            return "$"+String.format("%,.2f", number); // dj_segfault
        }
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.transaction_history_record, null);
        TextView direction = (TextView)view.findViewById(R.id.transfer_direction);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView amount = (TextView)view.findViewById(R.id.amount);
        TextView time = (TextView)view.findViewById(R.id.time);
        TextView trans_status = (TextView)view.findViewById(R.id.trans_status);
        try {
            String sender = arr.get(i).getString("sender_uid");
            String receiver = arr.get(i).getString("receiver_uid");
            if (this.uid.equals(sender)){
                direction.setText("→");
                direction.setTextColor(Color.RED);
                name.setText(arr.get(i).getString("receiver"));
            }
            else{
                direction.setText("←");
                direction.setTextColor(Color.GREEN);
                name.setText(arr.get(i).getString("sender"));
            }
            amount.setText(formatDecimal(arr.get(i).getDouble("amount")));
            time.setText(arr.get(i).getString("time"));
            trans_status.setText(arr.get(i).getString("trans_status"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }
}
