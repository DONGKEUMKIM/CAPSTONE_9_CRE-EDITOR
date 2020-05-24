package com.example.detection.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////

public class SQLiteManager extends SQLiteOpenHelper {

    public static SQLiteManager sqLiteManager = null;
    public static final String DATABASE_NAME = "subjectInfo.db";
    public static final int DB_VERSION = 1;

    private SQLiteDatabase db;

    //Subject table
    public static final String SUBJECT_TABLE_NAME = "SUBJECT";
    public static final String SUBJECT_ID = "ID";
    public static final String SUBJECT_NAME = "NAME";
    public static final String SUBJECT_PRIORITY = "PRIORITY";

    //TestTime table
    public static final String TESTTIME_TABLE_NAME = "TESTTIME";
    public static final String TESTTIME_ID = "ID";
    public static final String TESTTIME_SUBJECT_ID = "SUBJECT_ID";
    public static final String TESTTIME_DATE = "DATE";
    public static final String TESTTIME_DURINGTIME = "DURINGTIME";

    //Schedule table
    public static final String SCHEDULE_TABLE_NAME = "SCHEDULE";
    public static final String SCHEDULE_ID = "ID";
    public static final String SCHEDULE_SUBJECT_ID = "SUBJECT_ID";
    public static final String SCHEDULE_DATE = "DATE";
    public static final String SCHEDULE_DURINGTIME = "DURINGTIME";

    //////////////////////////////싱글톤 패턴////////////////////////////////////
    //SQLiteManager.sqLiteManager 로 접근

    public static SQLiteManager getInstance(Context context){
        if(sqLiteManager == null){
            sqLiteManager = new SQLiteManager(context);
        }

        return sqLiteManager;
    }

    private SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
        db = this.getWritableDatabase();
    }
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + SUBJECT_TABLE_NAME + " ("
                + SUBJECT_ID + " INTEGER PRIMARY KEY, "
                + SUBJECT_NAME + " TEXT, "
                + SUBJECT_PRIORITY + " INTEGER"
                + ")");

        db.execSQL("create table if not exists " + TESTTIME_TABLE_NAME + " ("
                + TESTTIME_ID + " TEXT PRIMARY KEY, "
                + TESTTIME_SUBJECT_ID + " INTEGER , "
                + TESTTIME_DATE + " TEXT, "
                + TESTTIME_DURINGTIME + " INTEGER"
                + ")");

        db.execSQL("create table if not exists " + SCHEDULE_TABLE_NAME + " ("
                + SCHEDULE_ID + " TEXT PRIMARY KEY, "
                + SCHEDULE_SUBJECT_ID + " INTEGER , "
                + SCHEDULE_DATE + " TEXT, "
                + SCHEDULE_DURINGTIME + " INTEGER"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SUBJECT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TESTTIME_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_TABLE_NAME);
        onCreate(db);
    }

    /**DB 초기화*/
    public boolean init(){
        //초기화가 필요 할 경우
        //해당 부분에 코딩


        ////////////////////////////DB 테스트////////////////////////////
        //DB에 데이터를 추가 할경우 참고 할것!!

        if(sqLiteManager != null){
            //과목 정보 추가
            //과목 코드와 과목명은 입력받도록 해야함
            //id(과목코드)값은 int형태의 num -> 순서대로 둘어가도록 로직을 짜야함
            //과목명의 경우 string
            //우선순위 -> int형태의 num -> 1~5사이의 값 형태로 들어가도록 로직을 짜야함
            sqLiteManager.insertSubjectData(new SubjectData(1, "머신러닝", 5));
            sqLiteManager.insertSubjectData(new SubjectData(2, "운영체제", 5));
        }

        //시험시간 정보는 과목정보가 존재하는 경우에 추가 할수 있도록 로직을 짜야함
        //시험시간데이터에 과목 코드가 필요 함으로..
        if(sqLiteManager != null){

            String id;
            String date;
            int subject_id;
            String subject_name;
            int duringtime;

            ///////////////////////////////날짜 추가/////////////////////////////////////////////////
            //날짜 데이터 포맷
            //날짜의 경우 선택된 값 ( 예시)Date selectedDate ) 을 받아온뒤
            //date변수로 할당하여 해당 포맷으로 변경
            //date = sdf.format(selectedDate);
            //이 date값을 DB에 추가
            Date selectedDate = new Date(System.currentTimeMillis());                   //선택된 날짜 (테스트용으로 현재 날짜로..)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd");        //날짜 포맷
            date = sdf.format(selectedDate);                                            //선택된 날짜의 포맷 변경
            ////////////////////////////////////////////////////////////////////////////////////////

            ///////////////////////////////id값 추가를 위한 과정//////////////////////////////////////
            Date currentDate = new Date(System.currentTimeMillis());                    //현재 시간
            sdf = new SimpleDateFormat("hhmmss");                               //포맷

            //id값의 경우 겹치면 안된다
            //따라서 선택된 날짜 포맷 + 현재시간 포맷 으로 설정
            id = new String(date.toString() + sdf.format(currentDate));
            ////////////////////////////////////////////////////////////////////////////////////////

            //////////////////////////////////과목 정보//////////////////////////////////////////////
            subject_name = "머신러닝";                     //운동 이름의 경우 선택돼서 들어온 값을 받는다 (테스트용으로 그냥 입력)
            subject_id = SQLiteManager.sqLiteManager.selectSubjectIdFromName(subject_name);        //과목이름으로부터 id(과목코드) 검색

            duringtime = 2;             //시험시간은 입력받아야함 (테스트용으로 2시간으로 입력)

            if(subject_id == -1)
            {
                System.out.println("잘못된 과목코드 입니다.");
            }

            if(SQLiteManager.sqLiteManager.insertTestTimeData((new TestTimeData(id,subject_id,date,duringtime)))){
                System.out.println("시험시간 정보 데이터가 추가되었습니다.");
            }

        }
        return true;
    }

    /**데이터 Insert**/
    //Subject Data insert
    public boolean insertSubjectData(SubjectData data){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SUBJECT_ID, data.getID());
        contentValues.put(SUBJECT_NAME, data.getName());
        contentValues.put(SUBJECT_PRIORITY, data.getPriority());
        long result = db.insert(SUBJECT_TABLE_NAME, null, contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }

    //Testtime Data insert
    public boolean insertTestTimeData(TestTimeData data){
        ContentValues contentValues = new ContentValues();
        contentValues.put(TESTTIME_ID, data.getID());
        contentValues.put(TESTTIME_SUBJECT_ID, data.getSubject_ID());
        contentValues.put(TESTTIME_DATE, data.getDate());
        contentValues.put(TESTTIME_DURINGTIME, data.getDuringtime());
        long result = db.insert(TESTTIME_TABLE_NAME, null, contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }

    /**데이터 Delete**/
    //과목 데이터 전부 삭제
    public void deleteSubjectTableALL(){
        db.execSQL("delete from " + SUBJECT_TABLE_NAME);
    }

    //시험시간 데이터 전부 삭제
    public void deleteTestTimeTableALL(){
        db.execSQL("delete from " + TESTTIME_TABLE_NAME);
    }

    //스케줄 데이터 전부 삭제
    public void deleteScheduleTableALL(){
        db.execSQL("delete from " + SCHEDULE_TABLE_NAME);
    }

    //Schedule Data insert
    public boolean insertScheduleData(ScheduleData data){
        ContentValues contentValues = new ContentValues();
        contentValues.put(SCHEDULE_ID, data.getID());
        contentValues.put(SCHEDULE_SUBJECT_ID, data.getSubject_ID());
        contentValues.put(SCHEDULE_DATE, data.getDate());
        contentValues.put(SCHEDULE_DURINGTIME, data.getDuringtime());
        long result = db.insert(SCHEDULE_TABLE_NAME, null, contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }

    // Subject 전체 조회
    public List<SubjectData> selectsubjectAll(){
        List<SubjectData> dataResultList = new ArrayList<SubjectData>();
        String sql = "select * from "+SUBJECT_TABLE_NAME+" ORDER BY "+SUBJECT_ID+" ASC;";
        Cursor results = db.rawQuery(sql, null);

        if(results.moveToFirst()){
            do{
                SubjectData subjectData
                        = new SubjectData(results.getInt(0), results.getString(1), // ID, Name
                        results.getInt(2)); //Priority
                dataResultList.add(subjectData);
            }while(results.moveToNext());
        }
        return dataResultList;
    }

    //Subject의 전체 이름 조회
    public ArrayList<String> selectAllSubjectName(){
        ArrayList<String> dataResultList = new ArrayList<String>();
        String sql = "select "+SUBJECT_NAME+" from "+SUBJECT_TABLE_NAME+" ORDER BY "+SUBJECT_ID+" DESC;";
        Cursor results = db.rawQuery(sql, null);

        if(results.moveToFirst()){
            do{
                String subjectData = results.getString(results.getColumnIndex(SUBJECT_NAME));
                dataResultList.add(subjectData);
            }while(results.moveToNext());
        }
        return dataResultList;
    }

    //Subject의 이름으로 부터 해당 SubjectData 조회
    public SubjectData selectSubjectDataFormSubjectname(String subjectname)
    {
        SubjectData dataResult = new SubjectData();
        String sql = "select * from "+SUBJECT_TABLE_NAME+" where " + SUBJECT_NAME + " = \'" + subjectname+"\' ;";
        Cursor results = db.rawQuery(sql,null);
        if(results.moveToFirst()){
            SubjectData subjectData
                    = new SubjectData(results.getInt(0), results.getString(1), // ID, Name
                    results.getInt(2));     // Priority
            dataResult = subjectData;
        }
        return dataResult;
    }

    //Subject의 ID값으로 부터 해당 SubjectData 조회
    public SubjectData selectSubjectDataFormSubjectID(int subjecID)
    {
        SubjectData dataResult = new SubjectData();
        String sql = "select * from "+SUBJECT_TABLE_NAME+" where " + SUBJECT_ID + " = \'" + subjecID+"\' ;";
        Cursor results = db.rawQuery(sql,null);
        if(results.moveToFirst()){
            SubjectData subjectData
                    = new SubjectData(results.getInt(0), results.getString(1), // ID, Name
                    results.getInt(2));     // Priority
            dataResult = subjectData;
        }
        return dataResult;
    }

    //Subject의 이름으로 부터 id조회
    public int selectSubjectIdFromName(String subjectName){
        int dataResult = -1;
        String sql = "select " + SUBJECT_ID + " from " + SUBJECT_TABLE_NAME+" where " + SUBJECT_NAME + " = \'" + subjectName+"\' ;";
        Cursor result = db.rawQuery(sql,null);
        if(result.moveToFirst()){
            dataResult = result.getInt(0);
        }
        return dataResult;
    }

    // TestTime 전체 조회
    public List<TestTimeData> selecttesttimeAll(){
        List<TestTimeData> dataResultList = new ArrayList<TestTimeData>();
        String sql = "select * from "+TESTTIME_TABLE_NAME+" ORDER BY "+TESTTIME_ID+" ASC;";
        Cursor results = db.rawQuery(sql, null);

        if(results.moveToFirst()){
            do{
                TestTimeData testtimeData
                        = new TestTimeData(results.getString(0), results.getInt(1), // ID, SubjectID
                        results.getString(2), results.getInt(3)); //Date , DuringTime
                dataResultList.add(testtimeData);

            }while(results.moveToNext());
        }
        return dataResultList;
    }

    //시험시간의 과목ID로 부터 해당 TestTimeData 조회
    public TestTimeData selectTestTimeDataFormSubjectID(int subjectID)
    {
        TestTimeData dataResult = new TestTimeData();
        String sql = "select * from "+TESTTIME_TABLE_NAME+" where " + TESTTIME_SUBJECT_ID + " = \'" + subjectID+"\' ;";
        Cursor results = db.rawQuery(sql,null);
        if(results.moveToFirst()){
            TestTimeData testtimeData
                    = new TestTimeData(results.getString(0), results.getInt(1), // ID, SubjectID
                    results.getString(2),results.getInt(3));     //date , DuringTime
            dataResult = testtimeData;
        }
        return dataResult;
    }

    // Schedule 전체 조회
    public List<ScheduleData> selectscheduleAll(){
        List<ScheduleData> dataResultList = new ArrayList<ScheduleData>();
        String sql = "select * from "+SCHEDULE_TABLE_NAME+" ORDER BY "+SCHEDULE_ID+" ASC;";
        Cursor results = db.rawQuery(sql, null);

        if(results.moveToFirst()){
            do{
                ScheduleData scheduleData
                        = new ScheduleData(results.getString(0), results.getInt(1), // ID, SubjectID
                        results.getString(2), results.getInt(3)); //Date , DuringTime
                dataResultList.add(scheduleData);

            }while(results.moveToNext());
        }
        return dataResultList;
    }

    //스케줄의 과목ID로 부터 해당 ScheduleData 조회
    //과목ID 에 등록된 스케줄이 여러개 일수 있기때문에
    //리스트로 리턴
    public List<ScheduleData> selectScheduleDataFormSubjectID(int subjectID)
    {
        List<ScheduleData> dataResultList = new ArrayList<ScheduleData>();
        String sql = "select * from "+SCHEDULE_TABLE_NAME+" where " + SCHEDULE_SUBJECT_ID + " = \'" + subjectID+"\' ;";
        Cursor results = db.rawQuery(sql,null);

        if(results.moveToFirst()){
            do{
                ScheduleData scheduleData
                        = new ScheduleData(results.getString(0), results.getInt(1), // ID, SubjectID
                        results.getString(2), results.getInt(3)); //Date , DuringTime
                dataResultList.add(scheduleData);

            }while(results.moveToNext());
        }
        return dataResultList;
    }
}
