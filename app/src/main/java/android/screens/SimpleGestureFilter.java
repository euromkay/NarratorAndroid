package android.screens;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;



public class SimpleGestureFilter extends SimpleOnGestureListener{

    public final static int SWIPE_LEFT  = 0;
    public final static int SWIPE_RIGHT = 1;

    public final static int MODE_SOLID       = 1;
    public final static int MODE_DYNAMIC     = 2;

    private final static int ACTION_FAKE = -13; //just an unlikely number

    private int mode             = MODE_DYNAMIC;
    private boolean tapIndicator = false;

    private Activity context;
    private GestureDetector detector;
    private SimpleGestureListener listener;

    public SimpleGestureFilter(Activity context, SimpleGestureListener sgl) {

        this.context = context;
        this.detector = new GestureDetector(context, this);
        this.listener = sgl;
    }

    public void onTouchEvent(MotionEvent event){


        boolean result = this.detector.onTouchEvent(event);

        if(this.mode == MODE_SOLID)
            event.setAction(MotionEvent.ACTION_CANCEL);
        else if (this.mode == MODE_DYNAMIC) {

            if(event.getAction() == ACTION_FAKE)
                event.setAction(MotionEvent.ACTION_UP);
            else if (result)
                event.setAction(MotionEvent.ACTION_CANCEL);
            else if(this.tapIndicator){
                event.setAction(MotionEvent.ACTION_DOWN);
                this.tapIndicator = false;
            }

        }
        //else just do nothing, it's Transparent
    }


    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        final float xDistance = Math.abs(e1.getX() - e2.getX());

        //if(xDistance > this.swipe_Max_Distance || yDistance > this.swipe_Max_Distance)
          //  return false;

        velocityX = Math.abs(velocityX);
        boolean result = false;

        if(velocityX > 100 && xDistance > 100){
            if(e1.getX() > e2.getX()) // right to left
                this.listener.onSwipe(SWIPE_LEFT);
            else
                this.listener.onSwipe(SWIPE_RIGHT);

            result = true;
        }

        return result;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        this.tapIndicator = true;
        return false;
    }

    public boolean onDoubleTap(MotionEvent arg) {
        this.listener.onDoubleTap();
        return true;
    }

    public boolean onDoubleTapEvent(MotionEvent arg) {
        return true;
    }

    public boolean onSingleTapConfirmed(MotionEvent arg) {

        if(this.mode == MODE_DYNAMIC){        // we owe an ACTION_UP, so we fake an
            arg.setAction(ACTION_FAKE);      //action which will be converted to an ACTION_UP later.
            this.context.dispatchTouchEvent(arg);
        }

        return false;
    }

    public interface SimpleGestureListener{
        void onSwipe(int direction);
        void onDoubleTap();
    }

}