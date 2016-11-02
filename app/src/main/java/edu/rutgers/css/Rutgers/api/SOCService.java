package edu.rutgers.css.Rutgers.api;

import java.util.List;

import edu.rutgers.css.Rutgers.api.soc.model.Course;
import edu.rutgers.css.Rutgers.api.soc.model.Subject;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Bindings for SOCApi
 */

public interface SOCService {
    @GET("onlineSubjects.json")
    Observable<List<Subject>> getOnlineSubjects(
        @Query("term") String term,
        @Query("year") String year,
        @Query("level") String level
    );

    @GET("subjects.json")
    Observable<List<Subject>> getSubjects(
        @Query("semester") String semester,
        @Query("campus") String campus,
        @Query("level") String level
    );

    @GET("onlineCourses.json")
    Observable<List<Course>> getOnlineCourses(
        @Query("term") String term,
        @Query("year") String year,
        @Query("level") String level,
        @Query("subject") String subject
    );

    @GET("courses.json")
    Observable<List<Course>> getCourses(
        @Query("semester") String semester,
        @Query("campus") String campus,
        @Query("level") String level,
        @Query("subject") String subject
    );

    @GET("onlineCourse.json")
    Observable<Course> getOnlineCourse(
        @Query("term") String term,
        @Query("year") String year,
        @Query("subject") String subject,
        @Query("courseNumber") String courseNumber
    );
}
