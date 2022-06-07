package com.hindbyte.velocity.tabs;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

public class SwipeToDismissListener implements View.OnTouchListener {

    public interface DismissCallback {
        void onDismiss();
    }

    private final DismissCallback callback;
    private float downX;
    private final int slop;
    private boolean swiping;
    private VelocityTracker velocityTracker;

    SwipeToDismissListener(View view, DismissCallback callback) {
        this.callback = callback;
        ViewConfiguration configuration = ViewConfiguration.get(view.getContext());
        this.slop = configuration.getScaledTouchSlop();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getRawX();
                velocityTracker = VelocityTracker.obtain();
                velocityTracker.addMovement(event);
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (velocityTracker != null) {
                    velocityTracker.addMovement(event);
                    float deltaX = event.getRawX() - downX;
                    if ((Math.abs(deltaX) > view.getHeight()*1.4) && swiping) {
                        callback.onDismiss();
                    } else if (swiping) {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .translationX(0f)
                                .setDuration(200);
                    }
                    downX = 0;
                    velocityTracker.recycle();
                    velocityTracker = null;
                    swiping = false;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (velocityTracker != null) {
                    velocityTracker.addMovement(event);
                    float deltaX = event.getRawX() - downX;
                    if (Math.abs(deltaX) > slop) {
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        MotionEvent cancelEvent = MotionEvent.obtainNoHistory(event);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        view.onTouchEvent(cancelEvent);
                        cancelEvent.recycle();
                        view.setScaleX(Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / view.getWidth())));
                        view.setScaleY(Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / view.getWidth())));
                        view.setTranslationX(deltaX*2);
                        swiping = true;
                    }
                }
                break;
            }
        }
        return false;
    }

}