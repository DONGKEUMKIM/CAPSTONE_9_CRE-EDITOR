package com.example.detection.db;

import java.io.Serializable;

//////////////////////////////////과목 정보/////////////////////////////////////
public class SubjectData implements Serializable {
    private static final long serialVersionUID = 2;
    private int id;                         //id (과목코드)
    private String name;                    //과목 이름
    private int priority;                   //우선 순위


    public SubjectData(int id , String name, int priority){

        this.id = id;
        this.name = name;
        this.priority = priority;
    }

    public int getID(){return this.id;}
    public String getName(){return this.name;}
    public int getPriority(){return this.priority;}

    public void setID(int id){this.id = id;}
    public void setName(String name){this.name = name;}
    public void setPriority(int priority){this.priority = priority;}

}
