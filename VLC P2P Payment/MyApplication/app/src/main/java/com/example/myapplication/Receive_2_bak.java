package com.example.myapplication;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;

public class Receive_2_bak extends AppCompatActivity {
    private String cameraID;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_2_bak);
        this.addListener();

        // Start the service

        startVideo();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
    public void addListener() {
        final Context context = this;
        Button cancelButton = (Button) findViewById(R.id.receive_2_cancel_btn);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopBackgroundThread();
                Receive_2_bak.this.finish();
            }
        });
    }
    //function for previous button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        this.finish();
        return true;
    }

    public void startVideo(){
        final Context context = this;
        this.textureView = (TextureView) findViewById(R.id.receive_2_textureview);

        if (this.textureView != null)
            this.textureView.setSurfaceTextureListener(textureListener);
        VLC.test(this);
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    private void startBackgroundThread(){
        this.mBackgroundThread = new HandlerThread("Camera Background");
        this.mBackgroundThread.start();
        this.mBackgroundHandler = new Handler(this.mBackgroundThread.getLooper());
    }
    private void stopBackgroundThread(){
        this.mBackgroundThread.quitSafely();
        try {
            this.mBackgroundThread.join();
            this.mBackgroundThread = null;
            this.mBackgroundHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = this.textureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize( textureView.getWidth(), textureView.getHeight());
            Surface surface = new Surface(texture);
            this.captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            this.captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface),new CameraCaptureSession.StateCallback() {


                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSessions = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(Receive_2_bak.this, "Configuration Change", Toast.LENGTH_SHORT).show();
                }
            }, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCamera(){
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            this.cameraID = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(this.cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }

            manager.openCamera(this.cameraID, stateCallback, null);

        } catch (CameraAccessException e){
            e.printStackTrace();
        }

    }


    private void updatePreview() {
        if (this.cameraDevice == null)
            Log.e("RECEIVE_2", "Update Preview error.");

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, this.mBackgroundHandler);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onRequestPermissionsRequest(int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "You cannot use this app without granting permission", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }


}




















