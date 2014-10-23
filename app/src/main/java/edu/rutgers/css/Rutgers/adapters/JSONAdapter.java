package edu.rutgers.css.Rutgers.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers.utils.RutgersUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Adapter for making menus from JSON arrays.
 */
public class JSONAdapter extends BaseAdapter {

    private final static String TAG = "JSONAdapter";

    private Context mContext;
    private JSONArray mItems;

    public static enum ViewTypes {
        DEF_TYPE, CAT_TYPE, FAQ_TYPE
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

    public JSONAdapter(Context context, JSONArray items) {
        mContext = context;
        mItems = items;
    }

    /**
     * Copy a JSON array to the member array
     */
    public void loadArray(JSONArray jsonArray) {
        if(jsonArray == null) return;

        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                this.add(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                Log.w(TAG, "loadArray(): " + e.getMessage());
            }
        }
    }

    /**
     * Add an object to the JSON array.
     */
    public void add(JSONObject o) {
        mItems.put(o);
        notifyDataSetChanged();
    }

    /**
     * Toggle DTable row pop-down view
     */
    public void togglePopdown(int position) {
        if(getItemViewType(position) != ViewTypes.FAQ_TYPE.ordinal()) return;

        JSONObject selected = mItems.optJSONObject(position);
        if(selected != null) {
            try {
                if(!selected.optBoolean("popped")) selected.put("popped", true);
                else selected.put("popped", false);
                notifyDataSetChanged();
            } catch (JSONException e) {
                Log.w(TAG, "togglePopdown(): " + e.getMessage());
            }
        }

    }

    @Override
    public Object getItem (int pos) {
       return mItems.opt(pos);
    }

    @Override
    public int getCount () {
        if(mItems == null) return 0;
        else return mItems.length();
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
        JSONObject json = (JSONObject) getItem(position);
        if(json == null) throw new IllegalArgumentException("Invalid index");

        // FAQ row
        if(json.has("answer")) return ViewTypes.FAQ_TYPE.ordinal();
        // Sub-menu row
        else if(json.has("children") && json.opt("children") instanceof JSONArray) return ViewTypes.CAT_TYPE.ordinal();
        // Regular text row
        else return ViewTypes.DEF_TYPE.ordinal();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch(viewTypes[getItemViewType(position)]) {
            case FAQ_TYPE:
                return getFAQView(position, convertView, parent);
            case CAT_TYPE:
                return getCategoryView(position, convertView, parent);
            case DEF_TYPE:
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

        JSONObject jsonObject = (JSONObject) getItem(position);
        String title = RutgersUtil.getLocalTitle(mContext, jsonObject.opt("title"));
        holder.titleTextView.setText(title);
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

        JSONObject jsonObject = (JSONObject) getItem(position);
        String title = RutgersUtil.getLocalTitle(mContext, jsonObject.opt("title"));
        holder.titleTextView.setText(title);
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

        JSONObject jsonObject = (JSONObject) getItem(position);
        String title = RutgersUtil.getLocalTitle(mContext, jsonObject.opt("title"));

        holder.titleTextView.setText(title);

        // Set pop-down contents
        holder.popdownTextView.setText(jsonObject.optString("answer"));

        // Toggle pop-down visibility
        if(jsonObject.optBoolean("popped")) holder.popdownLayout.setVisibility(View.VISIBLE);
        else holder.popdownLayout.setVisibility(View.GONE);

        return convertView;
    }

}