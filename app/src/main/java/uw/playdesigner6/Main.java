package uw.playdesigner6;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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



public class Main extends ActionBarActivity implements MultiChoiceListDialogFragment.multiChoiceListDialogListener, SingleChoiceListDialogFragment.singleChoiceListDialogListener {
//public class Main extends ActionBarActivity {
    private static final String TAG = Main.class.getSimpleName();
    public int SERVERPORT = 49152;//4445;

    public final String ip = "10.0.1.2";


    /*    private Paint paint_circle;
    private ImageView image_court, image_play;
    private Canvas canvas_play;
    private Bitmap bitmap_play;*/
    private PlayView playView;


/*    private TextView text_status;
    private Bitmap canvasBitmap;*/

    private final AppStates state_list = new AppStates("","", "", "", "");
    private String current_state;

    //Buttons
    private Button buttonPlaysExisting, buttonRemovePlay, buttonCreateNew;
    private Button buttonInitializationComplete, buttonIncrementStage, buttonPlayComplete;

    //TextViews
    private TextView textPlayName, textInitialization, textCurrentStage;
    private TextView textPlayComplete;

    //Strings
    private String playFilename;
    private String play_as_string;
    private static String xml_header = "<?xml version='1.0' encoding='UTF-8'?>" + "";

    //Integers
    private int currentStage = 1;
    private static int PLAYER_COUNT = 5;
//     private static int POINT_PER_STAGE = 100;


    //Other

    private Map<Integer,List<List<float[]>>> dataPlayers = new HashMap<Integer,List<List<float[]>>>();
    private List<Integer> dataBall = new ArrayList<Integer>();



/*    private String fileToLoad;

    private String[] playAsArray;
    private String playAsString;*/

    private MultiThreadingTCP multiThreadingTcp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Random random = new Random();
        //SERVERPORT = random.nextInt(65535 - 49152 + 1) + 49152;


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
        //setContentView(R.layout.activity_main);
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
        textPlayName = (TextView)findViewById(R.id.value_play_name);
        textInitialization = (TextView)findViewById(R.id.value_initialization);
        textCurrentStage = (TextView)findViewById(R.id.value_increment);
        textPlayComplete = (TextView)findViewById(R.id.value_play_complete);

        //Define current state
        current_state= state_list.SPLASH;
        updateButtonState(current_state);

        //Create hash map for points
        //dataPlayers = new HashMap<Integer,List<List<Float[]>>>();
        for (int i=0; i<Players.PLAYER_COUNT; i++) {
            //List<String> data = new ArrayList<String>();
            List<List<float[]>> outerList = new ArrayList<List<float[]>>();
            //String name = Integer.toString(i+1);
            dataPlayers.put(i, outerList);
        }
        playView.setupDataPoints(dataPlayers, dataBall);
        //Getting the public IP and upload it into amazon s3
        Thread ip = new GetIP();


        ip.start();

        //Starting the TCP Connection
        Log.w(TAG,"Starting the TCP CONNECTION");
        this.multiThreadingTcp = new MultiThreadingTCP(SERVERPORT);
        // playView.initializeCourt();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && isTaskRoot()) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.quit)
                    .setMessage(R.string.really_quit)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Stop the activity
                            killTCP();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();

            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }


    public void killTCP(){
        this.multiThreadingTcp.stopTCP();
        this.multiThreadingTcp.interrupt();
    }



    //Clear hash map
    public void mapClear(){
        Set<Integer> keys = dataPlayers.keySet();
        for (Integer key : keys){
            List<List<float[]>> points = dataPlayers.get(key);
            points.clear();
        }
    }

    //Extract last value from hash map
    public void mapLastValue(){
        Set<Integer> keys = dataPlayers.keySet();
        for (Integer key : keys){
            List<List<float[]>> points = dataPlayers.get(key);
            float[] last_entry = points.get(points.size()-1).get(0);
            points.clear();
            points.get(0).add(last_entry);
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



    //Handle Play Existing button click event
    public void onClickPlayExisting(View view)
    {
        //If replaying play, stop animation
        playView.stopPlay();

        // Get list of files in directory
        File dir = getFilesDir();
        
        // Convert filename list to string array
        String[] filenameList = dir.list();
        
        // Create bundle, including filename list to send to fragment
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

    }



    public void onClickDeletePlay(View view)
    {
        //If replaying play, stop animation
        playView.stopPlay();

        //CharSequence list="Tea";

        File dir = getFilesDir();
        //File[] file_list = dir.listFiles();
        String[] filenameList = dir.list();
        //System.out.println(filenameList);

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


    // Respond to Play Existing click okay event
    @Override
    public void singleChoiceOnOkay(int selectedItemIndex) {
        // Get list of files and directory
        File directory = getFilesDir();

        // Convert filename list to string array
        String[] filenameList = directory.list();

        StringBuilder stringBuilder = new StringBuilder();

        // Name of file/play selected
        String fileToPlay = filenameList[selectedItemIndex];

        // Print filename to log
        Log.d(TAG, "File to play: " + fileToPlay);

        stringBuilder = stringBuilder.append(" " + fileToPlay);

        // Update text view with play name
        textPlayName.setText(fileToPlay);

        // Create toast to display file selected
        Toast.makeText(this, "File to play: "
             + stringBuilder.toString(), Toast.LENGTH_SHORT).show();

        // Call method to load and replay play
        readPlayFromFile(fileToPlay);
    }
    @Override
    public void singleChoiceOnCancel() {
        Toast.makeText(this, "No files deleted", Toast.LENGTH_SHORT).show();
    }

    // Handle Create New button click event
    public void onClickCreateNewPlay(View view)
    {
        //If replaying play, stop animation
        playView.stopPlay();

        playView.initializeCourt();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("New Play");
        alert.setMessage("Enter play name");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                playFilename = input.getText().toString();
                textPlayName.setText(playFilename);
                current_state = state_list.INITIALIZING;
                updateButtonState(current_state);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();


        play_as_string = xml_header + "<play>";

    }

    // Handle Initialization Complete button click
    public void onClickInitializationComplete(View view)
    {
        //If replaying play, stop animation
        playView.stopPlay();

        textInitialization.setText("complete");
        current_state= state_list.INCREMENTING;
        updateButtonState(current_state);
        currentStage=1;
        textCurrentStage.setText(Integer.toString(currentStage));

        //mapLastValue();

        // TODO
        // Call PlayThe you function to handle initialization
        //playView.clearData();
        //System.out.println("Size after Initialization:" +Integer.toString(dataPlayers.get(0).size()));
        playView.saveLastPoint();
        playView.clearCanvas();
    }

    public void onClickIncrementStage(View view)
    {
        //If replaying play, stop animation
        playView.stopPlay();

        //play_as_string = concatenateStage(play_as_string);
        //currentStage++;
        //textCurrentStage.setText(Integer.toString(currentStage));

        //mapClear();

        playView.incrementStage();
        playView.clearCanvas();
        //playView.clearCanvas();

    }

    public void onClickPlayComplete(View view)
    {
        //If replaying play, stop animation
        playView.stopPlay();

        textPlayComplete.setText("complete");
        current_state= state_list.COMPLETE;

        updateButtonState(current_state);
        //play_as_string = concatenateStage(play_as_string);
        String playAsString = convertPlayToXML();
        String play_filename_full = playFilename + ".XML";
        writeToFile(play_filename_full, playAsString);
       // playView.clearData();
        playView.initializeCourt();


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

                   // System.out.println(player + ", " + ", " + stage + ", " + X + ", " + Y);
                }
            }
        }

    }

    // Update button state, based on app state
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

    // Read play from XML file
    public String readFromFile(String filename){
        String input_string ="";
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

    // Write play to XML file
    public void writeToFile(String filename, String string){
        /* Checks if external storage is available for read and write */
        //System.out.println(string);

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String convertPlayToXML() {

        int playViewWidth = playView.getWidth();
        int playViewHeight = playView.getHeight();
        System.out.println( "view height and weight ");
        System.out.println(playViewWidth);
        System.out.println(playViewHeight);

        String n = "\n";

        String t = "    ";
        t = "";

        String playAsString = "<?xml version='1.0' encoding='UTF-8'?> " + n;
        playAsString = playAsString + "<play>" + n;
        // Set of keys
        Set<Integer> players = dataPlayers.keySet();

        // Number of stages in play
        //players.iterator().next() TODO
        int stageCount = dataPlayers.get(0).size();

        // Loop on stages
        for (int currentStage = 0; currentStage < stageCount; currentStage++) {

            playAsString += "<stage>" + n;

            playAsString += "<ball>";

            playAsString += Integer.toString(dataBall.get(currentStage));

            playAsString += "</ball>" + n;

            // Loop on players
            for (Integer player : players) {

                // Data for given stage and player
                List<float[]> data = dataPlayers.get(player).get(currentStage);

                // Number of points
                int pointCount = data.size();

                String pointsAsString = "";

                for (int currentPoint = 0; currentPoint < pointCount; currentPoint++) {
                    // Points as string, comma delimited
                    pointsAsString = pointsAsString
                                    + Float.toString(data.get(currentPoint)[0]/playViewWidth)+
                                "," + Float.toString(data.get(currentPoint)[1]/playViewHeight)+
                                "," + Float.toString(data.get(currentPoint)[2])+
                                "," + Float.toString(data.get(currentPoint)[3]);

                    // Separate pairs of coordinates with a semicolon
                    if (currentPoint < pointCount-1){
                        pointsAsString = pointsAsString + ";";

                    }
                }

                playAsString = playAsString +
                        "<player>" + n +
                        t + "<id>" + Integer.toString(player) + "</id>" + n +
                        t + "<data>" + pointsAsString + "</data>" + n +
                        "</player>" + n;
            }
            playAsString = playAsString + "</stage>" + n;
        }
        playAsString = playAsString + "</play>";
        System.out.println( "=============================================");
        System.out.println(playAsString);
        return playAsString;
    }



    // Replay play
    public void readPlayFromFile(String filename){

        // XML parser based on tutorial found at:
        //      http://www.androidhive.info/2011/11/android-xml-parsing-tutorial/

        // Replay as XML
        String playAsXml = readFromFile(filename);


        Log.w(TAG, "sending message through tcp");
        multiThreadingTcp.sendMessage(playAsXml);


        // Create instance of XML parser
        XMLParser parser = new XMLParser();

        // Parse XML play into map
        PlayData currentPlayData = parser.getPlay(playAsXml, PLAYER_COUNT);

                // Send play to view for playing
        playView.startPlay(currentPlayData);

    }

    private class GetIP extends Thread{
        public void run() {
            System.out.println("getting ip");
            try {
/*                URL whatismyip = new URL("http://checkip.amazonaws.com/");
                BufferedReader in = null;
                in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
                final String ip = in.readLine(); //you get the IP as a String*/

                System.out.println("Finish getting ip " +  ip + " " + SERVERPORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
/*                        try {
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("IP_SERVER.txt", Context.MODE_PRIVATE));
                            outputStreamWriter.write(ip + " " + SERVERPORT);
                            outputStreamWriter.close();
                            uploadToCloud();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                    }
                });

            } /*catch (IOException e) {
                e.printStackTrace();
            }*/
            finally {

            }

        }
    }

/*    public void uploadToCloud(){
        UploadCredentials credentials = new UploadCredentials(getResources(),this);
        credentials.execute();
    }*/



    /*    public void onTest(View view) {
//        textCurrentStage.setText("SLDKFJSKDJF");
//        playView.initializeCourt();
//        playView.invalidate();
//        readPlayFromFile("qq.XML");
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

    // Concatenate most recent stage for file recording
/*    //https://xjaphx.wordpress.com/2011/10/27/android-xml-adventure-create-write-xml-data/
    public String concatenateStage(String stage) {

        String format =
            "<stage>" + "" +
                "<player>" + "" +
                    "<id>1</id>" + "" +
                    "<xy>'%s'</xy>" + "" +
                "</player>" + "" +
                "<player>" + "" +
                    "<id>2</id>" + "" +
                    "<xy>'%s'</xy>" + "" +
                "</player>" + "" +
                "<player>" + "" +
                    "<id>3</id>" + "" +
                    "<xy>'%s'</xy>" + "" +
                "</player>" + "" +
                "<player>" + "" +
                    "<id>4</id>" + "" +
                    "<xy>'%s'</xy>" + "" +
                "</player>" + "" +
                "<player>" + "" +
                    "<id>5</id>" + "" +
                    "<xy>'%s'</xy>" + "" +
                "</player>" + "" +
            "</stage>" + "\n"
            ;

         return stage + String.format(format,
                 dataPlayers.get("1"),
                 dataPlayers.get("2"),
                 dataPlayers.get("3"),
                 dataPlayers.get("4"),
                 dataPlayers.get("5"));
    }*/


    /*    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }*/

/*    public void printMap(){
        Set<String> keys = dataPlayers.keySet();
        for (String key : keys) {
            System.out.println("\nPlayer " + key + " : \n");
            List<String> points = dataPlayers.get(key);
            for (String point : points){
                System.out.print(point);
            }
        }
    }*/
}
