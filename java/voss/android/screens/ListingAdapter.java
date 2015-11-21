package voss.android.screens;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import voss.android.R;

public class ListingAdapter extends BaseAdapter{

	private ArrayList<String> data;
	private ArrayList<Integer> colors;
	private int layoutID;
	private Activity c;
	private Typeface font;
	
	public ListingAdapter(ArrayList<String> data, ArrayList<Integer> colors, Activity c){
		this.data = data;
		this.colors = colors;
		this.layoutID = R.layout.create_roles_right_item;
		this.c = c;
		font = Typeface.createFromAsset(c.getAssets(), "JosefinSans-Regular.ttf");
	}
	public ListingAdapter(String[] data, int[] colors, Activity c){
		this.data = new ArrayList<>();
		this.colors = new ArrayList<>();
		for(String s: data)
			this.data.add(s);
		for(int i: colors)
			this.colors.add(i);
		this.c = c;
		this.layoutID = R.layout.create_roles_right_item;
		font = Typeface.createFromAsset(c.getAssets(), "JosefinSans-Regular.ttf");
	}

	public void setLayoutID(int id){
		layoutID = id;
	}

	public int getCount() {
		return data.size();
	}

	public String getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		TextView result;

	    if (convertView == null) {
	        result = (TextView) c.getLayoutInflater().inflate(layoutID, parent, false);
	    } else {
	        result = (TextView) convertView;
	    }

	    String item = getItem(position);
	    int color = colors.get(position);
		result.setTypeface(font);

	    result.setText(item);
	    result.setTextColor(color);
	    
	    return result;
		
		
	}
	
}
