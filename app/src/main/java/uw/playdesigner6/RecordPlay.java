package uw.playdesigner6;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private final states state_list = new states("","", "", "", "");
    private String current_state;
    private Button button_play_existing;
    private Button button_create_new;
    private Button button_initialization_complete;
    private Button button_increment_stage;
    private Button button_play_complete;
    private TextView text_play_name;
    private TextView text_initialization;
    private TextView textCurrentStage;
    private TextView text_play_complete;
    private String play_filename;
    private String play_as_string;
    private static String xml_header = "<?xml version='1.0' encoding='UTF-8'?>" + "\n";
    private int currentStage = 1;

    private Map<String,List<String>> dataPoints;




    //canvas bitmap

    private Bitmap canvasBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_play);

        play_view = (PlayView)findViewById(R.id.play);

        //Define buttons
        button_play_existing = (Button)findViewById(R.id.button_play_existing);
        button_create_new = (Button)findViewById(R.id.button_create_new_play);
        button_initialization_complete = (Button)findViewById(R.id.button_initialization_complete);
        button_increment_stage = (Button)findViewById(R.id.button_increment_stage);
        button_play_complete = (Button)findViewById(R.id.button_play_complete);

        //Defined texts
        text_play_name = (TextView)findViewById(R.id.value_play_name);
        text_initialization = (TextView)findViewById(R.id.value_initialization);
        textCurrentStage = (TextView)findViewById(R.id.value_increment);
        text_play_complete = (TextView)findViewById(R.id.value_play_complete);


        //Define current state
        current_state= state_list.SPLASH;
        updateButtonState(current_state);


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
            System.out.println("\nPlayer " + key +" : \n");
            List<String> points = dataPoints.get(key);
            for (String point : points){
                System.out.print(point);
            }
        }
    }

    public void mapClear(){
        Set<String> keys = dataPoints.keySet();
        for (String key : keys){
            List<String> points = dataPoints.get(key);
            points.clear();
        }
    }

    public void mapLastValue(){
        Set<String> keys = dataPoints.keySet();
        for (String key : keys){
            List<String> points = dataPoints.get(key);
            String last_entry = points.get(points.size()-1);
            points.clear();
            points.add(last_entry);

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
        String REPLAY;
        String INITIALIZING;
        String INCREMENTING;
        String COMPLETE;
        states(String SPLASH_1, String REPLAY_1, String INITIALIZING_1, String INCREMENTING_1, String COMPLETE_1) {
            SPLASH = "SPLASH";
            REPLAY = "REPLAY";
            INITIALIZING = "INITIALIZING";
            INCREMENTING = "INCREMENTING";
            COMPLETE = "COMPLETE";
        }

    }

    public void onClickPlayExisting(View view)
    {

        File dir = getFilesDir();
        File[] file_list = dir.listFiles();
        String[] filename_list = dir.list();
        System.out.println( filename_list );

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make your selection");
        builder.setItems(filename_list, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int filename_list) {
                // Do something with the selection
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

        current_state= state_list.REPLAY;
        updateButtonState(current_state);

    }
    public void onClickCreateNewPlay(View view)
    {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("New Play");
        alert.setMessage("Enter play name");

// Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                play_filename = input.getText().toString();
                // Do something with value!
                text_play_name.setText(play_filename);
                current_state= state_list.INITIALIZING;
                updateButtonState(current_state);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

        play_view.initial_player_insert();
        play_as_string = xml_header + "<play>";



    }

    public void onClickInitializationComplete(View view)
    {
        text_initialization.setText("complete");
        current_state= state_list.INCREMENTING;
        updateButtonState(current_state);
        currentStage=1;
        textCurrentStage.setText(Integer.toString(currentStage));
        mapLastValue();
    }

    public void onClickIncrementStage(View view)
    {
        concatenateStage();
        currentStage++;
        textCurrentStage.setText(Integer.toString(currentStage));


        mapClear();


    }

    public void onClickPlayComplete(View view)
    {
        text_play_complete.setText("complete");
        current_state= state_list.COMPLETE;
        play_view.clear_canvas();
        //concatenateStage();
        updateButtonState(current_state);
        concatenateStage();
        mapClear();
        // System.out.println(play_as_string);
        play_as_string= play_as_string + "</play>";
        String play_filename_full = play_filename + ".XML";
        writeToFile(play_filename_full, play_as_string);
        String string_read = readFromFile(play_filename + ".XML");
//         System.out.println(" BREAK");
        System.out.println(string_read);

        //http://www.androidhive.info/2011/11/android-xml-parsing-tutorial/
        XMLParser parser = new XMLParser();
        String xml = string_read; // getting XML
        Document doc = parser.getDomElement(xml); // getting DOM element

        String KEY_STAGE = "stage";
        String KEY_PLAYER = "player";
        String KEY_INDEX= "stageNumber";
        String KEY_XY="xy";
        NodeList nl = doc.getElementsByTagName(KEY_STAGE);
        System.out.println(nl.getLength());
        String output = parser.parsePlay(doc,1);

        //System.out.println(doc);
        //System.out.println(nl);
        // looping through all item nodes <item>
        for (int i = 0; i < nl.getLength(); i++) {
            // creating new HashMap
            HashMap<String, String> map = new HashMap<String, String>();
            Element e = (Element) nl.item(i);
            System.out.println("Stage " +parser.getValue(e,KEY_STAGE));
            System.out.println("Stage number " + parser.getValue(e,KEY_INDEX));
            System.out.println("Player " + parser.getValue(e,KEY_PLAYER));



            //System.out.println(parser.getValue(e,KEY_XY));

        }
        // System.out.println(Environment.getDataDirectory());


        //clearMap();
        //printMap();
    }


    public void updateButtonState(String current_state) {
        if (current_state == state_list.SPLASH) {
            //Set the button_shape_enabled state
            button_play_existing.setEnabled(true);
            button_create_new.setEnabled(true);
            button_initialization_complete.setEnabled(false);
            button_increment_stage.setEnabled(false);
            button_play_complete.setEnabled(false);
        }
        else if (current_state == state_list.REPLAY) {
            //Set the button_shape_enabled state
            button_play_existing.setEnabled(false);
            button_create_new.setEnabled(false);
            button_initialization_complete.setEnabled(false);
            button_increment_stage.setEnabled(false);
            button_play_complete.setEnabled(false);
        }
        else if (current_state == state_list.INITIALIZING){
            //Set the button_shape_enabled state
            button_play_existing.setEnabled(false);
            button_create_new.setEnabled(false);
            button_initialization_complete.setEnabled(true);
            button_increment_stage.setEnabled(false);
            button_play_complete.setEnabled(false);
        }
        else if (current_state == state_list.INCREMENTING){
            //Set the button_shape_enabled state
            button_play_existing.setEnabled(false);
            button_create_new.setEnabled(false);
            button_initialization_complete.setEnabled(false);
            button_increment_stage.setEnabled(true);
            button_play_complete.setEnabled(true);
        }
        else if (current_state == state_list.COMPLETE){
            //Set the button_shape_enabled state
            button_play_existing.setEnabled(true);
            button_create_new.setEnabled(true);
            button_initialization_complete.setEnabled(false);
            button_increment_stage.setEnabled(false);
            button_play_complete.setEnabled(false);
        }
    }


    public String readFromFile(String filename){
        String input_string ="Garbage";
        try {
            FileInputStream file = openFileInput(filename);
            InputStreamReader input_stream = new InputStreamReader(file);

            BufferedReader reader = new BufferedReader(input_stream);
            StringBuilder input_string_builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                input_string_builder.append(line);   // add everything to StringBuilder
                // here you can have your logic of comparison.
                if(line.toString().equals(".")) {
                    // do something
                }
                input_string =input_string_builder.toString();
            }


        } catch (FileNotFoundException e) {
            // TODO

        } catch (IOException e) {
            // TODO
        } catch (Exception e){
            // TODO

        }
        return input_string;
    }

    public void writeToFile(String filename, String string){
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
    public void concatenateStage() {
        String format =
            "<stage>" + "\n" +
                "<stageNumber>'%s'</stageNumber>" + "\n" +
                "<player>" + "\n" +
                    "<id>1</id>" + "\n" +
                    "<xy>'%s'</xy>" + "\n" +
                "</player>" + "\n" +
                "<player>" + "\n" +
                    "<id>2</id>" + "\n" +
                    "<xy>'%s'</xy>" + "\n" +
                "</player>" + "\n" +
                "<player>" + "\n" +
                    "<id>3</id>" + "\n" +
                    "<xy>'%s'</xy>" + "\n" +
                "</player>" + "\n" +
                "<player>" + "\n" +
                    "<id>4</id>" + "\n" +
                    "<xy>'%s'</xy>" + "\n" +
                "</player>" + "\n" +
                "<player>" + "\n" +
                    "<id>5</id>" + "\n" +
                    "<xy>'%s'</xy>" + "\n" +
                "</player>" + "\n" +
            "</stage>" + "\n"
            ;
         play_as_string = play_as_string + String.format(format,
                currentStage,
                dataPoints.get("1"),
                dataPoints.get("2"),
                dataPoints.get("3"),
                dataPoints.get("4"),
                dataPoints.get("5"));


    }


}
