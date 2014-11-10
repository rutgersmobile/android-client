package edu.rutgers.css.Rutgers.ui;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import edu.rutgers.css.Rutgers2.R;

/**
 * FragmentActivity with special logo overlay on the action bar.
 */
public abstract class LogoFragmentActivity extends FragmentActivity {

    private int mRootLayoutId;
    private PopupWindow mLogoPopup;

    @Override
    protected void onStart() {
        super.onStart();
        showLogoOverlay(mRootLayoutId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissLogoOverlay();
    }

    public void setLogoRootLayoutId(int rootLayoutId) {
        mRootLayoutId = rootLayoutId;
    }

    /**
     * Hack to display special logo over the action bar icon.
     * @param rootLayoutId Resource ID of the root layout for the main activity
     */
    private void showLogoOverlay(int rootLayoutId) {
        // Make sure the root layout ID is valid
        if(findViewById(rootLayoutId) == null) throw new IllegalArgumentException("Invalid root layout ID");

        // Get a layout that just contains the logo to display
        final View logo = getLayoutInflater().inflate(R.layout.logo_split, null, false);
        logo.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Get the the action bar, home button, and title text views
        final View home = findViewById(android.R.id.home);
        final View decorView = getWindow().getDecorView();
        final View actionBarView = decorView.findViewById(getResources().getIdentifier("action_bar_container", "id", "android"));
        final TextView actionBarTitle = (TextView) decorView.findViewById(getResources().getIdentifier("action_bar_title", "id", "android"));

        // Post an event so that after the layouts are drawn, a popup containing the logo displays
        // over the normal action bar icon. Then add padding to the title so that it's not obscured
        // by the logo popup.
        findViewById(rootLayoutId).post(new Runnable() {
            public void run() {
                // Don't execute if activity is finishing/not running
                if(isFinishing() || actionBarView.getWindowToken() == null) return;

                int side = actionBarView.getHeight();

                /*
                 * The logo is split in half where the banner tapers off and transparency begins
                 * so that the opaque section is flush with the action bar and the transparent
                 * area hangs down past it. This is why height is set to twice that of the bar.
                 */
                mLogoPopup = new PopupWindow(logo, side+16, side*2, false);
                mLogoPopup.showAsDropDown(actionBarView, (int) home.getX(), -side);

                // Pass touch events through to the action bar
                mLogoPopup.setTouchable(false);

                // Move the title text over so it's not obscured by the logo
                int diff = Math.abs(mLogoPopup.getWidth() - home.getWidth());
                actionBarTitle.setPadding(diff, 0, 0, 0);
            }
        });
    }

    /**
     * Dismiss the logo popup.
     */
    private void dismissLogoOverlay() {
        if(mLogoPopup != null) {
            mLogoPopup.dismiss();
            mLogoPopup = null;
        }
    }

}
