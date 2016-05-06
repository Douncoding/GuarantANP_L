package com.douncoding.guaranteedanp_l;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.douncoding.dao.Lesson;
import com.douncoding.dao.LessonTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class LessonBasicInfoFragment extends Fragment {
    public static final String TAG = LessonBasicInfoFragment.class.getSimpleName();

    public static final String EXTRA_PARAM1 = "param1";

    Lesson mLesson;
    AppContext mApp;

    EditText mNameEdit;
    EditText mDescEdit;
    EditText mRoomEdit;
    EditText mPersonEdit;
    EditText mStartDateEdit;
    EditText mEndDateEdit;

    RecyclerView mLessonTimesView;
    LinearLayoutManager mTimesLayoutManager;
    LessonTimeAdapter mTimesAdapter;

    public LessonBasicInfoFragment() { }

    public static LessonBasicInfoFragment newInstance(int lessonId) {
        LessonBasicInfoFragment fragment = new LessonBasicInfoFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_PARAM1, lessonId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int lessonId = getArguments().getInt(EXTRA_PARAM1);
            mLesson = mApp.openDBReadable().getLessonDao().load((long)lessonId);
            Log.i(TAG, "강의정보 읽기 완료: 식별자:" + mLesson.getId());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mApp = (AppContext)context.getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lesson_basic_info, container, false);

        mNameEdit = (EditText) view.findViewById(R.id.input_name);
        mDescEdit = (EditText) view.findViewById(R.id.input_desc);
        mPersonEdit = (EditText) view.findViewById(R.id.input_personnel);
        mRoomEdit = (EditText) view.findViewById(R.id.input_room);
        mStartDateEdit = (EditText) view.findViewById(R.id.input_start_date);
        mEndDateEdit = (EditText) view.findViewById(R.id.input_end_date);

        mLessonTimesView = (RecyclerView)view.findViewById(R.id.input_time_list);
        mTimesAdapter = new LessonTimeAdapter();
        mTimesLayoutManager = new LinearLayoutManager(getContext());
        mLessonTimesView.setAdapter(mTimesAdapter);
        mLessonTimesView.setLayoutManager(mTimesLayoutManager);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mTimesAdapter = null;
        mTimesLayoutManager = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        String roomName = mApp.openDBReadable().getPlaceDao().load(mLesson.getPid()).getName();
        mRoomEdit.setText(roomName);

        mNameEdit.setText(mLesson.getName());
        mDescEdit.setText(mLesson.getDesc());
        mPersonEdit.setText(String.valueOf(mLesson.getPersonnel()));

        List<LessonTime> lessonTimes = mLesson.getLessonTimeList();
        if (lessonTimes.size() != 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Locale.KOREA);
            mStartDateEdit.setText(format.format(lessonTimes.get(0).getStartDate()));
            mEndDateEdit.setText(format.format(lessonTimes.get(0).getEndDate()));
            mTimesAdapter.addItem(lessonTimes);
        }
    }

    class LessonTimeAdapter extends RecyclerView.Adapter<LessonTimeAdapter.ViewHolder> {
        ArrayList<LessonTime> dataset = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_lesson_time, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LessonTime item = dataset.get(position);

            holder.mWeekText.setText(CommonUtils.dayOfString(item.getDay()));
            holder.mStartTimeText.setText(item.getStartTime());
            holder.mEndTimeText.setText(item.getEndTime());
        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }

        public boolean addItem(List<LessonTime> items) {
            if (items == null) {
                Log.w(TAG, "갱신하고자 하는 항목이 NULL 상태: ");
                return false;
            }

            dataset.addAll(items);
            notifyDataSetChanged();
            return true;
        }

        public List<LessonTime> get() {
            return dataset;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView mWeekText;
            TextView mStartTimeText;
            TextView mEndTimeText;

            public ViewHolder(View itemView) {
                super(itemView);

                mStartTimeText = (TextView)itemView.findViewById(R.id.item_start_time);
                mEndTimeText = (TextView)itemView.findViewById(R.id.item_end_time);
                mWeekText = (TextView)itemView.findViewById(R.id.item_week);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

            }
        }
    }
}
