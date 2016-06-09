package edu.rutgers.css.Rutgers.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.util.Swappable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.utils.ImageUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import lombok.Data;

/**
 * Adapter for bookmarks
 */
public final class BookmarkAdapter extends BaseAdapter implements Swappable {

    private final int removableResource;
    private final int lockedResource;
    private final int dividerResource;
    private final Context context;
    private final List<Link> links;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private static final int REMOVABLE = 0;
    private static final int LOCKED = 1;
    private static final int DIVIDER = 2;

    @Override
    public void swapItems(int positionOne, int positionTwo) {
        final int dividerPosition = dividerPosition();
        if (positionOne < dividerPosition && positionTwo < dividerPosition) {
            Collections.swap(links, positionOne, positionTwo);
        } else if (positionOne > dividerPosition && positionTwo > dividerPosition) {
            Collections.swap(links, positionOne - 1, positionTwo - 1);
        } else if (positionOne == dividerPosition && positionTwo < dividerPosition) {
            final Link link = links.get(positionTwo);
            if (!link.isRemovable()) {
                link.setEnabled(false);
            }
        } else if (positionOne == dividerPosition && positionTwo > dividerPosition) {
            links.get(positionTwo - 1).setEnabled(true);
        }
    }

    @Override
    public boolean isEnabled(int position) {
        return position != dividerPosition();
    }

    private final class ViewHolder {
        CheckBox checkBox;
        ImageButton imageButton;
        TextView textView;
        ImageView imageView;
    }

    @Data
    private final class TwoLists {
        private final List<Link> visible;
        private final List<Link> hidden;
    }

    private TwoLists splitVisible(List<Link> links) {
        final List<Link> visible = new ArrayList<>();
        final List<Link> hidden = new ArrayList<>();
        for (final Link link : links) {
            if (link.isEnabled()) {
                visible.add(link);
            } else {
                hidden.add(link);
            }
        }

        return new TwoLists(visible, hidden);
    }

    private int dividerPosition() {
        for (int i = 0; i < links.size(); i++) {
            if (!links.get(i).isEnabled()) {
                return i;
            }
        }

        return links.size();
    }

    public void addFromPrefs() {
        final List<Link> links = PrefUtils.getBookmarks(context);
        final TwoLists lists = splitVisible(links);
        this.links.addAll(lists.getVisible());
        this.links.addAll(lists.getHidden());
    }

    public BookmarkAdapter(final Context context, int removableResource, int lockedResource, int dividerResource) {
        this.context = context;
        this.removableResource = removableResource;
        this.lockedResource = lockedResource;
        this.dividerResource = dividerResource;
        this.links = new ArrayList<>();

        registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                PrefUtils.setBookmarks(context, links);
            }
        });

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                links.clear();
                links.addAll(PrefUtils.getBookmarks(context));
                notifyDataSetChanged();
            }
        };
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        final Link link = (Link) getItem(position);

        if (link == null) {
            return DIVIDER;
        }

        if (link.isRemovable()) {
            return REMOVABLE;
        }

        return LOCKED;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return links.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        final int dividerPosition = dividerPosition();
        if (position == dividerPosition) {
            return null;
        }

        if (position < dividerPosition) {
            return links.get(position);
        }

        return links.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        final Link link = (Link) getItem(position);

        if (link == null) {
            return 0;
        }

        return link.hashCode();
    }

    private void removeNoNotify(int position) {
        final int dividerPosition = dividerPosition();
        if (position < dividerPosition) {
            links.remove(position);
        } else if (position > dividerPosition) {
            links.remove(position - 1);
        }
    }

    public void remove(int position) {
        removeNoNotify(position);
        notifyDataSetChanged();
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;

        final int type = getItemViewType(position);

        if (convertView == null) {
            switch (type) {
                case REMOVABLE:
                    convertView = layoutInflater.inflate(removableResource, null);
                    holder = new ViewHolder();
                    holder.imageButton = (ImageButton) convertView.findViewById(R.id.delete_bookmark);
                    holder.textView = (TextView) convertView.findViewById(R.id.title);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                    convertView.setTag(holder);
                    break;
                case LOCKED:
                    convertView = layoutInflater.inflate(lockedResource, null);
                    holder = new ViewHolder();
                    holder.checkBox = (CheckBox) convertView.findViewById(R.id.toggle_bookmark);
                    holder.textView = (TextView) convertView.findViewById(R.id.title);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                    convertView.setTag(holder);
                    break;
                default:
                    convertView = layoutInflater.inflate(dividerResource, null);
                    holder = new ViewHolder();
                    convertView.setTag(holder);
                    break;
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Link link = (Link) getItem(position);
        if (link != null) {
            holder.textView.setText(link.getTitle());

            int iconRes = context.getResources().getIdentifier("ic_" + link.getHandle(), "drawable", Config.PACKAGE_NAME);
            holder.imageView.setImageDrawable(ImageUtils.getIcon(context.getResources(), iconRes, R.color.dark_gray));

            if (!link.isRemovable()) {
                holder.checkBox.setChecked(link.isEnabled());
                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox checkBox = (CheckBox) v;
                        link.setEnabled(checkBox.isChecked());
                        removeNoNotify(position);
                        links.add(dividerPosition(), link);
                        notifyDataSetChanged();
                    }
                });
            } else {
                holder.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        remove(position);
                    }
                });
            }
        }
        return convertView;
    }
}
