package edu.rutgers.css.Rutgers.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.ui.SettingsActivity;
import edu.rutgers.css.Rutgers.ui.fragments.AboutDisplay;
import edu.rutgers.css.Rutgers.ui.fragments.BookmarksDisplay;
import edu.rutgers.css.Rutgers.utils.ImageUtils;

/**
 * Adapter for holding channels
 * TODO refactor this class to something more extensible
 */
public class DrawerAdapter extends BaseAdapter {
    private final MainActivity activity;
    private final List<Link> links;

    private final int itemLayout;
    private final int dividerLayout;

    private static final int EXTRA = 4; // 1 divider, 1 about, 1 settings, 1 bookmarks

    private static final int DIVIDER_OFFSET = 0;
    private static final int ABOUT_OFFSET = 1;
    private static final int BOOKMARKS_OFFSET = 2;
    private static final int SETTINGS_OFFSET = 3;

    private static final int VIEW_TYPES = 2;
    private static final int PRESSABLE_TYPE = 0;
    private static final int DIVIDER_TYPE = 1;

    private class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    public DrawerAdapter(MainActivity activity, int itemLayout, int dividerLayout, List<Link> links) {
        this.activity = activity;
        this.itemLayout = itemLayout;
        this.dividerLayout = dividerLayout;
        this.links = links;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity.getApplicationContext());
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

        if (positionIsChannel(position)) {
            Link link = (Link) getItem(position);
            holder.textView.setText(link.getTitle());
            holder.imageView.setImageDrawable(ImageUtils.getIcon(activity.getApplicationContext().getResources(), link.getHandle()));
        } else if (positionIsSettings(position)) {
            holder.textView.setText("Settings");
            holder.imageView.setImageDrawable(ImageUtils.getIcon(activity.getApplicationContext().getResources(), "settings_black_24dp"));
        } else if (positionIsAbout(position)) {
            holder.textView.setText("About");
            holder.imageView.setImageDrawable(ImageUtils.getIcon(activity.getApplicationContext().getResources(), "info"));
        } else if (positionIsBookmarks(position)) {
            holder.textView.setText("Bookmarks");
            holder.imageView.setImageDrawable(ImageUtils.getIcon(activity.getApplicationContext().getResources(), "bookmark"));
        }

        return convertView;
    }

    private int getDisabled() {
        int disabled = 0;
        for (final Link link : links) {
            if (!link.isEnabled()) {
                disabled++;
            }
        }

        return disabled;
    }

    @Override
    public int getCount() {
        // The total count is the number of channels plus one for each of:
        // settings, about, divider
        return links.size() - getDisabled() + EXTRA;
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
        if (positionIsChannel(i)) {
            int j = 0;
            for (final Link link : links) {
                if (j == i && link.isEnabled()) {
                    return link;
                } else if (link.isEnabled()) {
                    j++;
                }
            }
            throw new IndexOutOfBoundsException();
        } else if (positionIsSettings(i)) {
            return SettingsActivity.class;
        } else if (positionIsBookmarks(i)){
            return BookmarksDisplay.class;
        } else if (positionIsAbout(i)) {
            return AboutDisplay.class;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(Link link) {
        this.links.add(link);
        notifyDataSetChanged();
    }

    public void addAll(Collection<Link> links) {
        this.links.addAll(links);
        notifyDataSetChanged();
    }

    public void clear() {
        links.clear();
        notifyDataSetChanged();
    }

    public boolean positionIsChannel(int position) {
        return position < links.size() - getDisabled();
    }

    public boolean positionIsDivider(int position) {
        return position == links.size() - getDisabled() + DIVIDER_OFFSET;
    }

    public boolean positionIsAbout(int position) {
        return position == links.size() - getDisabled() + ABOUT_OFFSET;
    }

    public boolean positionIsBookmarks(int position) {
        return position == links.size() - getDisabled() + BOOKMARKS_OFFSET;
    }

    public boolean positionIsSettings(int position) {
        return position == links.size() - getDisabled() + SETTINGS_OFFSET;
    }
}
