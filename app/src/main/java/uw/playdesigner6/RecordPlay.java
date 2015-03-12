package uw.playdesigner6;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RecordPlay extends ActionBarActivity{

    private Paint paint_circle;
    private ImageView image_court, image_play;
    private Canvas canvas_play;
    private Bitmap bitmap_play;
    private PlayView play_view;
    private TextView text_status;
    private MyListener mListener;
    private final states state_list = new states("","", "", "");
    private String current_state;
    private Button button_create_new;
    private Button button_initialization_complete;
    private Button button_increment_stage;
    private Button button_play_complete;
    private TextView text_play_name;
    private TextView text_initialization;
    private TextView text_current_stage;
    private TextView text_play_complete;

    private int current_stage = 0;

    private Map<String,List<String>> dataPoints;




    //canvas bitmap

    private Bitmap canvasBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_play);

        play_view = (PlayView)findViewById(R.id.play);

        //Define buttons
        button_create_new = (Button)findViewById(R.id.button_create_new_play);
        button_initialization_complete = (Button)findViewById(R.id.button_initialization_complete);
        button_increment_stage = (Button)findViewById(R.id.button_increment_stage);
        button_play_complete = (Button)findViewById(R.id.button_play_complete);

        //Defined texts
        text_play_name = (TextView)findViewById(R.id.value_play_name);
        text_initialization = (TextView)findViewById(R.id.value_initialization);
        text_current_stage = (TextView)findViewById(R.id.value_increment);
        text_play_complete = (TextView)findViewById(R.id.value_play_complete);


        //Define current state
        current_state= state_list.SPLASH;
        update_button_state(current_state);


        //onViewChange();
        dataPoints = new HashMap<String,List<String>>();
        List<String> data1 = new ArrayList<String>();
        List<String> data2 = new ArrayList<String>();
        List<String> data3 = new ArrayList<String>();
        List<String> data4 = new ArrayList<String>();
        List<String> data5 = new ArrayList<String>();

        dataPoints.put("1",data1);
        dataPoints.put("2",data2);
        dataPoints.put("3",data3);
        dataPoints.put("4",data4);
        dataPoints.put("5",data5);
        play_view.setupDataPoints(dataPoints);
    }


    public void printMap(){
        Set<String> keys = dataPoints.keySet();
        for (String key : keys) {
            System.out.println("Player " + key +" : ");
            List<String> points = dataPoints.get(key);
            for (String point : points){
                System.out.print(point);
            }
        }
    }

    public void clearMap(){
        Set<String> keys = dataPoints.keySet();
        for (String key : keys){
            List<String> points = dataPoints.get(key);
            points.clear();
        }
    }

    public void onViewChange(){
        text_status.setText("I hope partial success");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button_shape_enabled, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class states {
        String SPLASH;
        String INITIALIZING;
        String INCREMENTING;
        String COMPLETE;
        states(String SPLASH_1, String INITIALIZING_1, String INCREMENTING_1, String COMPLETE_1) {
            SPLASH = "SPLASH";
            INITIALIZING = "INITIALIZING";
            INCREMENTING = "INCREMENTING";
            COMPLETE = "COMPLETE";
        }

    }

    public void on_click_create_new_play(View view)
    {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("New Play");
        alert.setMessage("Enter play name");

// Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                // Do something with value!
                text_play_name.setText(value);
                current_state= state_list.INITIALIZING;
                update_button_state(current_state);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

        play_view.initial_player_insert();
    }

    public void on_click_initialization_complete(View view)
    {
        text_initialization.setText("complete");
        current_state= state_list.INCREMENTING;
        update_button_state(current_state);
        current_stage=1;
        text_current_stage.setText(Integer.toString(current_stage));
    }

    public void on_click_increment_stage(View view)
    {
        current_stage++;
        text_current_stage.setText(Integer.toString(current_stage));
    }

    public void on_click_play_complete(View view)
    {
        text_play_complete.setText("complete");
        current_state= state_list.COMPLETE;
        play_view.clear_canvas();

        update_button_state(current_state);
        String output = write_play();
        System.out.println(output);
        write_to_file("filename_1.XML", output);

        //printMap();
        //clearMap();
        //printMap();
    }


    public void update_button_state(String current_state) {
        if (current_state == state_list.SPLASH) {
            //Set the button_shape_enabled state
            button_create_new.setEnabled(true);
            button_initialization_complete.setEnabled(false);
            button_increment_stage.setEnabled(false);
            button_play_complete.setEnabled(false);
        }
        else if (current_state == state_list.INITIALIZING){
            //Set the button_shape_enabled state
            button_create_new.setEnabled(false);
            button_initialization_complete.setEnabled(true);
            button_increment_stage.setEnabled(false);
            button_play_complete.setEnabled(false);
        }
        else if (current_state == state_list.INCREMENTING){
            //Set the button_shape_enabled state
            button_create_new.setEnabled(false);
            button_initialization_complete.setEnabled(false);
            button_increment_stage.setEnabled(true);
            button_play_complete.setEnabled(true);
        }
        else if (current_state == state_list.COMPLETE){
            //Set the button_shape_enabled state
            button_create_new.setEnabled(true);
            button_initialization_complete.setEnabled(false);
            button_increment_stage.setEnabled(false);
            button_play_complete.setEnabled(false);
        }
    }




    public void write_to_file(String filename, String string){
        /* Checks if external storage is available for read and write */

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //https://xjaphx.wordpress.com/2011/10/27/android-xml-adventure-create-write-xml-data/
    public String write_play() {
        String format =
                "<?xml version='1.0' encoding='UTF-8'?>" +
                        "<record>" +
                        "   <study id='%s'>" +
                        "       <topic>%s</topic>" +
                        "       <content>%s</content>" +
                        "       <author>%s</author>" +
                        "       <date>%s</date>" +
                        "   </study>" +
                        "</record>";
        return String.format(format, "a", "B", "c", "d", "c");

    }
}
