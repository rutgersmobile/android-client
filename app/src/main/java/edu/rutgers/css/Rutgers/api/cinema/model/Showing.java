package edu.rutgers.css.Rutgers.api.cinema.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Represents a showing of a movie at the theatre
 */

public final class Showing {
    @SerializedName("session_id") private final String sessionId;
    @SerializedName("movie_id") private final String movieId;
    private final Date dateTime;
    @SerializedName("aud_id") private final String audId;

    public Showing(final String sessionId, final String movieId, final Date dateTime, final String audId) {
        this.sessionId = sessionId;
        this.movieId = movieId;
        this.dateTime = dateTime;
        this.audId = audId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMovieId() {
        return movieId;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public String getAudId() {
        return audId;
    }
}
