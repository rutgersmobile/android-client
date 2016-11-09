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
    // TODO should be https but it doesn't have a SHA-2 cert
    public static final String WEBREG_BASE_URL = "https://sims.rutgers.edu/webreg/";
    // Campus codes (full list should be read from res/raw/soc_campuses.json)
    public static final String CODE_CAMPUS_NB = "NB";
    public static final String CODE_CAMPUS_NWK = "NK";
    public static final String CODE_CAMPUS_CAM = "CM";
    public static final String CODE_CAMPUS_ONLINE = "ONLINE";
    // Course levels
    public static final String CODE_LEVEL_UNDERGRAD = "U";
    public static final String CODE_LEVEL_GRAD = "G";

    private static SOCService service;
    private static final String NOT_SET_UP_MESSAGE = "Set up service with SOCAPI::setService or SOCAPI::simpleSetup";

    /**
     * An easy way to set up the API for making calls. This will correctly configure the service
     * before using it.
     * @param client You should use the same client for all APIs. This will do actual HTTP work.
     * @param apiBase Base url for the api. Probably something like "http://webservices.nextbus.com/"
     */
    public static void simpleSetup(OkHttpClient client, String apiBase) {
        final Retrofit retrofit = new Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .baseUrl(apiBase)
            .build();

        SOCAPI.service = retrofit.create(SOCService.class);
    }

    /**
     * Set the retrofit service that will be used to make requests
     * @param service A retrofit service for making calls
     */
    public static void setService(SOCService service) {
        SOCAPI.service = service;
    }

    /**
     * Get the service previously set. You probably don't want to use this. Instead just call the
     * other static, public methods in the class
     * @return Service for making Rutgers API calls
     */
    public static SOCService getService() {
        if (service == null) {
            throw new IllegalStateException(NOT_SET_UP_MESSAGE);
        }

        return service;
    }

    /**
     * Get all online subjects for a certain term, year, and level
     * @param term Usually the month of a semester
     * @param year  The year the subjects are offered
     * @param level Graduate ("G") or undergraduate ("U")
     * @return List of subject codes with titles
     */
    public static Observable<List<Subject>> getOnlineSubjects(String term, String year, String level) {
        return getService().getOnlineSubjects(term, year, level);
    }

    /**
     * Get all subjects for a semester, campus, and level
     * @param semester Month + year like "092016"
     * @param campus Campus subjects are offered on, ex. "NB", "NWK"
     * @param level Graduate ("G") or undergraduate ("U")
     * @return List of subject codes with titles
     */
    public static Observable<List<Subject>> getSubjects(String semester, String campus, String level) {
        return getService().getSubjects(semester, campus, level);
    }

    /**
     * Get all online courses for a certain term, year, level, and subject
     * @param term Usually the month of a semester
     * @param year The year that the subjects are offered
     * @param level Graduate ("G") or undergraduate ("U")
     * @param subject Subject code, ex. "010" (Accounting)
     * @return List of courses with sections
     */
    public static Observable<List<Course>> getOnlineCourses(String term,
                                                            String year,
                                                            String level,
                                                            String subject) {
        return getService().getOnlineCourses(term, year, level, subject);
    }

    /**
     * Get all courses for a certain term, year, level, and subject
     * @param semester Month and year of the semester, ex. "092016"
     * @param campus Campus subjects are offered on, ex. "NB", "NWK"
     * @param level Graduate ("G") or undergraduate ("U")
     * @param subject Subject code, ex. "010" (Accounting)
     * @return List of courses with sections
     */
    public static Observable<List<Course>> getCourses(String semester,
                                                      String campus,
                                                      String level,
                                                      String subject) {
        return getService().getCourses(semester, campus, level, subject);
    }

    /**
     * Get a specific online course
     * @param term Usually the month of a semester
     * @param year The year that the subjects are offered
     * @param subject Subject code, ex. "010" (Accounting)
     * @param course Course code, ex. "101"
     * @return A course with sections and a title
     */
    public static Observable<Course> getOnlineCourse(String term,
                                                     String year,
                                                     String subject,
                                                     String course) {
        return getService().getOnlineCourse(term, year, subject, course);
    }

    /**
     * Get a specific course
     * @param semester Month and year of the semester, ex. "092016"
     * @param campus Campus subjects are offered on, ex. "NB", "NWK"
     * @param subject Subject code, ex. "010" (Accounting)
     * @param course Course code, ex. "101"
     * @return A course with sections and a title
     */
    public static Observable<Course> getCourse(String semester,
                                               String campus,
                                               String subject,
                                               String course) {
        return getService().getCourse(semester, campus, subject, course);
    }

    /**
     * Convert a semester code (e.g. "72014") to human-readable form ("Summer 2014")
     * @param semesterCode Semester code
     * @return Human-readable semester name
     */
    public static String translateSemester(String semesterCode) {
        if (semesterCode == null) return null;
        if (semesterCode.length() != 5) {
            return semesterCode;
        }

        int leadingDigit = Character.getNumericValue(semesterCode.charAt(0));
        if (leadingDigit < 0) {
            return semesterCode;
        }

        StringBuilder result = new StringBuilder();

        switch(leadingDigit) {
            case 0:
                result.append("Winter");
                break;
            case 1:
                result.append("Spring");
                break;
            case 7:
                result.append("Summer");
                break;
            case 9:
                result.append("Fall");
                break;
            default:
                return semesterCode;
        }

        result.append(" ");
        result.append(semesterCode.substring(1));

        return result.toString();
    }

    /**
     * Open WebReg registration for this course section in the browser.
     * @param semesterCode Semester code (e.g. 72014 for Summer 2014)
     * @param courseIndex Section index number
     */
    public static String getRegistrationLink(String semesterCode, String courseIndex) {
        return WEBREG_BASE_URL + "editSchedule.htm?login=cas&semesterSelection=" + semesterCode + "&indexList=" + courseIndex;
    }
}
