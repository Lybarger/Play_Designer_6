package uw.playdesigner6;

// http://www.codeproject.com/Articles/825739/Article-Beginners-Guide-to-Android-Animation-Gr#Frame by Frame or Drawable Animations


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.os.Handler;



public class AnimatedBoxView extends ImageView {
    private Context context;
    int x = this.getWidth()/2;
    int y = -1;
    private int xVelocity = 5;
    private int yVelocity = 5;
    private android.os.Handler handler;
    private final int FRAME_RATE = 30;

    public AnimatedBoxView(Context context, AttributeSet attrs)  {
        super(context, attrs);
        this.context = context;
        handler = new android.os.Handler();
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };
    protected void onDraw(Canvas canvas) {
        BitmapDrawable box = (BitmapDrawable) context.getResources().getDrawable(R.drawable.basketball50);
        if (y <0) {
            y = this.getHeight()/2;
        } else {
            y += yVelocity;
            if (y < 0 || (y > this.getHeight() - box.getBitmap().getHeight())) {
                yVelocity = yVelocity*-1;
            }
        }
        canvas.drawBitmap(box.getBitmap(), x, y, null);
        handler.postDelayed(runnable, FRAME_RATE);
    }
}