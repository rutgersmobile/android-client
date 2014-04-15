package edu.rutgers.css.Rutgers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

public class DTable extends Fragment {
	
	private ListView mList;
	private static final String TAG = "DTable";
	private JSONArray mData;
	private String mURL;
	private String mAPI;
	
	private AQuery aq;

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
		
		getActivity().setTitle(args.getString("title"));
		
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
		public View getView(int position, View convertView, ViewGroup parent) {
			// If we aren't given a view, inflate one
			if (convertView == null) convertView = getActivity().getLayoutInflater().inflate(R.layout.dtable_row, null);
			
			// Configure the view for this crime
			JSONObject c = (JSONObject) getItem(position);
			
			TextView titleTextView = (TextView)convertView.findViewById(R.id.text);
			
			try {
				titleTextView.setText(c.getString("title"));
			} catch (JSONException e) {
				titleTextView.setText("object does not have a title property");
			}
				
			return convertView;
		}
	}

}