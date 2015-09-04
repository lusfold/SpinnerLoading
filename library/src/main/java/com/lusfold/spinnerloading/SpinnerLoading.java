package com.lusfold.spinnerloading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.lusfold.spinnerloading.Animation.CallbackAnimation;
import com.lusfold.spinnerloading.utils.SpinnerLoadingUtils;

import java.util.ArrayList;

/**
 * @author <a href="http://www.lusfold.com" target="_blank">Lusfold</a>
 */
public class SpinnerLoading extends View implements CallbackAnimation.TransformationListener {
    public static final int DEFAULT_DURATION = 1200;
    public static final int DEFAULT_itemCount = 8;
    public static final int DEFAULT_CIRCLE_COLOR = 0xff33A7ff;
    public static final int DEFAULT_CIRCLE_COLOR_OUT = 0x8033A7ff;
    public static final float DEFAULT_SCALE_RATE = 0.2f;
    public static final int DEFAULT_RADIUS = 20;
    public static final int DEFAULT_WIDTH_FACTOR = 14;
    public static final int DEFAULT_WIDTH_PADDING_FACTOR = 2;

    private int itemCount = DEFAULT_itemCount;
    private Paint paint = new Paint();
    private float handle_len_rate = 2f;
    private float radius = DEFAULT_RADIUS;
    private final float SCALE_RATE = DEFAULT_SCALE_RATE;
    private ArrayList<Circle> circlePaths = new ArrayList<>();
    private float mInterpolatedTime;
    private CallbackAnimation callbackAnimation;
    private float width;
    private float preFac1;
    private float preFac2;
    private float pi2;

    public SpinnerLoading(Context context) {
        super(context);
        init();
    }

    public SpinnerLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpinnerLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    @Override
    public void onApplyTrans(float interpolatedTime) {
        mInterpolatedTime = interpolatedTime;
        invalidate();
    }


    private void init() {
        pi2 = (float) (Math.PI / 2);
        paint.setColor(DEFAULT_CIRCLE_COLOR);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        reMakeFactors();
    }


    private void drawSpinner(Canvas canvas, int j, int i, float v, float handle_len_rate, float maxDistance) {

        final Circle circle1 = circlePaths.get(i);
        final Circle circle2 = circlePaths.get(j);

        float[] center1 = circle1.center;
        float[] center2 = circle2.center;
        float radius1 = circle1.radius;
        float radius2 = circle2.radius;

        float d = SpinnerLoadingUtils.getDistance(center1, center2);
        if (d < maxDistance) {
            float scale2 = 1 + SCALE_RATE * (1 - d / maxDistance);
            radius2 *= scale2;
        }

        if (radius1 == 0 || radius2 == 0) {
            return;
        }

        if (j == 1) {
            paint.setShader(new RadialGradient(center1[0], center1[1], radius * 2, DEFAULT_CIRCLE_COLOR_OUT, Color.TRANSPARENT, Shader.TileMode.CLAMP));
            canvas.drawCircle(center1[0], center1[1], radius1 * 3, paint);
            paint.setShader(null);
            canvas.drawCircle(center1[0], center1[1], radius1, paint);
        }

        canvas.drawCircle(center2[0], center2[1], radius2, paint);

        float u1, u2;

        if (d > maxDistance || d <= Math.abs(radius1 - radius2)) {
            return;
        } else if (d < radius1 + radius2) {
            u1 = (float) Math.acos((radius1 * radius1 + d * d - radius2 * radius2) /
                    (2 * radius1 * d));
            u2 = (float) Math.acos((radius2 * radius2 + d * d - radius1 * radius1) /
                    (2 * radius2 * d));
        } else {
            u1 = 0;
            u2 = 0;
        }
        float[] centermin = new float[]{center2[0] - center1[0], center2[1] - center1[1]};

        float angle1 = (float) Math.atan2(centermin[1], centermin[0]);
        float angle2 = (float) Math.acos((radius1 - radius2) / d);
        float angle1a = angle1 + u1 + (angle2 - u1) * v;
        float angle1b = angle1 - u1 - (angle2 - u1) * v;
        float angle2a = (float) (angle1 + Math.PI - u2 - (Math.PI - u2 - angle2) * v);
        float angle2b = (float) (angle1 - Math.PI + u2 + (Math.PI - u2 - angle2) * v);

        float[] p1a1 = SpinnerLoadingUtils.getVector(angle1a, radius1);
        float[] p1b1 = SpinnerLoadingUtils.getVector(angle1b, radius1);
        float[] p2a1 = SpinnerLoadingUtils.getVector(angle2a, radius2);
        float[] p2b1 = SpinnerLoadingUtils.getVector(angle2b, radius2);

        float[] p1a = new float[]{p1a1[0] + center1[0], p1a1[1] + center1[1]};
        float[] p1b = new float[]{p1b1[0] + center1[0], p1b1[1] + center1[1]};
        float[] p2a = new float[]{p2a1[0] + center2[0], p2a1[1] + center2[1]};
        float[] p2b = new float[]{p2b1[0] + center2[0], p2b1[1] + center2[1]};


        float[] p1_p2 = new float[]{p1a[0] - p2a[0], p1a[1] - p2a[1]};

        float totalRadius = (radius1 + radius2);
        float d2 = Math.min(v * handle_len_rate, SpinnerLoadingUtils.getLength(p1_p2) / totalRadius);
        d2 *= Math.min(1, d * 2 / (radius1 + radius2));
        radius1 *= d2;
        radius2 *= d2;

        float[] sp1 = SpinnerLoadingUtils.getVector(angle1a - pi2, radius1);
        float[] sp2 = SpinnerLoadingUtils.getVector(angle2a + pi2, radius2);
        float[] sp3 = SpinnerLoadingUtils.getVector(angle2b - pi2, radius2);
        float[] sp4 = SpinnerLoadingUtils.getVector(angle1b + pi2, radius1);

        Path path1 = new Path();
        path1.moveTo(p1a[0], p1a[1]);
        path1.cubicTo(p1a[0] + sp1[0], p1a[1] + sp1[1], p2a[0] + sp2[0], p2a[1] + sp2[1], p2a[0], p2a[1]);
        path1.lineTo(p2b[0], p2b[1]);
        path1.cubicTo(p2b[0] + sp3[0], p2b[1] + sp3[1], p1b[0] + sp4[0], p1b[1] + sp4[1], p1b[0], p1b[1]);
        path1.lineTo(p1a[0], p1a[1]);
        path1.close();
        canvas.drawPath(path1, paint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        circlePaths.get(0).center[0] = (float) (preFac1 + preFac2 * Math.cos(2 * Math.PI * mInterpolatedTime));
        circlePaths.get(0).center[1] = (float) (preFac1 + preFac2 * Math.sin(2 * Math.PI * mInterpolatedTime));
        for (int i = 1, l = circlePaths.size(); i < l; i++) {
            drawSpinner(canvas, i, 0, 0.6f, handle_len_rate, radius * 3f);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (radius * DEFAULT_WIDTH_FACTOR), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (radius * DEFAULT_WIDTH_FACTOR), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private void stopAnimation() {
        this.clearAnimation();
        postInvalidate();
    }

    private void startAnimation() {
        if (callbackAnimation == null) {
            callbackAnimation = new CallbackAnimation(this);
            callbackAnimation.setDuration(DEFAULT_DURATION);
            callbackAnimation.setInterpolator(new LinearInterpolator());
            callbackAnimation.setRepeatCount(Animation.INFINITE);
        }
        startAnimation(callbackAnimation);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (visibility == GONE || visibility == INVISIBLE) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    /**
     * @param itemCount
     */
    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    /**
     * @param radius
     */
    public void setCircleRadius(int radius) {
        this.radius = radius;
        reMakeFactors();
    }

    /**
     * @param mode
     */
    public void setPaintMode(int mode) {
        paint.setStyle(mode == 0 ? Paint.Style.STROKE : Paint.Style.FILL);
    }

    private void reMakeFactors() {
        circlePaths.clear();
        width = radius * DEFAULT_WIDTH_FACTOR;
        preFac1 = radius * (DEFAULT_WIDTH_FACTOR / 2);
        preFac2 = preFac1 - radius * (DEFAULT_WIDTH_PADDING_FACTOR / 2 + 1);
        Circle circlePath = new Circle();
        circlePath.center = new float[]{radius * (DEFAULT_WIDTH_FACTOR - DEFAULT_WIDTH_PADDING_FACTOR / 2 - 1), radius * DEFAULT_WIDTH_FACTOR / 2};
        circlePath.radius = radius / 4 * 3;
        circlePaths.add(circlePath);

        for (int i = 1; i <= itemCount; i++) {
            circlePath = new Circle();
            circlePath.center = new float[]{(float) (preFac1 + preFac2 * Math.cos(Math.PI * 2 * i / itemCount)), (float) (preFac1 + preFac2 * Math.sin(Math.PI * 2 * i / itemCount))};
            circlePath.radius = radius;
            circlePaths.add(circlePath);
        }
    }
}
