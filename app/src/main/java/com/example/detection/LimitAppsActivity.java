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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.detection.fragment.ContentFragment;

import java.util.ArrayList;
import java.util.List;

public class LimitAppsActivity extends AppCompatActivity {

    public static ArrayList<LimitListViewItem> listViewItemList = new ArrayList<LimitListViewItem>() ;
    public static ArrayList<LimitListViewItem> listViewItemListCustom = new ArrayList<LimitListViewItem>() ;  // 사용자가 추가한 리스트
    static long currentTime;
    static long startTime;
    static long duringTime;

    Button startButton,addAllButton,removeAllButton;
    EditText et;
    ListView listView;
    ListView listView2;
    LimitListViewAdapter adapter;
    LimitListViewAdapter adapter2;
    ImageView imageView1;
    ImageView imageView2;

    NumberPicker hourPicker;
    NumberPicker minutePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.limit_main);

        if(!checkAccessibilityPermissions()) {  //  권한 확인
            setAccessibilityPermissions();
        }

        startButton = findViewById(R.id.start_button);
        addAllButton = findViewById(R.id.add_all_button);
        removeAllButton = findViewById(R.id.remove_all_button);
        et = findViewById(R.id.edit_text);
        listView = findViewById(R.id.list_view);
        listView2 = findViewById(R.id.list_view2);
        imageView1 = findViewById(R.id.image_view1);
        imageView2 = findViewById(R.id.image_view2);
        hourPicker = findViewById(R.id.hour_picker);
        minutePicker = findViewById(R.id.minute_picker);

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(6);
        hourPicker.setValue(0);

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(60);
        minutePicker.setValue(0);


        getInstalledApplList();                 //  설치 앱 정보 받기
        setMyAdapter();                         //  정보 바탕으로 Adapter 설정

        imageView1.setImageResource(R.drawable.ic_arrow_forward_black_24dp);
        imageView2.setImageResource(R.drawable.ic_arrow_back_black_24dp);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LimitAppsActivity.startTime = System.currentTimeMillis();
                LimitAppsActivity.duringTime = (hourPicker.getValue()*60+minutePicker.getValue())*60*1000;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("backfromDetection", 0);
                startActivity(intent);
                Toast myToast = Toast.makeText(getApplicationContext(),"잠금 시작", Toast.LENGTH_SHORT);
                myToast.show();
            }
        });

//        hourPicker.setOnScrollListener();

        addAllButton.setOnClickListener(new View.OnClickListener() {        // 전체 추가
            @Override
            public void onClick(View v) {
                for(int i=0;i<listViewItemList.size();i++){
                    boolean flag = true;
                    for(int j=0;j<listViewItemListCustom.size();j++){       // 기존에 있는지 검사
                        if(listViewItemListCustom.get(j).getAppName().equals(listViewItemList.get(i).getAppName())){
                            flag = false;
                        }
                    }
                    if(flag){                                               // 없으면 추가
                        listViewItemListCustom.add(new LimitListViewItem());
                        int temp = listViewItemListCustom.size()-1;
                        listViewItemListCustom.get(temp).setAppIconDrawable(listViewItemList.get(i).getAppIconDrawable());
                        listViewItemListCustom.get(temp).setAppName(listViewItemList.get(i).getAppName());
                        listViewItemListCustom.get(temp).setAppPackageName(listViewItemList.get(i).getAppPackageName());
                        listViewItemListCustom.get(temp).setSearched(listViewItemList.get(i).getSearched());
                    }
                }
                reMake();
            }
        });

        removeAllButton.setOnClickListener(new View.OnClickListener() {     // 전체 삭제
            @Override
            public void onClick(View v) {
                listViewItemListCustom.removeAll(listViewItemListCustom);
//                for(int i=0;i<listViewItemListCustom.size();i++){
//                    listViewItemListCustom.remove(i);
//                }
                reMake();
            }
        });

        // input창에 검색어를 입력시 "addTextChangedListener" 이벤트 리스너를 정의한다.
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {   //  문자 입력마다 리스트 갱신
                String text = et.getText().toString();
                search(text);
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView textView =(TextView) view.findViewById(R.id.textView1);
                String str = (String) textView.getText();

                for(int i=0;i<listViewItemList.size();i++){
                    if(listViewItemList.get(i).getAppName().equals(str)){
                        boolean flag = true;
                        for(int j=0;j<listViewItemListCustom.size();j++){       // 기존에 있는지 검사
                            if(listViewItemListCustom.get(j).getAppName().equals(str)){
                                flag = false;
                            }
                        }
                        if(flag){                                               // 없으면 추가
                            listViewItemListCustom.add(new LimitListViewItem());
                            int temp = listViewItemListCustom.size()-1;
                            listViewItemListCustom.get(temp).setAppIconDrawable(listViewItemList.get(i).getAppIconDrawable());
                            listViewItemListCustom.get(temp).setAppName(listViewItemList.get(i).getAppName());
                            listViewItemListCustom.get(temp).setAppPackageName(listViewItemList.get(i).getAppPackageName());
                            listViewItemListCustom.get(temp).setSearched(listViewItemList.get(i).getSearched());
                        }
                    }
                }
                reMake();
            }
        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView =(TextView) view.findViewById(R.id.textView1);
                String str = (String) textView.getText();

                for(int i=0;i<listViewItemListCustom.size();i++){
                    if(listViewItemListCustom.get(i).getAppName().equals(str)){
                        listViewItemListCustom.remove(i);
                    }
                }
                reMake();
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

    private void getInstalledApplList(){
        listViewItemList.clear();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> AppInfos = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo info : AppInfos) {
            ActivityInfo ai = info.activityInfo;
            if(ai.packageName.contains("com.sec")){ }
            else if(ai.packageName.contains("com.android")){ }
            else if(ai.packageName.contains("com.example")){ }
            else if(ai.packageName.contains("com.ddangyc")){ }
            else if(ai.packageName.contains("com.google.android")){ }
            else if(ai.packageName.contains("com.samsung")){ }
            else if(ai.packageName.contains("com.microsoft")){ }
            else if(ai.packageName.contains("com.kt")){ }
            else {
                listViewItemList.add(new LimitListViewItem());
                int temp = listViewItemList.size() - 1;

                listViewItemList.get(temp).setAppIconDrawable(ai.loadIcon(packageManager));
                listViewItemList.get(temp).setAppName(ai.loadLabel(packageManager).toString());
                listViewItemList.get(temp).setAppPackageName(ai.packageName);
                listViewItemList.get(temp).setSearched(true);
            }
        }
    }

    public void search(String charText) {
        // 문자 입력이 없을때는 모든 데이터를 보여준다.
        if (charText.length() == 0) {
            for(int i=0;i<listViewItemList.size();i++){
                listViewItemList.get(i).setSearched(true);
            }
            for(int i=0;i<listViewItemListCustom.size();i++){
                listViewItemListCustom.get(i).setSearched(true);
            }
        }
        // 문자 입력을 할때
        else {
            for(int i=0;i<listViewItemList.size();i++){
                listViewItemList.get(i).setSearched(false);
            }
            for(int i=0;i<listViewItemListCustom.size();i++){
                listViewItemListCustom.get(i).setSearched(false);
            }
//            for(int i=0;i<ListViewAdapter.listViewItemList.size();i++){
//                ListViewAdapter.listViewItemList.get(i).setSearched(false);
//            }
            for(int i=0;i<listViewItemList.size();i++) {
                if (listViewItemList.get(i).getAppName().toLowerCase().contains(charText)) {
                    listViewItemList.get(i).setSearched(true);
                }
            }
            for(int i=0;i<listViewItemListCustom.size();i++) {
                if (listViewItemListCustom.get(i).getAppName().toLowerCase().contains(charText)) {
                    listViewItemListCustom.get(i).setSearched(true);
                }
            }
        }
        setMyAdapter();
    }

    public void setMyAdapter(){
        listView.setAdapter(null);
        adapter = new LimitListViewAdapter();

        for(int i=0;i<listViewItemList.size();i++) {
            if(listViewItemList.get(i).getSearched()){
                adapter.addItem(listViewItemList.get(i).getAppIconDrawable(),
                        listViewItemList.get(i).getAppName(),
                        listViewItemList.get(i).getAppPackageName(),
                        listViewItemList.get(i).getSearched());
            }
        }
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        reMake();
    }

    public void reMake(){
        listView2.setAdapter(null);
        adapter2 = new LimitListViewAdapter();
        for(int i=0;i<listViewItemListCustom.size();i++) {
            if(listViewItemListCustom.get(i).getSearched()) {
                adapter2.addItem(listViewItemListCustom.get(i).getAppIconDrawable(),
                        listViewItemListCustom.get(i).getAppName(),
                        listViewItemListCustom.get(i).getAppPackageName(),
                        listViewItemListCustom.get(i).getSearched());
            }
        }
        listView2.setAdapter(adapter2);
    }
}