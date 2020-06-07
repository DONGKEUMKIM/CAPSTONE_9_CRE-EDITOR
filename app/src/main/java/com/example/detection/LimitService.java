package com.example.detection;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class LimitService extends AccessibilityService {
    private static final String TAG = "AccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {        // 켜진 앱이 제한앱이면 gotoHome
        for(int i=0;i<LimitAppsActivity.listViewItemListCustom.size();i++){
            if(event.getPackageName().equals(LimitAppsActivity.listViewItemListCustom.get(i).getAppPackageName())) {
                LimitAppsActivity.currentTime = System.currentTimeMillis();
                if(LimitAppsActivity.currentTime - LimitAppsActivity.startTime <= LimitAppsActivity.duringTime){
                    gotoHome();
                    Toast myToast = Toast.makeText(getApplicationContext(),"사용 제한 입니다.", Toast.LENGTH_SHORT);
                    myToast.show();
                }
            }
        }
    }

    public void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK; // 전체 이벤트 가져오기
        info.feedbackType = AccessibilityServiceInfo.DEFAULT | AccessibilityServiceInfo.FEEDBACK_HAPTIC;
        info.notificationTimeout = 100; // millisecond

        setServiceInfo(info);
    }

    @Override
    public void onInterrupt() {

    }

    private void gotoHome(){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_FORWARD_RESULT
                | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(intent);
    }
}