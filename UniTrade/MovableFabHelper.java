package com.example.unitrade;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MovableFabHelper {

    private float dX, dY;
    private long touchStartTime;
    private static final int CLICK_DURATION = 150; // ms

    public void enable(
            FloatingActionButton fab,
            View rootContainer,   // usually scrollArea or main layout
            View topBar,          // your appBarCheckout
            View bottomBar        // your bottomBar layout
    ) {

        fab.post(() -> {

            int[] rootPos = new int[2];
            int[] topPos = new int[2];
            int[] bottomPos = new int[2];

            rootContainer.getLocationOnScreen(rootPos);
            topBar.getLocationOnScreen(topPos);
            bottomBar.getLocationOnScreen(bottomPos);

            int rootTop = rootPos[1];

            // ---- LIMITS ----
            int topLimit = (topPos[1] + topBar.getHeight()) - rootTop;
            int bottomLimit = (bottomPos[1] - fab.getHeight()) - rootTop;

            // screen width
            DisplayMetrics dm = new DisplayMetrics();
            fab.getContext().getDisplay().getRealMetrics(dm);
            int screenWidth = dm.widthPixels;

            int leftLimit = 0;
            int rightLimit = screenWidth - fab.getWidth();

            fab.setOnTouchListener((v, event) -> {

                switch (event.getActionMasked()) {

                    case MotionEvent.ACTION_DOWN:
                        touchStartTime = System.currentTimeMillis();
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY - rootTop;

                        if (newX < leftLimit) newX = leftLimit;
                        if (newX > rightLimit) newX = rightLimit;

                        if (newY < topLimit) newY = topLimit;
                        if (newY > bottomLimit) newY = bottomLimit;

                        v.setX(newX);
                        v.setY(newY);
                        return true;

                    case MotionEvent.ACTION_UP:
                        // Detect real click (short touch)
                        if (System.currentTimeMillis() - touchStartTime < CLICK_DURATION) {
                            v.performClick();
                        }
                        return true;
                }

                return false;
            });
        });
    }
}
