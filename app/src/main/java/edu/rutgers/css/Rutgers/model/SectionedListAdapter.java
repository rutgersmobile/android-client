package edu.rutgers.css.Rutgers.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Adapter for data that is grouped by sections.
 */
public abstract class SectionedListAdapter<T, U> extends BaseAdapter implements StickyListHeadersAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private int mItemResource;
    private int mHeaderResource;
    private List<T> mSections;

    protected static class ViewHolder {
        TextView textView;
    }

    public SectionedListAdapter(@NonNull Context context, int itemResource, int headerResource) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mItemResource = itemResource;
        mHeaderResource = headerResource;

        mSections = new ArrayList<>();
    }

    /** Get title of the section. */
    public abstract String getSectionHeader(T section);

    /** Get an item from the section's collection. */
    public abstract U getSectionItem(T section, int position);

    /** Get the total number of items in a section's collection. */
    public abstract int getSectionItemCount(T section);

    /** Get the sum of the collection sizes for each section. */
    public int getCount() {
        if(mSections == null) return 0;

        int total = 0;
        for(T section: mSections) total += getSectionItemCount(section);
        return total;
    }

    public void add(T section) {
        mSections.add(section);
        notifyDataSetChanged();
    }

    public void addAll(Collection<T> sections) {
        mSections.addAll(sections);
        notifyDataSetChanged();
    }

    public void clear() {
        mSections.clear();
        notifyDataSetChanged();
    }

    /**
     * Get an object from the appropriate section. Position acts as an index into the collection of
     * all items from all sections contained in this adapter.
     */
    public U getItem(int position) {
        if((position+1) > getCount()) throw new IndexOutOfBoundsException("Tried to access item " + (position+1) + " out of " + getCount());

        int i = 0;
        for(T section: mSections) {
            int count = getSectionItemCount(section);
            if(i+count <= position) {
                i += count;
            } else {
                return getSectionItem(section, position - i);
            }
        }

        throw new IllegalStateException("Index " + position + " valid but could not find object.");
    }

    /** Get section that contains the item returned by {@link #getItem(int position)}. */
    protected T getSectionContainingItem(int position) {
        if((position+1) > getCount()) throw new IndexOutOfBoundsException("Tried to access item " + (position+1) + " out of " + getCount());

        int i = 0;
        for(T section: mSections) {
            int count = getSectionItemCount(section);
            if(i+count <= position) {
                i += count;
            } else {
                return section;
            }
        }

        throw new IllegalStateException("Index " + position + " valid but could not find section.");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = getLayoutInflater().inflate(mItemResource, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(getItem(position).toString());

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = getLayoutInflater().inflate(mHeaderResource, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(getSectionHeader(getSectionContainingItem(position)));

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public long getHeaderId(int position) {
        return getSectionContainingItem(position).hashCode();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    protected Context getContext() {
        return mContext;
    }

    protected LayoutInflater getLayoutInflater() {
        return mInflater;
    }

}
