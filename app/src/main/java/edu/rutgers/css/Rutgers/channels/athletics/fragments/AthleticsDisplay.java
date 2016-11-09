package edu.rutgers.css.Rutgers.channels.athletics.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.athletics.model.AthleticsAdapter;
import edu.rutgers.css.Rutgers.api.RutgersAPI;
import edu.rutgers.css.Rutgers.ui.VerticalSpaceItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.DtableChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Show service scores
 */
public final class AthleticsDisplay extends DtableChannelFragment {

    public static final String HANDLE = "athletics";

    private boolean loading = false;
    private AthleticsAdapter adapter;
    private String title;

    public static Bundle createArgs(@NonNull String title, @NonNull String resource) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, AthleticsDisplay.HANDLE);
        bundle.putString(ComponentFactory.ARG_TITLE_TAG, title);
        bundle.putString(ComponentFactory.ARG_DATA_TAG, resource);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        title = args.getString(ComponentFactory.ARG_TITLE_TAG);
        if (title != null) {
            getActivity().setTitle(title);
        }
        adapter = new AthleticsAdapter(getContext(), R.layout.row_athletics_game, new ArrayList<>());

        final String sport = args.getString(ComponentFactory.ARG_DATA_TAG);
        if (sport == null) {
            AppUtils.showFailedLoadToast(getContext());
            return;
        }

        RutgersAPI.getAthleticsGames(sport)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe(value -> {
                reset();
                adapter.addAll(value.getGames());
            }, error -> {
                reset();
                LOGE(TAG, error.getMessage());
                AppUtils.showFailedLoadToast(getContext());
            });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_recycler_progress);

        if (loading) showProgressCircle();

        if (title != null) {
            getActivity().setTitle(title);
        }

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(24));
        recyclerView.setAdapter(adapter);

        return v;
    }

    private void reset() {
        loading = false;
        adapter.clear();
        hideProgressCircle();
    }
}
