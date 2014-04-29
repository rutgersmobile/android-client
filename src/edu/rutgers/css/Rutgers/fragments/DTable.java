package edu.rutgers.css.Rutgers.fragments;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers2.R;

/**
 * Dynamic Table
 *
 */
public class DTable extends Fragment {
	
	private ListView mList;
	private static final String TAG = "DTable";
	private JSONArray mData;
	private String mURL;
	private String mAPI;
	
	private AQuery aq;

	public DTable() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		aq = new AQuery(this.getActivity());
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_dtable, parent, false);
		mList = (ListView) v.findViewById(R.id.dtable_list);
		
		Bundle args = getArguments();
		
		if(args.get("title") != null) {
			getActivity().setTitle(args.getString("title"));
		}
		
		try {

			if (args.get("data") != null) mData = new JSONArray(args.getString("data"));
			else if (args.get("url") != null) mURL = args.getString("url");
			else if (args.get("api") != null) mAPI = args.getString("api");
			else throw new IllegalStateException("DTable must have either a url or data in its arguments bundle");

			/*
			AsyncHttpClient client = new AsyncHttpClient();
			// If there's a URL, make a request
			if (mURL != null) client.get(mURL, new AsyncHttpResponseHandler() {
				
				@Override
				public void onSuccess (int status, Header[] headers, byte[] bytes) {
					String r = new String(bytes);
					try {
						mData = new JSONArray(r);
						setupList();
					} catch (JSONException e) {
						onFailure(-1, null, null, e);
					}
				}
				
				@Override
				public void onFailure (int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Request failed, making toast");
					Toast.makeText(getActivity(), "Getting data failed. Please try again later.", Toast.LENGTH_LONG).show();
				}
			});
			
			// Otherwise just setup the table
			else setupList();
			*/
			
			if (mURL != null) aq.ajax(mURL, JSONObject.class, new AjaxCallback<JSONObject>() {

                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                	try {
                		DTable.this.mData = json.getJSONArray("children");
                		setupList();
                	} catch (Exception e) {
                		e.printStackTrace();
                		Toast.makeText(getActivity(), "Getting data failed. Please try again later.", Toast.LENGTH_LONG).show();
                	}
                }
                
			});
			
			else setupList();
			
		} catch (JSONException e) {
			Log.e(TAG, "Bad json passed to dtable in data argument");
		}
		return v;
	}
	
	private void setupList () {
		JSONAdapter a = new JSONAdapter(mData);
		
		mList.setAdapter(a);
		
		/* Clicks on DTable item launch component in "view" field with arguments */
		mList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {				
					JSONObject c = (JSONObject) parent.getAdapter().getItem(position);
					Bundle args = new Bundle();
					FragmentManager fm = getActivity().getSupportFragmentManager();	

					try {	
						// This object has an array of more channels
						if(c.has("children")) {							
							Log.d(TAG, "Clicked \"" + getLocalTitle(c.get("title")) + "\"");
							args.putString("component", "dtable");
							args.putString("title", getLocalTitle(c.get("title")));
							args.putString("data", c.getJSONArray("children").toString());
						}
						// This object is a channel
						else {
							JSONObject channel = (JSONObject) c.getJSONObject("channel");
							Log.d(TAG, "Clicked \"" + getLocalTitle(channel.get("title")) + "\"");
							
							// Channel must have "title" field for title and "view" field to specify which fragment is going to be launched
							// TODO Should ComponentFactory take "view" argument instead of "component" argument to avoid this?
							args.putString("component", channel.getString("view"));
							
							Iterator<String> keys = channel.keys();
							while(keys.hasNext()) {
								String key = keys.next();
								Log.d(TAG, "Adding to args: \"" + key + "\", \"" + channel.get(key).toString() + "\"");
								args.putString(key, channel.get(key).toString()); // TODO Better handling of type mapped by "key"
							}
						}
						
						Fragment fragment = ComponentFactory.getInstance().createFragment(args);
						if(fragment == null) {
							Log.e(TAG, "Failed to create component");
							return;
						}
						fm.beginTransaction()
							.replace(R.id.main_content_frame, fragment)
							.addToBackStack(null)
							.commit(); 
						
					} catch (JSONException e) {
						Log.e(TAG, "JSONException: " + e.getMessage());
					}
					
				}
		});
	}

	/**
	 * In cases where multiple titles are specified ("homeTitle", "foreignTitle"), gets appropriate title
	 * according to configuration.
	 * TODO Update this when configuration by location is available. Just grabs "homeTitle" now when applicable.
	 * @param title String or JSONObject returned by get("title") on channel JSONObject
	 * @return Appropriate title to display
	 */
	private String getLocalTitle(Object title) {
		if(title.getClass() == String.class) {
			return (String) title;
		}
		else if(title.getClass() == JSONObject.class) {
			try {
				return ((JSONObject)title).getString("homeTitle");
			} catch (JSONException e) {
				return null;
			}
		}
		return null;
	}
	
	private class JSONAdapter extends BaseAdapter {
		private JSONArray mItems;
		
		public JSONAdapter(JSONArray rows) {
			mItems = rows;
		}
		
		@Override
		public Object getItem (int pos) {
			try {
				return mItems.get(pos);
			} catch (JSONException e) { 
				return null;
			}
		}
		
		@Override
		public int getCount () {
			return mItems.length();
		}
		
		@Override
		public long getItemId (int id) {
			return id;
		}
		
		@Override
		public int getItemViewType(int position) {
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
			
			// Configure the view for this crime
			JSONObject c = (JSONObject) getItem(position);
						
			// If we aren't given a view, inflate one. Get special layout for category items.
			if (convertView == null) {
				if(getItemViewType(position) == 1)
					convertView = getActivity().getLayoutInflater().inflate(R.layout.category_row, null);
				else
					convertView = getActivity().getLayoutInflater().inflate(R.layout.dtable_row, null);
			}
			
			try {
				title = getLocalTitle(c.get("title"));
			} catch (JSONException e) {
				title = "object does not have a title property";
			}
			
			TextView titleTextView = (TextView)convertView.findViewById(R.id.text);
			titleTextView.setText(title);
				
			return convertView;
		}
	}

}