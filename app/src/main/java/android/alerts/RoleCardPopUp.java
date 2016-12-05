package android.alerts;


import java.util.List;

import android.NActivity;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

    class ImageSetterTask extends AsyncTask<String, Void, Integer> {
        ImageView bmImage;
        Context ac;

        public ImageSetterTask(ImageView bmImage, Context ac) {
            this.bmImage = bmImage;
            this.ac = ac;
        }

        protected Integer doInBackground(String... urls) {
            String urldisplay = urls[0].toLowerCase();
            Bitmap mIcon11 = null;
            Integer drawableResourceId;
            try {
                drawableResourceId = ac.getResources().getIdentifier("cit", "drawable", ac.getPackageName());
                //drawableResourceId = 0;
               // bmImage.setImage
                //mIcon11 = BitmapFactory.decodeResource(ac.getResources(), drawableResourceId);
            } catch (Exception e) {
                drawableResourceId = 0;
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return drawableResourceId;
        }

        protected void onPostExecute(Integer result) {
            bmImage.setImageResource(result);
        }
    }

    class RoleCardAdapter extends BaseAdapter{

        String activeCard;
        private List<Member> members;
        private LayoutInflater inflater;
        private Context c;
        public RoleCardAdapter(Context c, List<Member> members) {
            this.members = members;
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.c = c;
        }

        private void collapse(View v){
            TextView tv = (TextView) v.findViewById(R.id.role_card_title);
            tv.setTextSize((float)(tv.getTextSize() / 2));

            v.findViewById(R.id.role_card_info).setVisibility(View.GONE);
        }

        private void expand(View v){
            TextView tv = (TextView) v.findViewById(R.id.role_card_title);
            tv.setTextSize((float)(tv.getTextSize() * 2));

            v.findViewById(R.id.role_card_info).setVisibility(View.VISIBLE);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            Member m = members.get(position);
            View roleView = inflater.inflate(R.layout.general_role_card, null);

            roleView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    TextView tv = (TextView) view.findViewById(R.id.role_card_title);
                    String text = tv.getText().toString();
                    if (text.equals(activeCard))
                        activeCard = null;
                    else
                        activeCard = text;

                    RoleCardAdapter.this.notifyDataSetChanged();
                }
            });

            TextView tv = (TextView) roleView.findViewById(R.id.role_card_title);
            tv.setText(m.getName());
            if(m.getName().equals(activeCard)) {
                tv.setTextSize(tv.getTextSize() * 1.2f);
            }
            NActivity.setTextColor(tv, m.getColor());

            TextView tv2 = (TextView) roleView.findViewById(R.id.role_card_info);
            tv2.setText(m.getDescription());
            if(!m.getName().equals(activeCard))
                tv2.setVisibility(View.GONE);

            ImageView iv = (ImageView) roleView.findViewById(R.id.role_card_picture);
            if(m.getName().equals(activeCard)){
                tv2.setVisibility(View.VISIBLE);
                loadImage(m.getName(), iv);
            }else{
                tv2.setVisibility(View.GONE);
            }

            return roleView;
        }

        private void loadImage(String name, ImageView iv){
            new ImageSetterTask(iv, this.c).execute(name);
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
