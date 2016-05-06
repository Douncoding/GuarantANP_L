package com.douncoding.guaranteedanp_l;

import android.content.Context;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.douncoding.dao.*;
import com.douncoding.dao.LessonTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LessonListFragment extends Fragment {
    public static final String TAG = LessonListFragment.class.getSimpleName();

    private OnListener mListener;

    RecyclerView mLessonListView;
    LinearLayoutManager mLayoutManager;
    LessonListAdapter mAdapter;

    // 오늘의 강의목록
    //ArrayList<Lesson> mTodayLessons = new ArrayList<>();
    HashMap<Lesson, Integer> mTodayLessons;

    AppContext mApp;
    WebService mWebService;

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

        mWebService = mApp.getWebServiceInstance();

        mTodayLessons = new HashMap<>();
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
        // 오늘의 강의목록 구성
        for (Lesson lesson : lessons) {
            for (LessonTime time: lesson.getLessonTimeList()) {
                if (isValidateLessonTime(time)) {
                    //mTodayLessons.add(lesson);
                    mTodayLessons.put(lesson, 0);
                }
            }
        }

        loadData();

        mAdapter.addItem(lessons);
    }

    private void loadData() {
        new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (Map.Entry<Lesson, Integer> entry : mTodayLessons.entrySet()) {
                    Lesson lesson = entry.getKey();

                    int lessonId = lesson.getId().intValue();
                    int todayAttendCount = 0;
                    try {
                        // 수강생 목록 구함
                        List<Student> mStudents =
                                mWebService.getStudentsOfLesson(lessonId).execute().body();

                        // 수강생별 출석부를 구함
                        for (Student student : mStudents) {
                            int studentId = student.getId().intValue();

                            // 출석현황
                            List<Attendance> attendances = mWebService.getAttendancesOfStudent(
                                    studentId, lessonId).execute().body();

                            // 오늘 날짜에 해당하는 출석기록만 구함
                            for (Attendance item: attendances) {
                                Calendar tc = Calendar.getInstance();
                                Calendar ec = Calendar.getInstance();
                                ec.setTime(item.getEnterTime());

                                if (tc.get(Calendar.DAY_OF_MONTH) == ec.get(Calendar.DAY_OF_MONTH)) {
                                    // 지각 또는 출석
                                    if (item.getState() != 0) {
                                        todayAttendCount++;
                                    }
                                }
                            }
                        } // for

                        Log.i(TAG, lesson.getName() +" 출석인원:" + todayAttendCount);
                        mTodayLessons.put(lesson, todayAttendCount);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } // for
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
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

        startDate.setTime(time.getStartDate());
        endDate.setTime(time.getEndDate());

        // 사이 날짜인지 확인
        if (currentDate.getTimeInMillis() < startDate.getTimeInMillis() ||
                currentDate.getTimeInMillis() > endDate.getTimeInMillis()) {
            Log.v(TAG, "포함되지 않는 날짜: " +
                    String.format(Locale.getDefault(), "시작:%d 현재:%d 종료:%d",
                            startDate.getTimeInMillis(),
                            currentDate.getTimeInMillis(),
                            endDate.getTimeInMillis()));
            return false;
        }

        // 요일이 같은지 확인
        if (currentDate.get(Calendar.DAY_OF_WEEK) != time.getDay()) {
            Log.v(TAG, "현재 요일:" + currentDate.get(Calendar.DAY_OF_WEEK) +
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
            int attendCount = 0;

            //for (Lesson lesson : mTodayLessons) {
            for (Map.Entry<Lesson, Integer> entry : mTodayLessons.entrySet()) {
                Lesson lesson = entry.getKey();
                attendCount = entry.getValue();
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

            // 기본정보
            holder.mNameText.setText(item.getName());
            holder.mPersonnelText.setText(String.valueOf(item.getPersonnel()));
            holder.mRoomText.setText(mPlaceDao.load(item.getPid()).getName());
            holder.mSubText.setText(builder.toString());

            // 출석정보
            holder.mASCountText.setText(String.valueOf(item.getPersonnel() - attendCount));
            holder.mATCountText.setText(String.valueOf(attendCount));
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
            TextView mDeleteAction;
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

                mDeleteAction = (TextView)itemView.findViewById(R.id.delete_action);
                mDeleteAction.setOnClickListener(this);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                final int lessonId = mDataset.get(getPosition()).getId().intValue();

                if (v.getId() == R.id.delete_action) {
                    mWebService.deleteLesson(lessonId).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Log.d(TAG, "삭제결과:" + response.code());
                            mApp.openDBWritable().getLessonDao().deleteByKey((long)lessonId);

                            mDataset.remove(getPosition());
                            notifyDataSetChanged();

                            Toast.makeText(getContext(),
                                    "삭제완료",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });
                } else {
                    if (mListener != null) {
                        mListener.onNavigateToDetailView(lessonId);
                    }
                }
            }
        }
    }
}
