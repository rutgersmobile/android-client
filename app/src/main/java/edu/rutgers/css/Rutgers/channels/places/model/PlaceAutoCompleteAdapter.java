package edu.rutgers.css.Rutgers.channels.places.model;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.model.KeyValPair;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Adapter for place auto-completion results. The filter executes queries on the Places API and
 * the results are placed into the adapter so that they can be shown in some form of list.
 */
public class PlaceAutoCompleteAdapter extends ArrayAdapter<KeyValPair> {

    private static final String TAG = "PlaceAutoCompleteAdapter";

    private PlaceSearchFilter mFilter = new PlaceSearchFilter();
    private List<KeyValPair> mData = new ArrayList<>();

    public PlaceAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public KeyValPair getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class PlaceSearchFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            final FilterResults results = new FilterResults();
            results.values = null;
            results.count = 0;

            // Empty or null constraint returns nothing
            if (charSequence == null) return results;

            final String filterString = StringUtils.trim(charSequence.toString());

            if (StringUtils.isBlank(filterString)) return results;

            try {
                // Do request for search results. Cap at 15 results.
                List<Place> places = PlacesAPI.searchPlaces(filterString, 15);
                List<KeyValPair> keyValPairs = new ArrayList<>(places.size());
                for (Place place: places) {
                    keyValPairs.add(new KeyValPair(place.getId(), place.getTitle()));
                }

                results.values = keyValPairs;
                results.count = keyValPairs.size();
            } catch (JsonSyntaxException | IOException e) {
                LOGE(TAG, e.getMessage());
            }

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mData.clear();
            if (filterResults.values != null) mData.addAll((List<KeyValPair>) filterResults.values);
            notifyDataSetChanged();
        }
    }

}
