package com.example.detection.fragment.subjectListFragment;

public class SubjectListViewComponent {
    private String subjectName;
    private String priority;
    private String testDate;
    private int id;

    public SubjectListViewComponent(String subjectName, String priority, String testDate,int id) {
        this.subjectName = subjectName;
        this.priority = priority;
        this.testDate = testDate;
        this.id = id;
    }
    public void setId(int id){this.id = id;}
    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }
    public int getId(){return id;}
    public String getPriority() {
        return priority;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getTestDate() {
        return testDate;
    }
}
