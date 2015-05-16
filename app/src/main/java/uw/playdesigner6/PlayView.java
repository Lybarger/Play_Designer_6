package uw.playdesigner6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by lybar_000 on 3/10/2015.
 */
public class PlayView extends View {

    //Define graphics variables
    //private Paint paintPlayerCircle, paintPlayerText, paintPath;

    private Paint paintCanvas;
    private Canvas canvasPlay;

    private Bitmap bitmapPlay;

    //Player definition constants
    private static int PLAYER_ICON_SIZE = 40;

    //private static int text_size = 30;

    //Define initial player positions
    private static int PLAYER_COUNT = 5;
    private static int[] INITIAL_X = new int[] {400, 100, 700, 250, 550};
    private static int[] INITIAL_Y = new int[] {200, 350, 350, 500, 500};
    private Players players;
    
    //private Map<String,List<String>> points;

    private Map<Integer,List<List<float[]>>> dataPlayers;
    private List<Integer> dataBall = new ArrayList<Integer>();

    private Ball ball;
    private Hoop hoop;
    

    //private List<Screen> screens = new ArrayList<Screen>();
    private Screens screensObject = new Screens();
    // Related to animation
    private Context context;
    private android.os.Handler handler;
    private final int FRAME_RATE = 30;

    private int pointIndex = 0;

    private GestureDetectorCompat gestureDetector;

    private boolean playExisting = false;
    private final int STAGE_LENGTH = 3;   //Time in seconds
    private int stageFrameCount = 0;
    private int currentStage = 0;
    private PlayInterpolated interpolatedPlay;
    private int FRAMES_PER_STAGE = FRAME_RATE*STAGE_LENGTH;

    //Instantiate view
    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Create handler for animation thread
        this.context = context;

        // Create handler for multithreading
        handler = new android.os.Handler();

        // Create gesture detector
        gestureDetector = new GestureDetectorCompat(context, new MyGestureListener());

        // Create canvas
        paintCanvas = new Paint(Paint.DITHER_FLAG);

        // Create players
        players = new Players(context);

        // Create ball
        ball = new Ball(INITIAL_X[0], INITIAL_Y[0], 0, context);

        // Create hoop
        hoop = new Hoop(context);

        // Initialize court (set the initial positions for objects)
//        initializeCourt();

    }

    // Create runnable animation thread
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            // Triggers update of canvas
            invalidate();
        }
    };

    //Set up hash map to store position information
    public void setupDataPoints(Map<Integer,List<List<float[]>>> dataPlayersTemp, List<Integer> dataBallTemp){

        this.dataPlayers = dataPlayersTemp;
        this.dataBall = dataBallTemp;
        addStageToMap();
    }


    //Create and display blank canvas
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmapPlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvasPlay = new Canvas(bitmapPlay);
    }

    //Initial insertion of players onto court
    public void initializeCourt(){

        // Clear canvas
        clearCanvas();

        // Remove data from map
        clearData();

        // Reset stage
        currentStage = 0;

        // Reinitialize player positions
        players.reinitialize();

        // Reinitialize ball position
        ball.reinitialize(players);

        // Remove all screens
        screensObject.clearAll();

        // Add first stage to map
        addStageToMap();
    }

    // Clear data in maps
    public void clearData(){
        Set<Integer> keys = dataPlayers.keySet();
        for (Integer key : keys){
            List<List<float[]>> points = dataPlayers.get(key);
            points.clear();
        }

        dataBall.clear();
    }

    public void saveLastPoint(){

        // Get a set of keys
        Set<Integer> keys = dataPlayers.keySet();

        // Loop on keys (players)
        for (Integer key : keys){

            // Put last XY coordinates into a list
            List<float[]> initialStage = new ArrayList<float[]>();

            // Clear all data for selected key (player)
            dataPlayers.get(key).clear();

            // Add list with last XY coordinates
            dataPlayers.get(key).add(initialStage);
            dataPlayers.get(key).get(0).add(new float[]{players.X[key], players.Y[key]});
        }

        dataBall.add(ball.playerIndex);

        currentStage = 0;

    }

    public void printData(){
        Set<Integer> keys = dataPlayers.keySet();
        for (Integer key : keys){
            List<List<float[]>> stages = dataPlayers.get(key);
            int stageCount = stages.size();
            for (int currentStage = 0; currentStage < stageCount; currentStage++) {
                List<float[]> points = stages.get(currentStage);
                int pointCount = points.size();
                for (int currentPoint = 0; currentPoint < pointCount; currentPoint++){
                    String player = Integer.toString(key);
                    String stage = Integer.toString(currentStage);
                    String X = Float.toString(points.get(currentPoint)[0]);
                    String Y = Float.toString(points.get(currentPoint)[1]);

                    System.out.println(player + ", " + ", " + stage + ", " + X + ", " + Y);
                }
            }
        }

    }



    // Increment stage (called by Main on button click)
    public void incrementStage(){
        currentStage++;
        dataBall.add(ball.playerIndex);
        addStageToMap();
    }

    private void addStageToMap(){

        Set<Integer> keys = dataPlayers.keySet();
        for (Integer key : keys){
            List<float[]> newStage = new ArrayList<float[]>();

            newStage.add(new float[]{players.X[key],players.Y[key]});
            dataPlayers.get(key).add(newStage);

        }

    }

    @Override
    // onDraw is called when:
    // 1) Initial draw of view
    // 2) Function invalidate() (Note that this is part of the animation thread/runnable)
    protected void onDraw(Canvas canvas) {

        // Update frame if replaying existing play
        if (playExisting) {
            updatePlay();
            pointIndex++;
        }

        // Draw path
        canvas.drawPath(players.path, players.paintPath);
        canvas.drawPath(ball.path,ball.paintPath);

        canvas.drawBitmap(ball.icon, ball.getX(), ball.getY(), null);

        // Update player icon positions
        for (int i = 0; i < PLAYER_COUNT; i++) {

            // Determine location of player icon, with offset
            float X = players.getX(i);
            float Y = players.getY(i);

            // Draw player icons on canvas
            canvas.drawBitmap(players.icon[i], X, Y, null);
        }

        canvas.drawBitmap(hoop.icon, hoop.getX(), hoop.getY(), null);

        // Need to determine if necessary
        canvas.drawBitmap(bitmapPlay, 0, 0, paintCanvas);

        screensObject.drawScreens(canvas);

        //Call in invalidate() using animation thread
        handler.postDelayed(runnable, FRAME_RATE);

        //canvas.drawText(Integer.toString(currentStage), 100, 100, new Paint());
        //canvas.drawText(Integer.toString(dataPlayers.get(0).size()), 100, 150, new Paint());
    }



    //Capture and handle touch events during play recording
    // http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
    public boolean onTouchEvent(MotionEvent event) {

        // If replaying play, stop on touch
        stopPlay();

        // Let gesture detector inspect actions first
        boolean gestureDetected = gestureDetector.onTouchEvent(event);
        if (gestureDetected){
            return gestureDetected;
        }

        //Capture touch location
        Location locationTouch = locationProjected(event.getX(), event.getY());

        // Update player positions based on touch event
        if (!ball.selected) {
            // Format of x
            // [0] = player index
            // [1] = player X coordinate
            // [2] = player Y coordinate
            float[] x = players.updatePositions(event, locationTouch);

            // If player position changed, update map
            if (x[0] >= 0) {
                float[] XY = new float[]{x[1], x[2]};
                dataPlayers.get((int)x[0]).get(currentStage).add(XY);
            }
        }

        // screensObject.updateScreens(players, ball);

        // Update ball based on touch event

        int previousPlayerWithBall = ball.playerIndex;
        ball.updateBallPosition(event, locationTouch, players, hoop);
        int currentPlayerWithBall = ball.playerIndex;
        if (previousPlayerWithBall != currentPlayerWithBall){
            incrementStage();
            incrementStage();

        }
        //updateBallPosition(event, locationTouch);
        screensObject.updateScreenPositions(players, ball);

        return true;
    }

    // https://developer.android.com/training/gestures/detector.html
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        //private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            //Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
            Location locationTouch = locationProjected(event.getX(), event.getY());

            // Update ball selection status
            Boolean distanceCheckBall = ball.updateStatus(players, hoop, locationTouch);


            for (int i = 0; i < PLAYER_COUNT; i++) {
                boolean distanceCheckScreen = (float) players.icon[i].getWidth()/2 > euclideanDistance(
                                                                    players.X[i], event.getX(),
                                                                    players.Y[i], event.getY());
                if (distanceCheckScreen && !distanceCheckBall){
                    screensObject.updateScreenCount(i, context, players);

                }

            }

            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            //Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
            return true;
        }
    }

    //Clear canvas (Used to remove path)
    public void clearCanvas(){
        players.path.reset();
        ball.path.reset();
        canvasPlay.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
    
    //Project location
    private Location locationProjected(float x, float y){
        float x_projected = x;
        float y_projected = y;

        //Currently this method does nothing.
        //It will be used if there needs to be a coordinate transformation.
        return new Location(x_projected, y_projected);
    }


    //Create string from points
    private String pointsToString(float x, float y){
        return "(" + Float.toString(x) + "," + Float.toString(y) + ")";
    }

    // Start play
    public void startPlay(Play playMap){
        //System.out.println( "startPlay");
        //printMap(playMap);

        // Allow play update method to be called from onDraw
        playExisting = true;

        // Create play as Play, based on points imported from XML file
        Play originalPlay = playMap;

        // Set current (initial) stage index to 0
        currentStage = 0;
        stageFrameCount = -1;

        // Interpolate play, such that each stage of each player has the same number of points
        interpolatedPlay = new PlayInterpolated(originalPlay, hoop);
    }

    // Stop play replay
    public void stopPlay(){
        playExisting = false;
    }

    // Calculate Euclidean distance between points
    private float euclideanDistance(float x1, float x2, float y1, float y2) {
        return (float)Math.pow(Math.pow(x2 - x1, 2) + Math.pow(y2-y1, 2), 0.5);
    }


    private void printMap(Map<Integer,List<List<float[]>>> play){
        Set<Integer> keys = play.keySet();
        for (Integer key : keys){
            List<List<float[]>> stages = play.get(key);
            int stageCount = stages.size();
            for (int currentStage = 0; currentStage < stageCount; currentStage++) {
                List<float[]> points = stages.get(currentStage);
                int pointCount = points.size();
                for (int currentPoint = 0; currentPoint < pointCount; currentPoint++){
                    String player = Integer.toString(key);
                    String stage = Integer.toString(currentStage);
                    String X = Float.toString(points.get(currentPoint)[0]);
                    String Y = Float.toString(points.get(currentPoint)[1]);

                    System.out.println("TEST LOOP" + player + ", " + ", " + stage + ", " + X + ", " + Y);
                }
            }
        }
    }

    // During play replay, update player positions
    public void updatePlay(){


        //List<float[]> coordinatesAsList;

// Determine if end of stage reached
        if (stageFrameCount >= FRAMES_PER_STAGE-1){

            // Increment stage
            currentStage++;

            // Determine if end of play reached
            if (currentStage >= interpolatedPlay.getStageCount()){
                // Reset stage
                currentStage = 0;
                ball.path.reset();
                float[] initialBallPosition = interpolatedPlay.dataBall.get(currentStage).get(stageFrameCount);
                ball.path.moveTo(initialBallPosition[0], initialBallPosition[1]);
                players.path.reset();
            }
            // Reset frame counter within stage
            stageFrameCount = 0;
        }
        else{
            // Increment frame count within stage
            stageFrameCount++;
        }

        // Loop on players
        for (int playerIndex : interpolatedPlay.dataPlayers.keySet()){
            // Extract relevant XY coordinates
            float[] XY = interpolatedPlay.getXYcoordinate(playerIndex, currentStage, stageFrameCount);

            // Update player positions
            if (currentStage == 0 && stageFrameCount == 0) {
                players.X[playerIndex] = XY[0];
                players.Y[playerIndex] = XY[1];
                players.path.moveTo(players.X[playerIndex], players.Y[playerIndex]);
            }
            else {
                players.path.moveTo(players.X[playerIndex], players.Y[playerIndex]);
                players.X[playerIndex] = XY[0];
                players.Y[playerIndex] = XY[1];
            }

            players.path.lineTo(players.X[playerIndex], players.Y[playerIndex]);
        }

        if (currentStage == 0 && stageFrameCount == 0) {
            float[] initialBallPosition = interpolatedPlay.dataBall.get(currentStage).get(stageFrameCount);
            ball.path.moveTo(initialBallPosition[0], initialBallPosition[1]);
        }
        ball.X = interpolatedPlay.dataBall.get(currentStage).get(stageFrameCount)[0];
        ball.Y = interpolatedPlay.dataBall.get(currentStage).get(stageFrameCount)[1];
        ball.path.lineTo(ball.X,ball.Y);



    }
}
