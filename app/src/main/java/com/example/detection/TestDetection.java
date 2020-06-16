package com.example.detection;

import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.detection.db.SQLiteManager;
import com.example.detection.db.ScheduleData;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
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
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

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
    private static final String LSTATE = "낮은졸음감지 상태";
    private static final String HSV = "HSV";
    private static final String FRAME = "FRAME";



    int frameCount = 0;
    public static final int SKIP_FRAME = 3;
    private static final String TAG = "opencv";
    private Mat matInput;
    private Mat matResult;
    private Mat matBinary;
    private Mat eyeROI;                             //눈이미지
    private int[] faceArray;                        //얼굴 바운더리를 int배열형태로 저장
    private int[] eyeArray;                         //눈 바운더리를 int배열형태로 저장
    private boolean openOrClose;                        // 눈 뜨고있으면 true 감으면 false
    private MatCirCularQueue frameBuffer;                   //프레임을 담을 버퍼큐

    private final static int SETTING_ON = 1;
    private final static int SETTING_OFF = 2;
    int StateOfSetting= SETTING_ON;

    private final static int REST = 0;

    //높은 졸음 판별 상태
    private final static int HIGH_COUNTING = 1;
    private final static int HIGH_DETECTREADY = 2;
    private final static int HIGH_DETECTING = 3;
    private final static int HIGH_WAKE_UP = 4;
    int StateOfDetectingHighDowsiness= REST;

    //낮은 졸음 판별 상태
    private final static int LOW_COUNTING = 1;
    private final static int LOW_DETECTREADY = 2;
    private final static int LOW_DETECTING = 3;
    private final static int LOW_WAKE_UP = 4;
    private final static int LOW_NOTACT = 5;
    int StateOfDetectingLowDowsiness= REST;

    private boolean islowcountupThreadRun = false;
    private boolean ishighcountupThreadRun = false;

    private boolean allthreadisrunning = true;

    int settingcount = 15;

    //과목 공부 시간  - 시간 단위
    //시간 -> 초 로 바꿀 필요
    //일단은 테스트로 분단위
    int subjectduringtime = 4;

    int cameraviewcount = 60;                       //60초 단위 촬영을 위한 카운트

    int high_detectingCount = 0;                            //높은졸음이 감지됐을때 시작되는 카운트
    int low_detectingCount = 0;                             //낮은졸음이 감지됐을때 시작되는 카운트

    int hsv_array[];

    //CountDownTimer duringTimecdTimer;

    //메인으로부터 넘겨 받을 정보
    private String scheduleID;     //선택된 스케줄의의 ID값
    private String subjectSN;       //선택된 과목 이름
    private String scheduleDate;    //선택된 날짜
    private int subjectDT;          //선택된 공부 시간

    private String conversionDT;


    //countdownThread  mcountdownthread;
    //settingcountdownThread msettingcountdownthread;

    //알람음 설정
    public static int alarmType = 1;
    //알람을 위한 브로드캐스트 리시버
    IntentFilter intentFilter;
    AlarmReceiver alarmReceiver;
    //Intent sendIntent;

    AlarmSoundService mAlarmsoundservice;

    //UI
    TextView highcountView;
    TextView lowcountView;
    TextView countdownView;
    TextView opencloseView;

    TextView settingtextView;
    TextView settingcountView;

    TextView subjectnameView;
    TextView duringtimeView;

    ImageView backgroundImageView;

    Button backBtn;
    //쓰레드 핸들러
    Handler mhighcountHandler = null;
    Handler mlowcountHandler = null;
    Handler mcountHandler = null;
    Handler msettingcountHnadler = null;

    private CameraBridgeViewBase mOpenCvCameraView;

    private int drawnessCounter = 0;




    //머신러닝 모델 파일 및 인터프리터
    Interpreter tf_lite;

    //프레임수
    int frameCounter = 0;


    /*
    native 함수
     */
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public native void InvertMat(long matAddrInput, long matAddrResult);
    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);
    public native long loadCascade(String cascadeFileName );
    public native void detect(long cascadeClassifier_face,
                              long cascadeClassifier_eye, long matAddrInput, long matAddrResult);

    public native int detectEyeAndFaceRect(long cascadeClassifier_face,
                                            long cascadeClassifier_eye, long cascadeClassifier_righteye,
                                            long matAddrInput, long matAddrResult, long eyeROI , int[] faceArray, int[] eyeArray);


    public native int getHSVfromImg(long matAddrInput, int[] faceArray , int []array);
    public native int getHfromInputImg(long matAddrInput , int[] faceArray);
    public native void makeFaceMaskImage(long matAddrInput, long matAddrResult, int[] faceRect, int[] hsvarray);

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
        eyeArray = new int[4];
        frameBuffer = new MatCirCularQueue();

        hsv_array = new int[3];

        //UI처리
        highcountView = (TextView)findViewById(R.id.HIGHSLEEPNUM);
        lowcountView = (TextView)findViewById(R.id.LOWSLEEPNUM);
        countdownView = (TextView)findViewById(R.id.COUNTDOWNNUM);
        opencloseView = findViewById(R.id.open_close);

        settingtextView = (TextView)findViewById(R.id.SETTING);
        settingcountView = (TextView)findViewById(R.id.setting_count);

        subjectnameView = (TextView)findViewById(R.id.subject_name);
        duringtimeView = (TextView)findViewById(R.id.duringtime);

        /**
        * 과목 ID, 과목 이름, 과목 공부시간 셋팅
        */
        scheduleID = getIntent().getExtras().getString("SID");
        subjectSN = getIntent().getExtras().getString("SN");
        subjectDT = getIntent().getExtras().getInt("DT");
        scheduleDate = getIntent().getExtras().getString("DATE");


        conversionDT = convert2conversionDTfromInt(subjectDT);

        subjectnameView.setText(subjectSN);
        duringtimeView.setText("공부시간 : " + String.valueOf(subjectDT) + "시간");

        //일단은 테스트용
        //subjectnameView.setText("캡스톤프로젝트");

        //subjectduringtime = 4;

        //duringtimeView.setText(String.valueOf(subjectduringtime));


        backBtn = (Button)findViewById(R.id.backbtn);

        backgroundImageView = (ImageView)(findViewById(R.id.backgroundimg));
        //알람을 위한 처리
        alarmReceiver = new AlarmReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ALARMSTART);
        intentFilter.addAction(ALARMEND);

        mAlarmsoundservice = new AlarmSoundService();

        /*duringTimecdTimer = new CountDownTimer(30000 , 1000) {


            @Override
            public void onTick(long millisUntilFinished) {
                duringtimeView.setText("공부시간 : " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                //학습 종료
            }
        };
        */

        //mcountdownthread = new countdownThread();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //뒤로가기는 일종의 치트키 개념
                //데모를 진행 할 때 전체 학습 시간 동안 보여 줄 수가 없기 때문에
                //이 버튼을 이용하여 빠져 나가야 한다
                //이 버튼을 눌렀을 때 마치 학습 시간이 다 된것 과 같이 작동 하도록 한다.
                //첫번째 방법 -> 현재 돌고 있는 모든 스레드를 종료 시키고 메인 액티비티로 이동
                //두번째 방법 -> 남은 학습 시간을 1초로 변경

                //만약 알람이 울리고 있을 경우
                //알람 종료

                //모든 스레드를 종료 시켜주는 조건
                allthreadisrunning = false;

                ScheduleData data = SQLiteManager.sqLiteManager.selectScheduleDataFormID(scheduleID);

                //이행여부를 1로 바꿔서 DB업데이트
                SQLiteManager.sqLiteManager.updateScheduleData(new ScheduleData(data.getID(), data.getSubject_ID(), data.getDate(), data.getDuringtime() , 1));

                ScheduleData data2 = SQLiteManager.sqLiteManager.selectScheduleDataFormID(scheduleID);

                int kkk = data2.getIsDone();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);

                if(drawnessCounter < 3)
                {
                    //졸음 횟수가 3회 미만 일때
                    //칭찬메세지
                    intent.putExtra("backfromDetection", 1);
                }
                else
                {
                    intent.putExtra("backfromDetection", 2);
                }

                //알람이 울리고 있는 경우 알람 종료
                registerReceiver(alarmReceiver, intentFilter);
                Intent sendIntent = new Intent(ALARMEND);
                sendBroadcast(sendIntent);


                startActivity(intent);
                finish();
            }
        });

        //UI처리 쓰레드 핸들러
        mhighcountHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                highcountView.setText(msg.arg1+"");

            }

        };

        mlowcountHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                lowcountView.setText(msg.arg1+"");

            }

        };

        mcountHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                countdownView.setText(msg.arg1+"");

                if(msg.arg1 == 0)
                {
                    //60초 카운트가 지났을 때
                    //휴식구간 or 감지구간 선택

                    /*
                    if(subjectduringtime == 0)
                    {
                        //druingtime 이 다 지났을 때 학습종료
                        //학습 이행 여부 변경 및 인텐트값 전달
                        //학습 이행이 잘 됐을 때 1 전달
                        //학습 이행이 잘 안됐을대 2 전달
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.putExtra("backfromDetection", 1);
                        startActivity(intent);
                        finish();

                        return;

                    }
                    subjectduringtime--;
                    duringtimeView.setText(String.valueOf(subjectduringtime));
                    */
                    Random rnd = new Random();
                    int randomValue = rnd.nextInt(3);   //0~2 까지의 난수 생성

                    System.out.println("난수발생 " + String.valueOf(randomValue));

                    //난수가 0 ,1 일 때 휴식구간으로 셋팅    2/3 확률
                    if (randomValue == 0 || randomValue == 1)
                    {
                        StateOfDetectingHighDowsiness = REST;
                        StateOfDetectingLowDowsiness = REST;

                        highcountView.setVisibility(View.INVISIBLE);
                        lowcountView.setVisibility(View.INVISIBLE);
                        opencloseView.setVisibility(View.INVISIBLE);

                        if(cameraActivitystate == CAMERA_ACTIVITING)
                        {
                            //감지구간에서 넘어왔을 때
                            cameraActivitystate = CAMERA_STOPPED;
                            mOpenCvCameraView.disableView();
                        }
                        else
                        {
                            cameraviewcount = 10;

                            countdownThread  mcountdownthread = new countdownThread();
                            mcountdownthread.start();
                        }
                    }
                    else
                    {
                        highcountView.setVisibility(View.VISIBLE);
                        lowcountView.setVisibility(View.VISIBLE);
                        opencloseView.setVisibility(View.VISIBLE);

                        StateOfDetectingHighDowsiness = HIGH_COUNTING;
                        StateOfDetectingLowDowsiness = LOW_COUNTING;

                        if(cameraActivitystate == CAMERA_STOPPED)
                        {
                            //휴식구간에서 넘어왔을 때
                            cameraActivitystate = CAMERA_ACTIVITING;
                            onResume();
                        }
                        else
                        {
                            cameraviewcount = 60;

                            countdownThread  mcountdownthread = new countdownThread();
                            mcountdownthread.start();
                        }
                    }
                }
            }
        };

        msettingcountHnadler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                settingcountView.setText(msg.arg1+"");

                if(msg.arg1 == 0 && StateOfSetting == SETTING_ON)
                {

                    settingcountView.setVisibility(View.INVISIBLE);
                    settingtextView.setVisibility(View.INVISIBLE);

                    StateOfSetting = SETTING_OFF;
                    settingtextView.setText("셋팅 완료");

                    //카운트 다운 시작
                    if(conversionDT != null)
                        countDown(conversionDT);

                    //셋팅이 끝났을 때
                    //                    //휴식구간부터 시작
                    StateOfDetectingHighDowsiness = REST;
                    StateOfDetectingLowDowsiness = REST;

                    highcountView.setVisibility(View.INVISIBLE);
                    lowcountView.setVisibility(View.INVISIBLE);
                    opencloseView.setVisibility(View.INVISIBLE);


                    cameraActivitystate = CAMERA_STOPPED;
                    mOpenCvCameraView.disableView();
                }
            }
        };

        /////////////////////////////////모델 로드//////////////////////////////////////
        loadModel("open_close.tflite");

        mOpenCvCameraView.setMaxFrameSize(1024 , 576);
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
        System.out.println("카메라 다시 시작");
        if(backgroundImageView != null)
        {
            //카메라 촬영 시작시
            //이미지는 얼굴 프레임 이미지
            backgroundImageView.setImageResource(R.drawable.face);
        }

        cameraviewcount = 60;

        if(StateOfSetting == SETTING_ON)
        {
            //셋팅 상태 일 경우
            //60초 카운트 시작 X

            //15초간의 셋팅
            //셋팅카운트스레드 시작
            settingcountdownThread msettingcountdownthread = new settingcountdownThread();
            msettingcountdownthread.start();
        }
        else if(StateOfSetting == SETTING_OFF)
        {
            //셋팅 상태가 아닐때
            //60초 카운트 시작
            countdownThread  mcountdownthread = new countdownThread();
            mcountdownthread.start();
        }
    }

    @Override
    public void onCameraViewStopped() {
        //카메라 촬영이 정지되었을때
        //60초 카운트 시작
        System.out.println("카메라 정지");

        if(backgroundImageView != null)
        {
            //카메라 촬영 휴식시
            //이미지는 휴식구간 이미지
            backgroundImageView.setImageResource(R.drawable.breakimg);
        }

        cameraviewcount = 10;

        countdownThread  mcountdownthread = new countdownThread();
        mcountdownthread.start();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        matInput = inputFrame.rgba();
        Core.flip(matInput, matInput,1);

        Log.d("FRAME", String.valueOf(frameCounter));

        //if(frameCounter % 10 == 0)
        //{
            try{
                getWriteLock();
                //matInput 과 같은 matResult , matGray생성

                if(matResult == null)
                    matResult = new Mat(matInput.rows(),matInput.cols(),matInput.type());

                //LandmarkDetection(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());

                if(StateOfSetting == SETTING_ON)
                {
                    //셋팅 상태일때
                    //1. 검출이 잘 되는 자세를 찾는다
                    //2. 얼굴 색의 기준 HSV를 찾는다

                    int value = detectEyeAndFaceRect(cascadeClassifier_face,cascadeClassifier_eye,cascadeClassifier_righteye,
                            matInput.getNativeObjAddr(),
                            matResult.getNativeObjAddr(), eyeROI.getNativeObjAddr(), faceArray, eyeArray);

                    if(value == 1)
                    {
                        //얼굴이 검출 됐을 때
                        //HSV 값을 추출
                        System.out.println(String.valueOf(value));
                        getHSVfromImg(matResult.getNativeObjAddr(), faceArray, hsv_array);
                        Log.d("HSV", String.valueOf(hsv_array[0]) + " " + String.valueOf(hsv_array[1]) + " " + String.valueOf(hsv_array[2]));
                    }

                    Log.d("FACERECT", String.valueOf(faceArray[0]) + " " + String.valueOf(faceArray[1]) + " " +
                            String.valueOf(faceArray[2]) + " " + String.valueOf(faceArray[3])
                            + " " + String.valueOf(matResult.cols())+ " " + String.valueOf(matResult.rows()));

                    //얼굴 부분 색상 검출 및 프레임 바이너리화
                    if(matBinary == null)
                        matBinary = new Mat(matResult.rows(),matResult.cols(),matResult.type());

                    makeFaceMaskImage(matResult.getNativeObjAddr(), matBinary.getNativeObjAddr(), faceArray, hsv_array);

                }
                else if(StateOfSetting == SETTING_OFF)
                {

                    Log.d("HSV", String.valueOf(hsv_array[0]) + " " + String.valueOf(hsv_array[1]) + " " + String.valueOf(hsv_array[2]));

                    //셋팅 상태가 아닐때
                    //기존의 검출 방법 시행

                    //얼굴 및 눈 검출
                    detectEyeAndFaceRect(cascadeClassifier_face,cascadeClassifier_eye,cascadeClassifier_righteye,
                            matInput.getNativeObjAddr(),
                            matResult.getNativeObjAddr(), eyeROI.getNativeObjAddr(), faceArray, eyeArray);

                    Bitmap bmp = null;
                    try {
                        //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
                        //Imgproc.cvtColor(eyeROI, eyeROI, Imgproc.COLOR_GRAY2RGBA, 4);
                        bmp = Bitmap.createBitmap(eyeROI.cols(), eyeROI.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(eyeROI, bmp);
                    }
                    catch (CvException e){Log.d("Exception",e.getMessage());}


                    if(bmp != null)
                    {
                        Bitmap resizedBmp = Bitmap.createScaledBitmap(bmp, 64, 64, true);
                        /////////////////////////bitmap을 input배열로 변환/////////////////////////
                        /////////////////////////////// 64X64  이미지가 몇X몇인지 64를 해당 값으로 바꿔줘야 함////////
                        int batchNum = 0;
                        int width = resizedBmp.getWidth();
                        int height = resizedBmp.getHeight();
                        float[][][][] input = new float[1][width][height][3];
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                int pixel = resizedBmp.getPixel(x, y);
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

                        opencloseView.setText("what?");
                        /////////////////output 값에 따라 결정/////////////////////////////////
                        if(output[0][0]>=0.5){
                            opencloseView.setText(output[0][0]+"open");
                            openOrClose = true;
                        }
                        else {
                            opencloseView.setText(output[0][0]+"close");
                            openOrClose = false;
                        }

                    }

                    /////////////////////////////큰 졸음 판별 단계////////////////////////////////////////////
                    //얼굴 부분 색상 검출 및 프레임 바이너리화
                    if(matBinary == null)
                        matBinary = new Mat(matResult.rows(),matResult.cols(),matResult.type());

                    makeFaceMaskImage(matResult.getNativeObjAddr(), matBinary.getNativeObjAddr(), faceArray, hsv_array);
                    //프레임버퍼에 프레임 저장
                    frameBuffer.Enqueue(matBinary);
                    //픽셀카운팅 및 졸음 판별
                    CountingThread countingThread = new CountingThread();
                    countingThread.start();

                    if(StateOfDetectingHighDowsiness == HIGH_DETECTREADY)
                    {
                        StateOfDetectingHighDowsiness = HIGH_DETECTING;
                        StateOfDetectingLowDowsiness = LOW_NOTACT;
                        if(ishighcountupThreadRun == false)
                        {
                            ishighcountupThreadRun = true;
                            DetectHighdrowsinessThread  detectThread = new DetectHighdrowsinessThread();
                            detectThread.start();
                        }

                        System.out.println("높은 졸음 디텍트스레드가 시작되었습니다.");
                        //카운트다운 스레드 시작 (10초)
                        //10초 카운트 스레드 종료 후 알람 이벤트 발생

                        //단 카운팅 스레드에서 다시 얼굴을 감지 했을시
                        //중간에 종료 할 수 있어야함

                        //이벤트 발생중에도 다시 얼굴을 감지 했을시
                        //중간에 종료
                    }
                    else if(StateOfDetectingHighDowsiness == HIGH_DETECTING)
                    {

                        if(high_detectingCount == 10)
                        {
                            drawnessCounter++;

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
                    if(openOrClose == false && StateOfDetectingLowDowsiness == LOW_COUNTING)
                    {
                        //눈 상태가 감은 눈일 경우
                        //상태 변경 COUNTING -> DETECTREADY
                        StateOfDetectingLowDowsiness = LOW_DETECTREADY;
                    }

                    if(StateOfDetectingLowDowsiness == LOW_DETECTREADY)
                    {
                        //상태변경 DETECTREADY -> DETECTING
                        StateOfDetectingLowDowsiness = LOW_DETECTING;

                        //눈이 감긴걸로 판별이 되면
                        //카운트를 증가 시켜주는 스레드가 시작 (10초 동안 지속)
                        if(islowcountupThreadRun == false)
                        {
                            islowcountupThreadRun = true;
                            DetectLowdrowsinessThread  deteclowThread = new DetectLowdrowsinessThread();
                            deteclowThread.start();
                        }

                        System.out.println("낮은 졸음 디텍트스레드가 시작되었습니다.");
                        //카운트다운 스레드 시작 (10초)
                        //10초 카운트 스레드 종료 후 알람 이벤트 발생
                    }
                    else if(StateOfDetectingLowDowsiness == LOW_DETECTING)
                    {
                        //낮은졸음 카운트가 올라가고 있을때
                        //다시 뜬 눈으로 감지 될 경우
                        if(openOrClose == true)
                        {
                            //상태 변경 DETECTING -> COUNTING
                            low_detectingCount = 0;
                            lowcountView.setText("0");
                            StateOfDetectingLowDowsiness = LOW_COUNTING;
                        }

                        if(low_detectingCount == 10)
                        {
                            drawnessCounter++;

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
                        //낮은 졸음 알람이 실행 중일 때
                        //다시 뜬 눈으로 검출 됐을 경우 원래 상태로 복귀

                        //리시버에게 알람을 종료하라는 메시지 송신
                        if(openOrClose == true)
                        {

                            StateOfDetectingLowDowsiness = LOW_COUNTING;
                            low_detectingCount = 0;
                            lowcountView.setText("0");
                            registerReceiver(alarmReceiver, intentFilter);
                            Intent sendIntent = new Intent(ALARMEND);
                            sendBroadcast(sendIntent);
                        }
                    }


                    Log.d("HSTATE", String.valueOf(StateOfDetectingHighDowsiness));
                    Log.d("LSTATE", String.valueOf(StateOfDetectingLowDowsiness));
                }
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            releaseWriteLock();

            if(StateOfSetting == SETTING_ON)
            {
                return matBinary;
            }
            else
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

    public void countDown(String time) {

        long conversionTime = 0;

        // 1000 단위가 1초
        // 60000 단위가 1분
        // 60000 * 3600 = 1시간

        String getHour = time.substring(0, 2);
        String getMin = time.substring(2, 4);
        String getSecond = time.substring(4, 6);

        System.out.println(time);

        // "00"이 아니고, 첫번째 자리가 0 이면 제거
        if (getHour.substring(0, 1) == "0") {
            getHour = getHour.substring(1, 2);
        }

        if (getMin.substring(0, 1) == "0") {
            getMin = getMin.substring(1, 2);
        }

        if (getSecond.substring(0, 1) == "0") {
            getSecond = getSecond.substring(1, 2);
        }

        // 변환시간
        conversionTime = Long.valueOf(getHour) * 1000 * 3600 + Long.valueOf(getMin) * 60 * 1000 + Long.valueOf(getSecond) * 1000;

        // 첫번쨰 인자 : 원하는 시간 (예를들어 30초면 30 x 1000(주기))
        // 두번쨰 인자 : 주기( 1000 = 1초)
        new CountDownTimer(conversionTime, 1000) {

            // 특정 시간마다 뷰 변경
            public void onTick(long millisUntilFinished) {

                // 시간단위
                long getHour = millisUntilFinished - millisUntilFinished / (60 * 60 * 60 * 1000);
                String hour = String.valueOf(millisUntilFinished / (60 * 60 * 1000));

                String min = String.valueOf((getHour % (60 * 60 * 1000) )/ (60 * 1000) );   //나머지
                // 분단위

                long getMin = millisUntilFinished - (millisUntilFinished / (60 * 60 * 1000)) ;
                //String min = String.valueOf(getMin / (60 * 1000)); // 몫

                // 초단위
                String second = String.valueOf((getMin % (60 * 1000)) / 1000); // 나머지

                // 밀리세컨드 단위
                String millis = String.valueOf((getMin % (60 * 1000)) % 1000); // 몫

                // 시간이 한자리면 0을 붙인다
                if (hour.length() == 1) {
                    hour = "0" + hour;
                }

                // 분이 한자리면 0을 붙인다
                if (min.length() == 1) {
                    min = "0" + min;
                }

                // 초가 한자리면 0을 붙인다
                if (second.length() == 1) {
                    second = "0" + second;
                }

                duringtimeView.setText(hour + ":" + min + ":" + second);

            }

            // 제한시간 종료시
            public void onFinish() {
                //공부 종료
                //메인액티비티로 이동

                //모든 스레드를 종료시킬수 있도록
                //allthreadisrunning 변수를 false로 셋팅
                allthreadisrunning = false;


                /*
                int subjectID = SQLiteManager.sqLiteManager.selectSubjectIdFromName(subjectSN);

                List<ScheduleData> Lists = SQLiteManager.sqLiteManager.selectScheduleDataFormSubjectID(subjectID);

                for(int lSize = 0 ; lSize < Lists.size() ; lSize++)
                {
                    if(Lists.get(lSize).getDate() == scheduleDate)
                    {
                        data = Lists.get(lSize);
                    }
                }
                */

                ScheduleData data = SQLiteManager.sqLiteManager.selectScheduleDataFormID(scheduleID);

                //이행여부를 1로 바꿔서 DB업데이트
                SQLiteManager.sqLiteManager.updateScheduleData(new ScheduleData(data.getID(), data.getSubject_ID(), data.getDate(), data.getDuringtime() , 1));

                Intent intent = new Intent(getApplicationContext(),MainActivity.class);

                if(drawnessCounter < 3)
                {
                    //졸음 횟수가 3회 미만 일때
                    //칭찬메세지
                    intent.putExtra("backfromDetection", 1);
                }
                else
                {
                    intent.putExtra("backfromDetection", 2);
                }
                startActivity(intent);
                finish();
            }
        }.start();
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

    public String convert2conversionDTfromInt(int duringtime)
    {
        String str = null;

        if(duringtime > 10)
        {
            str = String.valueOf(duringtime) + "0000";
        }
        else
        {
            str = "0" + String.valueOf(duringtime) + "0000";
        }


        return str;
    }

    public class eyedrawnessTherad extends Thread{
        @Override
        public void run() {
            howEyedrawness();
        }
    }

    private synchronized void countuplowdetectingcount(){
        low_detectingCount++;
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
            //            /////////////////////////////// 64X64  이미지가 몇X몇인지 64를 해당 값으로 바꿔줘야 함////////
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
                && sumofWhitePixels < countofPixels / 2)
        {
            StateOfDetectingHighDowsiness = HIGH_DETECTREADY;
        }
        else if(StateOfDetectingHighDowsiness == HIGH_WAKE_UP
                && sumofWhitePixels > countofPixels / 2)
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

            StateOfDetectingLowDowsiness = LOW_COUNTING;
            StateOfDetectingHighDowsiness = HIGH_COUNTING;
            high_detectingCount = 0;
            highcountView.setText("0");
        }
        else if (StateOfDetectingHighDowsiness == HIGH_DETECTING
                && sumofWhitePixels > countofPixels / 2)
        {
            StateOfDetectingHighDowsiness = HIGH_COUNTING;
            StateOfDetectingLowDowsiness = LOW_COUNTING;
            high_detectingCount = 0;
            highcountView.setText("0");
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
            while(high_detectingCount < 10 && StateOfDetectingHighDowsiness == HIGH_DETECTING && allthreadisrunning){
                Message message = mhighcountHandler.obtainMessage();
                high_detectingCount++;
                message.arg1 = high_detectingCount;
                mhighcountHandler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ishighcountupThreadRun = false;
            System.out.println("높은 졸음 디텍트스레드가 종료되었습니다.");

            //스레드가 종료되면 무조건 카운트를 0으로
            high_detectingCount = 0;
            highcountView.setText("0");

        }
    }

    public class DetectLowdrowsinessThread extends Thread{

        boolean isrun = true;

        public void run(){
            while(low_detectingCount < 10 && StateOfDetectingLowDowsiness == LOW_DETECTING && isrun && allthreadisrunning){
                Message message = mlowcountHandler.obtainMessage();
                countuplowdetectingcount();
                if(openOrClose == true)
                {
                    isrun = false;
                }
                message.arg1 = low_detectingCount;
                mlowcountHandler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            islowcountupThreadRun = false;
            System.out.println("낮은졸음 디텍트스레드가 종료되었습니다.");

            //스레드가 종료되면 무조건 카운트를 0 으로
            low_detectingCount = 0;
            lowcountView.setText("0");
        }
    }

    public class countdownThread extends Thread{

        public boolean isrun = true;

        public void run(){
            while(cameraviewcount > 0 && isrun && allthreadisrunning){
                Message message = mcountHandler.obtainMessage();
                cameraviewcount--;
                if(cameraviewcount == 0)
                    isrun = false;
                message.arg1 = cameraviewcount;
                mcountHandler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread( ).interrupt();
                    e.printStackTrace();
                }
            }
            System.out.println("카운트다운 스레드가 종료되었습니다.");
        }
    }

    public class settingcountdownThread extends Thread{

        public boolean isrun = true;

        public void run(){
            while(settingcount > 0 && isrun && allthreadisrunning){
                Message message = msettingcountHnadler.obtainMessage();
                settingcount--;
                if(settingcount == 0)
                    isrun = false;
                message.arg1 = settingcount;
                msettingcountHnadler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("셋팅카운트 스레드가 종료되었습니다.");
        }
    }

    /*public class subjectduringtimecountdown extends Thread{

        public boolean isrun = true;

        public void run(){
            while(cameraviewcount > 0 && isrun){
                Message message = mcountHandler.obtainMessage();
                cameraviewcount--;
                if(cameraviewcount == 0)
                    isrun = false;
                message.arg1 = cameraviewcount;
                mcountHandler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("카운트다운 스레드가 종료되었습니다.");
        }
    }
    */

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

                unregisterReceiver(alarmReceiver);
            }
            //unregisterReceiver(alarmReceiver);
        }
    }

}

