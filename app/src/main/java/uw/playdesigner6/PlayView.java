package uw.playdesigner6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lybar_000 on 3/10/2015.
 */
public class PlayView extends View {

    //Define graphics variables
    private Paint paint_circle, paint_canvas, paint_text, paint_path;
    private Canvas canvasPlay;
    private Bitmap bitmap_play;
    private Path path_play;

    //Player definition constants
    private int player_size = 20;
    private int selection_size = player_size*1;
    private int text_size = 30;
    private int text_shift = text_size*6/20;

    //Define initial player positions
    private static int PLAYER_COUNT = 5;
    private static int[] INITIAL_X = new int[] {400, 100, 700, 250, 550};
    private static int[] INITIAL_Y = new int[] {200, 350, 350, 500, 500};
    public Player[] players = new Player[PLAYER_COUNT];
    private Map<String,List<String>> points;

    // Related to animation
    private Context context;
    private android.os.Handler handler;
    private final int FRAME_RATE = 30;
    int x = this.getWidth()/2;
    int y = -1;
    private int xVelocity = 5;
    private int yVelocity = 5;
    private int pointIndex = 0;
    private Play newPlay;
    private Play play;

    public boolean playExisting = false;


    //Instantiate view
    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Related to animation
        this.context = context;
        handler = new android.os.Handler();


        setupDrawing();

/*        System.out.println( "this is a test of the resource accessing capabilities" );
        System.out.println(context.getString(R.string.buttonOkay));
        System.out.println(context.getResources().getStringArray());*/
    }

    // Related to animation
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    //Set up hash map to store position information
    public void setupDataPoints(Map<String,List<String>> points){
        this.points = points;
    }

    private void setupDrawing(){
        //Define circle format
        paint_circle = new Paint();
        paint_circle.setStyle(Paint.Style.FILL);
        paint_circle.setColor(getResources().getColor(R.color.husky_purple));

        //Define text format
        paint_text = new Paint();
        paint_text.setTextSize(text_size);
        paint_text.setTextAlign(Paint.Align.CENTER);
        paint_text.setColor(Color.WHITE);
        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);
        paint_text.setTypeface(tf);

        //Define path format
        paint_path = new Paint();
        paint_path.setColor(getResources().getColor(R.color.husky_metallic_gold));
        paint_path.setAntiAlias(true);
        paint_path.setStrokeWidth(5);
        paint_path.setStyle(Paint.Style.STROKE);
        paint_path.setStrokeJoin(Paint.Join.ROUND);
        paint_path.setStrokeCap(Paint.Cap.ROUND);

        //
        paint_canvas = new Paint(Paint.DITHER_FLAG);
        path_play = new Path();




    }

    //Create and display blank canvas
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap_play = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvasPlay = new Canvas(bitmap_play);
    }

    //Initial insertion of players onto court
    public void initialPlayerInsert(){
        //Repository for single XY coordinate
        List<String> data;
        
        //Loop on players
        for (int i=0; i<PLAYER_COUNT; i++) {
            //Define player initial positions
            String name = Integer.toString(i+1);
            players[i]=new Player(INITIAL_X[i], INITIAL_Y[i], name, false);
            
            //Draw players
            drawPlayer(players[i]);

            //Push initial positions into map
            data = points.get(name);
            data.add(pointsToString(INITIAL_X[i],INITIAL_Y[i]));
        }

    }

    @Override
    // onDraw is called when:
    // 1) Initial draw of view
    // 2) Function invalidate()

    protected void onDraw(Canvas canvas) {
        pointIndex++;
        //temporary();

        if (playExisting) {
            bouncingBall(canvas);
            boolean playStatus = updatePlay();
//             updatePlay();
        }

        canvas.drawBitmap(bitmap_play, 0, 0, paint_canvas);
        canvas.drawPath(path_play, paint_path);



        handler.postDelayed(runnable, FRAME_RATE);
        //handler.post(runnable);


    }



    private void bouncingBall(Canvas canvas){

        BitmapDrawable box = (BitmapDrawable) context.getResources().getDrawable(R.drawable.basketball50);

        Random r = new Random();
        x = r.nextInt(10)+30;
        if ( y <0) {
            y = this.getHeight()/2;
        } else {
            y += yVelocity;
            if (y < 0 || (y > this.getHeight() - box.getBitmap().getHeight())) {
                yVelocity = yVelocity*-1;
            }
        }


        canvas.drawBitmap(box.getBitmap(), x, y, null);

    }

    //Capture and handle touch events during play recording
    public boolean onTouchEvent(MotionEvent event) {

        stopPlay();


        //Capture touch location
        Location locationTouch = locationProjected(event.getX(), event.getY());

        //Update player positions
        for (int i=0; i<PLAYER_COUNT; i++) {
            players[i] = positionUpdate(event,locationTouch, players[i]);
        }

        //Clear the canvas
        clear_canvas();

        //Draw players on canvas
        for (int i=0; i<PLAYER_COUNT; i++) {
            drawPlayer(players[i]);
        }

        //Draw path from most recent player move
        canvasPlay.drawPath(path_play, paint_path);

        //Invalidate canvas, causing it to redraw
        //invalidate();

        return true;
    }

    //Draw individual player
    private void drawPlayer(Player player){
        canvasPlay.drawCircle(player.X, player.Y, player_size, paint_circle);
        canvasPlay.drawText(player.name, player.X, player.Y + text_shift,paint_text);
    }

    //Clear canvas
    public void clear_canvas(){
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


    //Update player position and selection status based on touch events
    private Player positionUpdate(MotionEvent event, Location locationTouch, Player player) {
        double delta_X = locationTouch.X - player.X;
        double delta_Y = locationTouch.Y - player.Y;
        double distance = Math.pow(Math.pow(delta_X, 2) + Math.pow(delta_Y, 2), 0.5);

        //Create repository points traversed by player
        List<String> data = points.get(player.name);



        //Address each touch event
        switch (event.getAction()) {

            //Touch DOWN
            case MotionEvent.ACTION_DOWN:
                player.selection_status = (distance < selection_size);
                if(player.selection_status) {
                    path_play.moveTo(player.X, player.Y);
                    data.add(pointsToString(player.X, player.Y));
                }
                break;

            //Touch MOVE
            case MotionEvent.ACTION_MOVE:
                if(player.selection_status) {
                    path_play.lineTo(locationTouch.X, locationTouch.Y);
                    player.X=locationTouch.X;
                    player.Y=locationTouch.Y;
                    data.add(pointsToString(player.X, player.Y));
                }
                break;

            //Touch UP
            case MotionEvent.ACTION_UP:
                if(player.selection_status) {
                    player.X=locationTouch.X;
                    player.Y=locationTouch.Y;
                    player.selection_status = false;
                    path_play.reset();
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

    public void temporary(){

        for (int i=0; i<PLAYER_COUNT; i++) {
            //Define player initial positions
            String name = Integer.toString(i+1);
            players[i]=new Player(INITIAL_X[i], INITIAL_Y[i], name, false);

            //Draw players
            drawPlayer(players[i]);
        }

        for (int playerIndex=0; playerIndex<PLAYER_COUNT; playerIndex++) {
            //players[playerIndex]=new Player(X, Y, playerName, false);


            // for (int pointIndex = 0; pointIndex < 1000; pointIndex++) {
                float X = playerIndex+pointIndex;
                float Y = playerIndex*2+pointIndex;

//                 System.out.println(Float.toString(X) + ", "  + Float.toString(Y));
                players[playerIndex].X = X;
                players[playerIndex].Y = Y;

                //Clear the canvas
                clear_canvas();

                //Draw players on canvas
//                 for (int i = 0; i < PLAYER_COUNT; i++) {
                    drawPlayer(players[playerIndex]);
//                 }
                //handler.post(runnable);
                //handler.postDelayed(runnable, FRAME_RATE);
                //Invalidate canvas, causing it to redraw
                //invalidate();
                //SystemClock.sleep(5);
//             }

        }
        pointIndex++;
        if (pointIndex > 1000){
            pointIndex = 0;
        }



    }

    public void startPlay(Map<String,List<String>> currentPlay){
        
        playExisting = true;

        // Get list of keys
        Set<String> keys = currentPlay.keySet();

        // Get number of stages in play
        int stageCount = 0;
        for (String key : keys){
            List<String> data = currentPlay.get(key);
            stageCount = data.size();
            break;
        }

        play = new Play(currentPlay, stageCount, 0, 0, false);

        // Initialize player objects and position
        for (int i=0; i<PLAYER_COUNT; i++) {
            //Define player initial positions
            String name = Integer.toString(i+1);
            players[i]=new Player(INITIAL_X[i], INITIAL_Y[i], name, false);

            //Draw players
            drawPlayer(players[i]);
        }

    }

    public void stopPlay(){
        playExisting = false;

    }


    public boolean updatePlay(){

        List<float[]> coordinatesAsList;

        //Clear the canvas
        clear_canvas();

        // Loop on players
        for (int playerIndex = 0; playerIndex < PLAYER_COUNT; playerIndex++) {

            // Get player number as string
            String playerName = Integer.toString(playerIndex+1);
            System.out.println("Stage: " + Integer.toString(play.currentStage) + ", " + "player: " + playerName);



pointIndex++;
            float X = playerIndex+pointIndex;
            float Y = playerIndex*2+pointIndex;

//                 System.out.println(Float.toString(X) + ", "  + Float.toString(Y));
            players[playerIndex].X = X;
            players[playerIndex].Y = Y;


            //Draw players on canvas
//                 for (int i = 0; i < PLAYER_COUNT; i++) {
            drawPlayer(players[playerIndex]);

        }
            // Extract X and Y coordinates
//             List<String> data = play.points.get(playerName);
//             System.out.println( data );
            // String pointsAsString = data.get(play.currentStage);
            // String[] pointsAsStringArray = pointsAsString.split("['(,)]+");



/*                for (int pointIndex = 0; pointIndex < pointsAsStringArray.length; pointIndex++) {
                String pointAsString = pointsAsStringArray[pointIndex];
                //System.out.println(pointAsString);

            }*/
            //System.out.println("pointsAsString: " + pointsAsString);
//             coordinatesAsList = parseCoordinates(pointsAsString);
//             int coordinateCount = coordinatesAsList.size();
            //System.out.println( "list size:" + coordinateCount);


//            if (coordinateCount > 0) {
//                //System.out.println("first point"  +  Float.toString(coordinatesAsList.get(0)[0]));
////                     for (int pointIndex = 0; pointIndex < coordinateCount; pointIndex++) {
//
//              //      System.out.println(play.currentPoint);
//                    float[] coordinatesAsNumber = coordinatesAsList.get(play.currentPoint);
//                    float X = coordinatesAsNumber[0];
//                    float Y = coordinatesAsNumber[1];
//
//                    System.out.println(playerName + ": " + Float.toString(X) + ", " + Float.toString(Y));
//
//
//                    //players[playerIndex]=new Player(X, Y, playerName, false);
//                    players[playerIndex].X = X;
//                    players[playerIndex].Y = Y;
//
//                    //Clear the canvas
//                    clear_canvas();
//
//                    //Draw players on canvas
//                    for (int i=0; i<PLAYER_COUNT; i++) {
//                        drawPlayer(players[i]);
//                    }
//
//                    //Invalidate canvas, causing it to redraw
//                    //invalidate();
//                    //SystemClock.sleep(10);
//                    //System.out.println(play.currentPoint);
//
//                }
//            }
//
////             }


        return true;
    }

    private List<float[]> parseCoordinates(String coordinatesAsString){


        List<float[]> output = new ArrayList<float[]>();

        // Determine if coordinates are present
        boolean coordinatesPresent = !coordinatesAsString.equals("'[]'");

        if (coordinatesPresent){

            // Remove single quotes and brackets
            Pattern p = Pattern.compile("[\\['\\]]");
            Matcher m = p.matcher(coordinatesAsString);
            coordinatesAsString = m.replaceAll("");

            // Split string into array
            // (?<=\\)) positive lookbehind means that it must be preceded by )
            // (?=\\() positive lookahead means that it must be suceeded by (
            // (,\\s*) means that it must be splitted on the , and any space after that
            String[] coordinatesAsStringArray = coordinatesAsString.split("(?<=\\))(,\\s*)(?=\\()");

            // Pattern for removing parentheses
            p = Pattern.compile("[\\(\\)]");


            int coordinateCount = coordinatesAsStringArray.length;

            // Loop on pairs of coordinates
            for (int i = 0; i < coordinateCount; i++) {
                // Set of coordinates in format (x,y)
                String[] currentCoordinate = coordinatesAsStringArray[i].split(",\\s*");

                // Extract x-coordinate as float, note that leading ( is removed
                m = p.matcher(currentCoordinate[0]);
                float x = Float.parseFloat(m.replaceAll(""));

                // Extract y-coordinate as float, note that leading ) is removed
                m = p.matcher(currentCoordinate[1]);
                float y = Float.parseFloat(m.replaceAll(""));

                 //System.out.println( "coordinates: " + Float.toString(x)+", "+Float.toString(y));

                // Add coordinates to output
                float[] coordinatesAsNumber = new float[2];

                       coordinatesAsNumber[0] = x;
                coordinatesAsNumber[1] = y;

                //System.out.println( "coordinates[]: " + Float.toString(coordinatesAsNumber[0])+", "+Float.toString(coordinatesAsNumber[1]));
                output.add(coordinatesAsNumber);

                //System.out.println("output1 :" + Float.toString(output.get(i)[0]) + " " + Float.toString(output.get(i)[1]));

            }
            //System.out.println("output2: " + Float.toString(output.get(0)[0]) + " " + Float.toString(output.get(0)[1]));
            //for (int i1 = 0; i1 < output.size(); i1++) {
//                System.out.println("output2: " + Float.toString(output.get(i1)[0]) + " " + Float.toString(output.get(i1)[1]));
//            }
        }

        return output;
    }


}
