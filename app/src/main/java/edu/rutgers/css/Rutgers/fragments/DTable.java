package edu.rutgers.css.Rutgers.fragments;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import edu.rutgers.css.Rutgers.AppUtil;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers2.R;

/**
 * Dynamic Table
 *
 */
public class DTable extends Fragment {
	
	private static final String TAG = "DTable";

    private static final int FAQ_TYPE = 2;
    private static final int CAT_TYPE = 1;
    private static final int DEF_TYPE = 0;

	private ListView mListView;
	private JSONArray mData;
	private JSONAdapter mAdapter;
	private String mURL;
	private String mAPI;
	private Context mContext;
    private String mHandle;
	
	private AQuery aq;

	public DTable() {
		// Required empty public constructor
	}

    /**
     * Get descriptive DTable tag to use for logging.
     * @return DTable tag combined with current handle, if possible
     */
    private String dTag() {
        if(mHandle != null) return TAG + "_" + mHandle;
        else return TAG;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		aq = new AQuery(mContext);

        if(savedInstanceState != null && savedInstanceState.getString("mData") != null) {
            mHandle = savedInstanceState.getString("mHandle");
            Log.v(dTag(), "Restoring mData");
            try {
                mData = new JSONArray(savedInstanceState.getString("mData"));
                mAdapter = new JSONAdapter(mData);
                return;
            } catch (JSONException e) {
                Log.w(dTag(), "onCreate(): " + e.getMessage());
            }
        }

        // Didn't restore adapter & data from state; initialize here
        mData = new JSONArray();
        mAdapter = new JSONAdapter(mData);

		Bundle args = getArguments();

        // Get handle for this DTable instance
        if(args.getString("handle") != null) mHandle = args.getString("handle");
        else if(args.getString("api") != null) mHandle = args.getString("api").replace(".txt","");
        else if(args.getString("title") != null) mHandle = args.getString("title");
        else mHandle = "null";

        // If a JSON array was provided in "data" field, load it
		if (args.getString("data") != null) {
			try {
				loadArray(new JSONArray(args.getString("data")));
			} catch (JSONException e) {
				Log.e(dTag(), "onCreateView(): " + e.getMessage());
			}
		}

        // Otherwise, check for URL or API argument
		else if (args.getString("url") != null) mURL = args.getString("url");
		else if (args.getString("api") != null) mAPI = args.getString("api");
		else {
            Log.e(dTag(), "DTable must have URL, API, or data in its arguments bundle");
            Toast.makeText(mContext, R.string.failed_internal, Toast.LENGTH_LONG).show();
        }
		
		if (mURL != null) {
            Request.json(mURL, Request.CACHE_NEVER).done(new AndroidDoneCallback<JSONObject>() {
                @Override
                public AndroidExecutionScope getExecutionScope() {
                    return AndroidExecutionScope.UI;
                }

                @Override
                public void onDone(JSONObject result) {
                    try {
                        loadArray(result.getJSONArray("children"));
                    } catch (JSONException e) {
                        Log.w(dTag(), "onCreateView(): " + e.getMessage());
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.failed_load), Toast.LENGTH_LONG).show();
                    }
                }
            }).fail(new AndroidFailCallback<AjaxStatus>() {
                @Override
                public AndroidExecutionScope getExecutionScope() {
                    return AndroidExecutionScope.UI;
                }

                @Override
                public void onFail(AjaxStatus status) {
                    Log.w(dTag(), AppUtil.formatAjaxStatus(status));
                    Toast.makeText(mContext, R.string.failed_load, Toast.LENGTH_LONG).show();
                }
            });
        }
        else if(mAPI != null) {
            Request.api(mAPI, Request.CACHE_NEVER).done(new AndroidDoneCallback<JSONObject>() {
                @Override
                public AndroidExecutionScope getExecutionScope() {
                    return AndroidExecutionScope.UI;
                }

                @Override
                public void onDone(JSONObject result) {
                    try {
                        loadArray(result.getJSONArray("children"));
                    }
                    catch (JSONException e) {
                        Log.w(dTag(), "onCreateView(): " + e.getMessage());
                        Toast.makeText(getActivity(), R.string.failed_load, Toast.LENGTH_LONG).show();
                    }
                }
            }).fail(new AndroidFailCallback<AjaxStatus>() {
                @Override
                public AndroidExecutionScope getExecutionScope() {
                    return AndroidExecutionScope.UI;
                }

                @Override
                public void onFail(AjaxStatus status) {
                    Log.w(dTag(), AppUtil.formatAjaxStatus(status));
                    Toast.makeText(mContext, R.string.failed_load, Toast.LENGTH_LONG).show();
                }
            });
        }
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_dtable, parent, false);
		mListView = (ListView) v.findViewById(R.id.dtable_list);

		Bundle args = getArguments();
		if(args.getString("title") != null) {
			getActivity().setTitle(args.getString("title"));
		}
		
		mListView.setAdapter(mAdapter);
		
		// Clicks on DTable item launch component in "view" field with arguments
		mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject clickedJson = (JSONObject) parent.getAdapter().getItem(position);
                Bundle args = new Bundle();

                try {
                    // This object has an array of more channels
                    if (mAdapter.getItemViewType(position) == CAT_TYPE) {
                        Log.v(dTag(), "Clicked \"" + AppUtil.getLocalTitle(mContext, clickedJson.get("title")) + "\"");
                        args.putString("component", "dtable");
                        args.putString("title", AppUtil.getLocalTitle(mContext, clickedJson.get("title")));
                        args.putString("data", clickedJson.getJSONArray("children").toString());
                    }
                    // This is a FAQ button
                    else if (mAdapter.getItemViewType(position) == FAQ_TYPE) {
                        // Toggle pop-down visibility
                        mAdapter.togglePopdown(position);
                        return;
                    }
                    // This object is a channel
                    else {
                        JSONObject channel = clickedJson.getJSONObject("channel");
                        Log.v(dTag(), "Clicked \"" + AppUtil.getLocalTitle(mContext, clickedJson.opt("title")) + "\"");

                        // Channel must have "title" field for title and "view" field to specify which fragment is going to be launched
                        args.putString("component", channel.getString("view"));

                        // First, attempt to get 'title' for the channel. If it's not set, fall back
                        // to the title set for the clickable item here. (They often match, but
                        // see rec.txt for an example of where they differ.)
                        Object title;
                        if(channel.has("title")) title = channel.get("title");
                        else if(clickedJson.has("title")) title = clickedJson.get("title");
                        else title = null;

                        if(title != null) args.putString("title", AppUtil.getLocalTitle(mContext, title));

                        // Add the rest of the JSON fields to the arg bundle
                        Iterator<String> keys = channel.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if(key.equalsIgnoreCase("title")) continue; // title was already handled above
                            Log.v(dTag(), "Adding to args: \"" + key + "\", \"" + channel.get(key).toString() + "\"");
                            args.putString(key, channel.get(key).toString()); // TODO Better handling of type mapped by "key"
                        }
                    }

                    ComponentFactory.getInstance().switchFragments(args);
                } catch (JSONException e) {
                    Log.w(dTag(), "onItemClick(): " + e.getMessage());
                }

            }

        });
		
		return v;
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If any data was actually loaded, save it in state
        if(mData != null && mData.length() > 0) {
            outState.putString("mData", mData.toString());
            outState.putString("mHandle", mHandle);
        }
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
				Log.w(dTag(), "loadArray(): " + e.getMessage());
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
            TextView popdownTextView;
            LinearLayout popdownLayout;
		}
		
		public JSONAdapter(JSONArray rows) {
            mItems = rows;
		}
		
		public void add(Object o) {
			mItems.put(o);
			this.notifyDataSetChanged();
		}

        public void togglePopdown(int position) {
            if(getItemViewType(position) != FAQ_TYPE) return;

            JSONObject selected = mItems.optJSONObject(position);
            if(selected != null) {
                try {
                    if(selected.optBoolean("popped") == false) selected.put("popped", true);
                    else selected.put("popped", false);
                    notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.w(dTag(), "togglePopdown(): " + e.getMessage());
                }
            }

        }
		
		@Override
		public Object getItem (int pos) {			
			try {
				return mItems.get(pos);
			} catch (JSONException e) {
				Log.w(dTag(), "getItem(): " + e.getMessage());
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
			JSONObject json = (JSONObject) getItem(position);

			// FAQ row
            if(json.has("answer")) return FAQ_TYPE;
			// Sub-menu row
		    else if(json.has("children") && json.opt("children") instanceof JSONArray) return CAT_TYPE;
            // Regular text thing
		    else return DEF_TYPE;
		}

		@Override
		public int getViewTypeCount() {
		    return 3;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String title;
			ViewHolder holder;
						
			// If we aren't given a view, inflate one. Get special layout for sub-menu items.
			if (convertView == null) {
				int res;

                switch(getItemViewType(position)) {
                    case FAQ_TYPE:
                        res = R.layout.dtable_popdown_row;
                        break;
                    case CAT_TYPE:
                        res = R.layout.category_row;
                        break;
                    case DEF_TYPE:
                    default:
                        res = R.layout.dtable_row;
                }

				convertView = getActivity().getLayoutInflater().inflate(res, null);
				holder = new ViewHolder();
				if(getItemViewType(position) == FAQ_TYPE) {
                    holder.titleTextView = (TextView) convertView.findViewById(R.id.mainTextView);
                    holder.popdownTextView = (TextView) convertView.findViewById(R.id.popdownTextView);
                    holder.popdownLayout = (LinearLayout) convertView.findViewById(R.id.popdownLayout);
                }
                else {
                    holder.titleTextView = (TextView) convertView.findViewById(R.id.text);
                }
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}

			JSONObject c = (JSONObject) getItem(position);
			title = AppUtil.getLocalTitle(mContext, c.opt("title"));

            holder.titleTextView.setText(title);

			if(getItemViewType(position) == FAQ_TYPE) {
                // Set pop-down contents
                holder.popdownTextView.setText(c.optString("answer"));

                // Toggle pop-down visibility
                if(c.optBoolean("popped")) holder.popdownLayout.setVisibility(View.VISIBLE);
                else holder.popdownLayout.setVisibility(View.GONE);
            }

			return convertView;
		}
	}

}