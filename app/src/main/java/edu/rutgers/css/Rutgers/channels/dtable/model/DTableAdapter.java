package edu.rutgers.css.Rutgers.channels.dtable.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.expandablelistitem.ExpandableListItemAdapter;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

/**
 * Adapter for making menus from DTable roots.
 */
public class DTableAdapter extends ExpandableListItemAdapter<DTableElement> {

    private final static String TAG = "DTableAdapter";

    private Context mContext;
    private List<DTableElement> mItems;
    private String mHomeCampus;

    public enum ViewTypes {
        ROOT_TYPE, CAT_TYPE, FAQ_TYPE, TEXT_TYPE
    }
    private static ViewTypes[] viewTypes = ViewTypes.values();

    static class ViewHolder {
        TextView titleTextView;
    }

    static class PopViewHolder {
        TextView popdownTextView;
    }

    public DTableAdapter(@NonNull Context context, List<DTableElement> elements) {
        super(context, R.layout.row_bus_prediction, R.id.titleFrame, R.id.contentFrame);
        mContext = context;
        setData(elements);
        mHomeCampus = RutgersUtils.getHomeCampus(mContext); // TODO listen for updates to home campus
    }

    public void setData(List<DTableElement> elements) {
        if (elements == null) {
            mItems = new ArrayList<>();
        } else {
            mItems = elements;
        }
        notifyDataSetChanged();
    }

    public boolean addAll(List<DTableElement> elements) {
        if (elements == null || elements.isEmpty()) return false;
        if (mItems.addAll(elements)) {
            notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        mItems.clear();
        super.clear();
    }

    @Override
    public DTableElement getItem (int pos) {
       return mItems.get(pos);
    }

    @Override
    public int getCount () {
        return mItems.size();
    }

    @Override
    public long getItemId (int id) {
        return id;
    }

    @Override
    public int getViewTypeCount() {
        return viewTypes.length;
    }

    @Override
    public int getItemViewType(int position) {
        DTableElement element = getItem(position);

        if (element instanceof DTableFAQ) return ViewTypes.FAQ_TYPE.ordinal();
        else if (element instanceof DTableRoot) return ViewTypes.CAT_TYPE.ordinal();
        else if (element instanceof DTableChannel) return ViewTypes.ROOT_TYPE.ordinal();
        else return ViewTypes.TEXT_TYPE.ordinal();
    }

    @NonNull
    @Override
    public View getTitleView(final int position, @Nullable View convertView, @NonNull ViewGroup viewGroup) {
        switch(viewTypes[getItemViewType(position)]) {
            case CAT_TYPE:
                return getCategoryView(position, convertView, viewGroup);
            case ROOT_TYPE:
            case FAQ_TYPE:
            default:
                return getDefaultView(position, convertView, viewGroup);
        }
    }

    @NonNull
    @Override
    public View getContentView(final int position, @Nullable View convertView, @NonNull ViewGroup viewGroup) {
        PopViewHolder holder;

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.popdown, null);
            holder = new PopViewHolder();
            holder.popdownTextView = (TextView) convertView.findViewById(R.id.popdownTextView);
            convertView.setTag(holder);
        } else {
            holder = (PopViewHolder) convertView.getTag();
        }

        DTableElement element = getItem(position);

        if (element instanceof DTableFAQ) {
            DTableFAQ faqElement = (DTableFAQ) element;
            holder.popdownTextView.setText(faqElement.getAnswer());
        } else {
            holder.popdownTextView.setText(null);
        }

        return convertView;
    }

    /**
     * Basic row with a line of text. Will display the appropriate "local" title based on
     * user's home campus if home and away strings are specified.
     */
    @NonNull
    private View getDefaultView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // If we aren't given a view, inflate one. Get special layout for sub-menu items.
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.row_title, null);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DTableElement element = mItems.get(position);

        holder.titleTextView.setText(element.getTitle(mHomeCampus));
        return convertView;
    }

    /**
     * Category row with category title. Will display the appropriate "local" title based on
     * user's home campus if home and away strings are specified.
     */
    @NonNull
    private View getCategoryView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // If we aren't given a view, inflate one. Get special layout for sub-menu items.
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.row_dtable_category, null);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DTableRoot rootElement = (DTableRoot) mItems.get(position);

        holder.titleTextView.setText(rootElement.getTitle(mHomeCampus));
        return convertView;
    }

}