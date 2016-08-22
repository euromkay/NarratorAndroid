package android.day;

import json.JSONArray;
import json.JSONException;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import voss.narrator.R;


public class PlayerDrawerAdapter extends RecyclerView.Adapter<PlayerDrawerAdapter.ViewHolder> {
    private JSONArray mDataset;
    private OnPlayerClickListener mListener;

    /**
     * Interface for receiving click events from cells.
     */
    public interface OnPlayerClickListener {
        void onPlayerClick(String name);
        int getMyColor(int id);
        void onExitAttempt();
    }

    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTextView;

        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    public PlayerDrawerAdapter(JSONArray myDataset, OnPlayerClickListener listener) {
        mDataset = myDataset;
        mListener = listener;
        setHasStableIds(false);
    }

	

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = vi.inflate(R.layout.player_item, parent, false);
        TextView tv = (TextView) v.findViewById(android.R.id.text1);
        return new ViewHolder(tv);
    }

    TextView prev;

    public void onBindViewHolder(ViewHolder holder, final int position) {
        final TextView tv = holder.mTextView;
        if (position == 0){
            tv.setText(DayScreenController.PlayerMenuHeader);

            prev = tv;
            tv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    mListener.onPlayerClick(null);
                    if (prev != null)
                        prev.setTextColor(mListener.getMyColor(R.color.white));
                    tv.setTextColor(mListener.getMyColor(R.color.yellow));
                    prev = tv;
                }
            });
            tv.setTextColor(mListener.getMyColor(R.color.yellow));
            return;
        }

        if (position == mDataset.length() + 1){
            tv.setText("Close Game");
            tv.setTextColor(mListener.getMyColor(R.color.redBlood));
            tv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    mListener.onExitAttempt();
                }
            });
            return;
        }

		try {
			final String name = mDataset.getString(position - 1);
			tv.setText(name);
	        tv.setTextColor(mListener.getMyColor(R.color.white));
	        tv.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	                mListener.onPlayerClick(name);
	                if (prev != null)
	                    prev.setTextColor(mListener.getMyColor(R.color.white));
	                tv.setTextColor(mListener.getMyColor(R.color.yellow));
	                prev = tv;
	            }
	        });
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
    }


    public int getItemCount() {
        return mDataset.length() + 2;
    }
    
    public JSONArray getPlayersInView(){
    	return mDataset;
    }
}
