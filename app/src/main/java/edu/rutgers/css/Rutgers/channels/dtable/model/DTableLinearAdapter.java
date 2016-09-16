package edu.rutgers.css.Rutgers.channels.dtable.model;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.Model.ParentWrapper;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.dtable.fragments.DTable;
import edu.rutgers.css.Rutgers.interfaces.FragmentMediator;

/**
 * Adapter for making menus from DTable roots.
 */
public class DTableLinearAdapter
        extends ExpandableRecyclerAdapter<
            DTableLinearAdapter.QuestionViewHolder,
            DTableLinearAdapter.AnswerViewHolder>
        implements DTableAdapter {

    private final static String TAG = "DTableLinearAdapter";
    private final FragmentMediator fm;
    private final String handle;
    private final String topHandle;
    private final String homeCampus;
    private final ArrayList<String> history;
    private final List<DTableElement> elements;

    public static class QuestionViewHolder extends ParentViewHolder {

        private final TextView questionText;

        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        public QuestionViewHolder(View itemView) {
            super(itemView);
            questionText = (TextView) itemView.findViewById(R.id.question);
        }

        public void bind(DTableElement item) {
            questionText.setText(item.getTitle());
        }
    }

    public static class AnswerViewHolder extends ChildViewHolder {

        private final TextView answerText;

        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        public AnswerViewHolder(View itemView) {
            super(itemView);
            answerText = (TextView) itemView.findViewById(R.id.answer);
        }

        public void bind(String answer) {
            answerText.setText(answer);
        }
    }

    public DTableLinearAdapter(@NonNull List<DTableElement> parentItemList,
                               FragmentMediator fm,
                               String handle,
                               String topHandle,
                               String homeCampus,
                               ArrayList<String> history) {
        super(parentItemList);
        this.elements = parentItemList;
        this.fm = fm;
        this.handle = handle;
        this.topHandle = topHandle;
        this.homeCampus = homeCampus;
        this.history = history;
    }

    @Override
    public QuestionViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        final View questionView = LayoutInflater
                .from(parentViewGroup.getContext())
                .inflate(R.layout.row_question, parentViewGroup, false);
        return new QuestionViewHolder(questionView);
    }

    @Override
    public AnswerViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        final View answerView = LayoutInflater
                .from(childViewGroup.getContext())
                .inflate(R.layout.row_answer, childViewGroup, false);
        return new AnswerViewHolder(answerView);
    }

    @Override
    public void onBindParentViewHolder(final QuestionViewHolder parentViewHolder, final int position, final ParentListItem parentListItem) {
        final DTableElement item = (DTableElement) parentListItem;
        parentViewHolder.bind(item);
        parentViewHolder.itemView.setOnClickListener(view -> {
            if (item instanceof DTableFAQ && !parentViewHolder.isExpanded()) {
                expandParent(item);
            } else {
                collapseParent(item);
            }

            if (item instanceof DTableRoot) {
                final DTableRoot root = (DTableRoot) item;
                String newHandle = handle + "_" + item.getTitle(homeCampus).replace(" ", "_").toLowerCase();
                Bundle newArgs = DTable.createArgs(item.getTitle(homeCampus), newHandle, topHandle, root.getLayout(), root);
                fm.switchFragments(newArgs);
            } else if (item instanceof DTableChannel) {
                // Channel row - launch channel
                final DTableChannel channel = (DTableChannel) item;
                final Bundle args = DTable.createChannelArgs(channel, homeCampus, topHandle, history);
                fm.switchFragments(args);
            }
        });
    }

    @Override
    public void onBindChildViewHolder(final AnswerViewHolder childViewHolder, final int position, Object childListItem) {
        if (childListItem instanceof String) {
            final String answer = (String) childListItem;
            childViewHolder.bind(answer);
            childViewHolder.itemView.setOnClickListener(view -> {
                final int adapterPosition = childViewHolder.getAdapterPosition();
                final ParentWrapper item = (ParentWrapper)mItemList.get(adapterPosition - 1);
                collapseParent(item.getParentListItem());
            });
        }
    }

    @Override
    public void addAll(Collection<? extends DTableElement> elements) {
        final int currentSize = this.elements.size();
        this.elements.addAll(elements);
        notifyParentItemRangeInserted(currentSize, currentSize + elements.size());
    }

    @Override
    public void clear() {
        final int currentSize = this.elements.size();
        elements.clear();
        notifyParentItemRangeRemoved(0, currentSize);
    }

    @Override
    public void addAllHistory(Collection<? extends String> history) {
        this.history.addAll(history);
    }

    @Override
    public void clearHistory() {
        this.history.clear();
    }
}