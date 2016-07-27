package edu.rutgers.css.Rutgers.channels.athletics.model;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.athletics.model.AthleticsGame;
import lombok.Builder;
import lombok.Data;

/**
 * Array adapter extended for Athletics items
 */
public final class AthleticsAdapter extends ArrayAdapter<AthleticsGame> {
    private final int layoutResource;
    private final String RUTGERS_CODE = "rutu";
    private int loserColor;

    @Data
    @Builder
    private static class ViewHolder {
        ImageView opponentIcon;
        TextView opponentIconText;
        TextView homeTeam;
        TextView homeScore;
        TextView awayTeam;
        TextView awayScore;
        TextView gameLocation;
        TextView gameDate;
        TextView gameTime;
    }

    public AthleticsAdapter(Context context, int resource, List<AthleticsGame> games) {
        super(context, resource, games);
        layoutResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = ViewHolder.builder()
                    .opponentIcon((ImageView) convertView.findViewById(R.id.opponent_icon))
                    .opponentIconText((TextView) convertView.findViewById(R.id.opponent_icon_text))
                    .homeTeam((TextView) convertView.findViewById(R.id.home_team))
                    .homeScore((TextView) convertView.findViewById(R.id.home_score))
                    .awayTeam((TextView) convertView.findViewById(R.id.away_team))
                    .awayScore((TextView) convertView.findViewById(R.id.away_score))
                    .gameLocation((TextView) convertView.findViewById(R.id.game_location))
                    .gameDate((TextView) convertView.findViewById(R.id.game_date))
                    .gameTime((TextView) convertView.findViewById(R.id.game_time))
                    .build();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final int defaultTextColor = holder.getHomeTeam().getCurrentTextColor();
        final int black = ContextCompat.getColor(getContext(), R.color.black);

        if (defaultTextColor != black) {
            loserColor = defaultTextColor;
        }

        holder.getHomeTeam().setTextColor(loserColor);
        holder.getHomeScore().setTextColor(loserColor);
        holder.getAwayTeam().setTextColor(loserColor);
        holder.getAwayScore().setTextColor(loserColor);

        final AthleticsGame game = getItem(position);

        final String opponent = StringUtils.capitalize(game.getHome().getCode().equals(RUTGERS_CODE)
                ? game.getAway().getName()
                : game.getHome().getName());

        holder.getOpponentIconText().setText(opponent.isEmpty()
                ? "R"
                : String.valueOf(opponent.charAt(0)));

        final String homeName = game.isEvent() ? game.getDescription() : game.getHome().getName();
        holder.getHomeTeam().setText(homeName);
        final Integer homeScore = game.getHome().getScore();
        holder.getHomeScore().setText(homeScore == null ? "" : String.valueOf(homeScore));

        final String awayName = game.isEvent() ? "" : game.getAway().getName();
        holder.getAwayTeam().setText(awayName);
        final Integer awayScore = game.getAway().getScore();
        holder.getAwayScore().setText(awayScore == null ? "" : String.valueOf(awayScore));

        holder.getGameLocation().setText(game.getLocation());
        holder.getGameDate().setText(DateFormat.getDateInstance().format(game.getStart().getDate()));
        holder.getGameTime().setText(game.getStart().isTime()
                ? DateFormat.getTimeInstance(DateFormat.SHORT).format(game.getStart().getDate())
                : game.getStart().getTimeString());

        // Only do coloring if at least one score exists
        if (homeScore != null || awayScore != null) {
            // If away score is doesn't exist (and therefore homeScore does)
            // OR if home score does exist (and so must away score)
            // AND if home score is higher (they both must exist here)
            if (awayScore == null || homeScore != null && homeScore > awayScore) {
                // Then color the home team as the winner
                holder.getHomeTeam().setTextColor(black);
                holder.getHomeScore().setTextColor(black);
            } else if (homeScore == null || homeScore < awayScore) {
                holder.getAwayTeam().setTextColor(black);
                holder.getAwayScore().setTextColor(black);
            }
        }

        return convertView;
    }
}
