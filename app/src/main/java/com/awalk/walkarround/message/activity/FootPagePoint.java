package com.awalk.walkarround.message.activity;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import com.awalk.walkarround.R;

/**
 * 页码标示
 */
public class FootPagePoint extends View {
    private Paint mPaint = null;
    /*总页数*/
    private int mTotalPage = 1;
    /*当前页数*/
    private int mCurrentPage = 0;
    /*当前页和非当前页的图片*/
    private Bitmap mBmpOff = null;
    private Bitmap mBmpOn = null;
    /*View的宽高*/
    private int mWidth;
    private int mHeight;
    /*V图片的显示大小*/
    private int mBmpWidth;
    /*间隔*/
    private int mSpaceWidth = 10;

    public FootPagePoint(Context context) {
        this(context, null);
    }

    public FootPagePoint(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FootPagePoint(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mWidth = displayMetrics.widthPixels;
        mHeight = displayMetrics.heightPixels;
        mBmpOff = BitmapFactory.decodeResource(getResources(), R.drawable.public_btn1_check_nm);
        mBmpOn = BitmapFactory.decodeResource(getResources(), R.drawable.public_btn1_check_pre);
        mBmpWidth = getResources().getDimensionPixelSize(
                R.dimen.page_indicator_width);
        mSpaceWidth = 10;
    }

    public void setOffBmp(Bitmap bmp) {
        if (bmp != null) {
            mBmpOff.recycle();
            mBmpOff = bmp;
        } else {
            mBmpOff.recycle();
            mBmpOff = BitmapFactory.decodeResource(getResources(),
                    R.drawable.public_btn1_check_nm);
        }
    }

    public void setOnBmp(Bitmap bmp) {
        if (bmp != null) {
            mBmpOn.recycle();
            mBmpOn = bmp;
        } else {
            mBmpOn.recycle();
            mBmpOn = BitmapFactory.decodeResource(getResources(),
                    R.drawable.public_btn1_check_pre);
        }
    }

    /**
     * 设置图片显示大小
     *
     * @param bmpWidth
     */
    public void setBmpWidth(int bmpWidth) {
        if (bmpWidth > 0) {
            mBmpWidth = bmpWidth;
        } else {
            mBmpWidth = getResources().getDimensionPixelSize(
                    R.dimen.page_indicator_width);
        }
    }

    /**
     * 设置间隔
     *
     * @param spaceWidth
     */
    public void setSpaceWidth(int spaceWidth) {
        if (spaceWidth > 0) {
            mSpaceWidth = spaceWidth;
        } else {
            mSpaceWidth = getResources().getDimensionPixelSize(
                    R.dimen.page_indicator_width);
        }
    }

    /**
     * 重新设置总个数以及当前选择项
     *
     * @param number
     * @param current
     */
    public void resetPagePoint(int number, int current) {
        mTotalPage = number;
        mCurrentPage = current;
    }

    /**
     * 切换当设定的项目
     *
     * @param nextOrPre
     */
    public void toPagePoint(int nextOrPre) {
        if (mCurrentPage == nextOrPre) {
            return;
        }
        mCurrentPage = nextOrPre;
        int totalWidth = mBmpWidth * mTotalPage + mSpaceWidth * (mTotalPage - 1);
        int startX = (mWidth - totalWidth) / 2;
        invalidate(startX, 0, startX + (mBmpWidth + mSpaceWidth) * mTotalPage,
                mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int totalWidth = mBmpWidth * mTotalPage + mSpaceWidth
                * (mTotalPage - 1);
        int dstRectHeight = 0;
        int dstRectWidth = 0;
        int startX = (mWidth - totalWidth) / 2;
        Bitmap tmpBmp = null;
        for (int i = 0; i < mTotalPage; i++) {
            if (mCurrentPage == i) {
                tmpBmp = mBmpOn;
            } else {
                tmpBmp = mBmpOff;
            }
            Rect srcRect = new Rect(0, 0, tmpBmp.getWidth(), tmpBmp.getHeight());
            dstRectWidth = mBmpWidth;
            dstRectHeight = mHeight;
            // 等比例缩放
            int widthScale = tmpBmp.getWidth() * dstRectHeight;
            int heightScale = tmpBmp.getHeight() * dstRectWidth;
            if (widthScale > heightScale) {
                dstRectHeight = heightScale / tmpBmp.getWidth();
            } else if (widthScale < heightScale) {
                dstRectWidth = widthScale / tmpBmp.getHeight();
            }
            int centerX = (mBmpWidth - dstRectWidth) / 2;
            int centerY = (mHeight - dstRectHeight) / 2;
            Rect dstRect = new Rect(startX + centerX, centerY, startX + centerX
                    + dstRectWidth, centerY + dstRectHeight);
            canvas.drawBitmap(tmpBmp, srcRect, dstRect, mPaint);
            startX += mSpaceWidth + mBmpWidth;
        }
    }
}