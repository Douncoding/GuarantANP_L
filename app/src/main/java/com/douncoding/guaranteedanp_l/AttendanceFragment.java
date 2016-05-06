package com.douncoding.guaranteedanp_l;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.douncoding.dao.*;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.douncoding.dao.LessonTime;
import com.github.mikephil.charting.utils.ColorTemplate;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 학생 별 출석 일 수 (Horizontal-BarChart)
 * 전체 출석비율 (PieChart)
 */
public class AttendanceFragment extends Fragment implements
        OnChartValueSelectedListener {
    public static final String TAG = AttendanceFragment.class.getSimpleName();

    public static final String EXTRA_PARAM1 = "param1";

    public AttendanceFragment() {}

    AppContext mApp;
    WebService mWebService;

    private Lesson mLesson;
    private List<Student> mStudents;
    private List<LessonTime> mLessonTime;
    private HashMap<Long, List<Attendance>> mAttendanceMap = new HashMap<>();

    /**
     * UI
     */
    HorizontalBarChart mHorizontalChart;
    PieChart mPieChart;

    Button mHorizonSelView;
    Button mPieSelView;

    public static AttendanceFragment newInstance(int lessonId) {
        AttendanceFragment fragment = new AttendanceFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_PARAM1, lessonId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mApp = (AppContext)context.getApplicationContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int lessonId = getArguments().getInt(EXTRA_PARAM1);
            mLesson = mApp.openDBReadable().getLessonDao().load((long)lessonId);
            mLessonTime = mLesson.getLessonTimeList();
            Log.i(TAG, "강의정보 읽기 완료: 식별자:" + mLesson.getId());
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mWebService = retrofit.create(WebService.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);

        mHorizontalChart = (HorizontalBarChart)view.findViewById(R.id.horizontal_chart);
        mPieChart = (PieChart)view.findViewById(R.id.pie_chart);
        mHorizonSelView = (Button)view.findViewById(R.id.student_chart_sel);
        mHorizonSelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPieChart.setVisibility(View.GONE);
                mHorizontalChart.setVisibility(View.VISIBLE);

                horizontalChartSetData();
                mHorizontalChart.invalidate();
                mHorizontalChart.animateY(3000);
            }
        });
        mPieSelView = (Button)view.findViewById(R.id.total_chart_sel);
        mPieSelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHorizontalChart.setVisibility(View.GONE);
                mPieChart.setVisibility(View.VISIBLE);

                // 수업일 수
                int day = getTotalDayOfLesson(mLesson.getLessonTimeList());
                // 학생 수 * 수업일 수
                int totalDay = day * mStudents.size();
                // 전체 출석일 수
                int attendDay = 0;

                for (int i = 0; i < mStudents.size(); i++) {
                    attendDay += getAttendanceDay(i);
                }

                pieChartSetData(attendDay, 0, 0, totalDay);
                mPieChart.invalidate();
                mPieChart.animateY(3000, Easing.EasingOption.EaseInOutQuad);
            }
        });

        mHorizontalChart.setOnChartValueSelectedListener(this);
        mHorizontalChart.animateY(3000);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadData();
    }

    private void loadData() {

        new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                int lessonId = mLesson.getId().intValue();

                try {
                    mStudents = mWebService.getStudentsOfLesson(lessonId).execute().body();

                    for (Student student : mStudents) {
                        int studentId = student.getId().intValue();

                        List<Attendance> attendances = mWebService.getAttendancesOfStudent(
                                studentId, lessonId).execute().body();
                        mAttendanceMap.put(student.getId(), attendances);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                horizontalChartSetData();
            }
        }.execute();
    }

    /**
     * 전체 수업 일 수를 구한다. 시작일 부터 종요일까지 주기로 선정된
     * 요일과 일치하는 개수를 의미한다.
     * @param lessonTimes 강의의 강의시간
     * @return 총 수업 일 수
     */
    private int getTotalDayOfLesson(List<LessonTime> lessonTimes) {
        int totalDay = 0;

        if (lessonTimes.size() == 0) {
            Log.w(TAG, "강의시간 목록 비어있음");
            return 0;
        }

        // 총 일수 계산을 위한 대표
        LessonTime time = lessonTimes.get(0);

        Calendar sc = Calendar.getInstance();
        Calendar ec = Calendar.getInstance();
        sc.setTime(time.getStartDate());
        ec.setTime(time.getEndDate());

        // 시작일 ~ 종료일 요일 표현
        while (sc.getTimeInMillis() <= ec.getTimeInMillis()) {
            sc.add(Calendar.DATE, 1);
            for (LessonTime day : lessonTimes) {
                if (sc.get(Calendar.DAY_OF_WEEK) == day.getDay()) {
                    totalDay++;
                }
            }
        }
        return totalDay;
    }

    /**
     * 학생의 출석 일 수 추출
     * @param pos 메모리상의 학생 리스트 중 구하고자 하는 학생의 위치
     * @return 출석일수
     */
    private int getAttendanceDay(int pos) {
        int attendDay = 0;

        Student student = mStudents.get(pos);

        List<Attendance> list = mAttendanceMap.get(student.getId());

        if (list == null || list.size() == 0)
            return 0;

        for (Attendance item : list) {
            switch (item.getState()) {
                case 0:

                    break;
                case 1:
                    attendDay++;
                    break;
            }
        }
        return attendDay;
    }

    /**
     * 학생 별 출석샅태를 출력
     */
    private void horizontalChartSetData() {
        ArrayList<BarEntry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        for (int i = 0; i < mStudents.size(); i++) {
            yVals.add(new BarEntry((float) getAttendanceDay(i), i));
            xVals.add(mStudents.get(i).getName());
        }

        BarDataSet set;

        set = new BarDataSet(yVals, "출석횟수");

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);
        mHorizontalChart.setData(data);
    }

    /**
     * 전체 학생의 출석상태를 출력
     * 0:출석 1:지각 2:결석
     */
    private void pieChartSetData(int attend, int late, int absent, int total) {
        // 입력된 값 이외의 값은 모두 결석으로 강제 처리
        absent += (total - (attend + late + absent));

        // 일 수를 확률 값으로 전환
        float pAttend, pLate, pAbsent;

        pAttend = ((float)attend/(float)total) * 100;
        pLate = ((float)late/(float)total) * 100;
        pAbsent = ((float)absent/(float)total) * 100;

        ArrayList<Entry> yVals = new ArrayList<>();
        yVals.add(new Entry(pAttend, 0));
        yVals.add(new Entry(pLate, 1));
        yVals.add(new Entry(pAbsent, 2));

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("출석");
        xVals.add("지각");
        xVals.add("결석");

        PieDataSet dataSet = new PieDataSet(yVals, null);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        dataSet.setColors(colors);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mPieChart.setData(data);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (e == null)
            return;

        RectF bounds = mHorizontalChart.getBarBounds((BarEntry) e);
        PointF position = mHorizontalChart.getPosition(e, mHorizontalChart.getData().getDataSetByIndex(dataSetIndex)
                .getAxisDependency());

        Log.i("bounds", bounds.toString());
        Log.i("position", position.toString());
    }

    @Override
    public void onNothingSelected() {

    }
}

