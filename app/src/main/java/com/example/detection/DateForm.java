package com.example.detection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateForm {

    //날짜 데이터의 형태의 경우
    //이 부분을 수정 하면됨
    //일단은 yyyy. MM. dd 의 형태
    //예시) 2020. 05. 19

    public SimpleDateFormat df_date;
    String date_str;
    public DateForm()
    {
        df_date = new SimpleDateFormat("yyyy. MM. dd");
    }

}