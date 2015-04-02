package uw.playdesigner6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lybar_000 on 3/10/2015.
 */
public class PlayView extends View {

    //Define graphics variables
    private Paint paint_circle, paint_canvas, paint_text, paint_path;
    private Canvas canvas_play;
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

    //Instantiate view
    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

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
        canvas_play = new Canvas(bitmap_play);
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
            data.add("(" + INITIAL_X + "," + INITIAL_Y + ")");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap_play, 0, 0, paint_canvas);
        canvas.drawPath(path_play, paint_path);
    }

    //Capture and handle touch events during play recording
    public boolean onTouchEvent(MotionEvent event) {

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
        canvas_play.drawPath(path_play, paint_path);

        //Invalidate canvas, causing it to redraw
        invalidate();

        return true;
    }

    //Draw individual player
    private void drawPlayer(Player player){
        canvas_play.drawCircle(player.X, player.Y, player_size, paint_circle);
        canvas_play.drawText(player.name, player.X, player.Y + text_shift,paint_text);
    }

    //Clear canvas
    public void clear_canvas(){
        canvas_play.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
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

}
