package edu.rutgers.css.Rutgers.model.rmenu;

public class RMenuHeaderRow extends RMenuRow {

    public RMenuHeaderRow(String title) {
        setTitle(title);
    }

    @Override
    public boolean getIsCategory() {
        return true;
    }

    @Override
    public boolean getIsClickable() {
        return false;
    }

}
