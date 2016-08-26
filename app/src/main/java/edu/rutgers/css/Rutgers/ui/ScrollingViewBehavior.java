package edu.rutgers.css.Rutgers.ui;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class ScrollingViewBehavior extends CoordinatorLayout.Behavior<View> {
    private int toolbarHeight;

    public ScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            this.toolbarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        } else {
            this.toolbarHeight = 0;
        }
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View view, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View view, View dependency) {
        if (dependency instanceof AppBarLayout) {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            int fabBottomMargin = lp.bottomMargin;
            int distanceToScroll = view.getHeight() + fabBottomMargin;
            float ratio = (float)dependency.getY()/(float)toolbarHeight;
            view.setTranslationY(-distanceToScroll * ratio);
        }
        return true;
    }
}