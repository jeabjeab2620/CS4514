package com.example.myapplication;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VLC {
    private static final String SYNC_HEADER = "101";
    private static final int BIT_RATE = 30;
    private static final long RATE = 1000 / (BIT_RATE); // Duration of each bit in milliseconds
    private static final int INTERVAL_MS = 1000; // interval in milliseconds

    public static void test(Context context){
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraID = cameraManager.getCameraIdList()[0];

            //send_ppm(cameraManager, cameraID, "100010001000100010001000100010001000100010001000100010001000");
            String uid = "1573438017";
            String amount = "120.2";
            String data = VLC.longToBinary(Long.parseLong(uid));


        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String convertPPMToBit(String data) {
        String toReturn = "";
        for(int i = 0; i < data.length(); i += 2) {
            if (data.substring(i, i +2).equals("10") || data.substring(i, i + 2).equals("11")){
                toReturn += "1";
            }
            else
                toReturn += "0";
        }
        return toReturn;
    }

    public static void sendUID(Context context, String uid) {
        try{

            String data = VLC.longToBinary(Long.parseLong(uid));
            Log.d("Data to Send", data);
            ArrayList<String> packets = VLC.createPackets(data, 1);

            VLC.sendPackets(context, packets);

        } catch(Exception e){
        e.printStackTrace();
        }
    }

    public static void sendAmount(Context context, String amount) {
        try{
            String data = VLC.floatToBinary(Float.parseFloat(amount));
            ArrayList<String> packets =  VLC.createPackets(data, 1);

            Log.d("Data to Send", data);

            VLC.sendPackets(context, packets);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void sendPackets(Context context, ArrayList<String> packets) throws CameraAccessException, InterruptedException {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String cameraID = cameraManager.getCameraIdList()[0];
        for(String packet: packets) {
            send_ppm(cameraManager, cameraID, packet);
            Log.d("Packet", packet);
            Thread.sleep(RATE);
        }
    }

    public static String longToBinary(Long data) {

        return Long.toBinaryString(data);
    }

    public static String floatToBinary(float data) {
        int bits = Float.floatToIntBits(data);
        return String.format("%32s", Integer.toBinaryString(bits)).replace(" ", "0");
    }

    public static Float binaryToFloat(String data) {
        int bits = Integer.parseInt(data, 2);
        return Float.intBitsToFloat(bits);

    }

    public static Long binaryToLong(String data) {
        return Long.parseLong(data, 2);
    }


    private static String calculateParity(String data) {
        String parityBits = "";
        int count = 0;

        for(int j = 0; j < data.length(); j++) {
            if(data.charAt(j) == '1')
                    count++;
        }
        if(count %2 == 0)
            parityBits += '1';
        else
            parityBits += '0';

        return parityBits;
    }
    private static ArrayList<String> createPackets(String data, int dataType) {
        ArrayList<String> packets = new ArrayList<>();
        int PAYLOAD_SIZE = 9;

        for(int i = 0; i < data.length() ; i += PAYLOAD_SIZE) {

            String payload = "";
            if (i + PAYLOAD_SIZE > data.length()) {
                payload += data.substring(i);
            }
            else
                payload += data.substring(i, i+PAYLOAD_SIZE);

            String parity = VLC.calculateParity(payload);
            String dt= (dataType == 1)?"1":"0";
            String packet;
            if ( i + PAYLOAD_SIZE >= data.length())
                packet = SYNC_HEADER  + "1"+ parity + dt +  payload + SYNC_HEADER;
            else
                packet = SYNC_HEADER  + "0"+ parity + dt + payload;



            String packet_ppm = "";

            //convert "1" to "10" and "0" to "00"
            for(int j = 0; j < packet.length(); j++) {
                if (packet.charAt(j) == '1') {
                    packet_ppm += "10";
                } else {
                    packet_ppm += "00";
                }
            }
            packets.add(packet_ppm);
        }

        return packets;


    }


    private static void send_ppm(CameraManager cameraManager, String cameraID, String data) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                VLC.send_schedule(data, cameraManager, cameraID);
            }
        });
        thread.setPriority(Thread.MAX_PRIORITY);

        thread.start();

        thread.join();

    }

    private static void send_schedule(String data, CameraManager cameraManager, String cameraID) {
        Timer timer = new Timer();
        final CountDownLatch latch = new CountDownLatch(1);
        timer.scheduleAtFixedRate(new TimerTask() {
            int index = 0;
            @Override
            public void run() {
                if (index >= data.length()) {
                    try {
                        flashOff(cameraManager, cameraID);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    latch.countDown();
                    this.cancel();

                } else {
                    char bit = data.charAt(index);

                    if (bit == '1') {
                        try {
                            //flashOn(cameraManager, cameraID);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                                flashOnwithBrightness(cameraManager, cameraID, 1);
                            } else {
                                flashOn(cameraManager, cameraID);
                            }

                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            flashOff(cameraManager, cameraID);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    index++;
                }


            }
        }, 0, RATE);

        while(latch.getCount() != 0 ) {

        }
        Log.d("DONE", "DONE");
    }

    public static double predictBrightness(CameraBridgeViewBase.CvCameraViewFrame frame) {
        // Convert the frame to grayscale
        Mat gray = frame.gray();

        // Extract only the necessary region from the frame
        Rect roi = new Rect(0, 0, gray.cols(), gray.rows() / 2);
        Mat gray_roi = gray.submat(roi);

        // Compute the mean of each row using OpenCV's reduce() function
        Mat row_means = new Mat();
        Core.reduce(gray_roi, row_means, 1, Core.REDUCE_AVG);

        // Count the number of bright rows
        int brightRows = 0;
        for (int i = 0; i < row_means.rows(); i++) {
            double mean = row_means.get(i, 0)[0];
            if (mean > 180) {
                brightRows++;
            }
        }

        return (double) brightRows / row_means.rows() * 100.0;
    }

    public static String readFlashlight(CameraBridgeViewBase.CvCameraViewFrame frame){
            Mat gray = frame.gray();

            Mat binary = new Mat();
            Imgproc.threshold(gray, binary, 200, 255, Imgproc.THRESH_BINARY);

            int totalPixels = binary.rows() * binary.cols();
            int brightPixels = Core.countNonZero(binary);

            int percentage = (int) (brightPixels / (double) totalPixels * 100);

            Log.d("VLC", String.valueOf(percentage));
            if (percentage > 10) {
                return "1";
            }
            return "0";

    }

    public static Pair<Mat, String> readFlashlight2(CameraBridgeViewBase.CvCameraViewFrame frame) {
        Mat original = frame.rgba();
        Mat gray = frame.gray();
        Mat blur = new Mat();
        String flashlight = "0";
        Imgproc.GaussianBlur(gray,blur, new Size(11,11), 0);

        Mat threshold = new Mat();
        Imgproc.threshold(blur, threshold, 220, 255, Imgproc.THRESH_BINARY);
        Imgproc.erode(threshold, threshold, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)), new Point(-1, -1), 2);
        Imgproc.dilate(threshold, threshold, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)), new Point(-1, -1), 4);

        Mat edged = new Mat();
        Imgproc.Canny(threshold, edged, 50, 150);

        ArrayList<MatOfPoint> lightContours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edged, lightContours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat circles = new Mat();
        Imgproc.HoughCircles(threshold, circles, Imgproc.HOUGH_GRADIENT, 1.0, (double) threshold.rows() / 8, 100, 30, 20, 100);
        String data = "";
        if (!lightContours.isEmpty()) {
            //Find the Maxmimum Contour, this is assumed to be the light beam
            MatOfPoint maxcontour = Collections.max(lightContours, Comparator.comparingDouble(Imgproc::contourArea));
            //avoids random spots of brightness by making sure the contour is reasonably sized
            if (Imgproc.contourArea(maxcontour) > 600) {
                MatOfPoint2f contours = new MatOfPoint2f(maxcontour.toArray());
                float[] radius = new float[1];
                Point center = new Point();
                Imgproc.minEnclosingCircle(contours, center, radius);
                Imgproc.circle(original, center, (int) radius[0], new Scalar(0, 255, 0), 4);
                Imgproc.rectangle(original, new Point(center.x - 5, center.y - 5), new Point(center.x + 5, center.y + 5), new Scalar(0, 128, 255), -1);
                flashlight = "1";
            }
        }

        return new Pair<>(original, flashlight);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void flashOn(CameraManager cameraManager, String cameraID) throws CameraAccessException {

            cameraManager.setTorchMode(cameraID, true);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static void flashOnwithBrightness(CameraManager cameraManager, String cameraID, double strength) throws CameraAccessException {

        cameraManager.turnOnTorchWithStrengthLevel(cameraID, (int) strength);


    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void flashOff(CameraManager cameraManager, String cameraID) throws CameraAccessException {
            cameraManager.setTorchMode(cameraID, false);

    }

    public static void getCameraFPS(Context context){
        try{
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        Log.d("FPS", Arrays.toString(Arrays.stream(cameraManager.getCameraCharacteristics(cameraManager.getCameraIdList()[0]).get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)).toArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void send_frame_ppm(String frame, CameraManager cameraManager, String cameraID) throws CameraAccessException, InterruptedException {

        frame = SYNC_HEADER + frame;

        Log.d("Frame Length", Integer.toString(frame.length()));
        Log.d("Frame", frame);
        if (frame.length() != 30)
            return;

        for(int i = 0; i < frame.length(); i++){
            TimeUnit.MILLISECONDS.sleep(10);
            if (frame.charAt(i) == '1')
                flashOn(cameraManager, cameraID);
            else
                flashOff(cameraManager, cameraID);
            TimeUnit.MILLISECONDS.sleep(RATE-10);
            flashOff(cameraManager, cameraID);
            TimeUnit.MILLISECONDS.sleep(RATE-10);
        }



    }
    public static void send_frame(String frame, CameraManager cameraManager, String cameraID) throws CameraAccessException, InterruptedException {

        frame = SYNC_HEADER + frame;

        Log.d("Frame Length", Integer.toString(frame.length()));
        Log.d("Frame", frame);
        if (frame.length() != 30)
            return;

        for(int i = 0; i < frame.length(); i++){

            TimeUnit.MILLISECONDS.sleep(6);
            if (frame.charAt(i) == '1')
                flashOn(cameraManager, cameraID);
            else
                flashOff(cameraManager, cameraID);
            TimeUnit.MILLISECONDS.sleep(RATE - 12);
            flashOff(cameraManager, cameraID);
        }



    }
}
