package edu.rutgers.css.Rutgers.api.soc.model;

import edu.rutgers.css.Rutgers.api.soc.Titleable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Basic text for for schedule
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ScheduleText implements Titleable {

    public enum TextType {
        DESCRIPTION, PREREQS
    }

    private final String title;
    private final TextType type;

    @Override
    public String getDisplayTitle() {
        return getTitle();
    }

}
