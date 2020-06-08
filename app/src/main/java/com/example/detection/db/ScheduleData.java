package com.example.detection.db;

import java.io.Serializable;

//////////////////////////////////스케쥴///////////////////////////////////////
public class ScheduleData implements Serializable {
    private static final long serialVersionUID = 1;
    private String id;                          //id
    private int subject_ID;                     //과목 코드
    private String date;                        //날짜
    private int duringtime;                     //공부 시간
    private boolean isDone;                      //이행 여부

    public ScheduleData(String id, int subject_ID, String date, int duringtime, int isdone) {
        this.id = id;
        this.subject_ID = subject_ID;
        this.date = date;
        this.duringtime = duringtime;

        if(isdone == 1) this.isDone = true;
        else this.isDone = false;
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

    public int getIsDone(){if(isDone) return 1; else return 0;}

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

    public void setIsDone(boolean isDone){this.isDone = isDone;}
}
