package com.example.detection.db;

import java.io.Serializable;

//////////////////////////////////시험 시간/////////////////////////////////////
public class TestTimeData implements Serializable {
    private static final long serialVersionUID = 3;
    private String id;                      //id
    private int subject_ID;                 //과목 코드
    private String date;                    //날짜
    private int duringtime;                 //시험 시간


    public TestTimeData(String id, int subject_ID, String date, int duringtime) {
        this.id = id;
        this.subject_ID = subject_ID;
        this.date = date;
        this.duringtime = duringtime;
    }

    public String getID() {
        return id;
    }

    public int getSubject_ID() {
        return subject_ID;
    }

    public String getDate() {
        return this.date;
    }

    public int getDuringtime() {
        return this.duringtime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSubject_ID(int subject_ID) {
        this.subject_ID = subject_ID;
    }

    public void setdate(String date) {
        this.date = date;
    }

    public void setDuringtime(int duringtime) {
        this.duringtime = duringtime;
    }
}
