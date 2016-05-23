package edu.rutgers.css.Rutgers.channels.athletics.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.athletics.model.AthleticsGame;
import edu.rutgers.css.Rutgers.api.athletics.model.AthleticsGames;
import edu.rutgers.css.Rutgers.channels.athletics.model.AthleticsAdapter;
import edu.rutgers.css.Rutgers.channels.athletics.model.loader.AthleticsGamesLoader;
import edu.rutgers.css.Rutgers.ui.fragments.DtableChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;

/**
 * Show athletics scores
 */
public class AthleticsDisplay extends DtableChannelFragment implements LoaderManager.LoaderCallbacks<AthleticsGames> {

    public static final String HANDLE = "athletics";
    private static final int LOADER_ID = AppUtils.getUniqueLoaderId();

    private boolean loading = false;
    private AthleticsAdapter adapter;

    public static Bundle createArgs(@NonNull String title, @NonNull String url) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, AthleticsDisplay.HANDLE);
        bundle.putString(ComponentFactory.ARG_TITLE_TAG, title);
        bundle.putString(ComponentFactory.ARG_URL_TAG, url);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        final String title = args.getString(ComponentFactory.ARG_TITLE_TAG);
        if (title != null) {
            getActivity().setTitle(title);
        }
        adapter = new AthleticsAdapter(getContext(), R.layout.row_athletics_game, new ArrayList<AthleticsGame>());

        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, args, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_list_progress);

        if (loading) showProgressCircle();

        final Bundle args = getArguments();
        final String title = args.getString(ComponentFactory.ARG_TITLE_TAG);
        if (title != null) {
            getActivity().setTitle(title);
        }

        final ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(adapter);

        return v;
    }

    @Override
    public Loader<AthleticsGames> onCreateLoader(int id, Bundle args) {
        final String resource = args.getString(ComponentFactory.ARG_DATA_TAG);
        if (resource != null) {
            return new AthleticsGamesLoader(getContext(), resource);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<AthleticsGames> loader, AthleticsGames data) {
        if (data == null) {
            AppUtils.showFailedLoadToast(getContext());
        }

        loading = false;
        hideProgressCircle();
        adapter.clear();
        if (data != null) {
            adapter.addAll(data.getGames());
        }
    }

    @Override
    public void onLoaderReset(Loader<AthleticsGames> loader) {
        adapter.clear();
        loading = false;
        hideProgressCircle();
    }
}
