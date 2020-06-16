#include <jni.h>
#include <string>
#include <iostream>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>


using namespace std;

using namespace cv;


float resize_img(Mat img_src, Mat &img_resize, int resize_width);

float resize_img(Mat img_src, Mat &img_resize, int resize_width){
    float scale = resize_width / (float)img_src.cols ;
    if (img_src.cols > resize_width) {
        int new_height = cvRound(img_src.rows * scale);
        resize(img_src, img_resize, Size(resize_width, new_height));
    }
    else {
        img_resize = img_src;
    }
    return scale;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_detection_TestDetection_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                          jlong mat_addr_input,
                                                          jlong mat_addr_result) {
    // TODO: implement ConvertRGBtoGray()

    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_detection_TestDetection_InvertMat(JNIEnv *env, jobject thiz, jlong mat_addr_input,
                                                   jlong mat_addr_result) {

    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    flip(matInput, matResult,1);
}extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_detection_TestDetection_loadCascade(JNIEnv *env, jobject thiz,
                                                     jstring cascade_file_name) {
    const char *nativeFileNameString = env->GetStringUTFChars(cascade_file_name, 0);
    string baseDir("/storage/emulated/0/");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();
    jlong ret = 0;
    ret = (jlong) new CascadeClassifier(pathDir);
    if (((CascadeClassifier *) ret)->empty()) {
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
    }
    else
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);
    env->ReleaseStringUTFChars(cascade_file_name, nativeFileNameString);
    return ret;

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_detection_TestDetection_detect(JNIEnv *env, jobject thiz,
                                                jlong cascade_classifier_face,
                                                jlong cascade_classifier_eye, jlong mat_addr_input,
                                                jlong mat_addr_result) {
    Mat &img_input = *(Mat *) mat_addr_input;
    Mat &img_result = *(Mat *) mat_addr_result;
    img_result = img_input.clone();
    std::vector<Rect> faces;

    Mat img_gray;

    cvtColor(img_input, img_gray, COLOR_BGR2GRAY);
    equalizeHist(img_gray, img_gray);

    Mat img_resize;

    float resizeRatio;
    resizeRatio = resize_img(img_gray, img_resize, 320);
    //-- Detect faces

    ((CascadeClassifier *) cascade_classifier_face)->detectMultiScale( img_resize, faces, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );

    __android_log_print(ANDROID_LOG_DEBUG, (char *) "native-lib :: ",
                        (char *) "face %d found ", faces.size());

    for (int i = 0; i < faces.size(); i++) {
        double real_facesize_x = faces[i].x / resizeRatio;
        double real_facesize_y = faces[i].y / resizeRatio;
        double real_facesize_width = faces[i].width / resizeRatio;
        double real_facesize_height = faces[i].height / resizeRatio;

        Point center( real_facesize_x + real_facesize_width / 2, real_facesize_y + real_facesize_height/2);

        ellipse(img_result, center, Size( real_facesize_width / 2, real_facesize_height / 2), 0, 0, 360,
                Scalar(255, 0, 255), 30, 8, 0);

        Rect face_area(real_facesize_x, real_facesize_y, real_facesize_width,real_facesize_height);
        Mat faceROI = img_gray( face_area );
        std::vector<Rect> eyes;

        //-- In each face, detect eyes
        ((CascadeClassifier *) cascade_classifier_eye)->detectMultiScale( faceROI, eyes, 1.1, 2, 0 |CASCADE_SCALE_IMAGE, Size(30, 30) );

        //Rect eye_area(eyes[0].x,eyes[0].y, eyes[0].width, eyes[0].width);
        for ( size_t j = 0; j < eyes.size(); j++ )
        {
            Rect eye_area(real_facesize_x + eyes[j].x,real_facesize_y +eyes[j].y, eyes[j].width, eyes[j].width);
            //Mat eyeROI = img_result(eye_area);
            //Point leftup(real_facesize_x+eyes[j].x , real_facesize_y + eyes[j].y);
            //Point rightdown(real_facesize_x+eyes[j].x + eyes[j].width , real_facesize_x+eyes[j].y + eyes[j].height);

            cv::rectangle(img_result, eye_area, Scalar(255,0,0), 15, 8, 0);

            //Point eye_center( real_facesize_x + eyes[j].x + eyes[j].width/2, real_facesize_y + eyes[j].y + eyes[j].height/2 );
            //int radius = cvRound( (eyes[j].width + eyes[j].height)*0.25 );
            //circle( img_result, eye_center, radius, Scalar( 255, 0, 0 ), 15, 8, 0 );
            //rectangle(img_result, leftup, rightdown, Scalar(255,0,0), 15, 8,0);
        }
    }
}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_detection_TestDetection_detectEyeAndFaceRect(JNIEnv *env, jobject thiz,
                                                              jlong cascade_classifier_face,
                                                              jlong cascade_classifier_eye,
                                                              jlong cascade_classifier_righteye,
                                                              jlong mat_addr_input,
                                                              jlong mat_addr_result, jlong eye_roi,
                                                              jintArray faceArray) {

    int returnValue = 0;                    // 얼굴이 검출되면 1 , 검출되지 않으면 0

    //int ySizeforCompare = 0;
    Mat &img_input = *(Mat *) mat_addr_input;
    Mat &img_result = *(Mat *) mat_addr_result;

    Mat &eye_ROI = *(Mat *)eye_roi;

    jintArray face_arr = (jintArray )env->NewGlobalRef((jobject) faceArray);

    img_result = img_input.clone();
    std::vector<Rect> faces;

    Mat img_gray;

    cvtColor(img_input, img_gray, COLOR_BGR2GRAY);
    equalizeHist(img_gray, img_gray);

    Mat img_resize;

    float resizeRatio;
    resizeRatio = resize_img(img_gray, img_resize, 320);
    //-- Detect faces

    ((CascadeClassifier *) cascade_classifier_face)->detectMultiScale( img_resize, faces, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30) );

    __android_log_print(ANDROID_LOG_DEBUG, (char *) "native-lib :: ",
                        (char *) "face %d found ", faces.size());


    if(faces.size() != 0)
    {
        returnValue = 1;
    }
    for (int i = 0; i < faces.size(); i++) {
        int real_facesize_x = faces[i].x / resizeRatio;
        int real_facesize_y = faces[i].y / resizeRatio;
        int real_facesize_width = faces[i].width / resizeRatio;
        int real_facesize_height = faces[i].height / resizeRatio;

        Point center( real_facesize_x + real_facesize_width / 2, real_facesize_y + real_facesize_height/2);

        //ellipse(img_result, center, Size( real_facesize_width / 2, real_facesize_height / 2), 0, 0, 360,
        //        Scalar(255, 0, 255), 30, 8, 0);

        Rect face_area(real_facesize_x, real_facesize_y, real_facesize_width,real_facesize_height);
        int ySizeforCompare = real_facesize_y + real_facesize_height/3;
        Mat faceROI = img_gray( face_area );

        int arr[4]={0,0,0,0};

        arr[0] = real_facesize_x;
        arr[1] = real_facesize_y;
        arr[2] = real_facesize_width;
        arr[3] = real_facesize_width;

        //jintArray ret = env->NewIntArray(4);
        env->SetIntArrayRegion(face_arr,0,4,arr);

        //cv::rectangle(img_result, face_area, Scalar(255,0,0), 15, 8, 0);

        std::vector<Rect> eyes;
        std::vector<Rect> righteyes;
        //-- In each face, detect eyes
        ((CascadeClassifier *) cascade_classifier_eye)->detectMultiScale( faceROI, eyes, 1.1, 2, 0 |CASCADE_SCALE_IMAGE, Size(30, 30) );
        Rect eye_area;
        //-- In each face, detect righteyes
        ((CascadeClassifier *) cascade_classifier_righteye)->detectMultiScale( faceROI, righteyes, 1.1, 2, 0 |CASCADE_SCALE_IMAGE, Size(30, 30) );
        Rect righteye_area;

        /*
        if(!eyes.empty() && !righteyes.empty())
        {
            Rect eye_area(real_facesize_x+ eyes[0].x,real_facesize_y + eyes[0].y, eyes[0].width, eyes[0].width);
            Rect righteye_area(real_facesize_x+ righteyes[0].x,real_facesize_y + righteyes[0].y, righteyes[0].width, righteyes[0].width);

            if(abs(eye_area.x - righteye_area.x) < 30)
            {
                cv::rectangle(img_result, righteye_area, Scalar(255,0,0), 15, 8, 0);
            }
        }
         */
        if(!righteyes.empty())
        {
            Rect righteye_area(real_facesize_x+ righteyes[0].x,real_facesize_y + righteyes[0].y, righteyes[0].width, righteyes[0].width);

            if((real_facesize_y + righteyes[0].y) < ySizeforCompare)
            {
                cv::rectangle(img_result, righteye_area, Scalar(255,0,0), 5, 8, 0);
            }
            eye_ROI = img_gray(righteye_area);

        }

        /*
        for ( size_t j = 0; j < eyes.size(); j++ )
        {
            Rect eye_area(real_facesize_x+ eyes[j].x,real_facesize_y + eyes[j].y, eyes[j].width, eyes[j].width);
            //Point leftup(real_facesize_x+eyes[j].x , real_facesize_y + eyes[j].y);
            //Point rightdown(real_facesize_x+eyes[j].x + eyes[j].width , real_facesize_x+eyes[j].y + eyes[j].height);

            //cv::rectangle(img_result, eye_area, Scalar(255,0,0), 15, 8, 0);

            //eye_ROI = img_gray ( eye_area ) ;
            //Point eye_center( real_facesize_x + eyes[j].x + eyes[j].width/2, real_facesize_y + eyes[j].y + eyes[j].height/2 );
            //int radius = cvRound( (eyes[j].width + eyes[j].height)*0.25 );
            //circle( img_result, eye_center, radius, Scalar( 255, 0, 0 ), 15, 8, 0 );
            //rectangle(img_result, leftup, rightdown, Scalar(255,0,0), 15, 8,0);
        }

        for ( size_t k = 0; k < righteyes.size(); k++ )
        {
            Rect righteye_area(real_facesize_x+ righteyes[k].x,real_facesize_y + righteyes[k].y, righteyes[k].width, righteyes[k].width);
            //Point leftup(real_facesize_x+eyes[j].x , real_facesize_y + eyes[j].y);
            //Point rightdown(real_facesize_x+eyes[j].x + eyes[j].width , real_facesize_x+eyes[j].y + eyes[j].height);

            //cv::rectangle(img_result, eye_area, Scalar(255,0,0), 15, 8, 0);

            //eye_ROI = img_gray ( eye_area ) ;
            //Point eye_center( real_facesize_x + eyes[j].x + eyes[j].width/2, real_facesize_y + eyes[j].y + eyes[j].height/2 );
            //int radius = cvRound( (eyes[j].width + eyes[j].height)*0.25 );
            //circle( img_result, eye_center, radius, Scalar( 255, 0, 0 ), 15, 8, 0 );
            //rectangle(img_result, leftup, rightdown, Scalar(255,0,0), 15, 8,0);
        }
         */
    }

    jint *face_Array = env->GetIntArrayElements(face_arr, NULL);

    Rect face_area(face_Array[0], face_Array[1], face_Array[2],face_Array[3]);
    cv::rectangle(img_result, face_area, Scalar(255,0,0), 5, 8, 0);

    return returnValue;
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_detection_TestDetection_makeFaceMaskImage(JNIEnv *env, jobject thiz,
                                                           jlong mat_addr_input,
                                                           jlong mat_addr_result,
                                                           jintArray face_Array,
                                                           jintArray hsv_array) {
    Mat &img_input = *(Mat *) mat_addr_input;
    Mat &img_result = *(Mat *) mat_addr_result;

    jint *faceArray = env->GetIntArrayElements(face_Array, NULL);

    jint *hsvArray = env->GetIntArrayElements(hsv_array, NULL);

    Rect faceRect(faceArray[0],faceArray[1],faceArray[2],faceArray[3]);


    cvtColor(img_input, img_result, COLOR_BGR2HSV);
    //cvtColor(img_input, matBinary2, COLOR_BGR2HSV);

    if(hsvArray[0] - 15 > 0) {
        cv::Scalar low(hsvArray[0] - 10, 30, 30);
        if(hsvArray[0] + 15 > 179)
        {
            cv::Scalar high(179 , 255  , 255);
            inRange(img_result,low, high, img_result);
        } else
        {
            cv::Scalar high(hsvArray[0] + 10 , 255  , 255);
            inRange(img_result,low, high, img_result);
        }
    }
    else if(hsvArray[0] - 15 <= 0)
    {
        cv::Scalar low(0, 30, 30);
        if(hsvArray[0] + 15 > 179)
        {
            cv::Scalar high(179 , 255  , 255);
            inRange(img_result,low, high, img_result);
        } else
        {
            cv::Scalar high(hsvArray[0] + 15 , 255  , 255);
            inRange(img_result,low, high, img_result);
        }
    }
    /*
    cv::Scalar low1(150 , 30, 30);
    cv::Scalar high1(180, 255, 255);

    inRange(matBinary1,low, high, matBinary1);
    inRange(matBinary2,low1, high1, matBinary2);

    matBinary1 |= matBinary2;

    matBinary1.copyTo(img_result);

    */
    Mat kernal = getStructuringElement(MORPH_ELLIPSE, Size(5,5));
    morphologyEx(img_result , img_result, MORPH_CLOSE, kernal);

    //얼굴 바운더리를 표시하고 싶을 경우
    //해당 주석을 지울것
    //cv::rectangle(img_result, faceRect, Scalar(255,0,0), 15, 8, 0 );

}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_detection_TestDetection_CountWhitePixelsInFaceBoundary(JNIEnv *env, jobject thiz,
                                                                        jlong mat_addr_input,
                                                                        jintArray face_array) {
    Mat &matInput = *(Mat *)mat_addr_input;
    jint *faceArray = env->GetIntArrayElements(face_array, NULL);

    int count = 0;
    uchar r,g,b;

    int xStart = faceArray[0];
    int yStart = faceArray[1];
    int xSize = xStart + faceArray[2];
    int ySize = yStart + faceArray[3];

    int y;
    int x;

    for (y = yStart ; y < ySize ; ++y);
    {
        Vec3b* pixel = matInput.ptr<cv::Vec3b>(y);
        for(x = xStart; x < xSize; ++x){
            //픽셀에서 값 가져오기
            r = pixel[x][2];
            g = pixel[x][1];
            b = pixel[x][0];

            if(r == 255 && g == 255 && b == 255)
                count++;
        }
    }
    return count;
}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_detection_TestDetection_CountWhitePixelsInOneRow(JNIEnv *env, jobject thiz,
                                                                  jlong mat_addr_input,
                                                                  jint index_of_start,
                                                                  jint index_of_end,
                                                                  jintArray face_array) {
    Mat &matInput = *(Mat *)mat_addr_input;
    jint *faceArray = env->GetIntArrayElements(face_array, NULL);

    int count = 0;
    uchar r,g,b;

    int xStart = faceArray[0];
    int xSize = xStart + faceArray[2];

    for(int y = index_of_start; y < index_of_end; ++y){
        //y번째 줄에서 첫 번째 픽셀에 대한 포인터
        Vec3b* pixel = matInput.ptr<Vec3b>(y);
        for(int x = xStart; x < xSize; ++x){
            //픽셀에서 값 가져오기
            r = pixel[x][2];
            g = pixel[x][1];
            b = pixel[x][0];

            if(r == 255 && g == 255 && b == 255)
                count++;
        }
    }
    return count;
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_detection_TestDetection_LandmarkDetection(JNIEnv *env, jobject thiz,
                                                           jlong mat_addr_input,
                                                           jlong mat_addr_result) {

    Mat &Image = *(Mat *)mat_addr_input;
    Mat &dst = *(Mat *)mat_addr_result;

    //facedetectionDlib(Image, dst);

    // TODO: implement LandmarkDetection()
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_detection_TestDetection_LoadModel(JNIEnv *env, jobject thiz) {

    //detector = get_frontal_face_detector();

    //dat 파일 로드
    //deserialize("/storage/emulated/0/shape_predictor_68_face_landmarks.dat") >> pose_model;
}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_detection_TestDetection_getHfromInputImg(JNIEnv *env, jobject thiz,
                                                          jlong mat_addr_input,
                                                          jintArray face_array) {

    Mat &matInput = *(Mat *)mat_addr_input;
    Mat hsvImg;
    jint *faceArray = env->GetIntArrayElements(face_array, NULL);

    cvtColor(matInput, hsvImg, COLOR_BGR2HSV);

    int xPoint = faceArray[0] + faceArray[2]/2;
    int yPoint = faceArray[1] + faceArray[3]/2;

    int hue = (int)hsvImg.at<Vec3b>(yPoint, xPoint)[0];

    return hue;
    // TODO: implement getHfromInputImg()
}extern "C"
JNIEXPORT jint JNICALL
Java_com_example_detection_TestDetection_getHSVfromImg(JNIEnv *env, jobject thiz,
                                                       jlong mat_addr_input, jintArray face_array, jintArray array) {


    Mat &matInput = *(Mat *)mat_addr_input;
    Mat hsvImg;

    jint *faceArray = env->GetIntArrayElements(face_array, NULL);

    jintArray huearray = (jintArray )env->NewGlobalRef((jobject) array);

    cvtColor(matInput, hsvImg, COLOR_BGR2HSV);

    int xPoint = faceArray[0] + faceArray[2]/1.5;
    int yPoint = faceArray[1] + faceArray[3]/2;

    Point eye_center( xPoint, yPoint );
    int radius = cvRound( 5 );
    circle( matInput, eye_center, radius, Scalar( 255, 0, 0 ), 5, 8, 0 );

    int arr[3]={0,0,0};

    if((int)hsvImg.at<Vec3b>(yPoint, xPoint)[0] != 0)
    {
        arr[0] = (int)hsvImg.at<Vec3b>(yPoint, xPoint)[0];
        arr[1] = (int)hsvImg.at<Vec3b>(yPoint, xPoint)[1];
        arr[2] = (int)hsvImg.at<Vec3b>(yPoint, xPoint)[2];
        env->SetIntArrayRegion(huearray,0,3,arr);
    }

    return xPoint;
}