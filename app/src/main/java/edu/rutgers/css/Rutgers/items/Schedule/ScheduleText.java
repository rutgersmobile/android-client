package edu.rutgers.css.Rutgers.items.Schedule;

/**
 * Basic text for for schedule
 */
public class ScheduleText extends SectionAdapterItem {

    public enum TextType {
        DESCRIPTION, SYNOPSIS, PREREQS
    }

    private String title;
    private TextType type;

    public ScheduleText(String title, TextType type) {
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
