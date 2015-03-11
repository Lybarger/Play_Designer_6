package uw.playdesigner6;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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




    //canvas bitmap

    private Bitmap canvasBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_play);

        play_view = (PlayView)findViewById(R.id.play);

        //Defined buttons
        button_create_new = (Button)findViewById(R.id.button_create_new_play);
        button_initialization_complete = (Button)findViewById(R.id.button_initialization_complete);
        button_increment_stage = (Button)findViewById(R.id.button_increment_stage);
        button_play_complete = (Button)findViewById(R.id.button_play_complete);



        //Define current state
        current_state= state_list.SPLASH;
        update_button_state(current_state);

        //text_status = (TextView)findViewById(R.id.summary_info);
        //text_status.setText("I hope this works");
        //onViewChange();


    }




    public void onViewChange(){
        text_status.setText("I hope partial success");

    }


/*        bitmap_play = Bitmap.createBitmap(image_play.getWidth(), 500, Bitmap.Config.ARGB_8888);
        canvas_play = new Canvas(bitmap_play);
        canvas_play.drawCircle(50, 100, 100,  paint_circle);
        image_play.setImageBitmap(bitmap_play);
        //image_play.invalidate();*/





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_play, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
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
    }

    public void on_click_initialization_complete(View view)
    {
        current_state= state_list.INCREMENTING;
        update_button_state(current_state);
    }

    public void on_click_increment_stage(View view)
    {

    }

    public void on_click_play_complete(View view)
    {

        current_state= state_list.COMPLETE;
        update_button_state(current_state);

    }


    public void update_button_state(String current_state) {
        if (current_state == state_list.SPLASH) {
            //Set the button state
            button_create_new.setEnabled(true);
            button_initialization_complete.setEnabled(false);
            button_increment_stage.setEnabled(false);
            button_play_complete.setEnabled(false);
        }
        else if (current_state == state_list.INITIALIZING){
            //Set the button state
            button_create_new.setEnabled(false);
            button_initialization_complete.setEnabled(true);
            button_increment_stage.setEnabled(false);
            button_play_complete.setEnabled(false);
        }
        else if (current_state == state_list.INCREMENTING){
            //Set the button state
            button_create_new.setEnabled(false);
            button_initialization_complete.setEnabled(false);
            button_increment_stage.setEnabled(true);
            button_play_complete.setEnabled(true);
        }
        else if (current_state == state_list.COMPLETE){
            //Set the button state
            button_create_new.setEnabled(false);
            button_initialization_complete.setEnabled(false);
            button_increment_stage.setEnabled(false);
            button_play_complete.setEnabled(false);
        }
    }
}
