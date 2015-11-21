package voss.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageView;
import voss.android.screens.SimpleGestureFilter;
import voss.android.screens.SimpleGestureFilter.SimpleGestureListener;
import voss.logic.Narrator;
import voss.logic.exceptions.UnsupportedMethodException;


public class ActivityTutorial extends Activity implements SimpleGestureListener{



    private SimpleGestureFilter detector;
    private ImageView image;
    int index;

    public static final int[] tutorials = new int[]{R.drawable.step0, R.drawable.step1, R.drawable.step2, R.drawable.step3, R.drawable.step4, R.drawable.step5, R.drawable.step6};

    public void onCreate(Bundle b0){
        super.onCreate(b0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tutorial);

        index = 0;
        image = (ImageView) findViewById(R.id.imageView);
        detector = new SimpleGestureFilter(this, this);
    }

    public void onDoubleTap(){
        Log.e("ActivityTutorial", "double tap detected");
    }

    public boolean dispatchTouchEvent(@NonNull MotionEvent me){
        // Call onTouchEvent of SimpleGestureFilter class
        detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    public void onSwipe(int direction){
        Log.e("ActivityTutorial", "swipe detected");
        if(direction == SimpleGestureFilter.SWIPE_LEFT && index < 6){
            index++;
        }else if (direction == SimpleGestureFilter.SWIPE_RIGHT && index != 0){
            index--;
        }
        image.setImageResource(tutorials[index]);
        image.invalidate();
    }

	public Narrator getNarrator() {
		throw new UnsupportedMethodException();
	}
}
