package com.takusemba.spotlight;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Spotlight
 *
 * @author takusemba
 * @since 26/06/2017
 **/
public class Spotlight {

    /**
     * Duration of Spotlight emerging
     */
    private static final long START_SPOTLIGHT_DURATION = 500L;
    /**
     * Duration of Spotlight disappearing
     */
    private static final long FINISH_SPOTLIGHT_DURATION = 500L;

    private static final long DEFAULT_DURATION = 1000L;
    private static final TimeInterpolator DEFAULT_ANIMATION = new DecelerateInterpolator(2f);

    private long duration = DEFAULT_DURATION;
    private boolean closeAfterEnd;
    private TimeInterpolator animation = DEFAULT_ANIMATION;
    private OnSpotlightStartedListener startedListener;
    private OnSpotlightEndedListener endedListener;
    private OnSpotlightCloseListener closeListener;

    private Spotlight() {
    }

    public static Spotlight with() {
        return new Spotlight();
    }

    /**
     * sets duration to {@link Target} Animation
     *
     * @param duration duration of Target Animation
     * @return the SpotlightView
     */
    public Spotlight setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    /**
     * sets duration to {@link Target} Animation
     *
     * @param animation type of Target Animation
     * @return the SpotlightView
     */
    public Spotlight setAnimation(TimeInterpolator animation) {
        this.animation = animation;
        return this;
    }

    public Spotlight setCloseAfterEnd(boolean closeAfterEnd) {
        this.closeAfterEnd = closeAfterEnd;
        return this;
    }

    /**
     * Sets Spotlight start Listener to Spotlight
     *
     * @param listener OnSpotlightStartedListener of Spotlight
     * @return This Spotlight
     */
    public Spotlight setOnSpotlightStartedListener(
            @NonNull final OnSpotlightStartedListener listener) {
        startedListener = listener;
        return this;
    }

    /**
     * Sets Spotlight end Listener to Spotlight
     *
     * @param listener OnSpotlightEndedListener of Spotlight
     * @return This Spotlight
     */
    public Spotlight setOnSpotlightEndedListener(@NonNull final OnSpotlightEndedListener listener) {
        endedListener = listener;
        return this;
    }

    public Spotlight setOnSpotlightCloseListener(@NonNull final OnSpotlightCloseListener closeListener) {
        this.closeListener = closeListener;
        return this;
    }

    /**
     * Shows {@link SpotlightView}
     */
    public <T extends Target> void start(@NonNull Activity activity, @NonNull T... targets) {
        SpotlightView spotlightView = createSpotlightView(activity);
        setUpSpotlightView(activity, spotlightView, new ArrayList<>(Arrays.asList(targets)));
    }

    private <T extends Target> void setUpSpotlightView(final Context context, final SpotlightView spotlightView, final @NonNull List<T> targets) {
        spotlightView.setOnSpotlightStateChangedListener(new SpotlightView.OnSpotlightStateChangedListener() {
            @Override
            public void onTargetClosed() {
                if (!targets.isEmpty()) {
                    Target target = targets.remove(0);
                    if (target.getListener() != null) {
                        target.getListener().onEnded(target);
                    }
                    if (!targets.isEmpty()) {
                        startTarget(targets, spotlightView);
                    } else {
                        finishSpotlight(context, spotlightView);
                    }
                }
            }

            @Override
            public void onTargetClicked() {
                finishTarget(targets, spotlightView);
            }
        });
        spotlightView.setOnSpotlightCloseListener(new OnSpotlightCloseListener() {
            @Override
            public void onClosed() {
                closeListener.onClosed();
            }
        });
        spotlightView.post(new Runnable() {
            @Override
            public void run() {
                startSpotlight(targets, spotlightView);
            }
        });
    }

    /**
     * Creates the spotlight view and starts
     */
    @SuppressWarnings("unchecked")
    private SpotlightView createSpotlightView(Activity context) {
        if (context == null) {
            throw new RuntimeException("context is null");
        }
        final View decorView = ((Activity) context).getWindow().getDecorView();
        SpotlightView spotlightView = new SpotlightView(context);
        spotlightView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        ((ViewGroup) decorView).addView(spotlightView);
        return spotlightView;
    }

    /**
     * show Target
     */
    @SuppressWarnings("unchecked")
    private <T extends Target> void startTarget(@NonNull List<T> targets, SpotlightView spotlightView) {
        if (!targets.isEmpty()) {
            Target target = targets.get(0);
            removeTutorialView(spotlightView);
            View view = target.getView();
            view.setId(R.id.tutorial);
            spotlightView.addView(view);
            spotlightView.turnUp(target.getPoints(), target.getRadius(), duration, animation);
            if (target.getListener() != null) target.getListener().onStarted(target);
        }
    }

    private void removeTutorialView(SpotlightView spotlightView) {
        for (int i = 0; i < spotlightView.getChildCount(); i++) {
            View view = spotlightView.getChildAt(i);
            if (view.getId() == R.id.tutorial) {
                spotlightView.removeView(view);
                break;
            }
        }
    }

    /**
     * show Spotlight
     */
    private <T extends Target> void startSpotlight(@NonNull final List<T> targets, final SpotlightView spotlightView) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(spotlightView, "alpha", 0f, 1f);
        objectAnimator.setDuration(START_SPOTLIGHT_DURATION);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (startedListener != null) startedListener.onStarted();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startTarget(targets, spotlightView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();
    }

    /**
     * hide Target
     */
    private <T extends Target> void finishTarget(@NonNull List<T> targets, SpotlightView spotlightView) {
        if (!targets.isEmpty()) {
            Target target = targets.get(0);
            spotlightView.turnDown(target.getRadius(), duration, animation);
        }
    }

    /**
     * hide Spotlight
     */
    private void finishSpotlight(final Context context, final SpotlightView spotlightView) {
        if (closeAfterEnd) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(spotlightView, "alpha", 1f, 0f);
            objectAnimator.setDuration(FINISH_SPOTLIGHT_DURATION);
            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    final View decorView = ((Activity) context).getWindow().getDecorView();
                    ((ViewGroup) decorView).removeView(spotlightView);
                    if (endedListener != null) endedListener.onEnded();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            objectAnimator.start();
        } else {
            removeTutorialView(spotlightView);
        }
    }
}
