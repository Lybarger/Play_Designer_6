package uw.playdesigner6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.util.ArrayList;
import java.lang.Math;
import java.util.Arrays;
import java.util.List;


/**
 * Created by lybar_000 on 5/8/2015.
 */
public class Screen {
    // Define attributes for icon creation
    private int WIDTH = 100;
    private int HEIGHT = 100;
    private int LINEWIDTH = 4;
    public float X; // X position
    public float Y; // Y position
    public int playerIndex; //Player associated with
    public boolean selected; //Player selection status (selected by touch event)
    private Bitmap iconOriginal;
    public Bitmap icon;
    public Paint paint;
    public float rotation = 0;
    public float[] historyX = new float[] {0, 0};
    public float[] historyY = new float[] {0, 0};

    private static Context context;
    private Matrix matrix = new Matrix();

    // Constructor
    public Screen(float playerXTemp, float playerYTemp , int playerIndexTemp, boolean selectionStatusTemp){

        X = playerXTemp;
        Y = playerYTemp;
        playerIndex = playerIndexTemp;
        selected = selectionStatusTemp;

    }

    // Get context, for the purpose of accessing XML resources
    public static void setContext(Context mcontext) {
        if (context == null)
            context = mcontext;
    }

    // Create screen icon as bitmap
    public void createIcon() {

        // Define paint for icon
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStrokeWidth(LINEWIDTH);
        paint.setColor(context.getResources().getColor(R.color.husky_dark_gray));


        // Create temporary icon and canvas
        iconOriginal = Bitmap.createBitmap(WIDTH + LINEWIDTH, HEIGHT + LINEWIDTH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(iconOriginal);

        // Create T shaped drawing
        canvas.drawLine((float) HEIGHT * 3 / 4, (float) WIDTH / 2, HEIGHT, (float) WIDTH / 2, paint);
        canvas.drawLine(HEIGHT, WIDTH / 3, HEIGHT, WIDTH * 2 / 3, paint);
        //canvas.drawRect(0,0,WIDTH,HEIGHT,paint);

        // Create rotation matrix
        //rotation = 0;
        matrix.setRotate(rotation);

        // Create rotated icon, based on rotation matrix
        icon = Bitmap.createBitmap(iconOriginal , 0, 0, iconOriginal.getWidth(), iconOriginal.getHeight(), matrix, true);

    }

    public void updateIcon(){
        matrix.setRotate(rotation);

        // Create rotated icon, based on rotation matrix
        icon = Bitmap.createBitmap(iconOriginal , 0, 0, iconOriginal.getWidth(), iconOriginal.getHeight(), matrix, true);
    }



    // X coordinates for insertion (accounts for width of icon)
    public float getX(){
        // Determine insertion point in view that will center icon at player position
        return X -(float)icon.getWidth() / 2;
    }

    // Y coordinate for insertion (accounts for height of icon)
    public float getY(){
        // Determine insertion point in view that will center icon at player position
        return Y -(float) icon.getHeight()/2;
    }

    public void updateLocation(float xTemp, float yTemp){
        if (xTemp != historyX[1] && yTemp != historyY[1]) {

            // Set icon location with in view
            X = xTemp;
            Y = yTemp;

            // Update history of X and Y coordinates
            historyX[0] = historyX[1];
            historyX[1] = X;
            historyY[0] = historyY[1];
            historyY[1] = Y;

            // Determine required rotation based on history
            rotation = (float)Math.toDegrees(Math.atan2(historyY[1] - historyY[0], historyX[1] - historyX[0]));
            if (rotation < 0){rotation = rotation + 360;}

            // Redraw icon
            updateIcon();
        }
    }

}

