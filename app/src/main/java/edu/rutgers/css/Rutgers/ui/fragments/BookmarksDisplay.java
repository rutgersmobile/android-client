package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.model.BookmarkAdapter;
import edu.rutgers.css.Rutgers.ui.BookmarkItemTouchHelperCallback;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.ui.OnStartDragListener;
import io.github.yavski.fabspeeddial.FabSpeedDial;

/**
 * Fragment for bookmark management
 */
public class BookmarksDisplay extends BaseDisplay implements OnStartDragListener {
    public static final String HANDLE = "bookmarks";
    private BookmarkAdapter adapter;
    private ItemTouchHelper itemTouchHelper;

    public static Bundle createArgs() {
        Bundle args = new Bundle();
        args.putString(ComponentFactory.ARG_HANDLE_TAG, BookmarksDisplay.HANDLE);
        args.putString(ComponentFactory.ARG_COMPONENT_TAG, BookmarksDisplay.HANDLE);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        adapter = new BookmarkAdapter(
            getContext(),
            R.layout.row_removable_constraint_bookmark,
            R.layout.row_constraint_bookmark,
            R.layout.row_divider,
            this
        );
        adapter.addFromPrefs();
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_recycler_progress, parent, false);

        getActivity().setTitle("Bookmarks");

        final FabSpeedDial fab = (FabSpeedDial) v.findViewById(R.id.fab_speed_dial);

        fab.setVisibility(View.GONE);

        final Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            ((MainActivity) getActivity()).syncDrawer();
        }

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        itemTouchHelper = new ItemTouchHelper(new BookmarkItemTouchHelperCallback(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bookmark_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle options button
        if (item.getItemId() == R.id.bookmark_enable_all) {
            adapter.enableAll();
            return true;
        } else if (item.getItemId() == R.id.bookmark_disable_all) {
            adapter.disableAll();
            return true;
        }

        return false;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
