package com.douncoding.guaranteedanp_l;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.douncoding.dao.Lesson;
import com.douncoding.dao.Place;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LessonCreateFragment extends Fragment implements View.OnClickListener,
    View.OnFocusChangeListener {
    public static final String TAG = LessonCreateFragment.class.getSimpleName();

    public LessonCreateFragment() { }

    //시스템 참조 변수
    AppContext mApp;
    List<Place> mPlaces;

    //기본정보 관련 변수
    EditText mNameEdit;
    EditText mDescEdit;
    EditText mRoomEdit;
    EditText mPersonEdit;

    //개강 및 종강 관련 변수
    EditText mStartDateEdit;
    EditText mEndDateEdit;
    Calendar mStartDayCalendar;
    Calendar mEndDayCalendar;
    Drawable successDraw;

    //수업시간 관련 변수
    CheckBox[] mWeekCheckBox;
    EditText mStartTimeEdit;
    EditText mEndTimeEdit;
    Button mTimeAddButton;
    RecyclerView mLessonTimesView;
    LinearLayoutManager mTimesLayoutManager;
    LessonTimeAdapter mTimesAdapter;

    // 네트워크 관련 변수
    WebService mWebService;

    public static LessonCreateFragment newInstance() {
        LessonCreateFragment fragment = new LessonCreateFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnListener {
        void moveToCompletedLessonCreate();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lesson_create, container, false);

        mApp = (AppContext)getContext().getApplicationContext();
        mPlaces = mApp.openDBReadable().getPlaceDao().loadAll();

        /**
         * 기본정보 관련 설정
         */
        mNameEdit = (EditText)view.findViewById(R.id.input_name);
        mDescEdit = (EditText)view.findViewById(R.id.input_desc);
        mPersonEdit = (EditText)view.findViewById(R.id.input_personnel);
        mRoomEdit = (EditText)view.findViewById(R.id.input_room);
        mRoomEdit.setKeyListener(null);
        mRoomEdit.setOnClickListener(this);

        /**
         * 개강 종강일 관련 설정
         */
        mStartDateEdit = (EditText)view.findViewById(R.id.input_start_date);
        mStartDateEdit.setOnFocusChangeListener(this);
        mEndDateEdit = (EditText)view.findViewById(R.id.input_end_date);
        mEndDateEdit.setOnFocusChangeListener(this);
        successDraw = getResources().getDrawable(R.drawable.ic_check_black_24dp);
        successDraw.setColorFilter(getResources().getColor(android.R.color.holo_green_dark)
                , PorterDuff.Mode.SRC_ATOP);

        /**
         * 수업시간 관련 설정
         */
        mStartTimeEdit = (EditText)view.findViewById(R.id.input_start_time);
        mEndTimeEdit = (EditText)view.findViewById(R.id.input_end_time);
        mTimeAddButton = (Button)view.findViewById(R.id.input_time_add);
        mTimeAddButton.setOnClickListener(this);

        mStartTimeEdit.setKeyListener(null);
        mStartTimeEdit.setOnClickListener(this);
        mStartTimeEdit.setOnFocusChangeListener(this);
        mEndTimeEdit.setKeyListener(null);
        mEndTimeEdit.setOnClickListener(this);
        mEndTimeEdit.setOnFocusChangeListener(this);

        mLessonTimesView = (RecyclerView)view.findViewById(R.id.input_time_list);
        mTimesAdapter = new LessonTimeAdapter();
        mTimesLayoutManager = new LinearLayoutManager(getContext());
        mLessonTimesView.setAdapter(mTimesAdapter);
        mLessonTimesView.setLayoutManager(mTimesLayoutManager);

        mWeekCheckBox = new CheckBox[7];
        mWeekCheckBox[0] = (CheckBox)view.findViewById(R.id.input_week_sun);
        mWeekCheckBox[1] = (CheckBox)view.findViewById(R.id.input_week_mon);
        mWeekCheckBox[2] = (CheckBox)view.findViewById(R.id.input_week_tues);
        mWeekCheckBox[3] = (CheckBox)view.findViewById(R.id.input_week_wen);
        mWeekCheckBox[4] = (CheckBox)view.findViewById(R.id.input_week_thur);
        mWeekCheckBox[5] = (CheckBox)view.findViewById(R.id.input_week_fri);
        mWeekCheckBox[6] = (CheckBox)view.findViewById(R.id.input_week_sat);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mWebService = retrofit.create(WebService.class);

        mStartDayCalendar = Calendar.getInstance();
        mEndDayCalendar = Calendar.getInstance();
    }

    @Override
    public void onPause() {
        super.onPause();

        mWebService = null;
        mStartDayCalendar = null;
        mEndDayCalendar = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.input_start_time:
                showLessonStartTimePicker();
                break;
            case R.id.input_end_time:
                showLessonEndTimePicker();
                break;
            case R.id.input_start_date:
                showLessonStartDatePicker();
                break;
            case R.id.input_end_date:
                showLessonEndDatePicker();
                break;
            case R.id.input_time_add:
                generateLessonTime();
                break;
            case R.id.input_room:
                showLessonPlaceSelectedDialog();
                break;
        }
    }

    /**
     * EditText 를 버튼처럼 사용하고자 하는 경우 클릭이벤트와 포커스이벤트에
     * 대해 모두 구현해야 한다. 이미 포커스를 가지고 있는 상태에서 값을 변경하기 위해
     * 다시 선택하는 경우와 처음 다른 항목을 선택한 경우를 생각해야한다.
     * 다른 EditText 를 클릭하는 경우 처음은 클릭이벤트가 발생하지 않기 때문이다. 이는
     * 클릭 시 키보드 리스너를 해제하기 떄문에 발생한다.
     * @param v
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus)
            return;

        switch (v.getId()) {
            case R.id.input_start_time:
                showLessonStartTimePicker();
                break;
            case R.id.input_end_time:
                showLessonEndTimePicker();
                break;
            case R.id.input_start_date:
                showLessonStartDatePicker();
                break;
            case R.id.input_end_date:
                showLessonEndDatePicker();
                break;
            case R.id.input_room:
                showLessonPlaceSelectedDialog();
                break;
        }
    }

    private void showLessonStartTimePicker() {
        Calendar c = Calendar.getInstance();

        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mStartTimeEdit.setText(String.format(Locale.getDefault(),
                        "%02d:%02d", hourOfDay, minute));
            }
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void showLessonEndTimePicker() {
        Calendar c = Calendar.getInstance();

        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mEndTimeEdit.setText(String.format(Locale.getDefault(),
                        "%02d:%02d", hourOfDay, minute));
            }
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void showLessonStartDatePicker() {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mStartDayCalendar.set(year, monthOfYear, dayOfMonth);

                mStartDateEdit.setText(new SimpleDateFormat("yyyy년 MM월 dd일 EE요일", Locale.KOREA)
                        .format(mStartDayCalendar.getTime()));

                mStartDateEdit.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, successDraw, null);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showLessonEndDatePicker() {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mEndDayCalendar.set(year, monthOfYear, dayOfMonth);

                mEndDateEdit.setText(new SimpleDateFormat("yyyy년 MM월 dd일 EE요일", Locale.KOREA)
                        .format(mEndDayCalendar.getTime()));

                if (mStartDayCalendar.getTimeInMillis() < mEndDayCalendar.getTimeInMillis()) {
                    mEndDateEdit.setError(null);
                    mEndDateEdit.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, successDraw, null);
                } else {
                    mEndDateEdit.setError("유요하지 못한 값입니다.");
                }
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showLessonPlaceSelectedDialog() {
        String items[] = new String[mPlaces.size()];

        Log.d(TAG, "강의실 조회 항목:" + mPlaces.size());
        for (int i = 0; i < items.length; i++) {
            items[i] = mPlaces.get(i).getName();
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.title("강의실을 선택하세요");
        builder.items(items);
        builder.itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                mRoomEdit.setText(text);
            }
        });
        builder.negativeText(android.R.string.cancel);
        builder.show();
    }

    /**
     * "수업시간 입력카드" 의 주기와 시작/종료시간을 추출하여 목록을 생성
     */
    private void generateLessonTime() {
        List<LessonTime> lessonTimes = new ArrayList<>();
        int pos = 1; // 1 = 월

        String startTime = CommonUtils.getStringAndEmptyErrorHandle(mStartTimeEdit);
        String endTime = CommonUtils.getStringAndEmptyErrorHandle(mEndTimeEdit);
        String startDate = CommonUtils.getStringAndEmptyErrorHandle(mStartDateEdit);
        String endDate = CommonUtils.getStringAndEmptyErrorHandle(mEndDateEdit);

        if (startTime == null || endTime == null
                || startDate == null || endDate == null) {
            return;
        }

        for (CheckBox box : mWeekCheckBox) {
            if (box.isChecked()) {
                lessonTimes.add(new LessonTime(
                        pos,
                        mStartDayCalendar.getTime(),
                        mEndDayCalendar.getTime(),
                        startTime,
                        endTime));
            }
            pos++;
        }

        mTimesAdapter.addItem(lessonTimes);
    }

    /**
     * 강의정보를 서버에 등록 요청
     */
    public void registerLesson() {
        new AsyncTask<Void, String, Void>() {
            Lesson upLesson;
            List<LessonTime> upLessonTime;
            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                upLesson = 강의기본정보추출();

                upLessonTime = mTimesAdapter.get();

                dialog = new ProgressDialog(getContext());
                dialog.setTitle("강의 개설 중...");
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Lesson respLesson = mWebService
                            .uploadLesson(upLesson)
                            .execute().body();

                    Log.d(TAG, "CHECK:" + new Gson().toJson(respLesson));
                    publishProgress("lesson", new Gson().toJson(respLesson));

                    List<LessonTime> respTimes = mWebService
                            .uploadLessonTime(upLessonTime, upLesson.getName())
                            .execute().body();

                    Log.d(TAG, "CHECK:" + new Gson().toJson(respTimes));
                    publishProgress("lessontimes");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                if (values[0].equals("lesson")) {
                    dialog.setMessage("강의 생성 중");
                } else if (values[0].equals("lessontimes")) {
                    dialog.setMessage("강의시간 생성 중");
                }
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dialog.dismiss();

                Toast.makeText(getContext()
                        , "강의생성 완료"
                        , Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }.execute();
    }

    private Lesson 강의기본정보추출() {
        Lesson mLesson = new Lesson();

        // 강의기본정보 이력
        String lname = mNameEdit.getText().toString();
        String ldesc = mDescEdit.getText().toString();
        String lperson = mPersonEdit.getText().toString();
        String pname = mRoomEdit.getText().toString();

        if (TextUtils.isEmpty(lname) || lname.length() > 20) {
            mNameEdit.setError("");
        } else {
            mNameEdit.setError(null);
            mLesson.setName(lname);
        }

        if (TextUtils.isEmpty(ldesc) || ldesc.length() > 200) {
            mDescEdit.setError("");
        } else {
            mDescEdit.setError(null);
            mLesson.setDesc(ldesc);
        }

        if (TextUtils.isEmpty(lperson)) {
            mPersonEdit.setError("");
        } else {
            mPersonEdit.setError(null);
            mLesson.setPersonnel(Integer.valueOf(lperson));
        }

        if (TextUtils.isEmpty(pname)) {
            mRoomEdit.setError("");
        } else {
            mRoomEdit.setError(null);
            for (Place place : mPlaces) {
                if (place.getName().equals(pname)) {
                    mLesson.setPid(place.getId());
                    break;
                }
            }
        }

        mLesson.setIid(mApp.내정보.얻기().getId());
        return mLesson;
    }

    /*
     * 날짜범위의 포함된 요일 항목을 추출하는 로직 추후에 필요한경우 참조해서 사용
     */
    private void 주단위강의시간추출() {
        // 강사가 입력한 주단위 시간 추출
        List<LessonTime> days = mTimesAdapter.get();

        // 시작일 ~ 종요일의 요일로 표현
        while (mStartDayCalendar.getTimeInMillis() <= mEndDayCalendar.getTimeInMillis()) {
            mStartDayCalendar.add(Calendar.DATE, 1);
            Log.e(TAG, "요일:" + mStartDayCalendar.get(Calendar.DAY_OF_WEEK));

            for (LessonTime day : days) {
                if (mStartDayCalendar.get(Calendar.DAY_OF_WEEK) == day.getDay()) {
                    Log.e(TAG, "해당하는 요일입니다.");
                    // 해당하는 날짜를 추출하여 전송값에 포함
                }
            }
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

            holder.mWeekText.setText(item.getDayString());
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
