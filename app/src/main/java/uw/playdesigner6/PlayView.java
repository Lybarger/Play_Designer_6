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
    private Map<String,List<String>> points;

    //Player definition constants
    private int player_size = 20;
    private int selection_size = player_size*1;
    private int text_size = 30;
    private int text_shift = text_size*6/20;

    //Initialize player locations
    private static int x_1 = 400;
    private static int y_1 = 200;
    private static int x_2 = 100;
    private static int y_2 = 350;
    private static int x_3 = 700;
    private static int y_3 = 350;
    private static int x_4 = 250;
    private static int y_4 = 500;
    private static int x_5 = 550;
    private static int y_5 = 500;

    public player player_1 = new player(x_1, y_1, "1", false);
    public player player_2 = new player(x_2, y_2, "2", false);
    public player player_3 = new player(x_3, y_3, "3", false);
    public player player_4 = new player(x_4, y_4, "4", false);
    public player player_5 = new player(x_5, y_5, "5", false);


    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
        //text_summary = (TextView)findViewById(R.id.summary_info);
        //text_summary.setText("testing");

        //paint_circle = new Paint();
        //paint_circle.setStyle(Paint.Style.FILL);
        //paint_circle.setColor(Color.BLUE);


        //image_court = (ImageView)findViewById(R.id.court);
        //image_play = (ImageView)findViewById(R.id.play);


    }

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
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);
        bitmap_play = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas_play = new Canvas(bitmap_play);




    }

    public void initial_player_insert(){
        draw_player(player_1);
        draw_player(player_2);
        draw_player(player_3);
        draw_player(player_4);
        draw_player(player_5);


        List<String> data = points.get("1");
        data.add("(" + x_1 + "," + y_1 + ")");
        data = points.get("2");
        data.add("(" + x_2 + "," + y_2 + ")");
        data = points.get("3");
        data.add("(" + x_3 + "," + y_3 + ")");
        data = points.get("4");
        data.add("(" + x_4 + "," + y_4 + ")");
        data = points.get("5");
        data.add("(" + x_5 + "," + y_5 + ")");

    }



    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap_play, 0, 0, paint_canvas);
        canvas.drawPath(path_play, paint_path);

    }

    public boolean onTouchEvent(MotionEvent event) {

        location location_touch = location_projected(event.getX(), event.getY());

        player_1 = position_update(event, location_touch, player_1);
        player_2 = position_update(event, location_touch, player_2);
        player_3 = position_update(event, location_touch, player_3);
        player_4 = position_update(event, location_touch, player_4);
        player_5 = position_update(event, location_touch, player_5);


        //canvas_play.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        clear_canvas();

        draw_player(player_1);
        draw_player(player_2);
        draw_player(player_3);
        draw_player(player_4);
        draw_player(player_5);

        canvas_play.drawPath(path_play, paint_path);

        invalidate();
        return true;
    }

    private void draw_player(player player){
        canvas_play.drawCircle(player.X, player.Y, player_size, paint_circle);
        canvas_play.drawText(player.name, player.X, player.Y + text_shift,paint_text);
    }

    public void clear_canvas(){
        canvas_play.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
    class location{
        float X;
        float Y;
        location(float X_temp, float Y_temp){
            X = X_temp;
            Y = Y_temp;
        }
    }

    private location location_projected(float x, float y){
        float x_projected = x;
        float y_projected = y;
        return new location(x_projected, y_projected);

    }

    class player {
        float X;
        float Y;
        String name;
        Boolean selection_status;

        player(float X_temp, float Y_temp , String name_temp, Boolean selection_status_temp){
            X = X_temp;
            Y = Y_temp;
            name = name_temp;
            selection_status = selection_status_temp;
        }
    }

    private player position_update(MotionEvent event, location location_touch, player player) {
        double delta_X = location_touch.X - player.X;
        double delta_Y = location_touch.Y - player.Y;
        double distance = Math.pow(Math.pow(delta_X, 2) + Math.pow(delta_Y, 2), 0.5);
        // List<String> data = points.get(player.name);
        List<String> data = points.get(player.name);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                player.selection_status = (distance < selection_size);
                if(player.selection_status) {
                    path_play.moveTo(player.X, player.Y);
                    data.add("(" + player.X + "," + player.Y + ")");
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(player.selection_status) {
                    path_play.lineTo(location_touch.X, location_touch.Y);
                    player.X=location_touch.X;
                    player.Y=location_touch.Y;
                    data.add("(" + player.X + "," + player.Y + ")");
                }
                break;
            case MotionEvent.ACTION_UP:
                if(player.selection_status) {
                    player.X=location_touch.X;
                    player.Y=location_touch.Y;
                    player.selection_status = false;
                    path_play.reset();
                    data.add("(" + player.X + "," + player.Y + ")");
                }
                break;
            default:
                //return false;
        }
        return player;

    }

}
