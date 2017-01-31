package edu.rutgers.css.Rutgers.channels.dtable.model;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.dtable.fragments.DTable;
import edu.rutgers.css.Rutgers.interfaces.FragmentMediator;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;

/**
 * Adapter for DTable with a Grid layout
 */
public class DTableGridAdapter extends RecyclerView.Adapter<DTableGridAdapter.ViewHolder> implements DTableAdapter {
    private final List<DTableElement> elements;
    private final FragmentMediator fm;
    private final String handle;
    private final String topHandle;
    private final String homeCampus;
    private final ArrayList<String> history;

    private final Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.card_title);
            image = (ImageView) itemView.findViewById(R.id.card_image);
        }

        public void bind(Context context, DTableElement element) {
            title.setText(element.getTitle(RutgersUtils.getHomeCampus(context)));
            String imageUrl = element.getImage();
            if (imageUrl != null) {
                imageUrl = Config.API_BASE + "img/" + imageUrl;
                Picasso.with(context)
                    .load(imageUrl)
                    .into(image);
            }
        }
    }

    public DTableGridAdapter(Context context,
                             @NonNull List<DTableElement> parentItemList,
                             FragmentMediator fm,
                             String handle,
                             String topHandle,
                             String homeCampus,
                             ArrayList<String> history) {
        this.context = context;
        this.elements = parentItemList;
        this.fm = fm;
        this.handle = handle;
        this.topHandle = topHandle;
        this.homeCampus = homeCampus;
        this.history = history;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.cell_dtable, parent, false);
        final int size = parent.getMeasuredWidth() / 2 - (int)(DTable.GRID_SPACING * 1.5);
        itemView.getLayoutParams().height = size;
        itemView.getLayoutParams().width = size;
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DTableElement item = elements.get(position);
        holder.bind(context, item);
        holder.itemView.setOnClickListener(view -> {
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
    public int getItemCount() {
        return elements.size();
    }

    @Override
    public void addAll(Collection<? extends DTableElement> elements) {
        this.elements.addAll(elements);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        elements.clear();
        notifyDataSetChanged();
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
