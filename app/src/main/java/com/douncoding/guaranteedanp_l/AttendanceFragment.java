package com.douncoding.guaranteedanp_l;

import android.content.Context;
import android.graphics.Color;
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
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.github.mikephil.charting.utils.ColorTemplate;

/**
 * 학생 별 출석 일 수 (Horizontal-BarChart)
 * 전체 출석비율 (PieChart)
 */
public class AttendanceFragment extends Fragment {
    public static final String TAG = AttendanceFragment.class.getSimpleName();

    public static final String EXTRA_PARAM1 = "param1";

    public AttendanceFragment() {}

    /**
     * 주요 자원
     */
    AppContext mApp;
    WebService mWebService;
    RollBookInteractor mRollBookInteractor;
    Lesson mLesson;

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
        }

        mWebService = mApp.getWebServiceInstance();

        mRollBookInteractor = new RollBookInteractor(mApp, mLesson);
        mRollBookInteractor.setOnCallback(new RollBookInteractor.OnCallback() {
            @Override
            public void onReload(Lesson aLesson) {
                horizontalChartSetData();
                mHorizontalChart.invalidate();
                mHorizontalChart.animateY(3000);
            }
        });
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

                pieChartSetData(
                        mRollBookInteractor.getAllTimesState(RollBookInteractor.AttendState.ATTEND),
                        mRollBookInteractor.getAllTimesState(RollBookInteractor.AttendState.LATE),
                        mRollBookInteractor.getAllTimesState(RollBookInteractor.AttendState.ABSENT),
                        mRollBookInteractor.getTotalDays());
                mPieChart.invalidate();
                mPieChart.animateY(3000, Easing.EasingOption.EaseInOutQuad);
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 학생 별 출석샅태를 출력
     */
    private void horizontalChartSetData() {
        ArrayList<BarEntry> yVals = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        List<Student> students = mRollBookInteractor.getStudents();

        for (int i = 0; i < students.size(); i++) {
            int count
                    = mRollBookInteractor.getTimesState(students.get(i),
                    RollBookInteractor.AttendState.ATTEND)
                    + mRollBookInteractor.getTimesState(students.get(i),
                    RollBookInteractor.AttendState.LATE);

            yVals.add(new BarEntry((float)count, i));
            xVals.add(students.get(i).getName());
        }

        if (xVals.size() == 0)
            return;

        BarDataSet set = new BarDataSet(yVals, "출석횟수");

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);
        mHorizontalChart.setData(data);
    }

    /**
     * 전체 학생의 출석상태를 출력
     */
    private void pieChartSetData(int attend, int late, int absent, int total) {
        // 입력된 값 이외의 값은 모두 결석으로 강제 처리
        absent += (total - (attend + late + absent));

        if (total == 0)
            return;

        Log.i(TAG, String.format(Locale.getDefault(),"출석:%d 지각:%d 결석:%d 종합:%d (일)",
                attend, late, absent, total));

        // 일 수를 확률 값으로 전환
        float pAttend, pLate, pAbsent;

        pAttend = ((float)attend/(float)total) * 100;
        pLate = ((float)late/(float)total) * 100;
        pAbsent = ((float)absent/(float)total) * 100;

        Log.i(TAG, String.format(Locale.getDefault(),"출석:%f 지각:%f 결석:%f (확)",
                pAttend, pLate, pAbsent));

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
}

