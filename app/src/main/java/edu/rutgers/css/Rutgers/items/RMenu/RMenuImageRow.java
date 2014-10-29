package edu.rutgers.css.Rutgers.items.RMenu;

import android.support.annotation.NonNull;

import java.net.URL;

/**
 * Image row for RMenuAdapter that displays an image from the network.
 */
public class RMenuImageRow extends RMenuRow {

    private URL imageURL;
    private int height;
    private int width;

    public RMenuImageRow(@NonNull URL imageURL, int width, int height) {
        this.imageURL = imageURL;
        this.height = height;
        this.width = width;

        setTitle(imageURL.getFile());
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getDrawableURL() {
        return imageURL.toString();
    }

    @Override
    public boolean getIsCategory() {
        return false;
    }

    @Override
    public boolean getIsClickable() {
        return false;
    }

}
