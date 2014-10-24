package edu.rutgers.css.Rutgers.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import edu.rutgers.css.Rutgers.items.DTableChannel;
import edu.rutgers.css.Rutgers.items.DTableElement;
import edu.rutgers.css.Rutgers.items.DTableFAQ;
import edu.rutgers.css.Rutgers.items.DTableRoot;
import edu.rutgers.css.Rutgers.utils.RutgersUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Adapter for making menus from DTable roots.
 */
public class DTableAdapter extends BaseAdapter {

    private final static String TAG = "DTableAdapter";

    private Context mContext;
    private List<DTableElement> mItems;
    private String mHomeCampus;

    public static enum ViewTypes {
        ROOT_TYPE, CAT_TYPE, FAQ_TYPE, TEXT_TYPE
    }
    private static ViewTypes[] viewTypes = ViewTypes.values();

    static class ViewHolder {
        TextView titleTextView;
    }

    static class FAQViewHolder {
        TextView titleTextView;
        TextView popdownTextView;
        LinearLayout popdownLayout;
    }

    public DTableAdapter(@NonNull Context context, @NonNull List<DTableElement> elements) {
        mContext = context;
        mItems = elements;
        mHomeCampus = RutgersUtil.getHomeCampus(mContext); // TODO listen for updates to home campus
    }

    public void setData(@NonNull List<DTableElement> elements) {
        mItems = elements;
        notifyDataSetChanged();
    }

    /**
     * Toggle DTable row pop-down view
     */
    public void togglePopdown(int position) {
        if(getItemViewType(position) == ViewTypes.FAQ_TYPE.ordinal()) {
            DTableFAQ selectedFAQ = (DTableFAQ) mItems.get(position);
            if (selectedFAQ.isOpened()) selectedFAQ.setOpened(false);
            else selectedFAQ.setOpened(true);
            notifyDataSetChanged();
        }
    }

    @Override
    public Object getItem (int pos) {
       return mItems.get(pos);
    }

    @Override
    public int getCount () {
        if(mItems == null) return 0;
        else return mItems.size();
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
        DTableElement element = (DTableElement) getItem(position);

        if(element instanceof DTableFAQ) return ViewTypes.FAQ_TYPE.ordinal();
        else if(element instanceof DTableRoot) return ViewTypes.CAT_TYPE.ordinal();
        else if(element instanceof DTableChannel) return ViewTypes.ROOT_TYPE.ordinal();
        else return ViewTypes.TEXT_TYPE.ordinal();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch(viewTypes[getItemViewType(position)]) {
            case FAQ_TYPE:
                return getFAQView(position, convertView, parent);
            case CAT_TYPE:
                return getCategoryView(position, convertView, parent);
            case ROOT_TYPE:
            default:
                return getDefaultView(position, convertView, parent);
        }
    }

    /**
     * Basic row with a line of text. Will display the appropriate "local" title based on
     * user's home campus if home and away strings are specified.
     */
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

    /**
     * FAQ row, which displays a line of text and can be clicked on to open a pop-down which
     * displays further text.
     */
    public View getFAQView(int position, View convertView, ViewGroup parent) {
        FAQViewHolder holder;

        // If we aren't given a view, inflate one. Get special layout for sub-menu items.
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.row_dtable_popdown, null);
            holder = new FAQViewHolder();
            holder.popdownTextView = (TextView) convertView.findViewById(R.id.popdownTextView);
            holder.popdownLayout = (LinearLayout) convertView.findViewById(R.id.popdownLayout);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (FAQViewHolder) convertView.getTag();
        }

        DTableFAQ faqElement = (DTableFAQ) mItems.get(position);

        holder.titleTextView.setText(faqElement.getTitle(mHomeCampus));

        // Set pop-down contents
        holder.popdownTextView.setText(faqElement.getAnswer());

        // Toggle pop-down visibility
        if(faqElement.isOpened()) holder.popdownLayout.setVisibility(View.VISIBLE);
        else holder.popdownLayout.setVisibility(View.GONE);

        return convertView;
    }

}