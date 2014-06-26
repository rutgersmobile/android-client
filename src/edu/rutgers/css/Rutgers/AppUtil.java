package edu.rutgers.css.Rutgers;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

public class AppUtil {

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
	
}
