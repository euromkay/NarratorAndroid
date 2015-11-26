package voss.android.screens;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import voss.android.R;
import voss.android.setup.ActivityCreateGame;

public class ListingAdapter extends BaseAdapter{

	private ArrayList<String> data;
	private ArrayList<Integer> colors;
	private int color;

	private int layoutID;
	private Activity c;
	private Typeface font;

	public ListingAdapter(Activity c){
		this.c = c;
		this.layoutID = R.layout.create_roles_right_item;
		color = ActivityCreateGame.parseColor(c, R.color.white);
		font = Typeface.createFromAsset(c.getAssets(), "JosefinSans-Regular.ttf");
	}

	public ListingAdapter(ArrayList<String> data, Activity c){
		this(c);
		this.data = data;

	}
	public ListingAdapter(String[] data, Activity c){
		this(c);
		this.data = new ArrayList<>();
		for(String s: data)
			this.data.add(s);
	}

	public ListingAdapter setColors(ArrayList<Integer> list){
		this.colors = list;
		return this;
	}
	public void setColors(int[] inputColors){
		colors = new ArrayList<>();
		for(int i: inputColors)
			colors.add(i);
	}

	public void setColor(int color){
		this.color = color;
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

	private Float textSize;
	public void setTextSize(float textSize){
		this.textSize = textSize;
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView result;

	    if (convertView == null) {
	        result = (TextView) c.getLayoutInflater().inflate(layoutID, parent, false);
	    } else {
	        result = (TextView) convertView;
	    }

	    String item = getItem(position);
	    int viewColor;
		if (colors != null)
			viewColor = colors.get(position);
		else
			viewColor = this.color;
		result.setTypeface(font);

		if (textSize != null){
			result.setTextSize(textSize);
		}

	    result.setText(item);
	    result.setTextColor(viewColor);
	    
	    return result;
		
		
	}
	
}
