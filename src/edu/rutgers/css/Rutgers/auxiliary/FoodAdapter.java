package edu.rutgers.css.Rutgers.auxiliary;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import edu.rutgers.css.Rutgers2.R;

/**
 * Array adapter extended for food menu items
 *
 */
public class FoodAdapter extends ArrayAdapter<FoodItem> {

	private int layoutResource;
	private int categoryResource;
	
	public FoodAdapter(Context context, int itemResource, int categoryResource, List<FoodItem> objects) {
		super(context, itemResource, objects);
		
		this.layoutResource = itemResource;
		this.categoryResource = categoryResource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		FoodItem curItem = this.getItem(position);
		
		if(convertView == null) {
				if(getItemViewType(position) == 1)
					convertView = mLayoutInflater.inflate(this.categoryResource, null);
				else
					convertView = mLayoutInflater.inflate(this.layoutResource, null);
		}

		TextView titleTextView = (TextView) convertView.findViewById(R.id.title);
		titleTextView.setText(curItem.getTitle());
		
		return convertView;
	}
	
	@Override
	public int getItemViewType(int position) {
	    if(getItem(position).getIsCategory())
	    	return 1;
	    else
	    	return 0;
	}

	@Override
	public int getViewTypeCount() {
	    return 2;
	}
	
}
