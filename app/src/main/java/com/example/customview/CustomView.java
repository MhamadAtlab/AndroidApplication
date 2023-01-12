package com.example.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class CustomView extends View {

    private boolean access=false;

    public boolean isAccess() {
        return access;
    }

    public void setAccess(boolean access) {
        this.access = access;
    }

    private Paint mArcPaint;
    private Paint mCentCirclePaint;
    private Paint mMidCirclePaint;
    private Paint mOutCirclePaint;
    private Paint mLinesPaint;

    private final static float DEFAULT_WIDTH_SMALL_LINE=7;
    private final static float DEFAULT_WIDTH_BIG_LINE=10;

    private float mWidthSmallLne;
    private float mWidthBigLine;

    private float mCentCircleRadius;
    private float mMidCircleRadius;
    private float mOutCircleRadius;
    private final static float CENT_CIRCLE_RADIUS = 120;
    private final static float MID_CIRCLE_RADIUS = 180;
    private final static float OUT_CIRCLE_RADIUS = 255;

    private float mMidStrokeWidth;
    private float mOutStrokeWidth;
    private final static float MID_STROKE_WIDTH = 120;
    private final static float OUT_STROKE_WIDTH = 30;



    private float mCurrentValue,mMaxValue,mMinValue;

    private final static float DEFAULT_MIN_VALUE = 0 ;
    private final static float DEFAULT_MAX_VALUE = 100;
    private final static float DEFAULT_CURRENT_VALUE = 25;


    private boolean mFirstEvent;
    private boolean mBlockEvent;
    private int mX_old = 0;
    private int mX_old_AfterBlock;

    private Point mCenterPoint;

    /**
     * boolean pour le double click
     */
    private Boolean mFirstClick = true;

    /**
     * initialisation de onCustomViewChangedListener
     */
    private OnCustomViewChangeListener onCustomViewChangedListener;


    public OnCustomViewChangeListener getOnCustomViewChangedListener() {
        return onCustomViewChangedListener;
    }

    public void setOnCustomViewChangeListener(OnCustomViewChangeListener cl){
        this.onCustomViewChangedListener=cl;
    }

    public void setmCurrentValue(float mCurrentValue) {
        this.mCurrentValue = mCurrentValue;
    }

    public float getmCurrentValue() {
        return mCurrentValue;
    }

    public CustomView(Context context) {
        super(context);
        init(context,null);
    }

    // Constructor
    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    /**
     * methode utilisee pour initialiser toutes les variables
     * @param context
     * @param attrs
     */
    private void init (Context context, @Nullable AttributeSet attrs){
        mCurrentValue=DEFAULT_CURRENT_VALUE;
        mMaxValue=DEFAULT_MAX_VALUE;
        mMinValue=DEFAULT_MIN_VALUE;

        mCentCircleRadius = dpToPixels(CENT_CIRCLE_RADIUS);
        mMidCircleRadius = dpToPixels(MID_CIRCLE_RADIUS);
        mOutCircleRadius = dpToPixels(OUT_CIRCLE_RADIUS);

        mMidStrokeWidth = dpToPixels(MID_STROKE_WIDTH);
        mOutStrokeWidth = dpToPixels(OUT_STROKE_WIDTH);

        mWidthSmallLne = dpToPixels(DEFAULT_WIDTH_SMALL_LINE);
        mWidthBigLine = dpToPixels(DEFAULT_WIDTH_BIG_LINE);

        mCentCirclePaint = new Paint();
        mCentCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCentCirclePaint.setColor(ContextCompat.getColor(context,R.color.centCircle));

        mMidCirclePaint = new Paint();
        mMidCirclePaint.setStyle(Paint.Style.STROKE);
        mMidCirclePaint.setStrokeWidth(mMidStrokeWidth);
        mMidCirclePaint.setColor(ContextCompat.getColor(context,R.color.midCircle));

        mOutCirclePaint = new Paint();
        mOutCirclePaint.setStyle(Paint.Style.STROKE);
        mOutCirclePaint.setStrokeWidth(mOutStrokeWidth);
        mOutCirclePaint.setColor(ContextCompat.getColor(context,R.color.outCircle));

        mArcPaint = new Paint();
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mMidStrokeWidth);
        mArcPaint.setColor(ContextCompat.getColor(context, R.color.arc));

        mArcPaint.setDither(true);                    // set the dither to true
        mArcPaint.setPathEffect(new CornerPathEffect(50) );   // set the path effect when they join.
        mArcPaint.setAntiAlias(true);

        mLinesPaint = new Paint();
        mLinesPaint.setColor(ContextCompat.getColor(context, R.color.lines));
        mLinesPaint.setStyle(Paint.Style.STROKE);
        mLinesPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    /**
     * methode utilisee pour convertir les pixels independants de la densite (dp) en pixels (px).
     * @param dp la valeur qu'on veut convertir
     * @return la valeur converter
     */
    private float dpToPixels(float dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp, getResources().getDisplayMetrics());
    }

    /**
     * methode appelee lorsque la "view" doit etre dessinee.
     * @param canvas objet canvas est utilise pour dessiner la view
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Point p1,p2;
        float z1,z2;
        float angle;
        float x1,y1,x2,y2;


        mCenterPoint = new Point(getWidth()/2, getHeight()/2);
        canvas.drawCircle(mCenterPoint.x,mCenterPoint.y,mCentCircleRadius,mCentCirclePaint);
        canvas.drawCircle(mCenterPoint.x,mCenterPoint.y,mMidCircleRadius,mMidCirclePaint);
        canvas.drawCircle(mCenterPoint.x,mCenterPoint.y,mOutCircleRadius,mOutCirclePaint);

        // RectF holds four float coordinates for a rectangle
        RectF oval = new RectF(mCenterPoint.x - mMidCircleRadius, mCenterPoint.y - mMidCircleRadius, mCenterPoint.x + mMidCircleRadius, mCenterPoint.y + mMidCircleRadius);
        canvas.drawArc(oval, -90, valueToAngle(mCurrentValue), false, mArcPaint);

        for (int i=0; i<16; i++){
            z1 = mMidCircleRadius - (mMidStrokeWidth/(4*(1+i%2)));
            z2 = mMidCircleRadius + (mMidStrokeWidth/(4*(1+i%2)));
            angle = (float) ((Math.PI/16)*i*2);
            x1 = (float) ((z1 * Math.sin(angle)) + (getWidth() / 2));
            y1 = (float) ((z1 * Math.cos(angle)) + (getHeight() / 2));
            x2 = (float) ((z2 * Math.sin(angle)) + (getWidth() / 2));
            y2 = (float) ((z2 * Math.cos(angle)) + (getHeight() / 2));
            p1 = new Point((int) x1, (int) y1);
            p2 = new Point((int) x2, (int) y2);
            if(i%2==0){
                mLinesPaint.setStrokeWidth(mWidthBigLine);
            }else {
                mLinesPaint.setStrokeWidth(mWidthSmallLne);
            }
            canvas.drawLine(p1.x,p1.y,p2.x,p2.y,mLinesPaint);
        }
    }

    /**
     * methode appelee pour determiner la taille et la position d'une vue
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int desiredWidth,desiredHeight;
        int height,width;

        desiredWidth= (int) (mOutCircleRadius*2+getPaddingLeft()+getPaddingRight()+mOutStrokeWidth);
        width =resolveSize(desiredWidth,widthMeasureSpec);

        desiredHeight = (int) (mOutCircleRadius*2+getPaddingLeft()+getPaddingRight()+mOutStrokeWidth);
        height =resolveSize(desiredHeight,heightMeasureSpec);

        // setMeasuredDimension() : This method must be called by onMeasure(int, int) to store
        // the measured width and measured height.
        setMeasuredDimension(width,height);

    }


    /**
     * convertir une valeur en angle
     * @param value
     * @return
     */
    private float valueToAngle(float value){
        return value*360/(mMaxValue-mMinValue);
    }

    /**
     * convertir un angle en valeur
     * @param angle
     * @return
     */
    private float angleToValue(float angle){
        return angle*(mMaxValue-mMinValue)/360;
    }

    /**
     * convertir une point en valeur entre 0 et 100
     * @param x
     * @param y
     * @return
     */
    private float positionToAngle(int x, int y){
        int xref,yref;
        float angle;
        xref = x - getWidth()/2;
        yref = getHeight()/2 - y;

        if(xref >0 && yref >0){
            angle = (float) (Math.atan((double)Math.abs(xref)/(double)Math.abs(yref))*360/(2*Math.PI));
        } else if (xref >0 && yref <0){
            angle = 180-(float) (Math.atan((double)Math.abs(xref)/(double)Math.abs(yref))*360/(2*Math.PI));
        } else if (xref <0 && yref <0){
            angle = 180+(float) (Math.atan((double)Math.abs(xref)/(double)Math.abs(yref))*360/(2*Math.PI));
        } else if (xref <0 && yref >0){
            angle = 360 -(float) (Math.atan((double)Math.abs(xref)/(double)Math.abs(yref))*360/(2*Math.PI));
        } else if (xref ==0 && yref >0){
            angle = 0;
        } else if (xref ==0 && yref <0){
            angle = 180;
        } else if (xref >0 && yref ==0){
            angle = 90;
        } else {
            angle = 270;
        }

        return angle;
    }


    /**
     * cette methode est appelee lorsqu'un evenement tactile est detecte sur une vue
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isAccess()) {
            // return super.onTouchEvent(event);
            int eventAction = event.getAction();

            // you may need the x/y location
            int x = (int) event.getX();
            int y = (int) event.getY();
            int xref, yref;
            xref = x - getWidth() / 2;
            yref = getHeight() / 2 - y;

            float r = (float) Math.sqrt(xref * xref + yref * yref);
            // put your code in here to handle the event
            //  if (r > (mMidCircleRadius - mMidStrokeWidth / 2) && r < (mMidCircleRadius + mMidStrokeWidth / 2)) {
            //
            switch (eventAction) {

                case MotionEvent.ACTION_DOWN:
                    mFirstEvent = true;
                    if (r < (mMidCircleRadius - mMidStrokeWidth / 2)) {
                        if (mFirstClick) {
                            mFirstClick = false;
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mFirstClick = true;
                                }
                            }, 500);
                        } else {
                            mCurrentValue = mMinValue;
                            onCustomViewChangedListener.onDoubleClick(mCurrentValue);
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (r > (mMidCircleRadius - mMidStrokeWidth / 2) && r < (mMidCircleRadius + mMidStrokeWidth / 2)) {
                        if (mFirstEvent) {
                            mCurrentValue = angleToValue(positionToAngle(x, y));
                            mX_old = xref;
                            mBlockEvent = false;
                            mFirstEvent = false;
                        } else {
                            if ((xref * mX_old) < 0 && yref > 0) {
                                if (mX_old > 0) mCurrentValue = mMinValue;
                                else mCurrentValue = mMaxValue;
                                mBlockEvent = true;
                                mX_old_AfterBlock = xref;
                            } else {
                                if (mBlockEvent) {
                                    if ((xref * mX_old_AfterBlock) < 0 && yref > 0) {
                                        mBlockEvent = false;
                                        mCurrentValue = angleToValue(positionToAngle(x, y));
                                        mX_old = xref;
                                    } else {
                                        mX_old_AfterBlock = xref;
                                    }
                                } else {
                                    mCurrentValue = angleToValue(positionToAngle(x, y));
                                    mX_old = xref;
                                }
                            }
                        }
                    }
                    break;

            }

        }
            onCustomViewChangedListener.onValueChanged(mCurrentValue);

            // tell the View to redraw the Canvas
            invalidate();

            // tell the View that we handled the event
            return true;

    }


    /**
     * interface qui permet de gerer les evenements qui se produisent dans l'application
     */
    public interface OnCustomViewChangeListener{
        void onValueChanged(float value);
        void onDoubleClick(float value);
    }


}
