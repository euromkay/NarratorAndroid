package android.screens;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import voss.narrator.R;
import android.setup.ActivityCreateGame;

public class ListingAdapter extends BaseAdapter{

	private ArrayList<String> colors;
	private String color;

	private ArrayList<String> data;
	private int layoutID;
	private Activity c;
	private Typeface font;
	private int length;

	public ListingAdapter(Activity c){
		this.c = c;
		this.layoutID = R.layout.create_roles_right_item;
		color = "#FFFFFF";//ActivityCreateGame.ParseColor(c, R.color.white);
		font = Typeface.createFromAsset(c.getAssets(), "JosefinSans-Regular.ttf");
	}

	public ListingAdapter(ArrayList<String> data, Activity c){
		this(c);
		this.data = data;
		length = data.size();
	}
	public ListingAdapter(String[] data, Activity c){
		this(c);
		this.data = new ArrayList<>();
		for(String s: data)
			this.data.add(s);

		length = data.length;
	}

	public ListingAdapter setColors(ArrayList<String> list){
		this.colors = list;
		return this;
	}
	public void setColors(String[] inputColors){
		colors = new ArrayList<>();
		for(String i: inputColors)
			colors.add(i);
	}

	public void setLimit(int limit){
		if(limit < length)
			length = limit;
	}

	public void setColor(String color){
		this.color = color;
	}

	public void setLayoutID(int id){
		layoutID = id;
	}

	public int getCount() {
		return length;
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
		String viewColor;
		if (colors != null)
			viewColor = colors.get(position);
		else
			viewColor = this.color;
		result.setTypeface(font);




		if (textSize != null){
			result.setTextSize(textSize);
		}

	    result.setText(item);
	    result.setTextColor(Integer.parseInt(viewColor.substring(1)));
	    
	    return result;
		
		
	}
	
}
