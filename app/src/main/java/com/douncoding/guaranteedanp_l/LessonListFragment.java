package com.douncoding.guaranteedanp_l;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.douncoding.dao.*;
import com.douncoding.dao.LessonTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class LessonListFragment extends Fragment {
    public static final String TAG = LessonListFragment.class.getSimpleName();

    // 최상위 자원
    AppContext mApp;
    // 데이터베이스 처리
    PrincipalInteractor mPrincipal;
    // 강의별 출석부 (현황)
    HashMap<Lesson, RollBookInteractor> mRollBookPerLesson;
    // 로딩이 완료된 출석부의 개수
    int mLoadRollBookCount = 0;

    // UI
    RecyclerView mLessonListView;
    LinearLayoutManager mLayoutManager;
    LessonListAdapter mAdapter;

    public LessonListFragment() { }

    public static LessonListFragment newInstance() {
        LessonListFragment fragment = new LessonListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    OnListener onListener;
    public interface OnListener {
        void onNavigateToDetailView(int lessonId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListener) {
            onListener = (OnListener) context;
        }

        mApp = (AppContext)context.getApplicationContext();

        // 강의 처리
        mPrincipal = new PrincipalInteractor(mApp);

        // 강의별 출석 처리
        mRollBookPerLesson = new HashMap<>();

        // 출석부 생성
        load();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onListener = null;
        mRollBookPerLesson = null;
        mPrincipal = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    /**
     * 초기화 로직
     * 화면구성와 데이터 로딩이 완료된 이후 출력할 결과
     */
    private void onInit() {
        // 오늘의 강의 먼저 출력
        for (Map.Entry<Lesson, RollBookInteractor> entry : mRollBookPerLesson.entrySet()) {
            if (entry.getValue() != null)
                mAdapter.addItem(entry.getKey());
        }

        for (Map.Entry<Lesson, RollBookInteractor> entry : mRollBookPerLesson.entrySet()) {
            if (entry.getValue() == null)
                mAdapter.addItem(entry.getKey());
        }
    }

    /**
     * 출석부 동기화
     */
    private void load() {
        // 내 강의목록
        final List<Lesson> ownLessons = mPrincipal.getOwnLessonList();
        // 오늘 내 강의목록
        final List<Lesson> todayLessons = mPrincipal.getTodayLessonList(ownLessons);

        for (Lesson lesson : ownLessons) {
            if (todayLessons.contains(lesson)) {
                RollBookInteractor rollBook = new RollBookInteractor(mApp, lesson);
                rollBook.setOnCallback(new RollBookInteractor.OnCallback() {
                    @Override
                    public void onReload(Lesson aLesson) {
                        mLoadRollBookCount++;
                        if (mLoadRollBookCount == todayLessons.size()) {
                            onInit();
                        }
                    }
                });
                // 오늘강의 목록만 출석부를 생성
                mRollBookPerLesson.put(lesson, rollBook);
            } else {
                mRollBookPerLesson.put(lesson, null);
            }

            Log.d(TAG, lesson.getName());
        }
    }

    class LessonListAdapter extends RecyclerView.Adapter<LessonListAdapter.ViewHolder> {
        ArrayList<Lesson> mDataSet = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_lesson, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Lesson item = mDataSet.get(position);

            StringBuilder builder = new StringBuilder();
            for (LessonTime time : item.getLessonTimeList()) {
                builder.append(CommonUtils.dayOfString(time.getDay()));
                builder.append(" ");
            }

            // 기본정보
            holder.mNameText.setText(item.getName());
            holder.mRoomText.setText(mPrincipal.getNameLessonRoom(item.getPid()));
            holder.mSubText.setText(builder.toString());
            holder.mPersonnelText.setText(String.valueOf(item.getPersonnel()));

            // 오늘 강의의 경우는 출석화면 현시
            if (mRollBookPerLesson.get(item) != null) {
                holder.mContainerView1.setVisibility(View.VISIBLE);
                holder.mContainerView2.setVisibility(View.GONE);

                // 출석정보 (지각 + 출석)
                List<Student> students = mRollBookPerLesson.get(item).getStudents();
                if (students != null) {
                    int total = students.size();
                    int attendCount
                            = mRollBookPerLesson.get(item).getTodayTimesState(RollBookInteractor.AttendState.ATTEND)
                            + mRollBookPerLesson.get(item).getTodayTimesState(RollBookInteractor.AttendState.LATE);
                    holder.mASCountText.setText(String.valueOf(total - attendCount));
                    holder.mATCountText.setText(String.valueOf(attendCount));
                    holder.mPersonnelText.setText(String.valueOf(total));
                }
            } else {
                holder.mContainerView2.setVisibility(View.VISIBLE);
                holder.mContainerView1.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mDataSet.size();
        }

        public void addItem(Lesson lessons) {
            mDataSet.add(lessons);
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
                final int lessonId = mDataSet.get(getPosition()).getId().intValue();

                if (v.getId() == R.id.delete_action) {
                    mPrincipal.deleteLesson(lessonId);
                    mDataSet.remove(getPosition());
                    notifyDataSetChanged();

                    Toast.makeText(getContext(),
                            "삭제완료",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (onListener != null) {
                        onListener.onNavigateToDetailView(lessonId);
                    }
                }
            }
        }
    }
}
