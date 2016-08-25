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
import lombok.Getter;

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

    @Getter
    private final Context context;

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
        @Getter View rutgersHome;
        @Getter ImageView opponentIcon;
        @Getter TextView notRutgers;
        @Getter LinearLayout score;
        @Getter TextView homeScore;
        @Getter TextView awayScore;
        @Getter TextView gameLocation;
        @Getter TextView gameDate;
        @Getter TextView gameTime;

        public static ViewHolderBuilder builder() {
            return new ViewHolderBuilder();
        }

        public static class ViewHolderBuilder {
            View rootView;
            View rutgersHome;
            ImageView opponentIcon;
            TextView notRutgers;
            LinearLayout score;
            TextView homeScore;
            TextView awayScore;
            TextView gameLocation;
            TextView gameDate;
            TextView gameTime;

            private ViewHolderBuilder() { }

            public ViewHolderBuilder rootView(View rootView) {
                this.rootView = rootView;
                return this;
            }

            public ViewHolderBuilder rutgersHome(View rutgersHome) {
                this.rutgersHome = rutgersHome;
                return this;
            }

            public ViewHolderBuilder opponentIcon(ImageView opponentIcon) {
                this.opponentIcon = opponentIcon;
                return this;
            }

            public ViewHolderBuilder notRutgers(TextView notRutgers) {
                this.notRutgers = notRutgers;
                return this;
            }

            public ViewHolderBuilder score(LinearLayout score) {
                this.score = score;
                return this;
            }

            public ViewHolderBuilder homeScore(TextView homeScore) {
                this.homeScore = homeScore;
                return this;
            }

            public ViewHolderBuilder awayScore(TextView awayScore) {
                this.awayScore = awayScore;
                return this;
            }

            public ViewHolderBuilder gameLocation(TextView gameLocation) {
                this.gameLocation = gameLocation;
                return this;
            }

            public ViewHolderBuilder gameDate(TextView gameDate) {
                this.gameDate = gameDate;
                return this;
            }

            public ViewHolderBuilder gameTime(TextView gameTime) {
                this.gameTime = gameTime;
                return this;
            }

            public ViewHolder build() {
                return new ViewHolder(
                        rootView,
                        rutgersHome,
                        opponentIcon,
                        notRutgers,
                        score,
                        homeScore,
                        awayScore,
                        gameLocation,
                        gameDate,
                        gameTime
                );
            }
        }

        public ViewHolder(View v,
                          View rutgersHome,
                          ImageView opponentIcon,
                          TextView notRutgers,
                          LinearLayout score,
                          TextView homeScore,
                          TextView awayScore,
                          TextView gameLocation,
                          TextView gameDate,
                          TextView gameTime) {
            super(v);
            this.rutgersHome = rutgersHome;
            this.opponentIcon = opponentIcon;
            this.notRutgers = notRutgers;
            this.score = score;
            this.homeScore = homeScore;
            this.awayScore = awayScore;
            this.gameLocation = gameLocation;
            this.gameDate = gameDate;
            this.gameTime = gameTime;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layoutResource, parent, false);

        return ViewHolder.builder()
                .rootView(v)
                .rutgersHome(v.findViewById(R.id.rutgers_home))
                .opponentIcon((ImageView) v.findViewById(R.id.opponent_icon))
                .notRutgers((TextView) v.findViewById(R.id.not_rutgers))
                .score((LinearLayout) v.findViewById(R.id.score))
                .homeScore((TextView) v.findViewById(R.id.home_score))
                .awayScore((TextView) v.findViewById(R.id.away_score))
                .gameLocation((TextView) v.findViewById(R.id.game_location))
                .gameDate((TextView) v.findViewById(R.id.game_date))
                .gameTime((TextView) v.findViewById(R.id.game_time))
                .build();
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
