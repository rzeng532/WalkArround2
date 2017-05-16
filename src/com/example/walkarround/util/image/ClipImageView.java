/**
 * Copyright (C) 2014-2017 CMCC All rights reserved
 */
package com.example.walkarround.util.image;

/**
 * TODO: description
 * Date: 2017-05-16
 *
 * @author Administrator
 */
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.*;
import android.widget.ImageView;

public class ClipImageView extends ImageView implements View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {
    private static final int BORDERDISTANCE = 0;
    public static final float DEFAULT_MAX_SCALE = 4.0F;
    public static final float DEFAULT_MID_SCALE = 2.0F;
    public static final float DEFAULT_MIN_SCALE = 1.0F;
    private float minScale;
    private float midScale;
    private float maxScale;
    private ClipImageView.MultiGestureDetector multiGestureDetector;
    private int borderlength;
    private int cutSize;
    private boolean isJusted;
    private final Matrix baseMatrix;
    private final Matrix drawMatrix;
    private final Matrix suppMatrix;
    private final RectF displayRect;
    private final float[] matrixValues;

    public ClipImageView(Context context) {
        this(context, (AttributeSet)null);
    }

    public ClipImageView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public ClipImageView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        this.minScale = 1.0F;
        this.midScale = 2.0F;
        this.maxScale = 4.0F;
        this.cutSize = 160;
        this.baseMatrix = new Matrix();
        this.drawMatrix = new Matrix();
        this.suppMatrix = new Matrix();
        this.displayRect = new RectF();
        this.matrixValues = new float[9];
        super.setScaleType(ScaleType.MATRIX);
        this.setOnTouchListener(this);
        this.multiGestureDetector = new ClipImageView.MultiGestureDetector(context);
    }

    public void setCutSize(int sizePx) {
        this.cutSize = sizePx;
    }

    private void configPosition() {
        super.setScaleType(ScaleType.MATRIX);
        Drawable d = this.getDrawable();
        if(d != null) {
            float viewWidth = (float)this.getWidth();
            float viewHeight = (float)this.getHeight();
            int drawableWidth = d.getIntrinsicWidth();
            int drawableHeight = d.getIntrinsicHeight();
            this.borderlength = (int)(viewWidth - 0.0F);
            float scale = 1.0F;
            if(drawableWidth <= drawableHeight) {
                this.baseMatrix.reset();
                scale = (float)this.borderlength / (float)drawableWidth;
                this.baseMatrix.postScale(scale, scale);
            } else {
                this.baseMatrix.reset();
                scale = (float)this.borderlength / (float)drawableHeight;
                this.baseMatrix.postScale(scale, scale);
            }

            this.baseMatrix.postTranslate((viewWidth - (float)drawableWidth * scale) / 2.0F, (viewHeight - (float)drawableHeight * scale) / 2.0F);
            this.resetMatrix();
            this.isJusted = true;
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        return this.multiGestureDetector.onTouchEvent(event);
    }

    @TargetApi(16)
    private void postOnAnimation(View view, Runnable runnable) {
        if(Build.VERSION.SDK_INT >= 16) {
            view.postOnAnimation(runnable);
        } else {
            view.postDelayed(runnable, 16L);
        }

    }

    public final float getScale() {
        this.suppMatrix.getValues(this.matrixValues);
        return this.matrixValues[0];
    }

    public void onGlobalLayout() {
        if(!this.isJusted) {
            this.configPosition();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    private void checkAndDisplayMatrix() {
        this.checkMatrixBounds();
        this.setImageMatrix(this.getDisplayMatrix());
    }

    private void checkMatrixBounds() {
        RectF rect = this.getDisplayRect(this.getDisplayMatrix());
        if(null != rect) {
            float deltaX = 0.0F;
            float deltaY = 0.0F;
            float viewWidth = (float)this.getWidth();
            float viewHeight = (float)this.getHeight();
            if(rect.top > (viewHeight - (float)this.borderlength) / 2.0F) {
                deltaY = (viewHeight - (float)this.borderlength) / 2.0F - rect.top;
            }

            if(rect.bottom < (viewHeight + (float)this.borderlength) / 2.0F) {
                deltaY = (viewHeight + (float)this.borderlength) / 2.0F - rect.bottom;
            }

            if(rect.left > (viewWidth - (float)this.borderlength) / 2.0F) {
                deltaX = (viewWidth - (float)this.borderlength) / 2.0F - rect.left;
            }

            if(rect.right < (viewWidth + (float)this.borderlength) / 2.0F) {
                deltaX = (viewWidth + (float)this.borderlength) / 2.0F - rect.right;
            }

            this.suppMatrix.postTranslate(deltaX, deltaY);
        }
    }

    private RectF getDisplayRect(Matrix matrix) {
        Drawable d = this.getDrawable();
        if(null != d) {
            this.displayRect.set(0.0F, 0.0F, (float)d.getIntrinsicWidth(), (float)d.getIntrinsicHeight());
            matrix.mapRect(this.displayRect);
            return this.displayRect;
        } else {
            return null;
        }
    }

    private void resetMatrix() {
        if(this.suppMatrix != null) {
            this.suppMatrix.reset();
            this.setImageMatrix(this.getDisplayMatrix());
        }
    }

    protected Matrix getDisplayMatrix() {
        this.drawMatrix.set(this.baseMatrix);
        this.drawMatrix.postConcat(this.suppMatrix);
        return this.drawMatrix;
    }

    public Bitmap clip() {
        Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);
        bitmap = Bitmap.createBitmap(bitmap, (this.getWidth() - this.borderlength) / 2, (this.getHeight() - this.borderlength) / 2, this.borderlength, this.borderlength);
        return bitmap;
    }

    private class AnimatedZoomRunnable implements Runnable {
        static final float ANIMATION_SCALE_PER_ITERATION_IN = 1.07F;
        static final float ANIMATION_SCALE_PER_ITERATION_OUT = 0.93F;
        private final float focalX;
        private final float focalY;
        private final float targetZoom;
        private final float deltaScale;

        public AnimatedZoomRunnable(float currentZoom, float targetZoom, float focalX, float focalY) {
            this.targetZoom = targetZoom;
            this.focalX = focalX;
            this.focalY = focalY;
            if(currentZoom < targetZoom) {
                this.deltaScale = 1.07F;
            } else {
                this.deltaScale = 0.93F;
            }

        }

        public void run() {
            ClipImageView.this.suppMatrix.postScale(this.deltaScale, this.deltaScale, this.focalX, this.focalY);
            ClipImageView.this.checkAndDisplayMatrix();
            float currentScale = ClipImageView.this.getScale();
            if((this.deltaScale <= 1.0F || currentScale >= this.targetZoom) && (this.deltaScale >= 1.0F || this.targetZoom >= currentScale)) {
                float delta = this.targetZoom / currentScale;
                ClipImageView.this.suppMatrix.postScale(delta, delta, this.focalX, this.focalY);
                ClipImageView.this.checkAndDisplayMatrix();
            } else {
                ClipImageView.this.postOnAnimation(ClipImageView.this, this);
            }

        }
    }

    private class MultiGestureDetector extends GestureDetector.SimpleOnGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        private final ScaleGestureDetector scaleGestureDetector;
        private final GestureDetector gestureDetector;
        private final float scaledTouchSlop;
        private VelocityTracker velocityTracker;
        private boolean isDragging;
        private float lastTouchX;
        private float lastTouchY;
        private float lastPointerCount;

        public MultiGestureDetector(Context context) {
            this.scaleGestureDetector = new ScaleGestureDetector(context, this);
            this.gestureDetector = new GestureDetector(context, this);
            this.gestureDetector.setOnDoubleTapListener(this);
            ViewConfiguration configuration = ViewConfiguration.get(context);
            this.scaledTouchSlop = (float)configuration.getScaledTouchSlop();
        }

        public boolean onScale(ScaleGestureDetector detector) {
            float scale = ClipImageView.this.getScale();
            float scaleFactor = detector.getScaleFactor();
            if(ClipImageView.this.getDrawable() != null && (scale < ClipImageView.this.maxScale && scaleFactor > 1.0F || scale > ClipImageView.this.minScale && scaleFactor < 1.0F)) {
                if(scaleFactor * scale < ClipImageView.this.minScale) {
                    scaleFactor = ClipImageView.this.minScale / scale;
                }

                if(scaleFactor * scale > ClipImageView.this.maxScale) {
                    scaleFactor = ClipImageView.this.maxScale / scale;
                }

                ClipImageView.this.suppMatrix.postScale(scaleFactor, scaleFactor, (float)(ClipImageView.this.getWidth() / 2), (float)(ClipImageView.this.getHeight() / 2));
                ClipImageView.this.checkAndDisplayMatrix();
            }

            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
        }

        public boolean onTouchEvent(MotionEvent event) {
            if(this.gestureDetector.onTouchEvent(event)) {
                return true;
            } else {
                this.scaleGestureDetector.onTouchEvent(event);
                float x = 0.0F;
                float y = 0.0F;
                int pointerCount = event.getPointerCount();

                for(int dx = 0; dx < pointerCount; ++dx) {
                    x += event.getX(dx);
                    y += event.getY(dx);
                }

                x /= (float)pointerCount;
                y /= (float)pointerCount;
                if((float)pointerCount != this.lastPointerCount) {
                    this.isDragging = false;
                    if(this.velocityTracker != null) {
                        this.velocityTracker.clear();
                    }

                    this.lastTouchX = x;
                    this.lastTouchY = y;
                }

                this.lastPointerCount = (float)pointerCount;
                switch(event.getAction()) {
                    case 0:
                        if(this.velocityTracker == null) {
                            this.velocityTracker = VelocityTracker.obtain();
                        } else {
                            this.velocityTracker.clear();
                        }

                        this.velocityTracker.addMovement(event);
                        this.lastTouchX = x;
                        this.lastTouchY = y;
                        this.isDragging = false;
                        break;
                    case 1:
                    case 3:
                        this.lastPointerCount = 0.0F;
                        if(this.velocityTracker != null) {
                            this.velocityTracker.recycle();
                            this.velocityTracker = null;
                        }
                        break;
                    case 2:
                        float var7 = x - this.lastTouchX;
                        float dy = y - this.lastTouchY;
                        if(!this.isDragging) {
                            this.isDragging = Math.sqrt((double)(var7 * var7 + dy * dy)) >= (double)this.scaledTouchSlop;
                        }

                        if(this.isDragging) {
                            if(ClipImageView.this.getDrawable() != null) {
                                ClipImageView.this.suppMatrix.postTranslate(var7, dy);
                                ClipImageView.this.checkAndDisplayMatrix();
                            }

                            this.lastTouchX = x;
                            this.lastTouchY = y;
                            if(this.velocityTracker != null) {
                                this.velocityTracker.addMovement(event);
                            }
                        }
                }

                return true;
            }
        }

        public boolean onDoubleTap(MotionEvent event) {
            try {
                float scale = ClipImageView.this.getScale();
                float x = (float)(ClipImageView.this.getWidth() / 2);
                float y = (float)(ClipImageView.this.getHeight() / 2);
                if(scale < ClipImageView.this.midScale) {
                    ClipImageView.this.post(ClipImageView.this.new AnimatedZoomRunnable(scale, ClipImageView.this.midScale, x, y));
                } else if(scale >= ClipImageView.this.midScale && scale < ClipImageView.this.maxScale) {
                    ClipImageView.this.post(ClipImageView.this.new AnimatedZoomRunnable(scale, ClipImageView.this.maxScale, x, y));
                } else {
                    ClipImageView.this.post(ClipImageView.this.new AnimatedZoomRunnable(scale, ClipImageView.this.minScale, x, y));
                }
            } catch (Exception var5) {
                ;
            }

            return true;
        }
    }
}
