package edu.rutgers.css.Rutgers.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.Serializable;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import lombok.Data;

/**
 * Fragment for viewing OSM maps
 */
public class MapDisplay extends BaseDisplay {
    public static final String HANDLE = "maps";
    private static final int STORAGE_REQUEST = 1;
    private static final String ARG_POINT = "mapPoint";

    MapView map;
    MapPoint center;

    public static OnlineTileSourceBase rutgersTileSource = new XYTileSource("rutgers",
            0, 18, 256, ".png", new String [] { "http://sauron.rutgers.edu/tiles/" });

    @Data
    public static final class MapPoint implements Serializable {
        final double x;
        final double y;
        final int z;

        public GeoPoint getGeoPoint() {
            return new GeoPoint(x, y);
        }
    }

    public static Bundle createArgs() {
        Bundle args = new Bundle();
        args.putString(ComponentFactory.ARG_HANDLE_TAG, MapDisplay.HANDLE);
        args.putString(ComponentFactory.ARG_COMPONENT_TAG, MapDisplay.HANDLE);
        return args;
    }

    public static Bundle createArgs(@NonNull final MapPoint point) {
        Bundle args = createArgs();
        args.putSerializable(ARG_POINT, point);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        center = (MapPoint) args.getSerializable(ARG_POINT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_map, parent, false);

        final Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            ((MainActivity) getActivity()).syncDrawer();
        }

        getActivity().setTitle("National Tiles");

        map = (MapView) v.findViewById(R.id.map);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, STORAGE_REQUEST);
        } else {
            setUpMap();
        }
        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == STORAGE_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setUpMap();
        }
    }

    private void setUpMap() {
        if (map != null) {
            map.setTileSource(rutgersTileSource);
            map.setMultiTouchControls(true);
            if (center != null) {
                IMapController mapController = map.getController();
                mapController.setZoom(center.getZ());
                mapController.setCenter(center.getGeoPoint());
            }
        }
    }
}
