package com.takusemba.spotlight;

import android.graphics.PointF;
import android.view.View;

import java.util.Collections;
import java.util.List;

/**
 * Target
 *
 * @author takusemba
 * @since 26/06/2017
 **/
public interface Target {

    /**
     * gets the point of this Target
     *
     * @return the point of this Target
     */
    List<PointProvider> getPoints();

    /**
     * gets the radius of this Target
     *
     * @return the radius of this Target
     */
    float getRadius();

    /**
     * gets the view of this Target
     *
     * @return the view of this Target
     */
    View getView();

    /**
     * gets the listener of this Target
     *
     * @return the listener of this Target
     */
    OnTargetStateChangedListener getListener();

    /**
     * default target
     */
    Target DEFAULT = new Target() {
        @Override
        public List<PointProvider> getPoints() {
            PointProvider provider = new PointProvider() {
                @Override
                public PointF getPoint() {
                    return new PointF(0, 0);
                }

                @Override
                public boolean isVisible() {
                    return true;
                }
            };
            return Collections.singletonList(provider);
        }

        @Override
        public float getRadius() {
            return 100f;
        }

        @Override
        public View getView() {
            return null;
        }

        @Override
        public OnTargetStateChangedListener getListener() {
            return null;
        }
    };
}