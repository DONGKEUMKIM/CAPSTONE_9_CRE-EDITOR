package com.example.detection.scheduleAutoCreate;

import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.detection.db.SQLiteManager;
import com.example.detection.db.ScheduleData;
import com.example.detection.db.SubjectData;
import com.example.detection.db.TestTimeData;
import com.google.android.material.tabs.TabLayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ScheduleAutoCreate {
    private static final String TAG = "MainActivity";
    SQLiteManager dbManager = SQLiteManager.sqLiteManager;
    private final String datePattern = "yyyy/MM/dd";
    private final SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
    private final int[] getRandomTimeImportant ={3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,1,1,1};
    private final int[] getRandomTimeNormal ={2,2,2,2,2,2,2,2,2,2,3,3,3,3,3,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,1,1,1};
    private final int[] getRandomTimeNotImportant ={1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,1,2,2,2,2,1,2,1,2,1,2,1,1,1,1,1,1};
    public ScheduleAutoCreate(){
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void init(){
        //test
        //dbManager.deleteScheduleTableALL();
        List<SubjectData> subjectData = dbManager.selectsubjectAll();
        Collections.sort(subjectData);
        for(int i=0;i<subjectData.size();i++) {
            //Log.d(TAG, "Autocreated : "+subjectData.get(i).getAutoCreated());
            if(subjectData.get(i).getAutoCreated()==1){
                Log.d(TAG,subjectData.get(i).getName()+"is already Autocreated");
                continue;
            }
            addNewAutoSchedule(subjectData.get(i));
            //subjectData.get(i).setAutoCreated(0);
            //dbManager.updateSubjectData(subjectData.get(i));
        }

    }//init end

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addNewAutoSchedule(SubjectData subjectData){
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        String[] curDate = sdf.format(date).split("/");
        cal.set(Integer.parseInt(curDate[0]),Integer.parseInt(curDate[1])-1,Integer.parseInt(curDate[2]));
        TestTimeData testTimeData = dbManager.selectTestTimeDataFormSubjectID(subjectData.getID());
        String[] testTime = testTimeData.getDate().split("/");
        int count =0;
        LocalDate date1 = LocalDate.of(Integer.parseInt(testTime[0]),Integer.parseInt(testTime[1]),Integer.parseInt(testTime[2]));
        LocalDate date2 = LocalDate.of(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));
        int pe = (int) (date1.toEpochDay() - date2.toEpochDay());
        Random random = new Random();
        //Period pe = Period.between(date2,date1);
        Log.d(TAG,"PERIOD : "+pe);
        if(pe<=0){
            return;
        }
        for(int i=0;i<pe;i++) {

            if (dbManager.selectScheduleDataByDate(cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DAY_OF_MONTH)).size() >= 3) {
                continue;
            }
            if (dbManager.selectScheduleDataFormSubjectID(subjectData.getID()).size() >= 30) {
                continue;
            }
            if (subjectData.getPriority() == 2) {
                if (count == 3) {
                    ScheduleData insertScheduleData = new ScheduleData(dbManager.generateRandomID(), subjectData.getID(), cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DAY_OF_MONTH), getRandomTimeImportant[random.nextInt(40)],0);
                    dbManager.insertScheduleData(insertScheduleData);
                    cal.add(Calendar.DATE,1);
                    count = 0;
                    subjectData.setAutoCreated(1);
                    dbManager.updateSubjectData(subjectData);
                    continue;
                }
            } else if (subjectData.getPriority() ==1 ) {
                if (count == 4) {
                    ScheduleData insertScheduleData = new ScheduleData(dbManager.generateRandomID(), subjectData.getID(), cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DAY_OF_MONTH), getRandomTimeNormal[random.nextInt(40)],0);
                    dbManager.insertScheduleData(insertScheduleData);
                    cal.add(Calendar.DATE,1);
                    count = 0;
                    subjectData.setAutoCreated(1);
                    dbManager.updateSubjectData(subjectData);
                    continue;
                }
            } else {
                if (count == 5) {
                    ScheduleData insertScheduleData = new ScheduleData(dbManager.generateRandomID(), subjectData.getID(), cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DATE), getRandomTimeNotImportant[random.nextInt(40)],0);
                    dbManager.insertScheduleData(insertScheduleData);
                    cal.add(Calendar.DATE,1);
                    count = 0;
                    subjectData.setAutoCreated(1);
                    dbManager.updateSubjectData(subjectData);
                    continue;
                }
            }
            cal.add(Calendar.DATE,1);
            count++;
        }

    }


    public void writeFile(String number){
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SAC");

        if(!saveFile.exists()){
            saveFile.mkdir();
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile+"/sacinit.txt", false));
            buf.append(number);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
    public String readFile(){
        String line = null;
        String a = null;
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SAC");
        if(!saveFile.exists()){
            saveFile.mkdir();
            writeFile("0");
            return "0";
        }
        try {
            BufferedReader buf = new BufferedReader(new FileReader(saveFile+"/sacinit.txt"));
            while((line=buf.readLine())!=null){
                a = line;
            }
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return a;
    }
    private boolean isDeviceStateInit(){
        if(readFile().equals("0"))
            return true;
        else
            return false;
    }
}
