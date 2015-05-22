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
    //private Screens screensObject = new Screens();
    // Related to animation
    private Context context;
    private android.os.Handler handler;
    private final int FRAME_RATE = 30;

    private int pointIndex = 0;

    private GestureDetectorCompat gestureDetector;

    private boolean playExisting = false;
    private final int STAGE_LENGTH = 3;   //Time in seconds

    private int frame = 0;
    private int stage = 0;
    private int playPauseCount = 0;
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
        stage = 0;

        // Reinitialize player positions
        players.reinitialize();

        // Reinitialize ball position
        ball.reinitialize(players);

        // Remove all screens
        //screensObject.clearAll();

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

        // Loop on keys (players)
        for (Integer key : dataPlayers.keySet()){

            // Put last XY coordinates into a list
            List<float[]> initialStage = new ArrayList<float[]>();
            initialStage.add(players.getCurrentData(key));

            // Clear all data for selected key (player)
            dataPlayers.get(key).clear();

            // Add list with last XY coordinates
            dataPlayers.get(key).add(initialStage);
            //dataPlayers.get(key).get(0).add(new float[]{players.X[key], players.Y[key]});
        }



        dataBall.add(ball.playerIndex);

        stage = 0;

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
        stage++;
        dataBall.add(ball.playerIndex);
        addStageToMap();
    }

    private void addStageToMap(){

        Set<Integer> keys = dataPlayers.keySet();
        for (Integer key : keys){
            List<float[]> newStage = new ArrayList<float[]>();

            newStage.add(players.getCurrentData(key));
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

        canvas.drawPath(ball.path, ball.paintPath);

        canvas.drawBitmap(ball.icon, ball.getX(), ball.getY(), null);

        players.updateCanvas(canvas);

        canvas.drawBitmap(hoop.icon, hoop.getX(), hoop.getY(), null);

        // Need to determine if necessary
        canvas.drawBitmap(bitmapPlay, 0, 0, paintCanvas);

        //screensObject.drawScreens(canvas);

        //Call in invalidate() using animation thread
        handler.postDelayed(runnable, FRAME_RATE);

        //canvas.drawText(Integer.toString(stage), 100, 100, new Paint());
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
            int playerIndex = players.updatePositions(event, locationTouch);

            // If player position changed, update map
            if (playerIndex >= 0) {
                //float[] XY = new float[]{x[1], x[2]};
                float[] temp = players.getCurrentData(playerIndex);
                System.out.println(temp[3]);
                dataPlayers.get(playerIndex).get(stage).add(players.getCurrentData(playerIndex));
            }
        }

        // screensObject.updateScreens(tempplayers, ball);

        // Update ball based on touch event

        int previousPlayerWithBall = ball.playerIndex;
        ball.updateBallPosition(event, locationTouch, players, hoop);
        int currentPlayerWithBall = ball.playerIndex;
        if (previousPlayerWithBall != currentPlayerWithBall){
            players.updateScreenState2(ball);
            incrementStage();
            //incrementStage();

        }
        //updateBallPosition(event, locationTouch);
        //screensObject.updateScreenPositions(players, ball);

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

            // Update screen state (dispayed or hidden)
            players.updateScreenState(event, ball);

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

        for (Integer key : dataPlayers.keySet()) {
            players.path[key].reset();
        }
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
        stage = 0;
        frame = 0;

        // Interpolate play, such that each stage of each player has the same number of points
        interpolatedPlay = new PlayInterpolated(originalPlay, hoop);

        // Update player and ball initial positions
        for (int playerIndex : interpolatedPlay.dataPlayers.keySet()){
            players.updateXY(playerIndex, interpolatedPlay.getXYcoordinate(playerIndex, 0, 0));
        }
        ball.updateXY(interpolatedPlay.dataBall.get(0).get(0));

        // Reset paths and move to initial positions
        players.pathsReset();
        players.pathsMoveToPlayerPositions();
        ball.path.reset();
        ball.path.moveTo(ball.X, ball.Y);

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
        if (frame >= FRAMES_PER_STAGE-1){

            // Increment stage
            stage++;
            // Reset frame counter within stage
            frame = 0;

            // Determine if end of play reached
            if (stage >= interpolatedPlay.getStageCount()){
/*
                if (playPauseCount<FRAMES_PER_STAGE){
                    playPauseCount7++;
                }
                else {*/
//                    playPauseCount = 0;

                // Reset stage
                stage = 0;

                // Reset paths
                players.pathsReset();
                ball.path.reset();

                // Update player and ball location to initial starting positions
                for (int i : interpolatedPlay.dataPlayers.keySet()) {
                    //players.updateXY(i, interpolatedPlay.getXYcoordinate(i, stage, frame));
                    //float[] xx = ;
                    players.updateData(i, interpolatedPlay.getData(i,stage,frame));
//System.out.println( "player" + players.X[1]);
                    //System.out.println("Playview " +Float.toString(xx[0]) + " " + Float.toString(xx[1]) + " " + Float.toString(xx[2]) + " " + Float.toString(xx[3]));
                    //System.out.println(players.screenPresent[i]);
                    //System.out.println(players.screenAngle[i]);
                    //players.screenPresent[i]=true;
                }
                ball.updateXY(interpolatedPlay.dataBall.get(stage).get(frame));

                // Move paths to initial starting locations
                players.pathsMoveToPlayerPositions();
                ball.pathMoveToPosition();

            }

        }
        else{
            // Increment frame count within stage
            frame++;
        }

        ball.updateXY(interpolatedPlay.dataBall.get(stage).get(frame));
        ball.beingPassed = true;

        // Loop on players
        for (int playerIndex : interpolatedPlay.dataPlayers.keySet()){
            // Store previous XY coordinates
            float Xprevious = players.X[playerIndex];
            float Yprevious = players.Y[playerIndex];

            // Update player positions
            players.updateData(playerIndex, interpolatedPlay.getData(playerIndex, stage, frame));

            // Determine average XY coordinates for quadratic interpolation
            float Xavg = (Xprevious + players.X[playerIndex])/2;
            float Yavg = (Yprevious + players.Y[playerIndex])/2;

            // Update player paths using quadratic interpolation
            players.path[playerIndex].quadTo(Xprevious, Yprevious, Xavg, Yavg);

            // Determine if player has bought all or if ball is being passed
            boolean playerHasBall = ((ball.X == players.X[playerIndex])&&(ball.Y == players.Y[playerIndex]));
            ball.beingPassed = ball.beingPassed && !playerHasBall;
        }

        // If ball is being passed, then draw path
        if (ball.beingPassed){ball.path.lineTo(ball.X,ball.Y); }
        // If player has ball, then do not draw path
        else {ball.path.moveTo(ball.X,ball.Y);}

    }
}
