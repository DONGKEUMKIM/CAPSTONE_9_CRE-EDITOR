package com.example.detection.fragment.popupFragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.detection.R;
import com.example.detection.db.SQLiteManager;
import com.example.detection.db.ScheduleData;

import java.util.ArrayList;

public class ScheduleDataPopupFragment extends AppCompatActivity {
    private Spinner spinner_name;
    private DatePicker datePicker;
    private NumberPicker numberPicker;
    private ScheduleData scheduleData;
    private Button okButton, cancelButton;
    private Intent intent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.schedule_insert_popup);
        spinner_name = (Spinner) findViewById(R.id.spinner_name);
        intent = getIntent();
        SQLiteManager dbManager = SQLiteManager.sqLiteManager;
        //ArrayList<String> subjectNameList = intent.getStringArrayListExtra("idArray");
        ArrayList<String> subjectNameList = dbManager.selectAllSubjectName();
        String scheduleID = intent.getStringExtra("scheduleid");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, subjectNameList);
        spinner_name.setAdapter(arrayAdapter);
        datePicker = (DatePicker) findViewById(R.id.schedule_datePicker);
        numberPicker = (NumberPicker) findViewById(R.id.schedule_timePicker);
        okButton = (Button) findViewById(R.id.schedule_okButton);
        cancelButton = (Button) findViewById(R.id.schedule_cancel_button);
        numberPicker.setMaxValue(12);
        numberPicker.setMinValue(1);
        numberPicker.setWrapSelectorWheel(true);
        //numberPicker.setValue(1);

        String initDate = Integer.toString(datePicker.getYear())+"/"+Integer.toString(datePicker.getMonth()+1)+"/"+Integer.toString(datePicker.getDayOfMonth());
        scheduleData = new ScheduleData(scheduleID, 0, initDate, 1);
        spinner_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                scheduleData.setSubject_ID(dbManager.selectSubjectIdFromName(subjectNameList.get(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
                scheduleData.setdate(i + "/" + (i1+1) + "/" + i2);
            }
        });
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                scheduleData.setDuringtime(i1);
            }
        });
    }

    private void okButtonClicked() {
        intent.putExtra("schedule", scheduleData);
        setResult(2, intent);
        finish();
    }

    ;

    private void cancelButtonClicked() {
        setResult(1);
        finish();
    }

    ;

}
