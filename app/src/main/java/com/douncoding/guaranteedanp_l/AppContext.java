package com.douncoding.guaranteedanp_l;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.douncoding.dao.*;
import com.douncoding.dao.LessonTime;

import java.util.Calendar;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 강사정보는 변경사항이 적음으로
 */
public class AppContext extends Application {
    public static final String TAG = AppContext.class.getSimpleName();

    // 데이터베이스 인터페이스
    DaoMaster.DevOpenHelper mHelper;

    MyAccount 내정보;

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup Database Manager
        mHelper = new DaoMaster.DevOpenHelper(this, Constants.DATABASE_NAME, null);

        내정보 = new MyAccount();
    }

    public DaoSession openDBReadable() {
        SQLiteDatabase database = mHelper.getReadableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        return daoMaster.newSession();
    }

    public DaoSession openDBWritable() {
        SQLiteDatabase database = mHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        return daoMaster.newSession();
    }

    public WebService getWebServiceInstance() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(WebService.class);
    }


    /**
     * 로그인 상태를 관리한다. 웹 서버와 세션처리가 없는 구조임에 따라
     * 내부적인 로그인 상태와 로그인한 사용자의 정보를 관리하기 위한 목적만
     * 가진다. (로그인 시점에 생성되며, 어플 종료시점에 반환된다.)
     */
    class MyAccount {
        SharedPreferences preferences;

        public MyAccount() {
            preferences = getSharedPreferences(
                    Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        }

        public Instructor 얻기() {
            Instructor instructor = new Instructor();
            instructor.setId(preferences.getLong("id", 0));
            instructor.setName(preferences.getString("name", null));
            instructor.setEmail(preferences.getString("email", null));
            instructor.setPhone(preferences.getString("phone", null));

            return instructor;
        }

        public boolean 로그인() {
            Instructor instructor = 얻기();

            if (instructor.getId() != 0) {
                return true;
            } else {
                return false;
            }
        }

        public void 저장(Instructor instructor) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("id", instructor.getId());
            editor.putString("name", instructor.getName());
            editor.putString("jobs", instructor.getJobs());
            editor.putString("email", instructor.getEmail());
            editor.putString("phone", instructor.getPhone());
            editor.apply();

            Log.i(TAG, "내정보 저장:");
        }

        public void 삭제() {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("id", 0);
            editor.putString("name", null);
            editor.putString("jobs", null);
            editor.putString("email", null);
            editor.putString("phone", null);
            editor.apply();

            Log.i(TAG, "내정보 삭제:");
        }
    }
}
