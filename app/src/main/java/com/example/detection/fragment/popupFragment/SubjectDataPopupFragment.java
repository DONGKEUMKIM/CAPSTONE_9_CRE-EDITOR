package com.example.detection.fragment.popupFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.detection.R;
import com.example.detection.db.SubjectData;
import com.example.detection.db.TestTimeData;
import com.rey.material.widget.Slider;

import java.util.ArrayList;

public class SubjectDataPopupFragment extends AppCompatActivity {

    Button okButton, cancelButton, testTimeSetButton;
    //DatePicker datePicker;
    Slider seekBar;
    View thumbView;
    ArrayList<String> idArray;
    EditText subjectName;
    private SubjectData subjectData;
    private TestTimeData testTimeData;
    private TextView priortyText;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.data_insert_popup);
        View thumbView = LayoutInflater.from(SubjectDataPopupFragment.this).inflate(R.layout.seek_bar_thumb, null, false);
        okButton = (Button) findViewById(R.id.input_data_okButton);
        cancelButton = (Button) findViewById(R.id.input_data_cancel_button);
        testTimeSetButton = (Button) findViewById(R.id.testTimeSet);
        seekBar = (Slider) findViewById(R.id.input_data_priority);
        seekBar.setPosition(0,false);
        subjectData = new SubjectData(0, "", 0);
        testTimeData = new TestTimeData("", 0, "", 0);
        priortyText = (TextView) findViewById(R.id.input_data_priority_text);
        subjectName = (EditText) findViewById(R.id.insert_data_name);
        intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String testTimeID = intent.getStringExtra("testtimeid");
        subjectData.setID(id);
        testTimeData.setId(testTimeID);
        testTimeData.setSubject_ID(id);


        subjectName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //subjectData.setName(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                subjectData.setName(editable.toString());
            }
        });

        seekBar.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
                if(newValue == 0){
                    priortyText.setText("Not Important");
                }
                if(newValue == 1){
                    priortyText.setText("Normal");
                }
                if(newValue == 2){
                    priortyText.setText("Important");
                }
                //priortyText.setText(Integer.toString(newValue));
                subjectData.setPriority(newValue);
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
        testTimeSetButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                testTimeSetButtonClicked(view);
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        return;
    }

    public Drawable getThumb(int progress) {
        ((TextView) thumbView.findViewById(R.id.tvProgress)).setText(progress + "");

        thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        thumbView.layout(0, 0, thumbView.getMeasuredWidth(), thumbView.getMeasuredHeight());
        thumbView.draw(canvas);

        return new BitmapDrawable(getResources(), bitmap);
    }

    private void okButtonClicked() {
        intent.putExtra("subject", subjectData);
        intent.putExtra("testtime", testTimeData);
        setResult(2, intent);
        finish();
    }

    ;

    private void cancelButtonClicked() {
        setResult(1);
        finish();
    }

    ;

    private void testTimeSetButtonClicked(View view) {
        Intent ttdIntent = new Intent(this, testTimeInsertPopupFragment.class);
        ttdIntent.putExtra("ttd", testTimeData);
        startActivityForResult(ttdIntent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 3)
            return;
        else {
            if (resultCode == 1)
                return;
            testTimeData = (TestTimeData) data.getExtras().getSerializable("testTime");
        }
    }
}
