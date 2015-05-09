package uw.playdesigner6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by lybar_000 on 3/10/2015.
 */
public class PlayView extends View {

    //Define graphics variables
    private Paint paintPlayerCircle, paintCanvas, paintPlayerText, paintPath;
    private Canvas canvasPlay;


    private Bitmap[] iconPlayer = new Bitmap[PLAYER_COUNT];
    private Bitmap bitmapPlay;

    private Path pathPlayer;

    //Player definition constants
    private static int PLAYER_ICON_SIZE = 40;


    private static int selection_size = PLAYER_ICON_SIZE*1;
    private static int text_size = 30;
    private static int text_shift = text_size*6/20;

    //Define initial player positions
    private static int PLAYER_COUNT = 5;
    private static int[] INITIAL_X = new int[] {400, 100, 700, 250, 550};
    private static int[] INITIAL_Y = new int[] {200, 350, 350, 500, 500};
    public Player[] players = new Player[PLAYER_COUNT];
    private Map<String,List<String>> points;
    private Ball ball;
    private Hoop hoop;
    

    private List<Screen> screens = new ArrayList<Screen>();

    // Related to animation
    private Context context;
    private android.os.Handler handler;
    private final int FRAME_RATE = 30;

    private int pointIndex = 0;

    private GestureDetectorCompat gestureDetector;

    public boolean playExisting = false;
    private final int STAGE_LENGTH = 3;   //Time in seconds
    private int stageFrameCount = 0;
    private int currentStage = 0;
    private Play interpolatedPlay;
    private int FRAMES_PER_STAGE = FRAME_RATE*STAGE_LENGTH;

        //Instantiate view
    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Create handler for animation thread
        this.context = context;
        handler = new android.os.Handler();

        gestureDetector = new GestureDetectorCompat(context, new MyGestureListener());

        setupDrawing();

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
    public void setupDataPoints(Map<String,List<String>> points){
        this.points = points;
    }

    private void setupDrawing(){



        paintPlayerCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPlayerCircle.setStyle(Paint.Style.FILL);
        paintPlayerCircle.setColor(getResources().getColor(R.color.husky_purple));


        //Define text format
        paintPlayerText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPlayerText.setTextSize(text_size);
        paintPlayerText.setTextAlign(Paint.Align.CENTER);
        paintPlayerText.setColor(Color.WHITE);
        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);
        paintPlayerText.setTypeface(tf);

        //Define path format
        paintPath = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPath.setColor(getResources().getColor(R.color.husky_metallic_gold));
        paintPath.setAntiAlias(true);
        paintPath.setStrokeWidth(5);
        paintPath.setStyle(Paint.Style.STROKE);
        paintPath.setStrokeJoin(Paint.Join.ROUND);
        paintPath.setStrokeCap(Paint.Cap.ROUND);



        // Create canvas
        paintCanvas = new Paint(Paint.DITHER_FLAG);

        // Create path
        pathPlayer = new Path();


        // Create player icons, consisting of circle and player name/number
        for (int i = 0; i < PLAYER_COUNT; i++) {
            // Create array of player icons as bitmap
            iconPlayer[i] = Bitmap.createBitmap(PLAYER_ICON_SIZE, PLAYER_ICON_SIZE, Bitmap.Config.ARGB_8888);

            // Add canvas to each bitmap in array
            Canvas temporaryCanvas = new Canvas(iconPlayer[i]);

            // Draw circle on canvas, for player icon
            temporaryCanvas.drawCircle(PLAYER_ICON_SIZE/2, PLAYER_ICON_SIZE/2, PLAYER_ICON_SIZE/2, paintPlayerCircle);

            // Add player name/number two player icon
            temporaryCanvas.drawText(Integer.toString(i + 1), PLAYER_ICON_SIZE/2, PLAYER_ICON_SIZE/2 + text_shift,paintPlayerText);

            // Initialize players, including location, name, and selection status
            players[i]=new Player(INITIAL_X[i], INITIAL_Y[i], Integer.toString(i + 1), false);
        }



        initializeBall();

        initializeHoop();




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
        clearCanvas();

        //Repository for single XY coordinate
        List<String> data;
        
        //Loop on players
        for (int i=0; i<PLAYER_COUNT; i++) {
            // Place players in initial positions
            players[i]=new Player(INITIAL_X[i], INITIAL_Y[i], Integer.toString(i + 1), false);

            //Push initial positions into map
            data = points.get(Integer.toString(i+1));
            data.add(pointsToString(INITIAL_X[i],INITIAL_Y[i]));
        }
        initializeBall();

    }

    private void initializeBall(){
        // Initialize ball, including location and selection status
        ball = new Ball(INITIAL_X[0], INITIAL_Y[0], 0, false, false);
        ball.setContext(context);
        ball.createIcon();
        ball.createPath();

    }
    public void initializeHoop(){
        hoop = new Hoop();
        hoop.setContext(context);
        hoop.createIcon();
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
        canvas.drawPath(pathPlayer, paintPath);
        canvas.drawPath(ball.path,ball.paintPath);

        canvas.drawBitmap(ball.icon, ball.getX(), ball.getY(), null);

        // Update player icon positions
        for (int i = 0; i < PLAYER_COUNT; i++) {

            // Determine location of player icon, with offset
            float X = players[i].X-PLAYER_ICON_SIZE/2;
            float Y = players[i].Y-PLAYER_ICON_SIZE/2;

            // Draw player icons on canvas
            canvas.drawBitmap(iconPlayer[i], X, Y, null);
        }

        canvas.drawBitmap(hoop.icon, hoop.getX(), hoop.getY(), null);

        // Need to determine if necessary
        canvas.drawBitmap(bitmapPlay, 0, 0, paintCanvas);

//         canvas.drawBitmap(screen.icon,screen.X, screen.Y,screen.paint);
        int screenCount = screens.size();
        if (screenCount > 0) {
            boolean[]removalList = new boolean[screenCount];

            for (int i = 0; i < screenCount; i++) {
                screens.get(i).updateLocation(players[screens.get(i).playerIndex].X,
                                              players[screens.get(i).playerIndex].Y);

                Screen screen = screens.get(i);
                canvas.drawBitmap(screen.icon, screen.getX(), screen.getY(), null);
                removalList[i] =  ball.playerIndex==screens.get(i).playerIndex;

            }
            for (int i = screenCount-1; i >= 0; i--){
                if (removalList[i]){
                    screens.remove(i);
                }
            }

        }


        //Call in invalidate() using animation thread
        handler.postDelayed(runnable, FRAME_RATE);
    }







    //Capture and handle touch events during play recording
    // http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
    public boolean onTouchEvent(MotionEvent event) {
//         Toast.makeText(context,"Touch event", Toast.LENGTH_SHORT).show();
        // If replaying play, stop on touch
        stopPlay();

        // Let gesture detector inspect actions first
        //this.gestureDetector.onTouchEvent(event);

        boolean gestureDetected = gestureDetector.onTouchEvent(event);
        if (gestureDetected){
            return gestureDetected;
        }


        //Capture touch location
        Location locationTouch = locationProjected(event.getX(), event.getY());


        if (ball.selected) {
            updateBallPosition(event,locationTouch);

        }
        //Update player positions, If ball not selected
        else {
            for (int i = 0; i < PLAYER_COUNT; i++) {
                players[i] = updatePlayerPosition(event, locationTouch, players, i);
            }
        }

        return true;
    }

    // https://developer.android.com/training/gestures/detector.html
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());

            // Check distance between ball and touch event
            // TRUE = ball double tapped
            // FALSE = ball NOT double tapped
            boolean distanceCheckBall = (float) ball.icon.getWidth()/2 >
                            euclideanDistance(ball.X, event.getX(),ball.Y, event.getY());
            if (distanceCheckBall) {
                // Ball double tapped, so toggle selection state
                ball.selected = !ball.selected;

                // Update ball icon, based on new selection state
                ball.createIcon();

                //
                if (ball.playerIndex >= 0) {
                    ball.path.moveTo(players[ball.playerIndex].X, players[ball.playerIndex].Y);
                }
                else {
                    ball.path.moveTo(hoop.X, hoop.Y);
                }
            }

            for (int i = 0; i < PLAYER_COUNT; i++) {
                boolean distanceCheckScreen = (float) iconPlayer[i].getWidth()/2 > euclideanDistance(
                                                                    players[i].X, event.getX(),
                                                                    players[i].Y, event.getY());
                if (distanceCheckScreen && !distanceCheckBall){
                    createScreen(i);

                }

            }

            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
            return true;
        }


    }

    private void createScreen(int playerIndex){
        int screenCount = screens.size();
        boolean screenPresent = false;
        for (int i = 0; i < screenCount; i++) {
            if (playerIndex==screens.get(i).playerIndex){
                screens.remove(i);
                screenPresent = screenPresent || true;
                break;
            }
        }
        if (!screenPresent) {

            Screen screen = new Screen(players[playerIndex].X, players[playerIndex].Y, playerIndex, false);
            screen.setContext(context);
            screen.createIcon();

            screens.add(screen);
        }



    }
    //Clear canvas (Used to remove path)
    public void clearCanvas(){
        pathPlayer.reset();
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


    private void updateBallPosition(MotionEvent event, Location location){
        // Only modified ball if previously selected through double tap
        if (ball.selected){


            float distance;
            // Evaluate touch actions
            switch (event.getAction()) {

                //Touch MOVE
                case MotionEvent.ACTION_MOVE:
                        // Update ball location based on touch
                        ball.updateLocation(location.X, location.Y);
                    break;

                //Touch UP
                case MotionEvent.ACTION_UP:
                    for (int i = 0; i < PLAYER_COUNT; i++) {

                        distance = euclideanDistance(location.X, players[i].X,
                                                     location.Y, players[i].Y);

                        if (distance<(float)hoop.icon.getWidth()){
                            ball.updateBall(players[i].X, players[i].Y, i, false);
                        }
                        else {
                            if (ball.playerIndex >= 0) {
                                ball.updateLocation(players[ball.playerIndex].X, players[ball.playerIndex].Y);
                            }
                            else {
                                ball.updateLocation(hoop.X, hoop.Y);
                            }
                        }
                    }

                    distance = euclideanDistance(location.X, hoop.X,
                                                 location.Y, hoop.Y);

                    if (distance<(float)hoop.icon.getWidth()){
                        ball.updateBall(hoop.X, hoop.Y, -1, false);
                    }
                    else {
                        if (ball.playerIndex >= 0) {
                            ball.X = players[ball.playerIndex].X;
                            ball.Y = players[ball.playerIndex].Y;
                        }
                        else {
                            ball.X = hoop.X;
                            ball.Y = hoop.Y;
                        }
                    }
                    break;
                default:
            }
        }


    }


    //Update player position and selection status based on touch events
    private Player updatePlayerPosition(MotionEvent event, Location locationTouch, Player[] players, int playerIndex) {
        Player player = players[playerIndex];



        float distanceToPlayer = Float.POSITIVE_INFINITY;
        float minDistanceToOtherPlayers = Float.POSITIVE_INFINITY;
        boolean obstacleEncountered = false;

        for (int i = 0; i < PLAYER_COUNT; i++) {
            float distance = euclideanDistance(locationTouch.X, players[i].X, locationTouch.Y, players[i].Y);
            minDistanceToOtherPlayers = Math.min(distance,minDistanceToOtherPlayers);
            obstacleEncountered = obstacleEncountered || (minDistanceToOtherPlayers < (float)PLAYER_ICON_SIZE);

            if (i == playerIndex) {
                distanceToPlayer = euclideanDistance(locationTouch.X, players[playerIndex].X,
                        locationTouch.Y, players[playerIndex].Y);
            }
        }



//        Touch touchEval = touchEvaluation(locationTouch);

        //Create repository points traversed by player
        List<String> data = points.get(player.name);

        //Address each touch event
        switch (event.getAction()) {

            //Touch DOWN

            case MotionEvent.ACTION_DOWN:
                player.selectionStatus = (distanceToPlayer < ((float)PLAYER_ICON_SIZE)/2);
                if(player.selectionStatus) {
                    //pathPlayer.reset();
                    pathPlayer.moveTo(player.X, player.Y);
                    data.add(pointsToString(player.X, player.Y));
                }
                break;

            //Touch MOVE
            case MotionEvent.ACTION_MOVE:

                if(player.selectionStatus && !obstacleEncountered) {

                    //pathPlayer.lineTo(locationTouch.X, locationTouch.Y);
                    float X = (locationTouch.X + players[playerIndex].X)/2;
                    float Y = (locationTouch.Y + players[playerIndex].Y)/2;

                    pathPlayer.quadTo(players[playerIndex].X, players[playerIndex].Y, X, Y);

                    player.X=locationTouch.X;
                    player.Y=locationTouch.Y;
                    data.add(pointsToString(player.X, player.Y));
                    if (playerIndex==ball.playerIndex){
                        ball.X = locationTouch.X;
                        ball.Y = locationTouch.Y;

                    }
                }
                break;

            //Touch UP
            case MotionEvent.ACTION_UP:
                if(player.selectionStatus) {
                    //pathPlayer.quadTo(players[playerIndex].X,players[playerIndex].Y,locationTouch.X,locationTouch.Y);
                    pathPlayer.lineTo(players[playerIndex].X, players[playerIndex].Y);
                    player.selectionStatus = false;

                    data.add(pointsToString(player.X, player.Y));
                }
                break;
            default:
                //return false;
        }
        return player;
    }

    //Create string from points
    private String pointsToString(float x, float y){
        return "(" + Float.toString(x) + "," + Float.toString(y) + ")";
    }

    // Start play
    public void startPlay(Map<Integer,List<List<float[]>>> playMap){

        // Allow play update method to be called from onDraw
        playExisting = true;

        // Create play as Play, based on points imported from XML file
        Play originalPlay = new Play(playMap, 0, 0, 0, false);

        // Set current (initial) stage index to 0
        currentStage = 0;

        // Interpolate play, such that each stage of each player has the same number of points
        interpolatedPlay = interpolatePlay(originalPlay);
    }

    // Stop play replay
    public void stopPlay(){
        playExisting = false;
    }

    // Calculate Euclidean distance between points
    private float euclideanDistance(float x1, float x2, float y1, float y2) {
        return (float)Math.pow(Math.pow(x2 - x1, 2) + Math.pow(y2-y1, 2), 0.5);
    }

    /*private TouchEvaluation touchEvaluation(Location location){
        float[] distance = new float[PLAYER_COUNT];
        boolean[] distanceCheck = new boolean[PLAYER_COUNT];
        int playerSelected = -1;


        return new TouchEvaluation();
    }*/




    // Interpolate play, such that each stage of each player has the same number of points
    public Play interpolatePlay(Play originalPlay){
        Map<Integer,List<List<float[]>>> interpolatedMap = new HashMap<Integer,List<List<float[]>>>();

        List<List<float[]>> stageList;
        List<float[]> originalCoordinates;
        float[] XY = new float[2];
        List<float[]> previousCoordinates;
        List<float[]> interpolatedCoordinates;

        // Number of stages in play
        int stageCount = originalPlay.getStageCount();

   // Loop on players
        for (int playerIndex : originalPlay.pointMap.keySet()){

            // Data structure for all of the information for a single player
            stageList = new ArrayList<List<float[]>>();

            // Loop on stages
            for (int stageIndex = 0; stageIndex < stageCount; stageIndex++) {

                // Get XY coordinates for player
                // originalCoordinates = new ArrayList<float[]>();
                originalCoordinates = originalPlay.getXYlist(playerIndex, stageIndex);

                // Initialize list for interpolated coordinates
                interpolatedCoordinates = new ArrayList<float[]>();

                // Number of points for selected stage and player
                int pointCount = originalCoordinates.size();

                // If no points present in current stage, use last point from last stage
                if (pointCount < 1) {
                    XY = new float[2];
                    previousCoordinates = stageList.get(stageIndex-1);
                    XY[0] = previousCoordinates.get(previousCoordinates.size()-1)[0];
                    XY[1] = previousCoordinates.get(previousCoordinates.size()-1)[1];
                    interpolatedCoordinates = new ArrayList<>(Collections.nCopies(FRAMES_PER_STAGE, XY));

                }
                // Only 1 coordinate in current stage, so repeat value
                else if (pointCount == 1) {
                    XY = new float[2];
                    XY[0] = originalCoordinates.get(0)[0];
                    XY[1] = originalCoordinates.get(0)[1];
                    interpolatedCoordinates = new ArrayList<>(Collections.nCopies(FRAMES_PER_STAGE, XY));
                }
                // pointCount > 1
                else {

                    // Determine Euclidean distance traveled by points
                    float distance = 0;

                    // Array of distances traveled
                    // Same length as points (i.e. first value is 0, and last value is total length)
                    float[] totalDistance = new float[pointCount];
                    totalDistance[0] = 0;

                    for (int pointIndex = 0; pointIndex < pointCount-1; pointIndex++) {

                        // Calculate distance between i and i+1 points
                        distance = euclideanDistance(
                                originalCoordinates.get(pointIndex    )[0],
                                originalCoordinates.get(pointIndex + 1)[0],
                                originalCoordinates.get(pointIndex    )[1],
                                originalCoordinates.get(pointIndex + 1)[1]);

                        // Create running total of distance traveled
                        totalDistance[pointIndex + 1] = totalDistance[pointIndex] + distance;
                        System.out.println(Integer.toString(pointIndex) + " " + Float.toString(totalDistance[pointIndex]) );
                    }

                    // Loop on points for interpolated coordinates
                    //int pointIndex2 = 0;
                    for (int pointIndex = 0; pointIndex < FRAMES_PER_STAGE; pointIndex++) {
                        XY = new float[2];


                        // Interpolate points, given that there are at least 2 points

                        // Percent of way through interpolated stage
                        float fractionalIndex = (pointCount - 1) * pointIndex/(float)FRAMES_PER_STAGE;

                        // Distance traveled in stage, based on progression in stage
                        float distanceTarget = totalDistance[pointCount-1]*pointIndex/((float)(FRAMES_PER_STAGE-1));
                        if (distanceTarget > totalDistance[pointCount-1]){
                            distanceTarget = totalDistance[pointCount-1];
                        }


                        //System.out.println("PtInd2 "+Integer.toString(pointIndex2) + ", Targ "
                        //        + Float.toString(distanceTarget) + ", Dist0 " + Float.toString(totalDistance[pointIndex2])
                        //        + ", Dist1 " + Float.toString(totalDistance[pointIndex2 + 1]));

                        // Find adjacent points to interpolate, based on Target distance



                        // Find index preceding target value
                        int previousIndex = 0;
                        int pointIndex2 = 0;
                        while (totalDistance[pointIndex2] < distanceTarget){
                            previousIndex = pointIndex2;
                            pointIndex2++;
                        }

                        // Find index following target value
                        int nextIndex = 0;
                        pointIndex2 = pointCount-1;
                        while ((totalDistance[pointIndex2] >= distanceTarget)&&(pointIndex2 > 0)){
                            nextIndex = pointIndex2;
                            pointIndex2--;
                        }

                        //System.out.println("PtInd2 " + Integer.toString(pointIndex2) + ", Targ "
                        //        + Float.toString(distanceTarget) + ", Dist0 " + Float.toString(totalDistance[pointIndex2])
                        //        + ", Dist1 " + Float.toString(totalDistance[pointIndex2 + 1]));
                        float interpolationWeight = (distanceTarget - totalDistance[previousIndex])/
                                                    (totalDistance[nextIndex] - totalDistance[previousIndex]);

                        // XY coordinates preceding and following target
                        float[] previousXY = originalCoordinates.get(previousIndex);
                        float[] nextXY = originalCoordinates.get(nextIndex);

                        // Interpolated coordinate
                        XY[0] = previousXY[0] + (nextXY[0] - previousXY[0]) * interpolationWeight;
                        XY[1] = previousXY[1] + (nextXY[1] - previousXY[1]) * interpolationWeight;

                        // Add interpolated XY points to Coordinate list
                        interpolatedCoordinates.add(XY);
                    }

                }
                    // Add coordinate list to stage
                stageList.add(interpolatedCoordinates);

            }// End loop on stage
            // Add stages to player
            interpolatedMap.put(playerIndex, stageList);
        }// End loop on player

        // Create interpolated play based on original play
        Play interpolatedPlay = originalPlay;

        // Update point map based on interpolated values
        interpolatedPlay.pointMap = interpolatedMap;

        return interpolatedPlay;
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
            }
            // Reset frame counter within stage
            stageFrameCount = 0;
        }
        else{
            // Increment frame count within stage
            stageFrameCount++;
        }

        // Loop on players
        for (int playerIndex : interpolatedPlay.pointMap.keySet()){
            // Extract relevant XY coordinates
            float[] XY = interpolatedPlay.getXYcoordinate(playerIndex, currentStage, stageFrameCount);

            // Update player positions
            players[playerIndex-1].X = XY[0];
            players[playerIndex-1].Y = XY[1];
        }
    }
}
