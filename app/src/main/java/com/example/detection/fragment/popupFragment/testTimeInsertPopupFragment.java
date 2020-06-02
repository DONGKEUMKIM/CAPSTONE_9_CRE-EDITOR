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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.detection.R;
import com.example.detection.db.TestTimeData;

import java.util.ArrayList;


public class testTimeInsertPopupFragment extends AppCompatActivity {
    //
    private DatePicker datePicker;
    private NumberPicker numberPicker;
    private Button okButton, cancelButton;
    private ArrayList<String> subjectNameList;
    private Intent intent;
    TestTimeData testTimeData;
    ArrayAdapter arrayAdapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        intent = getIntent();
        testTimeData = (TestTimeData) intent.getSerializableExtra("ttd");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.testtime_insert_popup);
        datePicker = (DatePicker) findViewById(R.id.testtime_datePicker);
        numberPicker = (NumberPicker) findViewById(R.id.testtime_duringTimePicker);
        okButton = (Button) findViewById(R.id.testtime_okButton);
        cancelButton = (Button) findViewById(R.id.testtime_cancel_button);
        //TODO
        numberPicker.setMaxValue(12);
        numberPicker.setMinValue(1);
        numberPicker.setValue(1);
        //numberPicker.setWrapSelectorWheel(false);
        testTimeData.setdate(datePicker.getYear() + "/" + datePicker.getMonth()+1 + "/" + datePicker.getDayOfMonth());
        testTimeData.setDuringtime(0);
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
                testTimeData.setdate(i + "/" + (i1) + "/" + i2);
            }
        });
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                testTimeData.setDuringtime(i1);
            }
        });


    }

    private void setSubjectNameList() {

    }

    private void okButtonClicked() {

        intent.putExtra("testTime", testTimeData);
        setResult(0, intent);
        finish();
    }

    ;

    private void cancelButtonClicked() {
        setResult(1);
        finish();
    }

    ;
}
