package edu.rutgers.css.Rutgers.model;

import android.content.Context;
import android.database.DataSetObserver;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.utils.ImageUtils;
import edu.rutgers.css.Rutgers.utils.PrefUtils;

/**
 * Adapter for bookmarks
 */
public final class BookmarkAdapter extends BaseAdapter implements Swappable {

    private final int removableResource;
    private final int lockedResource;
    private final Context context;
    private final List<Link> list;

    private static final int REMOVABLE = 0;
    private static final int LOCKED = 1;

    private final class ViewHolder {
        CheckBox checkBox;
        ImageButton imageButton;
        TextView textView;
        ImageView imageView;
    }

    @Override
    public void swapItems(int positionOne, int positionTwo) {
        Collections.swap(list, positionOne, positionTwo);
        notifyDataSetChanged();
    }

    public BookmarkAdapter(final Context context, int removableResource, int lockedResource) {
        this.context = context;
        this.removableResource = removableResource;
        this.lockedResource = lockedResource;
        this.list = new ArrayList<>();

        registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                PrefUtils.setBookmarks(context, list);
            }
        });
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        final Link link = (Link) getItem(position);
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
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    public void addAll(Collection<Link> collection) {
        list.addAll(collection);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        list.remove(position);
        notifyDataSetChanged();
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
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
                default:
                    convertView = layoutInflater.inflate(lockedResource, null);
                    holder = new ViewHolder();
                    holder.checkBox = (CheckBox) convertView.findViewById(R.id.toggle_bookmark);
                    holder.textView = (TextView) convertView.findViewById(R.id.title);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                    convertView.setTag(holder);
                    break;
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Link link = (Link) getItem(position);
        holder.textView.setText(link.getTitle());

        int iconRes = context.getResources().getIdentifier("ic_"+link.getHandle(), "drawable", Config.PACKAGE_NAME);
        holder.imageView.setImageDrawable(ImageUtils.getIcon(context.getResources(), iconRes, R.color.dark_gray));

        if (!link.isRemovable()) {
            holder.checkBox.setChecked(link.isEnabled());
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v;
                    link.setEnabled(checkBox.isChecked());
                    notifyDataSetChanged();
                }
            });
        } else {
            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).equals(link)) {
                            remove(i);
                        }
                    }
                }
            });
        }
        return convertView;
    }
}
