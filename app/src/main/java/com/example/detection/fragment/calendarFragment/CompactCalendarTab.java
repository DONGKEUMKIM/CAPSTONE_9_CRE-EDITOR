package com.example.detection.fragment.calendarFragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.detection.MainActivity;
import com.example.detection.R;
import com.example.detection.TestDetection;
import com.example.detection.db.SQLiteManager;
import com.example.detection.db.ScheduleData;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.qap.ctimelineview.TimelineRow;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CompactCalendarTab extends Fragment {

    private static final String TAG = "MainActivity";
    private Calendar currentCalender = Calendar.getInstance(Locale.getDefault());
    private SimpleDateFormat dateFormatForDisplaying = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.getDefault());
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
    private boolean shouldShow = false;
    private CompactCalendarView compactCalendarView;
    private ActionBar toolbar;
    private FloatingActionButton calednarFloatingActionButton;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainTabView = inflater.inflate(R.layout.fragment_calendar, container, false);
        final List<String> mutableBookings = new ArrayList<>();
        final List<String> idList = new ArrayList<>();

        final ListView bookingsListView = mainTabView.findViewById(R.id.bookings_listview);
        final Button showPreviousMonthBut = mainTabView.findViewById(R.id.prev_button);
        final Button showNextMonthBut = mainTabView.findViewById(R.id.next_button);
        final Button slideCalendarBut = mainTabView.findViewById(R.id.slide_calendar);
        final Button showCalendarWithAnimationBut = mainTabView.findViewById(R.id.show_with_animation_calendar);
        final Button setLocaleBut = mainTabView.findViewById(R.id.set_locale);
        final Button removeAllEventsBut = mainTabView.findViewById(R.id.remove_all_events);
        final Button addNewScheduleBut = mainTabView.findViewById(R.id.add_schedule_calendar);
        final CalendarArrayAdapter adapter = new CalendarArrayAdapter(getContext(), R.layout.calendar_list_view_component, mutableBookings);
        calednarFloatingActionButton = mainTabView.findViewById(R.id.calendar_floatingActionButton);
        bookingsListView.setAdapter(adapter);
        compactCalendarView = mainTabView.findViewById(R.id.compactcalendar_view);

        // below allows you to configure color for the current day in the month
        // compactCalendarView.setCurrentDayBackgroundColor(getResources().getColor(R.color.black));
        // below allows you to configure colors for the current day the user has selected
        // compactCalendarView.setCurrentSelectedDayBackgroundColor(getResources().getColor(R.color.dark_red));
        compactCalendarView.setUseThreeLetterAbbreviation(false);
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
        compactCalendarView.setIsRtl(false);
        compactCalendarView.displayOtherMonthDays(false);
        //compactCalendarView.setIsRtl(true);
        //loadEvents();
        //loadEventsForYear(2017);

        //공부를 시작하기 위해 넘겨줘야할 인텐트값 -> 공부 완료후 DB에 접근하여 이행여부 수정 위해
        //String seletedID;   //선택된 스케줄의의 ID값
        //String selectedSN;  //선택된 과목 이름
        //int selectedDT;     //선택된 공부 시간

        for(int i=2000;i<2050;i++){
            loadEventsForYear(i);
        }
        compactCalendarView.invalidate();
        bookingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                /*
                //디택션 화면으로 이동
                //인텐트값 전달
                //과목이름 , 공부시간

                //아이템으로 부터 ID, SN, DT 값 셋팅
                String subjectsceduleData = bookingsListView.getItemAtPosition(i).toString();
                String[] word = subjectsceduleData.split("at");

                String selectedSN = word[0];       //과목정보 셋팅


                word[1].split("for");

                int selectedDT = Integer.parseInt(word[0]);       //공부시간 셋팅

                Intent intent = new Intent(getContext(), TestDetection.class);
                //intent.putExtra("ID", seletedID);
                intent.putExtra("SN", selectedSN);
                intent.putExtra("DT", selectedDT);
                
                startActivity(intent);
                //TODO
                //On Schedule List Item Click Listener
                //Code the Detection Activity with result
                */

                //targetString
                String string = (String)adapterView.getAdapter().getItem(i);
                //Log.d(TAG, string);

                //string데이터로부터
                //과목 이름 string , 공부 시간 int 값 추출 필요
                //이 부분 코딩해주세요

                //테스트용 후에 지울걸


                //idList 스케줄의 idList
                //포지션값 i 로 아이템을 참조 했을 대 그 스케줄의 id string을 참조 할 수 있음
                //이걸로  과목명과 스케줄날짜 를 받아오도록 변경 할것!
                //

                String scheduleID = idList.get(i);
                String subjectName = null;
                String scheduleDate = null;


                String[] words = string.split(" ");
                int duringTime = 0;


                if(words[2].compareTo("at") != 0)
                {
                    //과목명에 띄어쓰기가 있는경우에 대한 예외처리
                    subjectName = words[1] + " " + words[2];
                    scheduleDate = words[4];
                    duringTime = Integer.parseInt(words[6]);

                }
                else
                {
                    subjectName = words[1];
                    scheduleDate = words[3];
                    duringTime = Integer.parseInt(words[5]);
                }

                /*
                //스케줄데이터의 이행여부 확인 방법 예시
                //SQlite 커리문 and가 동작 하지 않아 객체에 직접 접근하여 확인함..

                int subjectID = SQLiteManager.sqLiteManager.selectSubjectIdFromName(subjectName);
                List<ScheduleData> Lists = SQLiteManager.sqLiteManager.selectScheduleDataFormSubjectID(subjectID);

                int kkk;
                for(int lSize = 0 ; lSize < Lists.size() ; lSize++)
                {
                    if(Lists.get(lSize).getDate() == scheduleDate)
                        kkk = Lists.get(lSize).getIsDone();
                }
                */

                ((MainActivity)getActivity()).startDetectionFromSchedule(scheduleID, subjectName, scheduleDate, duringTime);

                ScheduleData scheduledata = SQLiteManager.sqLiteManager.selectScheduleDataFormID(scheduleID);
                System.out.println("이행여부 " + String.valueOf(scheduledata.getIsDone()));
            }
        });
        bookingsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                ((MainActivity)getActivity()).editSchedule(idList.get(i));

                return false;
            }
        });

        logEventsByMonth(compactCalendarView);

        // below line will display Sunday as the first day of the week
        // compactCalendarView.setShouldShowMondayAsFirstDay(false);

        // disable scrolling calendar
        // compactCalendarView.shouldScrollMonth(false);

        // show days from other months as greyed out days
        // compactCalendarView.displayOtherMonthDays(true);

        // show Sunday as first day of month
        // compactCalendarView.setShouldShowMondayAsFirstDay(false);

        //set initial title
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        toolbar.setTitle(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        //set title on calendar scroll
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                toolbar.setTitle(dateFormatForMonth.format(dateClicked));
                List<Event> bookingsFromMap = compactCalendarView.getEvents(dateClicked);
                Log.d(TAG, "inside onclick " + dateFormatForDisplaying.format(dateClicked));
                if (bookingsFromMap != null) {
                    Log.d(TAG, bookingsFromMap.toString());
                    mutableBookings.clear();
                    idList.clear();
                    for (Event booking : bookingsFromMap) {
                        mutableBookings.add(((String) booking.getData()));
                        idList.add(((String) booking.getData()).split(":")[1]);
                    }
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                toolbar.setTitle(dateFormatForMonth.format(firstDayOfNewMonth));
            }
        });

        showPreviousMonthBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compactCalendarView.scrollLeft();
            }
        });

        showNextMonthBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compactCalendarView.scrollRight();
            }
        });
        calednarFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewSchedule();
            }
        });
        addNewScheduleBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewSchedule();
            }
        });
        final View.OnClickListener showCalendarOnClickLis = getCalendarShowLis();
        slideCalendarBut.setOnClickListener(showCalendarOnClickLis);

        final View.OnClickListener exposeCalendarListener = getCalendarExposeLis();
        showCalendarWithAnimationBut.setOnClickListener(exposeCalendarListener);

        compactCalendarView.setAnimationListener(new CompactCalendarView.CompactCalendarAnimationListener() {
            @Override
            public void onOpened() {
            }

            @Override
            public void onClosed() {
            }
        });

        setLocaleBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Locale locale = Locale.KOREA;
                dateFormatForDisplaying = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", locale);
                TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
                dateFormatForDisplaying.setTimeZone(timeZone);
                dateFormatForMonth.setTimeZone(timeZone);
                compactCalendarView.setLocale(timeZone, locale);
                compactCalendarView.setUseThreeLetterAbbreviation(false);
                for(int i=2000;i<2050;i++){
                    loadEventsForYear(i);
                }
                logEventsByMonth(compactCalendarView);

            }
        });

        removeAllEventsBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compactCalendarView.removeAllEvents();
            }
        });


        // uncomment below to show indicators above small indicator events
        // compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);

        // uncomment below to open onCreate
        //openCalendarOnCreate(v);

        return mainTabView;
    }

    @NonNull
    private View.OnClickListener getCalendarShowLis() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!compactCalendarView.isAnimating()) {
                    if (shouldShow) {
                        compactCalendarView.showCalendar();
                    } else {
                        compactCalendarView.hideCalendar();
                    }
                    shouldShow = !shouldShow;
                }
            }
        };
    }

    @NonNull
    private View.OnClickListener getCalendarExposeLis() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!compactCalendarView.isAnimating()) {
                    if (shouldShow) {
                        compactCalendarView.showCalendarWithAnimation();
                    } else {
                        compactCalendarView.hideCalendarWithAnimation();
                    }
                    shouldShow = !shouldShow;
                }
            }
        };
    }

    private void openCalendarOnCreate(View v) {
        final RelativeLayout layout = v.findViewById(R.id.main_content);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                compactCalendarView.showCalendarWithAnimation();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));
        // Set to current day on resume to set calendar to latest day
        // toolbar.setTitle(dateFormatForMonth.format(new Date()));
    }

    private void loadEvents() {
        addEvents(-1, -1);
        addEvents(Calendar.DECEMBER, -1);
        addEvents(Calendar.AUGUST, -1);
    }

    private void loadEventsForYear(int year) {
        addEvents(Calendar.JANUARY, year);
        addEvents(Calendar.FEBRUARY, year);
        addEvents(Calendar.MARCH, year);
        addEvents(Calendar.APRIL, year);
        addEvents(Calendar.MAY, year);
        addEvents(Calendar.JUNE, year);
        addEvents(Calendar.JULY, year);
        addEvents(Calendar.AUGUST, year);
        addEvents(Calendar.SEPTEMBER, year);
        addEvents(Calendar.OCTOBER, year);
        addEvents(Calendar.NOVEMBER, year);
        addEvents(Calendar.DECEMBER, year);
    }

    private void logEventsByMonth(CompactCalendarView compactCalendarView) {
        currentCalender.setTime(new Date());
        currentCalender.set(Calendar.DAY_OF_MONTH, 1);
        currentCalender.set(Calendar.MONTH, Calendar.AUGUST);
        List<String> dates = new ArrayList<>();
        for (Event e : compactCalendarView.getEventsForMonth(new Date())) {
            dates.add(dateFormatForDisplaying.format(e.getTimeInMillis()));
        }
        Log.d(TAG, "Events for Aug with simple date formatter: " + dates);
        Log.d(TAG, "Events for Aug month using default local and timezone: " + compactCalendarView.getEventsForMonth(currentCalender.getTime()));
    }

    private List<ScheduleData> scheduleFilter(int month, int year){
        SQLiteManager dbManager = SQLiteManager.sqLiteManager;
        List<ScheduleData> scheduleData = dbManager.selectscheduleAll();
        List<ScheduleData> returnData = new ArrayList<ScheduleData>();
        for(int i=0; i<scheduleData.size();i++){
            if(Integer.parseInt(scheduleData.get(i).getDate().split("/")[0])==year){
                if(Integer.parseInt(scheduleData.get(i).getDate().split("/")[1])==month){
                    returnData.add(scheduleData.get(i));
                }
            }
        }
        return returnData;
    }

    private void addEvents(int month, int year) {

        List<ScheduleData> filteredData = scheduleFilter(month, year);
        currentCalender.setTime(new Date());
        currentCalender.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = currentCalender.getTime();
        for (int i = 0; i < currentCalender.getActualMaximum(Calendar.DATE); i++) {
            currentCalender.setTime(firstDayOfMonth);
            if (month > -1) {
                currentCalender.set(Calendar.MONTH, month-1);
            }
            if (year > -1) {
                currentCalender.set(Calendar.ERA, GregorianCalendar.AD);
                currentCalender.set(Calendar.YEAR, year);
            }
            currentCalender.add(Calendar.DATE, i);
            setToMidnight(currentCalender);
            long timeInMillis = currentCalender.getTimeInMillis();

            List<Event> events = getEvents(filteredData, timeInMillis, i+1);

            compactCalendarView.addEvents(events);
        }




    }
    private String getSubjectName(int subjID){
        SQLiteManager dbManager = SQLiteManager.sqLiteManager;
        return dbManager.selectSubjectDataFormSubjectID(subjID).getName();
    }
    private List<Event> getEvents(List<ScheduleData> scheduleData, long timeInMillis, int day) {
        List<Event> eventArray = new ArrayList<Event>();
        for(int i=0;i<scheduleData.size();i++){
            if(Integer.parseInt(scheduleData.get(i).getDate().split("/")[2])==day){
                eventArray.add( new Event(Color.argb(255, 169, 68, 65), timeInMillis,"Study "+getSubjectName(scheduleData.get(i).getSubject_ID())+ " at " + scheduleData.get(i).getDate() +" for "+ scheduleData.get(i).getDuringtime()+" Hours" + ":" + scheduleData.get(i).getID()+":"+scheduleData.get(i).getIsDone()  ) );
            }
        }
        return eventArray;
        /*
        if (day < 2) {
            return Arrays.asList(new Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + new Date(timeInMillis)));
        } else if (day > 2 && day <= 4) {
            return Arrays.asList(
                    new Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + new Date(timeInMillis)),
                    new Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + new Date(timeInMillis)));
        } else {
            return Arrays.asList(
                    new Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + new Date(timeInMillis)),
                    new Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + new Date(timeInMillis)),
                    new Event(Color.argb(255, 70, 68, 65), timeInMillis, "Event 3 at " + new Date(timeInMillis)));
        }*/
    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void addNewSchedule() {
        ((MainActivity) getActivity()).addNewSchedule();
    }
}