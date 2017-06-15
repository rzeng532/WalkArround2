/*
* Copyright (C) 2015 Vincent Mi
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.walkarround.base.view.roundedimageview;

import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView.ScaleType;
import com.example.walkarround.util.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Richard
 */
public class RoundedDrawable extends Drawable {

    public static final String TAG = "RoundedDrawable";
    private static final Logger MY_LOGGER = Logger.getLogger(RoundedDrawable.class.getSimpleName());
    public static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    private final RectF mBounds = new RectF();
    private final RectF mDrawableRect = new RectF();
    private final RectF mBitmapRect = new RectF();
    private final Bitmap mBitmap;
    private final Paint mBitmapPaint;
    private final int mBitmapWidth;
    private final int mBitmapHeight;
    private final RectF mBorderRect = new RectF();
    private final Paint mBorderPaint;
    private final Matrix mShaderMatrix = new Matrix();
    private final RectF mSquareCornersRect = new RectF();

    private Shader.TileMode mTileModeX = Shader.TileMode.CLAMP;
    private Shader.TileMode mTileModeY = Shader.TileMode.CLAMP;
    private boolean mRebuildShader = true;

    // [ topLeft, topRight, bottomLeft, bottomRight ]
    private float[] mCornerRadius = new float[4];
    private final boolean[] mCornersRounded = new boolean[]{true, true, true, true};

    private boolean mOval = false;
    private float mBorderWidth = 0;
    private ColorStateList mBorderColor = ColorStateList.valueOf(DEFAULT_BORDER_COLOR);
    private ScaleType mScaleType = ScaleType.FIT_CENTER;

    public RoundedDrawable(Bitmap bitmap) {
        mBitmap = bitmap;

        mBitmapWidth = bitmap.getWidth();
        mBitmapHeight = bitmap.getHeight();
        mBitmapRect.set(0, 0, mBitmapWidth, mBitmapHeight);

        mBitmapPaint = new Paint();
        mBitmapPaint.setStyle(Paint.Style.FILL);
        mBitmapPaint.setAntiAlias(true);

        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor.getColorForState(getState(), DEFAULT_BORDER_COLOR));
        mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    /**
     * Created a rounded drawable from a bitmap.
     * @param bitmap
     * @return
     */
    public static RoundedDrawable fromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            return new RoundedDrawable(bitmap);
        } else {
            return null;
        }
    }

    /**
     *
     * @param drawable
     * @return
     */
    public static Drawable fromDrawable(Drawable drawable) {
        if (drawable != null) {
            if (drawable instanceof RoundedDrawable) {
                // just return if it's already a RoundedDrawable
                return drawable;
            } else if (drawable instanceof LayerDrawable) {
                LayerDrawable ld = (LayerDrawable) drawable;
                int num = ld.getNumberOfLayers();

                // loop through layers to and change to RoundedDrawables if possible
                for (int i = 0; i < num; i++) {
                    Drawable d = ld.getDrawable(i);
                    ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d));
                }
                return ld;
            }

            // try to get a bitmap from the drawable and
            Bitmap bm = drawableToBitmap(drawable);
            if (bm != null) {
                return new RoundedDrawable(bm);
            }
        }
        return drawable;
    }

    /**
     * Generate a bitmap from a drawable
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap;
        int width = Math.max(drawable.getIntrinsicWidth(), 2);
        int height = Math.max(drawable.getIntrinsicHeight(), 2);
        try {
            bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
            MY_LOGGER.w( "Failed to create bitmap from drawable!");
            bitmap = null;
        }

        return bitmap;
    }

    public Bitmap getSourceBitmap() {
        return mBitmap;
    }

    @Override
    public boolean isStateful() {
        return mBorderColor.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        int newColor = mBorderColor.getColorForState(state, 0);
        if (mBorderPaint.getColor() != newColor) {
            mBorderPaint.setColor(newColor);
            return true;
        } else {
            return super.onStateChange(state);
        }
    }

    private void updateShaderMatrix() {
        float scale;
        float dx;
        float dy;

        switch (mScaleType) {
            case CENTER:
                mBorderRect.set(mBounds);
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2);

                mShaderMatrix.reset();
                mShaderMatrix.setTranslate((int) ((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f),
                        (int) ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f));
                break;

            case CENTER_CROP:
                mBorderRect.set(mBounds);
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2);

                mShaderMatrix.reset();

                dx = 0;
                dy = 0;

                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / (float) mBitmapHeight;
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f;
                } else {
                    scale = mBorderRect.width() / (float) mBitmapWidth;
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f;
                }

                mShaderMatrix.setScale(scale, scale);
                mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth / 2,
                        (int) (dy + 0.5f) + mBorderWidth / 2);
                break;

            case CENTER_INSIDE:
                mShaderMatrix.reset();

                if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) {
                    scale = 1.0f;
                } else {
                    scale = Math.min(mBounds.width() / (float) mBitmapWidth,
                            mBounds.height() / (float) mBitmapHeight);
                }

                dx = (int) ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f);
                dy = (int) ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f);

                mShaderMatrix.setScale(scale, scale);
                mShaderMatrix.postTranslate(dx, dy);

                mBorderRect.set(mBitmapRect);
                mShaderMatrix.mapRect(mBorderRect);
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2);
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL);
                break;

            default:
            case FIT_CENTER:
                mBorderRect.set(mBitmapRect);
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER);
                mShaderMatrix.mapRect(mBorderRect);
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2);
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL);
                break;

            case FIT_END:
                mBorderRect.set(mBitmapRect);
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.END);
                mShaderMatrix.mapRect(mBorderRect);
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2);
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL);
                break;

            case FIT_START:
                mBorderRect.set(mBitmapRect);
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.START);
                mShaderMatrix.mapRect(mBorderRect);
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2);
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL);
                break;

            case FIT_XY:
                mBorderRect.set(mBounds);
                mBorderRect.inset(mBorderWidth / 2, mBorderWidth / 2);
                mShaderMatrix.reset();
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL);
                break;
        }

        mDrawableRect.set(mBorderRect);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        mBounds.set(bounds);

        updateShaderMatrix();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mRebuildShader) {
            BitmapShader bitmapShader = new BitmapShader(mBitmap, mTileModeX, mTileModeY);
            if (mTileModeX == Shader.TileMode.CLAMP && mTileModeY == Shader.TileMode.CLAMP) {
                bitmapShader.setLocalMatrix(mShaderMatrix);
            }
            mBitmapPaint.setShader(bitmapShader);
            mRebuildShader = false;
        }

        if (mOval) {
            if (mBorderWidth > 0) {
                canvas.drawOval(mDrawableRect, mBitmapPaint);
                canvas.drawOval(mBorderRect, mBorderPaint);
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint);
            }
        } else {
            if (any(mCornersRounded)) {
                float lt = mCornerRadius[Corner.TOP_LEFT];
                float rt = mCornerRadius[Corner.TOP_RIGHT];
                float lb = mCornerRadius[Corner.BOTTOM_LEFT];
                float rb = mCornerRadius[Corner.BOTTOM_RIGHT];
                float[] radii = new float[]{lt, lt, rt, rt, rb, rb, lb, lb};
                Path path = new Path();
                path.addRoundRect(mDrawableRect, radii, Path.Direction.CW);
                canvas.drawPath(path, mBitmapPaint);
                if (mBorderWidth > 0) {
                    path = new Path();
                    path.addRoundRect(mBorderRect, radii, Path.Direction.CW);
                    canvas.drawPath(path, mBorderPaint);
//                    canvas.drawRoundRect(mBorderRect, radius, radius, mBorderPaint);
                    redrawBitmapForSquareCorners(canvas);
                    redrawBorderForSquareCorners(canvas);
                } else {
                    redrawBitmapForSquareCorners(canvas);
                }
            } else {
                canvas.drawRect(mDrawableRect, mBitmapPaint);
                if (mBorderWidth > 0) {
                    canvas.drawRect(mBorderRect, mBorderPaint);
                }
            }
        }
    }

    private void redrawBitmapForSquareCorners(Canvas canvas) {
        if (all(mCornersRounded)) {
            // no square corners
            return;
        }

        if (mCornerRadius[Corner.TOP_LEFT] == 0
                && mCornerRadius[Corner.TOP_RIGHT] == 0
                && mCornerRadius[Corner.BOTTOM_LEFT] == 0
                && mCornerRadius[Corner.BOTTOM_RIGHT] == 0) {
            // no round corners
            return;
        }

        float left = mDrawableRect.left;
        float top = mDrawableRect.top;
        float right = left + mDrawableRect.width();
        float bottom = top + mDrawableRect.height();

        if (!mCornersRounded[Corner.TOP_LEFT]) {
            mSquareCornersRect.set(left, top, left + mCornerRadius[Corner.TOP_LEFT], top + mCornerRadius[Corner.TOP_LEFT]);
            canvas.drawRect(mSquareCornersRect, mBitmapPaint);
        }

        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            mSquareCornersRect.set(right - mCornerRadius[Corner.TOP_RIGHT], top, right, mCornerRadius[Corner.TOP_RIGHT]);
            canvas.drawRect(mSquareCornersRect, mBitmapPaint);
        }

        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            mSquareCornersRect.set(right - mCornerRadius[Corner.BOTTOM_RIGHT], bottom - mCornerRadius[Corner.BOTTOM_RIGHT], right, bottom);
            canvas.drawRect(mSquareCornersRect, mBitmapPaint);
        }

        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            mSquareCornersRect.set(left, bottom - mCornerRadius[Corner.BOTTOM_LEFT], left + mCornerRadius[Corner.BOTTOM_LEFT], bottom);
            canvas.drawRect(mSquareCornersRect, mBitmapPaint);
        }
    }

    private void redrawBorderForSquareCorners(Canvas canvas) {
        if (all(mCornersRounded)) {
            // no square corners
            return;
        }

        if (mCornerRadius[Corner.TOP_LEFT] == 0
                && mCornerRadius[Corner.TOP_RIGHT] == 0
                && mCornerRadius[Corner.BOTTOM_LEFT] == 0
                && mCornerRadius[Corner.BOTTOM_RIGHT] == 0) {
            // no round corners
            return;
        }

        float left = mDrawableRect.left;
        float top = mDrawableRect.top;
        float right = left + mDrawableRect.width();
        float bottom = top + mDrawableRect.height();
        float offset = mBorderWidth / 2;

        if (!mCornersRounded[Corner.TOP_LEFT]) {
            canvas.drawLine(left - offset, top, left + mCornerRadius[Corner.TOP_LEFT], top, mBorderPaint);
            canvas.drawLine(left, top - offset, left, top + mCornerRadius[Corner.TOP_LEFT], mBorderPaint);
        }

        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            canvas.drawLine(right - mCornerRadius[Corner.TOP_RIGHT] - offset, top, right, top, mBorderPaint);
            canvas.drawLine(right, top - offset, right, top + mCornerRadius[Corner.TOP_RIGHT], mBorderPaint);
        }

        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            canvas.drawLine(right - mCornerRadius[Corner.BOTTOM_RIGHT] - offset, bottom, right + offset, bottom, mBorderPaint);
            canvas.drawLine(right, bottom - mCornerRadius[Corner.BOTTOM_RIGHT], right, bottom, mBorderPaint);
        }

        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            canvas.drawLine(left - offset, bottom, left + mCornerRadius[Corner.BOTTOM_LEFT], bottom, mBorderPaint);
            canvas.drawLine(left, bottom - mCornerRadius[Corner.BOTTOM_LEFT], left, bottom, mBorderPaint);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getAlpha() {
        return mBitmapPaint.getAlpha();
    }

    @Override
    public void setAlpha(int alpha) {
        mBitmapPaint.setAlpha(alpha);
        invalidateSelf();
    }

//    @Override
    public ColorFilter getColorFilter() {
        return mBitmapPaint.getColorFilter();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mBitmapPaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public void setDither(boolean dither) {
        mBitmapPaint.setDither(dither);
        invalidateSelf();
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mBitmapPaint.setFilterBitmap(filter);
        invalidateSelf();
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
    }

    /**
     * @return the corner radius.
     */
    public float[] getCornerRadius() {
        return mCornerRadius;
    }

    /**
     * @param corner the specific corner to get radius of.
     * @return the corner radius of the specified corner.
     */
    public float getCornerRadius(int corner) {
        return mCornersRounded[corner] ? mCornerRadius[corner] : 0f;
    }

    /**
     * Sets all corners to the specified radius.
     *
     * @param radius the radius.
     * @return
     */
    public RoundedDrawable setCornerRadius(float radius) {
        setCornerRadius(radius, radius, radius, radius);
        return this;
    }

    /**
     * Sets the corner radius of one specific corner.
     *
     * @param corner the corner.
     * @param radius the radius.
     * @return
     */
    public RoundedDrawable setCornerRadius(int corner, float radius) {
//    if (radius != 0 && mCornerRadius != 0 && mCornerRadius != radius) {
//      throw new IllegalArgumentException("Multiple nonzero corner radii not yet supported.");
//    }

        mCornerRadius[corner] = 0;
        mCornersRounded[corner] = radius > 0;

        return this;
    }

    /**
     * Sets the corner radii of all the corners.
     *
     * @param topLeft     top left corner radius.
     * @param topRight    top right corner radius
     * @param bottomRight bototm right corner radius.
     * @param bottomLeft  bottom left corner radius.
     * @return
     */
    public RoundedDrawable setCornerRadius(float topLeft, float topRight, float bottomRight,
                                           float bottomLeft) {
        Set<Float> radiusSet = new HashSet<>(4);
        radiusSet.add(topLeft);
        radiusSet.add(topRight);
        radiusSet.add(bottomRight);
        radiusSet.add(bottomLeft);

        radiusSet.remove(0f);

//    if (radiusSet.size() > 1) {
//      throw new IllegalArgumentException("Multiple nonzero corner radii not yet supported.");
//    }

//    if (!radiusSet.isEmpty()) {
//      float radius = radiusSet.iterator().next();
//      if (Float.isInfinite(radius) || Float.isNaN(radius) || radius < 0) {
//        throw new IllegalArgumentException("Invalid radius value: " + radius);
//      }
//      mCornerRadius = radius;
//    } else {
//      mCornerRadius = 0f;
//    }
        mCornerRadius[Corner.TOP_LEFT] = topLeft;
        mCornerRadius[Corner.TOP_RIGHT] = topRight;
        mCornerRadius[Corner.BOTTOM_RIGHT] = bottomRight;
        mCornerRadius[Corner.BOTTOM_LEFT] = bottomLeft;

        mCornersRounded[Corner.TOP_LEFT] = topLeft > 0;
        mCornersRounded[Corner.TOP_RIGHT] = topRight > 0;
        mCornersRounded[Corner.BOTTOM_RIGHT] = bottomRight > 0;
        mCornersRounded[Corner.BOTTOM_LEFT] = bottomLeft > 0;
        return this;
    }

    public float getBorderWidth() {
        return mBorderWidth;
    }

    public RoundedDrawable setBorderWidth(float width) {
        mBorderWidth = width;
        mBorderPaint.setStrokeWidth(mBorderWidth);
        return this;
    }

    public int getBorderColor() {
        return mBorderColor.getDefaultColor();
    }

    public RoundedDrawable setBorderColor(int color) {
        return setBorderColor(ColorStateList.valueOf(color));
    }

    public ColorStateList getBorderColors() {
        return mBorderColor;
    }

    public RoundedDrawable setBorderColor(ColorStateList colors) {
        mBorderColor = colors != null ? colors : ColorStateList.valueOf(0);
        mBorderPaint.setColor(mBorderColor.getColorForState(getState(), DEFAULT_BORDER_COLOR));
        return this;
    }

    public boolean isOval() {
        return mOval;
    }

    public RoundedDrawable setOval(boolean oval) {
        mOval = oval;
        return this;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    public RoundedDrawable setScaleType(ScaleType scaleType) {
        if (scaleType == null) {
            scaleType = ScaleType.FIT_CENTER;
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            updateShaderMatrix();
        }
        return this;
    }

    public Shader.TileMode getTileModeX() {
        return mTileModeX;
    }

    public RoundedDrawable setTileModeX(Shader.TileMode tileModeX) {
        if (mTileModeX != tileModeX) {
            mTileModeX = tileModeX;
            mRebuildShader = true;
            invalidateSelf();
        }
        return this;
    }

    public Shader.TileMode getTileModeY() {
        return mTileModeY;
    }

    public RoundedDrawable setTileModeY(Shader.TileMode tileModeY) {
        if (mTileModeY != tileModeY) {
            mTileModeY = tileModeY;
            mRebuildShader = true;
            invalidateSelf();
        }
        return this;
    }

    private static boolean only(int index, boolean[] booleans) {
        for (int i = 0, len = booleans.length; i < len; i++) {
            if (booleans[i] != (i == index)) {
                return false;
            }
        }
        return true;
    }

    private static boolean any(boolean[] booleans) {
        for (boolean b : booleans) {
            if (b) {
                return true;
            }
        }
        return false;
    }

    private static boolean all(boolean[] booleans) {
        for (boolean b : booleans) {
            if (b) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return
     */
    public Bitmap toBitmap() {
        return drawableToBitmap(this);
    }
}
