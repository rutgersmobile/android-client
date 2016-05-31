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

        holder.getHomeTeam().setText(game.getHome().getName());
        holder.getHomeScore().setText(String.valueOf(game.getHome().getScore()));

        holder.getAwayTeam().setText(game.getAway().getName());
        holder.getAwayScore().setText(String.valueOf(game.getAway().getScore()));

        holder.getGameLocation().setText(game.getLocation());
        holder.getGameDate().setText(DateFormat.getDateInstance().format(game.getStart()));
        holder.getGameTime().setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(game.getStart()));

        if (game.getHome().getScore() > game.getAway().getScore()) {
            holder.getHomeTeam().setTextColor(black);
            holder.getHomeScore().setTextColor(black);
        } else {
            holder.getAwayTeam().setTextColor(black);
            holder.getAwayScore().setTextColor(black);
        }

        return convertView;
    }
}
