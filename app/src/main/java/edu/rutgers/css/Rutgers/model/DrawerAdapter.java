package edu.rutgers.css.Rutgers.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.util.Swappable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.SettingsActivity;
import edu.rutgers.css.Rutgers.ui.fragments.AboutDisplay;
import edu.rutgers.css.Rutgers.utils.ImageUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

/**
 * Adapter for holding channels
 * TODO refactor this class to something more extensible
 */
public class DrawerAdapter extends BaseAdapter implements Swappable {
    private final Context context;
    private final List<Channel> channels;
    private final List<Link> uris;

    private final int itemLayout;
    private final int dividerLayout;

    private static final int EXTRA = 4; // 2 dividers, 1 about, 1 settings

    private static final int DIVIDER_SIZE = 1;

    private static final int DIVIDER_OFFSET = 0;
    private static final int ABOUT_OFFSET = 1;
    private static final int SETTINGS_OFFSET = 2;

    private static final int VIEW_TYPES = 2;
    private static final int PRESSABLE_TYPE = 0;
    private static final int DIVIDER_TYPE = 1;

    @Override
    public void swapItems(int positionOne, int positionTwo) {
        if (positionIsURI(positionOne) && positionIsURI(positionTwo)) {
            Collections.swap(uris, positionOne, positionTwo);
            PrefUtils.setBookmarks(context, uris);
            notifyDataSetChanged();
        }
    }

    private class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    public DrawerAdapter(Context context, int itemLayout, int dividerLayout, List<Link> uris, List<Channel> channels) {
        this.context = context;
        this.itemLayout = itemLayout;
        this.dividerLayout = dividerLayout;
        this.channels = channels;
        this.uris = uris;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        String homeCampus = RutgersUtils.getHomeCampus(context);
        ViewHolder holder;
        final int type = getItemViewType(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    type == PRESSABLE_TYPE ? itemLayout : dividerLayout, null);

            if (type == DIVIDER_TYPE) {
                convertView.setOnClickListener(null);
            }

            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (type == DIVIDER_TYPE) {
            return convertView;
        }

        if (positionIsURI(position)) {
            Link link = (Link) getItem(position);
            holder.textView.setText(link.getUri(Link.Schema.RUTGERS).toString());
            holder.imageView.setImageDrawable(ImageUtils.getIcon(context.getResources(), link.getHandle()));
        } else if (positionIsChannel(position)) {
            Channel channel = (Channel) getItem(position);
            holder.textView.setText(channel.getTitle(homeCampus));
            holder.imageView.setImageDrawable(ImageUtils.getIcon(context.getResources(), channel.getHandle()));
        } else if (positionIsSettings(position)) {
            holder.textView.setText("Settings");
            holder.imageView.setImageDrawable(ImageUtils.getIcon(context.getResources(), "settings_black_24dp"));
        } else if (positionIsAbout(position)) {
            holder.textView.setText("About");
            holder.imageView.setImageDrawable(ImageUtils.getIcon(context.getResources(), "info"));
        }

        return convertView;
    }

    @Override
    public int getCount() {
        // The total count is the number of channels plus one for each of:
        // settings, about, divider
        return uris.size() + channels.size() + EXTRA;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPES;
    }

    @Override
    public int getItemViewType(int position) {
        if (positionIsDivider(position)) {
            return DIVIDER_TYPE;
        }
        return PRESSABLE_TYPE;
    }

    @Override
    public Object getItem(int i) {
        if (positionIsURI(i)) {
            return uris.get(i);
        } else if (positionIsChannel(i)) {
            return channels.get(i - (uris.size() + DIVIDER_SIZE));
        } else if (positionIsSettings(i)) {
            return SettingsActivity.class;
        } else if (positionIsAbout(i)) {
            return AboutDisplay.class;
        }
        return null;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void addAllChannels(Collection<Channel> channels) {
        this.channels.addAll(channels);
        notifyDataSetChanged();
    }

    public void addAllLinks(Collection<Link> links) {
        this.uris.addAll(links);
        notifyDataSetChanged();
    }

    public void clearLinks() {
        this.uris.clear();
        notifyDataSetChanged();
    }

    public void clearChannels() {
        channels.clear();
        notifyDataSetChanged();
    }

    public boolean positionIsURI(int position) {
        return position < uris.size();
    }

    public boolean positionIsChannel(int position) {
        return position > uris.size() && position <= uris.size() + channels.size();
    }

    public boolean positionIsDivider(int position) {
        return position == getLowDividerPosition() || position == getHighDividerPosition();
    }

    public int getHighDividerPosition() {
        return uris.size() + DIVIDER_OFFSET;
    }

    public int getLowDividerPosition() {
        return uris.size() + channels.size() + DIVIDER_SIZE + DIVIDER_OFFSET;
    }

    public boolean positionIsAbout(int position) {
        return position == getAboutPosition();
    }

    public int getAboutPosition() {
        return uris.size() + DIVIDER_SIZE + channels.size() + ABOUT_OFFSET;
    }

    public boolean positionIsSettings(int position) {
        return position == getSettingsPosition();
    }

    public int getSettingsPosition() {
        return uris.size() + DIVIDER_SIZE + channels.size() + SETTINGS_OFFSET;
    }
}
