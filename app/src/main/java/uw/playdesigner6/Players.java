package uw.playdesigner6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.MotionEvent;

import java.util.List;
import java.util.Map;

/**
 * Created by lybar_000 on 4/2/2015.
 */
public class Players {


    public static int PLAYER_COUNT = 5;
    private static float[] INITIAL_X = new float[] {400, 100, 700, 250, 550};
    private static float[] INITIAL_Y = new float[] {200, 350, 350, 500, 500};
    private static String[] NAMES = new String[] {"1", "2", "3", "4", "5"};


    public float X[] = new float[PLAYER_COUNT]; //Player X positions
    public float Y[] = new float[PLAYER_COUNT]; //Player Y positions
    public String[] name = new String[PLAYER_COUNT]; //Player name (ID)

    public Boolean[] selectionStatus = new Boolean[PLAYER_COUNT]; //Player selection status (selected by touch event)
    public Bitmap[] icon = new Bitmap[PLAYER_COUNT];

    public Path path;

    private static Context context;
    public Paint paintCircle, paintText, paintPath;
    private static int LINEWIDTH = 4;
    private static int ICON_SIZE = 40;

    private static int TEXT_SIZE = 30;
    private static int TEXT_SHIFT = TEXT_SIZE*6/20;

    public Players(Context mcontext){


        for (int i = 0; i < PLAYER_COUNT; i++){
            X[i] = INITIAL_X[i];
            Y[i] = INITIAL_Y[i];
            name[i] = NAMES[i];

            selectionStatus[i] = false;
        }


        if (context == null) {
            context = mcontext;
        }

        createIcon();
        createPath();


    }

    public void reinitialize(){
        for (int i = 0; i < PLAYER_COUNT; i++){
            X[i] = INITIAL_X[i];
            Y[i] = INITIAL_Y[i];
            selectionStatus[i] = false;
        }
    }

    public void createIcon() {
        paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(context.getResources().getColor(R.color.husky_purple));


        //Define text format
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTextSize(TEXT_SIZE);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setColor(Color.WHITE);
        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);
        paintText.setTypeface(tf);

        for (int i = 0; i < PLAYER_COUNT; i++) {
            icon[i] = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888);

            // Add canvas to each bitmap in array
            Canvas temporaryCanvas = new Canvas(icon[i]);

            // Draw circle on canvas, for player icon
            temporaryCanvas.drawCircle(ICON_SIZE / 2, ICON_SIZE / 2, ICON_SIZE / 2, paintCircle);

            // Add player name/number two player icon
            temporaryCanvas.drawText(name[i], ICON_SIZE / 2, ICON_SIZE / 2 + TEXT_SHIFT, paintText);
        }
    }

    // Update player locations and build map of points
    public float[] updatePositions(MotionEvent event, Location touch) {
        //Players player = players[playerIndex];
        float[] output = new float[]{-1, -1, -1};

        for (int j = 0; j < PLAYER_COUNT; j++) {

            // Minimum distance between jth player and other players
            float minDistanceToOtherPlayers = Float.POSITIVE_INFINITY;

            // Too close to other players?
            boolean obstacleEncountered = false;

            // Loop on players
            for (int i = 0; i < PLAYER_COUNT; i++) {

                if (i != j) {

                    // Distance to other players
                    float distance = euclideanDistance(touch.X, X[i], touch.Y, Y[i]);

                    // Minimum distance to other players
                    minDistanceToOtherPlayers = Math.min(distance, minDistanceToOtherPlayers);

                    // Other player encountered?
                    obstacleEncountered = obstacleEncountered || (minDistanceToOtherPlayers < (float) ICON_SIZE / 2);

                }
            }

            // Distance between touch event and jth player
            float distanceToTouch = euclideanDistance(touch.X, X[j],
                        touch.Y, Y[j]);

            //Create repository points traversed by player
            //List<List<float[]>> data = dataPlayers.get(name[j]);

            //Address each touch event
            switch (event.getAction()) {

                //Touch DOWN
                case MotionEvent.ACTION_DOWN:
                    // Determine if touch event within bounds of jth player icon
                    selectionStatus[j] = (distanceToTouch < ((float) ICON_SIZE) / 2);

                    if (selectionStatus[j]) {
                        // Move path to location of current touch event
                        path.moveTo(X[j], Y[j]);

                        //float[] XY = new float[] {X[j], Y[j]};

                        // Create output array
                        output[0] = j;
                        output[1] = X[j];
                        output[2] = Y[j];
                        // Add points to map
                        //data.add(XY);
                    }
                    break;

                //Touch MOVE
                case MotionEvent.ACTION_MOVE:

                    // If jth player selected and no other players encountered
                    // update jth player position
                    if (selectionStatus[j] && !obstacleEncountered) {

                        // Determine midpoints between patch location and jth
                        // player location
                        float avgX = (touch.X + X[j]) / 2;
                        float avgY = (touch.Y + Y[j]) / 2;

                        // Build path using quadratic
                        path.quadTo(X[j], Y[j], avgX, avgY);

                        // Set jth player location
                        X[j] = touch.X;
                        Y[j] = touch.Y;

                        // Create output array
                        output[0] = j;
                        output[1] = X[j];
                        output[2] = Y[j];
                    }
                    break;

                //Touch UP
                case MotionEvent.ACTION_UP:
                    if (selectionStatus[j]) {

                        // Complete path outline
                        path.lineTo(X[j], Y[j]);

                        // Toggle selection status (selection event is complete)
                        selectionStatus[j] = false;

                        // Create output array
                        output[0] = j;
                        output[1] = X[j];
                        output[2] = Y[j];
                    }
                    break;
                default:
                    //return false;
            }

            //return player[j];
        }


        return output;
    }

    // Create path and path format
    public void createPath(){
        // Define formatting for path
        //Define path format
        paintPath = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPath.setColor(context.getResources().getColor(R.color.husky_metallic_gold));
        paintPath.setAntiAlias(true);
        paintPath.setStrokeWidth(5);
        paintPath.setStyle(Paint.Style.STROKE);
        paintPath.setStrokeJoin(Paint.Join.ROUND);
        paintPath.setStrokeCap(Paint.Cap.ROUND);

        // Create path
        path = new Path();
    }

    // Calculate Euclidean distance between points
    private float euclideanDistance(float x1, float x2, float y1, float y2) {
        return (float)Math.pow(Math.pow(x2 - x1, 2) + Math.pow(y2-y1, 2), 0.5);
    }

    //Create string from points
    private String pointsToString(float x, float y){
        return "(" + Float.toString(x) + "," + Float.toString(y) + ")";
    }


    // X coordinates for insertion (accounts for width of icon)
    public float getX(int playerIndex){
        // Determine insertion point in view that will center icon at player position
        return X[playerIndex]-(float)ICON_SIZE/2;
    }

    // Y coordinate for insertion (accounts for height of icon)
    public float getY(int playerIndex){
        // Determine insertion point in view that will center icon at player position
        return Y[playerIndex]-(float)ICON_SIZE/2;
    }
}
