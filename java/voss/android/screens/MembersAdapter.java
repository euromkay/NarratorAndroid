package voss.android.screens;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import voss.android.R;
import voss.logic.Player;
import voss.logic.PlayerList;

public class MembersAdapter extends BaseAdapter{

    private PlayerList data;
    private Activity c;

    public MembersAdapter(PlayerList data, Activity c){
        this.data = data;
        this.c = c;
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

        int color;
        String name;
        if (p.isAlive()) {
            color = getColor(R.color.white);
            name = p.getName();
        }
        else {
            if (p.isCleaned()) {
                name = p.getName() + " - ????";
                color = getColor(R.color.trimmings);
            }
            else {
                name = p.getDescription();
                color = p.getTeam().getAlignment();
            }
        }

        result.setText(name);
        result.setTextColor(color);

        return result;


    }

    private int getColor(int id){
        return c.getResources().getColor((id));
    }
}
