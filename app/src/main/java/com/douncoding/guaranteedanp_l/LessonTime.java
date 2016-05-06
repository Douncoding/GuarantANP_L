package com.douncoding.guaranteedanp_l;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 강의시간 또는 수업일자 DTO (예. 2016-4-30 20:00 ~ 12:00)
 */
public class LessonTime {

    int id;         // 강의시간 번호
    int lid;        // 강의번호
    int day;        // 요일
    String startDate; // 개강일
    String endDate;   // 종강일
    String startTime;   // 시작시간
    String endTime;     // 종료시간

    public LessonTime(int day, Date startDate, Date endDate, String startTime, String endTime) {
        setDay(day);
        this.startTime = startTime;
        this.endTime = endTime;

        setStartDate(startDate);

        setEndDate(endDate);
    }


    public int getDay() {
        return day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLid() {
        return lid;
    }

    public void setLid(int lid) {
        this.lid = lid;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.KOREA);
        this.startDate = format.format(startDate);
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.KOREA);
        this.endDate = format.format(endDate);
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDayString() {
        switch (this.day) {
            case 1:
                return "일";
            case 2:
                return "월";
            case 3:
                return "화";
            case 4:
                return "수";
            case 5:
                return "목";
            case 6:
                return "금";
            case 7:
                return "토";
            default:
                return "알수없음";
        }
    }

    public void setDay(int dayOfWeek) {
        this.day = dayOfWeek;
    }
}
