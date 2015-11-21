package voss.android.screens;

import voss.android.R;
import voss.logic.Narrator;
import voss.logic.Event;
import voss.packaging.Board;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class ActivityEndGame extends Activity{

	private Narrator narrator;
	
	public void onCreate(Bundle b){
		super.onCreate(b);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_end_game);
		
		if(b == null)
			narrator = Board.getNarrator(getIntent().getParcelableExtra(Narrator.KEY));
		else
			narrator = (Narrator) b.getParcelable(Narrator.KEY);
		
		
		
		String message = narrator.getWinMessage() + "\n\n";
		message += narrator.getEvents(Event.PRIVATE, true);
		
		((TextView) findViewById(R.id.end_text)).setText(message);
	}
	
}
