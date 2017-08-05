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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

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

    private static WeakReference<SpotlightView> spotlightViewWeakReference;
    private static WeakReference<Activity> contextWeakReference;
    private ArrayList<? extends Target> targets;
    private long duration = DEFAULT_DURATION;
    private TimeInterpolator animation = DEFAULT_ANIMATION;
    private OnSpotlightStartedListener startedListener;
    private OnSpotlightEndedListener endedListener;

    private Spotlight(){}

    public static Spotlight with() {
        return new Spotlight();
    }

    /**
     * Return context weak reference
     *
     * @return the activity
     */
    private static Context getContext() {
        return contextWeakReference.get();
    }

    /**
     * Returns {@link SpotlightView} weak reference
     *
     * @return the SpotlightView
     */
    private static SpotlightView getSpotlightView() {
        return spotlightViewWeakReference.get();
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

    /**
     * Shows {@link SpotlightView}
     */
    public  <T extends Target> void start(@NonNull Activity activity, @NonNull T... targets) {
        this.targets = new ArrayList<>(Arrays.asList(targets));
        if (contextWeakReference == null || contextWeakReference.isEnqueued()) {
            contextWeakReference = new WeakReference<>(activity);
        }
        if (spotlightViewWeakReference != null && spotlightViewWeakReference.isEnqueued()) {
            setUpSpotlightView(spotlightViewWeakReference.get());
        } else {
            SpotlightView spotlightView = createSpotlightView(activity);
            spotlightViewWeakReference = new WeakReference<>(spotlightView);
            setUpSpotlightView(spotlightView);
        }
    }

    private void setUpSpotlightView(SpotlightView spotlightView) {
        spotlightView.setOnSpotlightStateChangedListener(new SpotlightView.OnSpotlightStateChangedListener() {
            @Override
            public void onTargetClosed() {
                if (!targets.isEmpty()) {
                    Target target = targets.remove(0);
                    if (target.getListener() != null) target.getListener().onEnded(target);
                    if (!targets.isEmpty()) {
                        startTarget();
                    } else {
                        finishSpotlight();
                    }
                }
            }

            @Override
            public void onTargetClicked() {
                finishTarget();
            }
        });
        startSpotlight();
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
        SpotlightView spotlightView = new SpotlightView(getContext());
        spotlightView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        ((ViewGroup) decorView).addView(spotlightView);
        return spotlightView;
    }

    /**
     * show Target
     */
    @SuppressWarnings("unchecked")
    private void startTarget() {
        if (targets != null && !targets.isEmpty()) {
            Target target = targets.get(0);
            getSpotlightView().removeAllViews();
            getSpotlightView().addView(target.getView());
            getSpotlightView().turnUp(target.getPoints(), target.getRadius(), duration, animation);
            if (target.getListener() != null) target.getListener().onStarted(target);
        }
    }

    /**
     * show Spotlight
     */
    private void startSpotlight() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(getSpotlightView(), "alpha", 0f, 1f);
        objectAnimator.setDuration(START_SPOTLIGHT_DURATION);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (startedListener != null) startedListener.onStarted();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                startTarget();
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
    private void finishTarget() {
        if (targets != null && !targets.isEmpty()) {
            Target target = targets.get(0);
            getSpotlightView().turnDown(target.getRadius(), duration, animation);
        }
    }

    /**
     * hide Spotlight
     */
    private void finishSpotlight() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(getSpotlightView(), "alpha", 1f, 0f);
        objectAnimator.setDuration(FINISH_SPOTLIGHT_DURATION);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                final View decorView = ((Activity) getContext()).getWindow().getDecorView();
                ((ViewGroup) decorView).removeView(getSpotlightView());
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
    }
}
