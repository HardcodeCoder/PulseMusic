package com.hardcodecoder.pulsemusic.helper;


import com.google.android.material.appbar.AppBarLayout;

public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
    private State mCurrentState = State.IDLE;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        int mTotalScrollRange = appBarLayout.getTotalScrollRange();
        long frac = Math.round(0.3 * mTotalScrollRange);
        long abs = Math.abs(i);
        if (i == 0) {
            if (mCurrentState != State.EXPANDED) {
                onStateChanged(appBarLayout, State.EXPANDED);
            }
            mCurrentState = State.EXPANDED;
        } else if (abs >= mTotalScrollRange) {
            if (mCurrentState != State.COLLAPSED) {
                onStateChanged(appBarLayout, State.COLLAPSED);
            }
            mCurrentState = State.COLLAPSED;
        } else if (abs >= frac) {
            if (mCurrentState != State.STARTS_COLLAPSING) {
                onStateChanged(appBarLayout, State.STARTS_COLLAPSING);
            }
            mCurrentState = State.STARTS_COLLAPSING;
        } else {
            if (mCurrentState != State.IDLE) {
                onStateChanged(appBarLayout, State.IDLE);
            }
            mCurrentState = State.IDLE;
        }
    }

    /**
     * Notifies on state change
     *
     * @param appBarLayout Layout
     * @param state        Collapse state
     */
    public abstract void onStateChanged(AppBarLayout appBarLayout, State state);

    // State
    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE,
        STARTS_COLLAPSING
    }
}
