package reimschussel.diyslider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import java.lang.Math;


public class DIYSlider extends View {
    private final float PADDING = 50f;

    private final float LINE_Y = 100f;
    private final float LINE_WIDTH = 500f;
    private final float LINE_STROKE_WIDTH = 25f;
    private final int LINE_ACTIVE_COLOR = getResources().getColor(R.color.colorPrimary);
    private final int LINE_INACTIVE_COLOR = Color.LTGRAY;

    private final float CIRCLE_RADIUS = 50f;
    private final int CIRCLE_INACTIVE_COLOR = getResources().getColor(R.color.colorPrimary);
    private final int CIRCLE_ACTIVE_COLOR = getResources().getColor(R.color.colorPrimaryDark);

    private final float TEXT_SIZE = 90f;

    private String mSliderValue;

    private PointF mCircleCenter, mLineStart, mLineEnd, mTextPos;
    private Paint mCirclePaint, mLinePaintActive, mLinePaintInactive, mTextPaint;

    private final float ALLOWED_TOUCH_DIFFERENCE = CIRCLE_RADIUS + 5f;
    private boolean mIsActive = false;


    public DIYSlider(Context context){
        super(context);

        mCircleCenter = new PointF(PADDING, LINE_Y);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(CIRCLE_INACTIVE_COLOR);
        mCirclePaint.setAntiAlias(true);

        mLineStart = new PointF(PADDING, LINE_Y);
        mLineEnd = new PointF(LINE_WIDTH + PADDING, LINE_Y);

        mLinePaintActive = new Paint();
        mLinePaintActive.setColor(LINE_ACTIVE_COLOR);
        mLinePaintActive.setAntiAlias(true);
        mLinePaintActive.setStrokeWidth(LINE_STROKE_WIDTH);

        mLinePaintInactive = new Paint();
        mLinePaintInactive.setColor(LINE_INACTIVE_COLOR);
        mLinePaintInactive.setAntiAlias(true);
        mLinePaintInactive.setStrokeWidth(LINE_STROKE_WIDTH);

        mSliderValue = "0";

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(CIRCLE_RADIUS);
        mTextPaint.setAntiAlias(true);

        mTextPos = new PointF(10f, 200f);

        invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas){
        super.onDraw(canvas);

        drawLineFromPoints(mLineStart, mCircleCenter, canvas, mLinePaintActive);
        drawLineFromPoints(mCircleCenter, mLineEnd, canvas, mLinePaintInactive);

        canvas.drawCircle(mCircleCenter.x, mCircleCenter.y, CIRCLE_RADIUS, mCirclePaint);

        canvas.drawText(mSliderValue, mTextPos.x, mTextPos.y, mTextPaint);
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
        if(mCircleCenter.x > mLineEnd.x){
            mCircleCenter.x = mLineEnd.x;
        } else if(mCircleCenter.x < mLineStart.x){
            mCircleCenter.x = mLineStart.x;
        }

        if(mCircleCenter.y != LINE_Y){
            mCircleCenter.y = LINE_Y;
        }
    }

    private boolean isOnLine(PointF point){
        return (point.x >= mLineStart.x - CIRCLE_RADIUS && point.x <= mLineEnd.x + CIRCLE_RADIUS &&
                point.y >= LINE_Y - LINE_STROKE_WIDTH/2 && point.y <= LINE_Y + LINE_STROKE_WIDTH/2);
    }

    private void updateTextValue(){
        int value = (int)((mCircleCenter.x - mLineStart.x)/(LINE_WIDTH) * 100);
        mSliderValue = Integer.toString(value);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                PointF eventPoint = new PointF(event.getX(), event.getY());
                if(distanceBetweenPoints(eventPoint, mCircleCenter) <= ALLOWED_TOUCH_DIFFERENCE ||
                    isOnLine(eventPoint)){
                    mIsActive = true;
                    mCirclePaint.setColor(CIRCLE_ACTIVE_COLOR);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if(mIsActive){
                    mCircleCenter.x = event.getX();
                    mCircleCenter.y = event.getY();
                }
                break;

            case MotionEvent.ACTION_UP:
                if(mIsActive){
                    mCircleCenter.x = event.getX();
                    mCircleCenter.y = event.getY();
                    mCirclePaint.setColor(CIRCLE_INACTIVE_COLOR);
                    mIsActive = false;
                }
                break;
        }

        clamp();
        updateTextValue();
        invalidate();
        return true;
    }
}
