package com.douncoding.guaranteedanp_l;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class CreatorActivity extends AppCompatActivity implements
        View.OnClickListener, LessonCreateFragment.OnListener {
    public static final String TAG = CreatorActivity.class.getSimpleName();

    Toolbar mToolbar;
    ImageButton mCompleteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_create);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mCompleteButton = (ImageButton)findViewById(R.id.create_complete);
        mCompleteButton.setOnClickListener(this);

        if (getSupportActionBar() != null ) {
            getSupportActionBar().setTitle("강의 생성");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, LessonCreateFragment.newInstance()
                        , LessonCreateFragment.TAG)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.create_complete:
                Fragment fragment = getSupportFragmentManager()
                        .findFragmentByTag(LessonCreateFragment.TAG);

                if (fragment != null) {
                    ((LessonCreateFragment)fragment).registerLesson();
                }
                break;
        }
    }

    @Override
    public void moveToCompletedLessonCreate() {
        Log.d(TAG, "moveToCompletedLessonCreate");
        finish();
    }
}
