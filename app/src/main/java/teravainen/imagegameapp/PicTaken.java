package teravainen.imagegameapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.graphics.Point;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;
import android.util.Log;
import android.os.Bundle;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

//import org.apache.commons.codec.binary.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;



public class PicTaken extends AppCompatActivity {

    private Vision vision;


    public static Context myContext;

    public String testPath;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //do a switch statement for the item ids from toolbar_menu.xml
        switch (item.getItemId()){
            case R.id.mainScreen:
              //do something for the settings menu
            case R.id.toolbarHomeButton:
                finish();
                break;
            default:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_taken);

        //Hot to make screen orientation in portrait mode always, needs to be included in all activities where we want it to be locked to portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //initialize the toolbar
        android.support.v7.widget.Toolbar myToolBar = findViewById(R.id.myToolBar);
        setSupportActionBar(myToolBar);

        //Otetaan vastaan ThirdActivitysta lähetetty String data, jossa on PATH otettuun kuvaan
        Intent intent = getIntent();
        final String pathValue = intent.getStringExtra("pathToFile");

        ImageView myImage = findViewById(R.id.myImagePreview);
        myImage.setImageBitmap(BitmapFactory.decodeFile(pathValue));

        //Images opened as files from their pathValue
        final File originalImage = new File(pathValue);

        final Button analysisbutton = findViewById(R.id.analyzeButton);
        final Button declineAnalysisButton = findViewById(R.id.declineButton);

        /*try{
            copyFileUsingApacheCommonsIO(originalImage,copyImage);
        }catch (IOException e){
            e.printStackTrace();
        }*/

        //reset the analysis on the image in the shardpref when you create this activity
        //this activity won't be created unless a picture is taken which replaces the old one
        saveAnalysis("No analysis done on the image yet!");


        //Tehdään skaalattu versio kuvasta aina kun luodaan activity
        //tätä voidaan käyttää uploadaamiseen, koska sen koko on paljon pienempi
        //tallentaa piennennetyn kopion kuvasta nimellä test.jpg, korvaa edellisen test.jpg:n jos sellainen on olemassa
        rescaleImage(originalImage);
        testPath = Environment.getExternalStorageDirectory()+"/test.jpg";


        analysisbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Näytä loading animaatio ja kerro käyttäjälle että kuvaa analysoidaan
                ProgressBar progBar = findViewById(R.id.myProgressBar);
                progBar.setVisibility(View.VISIBLE);

                //hide the buttons when the analysis begins
                LinearLayout buttonHolder = findViewById(R.id.buttonHolderLayout);
                buttonHolder.setVisibility(View.GONE);

                TextView debugView =  findViewById(R.id.debugData);
                debugView.setText("Loading... This may take a little while...");

                detectLabels2(testPath);
                //detectLabels2(truePath);
                //detectLabels2(pathValue);
            }
        });

        declineAnalysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePicture = new Intent(getApplicationContext(), TakePictureActivity.class);
                startActivity(takePicture);
                finish();
            }
        });

        //Build the connector for the vision api
        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                //API KEY
                new VisionRequestInitializer("")
        );

        vision = visionBuilder.build();

    }

    //Suorittaa labelDetection requestin luomisen annetulle String muotoiselle tiedostosijainnille
    //Compressoi kuvaa vielä pienemmäksi ennen lähetystä
    //Tarjoaa Log.e muodossa eri vaiheet, joten voit seurata missä vaiheessa mikäkin tapahtuu
    //Kun saa JSON responsen, suorittaa compareData funktion jokaista JSON responsen labelia kohden
    //Lopuksi tulostaa kaikki labelit textviewiin
    public  void detectLabels2(final String imagePath){
        //List<AnnotateImageRequest> requests = new ArrayList<>();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    // Convert photo to byte array
                    //This uses a sample image, use the one provided by the camera app
                    //InputStream inputStream = getResources().openRawResource(R.raw.radiomastot);

                    //laitetaan otetun kuvan path FileInputstreamiin ja lähetetään se vision API:lle
                    // InputStream inputStream = getResources().openRawResource(R.raw.radiomastot);
                    //Ylempi kuva yhä lataa nopeasti, otettu kuva ei
                    FileInputStream inputStream = new FileInputStream(imagePath);

                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try{
                        int compression_factor = 30; // represents 10% compression, 0-100, the smaller number, the smaller image. 100 is 100%
                        bitmap.compress(Bitmap.CompressFormat.JPEG, compression_factor, baos);

                        Log.e("compression", "COMPRESSION COMPLETE");

                        byte[] photoData = baos.toByteArray();

                        Image inputImage = new Image();
                        inputImage.encodeContent(photoData);

                        Feature desiredFeature = new Feature();
                        desiredFeature.setType("LABEL_DETECTION");

                        AnnotateImageRequest request = new AnnotateImageRequest();
                        request.setImage(inputImage);
                        request.setFeatures(Arrays.asList(desiredFeature));

                        BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                        batchRequest.setRequests(Arrays.asList(request));

                        Log.e("vision", "Beginning Vision");

                        //call annotate() method offered by Google Visions API
                        BatchAnnotateImagesResponse batchResponse =
                                vision.images().annotate(batchRequest).execute();

                        Log.e("vision", "Vision Complete");

                        //Response
                        List<EntityAnnotation> myResponse = batchResponse.getResponses().get(0).getLabelAnnotations();

                        int numberOfLabels = myResponse.size();

                        String labelName = "";
                        for(int i=0; i<numberOfLabels; i++){
                            //hakee labelin descriptionin jokaista labelia kohden
                            //label.getScore() voi myös hyödyntää?
                            //tässä voidaan myös tehdä vertailu tehtävän suorittamisen kanssa, jos myResponse.get(i).getDescription() vastaa jotain haluttua kuvauksen kohdetta
                            labelName += "\n Label name: " + myResponse.get(i).getDescription();

                            //compare the description for each label found with compareData()function
                            String labelDescription = myResponse.get(i).getDescription();
                            compareData(labelDescription);
                        }

                        final String rtrMessage = "We found the following labels: " + labelName;

                        //TextView debugView =  (TextView)findViewById(R.id.debugData);
                        //debugView.setText(rtrMessage);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Hide the progressbar when done
                                ProgressBar progBar = findViewById(R.id.myProgressBar);
                                progBar.setVisibility(View.GONE);
                                //Toast.makeText(getApplicationContext(), rtrMessage, Toast.LENGTH_LONG).show();

                                //Make a toast informing user that he found a mission object
                                SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                boolean completionStatus = mySharedPref.getBoolean("Mprogress", false);
                                String comparisonValue = mySharedPref.getString("Mdescription","");
                                int awardPoints = mySharedPref.getInt("Mpoints", 0);

                                if(completionStatus == true){
                                    Toast.makeText(getApplicationContext(), "Found a mission object: " + comparisonValue
                                            + "\nAwarded with: " + awardPoints + " Points!", Toast.LENGTH_LONG).show();

                                    //here we should reset the current mission, so that it can't be completed again
                                    resetMission();
                                }

                                TextView debugView = findViewById(R.id.debugData);
                                debugView.setVisibility(View.VISIBLE);
                                debugView.setText(rtrMessage);
                                saveAnalysis(rtrMessage);
                            }
                        });

                    }finally {
                        baos.close();
                    }

                }catch (Exception e){
                    Log.d("Error", e.getMessage());
                }
            }
        });

    }

    public void saveAnalysis(String analysisData){
        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mySharedPref.edit();

        editor.putString("imageLabels", analysisData);
        editor.apply();
    }

    //Checks if a label found in the taken picture matches one requested in the missions
    public void compareData(String compareData){

        SharedPreferences mySharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String comparisonValue = mySharedPref.getString("Mdescription","");
        int awardPoints = mySharedPref.getInt("Mpoints", 0);
        boolean completionStatus = mySharedPref.getBoolean("Mprogress", false);

        if(compareData.equals(comparisonValue) && completionStatus != true){
            SharedPreferences.Editor editor = mySharedPref.edit();

            int score = mySharedPref.getInt("counter", 0);
            editor.putInt("counter", score + awardPoints);
            editor.putBoolean("Mprogress", true);
            editor.apply();

            //Toastaaminen tässä kohtaa jumittaa kaikean
            //Toast.makeText(getApplicationContext(), "Mission object " + comparisonValue +  " found! \n" + "You were awarded with: " + awardPoints, Toast.LENGTH_LONG).show();
        }

    }

    //ottaa File tiedostotyypin muuttujan ja suorittaa rescalee sen pienemmäksi sekä compressoi sitä hiukan
    //tallentaa uuden version tiedostosta test.jpg nimellä, joten alkuperäinen tiedosto pysyy samana
     public void rescaleImage(File targetFile){

        Bitmap b = BitmapFactory.decodeFile(targetFile.getAbsolutePath());

        int oriWidth = b.getWidth();
        int oriHeight = b.getHeight();

        final int destWidth = 2000; //or the width you need

         if(oriWidth > oriHeight){
             //picture is wider than we want it, calculate it's target height
             int destheight = oriHeight/(oriWidth / destWidth);
             //create a scaled bitmap so it reduces the image, not just trim it
             Bitmap b2 = Bitmap.createScaledBitmap(b, destWidth, destheight, false);
             ByteArrayOutputStream outStream = new ByteArrayOutputStream();
             //compress to the format you want, jpeg, png...
             //70 is the 0-100 quality percantage
             b2.compress(Bitmap.CompressFormat.JPEG, 90 , outStream);
             //save the file, until we use it
             File f = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "test.jpg");
             try{
                 f.createNewFile();
                 //write the bytes in file
                 FileOutputStream fo = new FileOutputStream(f);
                 fo.write(outStream.toByteArray());
                 //close the FileOutput
                 fo.close();
             }catch (IOException e){
                 e.printStackTrace();
             }
         }
    }

    //nollaa tehtävän, käytetään tässä tapauksessa jos pyydetty label löytyy JSON responsesta
    public void resetMission(){
        //Käyttää UtilityFunctions luokan funktiota resetScore
        myContext = getApplicationContext();
        UtilityFunctions.resetScore(myContext);
    }


    //________________________________________________________________________________________
    //DOWN HERE ARE SAMPLE CODES THAT ARE NOT USED IN THE APP, BUT CAN BE REFERENCED FOR IDEAS
    //_________________________________________________________________________________________

    //This is a sample that uses FACE_DETECTION on the picture
    private void detectFace(){
        // Create new thread
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    // Convert photo to byte array
                    InputStream inputStream = getResources().openRawResource(R.raw.radiomastot);
                    byte[] photoData = IOUtils.toByteArray(inputStream);

                    Image inputImage = new Image();
                    inputImage.encodeContent(photoData);

                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("FACE_DETECTION");

                    AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Arrays.asList(desiredFeature));

                    BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                    batchRequest.setRequests(Arrays.asList(request));

                    //call annotate() method offered by Google Visions API
                    BatchAnnotateImagesResponse batchResponse = vision.images().annotate(batchRequest).execute();

                    List<FaceAnnotation> faces = batchResponse.getResponses().get(0).getFaceAnnotations();

                    int numberOfFaces = faces.size();

                    String likelihoods = "";
                    for(int i=0; i<numberOfFaces;i++){
                        likelihoods += "\n It is " +
                                faces.get(i).getJoyLikelihood() + " that face " + i + " is happy";
                    }

                    final String message = "This photo has " + numberOfFaces + "faces" + likelihoods;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                    });

                }catch (Exception e){
                    Log.d("Error", e.getMessage());
                }
            }
        });

    }

    //this is the old version without the image compression so it sends the large image to the vision api
    public  void detectLabels(final String imagePath){
        //List<AnnotateImageRequest> requests = new ArrayList<>();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    // Convert photo to byte array
                    //This uses a sample image, use the one provided by the camera app
                    //InputStream inputStream = getResources().openRawResource(R.raw.radiomastot);

                    //laitetaan otetun kuvan path FileInputstreamiin ja lähetetään se vision API:lle
                    FileInputStream inputStream = new FileInputStream(imagePath);

                    byte[] photoData = IOUtils.toByteArray(inputStream);
                    //byte[] photoData = com.google.api.client.util.Base64.encodeBase64(imagePath.getBytes());

                    Image inputImage = new Image();
                    inputImage.encodeContent(photoData);

                    Log.d("encoded", "Image encoded!");

                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("LABEL_DETECTION");

                    AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Arrays.asList(desiredFeature));

                    Log.d("annotateImageRequest", "BatchAnnotate Done!");

                    BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                    batchRequest.setRequests(Arrays.asList(request));

                    Log.d("batchRequest", "BatchRequest Done");

                    //TÄSSÄ ON CHOKEPOINT SOVELLUKSELLE
                    //KUVA MITÄ KÄYTETÄÄN pic.jpg ON LIIAN ISO, radiomastot.jpg uploadaantuu noin 3 sekunnissa
                    //call annotate() method offered by Google Visions API
                    BatchAnnotateImagesResponse batchResponse =
                            vision.images().annotate(batchRequest).execute();
                    // END OF CHOKEPOINT
                    Log.d("request", "Vision.images() Done!");

                    //Response
                    List<EntityAnnotation> myResponse = batchResponse.getResponses().get(0).getLabelAnnotations();

                    Log.d("response", "Got the response!");

                    int numberOfLabels = myResponse.size();

                    String labelName = "";
                    for(int i=0; i<numberOfLabels; i++){
                        //hakee labelin descriptionin jokaista labelia kohden
                        //label.getScore() voi myös hyödyntää?
                        //tässä voidaan myös tehdä vertailu tehtävän suorittamisen kanssa, jos myResponse.get(i).getDescription() vastaa jotain haluttua kuvauksen kohdetta
                        labelName += "\n Label name: " + myResponse.get(i).getDescription();

                        //compare the description for each label found
                        String labelDescription = myResponse.get(i).getDescription();
                        compareData(labelDescription);
                    }

                    final String rtrMessage = "We found the following labels: " + labelName;

                    //TextView debugView =  (TextView)findViewById(R.id.debugData);
                    //debugView.setText(rtrMessage);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Hide the progressbar when done
                            ProgressBar progBar = findViewById(R.id.myProgressBar);
                            progBar.setVisibility(View.GONE);

                            Toast.makeText(getApplicationContext(), rtrMessage, Toast.LENGTH_LONG).show();
                            TextView debugView =  findViewById(R.id.debugData);
                            debugView.setText(rtrMessage);
                        }
                    });

                }catch (Exception e){
                    Log.d("Error", e.getMessage());
                }
            }
        });
    }

    //This copies a file to the destination
    //takes File type parameters, first the thing being copied and second where with what name
    private static void copyFileUsingApacheCommonsIO(File source, File dest)throws
            IOException{
        FileUtils.copyFile(source, dest);
    }

}
