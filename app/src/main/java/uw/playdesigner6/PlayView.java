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
    //private Bitmap court;
    private Court court;

    //Player definition constants
    private static int PLAYER_ICON_SIZE = 40;

    //private static int text_size = 30;

    //Define initial player positions
    private static int PLAYER_COUNT = 5;
    private static int[] INITIAL_X = new int[] {400, 100, 700, 250, 550};
    private static int[] INITIAL_Y = new int[] {200, 350, 350, 500, 500};
    private Players players;
    
    private boolean justPassed = false;

    //private Map<String,List<String>> points;

    private Map<Integer,List<List<float[]>>> dataPlayers;
    private List<Integer> dataBall = new ArrayList<Integer>();


    private Ball ball;
    private Hoop hoop;
    
    // Related to animation
    private Context context;
    private android.os.Handler handler;
    private final int FRAME_RATE = 30;

    private GestureDetectorCompat gestureDetector;

    private boolean playExisting = false;
    private final int STAGE_LENGTH = 3;   //Time in seconds

    private int frame = 0;
    private int stage = 0;
    private int playPauseCount = 0;
    private PlayInterpolated playInterpolated;
    private int FRAMES_PER_STAGE = FRAME_RATE*STAGE_LENGTH;

    private Playing playing;
    private PlayElements playElements;

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

        // Create court
        court = new Court(context);

        // Create players
        players = new Players(context, court);

        // Create ball
        ball = new Ball(0, context, court);

        // Create hoop
        hoop = new Hoop(context, court);




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



    // Increment stage (called by Main on button click)
    public void incrementStage(){
        stage++;
        dataBall.add(ball.playerIndex);
        addStageToMap();
        System.out.println("incremented stage ");
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
            //updatePlay();
            playElements = playing.updatePlay(playInterpolated,playElements);
            //pointIndex++;
        }

        // Draw court
        canvas.drawBitmap(court.bitmap, 0, 0, null);

        // Draw ball path
        canvas.drawPath(ball.path, ball.paintPath);

        // Draw ball
        canvas.drawBitmap(ball.icon, ball.getX(), ball.getY(), null);

        // Draw players
        players.updateCanvas(canvas);


        //Call in invalidate() using animation thread
        handler.postDelayed(runnable, FRAME_RATE);
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
                // Player moved

                float x1 = players.getCurrentData(playerIndex)[0];
                float y1 = players.getCurrentData(playerIndex)[1];
                int z = dataPlayers.get(playerIndex).get(stage).size()-1;
                float x2 = dataPlayers.get(playerIndex).get(stage).get(z)[0];
                float y2 = dataPlayers.get(playerIndex).get(stage).get(z)[1];

                //if (justPassed &&((x1 != x2) && (y1 != y2))){
                if (justPassed) {
                    System.out.println( "ball not selected, increment stage" );
                    incrementStage();
                }
                justPassed = false;

                // Player position changed
                dataPlayers.get(playerIndex).get(stage).add(players.getCurrentData(playerIndex));
            }
        }

        // Update ball based on touch event

        // Previous player with ball
        int previousPlayerWithBall = ball.playerIndex;

        // Update the ball position, based on positional player with ball
        ball.updateBallPosition(event, locationTouch, players, hoop);

        // Current player with ball
        int currentPlayerWithBall = ball.playerIndex;

        // Ball changed hands?
        if (previousPlayerWithBall != currentPlayerWithBall){

            // Remove screen symbol if player has ball
            players.updateScreenState2(ball);

            // Ball is passed, so increment stage
            incrementStage();
            justPassed = true;

        }
        //updateBallPosition(event, locationTouch);
        //screensObject.updateScreenPositions(players, ball);

        return true;
    }

    // https://developer.android.com/training/gestures/detector.html
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        //private static final String DEBUG_TAG = "Gestures";

        // Double tap touch event
        @Override
        public boolean onDoubleTap(MotionEvent event) {
            // Location of touch event
            Location locationTouch = locationProjected(event.getX(), event.getY());

            // Update ball selection status
            Boolean distanceCheckBall = ball.updateStatus(players, hoop, locationTouch);

            // Update screen state (dispayed or hidden)
            players.updateScreenState(event, ball);

            return true;
        }

        // Elements that compose double tap touch event
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
        ball.pathMoveToPosition();
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
    public void startPlay(PlayData playDataMap){

        // Allow play update method to be called from onDraw
        playExisting = true;

        // Create play as Play, based on points imported from XML file
        PlayData originalPlayData = playDataMap;

        // Interpolate play, such that each stage of each player has the same number of points
        playInterpolated = new PlayInterpolated(originalPlayData, court);

        playElements = new PlayElements(players, ball);
        playing = new Playing();
        playing.startPlay(playInterpolated, playElements);
    }

    // Stop play replay
    public void stopPlay(){
        playExisting = false;
    }

}
