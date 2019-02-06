package reimschussel.diyslider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
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
        private float mAngle;

        SliderThumb(float angle, float value){
            mPos = new PointF();
            setAngle(angle);
            mValue = value;
            mActive = false;
            mPaint = new Paint();
            mPaint.setColor(THUMB_INACTIVE_COLOR);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
        }

        PointF getPos(){
            return mPos;
        }
        float getAngle(){
            return mAngle;
        }
        float getEllipseAngle(){
            float delta_x = mPos.x - mArcOrigin.x;
            float delta_y = mArcOrigin.y - mPos.y;
            float angle = (float) Math.toDegrees(Math.atan2(mWidth*delta_y, mHeight*delta_x));

            if(angle < 0f){
                angle = 0f;
            } else if(angle > 90f){
                angle = 90f;
            }
            return angle;
        }
        void setAngle(float angle){
            mAngle = angle;
            updatePos();
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

        void updatePos(){
            float cos = (float)Math.cos(Math.toRadians(mAngle));
            float sin = (float)Math.sin(Math.toRadians(mAngle));
            double a_2 = Math.pow(mWidth, 2);
            double b_2 = Math.pow(mHeight, 2);

            double k = 1/(Math.sqrt(b_2 * Math.pow(cos, 2) + a_2 * Math.pow(sin, 2)));

            mPos.x = (float)(mArcOrigin.x + cos * k * mWidth * mHeight);
            mPos.y = (float)(mArcOrigin.y - sin * k * mWidth * mHeight);
        }

        void draw(Canvas canvas){
            canvas.drawCircle(mPos.x, mPos.y, mRadius, mPaint);
        }
    }

    private final float PADDING = 50f;
    private final float THUMB_RADIUS = 50f;
    private final int TOTAL_PADDING = (int)PADDING + (int)THUMB_RADIUS;
    private final float ALLOWED_TOUCH_DIFFERENCE = THUMB_RADIUS + 10f;
    private final float LINE_STROKE_WIDTH = 20f;

    private final int LINE_ACTIVE_COLOR = getResources().getColor(R.color.colorPrimary);
    private final int LINE_INACTIVE_COLOR = Color.LTGRAY;

    private final int THUMB_INACTIVE_COLOR = getResources().getColor(R.color.colorPrimary);
    private final int THUMB_ACTIVE_COLOR = getResources().getColor(R.color.colorPrimaryDark);

    private Paint mLinePaintActive, mLinePaintInactive;

    private int mWidth, mWidthPercent, mHeight, mHeightPercent;
    private RectF mOvalBounds;
    private PointF mArcOrigin;

    private float mMin, mMax;

    private SliderThumb mActiveThumb;
    private ArrayList<SliderThumb> mThumbs = new ArrayList<>();

    private OnDIYSliderChangeListener mListener;

    public DIYSlider(Context context, int thumbCount, float min, float max, int widthPercent, int heightPercent){
        super(context);

        mLinePaintActive = new Paint();
        mLinePaintActive.setColor(LINE_ACTIVE_COLOR);
        mLinePaintActive.setAntiAlias(true);
        mLinePaintActive.setStrokeWidth(LINE_STROKE_WIDTH);
        mLinePaintActive.setStyle(Paint.Style.STROKE);

        mLinePaintInactive = new Paint();
        mLinePaintInactive.setColor(LINE_INACTIVE_COLOR);
        mLinePaintInactive.setAntiAlias(true);
        mLinePaintInactive.setStrokeWidth(LINE_STROKE_WIDTH);
        mLinePaintInactive.setStyle(Paint.Style.STROKE);

        mMin = min;
        mMax = max;

        // Defaults if nothing happens.
        mWidth = 500;
        mHeight = 500;

        mArcOrigin = new PointF(TOTAL_PADDING, mHeight + TOTAL_PADDING);

        // Left/bottom halves of oval could be off screen entirely.
        mOvalBounds = new RectF();
        mOvalBounds.left = mArcOrigin.x - mWidth;
        mOvalBounds.bottom = mArcOrigin.y + mHeight;
        mOvalBounds.right = mArcOrigin.x + mWidth;
        mOvalBounds.top = mArcOrigin.y - mHeight;

        // Force 0 <= widthPercent <= 100
        mWidthPercent = widthPercent;
        if(mWidthPercent > 100) {
            mWidthPercent = 100;
        } else if(mWidthPercent < 0){
            mWidthPercent = 0;
        }

        // Force 0 <= heightPercent <= 100
        mHeightPercent = heightPercent;
        if(mHeightPercent > 100){
            mHeightPercent = 100;
        } else if(mHeightPercent < 0){
            mHeightPercent = 0;
        }

        // Default thumbs to 0 positions/values if nothing happens.
        for(int i = 0; i < thumbCount; i++){
            SliderThumb t = new SliderThumb(0f, 0f);
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
            mWidth = MeasureSpec.getSize(widthMeasureSpec) * mWidthPercent/100;
        }

        mHeight = 500;
        if(MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED){
            mHeight = MeasureSpec.getSize(heightMeasureSpec) * mHeightPercent/100;
        }
        setMeasuredDimension(mWidth + 2*TOTAL_PADDING,
                            mHeight + 2*TOTAL_PADDING);

        mArcOrigin.x = TOTAL_PADDING;
        mArcOrigin.y = mHeight + TOTAL_PADDING;

        // Left/bottom halves of oval could be off screen entirely.
        mOvalBounds.left = mArcOrigin.x - mWidth;
        mOvalBounds.bottom = mArcOrigin.y + mHeight;
        mOvalBounds.right = mArcOrigin.x + mWidth;
        mOvalBounds.top = mArcOrigin.y - mHeight;

        invalidate();
    }


    public void setup(){
        // Determine equidistant angles for thumbs along arc
        float angleSpacing = 90f / (float)(mThumbs.size());

        for(int i = 0; i < mThumbs.size(); i++){
            float angle = angleSpacing * (i+1);
            mThumbs.get(i).setAngle(angle);

            float value = getAngleValueOnSlider(angle);
            mThumbs.get(i).setValue(value);
        }

        invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas){
        super.onDraw(canvas);
        // Check number of thumbs in slider to determine where "active" color starts/ends.
        // More than 1 thumb: "active" is between original first and last thumbs.
        // Exactly 1 thumb: "active" is from beginning of line to thumb.
        // 0|default: No part of the line is "active".
        float activeAngleStart = 0f;
        float activeAngleEnd = 0f;
        if(mThumbs.size() > 1){
            activeAngleStart = mThumbs.get(0).getEllipseAngle();
            activeAngleEnd = mThumbs.get(mThumbs.size() - 1).getEllipseAngle() - activeAngleStart;
        } else if (mThumbs.size() == 1){
            activeAngleStart = 0f;
            activeAngleEnd = mThumbs.get(0).getEllipseAngle();
        }

        activeAngleStart *= -1;
        activeAngleEnd *= -1;

        canvas.drawArc(mOvalBounds,0f, -90f, false, mLinePaintInactive);
        canvas.drawArc(mOvalBounds, activeAngleStart, activeAngleEnd, false, mLinePaintActive);

        for(SliderThumb t : mThumbs){
            t.draw(canvas);
        }
    }

    private double distanceBetweenPoints(PointF start, PointF end){
        double xDiff = end.x - start.x;
        double yDiff = end.y - start.y;
        return Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
    }

    private float getAngleFromOrigin(PointF point){
        float delta_x = point.x - mArcOrigin.x;
        float delta_y = mArcOrigin.y - point.y;
        float angle = (float) Math.toDegrees(Math.atan2(delta_y, delta_x));

        if(angle < 0f){
            angle = 0f;
        } else if(angle > 90f){
            angle = 90f;
        }
        return angle;
    }

    private float getAngleValueOnSlider(float angle){
        float valueRange = mMax - mMin;
        return valueRange * angle / 90f + mMin;
    }

    private void updateThumbValue(){
        float value = getAngleValueOnSlider(mActiveThumb.getAngle());
        mActiveThumb.setValue(value);

        // Only bug the listener if there's a thumb to report.
        if(mListener != null && mThumbs.size() > 0){
            mListener.onValueChanged(this, getThumbValues());
        }
    }

    private SliderThumb getClosestThumb(PointF touch){
        double closestThumbDistance = distanceBetweenPoints(mArcOrigin, touch);
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
        PointF eventPoint = new PointF(event.getX(), event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                SliderThumb closest = getClosestThumb(eventPoint);
                if(mThumbs.size() > 0 &&
                        distanceBetweenPoints(eventPoint, closest.mPos) <= ALLOWED_TOUCH_DIFFERENCE){
                    mActiveThumb = closest;
                    mActiveThumb.setColor(THUMB_ACTIVE_COLOR);
                    mActiveThumb.setActive(true);

                    float angle = getAngleFromOrigin(eventPoint);
                    mActiveThumb.setAngle(angle);
                    updateThumbValue();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if(mActiveThumb != null){
                    float angle = getAngleFromOrigin(eventPoint);
                    mActiveThumb.setAngle(angle);
                    updateThumbValue();
                }
                break;

            case MotionEvent.ACTION_UP:
                if(mActiveThumb != null){
                    float angle = getAngleFromOrigin(eventPoint);
                    mActiveThumb.setAngle(angle);
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
