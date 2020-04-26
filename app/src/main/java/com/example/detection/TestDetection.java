package com.example.detection;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class TestDetection extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{


    //디버그용 TAG값
    private static final String PIXEL = "전체 픽셀";
    private static final String WPIXEL = "하얀색 픽셀";

    int frameCount = 0;
    public static final int SKIP_FRAME = 3;
    private static final String TAG = "opencv";
    private Mat matInput;
    private Mat matResult;
    private Mat eyeROI;                             //눈이미지
    private int[] faceArray;                        //얼굴 바운더리를 int배열형태로 저장
    private MatCirCularQueue frameBuffer;                   //프레임을 담을 버퍼큐

    //높은 졸음 판별 상태
    //private final static int REST = 0;
    private final static int COUNTING = 1;
    private final static int DETECTING = 2;
    private final static int WAKE_UP = 4;
    int StateOfDetectingHighDowsiness= COUNTING;

    private CameraBridgeViewBase mOpenCvCameraView;

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
        System.loadLibrary("dlib");
        System.loadLibrary("native-lib");
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
        copyFile("haarcascade_eye_tree_eyeglasses.xml.xml");
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

        Button button = (Button)findViewById(R.id.button);

        /*button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                    getWriteLock();

                    if(eyeROI != null)
                    {
                        File path = new File(Environment.getExternalStorageDirectory() + "/Images/");
                        path.mkdirs();
                        File file = new File(path, "image.jpg");

                        String filename = file.toString();

                        Imgproc.cvtColor(eyeROI, eyeROI, Imgproc.COLOR_BGR2RGBA);
                        boolean ret  = Imgcodecs.imwrite( filename, eyeROI);
                        if ( ret ) Log.d(TAG, "SUCESS");
                        else Log.d(TAG, "FAIL");

                        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        mediaScanIntent.setData(Uri.fromFile(file));
                        sendBroadcast(mediaScanIntent);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                releaseWriteLock();
            }
        });*/
        //LoadModel();
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

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        matInput = inputFrame.rgba();
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


            /*
            /////////////////////////////큰 졸음 판별 단계////////////////////////////////////////////
            //얼굴 부분 색상 검출 및 프레임 바이너리화
            makeFaceMaskImage(matResult.getNativeObjAddr(), matResult.getNativeObjAddr(), faceArray);
            //프레임버퍼에 프레임 저장
            frameBuffer.Enqueue(matResult);
            //픽셀카운팅 및 졸음 판별
            CountingThread countingThread = new CountingThread();
            countingThread.start();

            if(StateOfDetectingHighDowsiness == DETECTING)
            {
                //카운트다운 스레드 시작 (10초)
                //10초 카운트 스레드 종료 후 알람 이벤트 발생

                //단 카운팅 스레드에서 다시 얼굴을 감지 했을시
                //중간에 종료 할 수 있어야함

                //이벤트 발생중에도 다시 얼굴을 감지 했을시
                //중간에 종료
            }
            */
            //만약 여기서 감은눈 판별이 일어난다고 했을때
            //판별 후 메모리 해제 필요

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

        //검출된 얼굴의 픽셀값이 전체 픽셀의 1/3 이하일때
        //높은 졸음 판별 시작
        if(StateOfDetectingHighDowsiness == COUNTING
                && sumofWhitePixels < countofPixels / 3)
        {
            StateOfDetectingHighDowsiness = DETECTING;
        }
        else if(StateOfDetectingHighDowsiness == DETECTING
                && sumofWhitePixels > countofPixels / 3)
        {
            StateOfDetectingHighDowsiness = COUNTING;
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
            countPixelThread[i] = new CountPixelThread(mSource.getNativeObjAddr(),faceArray[1] + (i*faceArray[3])/numberOfThread,faceArray[1] +((i+1)*faceArray[3])/numberOfThread);
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
        int count = 0;
        public void run(){
            while(count < 10){
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
