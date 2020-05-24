package com.example.detection.fragment.popupFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.detection.R;
import com.example.detection.db.SubjectData;
import com.example.detection.db.TestTimeData;

import java.util.ArrayList;

public class SubjectDataPopupFragment extends AppCompatActivity {

    Button okButton, cancelButton,testTimeSetButton;
    //DatePicker datePicker;
    SeekBar seekBar;
    View thumbView;
    ArrayList idArray;
    private SubjectData subjectData;
    private TestTimeData testTimeData;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.data_insert_popup);
        View thumbView = LayoutInflater.from(SubjectDataPopupFragment.this).inflate(R.layout.seek_bar_thumb, null, false);
        okButton = (Button)findViewById(R.id.input_data_okButton);
        cancelButton = (Button)findViewById(R.id.input_data_cancel_button);
        testTimeSetButton = (Button)findViewById(R.id.testTimeSet);
        seekBar = (SeekBar)findViewById(R.id.input_data_priority);
        subjectData = new SubjectData(0,"",0);
        testTimeData = new TestTimeData("",0,"",0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // You can have your own calculation for progress
                seekBar.setThumb(getThumb(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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

    @Override public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
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
    private void okButtonClicked(){

        finish();
    };
    private void cancelButtonClicked(){finish();};
    private void testTimeSetButtonClicked(View view){
        Intent intent = new Intent(this, testTimeInsertPopupFragment.class);
        intent.putStringArrayListExtra("idArray",idArray);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode!=0)
            return;
        else{
            if(resultCode==1)
                return;
            testTimeData = (TestTimeData)data.getExtras().getSerializable("testTime");
        }
    }
}
