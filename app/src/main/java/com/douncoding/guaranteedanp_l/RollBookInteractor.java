package com.douncoding.guaranteedanp_l;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.douncoding.dao.*;
import com.douncoding.dao.LessonTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 출석과 관련된 로직처리를 수행
 * {@link Lesson} (강의)의 종속적인 로직 클래스
 */
public class RollBookInteractor {
    public static final String TAG = RollBookInteractor.class.getSimpleName();

    // 메인 자원
    AppContext mApp;
    WebService mWebService;

    // 주요 변수
    Lesson mLesson; // 강의
    HashMap<Student, List<Attendance>> mRollBook; // 출석부

    // 웹 자원 읽기 성공 여부
    boolean isLoad = false;
    OnCallback onCallback;

    public enum AttendState {
        ATTEND(1), LATE(2), ABSENT(3);
        int value;
        AttendState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public RollBookInteractor(@NonNull AppContext aApp, Lesson aLesson) {
        this.mApp = aApp;
        this.mWebService = aApp.getWebServiceInstance();
        this.mLesson = aLesson;
        this.mRollBook = new HashMap<>();

        new ReferWebResource().execute();
    }

    public void reload() {
        isLoad = false;
        new ReferWebResource().execute();
    }

    public interface OnCallback {
        void onReload(Lesson aLesson); // 웹 자원 읽기 완료 후 호출되는 콜백
    }

    public void setOnCallback(OnCallback aCallback) {
        this.onCallback = aCallback;
    }

    /**
     * 수강생 목록 조회
     */
    public List<Student> getStudents() {
        List<Student> students = new ArrayList<>();

        if (notYetWebservice()) return null;

        for (Map.Entry<Student, List<Attendance>> entry : mRollBook.entrySet()) {
            students.add(entry.getKey());
        }

        Log.v(TAG, String.format("%s 의 수강생의 수는 %d 입니다.",
                mLesson.getName(), students.size()));
        return students;
    }

    /**
     * 100% 출석률을 위해 채워야할 오늘까지의 총 수업일 수
     */
    public int getTotalDays() {
        //  수강생 수 * 오늘까지 수업일 수
        return getTotalDayOfLessonUntilToday() * getStudents().size();
    }

    /**
     * 오늘 까지의 총 수업일 수룰 구한다.
     */
    public int getTotalDayOfLessonUntilToday() {
        int totalDays = 0;

        if (notYetWebservice()) return -1;

        List<LessonTime> lessonTimeList = mLesson.getLessonTimeList();
        if (lessonTimeList == null || lessonTimeList.size() == 0) {
            return totalDays;
        }

        LessonTime standardTime = lessonTimeList.get(0);

        Calendar sc = Calendar.getInstance();
        sc.setTime(standardTime.getStartDate());

        Calendar ec = Calendar.getInstance();
        ec.setTime(standardTime.getEndDate());

        Calendar tc = Calendar.getInstance();
        if (ec.getTimeInMillis() < tc.getTimeInMillis()) {
            Log.w(TAG, "종료일이 이미 경과한 요청입니다. " +
                    "종료일 까지의 수업일 수로 대체합니다.");
            tc = ec;
        }

        // 시작일 ~ 오늘
        while (sc.getTimeInMillis() <= tc.getTimeInMillis()) {
            sc.add(Calendar.DATE, 1);
            for (com.douncoding.dao.LessonTime day : lessonTimeList) {
                if (sc.get(Calendar.DAY_OF_WEEK) == day.getDay()) {
                    totalDays++;
                }
            }
        }

        return totalDays;
    }

    /**
     * 수강생 별 출석상태 조회
     */
    public int getTimesState(Student student, AttendState state) {
        int targetCount = 0;
        if (notYetWebservice()) return -1;

        for (Attendance item : mRollBook.get(student)) {
            if (state.getValue() == item.getState()) {
                targetCount++;
            }
        }


        Log.d(TAG, String.format("학생 출석조회: 강의:%s 학생:%s 상태:%d 횟수:%d",
                mLesson.getName(), student.getName(), state.getValue(), targetCount));
        return targetCount;
    }

    /**
     * 오늘 강의 출석상태 조회
     */
    public int getTodayTimesState(AttendState state) {
        int todayTimes = 0;

        if (notYetWebservice()) return -1;

        for (Student student : getStudents()) {
            for (Attendance item : mRollBook.get(student)) {
                Calendar tc = Calendar.getInstance();
                Calendar ec = Calendar.getInstance();
                ec.setTime(item.getEnterTime());

                if (tc.get(Calendar.DAY_OF_MONTH) == ec.get(Calendar.DAY_OF_MONTH)) {
                    if (state.getValue() == item.getState()) {
                        todayTimes++;
                    }
                }
            }
        }
        return todayTimes;
    }

    /**
     * 전 학생의 출석상태 조회
     */
    public int getAllTimesState(AttendState state) {
        int targetCount = 0;
        if (notYetWebservice()) return -1;

        for (Student student : getStudents()) {
            for (Attendance item : mRollBook.get(student)) {
                if (state.getValue() == item.getState()) {
                    targetCount++;
                }
            }
        }

        Log.d(TAG, String.format("전체 출석조회: 강의:%s 상태:%d 횟수:%d",
                mLesson.getName(), state.getValue(), targetCount));
        return targetCount;
    }


    private boolean notYetWebservice() {
        if (isLoad) {
            return false;
        } else {
            Log.w(TAG, "아직 로딩작업이 완료되지 못했습니다.");
            return true;
        }
    }

    /**
     * 웹 서버의 자원 참조 (모든 출석목록 긇어오기)
     */
    class ReferWebResource extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            int lessonId = mLesson.getId().intValue();

            try {
                // 수강생 읽기
                List<Student> studentList =
                    mWebService.getStudentsOfLesson(lessonId).execute().body();

                // 출석 읽기
                for (Student student : studentList) {
                    int studentId = student.getId().intValue();
                    mRollBook.put(student, mWebService.getAttendancesOfStudent(studentId, lessonId).execute().body());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isLoad = true;

            if (onCallback != null)
                onCallback.onReload(mLesson);
        }
    }
}
