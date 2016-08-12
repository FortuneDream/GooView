package com.example.q.gooview;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Created by YQ on 2016/8/5.
 * 粘性控件
 */
public class GooView extends View {
    private Paint mPaint;
    private PointF mStickCenter = new PointF(300f, 300f);//固定圆,F为Float单位
    private float mStickRadius = 20f;
    private PointF[] mStickPoints;

    private PointF mDragCenter = new PointF(300f, 300f);
    private float mDragRadius = 25f;
    private PointF[] mDragPoints;

    private PointF mControlPoint;
    private int mStatusBarHeight;

    private float farestDistance = 130.0f;
    private boolean isOutOfRange;//超出范围

    private FloatEvaluator evaluator = new FloatEvaluator();
    private boolean isDisappear;

    public PointF getmStickCenter() {
        return mStickCenter;
    }

    public void setmStickCenter(PointF mStickCenter) {
        this.mStickCenter = mStickCenter;
    }

    public float getmStickRadius() {
        return mStickRadius;
    }

    public void setmStickRadius(float mStickRadius) {
        this.mStickRadius = mStickRadius;
    }

    public PointF getmDragCenter() {
        return mDragCenter;
    }

    public void setmDragCenter(PointF mDragCenter) {
        this.mDragCenter = mDragCenter;
    }

    public float getmDragRadius() {
        return mDragRadius;
    }

    public void setmDragRadius(float mDragRadius) {
        this.mDragRadius = mDragRadius;
    }

    public float getFarestDistance() {
        return farestDistance;
    }

    public void setFarestDistance(float farestDistance) {
        this.farestDistance = farestDistance;
    }



    public GooView(Context context) {
        this(context, null);
    }

    public GooView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
    }

    //两个控制点
    //path.cubicTo();
    @Override
    protected void onDraw(Canvas canvas) {
        //计算连接点值,控制点,固定圆半径
        //1.根据两圆心距离获取固定圆半径(实现：固定圆缩小特效)
        float tempStickRadius = getTempStickRadius();
        //2.获取直线与圆的交点(每个圆有两个)
        float yOffset = mStickCenter.y - mDragCenter.y;
        float xOffset = mStickCenter.x - mDragCenter.x;
        double lineK = 0;
        if (xOffset != 0) {
            lineK = yOffset / xOffset;//斜率
        }
        mDragPoints = GeometryUtil.getIntersectionPoints(mDragCenter, mDragRadius, lineK);//得到拖拽圆两个交点坐标
        mStickPoints = GeometryUtil.getIntersectionPoints(mStickCenter, tempStickRadius, lineK);//得到固定圆(临时的半径)两个交点坐标
        mControlPoint = GeometryUtil.getMiddlePoint(mDragCenter, mStickCenter);//得到控制点的坐标


        //可以吧save理解成把之前的画布保存，而save之后的是在一个新的画布上画画，restore表示合并：save之前保存的画布，和save到restore之间的画布
        canvas.save();//保存画布状态（坐标系等）
        canvas.translate(0, -mStatusBarHeight);

        //画出最大范围(参考用)
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mStickCenter.x, mStickCenter.y, farestDistance, mPaint);
        mPaint.setStyle(Paint.Style.FILL);

        if (!isDisappear) {

            if (!isOutOfRange) {//不超出范围的时候才画
                Path path = new Path();
                //3.画连接部分
                //跳到某个点
                path.moveTo(mStickPoints[0].x, mStickPoints[0].y);
                //画贝塞尔曲线:第一个点是控制点,第二个点是目标点
                path.quadTo(mControlPoint.x, mControlPoint.y, mDragPoints[0].x, mDragPoints[0].y);
                //画直线
                path.lineTo(mDragPoints[1].x, mDragPoints[1].y);
                //第二条贝塞尔
                path.quadTo(mControlPoint.x, mControlPoint.y, mStickPoints[1].x, mStickPoints[1].y);
                path.close();//自动封闭（回到起始点）
                canvas.drawPath(path, mPaint);
                //1.画固定圆
                canvas.drawCircle(mStickCenter.x, mStickCenter.y, tempStickRadius, mPaint);
            }

            //2.画拖拽圆
            canvas.drawCircle(mDragCenter.x, mDragCenter.y, mDragRadius, mPaint);
        }
        canvas.restore();//恢复保存的状态,及坐标系恢复
    }

    //根据两圆心距离获取固定圆半径(实现：固定圆缩小特效)
    private float getTempStickRadius() {
        float distance = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
        //0.0f-1.0f
        float percent = Math.min(distance, farestDistance) / farestDistance;

        return evaluator.evaluate(percent, mStickRadius, mStickRadius * 0.2f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x;
        float y;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isOutOfRange = false;
                isDisappear = false;
                x = event.getRawX();
                y = event.getRawY();
                updateDragCenter(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                x = event.getRawX();
                y = event.getRawY();
                updateDragCenter(x, y);
                //处理断开事件
                float distance = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
                if (distance > farestDistance) {
                    isOutOfRange = true;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                //最后的处理
                if (isOutOfRange) {
                    //超出拖拽范围，断开，松手，消失
                    float d = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
                    if (d > farestDistance) {
                        //松手还没有放回去
                        isDisappear = true;
                        invalidate();
                    }
                    //超出拖拽范围，断开，放回去，恢复
                    updateDragCenter(mStickCenter.x, mStickCenter.y);
                } else {
                    //没有超过拖拽范围，松手，弹回去
                    final PointF tempDragCenter = new PointF(mDragCenter.x, mDragCenter.y);//固定拖拽圆的圆心
                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.0f);
                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            //0.0->1.0
                            float percent = valueAnimator.getAnimatedFraction();
                            PointF p = GeometryUtil.getPointByPercent(tempDragCenter, mStickCenter, percent);//根据松手时固定圆心和拖拽圆心的距离，和差值器，更新动画
                            updateDragCenter(p.x, p.y);
                        }
                    });
                    valueAnimator.setInterpolator(new OvershootInterpolator(4));
                    valueAnimator.setDuration(500);
                    valueAnimator.start();
                }
                break;
        }
        return true;
    }

    //更新拖拽圆圆心坐标,并重绘界面
    private void updateDragCenter(float x, float y) {
        mDragCenter.set(x, y);
        invalidate();
    }

    //鼠标和圆心之间，差了一个状态栏的高度，所以需要移动画布
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStatusBarHeight = Utils.getStatusBarHeight(this);//得到状态栏的高度,getWindowVisibleDisplayFrame(frame);
    }
}
