package uw.playdesigner6;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

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
import android.util.Log;



public class Main extends ActionBarActivity implements MultiChoiceListDialogFragment.multiChoiceListDialogListener, SingleChoiceListDialogFragment.singleChoiceListDialogListener {
//public class Main extends ActionBarActivity {
    private static final String TAG = Main.class.getSimpleName();

    private Paint paint_circle;
    private ImageView image_court, image_play;
    private Canvas canvas_play;
    private Bitmap bitmap_play;
    private PlayView playView;

    private TextView text_status;
    private Bitmap canvasBitmap;

    private final AppStates state_list = new AppStates("","", "", "", "");
    private String current_state;

    //Buttons
    private Button buttonPlaysExisting, buttonRemovePlay, buttonCreateNew;
    private Button buttonInitializationComplete, buttonIncrementStage, buttonPlayComplete;

    //TextViews
    private TextView text_play_name, text_initialization, textCurrentStage;
    private TextView text_play_complete;

    //Strings
    private String play_filename;
    private String play_as_string;
    private static String xml_header = "<?xml version='1.0' encoding='UTF-8'?>" + "\n";

    //Integers
    private int currentStage = 1;
    private static int PLAYER_COUNT = 5;
    private static int POINT_PER_STAGE = 100;


    //Other
    private Map<String,List<String>> dataPoints;
    private String fileToLoad;

    private String[] playAsArray;
    private String playAsString;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
/*        String folder_main = "play_designer";

        File f = new File(Environment.getExternalStorageDirectory(),
                folder_main);
        if (!f.exists()) {
            f.mkdirs();
        }
        Boolean check = isExternalStorageWritable();
        System.out.println("EXTERNAL CHECK:" + check);*/

        //Load court view
        setContentView(R.layout.activity_main);
        playView = (PlayView)findViewById(R.id.play);


        //Define buttons
        buttonPlaysExisting = (Button)findViewById(R.id.buttonPlayExisting);
        buttonRemovePlay = (Button)findViewById(R.id.buttonRemovePlay);
        buttonCreateNew = (Button)findViewById(R.id.buttonCreateNewPlay);
        buttonInitializationComplete = (Button)findViewById(R.id.buttonInitializationComplete);
        buttonIncrementStage = (Button)findViewById(R.id.buttonIncrementStage);
        buttonPlayComplete = (Button)findViewById(R.id.buttonPlayComplete);

        //Define TextViews
        text_play_name = (TextView)findViewById(R.id.value_play_name);
        text_initialization = (TextView)findViewById(R.id.value_initialization);
        textCurrentStage = (TextView)findViewById(R.id.value_increment);
        text_play_complete = (TextView)findViewById(R.id.value_play_complete);

        //Define current state
        current_state= state_list.SPLASH;
        updateButtonState(current_state);

        //Create hash map for points
        dataPoints = new HashMap<String,List<String>>();
        for (int i=0; i<PLAYER_COUNT; i++) {
            List<String> data = new ArrayList<String>();
            String name = Integer.toString(i+1);
            dataPoints.put(name, data);
        }
        playView.setupDataPoints(dataPoints);

        // playView.initialPlayerInsert();
    }

/*    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }*/

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

    //Clear hash map
    public void mapClear(){
        Set<String> keys = dataPoints.keySet();
        for (String key : keys){
            List<String> points = dataPoints.get(key);
            points.clear();
        }
    }

    //Extract last value from hash map
    public void mapLastValue(){
        Set<String> keys = dataPoints.keySet();
        for (String key : keys){
            List<String> points = dataPoints.get(key);
            String last_entry = points.get(points.size()-1);
            points.clear();
            points.add(last_entry);
        }
    }

/*    public void onViewChange(){
        text_status.setText("I hope partial success");
    }*/


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

/*    public void onTest(View view) {
//        textCurrentStage.setText("SLDKFJSKDJF");
//        playView.initialPlayerInsert();
//        playView.invalidate();
//        playPlay("qq.XML");
//        playView.invalidate();
        new Thread(new Runnable() {
            public void run() {
                playView.post(new Runnable() {
                    public void run () {
                        for (int pointIndex = 0; pointIndex < 20; pointIndex++) {
                            playView.temporary();
                        }
                    }
                });
            }

        }).start();

    }*/

    //Handle Play Existing button click event
    public void onClickPlayExisting(View view)
    {

        //setContentView(R.layout.activity_main);
        //animatedBoxView = (AnimatedBoxView).findViewById(R.id.play);
                //CharSequence list="Tea";

        File dir = getFilesDir();
        //File[] file_list = dir.listFiles();
        String[] filenameList = dir.list();
        System.out.println( filenameList );

        Bundle bundle = new Bundle();
        bundle.putStringArray("list", filenameList);


        //DialogFragment dialog = SingleChoiceListDialogFragment.newInstance(list);
        DialogFragment dialog = new SingleChoiceListDialogFragment();
        dialog.setArguments(bundle);


        //include a tag to identify the fragment
         dialog.show(getSupportFragmentManager(),
                 "SingleChoiceListDialogFragment");


        //Update current state
        current_state= state_list.SPLASH;
        updateButtonState(current_state);

//         playPlay(fileToLoad);*/

        // playView.temporary();
    }



    public void onClickDeletePlay(View view)
    {
        //CharSequence list="Tea";

        File dir = getFilesDir();
        //File[] file_list = dir.listFiles();
        String[] filenameList = dir.list();
        System.out.println( filenameList );

        Bundle bundle = new Bundle();
        bundle.putStringArray("list", filenameList);



        //DialogFragment dialog = MultiChoiceListDialogFragment.newInstance(list);
        DialogFragment dialog = new MultiChoiceListDialogFragment();
        dialog.setArguments(bundle);


        //getList()

        //include a tag to identify the fragment
        dialog.show(getSupportFragmentManager(),
                "MultiChoiceListDialogFragment");

    }

    @Override
    public void multipleChoiceOnOkay(ArrayList<Integer> arrayList) {
        File directory = getFilesDir();
        //File[] file_list = dir.listFiles();
        String[] filenameList = directory.list();
        StringBuilder stringBuilder = new StringBuilder();
        if (arrayList.size() != 0) {
            for (int i = 0; i < arrayList.size(); i++) {
                String fileToRemove = filenameList[arrayList.get(i)];


                deleteFile(fileToRemove);
                Log.d(TAG, "Deleted file: " + fileToRemove);

                stringBuilder = stringBuilder.append(" " + fileToRemove);
            }
            Toast.makeText(this, "Deleted files: "
                    + stringBuilder.toString(), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "No files deleted", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void multipleChoiceOnCancel() {
        Toast.makeText(this, "No files deleted", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void singleChoiceOnOkay(int selectedItemIndex) {
        File directory = getFilesDir();
        //File[] file_list = dir.listFiles();
        String[] filenameList = directory.list();
        StringBuilder stringBuilder = new StringBuilder();
//         if (selectedItemIndex.hasValue) {
//             for (int i = 0; i < arrayList.size(); i++) {
                String fileToPlay = filenameList[selectedItemIndex];


                //deleteFile(fileToRemove);
                Log.d(TAG, "File to play: " + fileToPlay);

                stringBuilder = stringBuilder.append(" " + fileToPlay);
// // dental            }
            Toast.makeText(this, "File to play: "
                     + stringBuilder.toString(), Toast.LENGTH_SHORT).show();

        playPlay(fileToPlay);

//         }
//         else {
//             Toast.makeText(this, "No files deleted", Toast.LENGTH_SHORT).show();
//         }

    }
    @Override
    public void singleChoiceOnCancel() {
        Toast.makeText(this, "No files deleted", Toast.LENGTH_SHORT).show();
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

        playView.initialPlayerInsert();
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
        play_as_string = concatenateStage(play_as_string);
        System.out.println(play_as_string);
        currentStage++;
        textCurrentStage.setText(Integer.toString(currentStage));
        mapClear();


    }



    public void onClickPlayComplete(View view)
    {
        text_play_complete.setText("complete");
        current_state= state_list.COMPLETE;
        playView.clear_canvas();
        //concatenateStage();
        updateButtonState(current_state);
        play_as_string = concatenateStage(play_as_string);
        mapClear();
        // System.out.println(play_as_string);
        play_as_string= play_as_string + "</play>";
        String play_filename_full = play_filename + ".XML";
        writeToFile(play_filename_full, play_as_string);

    }



    public void updateButtonState(String current_state) {
        if (current_state == state_list.SPLASH) {
            //Set the button_shape_enabled state
            buttonPlaysExisting.setEnabled(true);
            buttonRemovePlay.setEnabled(true);
            buttonCreateNew.setEnabled(true);
            buttonInitializationComplete.setEnabled(false);
            buttonIncrementStage.setEnabled(false);
            buttonPlayComplete.setEnabled(false);
        }
        else if (current_state == state_list.REPLAY) {
            //Set the button_shape_enabled state
            buttonPlaysExisting.setEnabled(false);
            buttonRemovePlay.setEnabled(false);
            buttonCreateNew.setEnabled(false);
            buttonInitializationComplete.setEnabled(false);
            buttonIncrementStage.setEnabled(false);
            buttonPlayComplete.setEnabled(false);
        }
        else if (current_state == state_list.INITIALIZING){
            //Set the button_shape_enabled state
            buttonPlaysExisting.setEnabled(false);
            buttonRemovePlay.setEnabled(false);
            buttonCreateNew.setEnabled(false);
            buttonInitializationComplete.setEnabled(true);
            buttonIncrementStage.setEnabled(false);
            buttonPlayComplete.setEnabled(false);
        }
        else if (current_state == state_list.INCREMENTING){
            //Set the button_shape_enabled state
            buttonPlaysExisting.setEnabled(false);
            buttonRemovePlay.setEnabled(false);
            buttonCreateNew.setEnabled(false);
            buttonInitializationComplete.setEnabled(false);
            buttonIncrementStage.setEnabled(true);
            buttonPlayComplete.setEnabled(true);
        }
        else if (current_state == state_list.COMPLETE){
            //Set the button_shape_enabled state
            buttonPlaysExisting.setEnabled(true);
            buttonRemovePlay.setEnabled(true);
            buttonCreateNew.setEnabled(true);
            buttonInitializationComplete.setEnabled(false);
            buttonIncrementStage.setEnabled(false);
            buttonPlayComplete.setEnabled(false);
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
    public String concatenateStage(String stage) {

        for (int i=0; i<PLAYER_COUNT; i++) {
            System.out.println("concatenate stage: " + dataPoints.get(Integer.toString(i)));
        }


        String format =
            "<stage>" + "\n" +
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

         return stage + String.format(format,
                dataPoints.get("1"),
                dataPoints.get("2"),
                dataPoints.get("3"),
                dataPoints.get("4"),
                dataPoints.get("5"));
    }

    public void playPlay(String filename){

        // XML parser based on tutorial found at:
        //      http://www.androidhive.info/2011/11/android-xml-parsing-tutorial/

        // Replay as XML
        String playAsXml = readFromFile(filename);

        // Create instance of XML parser
        XMLParser parser = new XMLParser();

        // Parse XML play into map
        Map<String,List<String>> currentPlay = parser.getPlay(playAsXml, PLAYER_COUNT);

        // Send play to view for playing
        playView.startPlay(currentPlay);

    }
}