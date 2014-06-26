package edu.rutgers.css.Rutgers.fragments;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import edu.rutgers.css.Rutgers.AppUtil;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers2.R;

/**
 * Dynamic Table
 *
 */
public class DTable extends Fragment {
	
	private static final String TAG = "DTable";
	
	private ListView mList;
	private JSONArray mData;
	private JSONAdapter mAdapter;
	private String mURL;
	private String mAPI;
	private Context mContext;
	
	private AQuery aq;

	public DTable() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		aq = new AQuery(mContext);
		mData = new JSONArray();
		mAdapter = new JSONAdapter(mData);
		
		Bundle args = getArguments();
		if (args.get("data") != null) {
			try {
				loadArray(new JSONArray(args.getString("data")));
			} catch (JSONException e) {
				Log.e(TAG, "onCreateView(): " + e.getMessage());
			}
		}
		else if (args.get("url") != null) mURL = args.getString("url");
		else if (args.get("api") != null) mAPI = args.getString("api");
		else throw new IllegalStateException("DTable must have either a url or data in its arguments bundle");
		
		if (mURL != null) aq.ajax(mURL, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
            	try {
            		loadArray(json.getJSONArray("children"));
            	} catch (JSONException e) {
            		Log.w(TAG, "onCreateView(): " + e.getMessage());
            		Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.failed_load), Toast.LENGTH_LONG).show();
            	}
            }
            
		});
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_dtable, parent, false);
		mList = (ListView) v.findViewById(R.id.dtable_list);

		Bundle args = getArguments();
		if(args.getString("title") != null) {
			getActivity().setTitle(args.getString("title"));
		}
		
		mList.setAdapter(mAdapter);
		
		// Clicks on DTable item launch component in "view" field with arguments
		mList.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {				
				JSONObject clickedJson = (JSONObject) parent.getAdapter().getItem(position);
				Bundle args = new Bundle();

				try {	
					// This object has an array of more channels
					if(clickedJson.has("children")) {							
						Log.v(TAG, "Clicked \"" + AppUtil.getLocalTitle(mContext, clickedJson.get("title")) + "\"");
						args.putString("component", "dtable");
						args.putString("title", AppUtil.getLocalTitle(mContext, clickedJson.get("title")));
						args.putString("data", clickedJson.getJSONArray("children").toString());
					}
					// This object is a channel
					else {
						JSONObject channel = (JSONObject) clickedJson.getJSONObject("channel");
						Log.v(TAG, "Clicked \"" + AppUtil.getLocalTitle(mContext, channel.get("title")) + "\"");
						
						// Channel must have "title" field for title and "view" field to specify which fragment is going to be launched
						args.putString("component", channel.getString("view"));
						
						Iterator<String> keys = channel.keys();
						while(keys.hasNext()) {
							String key = keys.next();
							Log.v(TAG, "Adding to args: \"" + key + "\", \"" + channel.get(key).toString() + "\"");
							args.putString(key, channel.get(key).toString()); // TODO Better handling of type mapped by "key"
						}
					}
					
					ComponentFactory.getInstance().switchFragments(args);
				} catch (JSONException e) {
					Log.w(TAG, "onItemClick(): " + e.getMessage());
				}
				
			}
			
		});
		
		return v;
	}

	/**
	 * Copy a JSON array to the member array
	 * @param in JSON array to copy
	 */
	private void loadArray(JSONArray in) {
		if(in == null) return;
		
		for(int i = 0; i < in.length(); i++) {
			try {
				mAdapter.add(in.get(i));
			} catch (JSONException e) {
				Log.w(TAG, "loadArray(): " + e.getMessage());
			}
		}
	}
	
	/**
	 * Private adapter to make menus from JSON data
	 */
	private class JSONAdapter extends BaseAdapter {
		private JSONArray mItems;
	
		private class ViewHolder {
			TextView titleTextView;
		}
		
		public JSONAdapter(JSONArray rows) {
			mItems = rows;
		}
		
		public void add(Object o) {
			mItems.put(o);
			this.notifyDataSetChanged();
		}
		
		@Override
		public Object getItem (int pos) {			
			try {
				return mItems.get(pos);
			} catch (JSONException e) {
				Log.w(TAG, "getItem(): " + e.getMessage());
				return null;
			}
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
			// Sub-menu row
		    if(((JSONObject)getItem(position)).has("children"))
		    	return 1;
		    else
		    	return 0;
		}

		@Override
		public int getViewTypeCount() {
		    return 2;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String title;
			ViewHolder holder;
						
			// If we aren't given a view, inflate one. Get special layout for sub-menu items.
			if (convertView == null) {
				int res;
				
				if(getItemViewType(position) == 1) res = R.layout.category_row;
				else res = R.layout.dtable_row; 

				convertView = getActivity().getLayoutInflater().inflate(res, null);
				holder = new ViewHolder();
				holder.titleTextView = (TextView) convertView.findViewById(R.id.text);
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}

			JSONObject c = (JSONObject) getItem(position);
			title = AppUtil.getLocalTitle(mContext, c.opt("title"));
			
			holder.titleTextView.setText(title);
				
			return convertView;
		}
	}

}