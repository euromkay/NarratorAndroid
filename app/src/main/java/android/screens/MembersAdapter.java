package android.screens;

import android.NActivity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import shared.logic.Player;
import shared.logic.PlayerList;
import voss.narrator.R;

public class MembersAdapter extends BaseAdapter{

    private PlayerList data;
    private NActivity c;
    private Typeface font;

    public MembersAdapter(PlayerList data, NActivity c){
        this.data = data;
        this.c = c;
        font = Typeface.createFromAsset(c.getAssets(), "JosefinSans-Regular.ttf");
    }

    public int getCount() {
        return data.size();
    }

    public String getItem(int position) {
        return data.get(position).getName();
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

        Player p = data.get(position);

        String color;
        String name;
        if (p.isAlive()) {
            color = "#000000";
            name = p.getName();
        }
        else {
            if (p.isCleaned()) {
                name = p.getName() + " - ????";
                color = "#49C500";//getColor(R.color.trimmings);
            }
            else {
                name = p.getDescription();
                color = p.getTeam().getColor();
            }
        }

        result.setText(name);
        result.setTypeface(font);
        result.setTextColor(Color.parseColor(color));

        return result;


    }
}
