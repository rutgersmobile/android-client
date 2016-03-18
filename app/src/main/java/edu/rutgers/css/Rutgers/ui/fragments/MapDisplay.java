package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;

/**
 * Fragment for viewing OSM maps
 */
public class MapDisplay extends BaseDisplay {
    public static final String HANDLE = "maps";

    public static Bundle createArgs() {
        Bundle args = new Bundle();
        args.putString(ComponentFactory.ARG_HANDLE_TAG, MapDisplay.HANDLE);
        args.putString(ComponentFactory.ARG_COMPONENT_TAG, MapDisplay.HANDLE);
        return args;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_map, parent, false);
        final MapView map = (MapView) v.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        final IMapController mapController = map.getController();
        mapController.setZoom(9);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);
        return v;
    }
}
