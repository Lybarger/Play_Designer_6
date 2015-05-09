package uw.playdesigner6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by lybar_000 on 5/6/2015.
 */
public class Ball {
    private int WIDTH = 40;
    private int LINEWIDTH_DEFAULT = 5;
    private int LINEWIDTH_SELECTED = 10;
    private int LINEWIDTH_PATH = 4;
    private float[] dashLength = new float[]{10.0f, 10.0f};
    private static Context context;

    public float X; //Ball X position
    public float Y; //Ball Y position
    public int playerIndex; //Player with ball (Note: value of -1 denotes hoop)
    public boolean beingPassed; //Ball being passed
    public boolean selected; //Player selection status (selected by touch event)
    public Paint paint, paintPath;
    public Bitmap icon;
    public Path path;

    
    // Constructor
    public Ball(float XTemp, float YTemp , int playerIndexTemp, boolean beingPassedTemp, boolean selectionStatusTemp){
        X = XTemp;
        Y = YTemp;
        playerIndex = playerIndexTemp;
        beingPassed = beingPassedTemp;
        selected = selectionStatusTemp;
    }

    // Get context, for the purpose of accessing XML resources
    public static void setContext(Context mcontext) {
        if (context == null)
            context = mcontext;
    }

    // Create ball icon as bitmap
    public void createIcon() {

        int size;
        float radius;
        float midpoint;

        // Define ball format if selected
        if (selected) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(LINEWIDTH_SELECTED);
            paint.setColor(context.getResources().getColor(R.color.Red));

            size = WIDTH + 2*LINEWIDTH_SELECTED;
            radius = (float)WIDTH/2 + LINEWIDTH_SELECTED/2;

        }
        // Define ball format if NOT selected
        else {

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(LINEWIDTH_DEFAULT);
            paint.setColor(context.getResources().getColor(R.color.DarkOrange));

            size = WIDTH + 2*LINEWIDTH_DEFAULT;
            radius = (float)WIDTH/2 + LINEWIDTH_DEFAULT/2;

        }

        // Create icon as bitmap
        icon = Bitmap.createBitmap(size, size,Bitmap.Config.ARGB_8888);

        // Create canvas and draw circle, representing ball
        Canvas temporaryCanvas = new Canvas(icon);
        midpoint = (float)size/2;
        temporaryCanvas.drawCircle(midpoint, midpoint, radius, paint);

    }

    // X coordinates for insertion (accounts for width of icon)
    public float getX(){
        // Determine insertion point in view that will center icon at player position
        return X-(float)icon.getWidth() / 2;
    }

    // Y coordinate for insertion (accounts for height of icon)
    public float getY(){
        // Determine insertion point in view that will center icon at player position
        return Y-(float)icon.getHeight()/2;
    }

    // Create path and path format
    public void createPath(){
        // Define formatting for path
        paintPath = new Paint(Paint.ANTI_ALIAS_FLAG);
        DashPathEffect dashPathEffect = new DashPathEffect(dashLength, 0);
        paintPath.setPathEffect(dashPathEffect);
        paintPath.setColor(context.getResources().getColor(R.color.husky_metallic_gold));
        paintPath.setAntiAlias(true);
        paintPath.setStrokeWidth(LINEWIDTH_PATH);
        paintPath.setStyle(Paint.Style.STROKE);

        // Create path
        path = new Path();
    }

    // Update ball on double tap
    public void updateBall(float XTemp, float YTemp, int playerIndexTemp, boolean selectedTemp){
        X = XTemp;
        Y = YTemp;
        playerIndex = playerIndexTemp;
        selected = selectedTemp;
        createIcon();
        path.lineTo(X, Y);
    }

    // Update location
    public void updateLocation(float XTemp, float YTemp){
        X = XTemp;
        Y = YTemp;
    }

}