package teravainen.imagegameapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TakePictureActivity extends AppCompatActivity {

    private TextureView textureView;
    private Button takePictureButton;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private static final String TAG = "TakePictureActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        textureView = (TextureView) findViewById(R.id.textureView2);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        takePictureButton = (Button) findViewById(R.id.buttonCapture);
        assert takePictureButton != null;
        takePictureButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                takePicture();
            }
        });
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback(){
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result){
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(TakePictureActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };

    private void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("Kameran Tausta");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        //Tähän tehdään kuvan ottamisfunktio
       if(null == cameraDevice){
           Log.e(TAG, "cameradevice is null");
           return;
       }
       CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
       try{
           CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
           Size[] jpegSizes = null;
           if(characteristics != null){
               jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
           }
           int width = 640;
           int height = 480;
           if(jpegSizes != null && 0 < jpegSizes.length){
               width = jpegSizes[0].getWidth();
               height = jpegSizes[0].getHeight();
           }
           ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1 );
           List<Surface> outputSurfaces = new ArrayList<Surface>(2);
           outputSurfaces.add(reader.getSurface());
           outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
           final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
           captureBuilder.addTarget(reader.getSurface());
           captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
           //orientation
           int rotation = getWindowManager().getDefaultDisplay().getRotation();
           captureBuilder.set(captureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));

           //testi muuttuja mypathiin tallennetaan kuvan tallennusnimi
           final String myPath = Environment.getExternalStorageDirectory()+"/pic.jpg";

           //final File file = new File(Environment.getExternalStorageDirectory()+"/pic.jpg");
           final File file = new File(myPath);

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try{
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }catch (IOException e){
                        e.printStackTrace();
                    }finally{
                        if (image != null){
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException{
                    OutputStream output = null;
                    try{
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    }finally{
                        if (null != output){
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback(){
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result){
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(TakePictureActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();

                    //Avataan uusi activity johon lähetetään tallennetun kuvan PATH
                    //Suljetaan tämä aktiviteetti, jotta kamera ei aukea jos käyttäjä palaa takaisinpäin

                    Intent picIntent = new Intent(TakePictureActivity.this, PicTaken.class);
                    picIntent.putExtra("pathToFile",myPath);
                    TakePictureActivity.this.startActivity(picIntent);
                    finish();


                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try{
                        session.capture(captureBuilder.build(),captureListener, mBackgroundHandler);
                    }catch (CameraAccessException e){
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
       }catch (CameraAccessException e){
           e.printStackTrace();
       }
    }

    private void createCameraPreview() {
        try{
            //tehdään textureviewiin kuvaa kamerasta
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(null == cameraDevice){
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(TakePictureActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                    }
                },null);
            } catch(CameraAccessException e){
                e.printStackTrace();
            }
    }

    private void openCamera() {
        //avataan kamera
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(TakePictureActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        }
        catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        //päivitetään kuvaa textureviewiin
        if(null == cameraDevice){
            Log.e(TAG, "updatepreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void closeCamera(){
        if(null != cameraDevice){
            cameraDevice.close();
            cameraDevice = null;
        }
        if(null != imageReader){
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                //close app
                Toast.makeText(TakePictureActivity.this, "Sorry, can't use this app without granting permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }
    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

}