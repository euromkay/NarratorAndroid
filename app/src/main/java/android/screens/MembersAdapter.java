package android.screens;

import android.JUtils;
import android.NActivity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.texting.StateObject;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import json.JSONArray;
import json.JSONObject;
import voss.narrator.R;

public class MembersAdapter extends BaseAdapter{

    private JSONArray data;
    private NActivity c;
    private Typeface font;

    public MembersAdapter(JSONArray data, NActivity c){
        this.data = data;
        this.c = c;
        font = Typeface.createFromAsset(c.getAssets(), "JosefinSans-Regular.ttf");
    }

    public int getCount() {
        return data.length();
    }

    public String getItem(int position) {
        JSONObject jPlayer = JUtils.getJSONObject(data, position);
        return JUtils.getString(jPlayer, StateObject.playerName);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        TextView result;

        if (convertView == null) {
            result = (TextView) c.getLayoutInflater().inflate(R.layout.create_roles_right_item, parent, false);
        } else {
            result = (TextView) convertView;
        }

        JSONObject jPlayer = JUtils.getJSONObject(data, position);


        String color = JUtils.getString(jPlayer, StateObject.playerColor);
        String name = JUtils.getString(jPlayer, StateObject.playerDescription);

        result.setText(name);
        result.setTypeface(font);
        result.setTextColor(Color.parseColor(color));

        return result;


    }
}
