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
        Bitmap iconOriginal = Bitmap.createBitmap(WIDTH + LINEWIDTH, HEIGHT + LINEWIDTH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(iconOriginal);

        // Create T shaped drawing
        canvas.drawLine((float) WIDTH / 2, (float)HEIGHT*3/4, (float) WIDTH / 2, HEIGHT, paint);
        canvas.drawLine(WIDTH/3,HEIGHT,WIDTH*2/3, HEIGHT, paint);

        // Create rotation matrix
        matrix.postRotate(rotation);

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
        X = xTemp;
        Y = yTemp;
        historyX[0] = historyX[1];
        historyX[1] = X;
        historyY[0] = historyY[1];
        historyY[1] = Y;

        rotation = (float)Math.atan2(historyY[1] - historyY[0],historyX[1]-historyX[0])*180f/3.14f;
        System.out.println( rotation );
    }

/*    public float getAngle(){
        return (float)Math.atan2(historyY.get(1) - historyY.get(0),
                                 historyX.get(1)-historyX.get(0));

    }*/
}

