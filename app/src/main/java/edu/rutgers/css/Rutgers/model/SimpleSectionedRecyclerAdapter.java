package edu.rutgers.css.Rutgers.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Recycler alternative for SimpleSectionedAdapter
 */

public class SimpleSectionedRecyclerAdapter<T> extends SectionedRecyclerViewAdapter<SimpleSectionedRecyclerAdapter.ViewHolder> implements Filterable {
    private final List<SimpleSection<T>> sections;
    private final int headerResource;
    private final int itemResource;
    private final int textViewId;

    private final PublishSubject<IndexedItem<T>> onClickSubject = PublishSubject.create();

    private final Object mLock = new Object();
    private List<FilteredSection<T>> filteredSections;
    private SectionAdapterFilter filter;

    protected void setFilteredSections(List<FilteredSection<T>> filteredSections) {
        this.filteredSections = filteredSections;
        notifyDataSetChanged();
    }

    public static class IndexedItem<U> {
        private U item;
        private int section;
        private int relativePosition;
        private int absolutePosition;

        public IndexedItem(U item, int section, int relativePosition, int absolutePosition) {
            this.item = item;
            this.section = section;
            this.relativePosition = relativePosition;
            this.absolutePosition = absolutePosition;
        }

        public U getItem() {
            return item;
        }

        public int getSection() {
            return section;
        }

        public int getRelativePosition() {
            return relativePosition;
        }

        public int getAbsolutePosition() {
            return absolutePosition;
        }
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new SectionAdapterFilter();
        }
        return filter;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView, final int textViewId) {
            super(itemView);
            textView = (TextView) itemView.findViewById(textViewId);
        }

        public TextView getTextView() {
            return textView;
        }
    }

    public SimpleSectionedRecyclerAdapter(final List<SimpleSection<T>> sections,
                                          final int headerResource,
                                          final int itemResource,
                                          final int textViewId) {
        this.sections = sections;
        this.headerResource = headerResource;
        this.itemResource = itemResource;
        this.textViewId = textViewId;
    }

    @Override
    public int getSectionCount() {
        if (filteredSections != null) {
            return filteredSections.size();
        }
        return sections.size();
    }

    @Override
    public int getItemCount(int section) {
        if (filteredSections != null) {
            return filteredSections.get(section).matches.size();
        }
        return sections.get(section).getItems().size();
    }

    public SimpleSection<T> getSection(int section) {
        if (filteredSections != null) {
            return filteredSections.get(section).section;
        }
        return sections.get(section);
    }

    public T getItem(int section, int relativePosition) {
        if (filteredSections != null) {
            return filteredSections.get(section).matches.get(relativePosition);
        }
        return sections.get(section).getItems().get(relativePosition);
    }

    @Override
    public void onBindHeaderViewHolder(ViewHolder holder, int section) {
        holder.getTextView().setText(getSection(section).getHeader());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int section, int relativePosition, int absolutePosition) {
        final T item = getItem(section, relativePosition);
        holder.getTextView().setText(getItemRepresentation(item));
        holder.itemView.setOnClickListener(view -> getOnClickSubject().onNext(new IndexedItem<>(item, section, relativePosition, absolutePosition)));
    }

    protected String getItemRepresentation(T item) {
        return item.toString();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(viewType == VIEW_TYPE_HEADER ? headerResource : itemResource, parent, false);
        return new ViewHolder(itemView, textViewId);
    }

    public void clear() {
        sections.clear();
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends SimpleSection<T>> collection) {
        sections.addAll(collection);
        notifyDataSetChanged();
    }

    public void add(SimpleSection<T> section) {
        sections.add(section);
        notifyDataSetChanged();
    }

    public Observable<IndexedItem<T>> getIndexedPositionClicks() {
        return onClickSubject.asObservable();
    }

    public Observable<T> getPositionClicks() {
        return getIndexedPositionClicks().map(IndexedItem::getItem);
    }

    protected PublishSubject<IndexedItem<T>> getOnClickSubject() {
        return onClickSubject;
    }

    protected static class FilteredSection<U> {
        private SimpleSection<U> section;
        private ArrayList<U> matches;

        public FilteredSection(SimpleSection<U> section, ArrayList<U> matches) {
            this.section = section;
            this.matches = matches;
        }

        public SimpleSection<U> getSection() {
            return section;
        }

        public ArrayList<U> getMatches() {
            return matches;
        }
    }

    private class SectionAdapterFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            final FilterResults results = new FilterResults();

            // If the prefix is blank, return the original values.
            if (StringUtils.isBlank(prefix)) {
                results.values = null;
                results.count = 0;

                return results;
            }

            final ArrayList<SimpleSection<T>> originalSections;
            synchronized (mLock) {
                originalSections = new ArrayList<>(sections);
            }

            final ArrayList<FilteredSection> filteredSections = new ArrayList<>();

            // Filter the items of each section.
            for (SimpleSection<T> section: originalSections) {
                final ArrayList<T> sectionMatches = new ArrayList<>();
                for (T value : section.getItems()) {
                    final String valueText = value.toString();

                    if (StringUtils.startsWithIgnoreCase(valueText, prefix)) {
                        sectionMatches.add(value);
                    } else {
                        final String[] words = StringUtils.split(valueText, ' ');

                        for (String word: words) {
                            if (StringUtils.startsWithIgnoreCase(word, prefix)) {
                                sectionMatches.add(value);
                                break;
                            }
                        }
                    }
                }

                // If this section had matches, include it.
                if (!sectionMatches.isEmpty()) {
                    FilteredSection filteredSection = new FilteredSection<>(section, sectionMatches);
                    filteredSections.add(filteredSection);
                }
            }

            results.values = filteredSections;
            results.count = filteredSections.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence prefix, Filter.FilterResults filterResults) {
            if (filterResults.values == null) {
                // The constraint was null or blank; show original values.
                setFilteredSections(null);
            } else {
                // Show filter results
                setFilteredSections((List<FilteredSection<T>>) filterResults.values);
            }
        }
    }
}
