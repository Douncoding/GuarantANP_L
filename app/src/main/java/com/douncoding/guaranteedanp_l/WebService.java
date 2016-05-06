package com.douncoding.guaranteedanp_l;

import com.douncoding.dao.Instructor;
import com.douncoding.dao.Lesson;
import com.douncoding.dao.Place;
import com.douncoding.dao.Student;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WebService {
    @GET("/lessons")
    Call<List<Lesson>> loadLessons();

    /**
     * 강사번호를 통해 강사정보를 조회
     * @param id 강사번호
     * @return 강사정보
     */
    @GET("/instructors/{id}")
    Call<Instructor> loadInstructor(@Path("id") int id);

    /**
     * 등록된 모든 강사정보를 조회
     * @return 강사목록
     */
    @GET("/instructors/all")
    Call<List<Instructor>> loadAllInstructors();

    /**
     * 등록된 모든 장소목록
     * @return 강의실 목록
     */
    @GET("/place")
    Call<List<Place>> loadAllPlaces();

    /**
     * 등록된 모든 강의목록
     * @return 강의목록
     */
    @GET("/lessons")
    Call<List<Lesson>> loadAllLessons();

    /**
     * 학생 로그인
     * @param email 가입한 주소
     * @param password 비밀번호
     * @return 로그인한 학생정보
     */
    @GET("/instructors/{email}/login")
    Call<Instructor> login(
            @Path("email") String email,
            @Query("password") String password);

    /**
     * 강의생성
     * @param lesson 강의정보
     * @return 생성된 강의 인슽천스
     */
    @POST("/lessons/new")
    Call<Lesson> uploadLesson(@Body Lesson lesson);

    /**
     * 강의삭제
     * @param lessonId
     * @return
     */
    @DELETE("/lessons/{id}")
    Call<ResponseBody> deleteLesson(@Path("id") int lessonId);

    /**
     * 강의시간 업로드
     */
    @POST("/lessons/times/{lessonName}")
    Call<List<LessonTime>> uploadLessonTime(
            @Body List<LessonTime> lessonTimes,
            @Path("lessonName") String lessonName);

    /**
     * 강의시간 로딩
     */
    @GET("/lessons/times")
    Call<List<com.douncoding.dao.LessonTime>> getLessonTimes(@Query("lessonName") String lessonName);

    /**
     * 요청한 강의를 수강하는 학생 목록
     * @param lessonId 강의번호
     * @return 학생목록
     */
    @GET("/enrollments/lesson/{lid}")
    Call<List<Student>> getStudentsOfLesson(@Path("lid") int lessonId);

    /**
     * 학생 출석목록 조회
     */
    @GET("/attendances/student/{sid}/lesson/{lid}")
    Call<List<Attendance>> getAttendancesOfStudent(
            @Path("sid") int studentId,
            @Path("lid") int lessonId);

    /**
     * 강의일자의 출석현황
     * @param lessonTimeId 강의일자 (강의번호가 꼭 포함되어야 함)
     * @return
     */
    @GET("/attendances/lessontime/{ltid}")
    Call<List<Integer>> getAttendDaysOfLessonTime(@Path("ltid") int lessonTimeId);
}
