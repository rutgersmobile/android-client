package edu.rutgers.css.Rutgers.channels.athletics.model;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Collection;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.athletics.model.AthleticsGame;

/**
 * Recycler adapter for AthleticsGames backed by a list.
 */
public final class AthleticsAdapter extends RecyclerView.Adapter<AthleticsAdapter.ViewHolder> {
    private static final String RUTGERS_CODE = "rutu";
    private final int pantone186;
    private final int pantone431;
    private final int black;
    private static final String imageURLBase = "http://grfx.cstv.com/graphics/school-logos/";
    private static final String imageExtLarge =  "-lg.png";
    private static final String imageExtSmall =  "-sm.png";
    private static final String imageRutgersLarge = imageURLBase + RUTGERS_CODE + imageExtLarge;

    private final Context context;

    public Context getContext() {
        return context;
    }

    private final int layoutResource;
    private final List<AthleticsGame> games;

    public AthleticsAdapter(final Context context, final int layoutResource, final List<AthleticsGame> games) {
        this.context = context;
        this.games = games;
        this.layoutResource = layoutResource;
        this.pantone186 = ContextCompat.getColor(context, R.color.pantone186);
        this.pantone431 = ContextCompat.getColor(context, R.color.pantone431);
        this.black = ContextCompat.getColor(context, R.color.black);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View rutgersHome;
        private final ImageView opponentIcon;
        private final TextView notRutgers;
        private final LinearLayout score;
        private final TextView homeScore;
        private final TextView awayScore;
        private final TextView gameLocation;
        private final TextView gameDate;
        private final TextView gameTime;

        public ViewHolder(View v) {
            super(v);
            this.rutgersHome = v.findViewById(R.id.rutgers_home);
            this.opponentIcon = (ImageView) v.findViewById(R.id.opponent_icon);
            this.notRutgers = (TextView) v.findViewById(R.id.not_rutgers);
            this.score = (LinearLayout) v.findViewById(R.id.score);
            this.homeScore = (TextView) v.findViewById(R.id.home_score);
            this.awayScore = (TextView) v.findViewById(R.id.away_score);
            this.gameLocation = (TextView) v.findViewById(R.id.game_location);
            this.gameDate = (TextView) v.findViewById(R.id.game_date);
            this.gameTime = (TextView) v.findViewById(R.id.game_time);
        }

        public View getRutgersHome() {
            return rutgersHome;
        }

        public ImageView getOpponentIcon() {
            return opponentIcon;
        }

        public TextView getNotRutgers() {
            return notRutgers;
        }

        public LinearLayout getScore() {
            return score;
        }

        public TextView getHomeScore() {
            return homeScore;
        }

        public TextView getAwayScore() {
            return awayScore;
        }

        public TextView getGameLocation() {
            return gameLocation;
        }

        public TextView getGameDate() {
            return gameDate;
        }

        public TextView getGameTime() {
            return gameTime;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layoutResource, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final AthleticsGame game = games.get(position);

        final boolean rutgersHome = game.getHome().getCode().equals(RUTGERS_CODE) && !game.isEvent();

        final String opponent = rutgersHome
                ? ("vs. " + game.getAway().getName())
                : ("@ " + game.getHome().getName());

        holder.getRutgersHome().setBackgroundColor(rutgersHome ? pantone186 : pantone431);

        final String notRutgers = game.isEvent() ? game.getDescription() : opponent;
        holder.getNotRutgers().setText(notRutgers);

        final Integer homeScore = game.getHome().getScore();
        final Integer awayScore = game.getAway().getScore();

        if (homeScore != null || awayScore != null) {
            holder.getScore().setVisibility(View.VISIBLE);
            holder.getHomeScore().setText(String.valueOf(homeScore));
            holder.getAwayScore().setText(String.valueOf(awayScore));
        } else {
            holder.getScore().setVisibility(View.INVISIBLE);
        }

        if (rutgersHome) {
            holder.getHomeScore().setTextColor(pantone186);
            holder.getAwayScore().setTextColor(black);
        } else {
            holder.getHomeScore().setTextColor(black);
            holder.getAwayScore().setTextColor(pantone186);
        }

        holder.getGameLocation().setText(game.getLocation());
        holder.getGameDate().setText(DateFormat.getDateInstance().format(game.getStart().getDate()));
        holder.getGameTime().setText(game.getStart().isTime()
                ? DateFormat.getTimeInstance(DateFormat.SHORT).format(game.getStart().getDate())
                : game.getStart().getTimeString());

        final String opponentCode = rutgersHome
                ? game.getAway().getCode()
                : game.getHome().getCode();
        final String imageURL = imageURLBase + opponentCode + imageExtLarge;
        Picasso.with(getContext())
                .load(imageURL)
                .into(holder.opponentIcon, new Callback() {
                    @Override
                    public void onSuccess() { }

                    @Override
                    public void onError() {
                        Picasso.with(getContext())
                                .load(imageRutgersLarge)
                                .into(holder.opponentIcon);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return this.games.size();
    }

    public void addAll(final Collection<AthleticsGame> games) {
        this.games.addAll(games);
        notifyDataSetChanged();
    }

    public void clear() {
        games.clear();
        notifyDataSetChanged();
    }
}
