package edu.rutgers.css.Rutgers;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class JSONArrayAdapter implements ListAdapter {
	
	private static final String TAG = "JSONArrayAdapter";
	private JSONArray mSource;
	private int mResource;
	private Context mContext;
	
	public JSONArrayAdapter (Context context, JSONArray source, int resource) {
		mSource = source;
		mResource = resource;
		mContext = context;
	}
	
	public int getCount () {
		return mSource.length();
	}
	
	public Object getItem (int position) {
		try {
			return mSource.getJSONObject(position);
		} catch (JSONException e) {
			Log.e(TAG, "JSONArrayAdapter requires that all items are objects");
			return null;
		}
	}
	
	private boolean isTextView (View v) {
		try {
			TextView l = (TextView) v;
			return true;
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	private void setData (View destView, String data) {
		if (destView != null) {
			if (isTextView(destView)) {
				TextView t = (TextView) destView;
				t.setText(data);
			}
		}
	}
	
	public View getView (int position, View view, ViewGroup parent) {
		if (view == null) view = LayoutInflater.from(mContext).inflate(mResource, null);
		
		try {
			JSONObject data = (JSONObject) getItem(position);
			Iterator<?> keys = data.keys();
			Log.d(TAG, "generatign a view");
			// For each key in the json object, check if there's a resource in the view with the same id as the key.
			// If so, try to set its data to the data in the key
			while (keys.hasNext()) {
				String key = (String)keys.next();
				int destId = view.getResources().getIdentifier(key, "id", mContext.getPackageName());
				View destView = view.findViewById(destId);
				try {
					setData(destView, data.getString(key));
				} catch (JSONException e) {
					Log.e(TAG, "JSONArrayAdapter requires that keys map to strings");
				}
			}
		} catch (ClassCastException e) {
			String data = (String) getItem(position);
			View destView = view.findViewById(R.id.title);
			setData(destView, data);
		}
		
		return view;
	}
	
	public long getItemId (int position) { return (long) position; }
	public int getItemViewType (int position) { return 1; }
	public int getViewTypeCount () { return 1; }
	public boolean hasStableIds () { return true; }
	public boolean isEmpty () { return mSource.length() == 0; }
	public boolean isEnabled (int position) { return true; }
	public boolean areAllItemsEnabled () { return true; }
	public void registerDataSetObserver (DataSetObserver o) {}
	public void unregisterDataSetObserver (DataSetObserver o) {}
	
}
