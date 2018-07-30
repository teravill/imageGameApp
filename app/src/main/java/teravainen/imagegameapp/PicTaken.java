package teravainen.imagegameapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.graphics.Point;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;



public class PicTaken extends AppCompatActivity {

    private Vision vision;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MAX_LABEL_RESULTS = 10;

    public static Context myContext;

    public String truePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_taken);

        //Hot to make screen orientation in portrait mode always, needs to be included in all activities where we want it to be locked to portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TextView debugView =  (TextView)findViewById(R.id.debugData);

        //Otetaan vastaan ThirdActivitysta lähetetty String data, jossa on PATH otettuun kuvaan
        Intent intent = getIntent();
        final String pathValue = intent.getStringExtra("pathToFile");

        //Näytetään PATH stringi debuggauksen avuksi
        TextView myPicPreview = (TextView) findViewById(R.id.picPreview);
        myPicPreview.setText(pathValue);


        //Avataan imageviewiin kuva käyttämällä saatua PATH valueta
        Bitmap bitmap = BitmapFactory.decodeFile(pathValue);

        ImageView myImage = (ImageView) findViewById(R.id.myImagePreview);
        myImage.setImageBitmap(BitmapFactory.decodeFile(pathValue));

        //assign truepath to to resized and compressed image
        truePath = resizeAndCompressImageBeforeSend(getApplicationContext(), pathValue, "Cpic");


        final Button analysisbutton = findViewById(R.id.analyzeButton);
        final Button compressButton = findViewById(R.id.compressButton);


        compressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 truePath = resizeAndCompressImageBeforeSend(getApplicationContext(), pathValue, "Cpic");
            }
        });

        analysisbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Näytä loading animaatio ja kerro käyttäjälle että kuvaa analysoidaan
                ProgressBar progBar = (ProgressBar)findViewById(R.id.myProgressBar);
                progBar.setVisibility(View.VISIBLE);

                TextView debugView =  (TextView)findViewById(R.id.debugData);
                debugView.setText("Loading... This may take a little while...");

                detectLabels2(truePath);
                //detectLabels2(pathValue);
            }
        });


        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                //API KEY
                new VisionRequestInitializer("AIzaSyA_HI2asA_3KcYBSyFnRw_BC6vCHs867WU")
        );

        vision = visionBuilder.build();

    }

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
                            ProgressBar progBar = (ProgressBar)findViewById(R.id.myProgressBar);
                            progBar.setVisibility(View.GONE);

                           Toast.makeText(getApplicationContext(), rtrMessage, Toast.LENGTH_LONG).show();
                            TextView debugView =  (TextView)findViewById(R.id.debugData);
                            debugView.setText(rtrMessage);
                        }
                    });

                }catch (Exception e){
                    Log.d("Error", e.getMessage());
                }
            }
        });
    }

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


    public static String resizeAndCompressImageBeforeSend(Context context, String filePath, String fileName){
        //play around with the first value to reduce filesize without compromising the number of labels found
        final int MAX_IMAGE_SIZE = 200 * 1024; //max final file size in kilobytes

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        //calculate inSampleSize(First we are going to resize the image to 800x800in order to not have a big but very low quality image.
        //resizing the image will already reduce the file size, but after resizing we will check the file size and start to compress image
        options.inSampleSize = calculateInSampleSize(options, 800, 800);

        //decode bitmap with inSamplesize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bmpPic = BitmapFactory.decodeFile(filePath, options);

        int compressQuality = 100;
        int streamLength;
        do{
            ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
            Log.d("compressBitmap", "Quality: " + compressQuality);
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream);
            byte[] bmpPicByteArray = bmpStream.toByteArray();
            streamLength = bmpPicByteArray.length;
            compressQuality -= 5;
            Log.d("compressBitmap", "Size: " + streamLength/1024+ " kb");
        }while (streamLength >= MAX_IMAGE_SIZE);

        try{
            //save the resized and compressed file to disk cache
            Log.d("compressBitmap", "cacheDir: " + context.getCacheDir());
            FileOutputStream bmpFile = new FileOutputStream(context.getCacheDir() + fileName);
            bmpPic.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpFile);
            bmpFile.flush();
            bmpFile.close();
        }catch (Exception e){
            Log.e("compressBitmap", "Error on saving file");
        }
        //return the path of resized and compressed file
        return context.getCacheDir()+fileName;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        String debugTag = "MemoryInformation";

        final int height = options.outHeight;
        final int width = options.outWidth;
        Log.d(debugTag, "image height: "+ height + " ---image width: " + width);
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            //calculate the larges inSampleSize value that is a power of 2 and keeps both
            //height and width larger than the requested height and width
            while((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth){
                inSampleSize *= 2;
            }
        }
        Log.d(debugTag, "inSampleSize: " + inSampleSize);
        return inSampleSize;
    }



    //Muuten sama kuin detectLabels funktio, mutta tässä kompressataan kuva ennen lähettämistä jotta tiedostonkoko pysyy pienempänä
    // -> Prosessissa kestää huomattavasti vähemmän aikaa
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
                                ProgressBar progBar = (ProgressBar)findViewById(R.id.myProgressBar);
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

                                TextView debugView =  (TextView)findViewById(R.id.debugData);
                                debugView.setText(rtrMessage);
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

    public void resetMission(){
        //Tämän funktion voi poistaa ja käyttää sen tekstiä suoraan kohdassa missä funktiota kutsutaan

        //Käyttää UtilityFunctions luokan funktiota resetScore
        myContext = getApplicationContext();
        UtilityFunctions.resetScore(myContext);


    }

}
