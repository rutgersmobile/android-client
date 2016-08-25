package edu.rutgers.css.Rutgers.channels.athletics.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.athletics.AthleticsAPI;
import edu.rutgers.css.Rutgers.api.athletics.model.AthleticsGame;
import edu.rutgers.css.Rutgers.api.athletics.model.AthleticsGames;
import edu.rutgers.css.Rutgers.channels.athletics.model.AthleticsAdapter;
import edu.rutgers.css.Rutgers.ui.VerticalSpaceItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.DtableChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Show athletics scores
 */
public final class AthleticsDisplay extends DtableChannelFragment {

    public static final String HANDLE = "athletics";

    private boolean loading = false;
    private AthleticsAdapter adapter;
    private Subscription athleticsGamesSubscription;
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
        adapter = new AthleticsAdapter(getContext(), R.layout.row_athletics_game, new ArrayList<AthleticsGame>());

        final String resource = args.getString(ComponentFactory.ARG_DATA_TAG);
        if (resource == null) {
            AppUtils.showFailedLoadToast(getContext());
            return;
        }

        Single<AthleticsGames> athleticsGames = Single.fromCallable(new Callable<AthleticsGames>() {
            @Override
            public AthleticsGames call() throws Exception {
                return AthleticsAPI.getGames(resource);
            }
        });

        athleticsGamesSubscription = athleticsGames
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<AthleticsGames>() {
                    @Override
                    public void onSuccess(AthleticsGames value) {
                        reset();
                        adapter.addAll(value.getGames());
                    }

                    @Override
                    public void onError(Throwable error) {
                        reset();
                        AppUtils.showFailedLoadToast(getContext());
                    }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        athleticsGamesSubscription.unsubscribe();
    }

    private void reset() {
        loading = false;
        adapter.clear();
        hideProgressCircle();
    }
}
