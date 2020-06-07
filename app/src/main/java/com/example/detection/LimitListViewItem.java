package com.example.detection;

import android.graphics.drawable.Drawable;

public class LimitListViewItem {
    private Drawable appIconDrawable ;
    private String appName ;
    private String appPackageName ;
    private boolean searched ;

    public void setAppIconDrawable(Drawable icon) {
        appIconDrawable = icon ;
    }
    public void setAppName(String title) {
        appName = title ;
    }
    public void setAppPackageName(String desc) {
        appPackageName = desc ;
    }
    public void setSearched(boolean b) {
        searched = b ;
    }

    public Drawable getAppIconDrawable() {
        return this.appIconDrawable ;
    }
    public String getAppName() {
        return this.appName ;
    }
    public String getAppPackageName() {
        return this.appPackageName ;
    }
    public boolean getSearched() {
        return this.searched ;
    }
}