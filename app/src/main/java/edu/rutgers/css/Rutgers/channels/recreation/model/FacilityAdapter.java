package edu.rutgers.css.Rutgers.channels.recreation.model;

import android.content.Context;
import android.support.annotation.NonNull;

import edu.rutgers.css.Rutgers.api.recreation.model.Campus;
import edu.rutgers.css.Rutgers.api.recreation.model.facility.Facility;
import edu.rutgers.css.Rutgers.model.SectionedListAdapter;

/**
 * Recreation facilities adapter. Campuses are sections, facilities are the items.
 */
public class FacilityAdapter extends SectionedListAdapter<Campus, Facility> {

    public FacilityAdapter(@NonNull Context context, int itemResource, int headerResource, int textViewId) {
        super(context, itemResource, headerResource, textViewId);
    }

    @Override
    public String getSectionHeader(Campus section) {
        return section.getTitle();
    }

    @Override
    public Facility getSectionItem(Campus section, int position) {
        return section.getFacilities().get(position);
    }

    @Override
    public int getSectionItemCount(Campus section) {
        return section.getFacilities().size();
    }

    @Override
    public Campus getSectionContainingItem(int position) {
        return super.getSectionContainingItem(position);
    }

}
