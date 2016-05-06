package com.douncoding.guaranteedanp_l;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.douncoding.dao.Lesson;

public class LessonDetailsActivity extends AppCompatActivity {
    public static final String TAG = LessonDetailsActivity.class.getSimpleName();

    ViewPager mViewPager;
    TabLayout mTabLayout;

    AppContext mApp;
    int mLessonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager)findViewById(R.id.container);
        mTabLayout = (TabLayout)findViewById(R.id.main_tabs);

        mApp = (AppContext)getApplicationContext();

        mLessonId = getIntent().getIntExtra("lid", -1);
        if (mLessonId == -1) {
            Log.e(TAG, "잘못된 강의번호 수신!!");
        }

        setupTab();
    }

    private void setupTab() {
        SectionPagerAdapter mSectionPagerAdapter =
                new SectionPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionPagerAdapter);
        mViewPager.setCurrentItem(0);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return LessonBasicInfoFragment.newInstance(mLessonId);
                case 1:
                    return AttendanceFragment.newInstance(mLessonId);
                case 2:
                    return LessonContactFragment.newInstance(mLessonId);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "강의";
                case 1:
                    return "출석부";
                case 2:
                    return "연락처";
                default:
                    return null;
            }
        }
    }
}
