package edu.rutgers.css.Rutgers.channels.bus.model;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.jdeferred.DeferredManager;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

/**
 * AsyncTaskLoader that gets all Nextbus info.
 */
public class NextbusAllLoader extends AsyncTaskLoader<List<SimpleSection<NextbusItem>>> {

    private static final String TAG = "NextbusLoader";

    private List<SimpleSection<NextbusItem>> mData;

    public NextbusAllLoader(Context context) {
        super(context);
    }

    @Override
    public List<SimpleSection<NextbusItem>> loadInBackground() {
        final List<SimpleSection<NextbusItem>> simpleSections = new ArrayList<>();

        // Get home campus for result ordering
        String userHome = RutgersUtils.getHomeCampus(getContext());
        final boolean nbHome = userHome.equals(getContext().getString(R.string.campus_nb_full));

        // Get promises for all route & stop information
        final Promise<List<RouteStub>, Exception, Void> nbRoutes = NextbusAPI.getAllRoutes(NextbusAPI.AGENCY_NB);
        final Promise<List<StopStub>, Exception, Void> nbStops = NextbusAPI.getAllStops(NextbusAPI.AGENCY_NB);
        final Promise<List<RouteStub>, Exception, Void> nwkRoutes = NextbusAPI.getAllRoutes(NextbusAPI.AGENCY_NWK);
        final Promise<List<StopStub>, Exception, Void> nwkStops = NextbusAPI.getAllStops(NextbusAPI.AGENCY_NWK);

        // Synchronized load of all route & stop information
        DeferredManager dm = new DefaultDeferredManager();
        try {
            dm.when(nbRoutes, nbStops, nwkRoutes, nwkStops).done(new DoneCallback<MultipleResults>() {

                @Override
                public void onDone(MultipleResults results) {
                    List<NextbusItem> nbRoutesResult = (List<NextbusItem>) results.get(0).getResult();
                    List<NextbusItem> nbStopsResult = (List<NextbusItem>) results.get(1).getResult();
                    List<NextbusItem> nwkRoutesResult = (List<NextbusItem>) results.get(2).getResult();
                    List<NextbusItem> nwkStopsResult = (List<NextbusItem>) results.get(3).getResult();

                    if (nbHome) {
                        simpleSections.add(new SimpleSection<>(getContext().getString(R.string.bus_nb_all_routes_header), nbRoutesResult));
                        simpleSections.add(new SimpleSection<>(getContext().getString(R.string.bus_nb_all_stops_header), nbStopsResult));
                        simpleSections.add(new SimpleSection<>(getContext().getString(R.string.bus_nwk_all_routes_header), nwkRoutesResult));
                        simpleSections.add(new SimpleSection<>(getContext().getString(R.string.bus_nwk_all_stops_header), nwkStopsResult));
                    } else {
                        simpleSections.add(new SimpleSection<>(getContext().getString(R.string.bus_nwk_all_routes_header), nwkRoutesResult));
                        simpleSections.add(new SimpleSection<>(getContext().getString(R.string.bus_nwk_all_stops_header), nwkStopsResult));
                        simpleSections.add(new SimpleSection<>(getContext().getString(R.string.bus_nb_all_routes_header), nbRoutesResult));
                        simpleSections.add(new SimpleSection<>(getContext().getString(R.string.bus_nb_all_stops_header), nbStopsResult));
                    }
                }

            }).fail(new FailCallback<OneReject>() {

                @Override
                public void onFail(OneReject result) {
                    Exception e = (Exception) result.getReject();
                    Log.e(TAG, e.getMessage()); // TODO Toast on UI thread
                }

            }).waitSafely();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }

        return simpleSections;
    }

    @Override
    public void deliverResult(List<SimpleSection<NextbusItem>> simpleSections) {
        if (isReset()) {
            Log.d(TAG, "Received async query while loader was reset");
            return;
        } else if (isStarted()) {
            List<SimpleSection<NextbusItem>> oldItems = mData; // keep ref to old
            mData = simpleSections;
            super.deliverResult(mData);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        mData = null;
    }

}
