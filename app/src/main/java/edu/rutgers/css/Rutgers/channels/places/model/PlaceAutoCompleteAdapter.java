package edu.rutgers.css.Rutgers.channels.places.model;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.model.KeyValPair;

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
            if (charSequence == null || charSequence.toString().isEmpty()) return results;

            // Do request for search results
            Promise<List<Place>, Exception, Void> p = PlacesAPI.searchPlaces(charSequence.toString());
            p.done(new DoneCallback<List<Place>>() {
                @Override
                public void onDone(List<Place> result) {
                    List<KeyValPair> keyValPairs = new ArrayList<>(result.size());
                    for (Place place: result) {
                        keyValPairs.add(new KeyValPair(place.getId(), place.getTitle()));
                    }

                    results.values = keyValPairs;
                    results.count = keyValPairs.size();
                }
            }).fail(new FailCallback<Exception>() {
                @Override
                public void onFail(Exception result) {
                    Log.w(TAG, result.getMessage());
                }
            });

            // Wait for the request to finish before returning results
            try {
                p.waitSafely();
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mData.clear();
            if (filterResults.values != null) mData.addAll((List<KeyValPair>) filterResults.values);
            notifyDataSetChanged();
        }
    }

}
