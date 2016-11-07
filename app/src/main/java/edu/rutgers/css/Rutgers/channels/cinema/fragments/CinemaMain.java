package edu.rutgers.css.Rutgers.channels.cinema.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.cinema.model.CinemaAdapter;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.api.RutgersAPI;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Main cinema fragment
 */

public class CinemaMain extends BaseChannelFragment {
    private static final String TAG = "CinemaMain";
    public static final String HANDLE = "cinema";

    private CinemaAdapter adapter;

    private boolean mLoading = false;

    public Bundle createArgs() {
        final Bundle args = new Bundle();
        args.putString(ComponentFactory.ARG_COMPONENT_TAG, CinemaMain.HANDLE);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Cinema");
        adapter = new CinemaAdapter(new ArrayList<>());
        RutgersAPI.getMovies()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe(movies -> {
                reset();
                adapter.addAll(movies);
            }, error -> {
                reset();
                LOGE(TAG, error.getMessage());
                AppUtils.showFailedLoadToast(getContext());
            });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_recycler_progress);
        if (mLoading) showProgressCircle();

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setAdapter(adapter);

        return v;
    }

    @Override
    public Link getLink() {
        return null;
    }

    private void reset() {
        adapter.clear();
        mLoading = false;
        hideProgressCircle();
    }
}
