package android.alerts;


import java.util.HashMap;
import java.util.List;

import android.NActivity;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import shared.logic.Member;
import voss.narrator.R;

public class RoleCardPopUp extends DialogFragment {

    public View mainView;
    public List<Member> members;
    private NActivity activity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.role_card_layout, container);

        getDialog().setTitle("Role Emporium");

        populateMembers();

        return mainView;
    }

    public void populateMembers(){
        if(mainView == null || members == null)
            return;
        RoleCardAdapter rca = new RoleCardAdapter(activity, members);
        ListView lv = (ListView) mainView.findViewById(R.id.role_card_layout_lv);
        lv.setAdapter(rca);
    }

    public void onAttach(Context c){
        setMembers((NActivity) c);
        super.onAttach(c);
    }
    public void onAttach(Activity a){
        setMembers((NActivity) a);
        super.onAttach(a);
    }

    private void setMembers(NActivity na){
        activity = na;
        members = na.setMembers();
        populateMembers();
    }

    class RoleCardAdapter extends BaseAdapter{

        String activeCard;
        private List<Member> members;
        private LayoutInflater inflater;
        private Context c;
        private HashMap<String, String> clickedToBase;
        RoleCardAdapter(Context c, List<Member> members) {
            this.members = members;
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.c = c;
            clickedToBase = new HashMap<>();

            for(Member m : members){
                if(!m.getName().equals(m.getBaseName())){
                    clickedToBase.put(m.getName(), m.getBaseName());
                }
            }
        }



        public View getView(int position, View convertView, ViewGroup parent){
            Member m = members.get(position);
            View roleView = inflater.inflate(R.layout.general_role_card, null);

            roleView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    TextView tv = (TextView) view.findViewById(R.id.role_card_title);
                    String text = tv.getText().toString();
                    if(clickedToBase.containsKey(text))
                        text = clickedToBase.get(text);
                    if (text.equals(activeCard))
                        activeCard = null;
                    else
                        activeCard = text;

                    RoleCardAdapter.this.notifyDataSetChanged();
                }
            });

            boolean activeCard = m.getBaseName().equals(this.activeCard);

            TextView tv = (TextView) roleView.findViewById(R.id.role_card_title);
            tv.setText(m.getName());
            NActivity.SetFont(tv, c, true);
            if(activeCard) {
                tv.setTextSize(tv.getTextSize() * 1.05f);
            }
            NActivity.setTextColor(tv, m.getColor());

            TextView tv2 = (TextView) roleView.findViewById(R.id.role_card_info);
            tv2.setText(m.getDescription());
            NActivity.SetFont(tv, c, false);

            ImageView iv = (ImageView) roleView.findViewById(R.id.role_card_picture);
            if(activeCard){
                iv.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.VISIBLE);
                loadImage(m.getBaseName(), iv);
            }else{
                iv.setVisibility(View.GONE);
                tv2.setVisibility(View.GONE);
            }

            return roleView;
        }

        private void loadImage(String name, ImageView iv){
            String urldisplay = name.toLowerCase().replaceAll(" ", "");
            Integer drawableResourceId;

            drawableResourceId = c.getResources().getIdentifier(urldisplay, "drawable", c.getPackageName());
            if(drawableResourceId == 0)
                drawableResourceId = c.getResources().getIdentifier("citizen", "drawable", c.getPackageName());
            iv.setImageResource(drawableResourceId);
        }

        public int getCount() {
            return members.size();
        }

        public Object getItem(int position){
            return members.get(position);
        }

        public long getItemId(int position){
            return members.get(position).hashCode();
        }
    }

    public void onDismiss(final DialogInterface arg0) {
        activity.roleCardPopUp = null;
        super.onDismiss(arg0);
    }
}
