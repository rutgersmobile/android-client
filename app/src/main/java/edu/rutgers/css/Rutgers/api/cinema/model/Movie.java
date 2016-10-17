package edu.rutgers.css.Rutgers.api.cinema.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents a movie and it's showtimes
 */

public final class Movie {
    @SerializedName("movie_id") private final String movieId;
    private final String name;
    private final String rating;
    private final String runtime;
    private final String studio;
    private final List<Showing> showings;

    public Movie(final String movieId, final String name, final String rating, final String runtime,
                 final String studio, final List<Showing> showings) {
        this.movieId = movieId;
        this.name = name;
        this.rating = rating;
        this.runtime = runtime;
        this.studio = studio;
        this.showings = showings;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getName() {
        return name;
    }

    public String getRating() {
        return rating;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getStudio() {
        return studio;
    }

    public List<Showing> getShowings() {
        return showings;
    }
}
