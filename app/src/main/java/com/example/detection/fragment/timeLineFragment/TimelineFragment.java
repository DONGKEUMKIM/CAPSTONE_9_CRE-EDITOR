package com.example.detection.fragment.timeLineFragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.detection.MainActivity;
import com.example.detection.R;
import com.example.detection.db.SQLiteManager;
import com.example.detection.db.ScheduleData;

import org.qap.ctimelineview.TimelineRow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TimelineFragment extends Fragment {

    //Create Timeline Rows List
    private ArrayList<TimelineRow> timelineRowsList = new ArrayList<>();
    List<ScheduleData> listScheduleData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        // Add Random Rows to the List
        super.onActivityCreated(savedInstanceState);
        for (int i = 0; i < 15; i++) {
            //add the new row to the list
            //timelineRowsList.add(createRandomTimelineRow(i));
        }
        timelineRowsList.clear();
        SQLiteManager dbManager = SQLiteManager.sqLiteManager;
        listScheduleData = new ArrayList<ScheduleData>();
        listScheduleData = dbManager.selectscheduleAll();
        if(listScheduleData.size()==0){
            try {
                timelineRowsList.add(emptyTimelineRow());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else {
            for (int i = 0; i < listScheduleData.size(); i++) {
                try {
                    timelineRowsList.add(createTimelineRow(i));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }


        //Create the Timeline Adapter
        ArrayAdapter<TimelineRow> myAdapter = new TimelineViewAdapter(this.getActivity(), R.layout.ctimeline_row, timelineRowsList,
                //if true, list will be sorted by date
                true);

        View v = getView();
        //Get the ListView and Bind it with the Timeline Adapter
        ListView myListView = (ListView) v.findViewById(R.id.timeline_listView);
        myListView.setAdapter(myAdapter);

        /*
        for (int j = 0; j < listScheduleData.size(); j++) {

            try {
                myAdapter.add(createTimelineRow(j));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        */
        //if you wish to handle list scrolling
        myListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int currentScrollState;
            private int currentFirstVisibleItem;
            private int totalItem;
            private LinearLayout lBelow;


            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                this.currentScrollState = scrollState;
                this.isScrollCompleted();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.totalItem = totalItemCount;


            }

            private void isScrollCompleted() {
                if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                        && this.currentScrollState == SCROLL_STATE_IDLE) {
                    Toast.makeText(getActivity(), "End of Timeline", Toast.LENGTH_SHORT).show();
                    ////on scrolling to end of the list, add new rows
                    for (int i = 0; i < 15; i++) {
                        //myAdapter.add(createRandomTimelineRow(i));
                    }

                }
            }


        });

        //if you wish to handle the clicks on the rows
        AdapterView.OnItemClickListener adapterListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TimelineRow row = timelineRowsList.get(position);
                Toast.makeText(getActivity(), row.getTitle(), Toast.LENGTH_SHORT).show();
            }
        };
        myListView.setOnItemClickListener(adapterListener);


    }
    private TimelineRow emptyTimelineRow() throws ParseException{
        TimelineRow myRow = new TimelineRow(0);
        myRow.setTitle("No Schedule");
        return  myRow;
    }

    private TimelineRow createTimelineRow(int id) throws ParseException {
        //SQLiteManager dbManager = ((MainActivity) getActivity()).getDbManager();

        TimelineRow myRow = new TimelineRow(id);
        ScheduleData sch = listScheduleData.get(id);
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy/MM/dd");
        String[] schdatesplit = sch.getDate().split("/");
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(schdatesplit[0]),Integer.parseInt(schdatesplit[1])-1,Integer.parseInt(schdatesplit[2]),0,0,0);
        cal.set(Calendar.MILLISECOND,0);
        Date date = cal.getTime();
        //to set the row Date (optional)
        myRow.setDate(date);
        //to set the row Title (optional)
        myRow.setTitle(((MainActivity) Objects.requireNonNull(getActivity())).getSubjectData(sch.getSubject_ID()).getName());
        //to set the row Description (optional)
        myRow.setDescription(String.valueOf(sch.getDuringtime()));
        //to set the row bitmap image (optional)
        if(sch.getIsDone()==0) {
            myRow.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic_incomplete));
        }else{
            myRow.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic_complete));
        }
        //to set row Below Line Color (optional)
        if(sch.getIsDone()==0) {
            myRow.setBellowLineColor(Color.argb(255,255,0,0));
        }else{
            myRow.setBellowLineColor(Color.argb(255,0,255,0));
        }
        //myRow.setBellowLineColor(getRandomColor());
        //to set row Below Line Size in dp (optional)
        myRow.setBellowLineSize(sch.getDuringtime()*3);
        //to set row Image Size in dp (optional)
        myRow.setImageSize(30);
        //to set background color of the row image (optional)
        //myRow.setBackgroundColor(getRandomColor());
        //to set the Background Size of the row image in dp (optional)
        //myRow.setBackgroundSize(sch.getDuringtime() * 3);
        //to set row Date text color (optional)
        //myRow.setDateColor(getRandomColor());
        //to set row Title text color (optional)
        //myRow.setTitleColor(getRandomColor());
        //to set row Description text color (optional)
        //myRow.setDescriptionColor(getRandomColor());
        //Drawable drawable = getResources().getDrawable(R.drawable.time_line_component);
        //BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
        //myRow.setImage(bitmapDrawable.getBitmap());

        return myRow;
    }

    //Method to create new Timeline Row
    private TimelineRow createRandomTimelineRow(int id) {

        // Create new timeline row (pass your Id)
        TimelineRow myRow = new TimelineRow(id);

        //to set the row Date (optional)
        myRow.setDate(getRandomDate());
        //to set the row Title (optional)
        myRow.setTitle("Title " + id);
        //to set the row Description (optional)
        myRow.setDescription("Description " + id);
        //to set the row bitmap image (optional)
        myRow.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic_check_circle_black_24dp));
        //to set row Below Line Color (optional)
        myRow.setBellowLineColor(getRandomColor());
        //to set row Below Line Size in dp (optional)
        myRow.setBellowLineSize(getRandomNumber(2, 25));
        //to set row Image Size in dp (optional)
        myRow.setImageSize(getRandomNumber(25, 40));
        //to set background color of the row image (optional)
        myRow.setBackgroundColor(getRandomColor());
        //to set the Background Size of the row image in dp (optional)
        myRow.setBackgroundSize(getRandomNumber(25, 40));
        //to set row Date text color (optional)
        myRow.setDateColor(getRandomColor());
        //to set row Title text color (optional)
        myRow.setTitleColor(getRandomColor());
        //to set row Description text color (optional)
        myRow.setDescriptionColor(getRandomColor());

        return myRow;
    }


    //Random Methods
    public int getRandomColor() {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        ;
        return color;
    }

    public int getRandomNumber(int min, int max) {
        return min + (int) (Math.random() * max);
    }


    public Date getRandomDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date startDate = null;
        Date endDate = new Date();
        try {
            startDate = sdf.parse("02/09/2015");
            long random = ThreadLocalRandom.current().nextLong(startDate.getTime(), endDate.getTime());
            endDate = new Date(random);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return endDate;
    }
    public Date getDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date startDate = new Date();
        Date endDate=date;
        return endDate;

    }

}