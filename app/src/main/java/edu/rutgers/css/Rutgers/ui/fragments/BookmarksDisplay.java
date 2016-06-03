package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.model.BookmarkAdapter;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.utils.PrefUtils;

/**
 * Fragment for bookmark management
 */
public class BookmarksDisplay extends BaseDisplay {
    public static final String HANDLE = "bookmarks";

    public static Bundle createArgs() {
        Bundle args = new Bundle();
        args.putString(ComponentFactory.ARG_HANDLE_TAG, BookmarksDisplay.HANDLE);
        args.putString(ComponentFactory.ARG_COMPONENT_TAG, BookmarksDisplay.HANDLE);
        return args;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_dynamic_list, parent, false);

        getActivity().setTitle("Bookmarks");

        final FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        final FloatingActionButton shareFab = (FloatingActionButton) v.findViewById(R.id.mini_share_fab);
        final FloatingActionButton bookmarkFab = (FloatingActionButton) v.findViewById(R.id.mini_bookmark_fab);

        fab.setVisibility(View.GONE);
        shareFab.setVisibility(View.GONE);
        bookmarkFab.setVisibility(View.GONE);

        final Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            ((MainActivity) getActivity()).syncDrawer();
        }

        final BookmarkAdapter adapter = new BookmarkAdapter(getContext(), R.layout.row_bookmark_item, R.layout.row_bookmark_toggle_item);
        adapter.addAll(PrefUtils.getBookmarks(getContext()));

        final DynamicListView bookmarks = (DynamicListView) v.findViewById(R.id.dynamic_list);
        bookmarks.setAdapter(adapter);
        bookmarks.enableDragAndDrop();
        bookmarks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                bookmarks.startDragging(position);
                return true;
            }
        });

        return v;
    }
}
