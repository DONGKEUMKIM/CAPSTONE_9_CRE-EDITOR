package com.example.detection;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.tensorflow.lite.Interpreter;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class TestDetection extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    //카메라 촬영 상태
    private static final int CAMERA_ACTIVITING = 0;
    private static final int CAMERA_STOPPED = 1;
    int cameraActivitystate = CAMERA_ACTIVITING;

    private static final String ALARMSTART = "ALARMSTART";
    private static final String ALARMEND = "ALARMEND";

    //디버그용 TAG값
    private static final String PIXEL = "전체 픽셀";
    private static final String WPIXEL = "하얀색 픽셀";
    private static final String HSTATE = "높은졸음감지 상태";
    private static final String CONFIRMALARM = "알람 확인";
    private static final String FACERECT = "얼굴 바운더리";

    int frameCount = 0;
    public static final int SKIP_FRAME = 3;
    private static final String TAG = "opencv";
    private Mat matInput;
    private Mat matResult;
    private Mat matBinary;
    private Mat eyeROI;                             //눈이미지
    private int[] faceArray;                        //얼굴 바운더리를 int배열형태로 저장
    private MatCirCularQueue frameBuffer;                   //프레임을 담을 버퍼큐

    //높은 졸음 판별 상태
    //private final static int REST = 0;
    private final static int HIGH_COUNTING = 1;
    private final static int HIGH_DETECTREADY = 2;
    private final static int HIGH_DETECTING = 3;
    private final static int HIGH_WAKE_UP = 4;
    int StateOfDetectingHighDowsiness= HIGH_COUNTING;

    //낮은 졸음 판별 상태
    private final static int LOW_COUNTING = 1;
    private final static int LOW_DETECTREADY = 2;
    private final static int LOW_DETECTING = 3;
    private final static int LOW_WAKE_UP = 4;
    int StateOfDetectingLowDowsiness= LOW_COUNTING;

    int cameraviewcount = 100;                       //60초 단위 촬영을 위한 카운트
    int detectingCount = 0;                         //높은졸음이 감지됐을때 시작되는 카운트

    //알람을 위한 브로드캐스트 리시버
    IntentFilter intentFilter;
    AlarmReceiver alarmReceiver;
    //Intent sendIntent;

    AlarmSoundService mAlarmsoundservice;
    MediaPlayer mMediaplayer;

    //UI
    TextView countView;
    TextView countdownView;
    //쓰레드 핸들러
    Handler mHandler = null;
    Handler mcountHandler = null;
    private CameraBridgeViewBase mOpenCvCameraView;


    //머신러닝 모델 파일 및 인터프리터
    Interpreter tf_lite;
    /*
    native 함수
     */
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public native void InvertMat(long matAddrInput, long matAddrResult);
    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);
    public native long loadCascade(String cascadeFileName );
    public native void detect(long cascadeClassifier_face,
                              long cascadeClassifier_eye, long matAddrInput, long matAddrResult);

    public native void detectEyeAndFaceRect(long cascadeClassifier_face,
                                  long cascadeClassifier_eye, long cascadeClassifier_righteye,
                                            long matAddrInput, long matAddrResult, long eyeROI , int[] faceArray);

    public native void makeFaceMaskImage(long matAddrInput, long matAddrResult, int[] faceRect);

    public native int CountWhitePixelsInFaceBoundary(long matAddrInput, int[] faceArray);               //쓰레드 처리 하지 않았을대
    public native int CountWhitePixelsInOneRow(long matAddrInput, int indexOfStart, int indexOfEnd , int[] faceArray);    //쓰레드 처리 하였을때

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eye = 0;
    public long cascadeClassifier_righteye = 0;

    private final Semaphore writeLock = new Semaphore(1);

    public void getWriteLock() throws InterruptedException {
        writeLock.acquire();

    }

    public void releaseWriteLock() {
        writeLock.release();
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("opencv_java4");
        //System.loadLibrary("dlib");
        System.loadLibrary("native-lib");
    }

    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void loadModel(String filename)
    {
        AssetManager assetManager = this.getAssets();
        try {
            tf_lite = new Interpreter(loadModelFile(assetManager, filename));

        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }
    }

    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

    }

    private void read_cascade_file(){
        copyFile("haarcascade_frontalface_alt.xml");
        copyFile("haarcascade_eye_tree_eyeglasses.xml");
        copyFile("haarcascade_righteye_2splits.xml");

        Log.d(TAG, "read_cascade_file:");
        cascadeClassifier_face = loadCascade( "haarcascade_frontalface_alt.xml");

        Log.d(TAG, "read_cascade_file:");
        cascadeClassifier_eye = loadCascade( "haarcascade_eye_tree_eyeglasses.xml");

        Log.d(TAG, "read_cascade_file:");
        cascadeClassifier_righteye = loadCascade( "haarcascade_righteye_2splits.xml");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_detection);

        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(1); // front-camera(1),  back-camera(0)

        eyeROI = new Mat();
        faceArray = new int[4];
        frameBuffer = new MatCirCularQueue();

        //UI처리
        countView = (TextView)findViewById(R.id.countView);
        countdownView = (TextView)findViewById(R.id.countdownview);

        //알람을 위한 처리
        alarmReceiver = new AlarmReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ALARMSTART);
        intentFilter.addAction(ALARMEND);

        mAlarmsoundservice = new AlarmSoundService();

        //UI처리 쓰레드 핸들러
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                countView.setText(msg.arg1+"");

            }

        };

        mcountHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                countdownView.setText(msg.arg1+"");

                if(msg.arg1 == 0 &&cameraActivitystate == CAMERA_ACTIVITING)
                {
                    cameraActivitystate = CAMERA_STOPPED;
                    mOpenCvCameraView.disableView();

                }
                else if(msg.arg1 == 0 && cameraActivitystate == CAMERA_STOPPED)
                {
                    cameraActivitystate = CAMERA_ACTIVITING;

                    onResume();

                    //onCameraViewStarted(mOpenCvCameraView.getWidth() , mOpenCvCameraView.getHeight());
                }
            }
        };

        /////////////////////////////////모델 로드//////////////////////////////////////
        loadModel("open_close.tflite");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        //카메라 촬영이 시작되었을때
        //60초 카운트 시작
        cameraviewcount = 100;
        countdownThread  mcountdownthread = new countdownThread();
        mcountdownthread.start();
    }

    @Override
    public void onCameraViewStopped() {
        //카메라 촬영이 정지되었을때
        //60초 카운트 시작
        cameraviewcount = 100;
        countdownThread  mcountdownthread = new countdownThread();
        mcountdownthread.start();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        matInput = inputFrame.rgba();
        Core.flip(matInput, matInput,1);
        try{
            getWriteLock();
            //matInput 과 같은 matResult , matGray생성

            if(matResult == null)
                matResult = new Mat(matInput.rows(),matInput.cols(),matInput.type());

            //LandmarkDetection(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());

            //눈, 얼굴바운더리 검출
            detectEyeAndFaceRect(cascadeClassifier_face,cascadeClassifier_eye,cascadeClassifier_righteye,
                    matInput.getNativeObjAddr(),
                    matResult.getNativeObjAddr(), eyeROI.getNativeObjAddr(), faceArray);

            Log.d("FACERECT", String.valueOf(faceArray[0]) + " " + String.valueOf(faceArray[1]) + " " +
                    String.valueOf(faceArray[2]) + " " + String.valueOf(faceArray[3])
                    + " " + String.valueOf(matResult.cols())+ " " + String.valueOf(matResult.rows()));

            //eyedrawnessTherad eyeThread = new eyedrawnessTherad();
            //eyeThread.start();

            Bitmap bmp = null;
            try {
                //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
                Imgproc.cvtColor(eyeROI, eyeROI, Imgproc.COLOR_GRAY2RGBA, 4);
                bmp = Bitmap.createBitmap(eyeROI.cols(), eyeROI.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(eyeROI, bmp);
            }
            catch (CvException e){Log.d("Exception",e.getMessage());}

            if(bmp != null)
            {
                /////////////////////////bitmap을 input배열로 변환/////////////////////////
                /////////////////////////////// 64X64  이미지가 몇X몇인지 64를 해당 값으로 바꿔줘야 함////////
                int batchNum = 0;
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                float[][][][] input = new float[1][width][height][3];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int pixel = bmp.getPixel(x, y);
                        // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                        // model. For example, some models might require values to be normalized
                        // to the range [0.0, 1.0] instead.
                        input[batchNum][x][y][0] = (Color.red(pixel)) - 127 / 128.0f;
                        input[batchNum][x][y][1] = (Color.green(pixel)) - 127 / 128.0f;
                        input[batchNum][x][y][2] = (Color.blue(pixel)) - 127 / 128.0f;
                    }
                }


                float[][] output = new float[1][1];
                tf_lite.run(input, output);

                /////////////////output 값에 따라 결정/////////////////////////////////
                if(output[0][0]>=0.5){
                    //textView.setText(output[0][0]+"open");
                    Log.d("Eye", "open");
                }
                else {
                    //textView.setText(output[0][0]+"close");
                    Log.d("Eye", "close");
                }

            }

            /////////////////////////////큰 졸음 판별 단계////////////////////////////////////////////
            //얼굴 부분 색상 검출 및 프레임 바이너리화
            if(matBinary == null)
                matBinary = new Mat(matResult.rows(),matResult.cols(),matResult.type());
            makeFaceMaskImage(matResult.getNativeObjAddr(), matBinary.getNativeObjAddr(), faceArray);
            //프레임버퍼에 프레임 저장
            frameBuffer.Enqueue(matBinary);
            //픽셀카운팅 및 졸음 판별
            CountingThread countingThread = new CountingThread();
            countingThread.start();

            if(StateOfDetectingHighDowsiness == HIGH_DETECTREADY)
            {
                StateOfDetectingHighDowsiness = HIGH_DETECTING;
                DetectHighdrowsinessThread  detectThread = new DetectHighdrowsinessThread();
                detectThread.start();
                System.out.println("디텍트스레드가 시작되었습니다.");
                //카운트다운 스레드 시작 (10초)
                //10초 카운트 스레드 종료 후 알람 이벤트 발생

                //단 카운팅 스레드에서 다시 얼굴을 감지 했을시
                //중간에 종료 할 수 있어야함

                //이벤트 발생중에도 다시 얼굴을 감지 했을시
                //중간에 종료
            }
            else if(StateOfDetectingHighDowsiness == HIGH_DETECTING)
            {

                if(detectingCount == 10)
                {
                    System.out.println("방송 송출");
                    StateOfDetectingHighDowsiness = HIGH_WAKE_UP;
                    //알람 리시버에게 알람수행을 위한 메시지 송신

                    registerReceiver(alarmReceiver, intentFilter);
                    Intent sendIntent = new Intent(ALARMSTART);
                    sendBroadcast(sendIntent);
                    //intentFilter.addAction(ALARMSTART);
                    //registerReceiver(alarmReceiver,intentFilter);

                    //send
                    //높은 졸음 알람 발생!!
                    //Log.d("CONFIRMALARM", "알람이 울리고 있습니다.");
                }
            }

            /////////////////////////////낮은 졸음 판별 단계////////////////////////////////////////////
            //eyeROI (검출된 눈 바운더리 이미지)
            //학습된 모델로 감은눈인지 뜬 눈인지 실시간 판별
            //판별후 나온 결과를 boolean 값으로 리턴 (뜬 눈 = true , 감은 눈 = false)
            //주석 지울것
            /*if(판별 리턴값 == false && StateOfDetectingLowDowsiness == LOW_COUNTING)
            {
                //눈 상태가 감은 눈일 경우
                //상태 변경 COUNTING -> DETECTREADY
                StateOfDetectingLowDowsiness = LOW_DETECTREADY;
            }

            if(StateOfDetectingHighDowsiness == LOW_DETECTREADY)
            {
                //상태변경 DETECTREADY -> DETECTING
                StateOfDetectingLowDowsiness = LOW_DETECTING;

                //눈이 감긴걸로 판별이 되면
                //카운트를 증가 시켜주는 스레드가 시작 (10초 동안 지속)
                DetectLowdrowsinessThread  detectrowThread = new DetectLowrowsinessThread();
                detectrowThread.start();
                System.out.println("디텍트스레드가 시작되었습니다.");
                //카운트다운 스레드 시작 (10초)
                //10초 카운트 스레드 종료 후 알람 이벤트 발생
            }
            else if(StateOfDetectingLowDowsiness == LOW_DETECTING)
            {
                //다시 뜬 눈으로 감지 될 경우
                if(판별 리턴값 == true)
                {
                //상태 변경 DETECTING -> COUNTING
                StateOfDetectingLowDowsiness = LOW_COUNTING;
                }

                if(detectingCount == 10)
                {
                    System.out.println("방송 송출");
                    StateOfDetectingLowDowsiness = LOW_WAKE_UP;
                    //알람 리시버에게 알람수행을 위한 메시지 송신

                    registerReceiver(alarmReceiver, intentFilter);
                    Intent sendIntent = new Intent(ALARMSTART);
                    sendBroadcast(sendIntent);
                    //intentFilter.addAction(ALARMSTART);
                    //registerReceiver(alarmReceiver,intentFilter);

                    //send
                    //낮은 졸음 알람 발생!!
                    //Log.d("CONFIRMALARM", "알람이 울리고 있습니다.");
                }
            }
            else if(StateOfDetectingLowDowsiness == LOW_WAKE_UP)
                {
                    //낮은 졸음 감지중
                    //다시 뜬 눈으로 검출 됐을 경우 원래 상태로 복귀

                    //리시버에게 알람을 종료하라는 메시지 송신
                    registerReceiver(alarmReceiver, intentFilter);
                    Intent sendIntent = new Intent(ALARMEND);
                    sendBroadcast(sendIntent);

                    //intentFilter = new IntentFilter();
                    //intentFilter.addAction(ALARMEND);
                    //registerReceiver(alarmReceiver,intentFilter);

                    StateOfDetectingLowDowsiness = LOW_COUNTING;
                    detectingCount = 0;
                    countView.setText("0");
                 }
            */


            //판별 후 메모리 해제 필요

            Log.d("HSTATE", String.valueOf(StateOfDetectingHighDowsiness));

        } catch(InterruptedException e){
            e.printStackTrace();
        }
        releaseWriteLock();
        return matResult;
    }


    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }


    //여기서부턴 퍼미션 관련 메소드
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;


    protected void onCameraPermissionGranted() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();
        if (cameraViews == null) {
            return;
        }
        for (CameraBridgeViewBase cameraBridgeViewBase: cameraViews) {
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.setCameraPermissionGranted();

                read_cascade_file();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean havePermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
                havePermission = false;
            }
        }
        if (havePermission) {
            onCameraPermissionGranted();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED&& grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted();
        }else{
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder( TestDetection.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }

    public class eyedrawnessTherad extends Thread{
        @Override
        public void run() {
            howEyedrawness();
        }
    }

    private synchronized void howEyedrawness(){
        Bitmap bmp = null;
        try {
            //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
            Imgproc.cvtColor(eyeROI, eyeROI, Imgproc.COLOR_GRAY2RGBA, 4);
            bmp = Bitmap.createBitmap(eyeROI.cols(), eyeROI.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(eyeROI, bmp);
        }
        catch (CvException e){Log.d("Exception",e.getMessage());}

        if(bmp != null)
        {
            /////////////////////////bitmap을 input배열로 변환/////////////////////////
            /////////////////////////////// 64X64  이미지가 몇X몇인지 64를 해당 값으로 바꿔줘야 함////////
            int batchNum = 0;
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            float[][][][] input = new float[1][width][height][3];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = bmp.getPixel(x, y);
                    // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                    // model. For example, some models might require values to be normalized
                    // to the range [0.0, 1.0] instead.
                    input[batchNum][x][y][0] = (Color.red(pixel)) - 127 / 128.0f;
                    input[batchNum][x][y][1] = (Color.green(pixel)) - 127 / 128.0f;
                    input[batchNum][x][y][2] = (Color.blue(pixel)) - 127 / 128.0f;
                }
            }


            float[][] output = new float[1][1];
            tf_lite.run(input, output);

            /////////////////output 값에 따라 결정/////////////////////////////////
            if(output[0][0]>=0.5){
                //textView.setText(output[0][0]+"open");
                Log.d("Eye", "open");
            }
            else {
                //textView.setText(output[0][0]+"close");
                Log.d("Eye", "close");
            }

        }

    }

    public class CountingThread extends Thread{
        public void run(){
            countUsingWhitePixel();
        }
    }
    public void countUsingWhitePixel(){
        Mat matFrame = frameBuffer.Dequeue();


        int countofPixels = faceArray[2] * faceArray[3];                                            //얼굴 바운더리 내의 전체 픽셀개수
        Log.d("PIXEL", String.valueOf(countofPixels));

        int sumofWhitePixels = sumOfWhitePixels(matFrame,4, faceArray);
        Log.d("WPIXEL", String.valueOf(sumofWhitePixels));

        //높은 졸음 판별 시작 준비 상태로
        ChangeStateToReady(sumofWhitePixels, countofPixels);
    }

    private synchronized void ChangeStateToReady(int sumofWhitePixels, int countofPixels)
    {

            //검출된 얼굴의 픽셀값이 전체 픽셀의 1/5 이하일때
            //높은 졸음 판별 시작
            if(StateOfDetectingHighDowsiness == HIGH_COUNTING
                    && sumofWhitePixels < countofPixels / 5)
            {
                StateOfDetectingHighDowsiness = HIGH_DETECTREADY;
            }
            else if(StateOfDetectingHighDowsiness == HIGH_WAKE_UP
                    && sumofWhitePixels > countofPixels / 5)
            {
                //높은 졸음 감지중
                //다시 얼굴이 검출 됐을 경우 원래 상태로 복귀

                //리시버에게 알람을 종료하라는 메시지 송신
                registerReceiver(alarmReceiver, intentFilter);
                Intent sendIntent = new Intent(ALARMEND);
                sendBroadcast(sendIntent);

                //intentFilter = new IntentFilter();
                //intentFilter.addAction(ALARMEND);
                //registerReceiver(alarmReceiver,intentFilter);

                StateOfDetectingHighDowsiness = HIGH_COUNTING;
                detectingCount = 0;
                countView.setText("0");
            }
        else if (StateOfDetectingHighDowsiness == HIGH_DETECTING
                && sumofWhitePixels > countofPixels / 5)
        {
            StateOfDetectingHighDowsiness = HIGH_COUNTING;
            detectingCount = 0;
            countView.setText("0");
        }
    }

    class CountPixelThread extends Thread{
        int indexOfStart;
        int indexOfEnd;
        long matInput;
        int ans;
        CountPixelThread(long matInput, int start, int end){
            this.matInput = matInput;
            this.indexOfStart = start;
            this.indexOfEnd = end;
            this.ans = 0;}

        @Override
        public void run() {
            this.ans = CountWhitePixelsInOneRow(matInput,indexOfStart,indexOfEnd,faceArray);
        }
    }

    private int sumOfWhitePixels(Mat mSource, int numberOfThread, int[] faceArray){
        int sum = 0;
        CountPixelThread countPixelThread[] = new CountPixelThread[numberOfThread];
        for(int i = 0; i < numberOfThread; i ++){
            countPixelThread[i] = new CountPixelThread(mSource.getNativeObjAddr(), faceArray[1] + i*(faceArray[3]/numberOfThread),faceArray[1] +(i+1)*(faceArray[3]/numberOfThread));
            countPixelThread[i].start();
        }
        for(int i = 0; i < numberOfThread; i++){
            try {
                countPixelThread[i].join();
                sum += countPixelThread[i].ans;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return sum;
    }

    public class DetectHighdrowsinessThread extends Thread{
        public void run(){
            while(detectingCount < 10 && StateOfDetectingHighDowsiness == HIGH_DETECTING){
                Message message = mHandler.obtainMessage();
                detectingCount++;
                message.arg1 = detectingCount;
                mHandler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("디텍트스레드가 종료되었습니다.");

        }
    }

    public class DetectLowdrowsinessThread extends Thread{
        public void run(){
            while(detectingCount < 10 && StateOfDetectingHighDowsiness == LOW_COUNTING){
                Message message = mHandler.obtainMessage();
                detectingCount++;
                message.arg1 = detectingCount;
                mHandler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("디텍트스레드가 종료되었습니다.");
        }
    }

    public class countdownThread extends Thread{
        public void run(){
            while(cameraviewcount > 0){
                Message message = mcountHandler.obtainMessage();
                cameraviewcount--;
                message.arg1 = cameraviewcount;
                mcountHandler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("디텍트스레드가 종료되었습니다.");
        }
    }

    public class AlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if(act.equals(ALARMSTART))
            {
                System.out.println("방송을 받았습니다.");
                Intent mServiceintent = new Intent (context, AlarmSoundService.class);
                //알람 시작 방송을 받았을때
                context.startService(mServiceintent);
            }
            else if(act.equals(ALARMEND))
            {
                //알람 종료 방송을 받았을때
                System.out.println("방송을 받았습니다.");
                Intent mServiceintent = new Intent (context, AlarmSoundService.class);
                context.stopService(mServiceintent);
            }
            //unregisterReceiver(alarmReceiver);
        }
    }

    public class AlarmSoundService extends Service{
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        public AlarmSoundService()
        {

        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            Toast.makeText(this, "알람이 울립니다.", Toast.LENGTH_SHORT).show();
            //return super.onStartCommand(intent, flags, startId);
            mMediaplayer = MediaPlayer.create(this, R.raw.alarmsound);
            mMediaplayer.start();

            return START_NOT_STICKY;
        }

        @Override
        public void onDestroy() {
            unregisterReceiver(alarmReceiver);
            mMediaplayer.stop();
            mMediaplayer.release();
            super.onDestroy();
        }
    }
}


