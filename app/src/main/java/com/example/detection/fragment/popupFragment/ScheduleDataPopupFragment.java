package com.example.detection.fragment.popupFragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.detection.R;
import com.example.detection.db.ScheduleData;

import java.util.ArrayList;

public class ScheduleDataPopupFragment extends AppCompatActivity {
    private Spinner spinner_name;
    private DatePicker datePicker;
    private NumberPicker numberPicker;
    private ScheduleData scheduleData;
    private Button okButton,cancelButton;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.schedule_insert_popup);
        spinner_name = (Spinner)findViewById(R.id.spinner_name);
        Intent intent = getIntent();
        ArrayList<String> subjectNameList = intent.getStringArrayListExtra("idArray");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, subjectNameList);
        spinner_name.setAdapter(arrayAdapter);
        datePicker = (DatePicker)findViewById(R.id.schedule_datePicker);
        numberPicker = (NumberPicker)findViewById(R.id.schedule_timePicker);
        okButton = (Button)findViewById(R.id.schedule_okButton);
        cancelButton = (Button)findViewById(R.id.schedule_cancel_button);
        numberPicker.setMaxValue(12);
        numberPicker.setMinValue(1);
        numberPicker.setWrapSelectorWheel(true);


        //numberPicker.setValue(1);


        scheduleData = new ScheduleData("",0,"",0);
        okButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                okButtonClicked();
            }
        });
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelButtonClicked();
            }
        });
        datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                scheduleData.setdate(i+"/"+i1+"/"+i2);
            }
        });
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
               scheduleData.setDuringtime(i1);
            }
        });
    }
    private void okButtonClicked(){
        Intent data = new Intent();
        data.putExtra("schedule",scheduleData);
        setResult(0);
        finish();
    };
    private void cancelButtonClicked(){
        setResult(1);
        finish();
    };

}
