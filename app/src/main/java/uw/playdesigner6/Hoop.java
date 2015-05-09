package uw.playdesigner6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by lybar_000 on 5/9/2015.
 */
public class Hoop {

    public static int X = 400;
    public static int Y = 560;
    private static int LINEWIDTH = 4;
    private static int ICON_SIZE = 40;
    private Paint paint;
    private static Context context;

    public Bitmap icon;

    public Hoop(){


    }

    // Get context, for the purpose of accessing XML resources
    public static void setContext(Context mcontext) {
        if (context == null)
            context = mcontext;
    }

    public void createIcon() {
        //Define circle format
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINEWIDTH);
        paint.setColor(context.getResources().getColor(R.color.husky_black));

        icon = Bitmap.createBitmap(ICON_SIZE + LINEWIDTH * 2,
                ICON_SIZE + LINEWIDTH * 2,
                Bitmap.Config.ARGB_8888);

        // Add canvas to each bitmap in array
        Canvas temporaryCanvas = new Canvas(icon);

        // Draw circle on canvas, for player icon
        float midpoint = (float) icon.getWidth() / 2;
        float radius = midpoint - LINEWIDTH;
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
}
