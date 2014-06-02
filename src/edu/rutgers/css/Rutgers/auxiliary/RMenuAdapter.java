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
 * Array adapter extended for menus with items and section headers.
 *
 */
public class RMenuAdapter<E extends RMenuPart> extends ArrayAdapter<E> {

	private int itemResource;
	private int categoryResource;
	
	public RMenuAdapter(Context context, int itemResource, int categoryResource, List<E> objects) {
		super(context, itemResource, objects);
		
		this.itemResource = itemResource;
		this.categoryResource = categoryResource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mLayoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		E curItem = this.getItem(position);
		
		if(convertView == null) {
			if(getItemViewType(position) == 1) convertView = mLayoutInflater.inflate(this.categoryResource, null);
			else convertView = mLayoutInflater.inflate(this.itemResource, null);
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
