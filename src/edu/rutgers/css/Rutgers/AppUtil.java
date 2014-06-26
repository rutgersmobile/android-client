package edu.rutgers.css.Rutgers;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.rutgers.css.Rutgers2.R;

public class AppUtil {

	private static final String TAG = "AppUtil";
	
	public static String getFullCampusTitle(Context context, String campusTag) {
		if(campusTag == null) return null;
		
		Resources res = context.getResources();
		
		try {
			int id = res.getIdentifier("campus_"+campusTag+"_full", "string", "edu.rutgers.css.Rutgers2");
			return res.getString(id);
		} catch (NotFoundException e) {
			return null;
		}
	}
	
	/**
	 * In cases where multiple titles are specified ("homeTitle", "foreignTitle"), gets appropriate title
	 * according to user configuration.
	 * @param title String or JSONObject returned by get("title") on channel JSONObject
	 * @return Appropriate title to display
	 */
	public static String getLocalTitle(Context context, Object title) {
		if(title == null) {
			return "(No title)";
		}
		// "title" is just a string - nothing to do
		else if(title.getClass() == String.class) {
			return (String) title;
		}
		// "title" is a JSON Object - figure out which string to display
		else if(title.getClass() == JSONObject.class) {
			JSONObject titles = (JSONObject) title;
			
			// Get the full name of the user's home campus (default to NB if there's no config)
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			String userHome = AppUtil.getFullCampusTitle(context, sharedPref.getString("campus_list", context.getResources().getString(R.string.campus_nb_tag)));
			Log.v(TAG, "User home: " + userHome);
			
			try {
				String titleHome = titles.getString("homeCampus");
				// If user config home matches title's home, show home title
				if(titleHome.equalsIgnoreCase(userHome)) {
					return titles.getString("homeTitle");
				}
				// If not, use foreign title
				else {
					return titles.getString("foreignTitle");
				}
			} catch (JSONException e) {
				Log.w(TAG, "getLocalTitle(): " + e.getMessage());
				Log.w(TAG, "title JSON: " + title.toString());
				return title.toString();
			}
		}
		
		return null;
	}

}
