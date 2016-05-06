package com.douncoding.guaranteedanp_l;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.douncoding.dao.Instructor;

public class MainActivity extends AppCompatActivity implements
    LessonListFragment.OnListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    /**
     * 툴바와 메뉴화면
     */
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    Toolbar mToolbar;
    AppBarLayout mAppBarLayout;
    CollapsingToolbarLayout mCollapsingLayout;
    FloatingActionButton mFab;

    /**
     * 사용자 정보
     */
    LinearLayout mProfileView;
    TextView mProfileName;
    TextView mProfileJobs;

    AppContext mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mAppBarLayout = (AppBarLayout)findViewById(R.id.appbar_layout);
        mCollapsingLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_layout);
        mFab = (FloatingActionButton)findViewById(R.id.fab);
        mProfileView = (LinearLayout)findViewById(R.id.profile_container);

        mProfileName = (TextView)findViewById(R.id.profile_name);
        mProfileJobs = (TextView)findViewById(R.id.profile_jobs);

        mApp = (AppContext) getApplicationContext();

        /**
         * 툴바 생성
         */
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() == null) {
            throw new NullPointerException("toolbar bind error");
        }

        /**
         * 네비케이션 생성
         */
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        /**
         * Collasping 생성
         */
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                if (scrollRange + verticalOffset == 0) {
                    mProfileView.setVisibility(View.GONE);
                } else {
                    mProfileView.setVisibility(View.VISIBLE);
                    mCollapsingLayout.setTitle("");
                }
            }
        });

        /**
         * 내정보 화면
         */
        setupProfileView();

        /**
         * 강의생성 화면 출력
         */
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreatorActivity.class));
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        mToolbar.setTitle("내 강의정보");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, LessonListFragment.newInstance())
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setupProfileView() {
        Instructor student = mApp.내정보.얻기();

        mProfileName.setText(student.getName());
        mProfileJobs.setText(student.getEmail());
    }

    /**
     *
     */
    @Override
    public void onNavigateToDetailView(int lessonId) {
        Intent intent = new Intent(this, LessonDetailsActivity.class);
        intent.putExtra("lid", lessonId);
        startActivity(intent);
    }
}
