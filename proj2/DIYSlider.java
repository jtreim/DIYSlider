package reimschussel.diyslider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class DIYSlider extends View {
    public interface OnDIYSliderChangeListener {
        void onStartObservingTouch(DIYSlider slider, ArrayList<Float> values);
        void onValueChanged(DIYSlider slider, ArrayList<Float> values);
        void onStopObservingTouch(DIYSlider slider, ArrayList<Float> values);
    }

    private class SliderThumb {
        private PointF mPos;
        private float mValue;
        private boolean mActive;
        private Paint mPaint;
        private float mRadius;

        SliderThumb(PointF pos, float value){
            mPos = pos;
            mValue = value;
            mActive = false;
            mPaint = new Paint();
            mPaint.setColor(THUMB_INACTIVE_COLOR);
            mPaint.setAntiAlias(true);
        }

        float getX(){
            return mPos.x;
        }
        float getY(){
            return mPos.y;
        }
        void setX(float x){
            mPos.x = x;
        }
        void setY(float y){
            mPos.y = y;
        }
        float getValue(){
            return mValue;
        }
        void setValue(float v){
            mValue = v;
        }
        boolean isActive(){
            return mActive;
        }
        void setActive(boolean isActive){
            mActive = isActive;
        }
        void setColor(int color){
            mPaint.setColor(color);
        }
        float getRadius(){
            return mRadius;
        }
        void setRadius(float radius){
            mRadius = radius;
        }


        void draw(Canvas canvas){
            canvas.drawCircle(mPos.x, mPos.y, mRadius, mPaint);
        }
    }

    private final float PADDING = 50f;

    private final float LINE_Y = 100f;
    private final float LINE_STROKE_WIDTH = 25f;
    private final int LINE_ACTIVE_COLOR = getResources().getColor(R.color.colorPrimary);
    private final int LINE_INACTIVE_COLOR = Color.LTGRAY;

    private final float THUMB_RADIUS = 50f;
    private final float ALLOWED_TOUCH_DIFFERENCE = THUMB_RADIUS + 5f;
    private final int THUMB_INACTIVE_COLOR = getResources().getColor(R.color.colorPrimary);
    private final int THUMB_ACTIVE_COLOR = getResources().getColor(R.color.colorPrimaryDark);

    private int mWidth, mWidthPercent, mHeight;
    private ArrayList<SliderThumb> mThumbs = new ArrayList<>();
    private PointF mLineStart, mLineEnd;
    private Paint mLinePaintActive, mLinePaintInactive;

    private float mMin, mMax;

    private SliderThumb mActiveThumb;

    private OnDIYSliderChangeListener mListener;

    public DIYSlider(Context context, int thumbCount, float min, float max, int widthPercent){
        super(context);

        mLineStart = new PointF(PADDING, LINE_Y);
        mLineEnd = new PointF(mWidth + PADDING, LINE_Y);

        mLinePaintActive = new Paint();
        mLinePaintActive.setColor(LINE_ACTIVE_COLOR);
        mLinePaintActive.setAntiAlias(true);
        mLinePaintActive.setStrokeWidth(LINE_STROKE_WIDTH);

        mLinePaintInactive = new Paint();
        mLinePaintInactive.setColor(LINE_INACTIVE_COLOR);
        mLinePaintInactive.setAntiAlias(true);
        mLinePaintInactive.setStrokeWidth(LINE_STROKE_WIDTH);

        mMin = min;
        mMax = max;

        // Default width if nothing happens.
        mWidth = 500;

        // Force 0 <= widthPercent <= 100
        mWidthPercent = widthPercent;
        if(mWidthPercent > 100) {
            mWidthPercent = 100;
        } else if(mWidthPercent < 0){
            mWidthPercent = 0;
        }

        // Default height if nothing happens.
        mHeight = (int)(THUMB_RADIUS * 2.0f + PADDING * 2.0f);

        // Default thumb positions/values if nothing happens.
        for(int i = 0; i < thumbCount; i++){
            PointF pos = new PointF(PADDING, LINE_Y);
            SliderThumb t = new SliderThumb(pos, mMin);
            t.setRadius(THUMB_RADIUS);
            mThumbs.add(t);
        }
        invalidate();
    }

    public void setListener(OnDIYSliderChangeListener listener){
        mListener = listener;
        mListener.onStartObservingTouch(this, getThumbValues());
    }

    public void removeListener(){
        mListener.onStopObservingTouch(this, getThumbValues());
        mListener = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        mWidth = 500;
        if(MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED){
            mWidth = MeasureSpec.getSize(widthMeasureSpec) * mWidthPercent/100 - (int)PADDING;
        }

        mLineEnd.x = mWidth;

        mHeight = (int)(THUMB_RADIUS * 2.0f + PADDING * 2.0f);
        if(MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED){
            mHeight = Math.min(mHeight, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(mWidth + (int)PADDING, mHeight);

        invalidate();
    }

    public void setup(){
        // Determine equidistant spacing between thumbs along line
        float thumbSpacing = mWidth/(float)(mThumbs.size());

        for(int i = 0; i < mThumbs.size(); i++){
            float x = thumbSpacing * (i) + PADDING;
            SliderThumb t = mThumbs.get(i);
            t.setX(x);
            float value = getPositionValueOnSlider(t.mPos);
            t.setValue(value);
        }
        invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas){
        super.onDraw(canvas);

        // Check number of thumbs in slider to determine where "active" line color starts/ends.
        // More than 1 thumb: "active" is between original first and last thumbs.
        // Exactly 1 thumb: "active" is from beginning of line to thumb.
        // 0 thumbs: No part of the line is "active".
        PointF activeLineStart, activeLineEnd;
        if(mThumbs.size() > 1){
            activeLineStart = mThumbs.get(0).mPos;
            activeLineEnd = mThumbs.get(mThumbs.size() - 1).mPos;
        } else if (mThumbs.size() == 1){
            activeLineStart = mLineStart;
            activeLineEnd = mThumbs.get(0).mPos;
        } else {
            activeLineStart = mLineEnd;
            activeLineEnd = mLineEnd;
        }

        drawLineFromPoints(mLineStart, mLineEnd, canvas, mLinePaintInactive);
        drawLineFromPoints(activeLineStart, activeLineEnd, canvas, mLinePaintActive);

        for(SliderThumb t : mThumbs){
            t.draw(canvas);
        }
    }

    private double distanceBetweenPoints(PointF start, PointF end){
        double xDiff = end.x - start.x;
        double yDiff = end.y - start.y;
        return Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
    }

    private void drawLineFromPoints(PointF start, PointF end, Canvas canvas, Paint mPaint){
        canvas.drawLine(start.x, start.y, end.x, end.y, mPaint);
    }

    private void clamp(){
        if (mActiveThumb.getX() > mLineEnd.x) {
            mActiveThumb.setX(mLineEnd.x);
        } else if (mActiveThumb.getX() < mLineStart.x) {
            mActiveThumb.setX(mLineStart.x);
        }

        if (mActiveThumb.getY() != LINE_Y) {
            mActiveThumb.setY(LINE_Y);
        }
    }

    private boolean isOnLine(PointF point){
        return (point.x >= mLineStart.x - ALLOWED_TOUCH_DIFFERENCE &&
                point.x <= mLineEnd.x + ALLOWED_TOUCH_DIFFERENCE &&
                point.y >= LINE_Y - ALLOWED_TOUCH_DIFFERENCE &&
                point.y <= LINE_Y + ALLOWED_TOUCH_DIFFERENCE);
    }

    private float getPositionValueOnSlider(PointF pos){
        float value = (pos.x - mLineStart.x) / (mWidth - PADDING);
        value = value * (mMax - mMin) + mMin;
        return value;
    }

    private void updateThumbValue(){
        float value = getPositionValueOnSlider(mActiveThumb.mPos);
        mActiveThumb.setValue(value);

        // Only bug the listener if there's a thumb to report.
        if(mListener != null && mThumbs.size() > 0){
            mListener.onValueChanged(this, getThumbValues());
        }
    }

    private SliderThumb getClosestThumb(PointF touch){
        double closestThumbDistance = distanceBetweenPoints(mLineStart, mLineEnd);
        SliderThumb closest = null;
        for(SliderThumb t : mThumbs){
            double distanceToThumb = distanceBetweenPoints(touch, t.mPos);
            if(distanceToThumb < closestThumbDistance){
                closest = t;
                closestThumbDistance = distanceToThumb;
            }
        }

        return closest;
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                PointF eventPoint = new PointF(event.getX(), event.getY());
                if(isOnLine(eventPoint) && mThumbs.size() > 0){
                    mActiveThumb = getClosestThumb(eventPoint);
                    mActiveThumb.setColor(THUMB_ACTIVE_COLOR);
                    mActiveThumb.setActive(true);

                    mActiveThumb.setX(event.getX());
                    mActiveThumb.setY(event.getY());
                    clamp();
                    updateThumbValue();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if(mActiveThumb != null){
                    mActiveThumb.setX(event.getX());
                    mActiveThumb.setY(event.getY());
                    clamp();
                    updateThumbValue();
                }
                break;

            case MotionEvent.ACTION_UP:
                if(mActiveThumb != null){
                    mActiveThumb.setX(event.getX());
                    mActiveThumb.setY(event.getY());
                    clamp();
                    updateThumbValue();

                    // We're done listening to this nonsense.
                    mActiveThumb.setColor(THUMB_INACTIVE_COLOR);
                    mActiveThumb.setActive(false);
                    mActiveThumb = null;
                }
                break;
        }

        invalidate();
        return true;
    }

    public ArrayList<Float> getThumbValues(){
        ArrayList<Float> thumbValues = new ArrayList<>();
        for(SliderThumb t : mThumbs){
            Float value = t.getValue();
            thumbValues.add(value);
        }

        return thumbValues;
    }
}
