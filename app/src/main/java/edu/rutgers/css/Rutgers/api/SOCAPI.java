package edu.rutgers.css.Rutgers.api;

import java.util.List;

import edu.rutgers.css.Rutgers.api.model.soc.Course;
import edu.rutgers.css.Rutgers.api.model.soc.Subject;
import edu.rutgers.css.Rutgers.api.service.SOCService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Service for SOC API
 */

public final class SOCAPI {
    private static SOCService service;
    private static final String NOT_SET_UP_MESSAGE = "Set up service with SOCAPI::setService or SOCAPI::simpleSetup";

    public static void simpleSetup(OkHttpClient client, String apiBase) {
        final Retrofit retrofit = new Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(apiBase)
            .build();

        SOCAPI.service = retrofit.create(SOCService.class);
    }

    public static void setService(SOCService service) {
        SOCAPI.service = service;
    }

    public static SOCService getService() {
        if (service == null) {
            throw new IllegalStateException(NOT_SET_UP_MESSAGE);
        }

        return service;
    }

    public static Observable<List<Subject>> getOnlineSubjects(String term, String year, String level) {
        return getService().getOnlineSubjects(term, year, level);
    }

    public static Observable<List<Subject>> getSubjects(String semester, String campus, String level) {
        return getService().getSubjects(semester, campus, level);
    }

    public static Observable<List<Course>> getOnlineCourses(String term,
                                                            String year,
                                                            String level,
                                                            String subject) {
        return getService().getOnlineCourses(term, year, level, subject);
    }

    public static Observable<List<Course>> getCourses(String semester,
                                                      String campus,
                                                      String level,
                                                      String subject) {
        return getService().getCourses(semester, campus, level, subject);
    }

    public static Observable<Course> getOnlineCourse(String term,
                                                     String year,
                                                     String subject,
                                                     String course) {
        return getService().getOnlineCourse(term, year, subject, course);
    }

    public static Observable<Course> getCourse(String semester,
                                               String campus,
                                               String subject,
                                               String course) {
        return getService().getCourse(semester, campus, subject, course);
    }
}
