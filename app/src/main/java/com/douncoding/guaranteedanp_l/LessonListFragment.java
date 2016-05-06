package com.douncoding.guaranteedanp_l;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.douncoding.dao.*;
import com.douncoding.dao.LessonTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class LessonListFragment extends Fragment {
    public static final String TAG = LessonListFragment.class.getSimpleName();

    private OnListener mListener;

    RecyclerView mLessonListView;
    LinearLayoutManager mLayoutManager;
    LessonListAdapter mAdapter;
    ArrayList<Lesson> mTodayLessons = new ArrayList<>();

    AppContext mApp;
    PlaceDao mPlaceDao;

    public LessonListFragment() { }

    public static LessonListFragment newInstance() {
        LessonListFragment fragment = new LessonListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlaceDao = mApp.openDBReadable().getPlaceDao();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lesson_list, container, false);

        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new LessonListAdapter();

        mLessonListView = (RecyclerView)view.findViewById(R.id.lesson_list);
        mLessonListView.setLayoutManager(mLayoutManager);
        mLessonListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListener) {
            mListener = (OnListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ");
        }

        mApp = (AppContext)context.getApplicationContext();
        if (mApp == null) {
            throw new RuntimeException("ApplicationContext is null..");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListener {
        void onNavigateToDetailView(int lessonId);
    }

    @Override
    public void onResume() {
        super.onResume();

        List<Lesson> lessons = loadOwnLessons();
        for (Lesson lesson : lessons) {
            for (LessonTime time: lesson.getLessonTimeList()) {
                if (isValidateLessonTime(time)) {
                    mTodayLessons.add(lesson);
                }
            }
        }

        mAdapter.addItem(lessons);
    }

    private List<Lesson> loadOwnLessons() {
        Instructor own = mApp.내정보.얻기();

        List<Lesson> ownLessons = new ArrayList<>();
        List<Lesson> allLessons = mApp.openDBReadable().getLessonDao().loadAll();

        for (Lesson lesson : allLessons) {
            if (lesson.getIid() == own.getId()) {
                ownLessons.add(lesson);
            }
        }

        return ownLessons;
    }

    /**
     * 오늘 강의 강의 인지확인
     * @param time 확인을 원하는 강의시간
     * @return 포함여부
     */
    private boolean isValidateLessonTime(com.douncoding.dao.LessonTime time) {
        Calendar currentDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        String[] startHourAndMin = time.getStartTime().split(":");
        String[] endHourAndMin = time.getEndTime().split(":");

        startDate.setTime(time.getStartDate());
        endDate.setTime(time.getEndDate());

        // 사이 날짜인지 확인
        if (currentDate.getTimeInMillis() < startDate.getTimeInMillis() ||
                currentDate.getTimeInMillis() > endDate.getTimeInMillis()) {
            Log.i(TAG, "포함되지 않는 날짜: " +
                    String.format(Locale.getDefault(), "시작:%d 현재:%d 종료:%d",
                            startDate.getTimeInMillis(),
                            currentDate.getTimeInMillis(),
                            endDate.getTimeInMillis()));
            return false;
        }

        // 요일이 같은지 확인
        if (currentDate.get(Calendar.DAY_OF_WEEK) != time.getDay()) {
            Log.i(TAG, "현재 요일:" + currentDate.get(Calendar.DAY_OF_WEEK) +
                    " 과목 요일:" + time.getDay());
            return false;
        }
        return true;
    }

    class LessonListAdapter extends RecyclerView.Adapter<LessonListAdapter.ViewHolder> {

        ArrayList<Lesson> mDataset = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_lesson, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Lesson item = mDataset.get(position);

            boolean todayLesson = false;

            for (Lesson lesson : mTodayLessons) {
                if (lesson.getId().equals(item.getId())) {
                    todayLesson = true;
                    break;
                }
            }

            // 오늘 강의의 경우는 출석화면 현시
            if (todayLesson) {
                holder.mContainerView1.setVisibility(View.VISIBLE);
                holder.mContainerView2.setVisibility(View.GONE);
            } else {
                holder.mContainerView2.setVisibility(View.VISIBLE);
                holder.mContainerView1.setVisibility(View.GONE);
            }

            StringBuilder builder = new StringBuilder();
            for (LessonTime time : item.getLessonTimeList()) {
                builder.append(CommonUtils.dayOfString(time.getDay()));
                builder.append(" ");
            }

            holder.mNameText.setText(item.getName());
            holder.mPersonnelText.setText(String.valueOf(item.getPersonnel()));
            holder.mRoomText.setText(mPlaceDao.load(item.getPid()).getName());
            holder.mSubText.setText(builder.toString());
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public void addItem(List<Lesson> lessons) {
            mDataset.addAll(lessons);
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {

            TextView mNameText;
            TextView mSubText;
            EditText mRoomText;
            EditText mPersonnelText;
            TextView mATCountText;
            TextView mASCountText;
            LinearLayout mContainerView1;
            LinearLayout mContainerView2;

            public ViewHolder(View itemView) {
                super(itemView);

                mNameText = (TextView)itemView.findViewById(R.id.name_text);
                mSubText = (TextView)itemView.findViewById(R.id.subtitle_text);
                mRoomText = (EditText)itemView.findViewById(R.id.place_text);
                mPersonnelText = (EditText)itemView.findViewById(R.id.personnel_text);

                mATCountText = (TextView)itemView.findViewById(R.id.attend_count);
                mASCountText = (TextView)itemView.findViewById(R.id.absent_count);
                mContainerView1 = (LinearLayout)itemView.findViewById(R.id.tody_container);
                mContainerView2 = (LinearLayout)itemView.findViewById(R.id.not_toady_container);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int lessonId = mDataset.get(getPosition()).getId().intValue();
                    mListener.onNavigateToDetailView(lessonId);
                }
            }
        }
    }
}
