package edu.rutgers.css.Rutgers.api.model.soc;

/**
 * Basic text for for schedule
 */
public class ScheduleText implements Titleable {

    public enum TextType {
        DESCRIPTION, PREREQS
    }

    private final String title;
    private final TextType type;

    public ScheduleText(final String title, final TextType type) {
        this.title = title;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public TextType getType() {
        return type;
    }

    @Override
    public String getDisplayTitle() {
        return getTitle();
    }

}
