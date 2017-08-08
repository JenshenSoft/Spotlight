package com.takusemba.spotlight;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import static android.view.Gravity.END;
import static android.view.Gravity.TOP;

/**
 * Spotlight View
 *
 * @author takusemba
 * @since 26/06/2017
 **/
class SpotlightView extends FrameLayout {

    private final Paint paint = new Paint();
    private final Paint spotPaint = new Paint();
    private final List<PointProvider> points = new ArrayList<>();
    private ValueAnimator animator;
    private OnSpotlightStateChangedListener listener;
    private ImageView closeButton;

    public SpotlightView(@NonNull Context context) {
        super(context, null);
        init();
    }

    public SpotlightView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public SpotlightView(@NonNull Context context, @Nullable AttributeSet attrs,
                         @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        points.clear();
        if (animator == null) {
            return;
        }
        if (animator.isStarted()) {
            animator.cancel();
        }
        animator = null;
    }

    /**
     * sets listener to {@link SpotlightView}
     */
    public void setOnSpotlightStateChangedListener(OnSpotlightStateChangedListener l) {
        this.listener = l;
    }

    public void setOnSpotlightCloseListener(final OnSpotlightCloseListener closeListener) {
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                closeListener.onClosed();
            }
        });
    }

    public void setSpotlightBackgroundColor(@ColorInt int color) {
        paint.setColor(color);
    }

    /**
     * prepares to show this Spotlight
     */
    private void init() {
        paint.setColor(ContextCompat.getColor(getContext(), R.color.background));
        bringToFront();
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        int padding = getResources().getDimensionPixelOffset(R.dimen.close_button_padding);
        closeButton = new ImageView(getContext());
        closeButton.setImageResource(R.drawable.ic_close);
        closeButton.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, TOP | END));
        closeButton.setPadding(padding, padding, padding, padding);
        addView(closeButton);
        spotPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animator != null && !animator.isRunning() && (float) animator.getAnimatedValue() > 0) {
                    if (listener != null) listener.onTargetClicked();
                }
            }
        });
    }

    /**
     * draws black background and trims a circle
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        if (animator != null) {
            for (PointProvider point : points) {
                PointF pointF = point.getPoint();
                canvas.drawCircle(pointF.x, pointF.y, (float) animator.getAnimatedValue(), spotPaint);
            }
        }
    }

    /**
     * starts an animation to show a circle
     *
     * @param x         initial position x where the circle is showing up
     * @param y         initial position y where the circle is showing up
     * @param radius    radius of the circle
     * @param duration  duration of the animation
     * @param animation type of the animation
     */
    void turnUp(List<PointProvider> points, float radius, long duration, TimeInterpolator animation) {
        this.points.clear();
        this.points.addAll(points);
        animator = ValueAnimator.ofFloat(0f, radius);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                SpotlightView.this.invalidate();
            }
        });
        animator.setInterpolator(animation);
        animator.setDuration(duration);
        animator.start();
    }

    /**
     * starts an animation to close the circle
     *
     * @param radius    radius of the circle
     * @param duration  duration of the animation
     * @param animation type of the animation
     */
    void turnDown(float radius, long duration, TimeInterpolator animation) {
        animator = ValueAnimator.ofFloat(radius, 0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                SpotlightView.this.invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) listener.onTargetClosed();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setInterpolator(animation);
        animator.setDuration(duration);
        animator.start();
    }

    /**
     * Listener to control Target state
     */
    interface OnSpotlightStateChangedListener {
        /**
         * Called when Target closed completely
         */
        void onTargetClosed();

        /**
         * Called when Target is Clicked
         */
        void onTargetClicked();
    }
}