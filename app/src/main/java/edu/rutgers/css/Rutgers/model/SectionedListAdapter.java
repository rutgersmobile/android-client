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

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Adapter for data that is grouped by sections.
 *
 * <p>This abstract class defines three abstract methods
 * that make it easy to add multiple objects that represent or contain some kind of collection
 * to an adapter. The contents of each collection can then be displayed in order with header
 * separators. This avoids the need to mix header objects and data objects in an adapter/list, and
 * allows for the nice "sticky headers" behavior.
 *
 * <p>For simple usage, this class has default methods for inflating the given item and header layouts
 * and populating a TextView using the {@link #toString()} method for items and the
 * {@link #getSectionHeader(T section)} method for headers.
 * 
 * <p>For more advanced usage, you can override the {@link #getView(int position, View convertView, ViewGroup parent)}
 * and {@link #getHeaderView(int, android.view.View, android.view.ViewGroup)} methods.
 */
public abstract class SectionedListAdapter<T, U> extends BaseAdapter
        implements StickyListHeadersAdapter {

    private Context mContext;
    private LayoutInflater mInflater;

    /** Resource ID of view to inflate for displaying an item. */
    private int mItemResource;

    /** Resource ID of view to inflate for displaying a header. */
    private int mHeaderResource;

    /** Resource ID of TextView to display header or item string representation in. */
    private int mTextViewId;

    /** Contains the objects representing each section in the list. */
    private List<T> mSections;

    /** Lock used to modify the content of {@link #mSections}. */
    protected final Object mLock = new Object();

    /** Holder for default getView() implementations. */
    private static class ViewHolder {
        TextView textView;
    }

    /**
     * Constructor
     * @param context Context
     * @param itemResource Layout resource for item views
     * @param headerResource Layout resource for header views
     * @param textViewId ID of text view to populate in the item and header layouts
     */
    public SectionedListAdapter(@NonNull Context context, int itemResource, int headerResource, int textViewId) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mItemResource = itemResource;
        mHeaderResource = headerResource;
        mTextViewId = textViewId;
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
        int total = 0;
        for(T section: mSections) total += getSectionItemCount(section);
        return total;
    }

    /** Add the specified object to the end of the list. */
    public void add(T section) {
        synchronized (mLock) {
            mSections.add(section);
        }
        notifyDataSetChanged();
    }

    /** Add the group of specified objects to the end of the list. */
    public void addAll(Collection<T> sections) {
        synchronized (mLock) {
            mSections.addAll(sections);
        }
        notifyDataSetChanged();
    }

    /** Remove all objects from the list. */
    public void clear() {
        synchronized (mLock) {
            mSections.clear();
        }
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
            convertView = getLayoutInflater().inflate(getItemResource(), null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(getTextViewId());
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
            convertView = getLayoutInflater().inflate(getHeaderResource(), null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(getTextViewId());
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

    protected int getItemResource() {
        return mItemResource;
    }

    protected int getHeaderResource() {
        return mHeaderResource;
    }

    protected int getTextViewId() {
        return mTextViewId;
    }

    protected List<T> getSections() {
        return mSections;
    }

}
