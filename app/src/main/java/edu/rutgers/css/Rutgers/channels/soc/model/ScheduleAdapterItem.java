package edu.rutgers.css.Rutgers.channels.soc.model;

public abstract class ScheduleAdapterItem {
    public abstract String getDisplayTitle();
    public abstract String getTitle();
    public abstract String getCode();

    public boolean equals(ScheduleAdapterItem other) {
        return this.getTitle().equals(other.getTitle()) && this.getCode().equals(other.getCode());
    }
}
