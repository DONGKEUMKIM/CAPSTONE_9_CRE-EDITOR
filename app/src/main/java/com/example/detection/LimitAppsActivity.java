package com.example.detection;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.detection.fragment.ContentFragment;

import java.util.ArrayList;
import java.util.List;

public class LimitAppsActivity extends AppCompatActivity {
    //    static final String[] LIST_MENU = {"LIST1", "LIST2", "LIST3"} ;
    static final ArrayList<String> App_list = new ArrayList<String>();
    static final ArrayList<String> packageName_list = new ArrayList<String>();
    static final ArrayList<Drawable> icon_list = new ArrayList<Drawable>();
    static final ArrayList<String> clicked_packageName_list = new ArrayList<String>();
    Button button;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.limit_main);

        if(!checkAccessibilityPermissions()) {  //  권한 확인
            setAccessibilityPermissions();
        }

        button=findViewById(R.id.button);

        getInstallApplList();

        final LimitListViewAdapter adapter = new LimitListViewAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);

        // 리스트 작성
        for(int i=0;i<App_list.size();i++) {
            adapter.addItem(icon_list.get(i), App_list.get(i), packageName_list.get(i));
        }

        button.setOnClickListener(new View.OnClickListener() {  //버튼 눌리면 메인 화면으로 넘어감
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(),MainActivity.class);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast myToast = Toast.makeText(getApplicationContext(),"버튼눌림"+position, Toast.LENGTH_SHORT);
                myToast.show();
                if(!clicked_packageName_list.contains(packageName_list.get(position))) {    //추가
                    clicked_packageName_list.add(packageName_list.get(position));
                    view.setBackgroundColor(Color.BLUE);
                }
                else{
                    clicked_packageName_list.remove(packageName_list.get(position));        //삭제
                    view.setBackgroundColor(Color.WHITE);
                }
            }
        });
    }


    public boolean checkAccessibilityPermissions() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        // getEnabledAccessibilityServiceList는 현재 접근성 권한을 가진 리스트를 가져오게 된다
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT);

        for (int i = 0; i < list.size(); i++) {
            AccessibilityServiceInfo info = list.get(i);

            // 접근성 권한을 가진 앱의 패키지 네임과 패키지 네임이 같으면 현재앱이 접근성 권한을 가지고 있다고 판단함
            if (info.getResolveInfo().serviceInfo.packageName.equals(getApplication().getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public void setAccessibilityPermissions() {
        AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
        gsDialog.setTitle("접근성 권한 설정");
        gsDialog.setMessage("접근성 권한을 필요로 합니다");
        gsDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 설정화면으로 보내는 부분
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return;
            }
        }).create().show();

    }

    private void getInstallApplList(){
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> AppInfos = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo info : AppInfos) {
            ActivityInfo ai = info.activityInfo;
            App_list.add(ai.loadLabel(packageManager).toString());
            packageName_list.add(ai.packageName);
            icon_list.add(ai.loadIcon(packageManager));
            Log.d("app name", "??"+ai.loadLabel(packageManager).toString());
            Log.d("app package Name","??"+ai.packageName);
        }
    }
}
