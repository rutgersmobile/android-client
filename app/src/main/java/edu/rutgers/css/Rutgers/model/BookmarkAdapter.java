package edu.rutgers.css.Rutgers.model;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

    private final int rowLayout;
    private final Context context;
    private final List<Link> list;

    private final class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    @Override
    public void swapItems(int positionOne, int positionTwo) {
        Collections.swap(list, positionOne, positionTwo);
        notifyDataSetChanged();
    }

    public BookmarkAdapter(final Context context, int resource) {
        this.context = context;
        this.rowLayout = resource;
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

        if (convertView == null) {
            convertView = layoutInflater.inflate(rowLayout, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Link link = (Link) getItem(position);
        holder.textView.setText(link.getTitle());

        int iconRes = context.getResources().getIdentifier("ic_"+link.getHandle(), "drawable", Config.PACKAGE_NAME);
        holder.imageView.setImageDrawable(ImageUtils.getIcon(context.getResources(), iconRes, R.color.dark_gray));

        return convertView;
    }
}
