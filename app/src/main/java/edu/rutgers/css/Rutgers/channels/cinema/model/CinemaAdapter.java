package edu.rutgers.css.Rutgers.channels.cinema.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;

import java.text.DateFormat;
import java.util.Collection;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.cinema.model.Movie;
import edu.rutgers.css.Rutgers.api.cinema.model.Showing;

/**
 * Adapter for movie times
 */

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.ViewHolder> {
    private final List<Movie> movies;

    public CinemaAdapter(final List<Movie> movies) {
        this.movies = movies;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.row_generic_text, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Movie movie = movies.get(position);
        String text = WordUtils.capitalizeFully(movie.getName());
        for (final Showing showing : movie.getShowings()) {
            text += " " + DateFormat.getTimeInstance(DateFormat.SHORT).format(showing.getDateTime());
        }
        holder.textView.setText(text);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(final View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.generic_text);
        }
    }

    public void addAll(final Collection<Movie> movies) {
        this.movies.addAll(movies);
        notifyDataSetChanged();
    }

    public void clear() {
        movies.clear();
        notifyDataSetChanged();
    }
}
