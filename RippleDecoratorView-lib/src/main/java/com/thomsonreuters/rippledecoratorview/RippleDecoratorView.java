/* Copyright 2015 Thomson Reuters

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

package com.thomsonreuters.rippledecoratorview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

public class RippleDecoratorView extends RelativeLayout {
    public static final float NANOS_TO_MILLIS = 1000000.0F;

    public enum Triggers {
        onUp, onDown, onTap, none;
        private static Triggers fromOrdinal(int ord) {
            switch (ord) {
                case 0:
                    return onUp;
                case 1:
                    return onDown;
                case 2:
                    return onTap;
                default:
                    return none;
            }
        }
    }

    public enum Styles {
        STROKE(Paint.Style.STROKE), FILL(Paint.Style.FILL);
        private Paint.Style mStyle;

        Styles(Paint.Style mStyle) {
            this.mStyle = mStyle;
        }

        private static Styles fromOrdinal(int ord) {
            switch (ord) {
                case 0:
                    return STROKE;
                case 1:
                    return FILL;
                default:
                    return STROKE;
            }
        }

        public Paint.Style getStyle() {
            return this.mStyle;
        }
    }

    public static final int RIPPLE_COLOR = Color.WHITE;

    public static final Styles RIPPLE_STYLE = Styles.STROKE;

    public static final float RIPPLE_MAX_ALPHA = 1.0F;

    public static final boolean RIPPLE_CENTERED = false;

    public static final int RIPPLE_PADDING = 0;

    public static final float RIPPLE_RADIUS = -1.0F;

    public static final Triggers RIPPLE_ANIMATION_TRIGGER = Triggers.onTap;

    public static final float RIPPLE_ANIMATION_DURATION = 400.0F;

    public static final int RIPPLE_ANIMATION_FRAMES = 60;

    public static final boolean ZOOM_ANIMATION = false;

    public static final Triggers ZOOM_ANIMATION_TRIGGER = Triggers.onTap;

    public static final float ZOOM_SCALE = 1.03F;

    public static final boolean HIGHLIGHT_ANIMATION = false;

    public static final float HIGHLIGHT_MAX_ALPHA = 0.2F;

    private int mRippleColor;

    private Styles mRippleStyle = RIPPLE_STYLE;

    private float mRippleMaxAlpha = RIPPLE_MAX_ALPHA;

    private boolean mRippleCentered = RIPPLE_CENTERED;

    private int mRipplePadding = RIPPLE_PADDING;

    private float mRippleRadius = RIPPLE_RADIUS;

    private Triggers mRippleAnimationTrigger = RIPPLE_ANIMATION_TRIGGER;

    private float mRippleAnimationDuration = RIPPLE_ANIMATION_DURATION;

    private int mRippleAnimationFrames = RIPPLE_ANIMATION_FRAMES;

    private int mRippleAnimationPeakFrame;

    private boolean mZoomAnimation = ZOOM_ANIMATION;

    private Triggers mZoomAnimationTrigger = ZOOM_ANIMATION_TRIGGER;

    private float mZoomAnimationScale = ZOOM_SCALE;

    private float mZoomAnimationDuration;

    private boolean mHighlightAnimation = HIGHLIGHT_ANIMATION;

    private int mHighlighColor;

    private float mHighlightMaxAlpha = HIGHLIGHT_MAX_ALPHA;

    private int mHighlightAnimationPeakFrame;

    private Interpolator mInterpolator = new LinearInterpolator();

    private Interpolator mZoomInterpolator = new LinearInterpolator();

    private long mAnimationStartNanoTime = 0L;

    private int mWidth;

    private int mHeight;

    private float mFrameDuration;

    private Handler mCanvasHandler;

    private boolean mIsAnimationRunning = false;

    private int mCurrentFrame = 0;

    private float mPositionX = -1;

    private float mPositionY = -1;

    private ScaleAnimation mScaleAnimation;

    private Paint mRipplePaint;

    private Paint mHighlightPaint;

    private GestureDetector mDownGestureDetector;

    private GestureDetector mTapGestureDetector;

    private Runnable invalidateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    public RippleDecoratorView(Context context) {
        super(context);
        init(context, null);
    }

    public RippleDecoratorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RippleDecoratorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        this.setWillNotDraw(false);
        this.setDrawingCacheEnabled(true);
        if (null == attrs) {
            /* Obtain empty array */
            initFromTypedArray(context.obtainStyledAttributes(new int[] {}));
        } else {
            initFromTypedArray(context.obtainStyledAttributes(attrs,
                    R.styleable.RippleDecoratorView));
        }
        initPaints();
        initGestures(context);
        mCanvasHandler = new Handler();
    }

    private void initGestures(Context context) {
        mDownGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }
                });
        mTapGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        return true;
                    }
                });
    }

    private void initPaints() {
        mRipplePaint = new Paint();
        mRipplePaint.setAntiAlias(true);
        mRipplePaint
                .setStrokeWidth(2 * this.getContext().getResources().getDisplayMetrics().density);
        mHighlightPaint = new Paint();
        mHighlightPaint.setAntiAlias(false);
        mHighlightPaint.setStyle(Paint.Style.FILL);
    }

    private void initFromTypedArray(TypedArray typedArray) {
        mRippleColor = typedArray.getColor(R.styleable.RippleDecoratorView_rdv_rippleColor,
                RIPPLE_COLOR);
        mRippleStyle = Styles.fromOrdinal(typedArray.getInt(
                R.styleable.RippleDecoratorView_rdv_rippleStyle, Styles.STROKE.ordinal()));
        mRippleMaxAlpha = 255.0F * Math.min(1.0F, typedArray.getFloat(
                R.styleable.RippleDecoratorView_rdv_rippleMaxAlpha, mRippleMaxAlpha));
        mRippleCentered = typedArray.getBoolean(R.styleable.RippleDecoratorView_rdv_rippleCentered,
                mRippleCentered);
        mRipplePadding = typedArray.getDimensionPixelSize(
                R.styleable.RippleDecoratorView_rdv_ripplePadding, mRipplePadding);
        mRippleRadius = typedArray.getDimensionPixelSize(
                R.styleable.RippleDecoratorView_rdv_rippleRadius, (int)mRippleRadius);
        mRippleAnimationTrigger = Triggers.fromOrdinal(typedArray.getInt(
                R.styleable.RippleDecoratorView_rdv_rippleAnimationTrigger,
                mRippleAnimationTrigger.ordinal()));
        mRippleAnimationDuration = typedArray.getFloat(
                R.styleable.RippleDecoratorView_rdv_rippleAnimationDuration,
                mRippleAnimationDuration);
        mRippleAnimationFrames = typedArray.getInt(
                R.styleable.RippleDecoratorView_rdv_rippleAnimationFrames, mRippleAnimationFrames);
        mRippleAnimationPeakFrame = typedArray.getInt(
                R.styleable.RippleDecoratorView_rdv_rippleAnimationPeakFrame,
                mRippleAnimationFrames);
        mZoomAnimation = typedArray.getBoolean(R.styleable.RippleDecoratorView_rdv_zoomAnimation,
                mZoomAnimation);
        mZoomAnimationTrigger = Triggers.fromOrdinal(typedArray.getInt(
                R.styleable.RippleDecoratorView_rdv_rippleAnimationTrigger,
                mZoomAnimationTrigger.ordinal()));
        mZoomAnimationScale = typedArray.getFloat(
                R.styleable.RippleDecoratorView_rdv_zoomAnimationScale, mZoomAnimationScale);
        mZoomAnimationDuration = typedArray
                .getFloat(R.styleable.RippleDecoratorView_rdv_zoomAnimationDuration,
                        mRippleAnimationDuration);
        mHighlightAnimation = typedArray.getBoolean(
                R.styleable.RippleDecoratorView_rdv_highlightAnimation, mHighlightAnimation);
        mHighlighColor = typedArray.getColor(R.styleable.RippleDecoratorView_rdv_highlightColor,
                mRippleColor);
        mHighlightMaxAlpha = 255.0F * Math.min(1.0F, typedArray.getFloat(
                R.styleable.RippleDecoratorView_rdv_highlightMaxAlpha, mHighlightMaxAlpha));
        mHighlightAnimationPeakFrame = typedArray.getInt(
                R.styleable.RippleDecoratorView_rdv_highlightAnimationPeakFrame,
                mRippleAnimationFrames);
        typedArray.recycle();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mIsAnimationRunning) {
            float deltaMilliseconds = (System.nanoTime() - mAnimationStartNanoTime)
                    / NANOS_TO_MILLIS;
            if (mRippleAnimationDuration <= deltaMilliseconds) {
                mIsAnimationRunning = false;
                mCurrentFrame = 0;
                canvas.restore();
                mAnimationStartNanoTime = 0;
                invalidate();
            } else {
                mCanvasHandler.postDelayed(invalidateRunnable, (long)mFrameDuration);
                validateAnimation(mRippleAnimationFrames, mRippleAnimationPeakFrame,
                        mHighlightAnimation, mHighlightAnimationPeakFrame);
                if (mCurrentFrame == 0) {
                    canvas.save();
                }
                mCurrentFrame = (int)(deltaMilliseconds / mFrameDuration);
                /* Java integer division returns an integer #justjavathings */
                if (mHighlightAnimation) {
                    Paint highLightPaint = preparePaintHighlight(mHighlightPaint, mCurrentFrame,
                            mRippleAnimationFrames, mHighlightAnimationPeakFrame,
                            mHighlightMaxAlpha, mInterpolator);
                    canvas.drawRect(canvas.getClipBounds(), highLightPaint);
                }
                Paint highLightPaint = preparePaintRipple(mRipplePaint, mCurrentFrame,
                        mRippleAnimationFrames, mRippleAnimationPeakFrame, mRippleMaxAlpha,
                        mFrameDuration, mRippleAnimationDuration);
                canvas.drawCircle(
                        mPositionX,
                        mPositionY,
                        (mRippleRadius * (((float)mCurrentFrame * mFrameDuration) / mRippleAnimationDuration)),
                        highLightPaint);
            }
        }
    }

    private void validateAnimation(int animationFrames, int rippleAnimationPeakFrame,
            boolean highlightAnimation, int highlightAnimationPeakFrame) {
        if (animationFrames <= 0) {
            throw new IllegalArgumentException("Animation frames need to be higher than 0");
        }
        if ((highlightAnimation && highlightAnimationPeakFrame > animationFrames)
                || rippleAnimationPeakFrame > animationFrames) {
            throw new IllegalArgumentException("Peak frames cannot be higher than total frames");
        }
    }

    private Paint preparePaintHighlight(Paint highlightPaint, int currentFrame,
            int animationFrames, int highlightAnimationPeakFrame, float highlightMaxAlpha,
            Interpolator interpolator) {
        float rectInterpolatorPosition = (currentFrame - 1 < highlightAnimationPeakFrame) ? (float)currentFrame
                / (float)highlightAnimationPeakFrame
                : 1 - (((float)(currentFrame - highlightAnimationPeakFrame) / (float)(animationFrames - highlightAnimationPeakFrame)));
        int rectAlpha = (int)(highlightMaxAlpha * interpolator
                .getInterpolation(rectInterpolatorPosition));
        highlightPaint.setAlpha(rectAlpha);
        return highlightPaint;
    }

    private Paint preparePaintRipple(Paint ripplePaint, int currentFrame, int animationFrames,
            int rippleAnimationPeakFrame, float rippleMaxAlpha, float frameDuration,
            float rippleAnimationDuration) {
        float rippleInterpolatorPosition = (currentFrame - 1 < rippleAnimationPeakFrame) ? (float)currentFrame
                / (float)rippleAnimationPeakFrame
                : (1 - ((float)(currentFrame - rippleAnimationPeakFrame) / (float)(animationFrames - rippleAnimationPeakFrame)));
        int rippleAlpha = (int)(rippleMaxAlpha * mInterpolator
                .getInterpolation(rippleInterpolatorPosition));
        ripplePaint
                .setAlpha((int)(rippleAlpha - ((rippleAlpha) * (((float)currentFrame * frameDuration) / rippleAnimationDuration))));
        return ripplePaint;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mWidth = width;
        mHeight = height;
        mScaleAnimation = new ScaleAnimation(1.0F, mZoomAnimationScale, 1.0F, mZoomAnimationScale,
                width / 2, height / 2);
        mScaleAnimation.setDuration((long)(mZoomAnimationDuration / 2));
        mScaleAnimation.setRepeatMode(Animation.REVERSE);
        mScaleAnimation.setRepeatCount(1);
        mScaleAnimation.setInterpolator(mZoomInterpolator);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            checkAnimations(event, Triggers.onUp);
        }
        if (mDownGestureDetector.onTouchEvent(event)) {
            checkAnimations(event, Triggers.onDown);
        }
        if (mTapGestureDetector.onTouchEvent(event)) {
            checkAnimations(event, Triggers.onTap);
        }
        return true;
    }

    private void checkAnimations(MotionEvent event, Triggers triggers) {
        if (mZoomAnimation && mZoomAnimationTrigger == triggers) {
            this.startAnimation(mScaleAnimation);
        }
        if (!mIsAnimationRunning && mRippleAnimationTrigger == triggers) {
            startDrawAnimation(event.getX(), event.getY());
        }
    }

    private void startDrawAnimation(float x, float y) {
        mFrameDuration = mRippleAnimationDuration / mRippleAnimationFrames;
        mRipplePaint.setStyle((mRippleStyle.getStyle()));
        mRipplePaint.setColor(mRippleColor);
        mRipplePaint.setAlpha(0);
        mHighlightPaint.setColor(mHighlighColor);
        mHighlightPaint.setAlpha(0);
        if (mRippleRadius == -1) {
            mRippleRadius = Math.max(mWidth, mHeight) / 2 - mRipplePadding;
        }
        if (mRippleCentered) {
            this.mPositionX = getMeasuredWidth() / 2;
            this.mPositionY = getMeasuredHeight() / 2;
        } else {
            this.mPositionX = x;
            this.mPositionY = y;
        }
        mIsAnimationRunning = true;
        mAnimationStartNanoTime = System.nanoTime();
        invalidate();
        this.performClick();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimation();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        onTouchEvent(event);
        return false;
    }

    // /////////////
    // PUBLIC API //
    // /////////////
    /**
     * Starts animations for this view programmatically.
     * 
     * @param x Center of the animation in the x axis
     * @param y Center of the animation in the y axis
     */
    public void doAnimation(float x, float y) {
        startDrawAnimation(x, y);
        if (mZoomAnimation) {
            this.startAnimation(mScaleAnimation);
        }
    }

    /**
     * Cancels all running animations for this view. NOTE: Does not cancel zoom animation.
     */
    public void cancelAnimation() {
        mIsAnimationRunning = false;
        // FIXME cancelling zoom has unwanted side effects
    }

    /**
     * Get the color of the ripple.
     *
     * @return ripple color
     */
    public int getRippleColor() {
        return this.mRippleColor;
    }

    /**
     * Set the color of the ripple. NOTE: Do not send color ids.
     *
     * @param rippleColor ripple color
     */
    public void setRippleColor(int rippleColor) {
        this.mRippleColor = rippleColor;
        mRipplePaint.setColor(rippleColor);
    }

    /**
     * Get the ripple style: stroke only draws the outline, fill draws the full circle.
     *
     * @return ripple style
     */
    public Styles getRippleStyle() {
        return this.mRippleStyle;
    }

    /**
     * Set the ripple style: stroke only draws the outline, fill draws the full circle.
     *
     * @param styles ripple style
     */
    public void setRippleStyle(Styles styles) {
        this.mRippleStyle = styles;
    }

    /**
     * Get the maximum transparency reached by the ripple during the animation.
     *
     * @return maximum alpha value [0, 1]
     */
    public float getRippleMaxAlpha() {
        return this.mRippleMaxAlpha;
    }

    /**
     * Set the maximum transparency reached by the ripple during the animation.
     *
     * @param rippleMaxAlpha maximum alpha value [0, 1]
     */
    public void setRippleMaxAlpha(final float rippleMaxAlpha) {
        this.mRippleMaxAlpha = rippleMaxAlpha;
    }

    /**
     * Get whether the ripple originates from the touching point, or the center of the view.
     *
     * @return true if centered
     */
    public boolean isRippleCentered() {
        return this.mRippleCentered;
    }

    /**
     * Set whether the ripple originates from the touching point, or the center of the view.
     *
     * @param rippleCentered true if centered
     */
    public void setRippleCentered(final boolean rippleCentered) {
        this.mRippleCentered = rippleCentered;
    }

    /**
     * If the radius is the size of the view, the radius size is reduced by this amount.
     *
     * @return padding amount in pixels
     */
    public int getRipplePadding() {
        return this.mRipplePadding;
    }

    /**
     * If the radius is the size of the view, the radius size is reduced by this amount.
     *
     * @param ripplePadding padding amount in pixels
     */
    public void setRipplePadding(final int ripplePadding) {
        this.mRipplePadding = ripplePadding;
    }

    /**
     * Get the radius of the ripple.
     *
     * @return radius or -1 if full size of the view
     */
    public float getRippleRadius() {
        return this.mRippleRadius;
    }

    /**
     * Set the radius of the ripple.
     *
     * @param rippleRadius radius or -1 if full size of the view
     */
    public void setRippleRadius(final float rippleRadius) {
        this.mRippleRadius = rippleRadius;
    }

    /**
     * Get when the ripple animation will be played: onTap, onTouchDown or onTouchUp.
     * 
     * @return current trigger
     */
    public Triggers getRippleAnimationTrigger() {
        return mRippleAnimationTrigger;
    }

    /**
     * Set when the ripple animation will be played: onTap, onTouchDown or onTouchUp.
     * 
     * @param trigger new trigger
     */
    public void setRippleAnimationTrigger(Triggers trigger) {
        this.mRippleAnimationTrigger = trigger;
    }

    /**
     * Get the time it takes for the ripple/highlight animation to complete.
     * 
     * @return time in milliseconds
     */
    public float getRippleAnimationDuration() {
        return this.mRippleAnimationDuration;
    }

    /**
     * Set the time it takes for the ripple/highlight animation to complete.
     * 
     * @param rippleAnimationDuration time in milliseconds
     */
    public void setRippleAnimationDuration(final float rippleAnimationDuration) {
        this.mRippleAnimationDuration = rippleAnimationDuration;
    }

    /**
     * Get the number of frames the animation is divided into.
     * 
     * @return number of frames
     */
    public int getRippleAnimationFrames() {
        return this.mRippleAnimationFrames;
    }

    /**
     * Set the number of frames the animation is divided into.
     * 
     * @param rippleAnimationFrames number of frames
     */
    public void setRippleAnimationFrames(final int rippleAnimationFrames) {
        this.mRippleAnimationFrames = rippleAnimationFrames;
    }

    /**
     * Get for the ripple in what frame the animation goes from fade-in into fade-out.
     * 
     * @return frame
     */
    public int getRippleAnimationPeakFrame() {
        return this.mRippleAnimationPeakFrame;
    }

    /**
     * Set for the ripple in what frame the animation goes from fade-in into fade-out.
     * 
     * @param rippleAnimationPeakFrame frame
     */
    public void setRippleAnimationPeakFrame(final int rippleAnimationPeakFrame) {
        this.mRippleAnimationPeakFrame = rippleAnimationPeakFrame;
    }

    /**
     * Whether the zoom animation is active.
     * 
     * @return true if active
     */
    public boolean isZoomAnimation() {
        return this.mZoomAnimation;
    }

    /**
     * Activates and deactivates the zoom animation.
     * 
     * @param zoomAnimation true for active
     */
    public void setZoomAnimation(final boolean zoomAnimation) {
        this.mZoomAnimation = zoomAnimation;
    }

    /**
     * Get when the zoom animation will be played: onTap, onTouchDown or onTouchUp.
     * 
     * @return trigger
     */
    public Triggers getZoomAnimationTrigger() {
        return this.mZoomAnimationTrigger;
    }

    /**
     * Set when the zoom animation will be played: onTap, onTouchDown or onTouchUp.
     * 
     * @param trigger trigger
     */
    public void setZoomAnimationTrigger(Triggers trigger) {
        this.mZoomAnimationTrigger = trigger;
    }

    /**
     * Get the scale to which the view zooms.
     * 
     * @return scale
     */
    public float getZoomAnimationScale() {
        return this.mZoomAnimationScale;
    }

    /**
     * Set the scale to which the view zooms.
     * 
     * @param mZoomAnimationScale scale
     */
    public void setZoomAnimationScale(final float mZoomAnimationScale) {
        this.mZoomAnimationScale = mZoomAnimationScale;
    }

    /**
     * Get the time it takes for the zoom animation to complete.
     * 
     * @return time in milliseconds
     */
    public float getZoomAnimationDuration() {
        return this.mZoomAnimationDuration;
    }

    /**
     * Set the time it takes for the zoom animation to complete.
     * 
     * @param zoomAnimationDuration time in milliseconds
     */
    public void setZoomAnimationDuration(final float zoomAnimationDuration) {
        this.mZoomAnimationDuration = zoomAnimationDuration;
    }

    /**
     * Whether the highlight animation is active
     * 
     * @return true if active
     */
    public boolean isHighlightAnimation() {
        return this.mHighlightAnimation;
    }

    /**
     * Activate or deactivate the highlight animation
     * 
     * @param highlightAnimation true if active
     */
    public void setHighlightAnimation(final boolean highlightAnimation) {
        this.mHighlightAnimation = highlightAnimation;
    }

    /**
     * Get the color of the highlight effect.
     * 
     * @return highlight color
     */
    public int getHighlighColor() {
        return this.mHighlighColor;
    }

    /**
     * Set the color of the highlight effect. NOTE: Do not send color ids.
     * 
     * @param highlighColor highlight color
     */
    public void setHighlighColor(int highlighColor) {
        this.mHighlighColor = highlighColor;
        mHighlightPaint.setColor(highlighColor);
    }

    /**
     * Get the maximum transparency reached by the highlight during the animation.
     * 
     * @return highlight maximum alpha value [0, 1]
     */
    public float getHighlightMaxAlpha() {
        return this.mHighlightMaxAlpha;
    }

    /**
     * Set the maximum transparency reached by the highlight during the animation.
     * 
     * @param highlightMaxAlpha highlight maximum alpha value [0, 1]
     */
    public void setHighlightMaxAlpha(final float highlightMaxAlpha) {
        this.mHighlightMaxAlpha = highlightMaxAlpha;
    }

    /**
     * Get for the ripple in what frame the animation goes from fade-in into fade-out.
     * 
     * @return frame
     */
    public int getHighlightAnimationPeakFrame() {
        return this.mHighlightAnimationPeakFrame;
    }

    /**
     * Set for the ripple in what frame the animation goes from fade-in into fade-out.
     * 
     * @param highlightAnimationPeakFrame frame
     */
    public void setHighlightAnimationPeakFrame(final int highlightAnimationPeakFrame) {
        this.mHighlightAnimationPeakFrame = highlightAnimationPeakFrame;
    }

    /**
     * Get animation interpolator used for highlight and ripple.
     * 
     * @return interpolator
     */
    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    /**
     * Set interpolator used for highlight and ripple.
     * 
     * @param interpolator interpolator
     */
    public void setInterpolator(final Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    /**
     * Get animation interpolator used for zoom.
     * 
     * @return interpolator
     */
    public Interpolator getZoomInterpolator() {
        return this.mZoomInterpolator;
    }

    /**
     * Set animation interpolator used for zoom.
     * 
     * @param zoomInterpolator interpolator
     */
    public void setZoomInterpolator(final Interpolator zoomInterpolator) {
        this.mZoomInterpolator = zoomInterpolator;
    }
}
