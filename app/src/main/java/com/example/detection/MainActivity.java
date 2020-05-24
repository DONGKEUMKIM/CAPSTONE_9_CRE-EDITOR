package com.example.detection;

import android.animation.Animator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.detection.fragment.calendarFragment.CompactCalendarTab;
import com.example.detection.fragment.timeLineFragment.TimelineFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import yalantis.com.sidemenu.interfaces.Resourceble;
import yalantis.com.sidemenu.interfaces.ScreenShotable;
import yalantis.com.sidemenu.model.SlideMenuItem;
import yalantis.com.sidemenu.util.ViewAnimator;

public class MainActivity extends AppCompatActivity implements ViewAnimator.ViewAnimatorListener {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private List<SlideMenuItem> list = new ArrayList<>();
    private ViewAnimator viewAnimator;
    private int res = R.drawable.content_music;
    private LinearLayout linearLayout;

    private int fragment_main = R.layout.fragment_main;
    private int fragment_calendar = R.layout.fragment_calendar;
    private int fragment_timeline = R.layout.fragment_timeline;

    private FragmentManager fragmentManager;
    //private ContentFragment content_frag;
    private CompactCalendarTab calendar_frag;
    private TimelineFragment timeline_frag;


    private FragmentTransaction transaction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        ContentFragment contentFragment = ContentFragment.newInstance(R.drawable.content_dashboard);
        calendar_frag = new CompactCalendarTab();
        timeline_frag = new TimelineFragment();

        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_frame, contentFragment).commitAllowingStateLoss();

        //ContentFragment contentFragment = ContentFragment.newInstance(R.drawable.content_music);
        //getSupportFragmentManager().beginTransaction()
        //        .replace(R.id.content_frame, contentFragment)
        //        .commit();



        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.TRANSPARENT);

        linearLayout = findViewById(R.id.left_drawer);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
            }
        });


        setActionBar();
        createMenuList();
        viewAnimator = new ViewAnimator<>(this, list, contentFragment, drawerLayout, this);

        //List<SubjectData> subjectresults = SQLiteManager.sqLiteManager.selectsubjectAll();
        //List<TestTimeData> s = SQLiteManager.sqLiteManager.selecttesttimeAll();

        int kkk = 0;
    }

    private void createMenuList() {
        SlideMenuItem menuItem0 = new SlideMenuItem(ContentFragment.CLOSE, R.drawable.close_icon);
        list.add(menuItem0);
        SlideMenuItem menuItem = new SlideMenuItem(ContentFragment.Dashboard, R.drawable.dashboard_icon);
        list.add(menuItem);
        SlideMenuItem menuItem2 = new SlideMenuItem(ContentFragment.Calendar, R.drawable.calendar_icon);
        list.add(menuItem2);
        SlideMenuItem menuItem3 = new SlideMenuItem(ContentFragment.Watch, R.drawable.watch_icon);
        list.add(menuItem3);
        SlideMenuItem menuItem4 = new SlideMenuItem(ContentFragment.Scores, R.drawable.scores_icon);
        list.add(menuItem4);
        SlideMenuItem menuItem5 = new SlideMenuItem(ContentFragment.Timeline, R.drawable.timeline_icon);
        list.add(menuItem5);
        SlideMenuItem menuItem6 = new SlideMenuItem(ContentFragment.Search, R.drawable.search_icon);
        list.add(menuItem6);
        SlideMenuItem menuItem7 = new SlideMenuItem(ContentFragment.Setting, R.drawable.settings_icon);
        list.add(menuItem7);
    }


    private void setActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(
                this,                //host activ
                drawerLayout,        //drawer obj
                toolbar,  // icon replace
                R.string.drawer_open,  //open drawer description
                R.string.drawer_close  //"close drawer" description
        ) {

            // Called when a drawer has settled in a completely closed state
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                linearLayout.removeAllViews();
                linearLayout.invalidate();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset > 0.6 && linearLayout.getChildCount() == 0)
                    viewAnimator.showMenuContent();
            }

            // Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ScreenShotable replaceFragment(ScreenShotable screenShotable, int topPosition,String contentName) {
        if(contentName.equals("Watch")) {
            this.res = R.drawable.content_dashboard;
        }else if(contentName.equals("Timeline")){
            this.res = R.drawable.content_timeline;
        }else if(contentName.equals("Calendar")){
            this.res = R.drawable.content_calendar;
        }else {
            this.res = this.res == R.drawable.content_music ? R.drawable.content_dashboard : R.drawable.content_music;
        }



        View view = findViewById(R.id.content_frame);
        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        Animator animator = ViewAnimationUtils.createCircularReveal(view, 0, topPosition, 0, finalRadius);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(ViewAnimator.CIRCULAR_REVEAL_ANIMATION_DURATION);

        findViewById(R.id.content_overlay).setBackground(new BitmapDrawable(getResources(), screenShotable.getBitmap()));
        animator.start();

        ContentFragment contentFragment = ContentFragment.newInstance(this.res);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, contentFragment).commit();
        return contentFragment;
    }

    @Override
    public ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable, int position) {
        transaction = fragmentManager.beginTransaction();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        if (ContentFragment.CLOSE.equals(slideMenuItem.getName())) {
            return screenShotable;
        }
        if (ContentFragment.Watch.equals(slideMenuItem.getName())){
            Intent intent = new Intent(getApplicationContext(),TestDetection.class);
            startActivity(intent);
        }
        if (ContentFragment.Timeline.equals(slideMenuItem.getName())){

            transaction.replace(R.id.frameLayout,timeline_frag).commitAllowingStateLoss();
        }
        if(ContentFragment.Calendar.equals(slideMenuItem.getName())){
            transaction.replace(R.id.frameLayout,calendar_frag).commitAllowingStateLoss();

        }
        return replaceFragment(screenShotable, position,slideMenuItem.getName());
    }

    @Override
    public void disableHomeButton() {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(false);

    }

    @Override
    public void enableHomeButton() {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        drawerLayout.closeDrawers();

    }

    @Override
    public void addViewToContainer(View view) {
        linearLayout.addView(view);
    }


    //week view Listener




}
