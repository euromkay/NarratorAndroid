package android.day;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import shared.roles.Driver;
import shared.roles.Witch;
import voss.narrator.R;

public class TargetablesAdapter extends BaseAdapter implements OnItemClickListener, OnCheckedChangeListener{

    DayManager manager;
    private static LayoutInflater inflater;
    public ArrayList<String> targetables;
    private HashMap<CompoundButton, String> buttonToPlayer;
    public  ArrayList<ClickAction> clicked;
    public HashMap<String, ArrayList<Integer>> selected;
    
    //private static final String[] doubleCommands = {Witch.Control};
	public static final int MAX = 3;
    
    public TargetablesAdapter(DayManager manager, ArrayList<String> targetables, HashMap<String, ArrayList<Integer>> selected){
        this.manager = manager;
        this.targetables = targetables;
    
        inflater = (LayoutInflater) manager.dScreenController.dScreen
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        buttonToPlayer = new HashMap<>();
        clicked = new ArrayList<>();   
        this.selected = selected;
    }
    
    private void setRow(String name, View v){
		ArrayList<Integer> list = selected.get(name);
		if(list == null)
			throw new NullPointerException();
		CheckBox cb;
    	for(int column = 0; column < MAX; column ++){
	    	cb = getCheckBox(column, v);
			cb.setOnCheckedChangeListener(null);
			cb.setChecked(list.contains(column));
			cb.setOnCheckedChangeListener(this);
		}
    }
    
    public int getCount(){
        return targetables.size();
    }

    public View getView(int position, View vi, ViewGroup parent){
        if(vi == null)
            vi = inflater.inflate(R.layout.action_target_view, null);
        String name = targetables.get(position);

        String displayedName = name;
        TextView tv = (TextView) vi.findViewById(R.id.target_name);
        if(manager.ns.isDay()){
        	if(displayedName.equals("Skip Day")){
        		displayedName = manager.ns.getSkipVotes() + " - " + displayedName;
        	}else{
        		displayedName = manager.ns.getVoteCount(displayedName) + " - " + displayedName;
        	}
        }
        tv.setText(displayedName);

        
        
        for(int i = 0; i < MAX; i++){
        	CheckBox cb = getCheckBox(i, vi);
			cb.setOnCheckedChangeListener(this);
        	buttonToPlayer.put(cb, name);
        	
        	if(i == 0 && manager.getCurrentPlayer() != null){
        		cb.setVisibility(View.VISIBLE);
        	}else if(i == 1 && manager.getCommand().equals(Witch.Control)){
        		if(name.equals(manager.getCurrentPlayer())){
        			cb.setVisibility(View.INVISIBLE);
        		}else{
        			cb.setVisibility(View.VISIBLE);
        		}
        	}else
        		cb.setVisibility(View.GONE);
        			
        }
        
        setRow(name, vi);
        
        return vi;
    }
    
    private CheckBox getCheckBox(int column, View v){
    	return (CheckBox) v.findViewById(TranslateColumnToViewId(column));
    }
    
    public static int TranslateColumnToViewId(int column){
    	if(column == 0)
    		return R.id.target_r1;
    	if(column == 1)
    		return R.id.target_r2;
    	if(column == 2)
    		return R.id.target_r3;
    	
    	return column;
    }
    
    public void onItemClick(AdapterView<?> unused, View view, int position,	long id) {
		if(manager.getCurrentPlayer() == null){
			if(manager.dScreenController.dScreen.onePersonActive())
				manager.dScreenController.dScreen.onBackPressed();
			return;
		}
		try {
			String selected = targetables.get(position);
			
			CheckBox cb1 = getCheckBox(0, view);
			CheckBox cb2 = getCheckBox(1, view);
			CheckBox cb3 = getCheckBox(2, view);
			if(cb2.getVisibility() == View.GONE){
				cb1.setChecked(!cb1.isChecked());
				//onCheckedChanged(cb1, false);
			
			}else if(cb1.isChecked() && cb2.isChecked()){//witch case
				setCheckBox(cb1, false);
				setCheckBox(cb2, false);
				clicked.remove(new ClickAction(selected, cb1));
				clicked.remove(new ClickAction(selected, cb2));
				manager.command(false, selected, selected);
			
			}else if(cb2.isChecked()){
				//clicked.remove(new ClickAction(selected, cb1));
				setCheckBox(cb2, false);
				cb1.setChecked(true);
				//onCheckedChanged(cb2, true);
			
			}else if(cb3.getVisibility() == View.GONE){
				//2 are visible, c2 isn't checked
				if(cb1.isChecked()){
					//clicked.remove(new ClickAction(selected, cb1));
					cb2.setChecked(true);
					//onCheckedChanged(cb2, false);
				}else{
					if(cb2.getVisibility() == View.VISIBLE){
						cb2.setChecked(true);
						//onCheckedChanged(cb1, true);
					}else{
						cb1.setChecked(true);
						//onCheckedChanged(cb2, true);
					}
				}
			}
			
		}catch (IndexOutOfBoundsException|NullPointerException e){
	
			manager.dScreenController.dScreen.log("accessing out of bounds again");
			e.printStackTrace();
		}
		
	}
    
    private void setCheckBox(CheckBox cb, boolean value){
    	cb.setOnCheckedChangeListener(null);
    	cb.setChecked(value);
    	String name = GetName(cb);
    	
    	ClickAction ca = new ClickAction(name, cb);
    	if(clicked.contains(ca))
    		clicked.remove(ca);
    	else
    		clicked.add(ca);
    	
    	cb.setOnCheckedChangeListener(this);
    }
    
    public static CheckBox getCheckBox(View mainView, int i){
    	if (i == 0)
			i = R.id.target_r1;
		else if (i == 1)
			i = R.id.target_r2;
		else if(i == 2)
			i = R.id.target_r3;
    	return (CheckBox) mainView.findViewById(i);
    }

    public Object getItem(int position) {
        return targetables.get(position);
    }

    public long getItemId(int i){
        return i;
    }

	



	public String GetName(View v){
		ViewGroup vg = (ViewGroup) v.getParent();
		TextView tv = (TextView) vg.findViewById(R.id.target_name);

		return tv.getText().toString();

	}


//	String lastClicked;
	public void onCheckedChanged(CompoundButton buttonView, boolean newValue) {
		manager.dScreenController.dScreen.toast(GetName(buttonView));
		String newTarget = buttonToPlayer.get(buttonView);
		ClickAction ca = new ClickAction(newTarget, (CheckBox) buttonView);
		if(clicked.contains(ca)){
			manager.command(false, ClickAction.getTargets(clicked));
			clicked.remove(ca);
			return;
		}
		clicked.add(ca);
		
		if(manager.getCommand().startsWith(Driver.COMMAND)){
			if(clicked.size() > 2)
				clicked.remove(0);
			if(clicked.size() == 2){
				manager.command(true, clicked.get(0).name, clicked.get(1).name);
			}
		}else if(getCheckBox(1, (View) buttonView.getParent()).getVisibility() == View.GONE){ //regular targets
			manager.command(true, newTarget);
		}else if(manager.getCommand().equals(Witch.Control)){
			String target = null;
			String control = null;
			ClickAction toUncheck = null;
			for(ClickAction cat: clicked){
				if(ca.position == cat.position && !ca.equals(cat))
					toUncheck = cat;
				else{
					if(cat.position == R.id.target_r2)
						control = cat.name;
					if(cat.position == R.id.target_r1)
						target = cat.name;
				}
			}
			if(toUncheck != null){
				toUncheck.cb.setOnCheckedChangeListener(null);
				toUncheck.cb.setChecked(false);
				toUncheck.cb.setOnCheckedChangeListener(this);
				clicked.remove(toUncheck);
			}
			if(target != null && control != null){
				manager.command(true, control, target);
			}
		}
	}

	public static class ClickAction{
		
		String name;
		int position;
		CheckBox cb;
		public ClickAction(String name, CheckBox buttonView){
			if(name == null)
				throw new NullPointerException();
			this.name = name;
			this.position = buttonView.getId();
			this.cb = buttonView;
		}
		
		public int hashCode(){
			return (name + position).hashCode();
		}
		
		public boolean equals(Object o){
			if(o == null)
				return false;
			if(o == this)
				return true;
			
			if(!(o instanceof ClickAction))
				return false;
			
			ClickAction ca = (ClickAction) o;
			if(!ca.name.equals(this.name))
				return false;
			
			if(ca.position != this.position)
				return false;
			
			return true;
		}
		
		static ArrayList<String> getTargets(ArrayList<ClickAction> input){
			ArrayList<String> list = new ArrayList<>();
			
			for(ClickAction ca: input){
				list.add(ca.name);
			}
			
			return list;
		}
		
		public String toString(){
			
			int column = -1;
			
			if(position == R.id.target_r1)
				column = 0;
			else if(position == R.id.target_r2)
				column = 1;
			else if(position == R.id.target_r3)
				column = 2;
			
			return name + column;
		}
	}

	public void setCheckBoxes(HashMap<String, ArrayList<Integer>> checkedPositions) {
		this.selected = checkedPositions;
		notifyDataSetChanged();
	}

	
	

}
