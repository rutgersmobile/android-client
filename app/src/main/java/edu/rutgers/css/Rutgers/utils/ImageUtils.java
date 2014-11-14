package edu.rutgers.css.Rutgers.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers2.R;

/**
 * Image utilities
 */
public final class ImageUtils {

    private static final String TAG = "ImageUtils";

    private ImageUtils() {}

    /**
     * Get icon by resource ID, colored white
     * @param resources Application Resources
     * @param drawableResource Icon resource ID
     * @return Icon drawable
     */
    public static Drawable getIcon(@NonNull Resources resources, int drawableResource) {
        return getIcon(resources, drawableResource, R.color.white);
    }

    /**
     * Get icon by resource ID with specified color
     * @param resources Application Resources
     * @param drawableResource Icon resource ID
     * @param colorResource Color to be applied to icon
     * @return Icon drawable
     */
    public static Drawable getIcon(@NonNull Resources resources, int drawableResource, int colorResource) {
        if(drawableResource == 0) return null;
        if(colorResource == 0) colorResource = R.color.white;

        try {
            Drawable drawable = resources.getDrawable(drawableResource);
            int color = resources.getColor(colorResource);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            return drawable;
        } catch(Resources.NotFoundException e) {
            Log.w(TAG, "getIcon(): " + e.getMessage());
            return null;
        }
    }

    /**
     * Get icon by channel handle
     * @param resources Application Resources
     * @param handle Channel handle
     * @return Icon drawable for channel
     */
    public static Drawable getIcon(@NonNull Resources resources, @NonNull String handle) {
        int iconRes = 0, colorRes = 0;

        // Look up the icon resource
        try {
            iconRes = resources.getIdentifier("ic_"+handle, "drawable", Config.PACKAGE_NAME);
        } catch(Resources.NotFoundException e) {
            Log.w(TAG, "getIcon(): " + e.getMessage());
        }

        // Look up the color resource
        try {
            colorRes = resources.getIdentifier(handle+"_icon_color", "color", Config.PACKAGE_NAME);
        } catch(Resources.NotFoundException e) {
            Log.w(TAG, "getIcon(): " + e.getMessage());
        }

        return getIcon(resources, iconRes, colorRes);
    }

    public static Bitmap decodeSampledBitmapFromResource(@NonNull Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(res, resId, options);
    }

    private static int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
