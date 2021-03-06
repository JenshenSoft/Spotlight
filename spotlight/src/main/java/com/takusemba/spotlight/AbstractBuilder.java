package com.takusemba.spotlight;

import android.app.Activity;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by takusemba on 2017/06/28.
 */

abstract class AbstractBuilder<T extends AbstractBuilder<T, S>, S extends Target> {

    private WeakReference<Activity> contextWeakReference;
    protected OnTargetStateChangedListener listener;
    protected List<PointProvider> points;
    protected float radius = 100f;

    /**
     * return the builder itself
     */
    protected abstract T self();

    /**
     * return the built {@link Target}
     */
    protected abstract S build();

    /**
     * Return context weak reference
     *
     * @return the activity
     */
    protected Activity getContext() {
        return contextWeakReference.get();
    }

    /**
     * Constructor
     */
    protected AbstractBuilder(@NonNull Activity context) {
        contextWeakReference = new WeakReference<>(context);
        points = new ArrayList<>();
    }

    public T addPointProvider(PointProvider pointProvider) {
        points.add(pointProvider);
        return self();
    }

    public T addPointsProviders(List<PointProvider> pointProviders) {
        points.addAll(pointProviders);
        return self();
    }

    /**
     * Sets the initial position of target
     *
     * @param y starting position of y where spotlight reveals
     * @param x starting position of x where spotlight reveals
     * @return This Builder
     */
    public T addPoint(final float x, final float y) {
        points.add(new PointProvider() {
            @Override
            public PointF getPoint() {
                return new PointF(x, y);
            }

            @Override
            public boolean isVisible() {
                return true;
            }
        });
        return self();
    }

    /**
     * Sets the initial position of target
     *
     * @param point starting position where spotlight reveals
     * @return This Builder
     */
    public T addPoint(@NonNull PointF point) {
        return addPoint(point.x, point.y);
    }

    /**
     * Sets the initial position of target
     * Make sure the view already has a fixed position
     *
     * @param view starting position where spotlight reveals
     * @return This Builder
     */
    public T addPointLazy(final @NonNull View view) {
        addPointProvider(new PointProvider() {
            @Override
            public PointF getPoint() {
                int[] location = new int[2];
                view.getLocationInWindow(location);
                int x = location[0] + view.getWidth() / 2;
                int y = location[1] + view.getHeight() / 2;
                return new PointF(x, y);
            }

            @Override
            public boolean isVisible() {
                return view.getVisibility() == View.VISIBLE;
            }
        });
        return self();
    }

    /**
     * Sets the initial position of target
     * Make sure the view already has a fixed position
     *
     * @param view starting position where spotlight reveals
     * @return This Builder
     */
    public T addPoint(@NonNull View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        int x = location[0] + view.getWidth() / 2;
        int y = location[1] + view.getHeight() / 2;
        return addPoint(x, y);
    }

    /**
     * Sets the initial position of target
     * Make sure the view already has a fixed position
     *
     * @param view starting position where spotlight reveals
     * @return This Builder
     */
    public T addPointsLazy(@NonNull List<View> views) {
        final int[] location = new int[2];
        for (final View view : views) {
            addPointProvider(new PointProvider() {
                @Override
                public PointF getPoint() {
                    view.getLocationInWindow(location);
                    int x = location[0] + view.getWidth() / 2;
                    int y = location[1] + view.getHeight() / 2;
                    return new PointF(x, y);
                }

                @Override
                public boolean isVisible() {
                    return view.getVisibility() == View.VISIBLE;
                }
            });
        }
        return self();
    }

    /**
     * Sets the initial position of target
     * Make sure the view already has a fixed position
     *
     * @param view starting position where spotlight reveals
     * @return This Builder
     */
    public T addPoints(@NonNull List<View> views) {
        int[] location = new int[2];
        for (View view : views) {
            view.getLocationInWindow(location);
            int x = location[0] + view.getWidth() / 2;
            int y = location[1] + view.getHeight() / 2;
            addPoint(x, y);
        }
        return self();
    }

    /**
     * Sets the initial position of target
     * Make sure the view already has a fixed position
     *
     * @param view starting position where spotlight reveals
     * @return This Builder
     */
    public T addPointsF(@NonNull List<PointF> points) {
        for (PointF point : points) {
            addPoint(point.x, point.y);
        }
        return self();
    }

    /**
     * Sets the radius of target
     *
     * @param radius radius of target
     * @return This Builder
     */
    public T setRadius(float radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be greater than 0");
        }
        this.radius = radius;
        return self();
    }

    /**
     * Sets Target state changed Listener to target
     *
     * @param listener OnTargetStateChangedListener of target
     * @return This Builder
     */
    public T setOnSpotlightStartedListener(@NonNull final OnTargetStateChangedListener<S> listener) {
        this.listener = listener;
        return self();
    }
}
