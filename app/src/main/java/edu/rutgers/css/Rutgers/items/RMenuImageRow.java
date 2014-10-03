package edu.rutgers.css.Rutgers.items;

import java.net.URL;

/**
 * Created by jamchamb on 10/3/14.
 */
public class RMenuImageRow extends RMenuRow {

    private URL imageURL;
    private int height;
    private int width;

    public RMenuImageRow(URL imageURL, int width, int height) {
        this.imageURL = imageURL;
        this.height = height;
        this.width = width;
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

    @Override
    public String toString() {
        return imageURL.getFile();
    }

}
