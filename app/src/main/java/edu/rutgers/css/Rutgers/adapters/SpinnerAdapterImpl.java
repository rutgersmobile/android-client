package edu.rutgers.css.Rutgers.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by jamchamb on 9/2/14.
 */
public class SpinnerAdapterImpl<T> extends ArrayAdapter<T> {

    private Activity mContext;

    static class ViewHolder {
        TextView textView;
    }

    public SpinnerAdapterImpl(Activity context, int resource) {
        super(context, resource);
        mContext = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            LayoutInflater layoutInflater = mContext.getLayoutInflater();
            convertView = layoutInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(getItem(position).toString());

        return convertView;
    }

}
