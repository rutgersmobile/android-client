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

import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Adapter for making menus from JSON arrays.
 */
public class JSONAdapter extends BaseAdapter {

    private final static String TAG = "JSONAdapter";

    private Context mContext;
    private JSONArray mItems;

    public static enum ViewTypes {
        DEF_TYPE, CAT_TYPE, FAQ_TYPE;
    }

    private class ViewHolder {
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
     * @param in JSON array to copy
     */
    public void loadArray(JSONArray in) {
        if(in == null) return;

        for(int i = 0; i < in.length(); i++) {
            try {
                this.add(in.get(i));
            } catch (JSONException e) {
                Log.w(TAG, "loadArray(): " + e.getMessage());
            }
        }
    }

    /**
     * Add an object to the JSON array.
     * @param o JSON Object to add
     */
    public void add(Object o) {
        if(o instanceof JSONObject) {
            mItems.put(o);
            this.notifyDataSetChanged();
        }
        else {
            Log.e(TAG, "Tried to add non-JSON object to JSON array");
            throw new IllegalArgumentException("JSONAdapter only accepts JSON Objects");
        }
    }

    /**
     * Toggle DTable row pop-down view
     * @param position
     */
    public void togglePopdown(int position) {
        if(getItemViewType(position) != ViewTypes.FAQ_TYPE.ordinal()) return;

        JSONObject selected = mItems.optJSONObject(position);
        if(selected != null) {
            try {
                if(selected.optBoolean("popped") == false) selected.put("popped", true);
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
    public int getViewTypeCount() {
        return ViewTypes.values().length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String title;
        ViewHolder holder;

        // If we aren't given a view, inflate one. Get special layout for sub-menu items.
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int res;

            if(getItemViewType(position) == ViewTypes.FAQ_TYPE.ordinal()) res = R.layout.row_dtable_popdown;
            else if(getItemViewType(position) == ViewTypes.CAT_TYPE.ordinal()) res = R.layout.row_category;
            else res = R.layout.row_title;

            convertView = layoutInflater.inflate(res, null);
            holder = new ViewHolder();
            if(getItemViewType(position) == ViewTypes.FAQ_TYPE.ordinal()) {
                holder.popdownTextView = (TextView) convertView.findViewById(R.id.popdownTextView);
                holder.popdownLayout = (LinearLayout) convertView.findViewById(R.id.popdownLayout);
            }

            holder.titleTextView = (TextView) convertView.findViewById(R.id.title);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject c = (JSONObject) getItem(position);
        title = AppUtil.getLocalTitle(mContext, c.opt("title"));

        holder.titleTextView.setText(title);

        if(getItemViewType(position) == ViewTypes.FAQ_TYPE.ordinal()) {
            // Set pop-down contents
            holder.popdownTextView.setText(c.optString("answer"));

            // Toggle pop-down visibility
            if(c.optBoolean("popped")) holder.popdownLayout.setVisibility(View.VISIBLE);
            else holder.popdownLayout.setVisibility(View.GONE);
        }

        return convertView;
    }
}