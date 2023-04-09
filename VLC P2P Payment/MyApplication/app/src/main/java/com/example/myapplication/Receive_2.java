package com.example.myapplication;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class Receive_2 extends CameraActivity {

    private JavaCamera2View mOpenCVCameraView;
    private RandomAccessQueue<Character> data = new RandomAccessQueue<>();
    private String packetData = "";
    private Boolean mStopCamera = false;
    private Thread imageProcessListener;
    private String mUID;
    private String mTID = "";
    private String senderUID;

    private Thread mWaitStatusThread;
    private Timer mWaitStatusTimer;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case LoaderCallbackInterface.SUCCESS:{
                    Log.v("OpenCV", "OpenCV Loaded");
                    mOpenCVCameraView.enableView();
                    mOpenCVCameraView.setMaxFrameSize(640, 480);
                }break;
                default:{
                    super.onManagerConnected(status);
                }break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.receive_2);

        this.addListener();
         String amount = getIntent().getStringExtra("amount");

        imageProcessListener =  new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mStopCamera) {
                    try {
                        // Wait for 100 milliseconds
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Stop the camera
                mOpenCVCameraView.disableView();

                try {
                    (new APIRequest()).getToken(getApplicationContext(), new ServerCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                if (response.has("error")) {
                                    Log.e("API Call Error", response.getString("error"));
                                    Toast.makeText(getApplicationContext(),  response.getString("error"),Toast.LENGTH_LONG).show();
                                    finish();
                                } else {
                                    String token = response.getString("access_token");

                                    (new APIRequest()).requestPayment(Receive_2.this, token,senderUID, mUID, amount, new ServerCallback() {
                                        @Override
                                        public void onSuccess(JSONObject response) {
                                            try{
                                                if(response.has("error")) {
                                                    Log.e("API Call Error", response.getString("error"));

                                                    Toast.makeText(getApplicationContext(),  response.getString("error"),Toast.LENGTH_LONG).show();
                                                    finish();
                                                } else{
                                                    mTID = response.getString("tid");
                                                    Toast.makeText(getApplicationContext(), "Payment Request Success", Toast.LENGTH_LONG).show();
                                                    VLC.sendUID(getApplicationContext(), mTID);

                                                    waitForStatus();


                                                }
                                            } catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            (new APIRequest()).getToken(this, new ServerCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("error")) {
                            Log.e("API Call Error", response.getString("error"));
                        } else {
                            String token = response.getString("access_token");

                            (new APIRequest()).getUID(Receive_2.this, token, new ServerCallback() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    try{
                                        if(response.has("error")) {
                                            Log.e("API Call Error", response.getString("error"));
                                        } else{
                                            mUID = response.getString("uid");


                                            getPermission();

                                            mOpenCVCameraView.setCvCameraViewListener(cvCameraViewListener2);

                                            mOpenCVCameraView.setVisibility(View.VISIBLE);

                                            //start image processing detection
                                            imageProcessListener.start();

                                        }
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        mOpenCVCameraView = findViewById(R.id.receive_2_camera_view);

    }

    private void waitForStatus() {
        mWaitStatusTimer = new Timer();
        final CountDownLatch latch = new CountDownLatch(1);

        mWaitStatusThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mWaitStatusTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            (new APIRequest()).getToken(getApplicationContext(), new ServerCallback() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    try {
                                        if (response.has("error")) {
                                            Log.e("API Call Error", response.getString("error"));
                                            Toast.makeText(getApplicationContext(),  response.getString("error"),Toast.LENGTH_LONG).show();

                                        } else {
                                            String token = response.getString("access_token");

                                            (new APIRequest()).getTransactionStatus(getApplicationContext(), token, mTID,new ServerCallback() {
                                                @Override
                                                public void onSuccess(JSONObject response) {
                                                    try{
                                                        if(response.has("error")) {
                                                            Log.e("API Call Error", response.getString("error"));
                                                            Toast.makeText(getApplicationContext(),  response.getString("error"),Toast.LENGTH_LONG).show();
                                                            cancel();
                                                        } else{
                                                            String mStatus = response.getString("status");

                                                            if (mStatus.equals("APPROVED")) {

                                                                latch.countDown();
                                                                Toast.makeText(getApplicationContext(), "APPROVED", Toast.LENGTH_SHORT).show();
                                                                cancel();
                                                            }
                                                            else if(mStatus.equals("REJECTED")) {
                                                                latch.countDown();
                                                                Toast.makeText(getApplicationContext(), "REJECTED", Toast.LENGTH_SHORT).show();
                                                                cancel();
                                                            }
                                                        }
                                                    } catch (Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });

                                        }
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 0, 5000);

                while(latch.getCount() != 0 ) {

                }

                finish();
            }
        });

        mWaitStatusThread.start();
    }

    public void addListener() {
        final Context context = this;
        Button cancelButton = (Button) findViewById(R.id.receive_2_cancel_btn);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try{

                    mWaitStatusTimer.cancel();
                    mWaitStatusThread.stop();
                    (new APIRequest()).cancelPayment(context, mTID, new ServerCallback(){

                        @Override
                        public void onSuccess(JSONObject response) {
                            try{
                                if (response.has("error")){
                                    Toast.makeText(getApplicationContext(), response.getString("error"),Toast.LENGTH_LONG).show();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "Cancel Success", Toast.LENGTH_SHORT).show();
                                }


                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
                Receive_2.this.finish();
            }
        });
    }

    private CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener2 = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {
        }

        @Override
        public void onCameraViewStopped() {

        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            Mat rgba = inputFrame.rgba();
            Mat gray = inputFrame.gray();
            Mat threshold = new Mat();
            Imgproc.GaussianBlur(gray, gray, new Size(9, 9), 2, 2);
            Imgproc.threshold(gray, threshold, 180, 255, Imgproc.THRESH_BINARY);

            Mat hierarchy = new Mat();
            java.util.List<MatOfPoint> contours = new java.util.ArrayList<>();
            Imgproc.findContours(threshold, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

            MatOfPoint largestContour = null;
            double largestArea = 0;
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);

                double spotPercentage = area / (rgba.size().width * rgba.size().height) * 100;
                if (area > largestArea && spotPercentage >= 0.5 && spotPercentage <= 10) {
                    largestContour = contour;
                    largestArea = area;
                }
            }

            if (largestContour != null) {
                double spotPercentage = largestArea / (rgba.size().width * rgba.size().height) * 100;
                Mat mask = Mat.zeros(rgba.size(), CvType.CV_8UC1);
                Imgproc.drawContours(mask, Collections.singletonList(largestContour), 0, new Scalar(255), -1);
                Scalar mean = Core.mean(rgba, mask);
                Imgproc.drawContours(rgba, Collections.singletonList(largestContour), -1, new Scalar(0, 255, 0), 2);
                Imgproc.putText(rgba, String.format("Area Size: %.2f", spotPercentage), new Point(50, 100), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255), 2);
                Imgproc.putText(rgba, String.format("Mean Area: %.2f", mean.val[0]), new Point(50, 50), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255), 2);

                if (mean.val[0] >= 200) {
                    data.enqueue('1');
                } else
                    data.enqueue('0');

            }
            else
                data.enqueue('0');
            Log.d("Flashlight Data", data.getString());

            String endFlag = "0";
            if (data.size() > 60 && VLC.convertPPMToBit(data.getHeader()).equals("101"))
            {
                String packet = data.getPacket();
                String converted = VLC.convertPPMToBit(packet);

                endFlag = converted.substring(3, 4);
                String parityBit = converted.substring(4,5);
                String dataType = converted.substring(5,6);
                String payload;
                Log.d("END FLAG", endFlag);
                if (endFlag.equals("0"))
                {
                    payload = converted.substring(6, 15);
                    data.dequeue(30);
                }
                else
                {
                    payload = converted.substring(6);

                    int i = payload.length() - 3;
                    for(; i > 0; i--) {
                        if(payload.startsWith("101", i)) {
                            payload = payload.substring(0, i);
                            break;
                        }
                    }
                    data.dequeue(6 + i);
                }
                packetData += payload;

                Log.d("PAYLOAD RESULT", payload);

            }

            if(endFlag.equals("1")) {
                Log.d("PACKET RESULT", packetData);
                senderUID = String.valueOf(VLC.binaryToLong(packetData));
                Toast.makeText(getApplicationContext(), "UID: " + String.valueOf(VLC.binaryToLong(packetData)), Toast.LENGTH_LONG).show();
                mStopCamera = true;

            }



            return rgba;
        }
    };

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(this.mOpenCVCameraView);
    }

    void getPermission() {
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_LONG).show();
        if(mOpenCVCameraView != null) {
            mOpenCVCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV not found. Initialising.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);

        } else{
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mOpenCVCameraView != null) {
            mOpenCVCameraView.disableView();
        }
    }
}




















