package com.guc.testjgpush.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.guc.testjgpush.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by guc on 2018/12/17.
 * 描述：自定义Pie
 */
public class MyPieChartView extends View {

    private static final String TAG = "MyPieChartView";
    private final int MIN_HEIGHT;
    private final int MIN_WIDTH;
    private Context mContext;
    private Paint mPaint;
    private List<PieData> mPieDatas;

    //圆弧半径
    private int mRadius;
    private int mRadiusInner;
    //圆弧中心点小圆点的圆心半径
    private int mCenterPointRadius;
    //指示线宽度
    private int mLineWidth;
    //圆弧开始绘制的角度
    private float mStartAngle = 0;

    private int mBgColor;
    private int mInnerCicleColor;
    private int mCenterX, mCenterY;//圆心x,y点坐标
    private Rect mTextRect;

    //是否展示文字
    private boolean isShowRateText = true;
    private boolean isDrawCenterText = true;
    private int mTextSize4Describe;
    private float mSumScore;

    public MyPieChartView(Context context) {
        this(context, null);
    }

    public MyPieChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyPieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        MIN_HEIGHT = dp2px(150);
        MIN_WIDTH = dp2px(300);
        initAttrs(attrs, defStyleAttr);
        initPaint();

        mPieDatas = new ArrayList<>();
        mPieDatas.add(new PieData(5, "考勤", getResources().getColor(R.color.colorScoreRed)));
        mPieDatas.add(new PieData(10, "基础信息", getResources().getColor(R.color.colorScoreOrange)));
        mPieDatas.add(new PieData(25, "治安防范", getResources().getColor(R.color.colorScoreYellow)));
        mPieDatas.add(new PieData(20, "案件办理", getResources().getColor(R.color.colorScoreGreenLight)));
        mPieDatas.add(new PieData(10, "隐患盘查", getResources().getColor(R.color.colorScoreGreen)));
    }

    public void setDatas(List<PieData> datas) {
        if (mPieDatas == null) {
            mPieDatas = new ArrayList<>();
        } else {
            mPieDatas.clear();
        }
        mPieDatas.addAll(datas);
        invalidate();
    }
    private void initAttrs(AttributeSet attrs, int defStyleAttr) {
        TypedArray array = mContext.obtainStyledAttributes(attrs, R.styleable.MyPieChartView, defStyleAttr, 0);
        mBgColor = array.getColor(R.styleable.MyPieChartView_bgColor, Color.WHITE);
        mInnerCicleColor = array.getColor(R.styleable.MyPieChartView_innerCircleColor, Color.WHITE);
        mRadius = (int) array.getDimension(R.styleable.MyPieChartView_radius, dp2px(70));
        mRadiusInner = (int) array.getDimension(R.styleable.MyPieChartView_radius, dp2px(40));
        mCenterPointRadius = (int) array.getDimension(R.styleable.MyPieChartView_radiusCenterPoint, dp2px(2));
        mTextSize4Describe = (int) array.getDimension(R.styleable.MyPieChartView_textSize4Describe, 32);
        mLineWidth = (int) array.getDimension(R.styleable.MyPieChartView_lineWith, dp2px(2));
        isDrawCenterText = array.getBoolean(R.styleable.MyPieChartView_isDrawCenterText, true);
        array.recycle();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mTextRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            //边沿线和文字所占的长度：(xOffset + yOffset + textRect.width())
            heightSize = MIN_HEIGHT;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            if (heightSize < MIN_HEIGHT) {
                heightSize = MIN_HEIGHT;
            }
        }
        if (widthMode == MeasureSpec.AT_MOST) {

            widthSize = MIN_WIDTH;
        } else {
            if (widthSize < MIN_WIDTH) {
                widthSize = MIN_WIDTH;
            }
        }
        //保存测量结果
        mCenterX = widthSize / 2;
        mCenterY = heightSize / 2;
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(mBgColor);
        mPaint.setStyle(Paint.Style.FILL);
        if (mPieDatas != null && mPieDatas.size() > 0) {//开始绘制
            calculateRaleList(mPieDatas);
            PieData data;
            //1.绘制圆饼
            RectF rectF = new RectF(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);
            RectF rectF2 = new RectF(mCenterX - mRadius - dp2px(4), mCenterY - mRadius - dp2px(4), mCenterX + mRadius + dp2px(4), mCenterY + mRadius + dp2px(4));
            for (int i = 0; i < mPieDatas.size(); i++) {
                data = mPieDatas.get(i);
                mPaint.setColor(data.colorLine);
                float sweepAngle = data.proportion * (360);
                canvas.drawArc(rectF, mStartAngle, sweepAngle, true, mPaint);
                dealPoint(rectF2, mStartAngle, (data.proportion * 360) / 2, data);
                mStartAngle += sweepAngle;
            }
            //(2)处理每块圆饼弧的中心点，绘制折线，显示对应的文字
            if (isShowRateText) {
                drawableIndicateAndDescribe(canvas, mPieDatas);
            }
            //(3)绘制内部中空的圆
            mPaint.setColor(mInnerCicleColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mCenterX, mCenterY, mRadiusInner, mPaint);
            if (isDrawCenterText) {
                mPaint.setColor(Color.BLACK);
                mPaint.setTextSize(mTextSize4Describe);
                String sum = String.format("%d", (int) mSumScore);
                mPaint.getTextBounds(sum, 0, sum.length(), mTextRect);
                canvas.drawText(sum, mCenterX - mTextRect.width() / 2, mCenterY + mTextRect.height() / 2, mPaint);
            }
        } else {//暂无数据
            mPaint.setColor(Color.BLACK);
            mPaint.setTextSize(mTextSize4Describe);
            String describe = "暂无数据";
            mPaint.getTextBounds(describe, 0, describe.length(), mTextRect);
            canvas.drawText(describe, mCenterX - mTextRect.width() / 2, mCenterY + mTextRect.height() / 2, mPaint);
        }
    }

    /**
     * 处理每块圆饼弧的中心点，绘制折线，显示对应的文字
     *
     * @param canvas    画布
     * @param mPieDatas 数据
     */
    private void drawableIndicateAndDescribe(Canvas canvas, List<PieData> mPieDatas) {
        int leftNum = 0;
        int rightNum = 0;
        List<PieData> leftPieDatas = new ArrayList<>();//左侧数据
        List<PieData> rightPieDatas = new ArrayList<>();//右侧数据

        for (PieData pieData : mPieDatas) {
            if (pieData.centerPoint.x < mCenterX) {
                leftNum++;
                pieData.isRight = false;
                leftPieDatas.add(pieData);
            } else {
                rightNum++;
                pieData.isRight = true;
                rightPieDatas.add(pieData);
            }
        }
        Collections.sort(leftPieDatas);
        Collections.sort(rightPieDatas);
        Log.e(TAG, "drawableIndicateAndDescribe: left:" + leftNum + "\rright:" + rightNum);
        int perRightHeight = 2 * (mRadius + 2 * mCenterPointRadius) / rightNum;//右侧间距
        int perleftHeight = 2 * (mRadius + 2 * mCenterPointRadius) / leftNum; //左侧间距
        Log.e(TAG, "drawableIndicateAndDescribe: perleftHeight:" + perleftHeight + "perRightHeight:" + perRightHeight);
        PieData pieData;
        Path path;
        //画左侧指示点
        for (int i = 0; i < leftPieDatas.size(); i++) {
            pieData = leftPieDatas.get(i);
            mPaint.setStrokeWidth(mLineWidth);
            //画指示点
            mPaint.setColor(pieData.colorLine);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(pieData.centerPoint.x, pieData.centerPoint.y, mCenterPointRadius, mPaint);
            //划线
            mPaint.setStyle(Paint.Style.STROKE);
            path = new Path();
            path.moveTo(pieData.centerPoint.x, pieData.centerPoint.y);
            path.lineTo((mCenterX - mRadius - dp2px(4) - dp2px(5)), perleftHeight * i + perleftHeight / 2);
            path.lineTo(dp2px(10), perleftHeight * i + perleftHeight / 2);
            canvas.drawPath(path, mPaint);

            //写字
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(dp2px(0.75f));
            mPaint.setColor(pieData.colorDescribe);
            mPaint.setTextSize(mTextSize4Describe);

            String deccribe = pieData.describe;
            mPaint.getTextBounds(deccribe, 0, deccribe.length(), mTextRect);
            canvas.drawText(deccribe, dp2px(10), perleftHeight * i + perleftHeight / 2 + mTextRect.height(), mPaint);

            String score = String.format("%d(%.1f%%)", (int) pieData.score, pieData.proportion * 100);
            mPaint.setColor(pieData.colorScore);
            mPaint.getTextBounds(score, 0, score.length(), mTextRect);
            canvas.drawText(score, dp2px(10), perleftHeight * i + perleftHeight / 2 - 3 * mLineWidth / 2, mPaint);
        }
        //画右侧指示点
        for (int i = 0; i < rightPieDatas.size(); i++) {
            pieData = rightPieDatas.get(i);
            mPaint.setStrokeWidth(mLineWidth);
            //画指示点
            mPaint.setColor(pieData.colorLine);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(pieData.centerPoint.x, pieData.centerPoint.y, mCenterPointRadius, mPaint);
            //画线
            mPaint.setStyle(Paint.Style.STROKE);
            path = new Path();
            path.moveTo(pieData.centerPoint.x, pieData.centerPoint.y);
            path.lineTo((mCenterX + mRadius + dp2px(4)) + dp2px(5), perRightHeight * i + perRightHeight / 2);
            path.lineTo(2 * mCenterX - dp2px(10), perRightHeight * i + perRightHeight / 2);
            canvas.drawPath(path, mPaint);
            //写字
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(dp2px(0.75f));
            mPaint.setColor(pieData.colorDescribe);
            mPaint.setTextSize(mTextSize4Describe);

            String deccribe = pieData.describe;
            mPaint.getTextBounds(deccribe, 0, deccribe.length(), mTextRect);
            canvas.drawText(deccribe, 2 * mCenterX - dp2px(10) - mTextRect.width(), perRightHeight * i + perRightHeight / 2 + mTextRect.height(), mPaint);

            String score = String.format("%d(%.1f%%)", (int) pieData.score, pieData.proportion * 100);
            mPaint.setColor(pieData.colorScore);
            mPaint.getTextBounds(score, 0, score.length(), mTextRect);
            canvas.drawText(score, 2 * mCenterX - dp2px(10) - mTextRect.width(), perRightHeight * i + perRightHeight / 2 - 3 * mLineWidth / 2, mPaint);
        }
    }

    //处理获取每段弧中点坐标
    private void dealPoint(RectF rectF, float startAngle, float endAngle, PieData data) {
        Path path = new Path();
        //通过Path类画一个90度（180—270）的内切圆弧路径
        path.addArc(rectF, startAngle, endAngle);

        PathMeasure measure = new PathMeasure(path, false);

        float[] coords = new float[]{0f, 0f};
        //利用PathMeasure分别测量出各个点的坐标值coords
        int divisor = 1;
        measure.getPosTan(measure.getLength() / divisor, coords, null);
        float x = coords[0];
        float y = coords[1];
        Point point = new Point(Math.round(x), Math.round(y));
        data.centerPoint = point;
    }

    /**
     * 计算占比
     *
     * @param mPieDatas
     */
    private void calculateRaleList(List<PieData> mPieDatas) {
        float sum = 0f;
        for (int i = 0; i < mPieDatas.size(); i++) {
            sum += mPieDatas.get(i).score;
        }
        mSumScore = sum;
        for (int i = 0; i < mPieDatas.size(); i++) {
            mPieDatas.get(i).proportion = mPieDatas.get(i).score / sum;
        }
    }

    private int dp2px(final float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获取格式化的保留两位数的数
     */
    private String getFormatPercentRate(float dataValue) {
        DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format(dataValue);
    }

    /**
     * 饼状图数据
     */
    public class PieData implements Comparable<PieData> {
        public float score;
        public String describe;
        public int colorLine;
        public int colorDescribe;
        public int colorScore;
        public float proportion;//占比
        public Point centerPoint;//指示点位置
        public boolean isRight = true;//右侧的点

        public PieData(float score, String describe, int colorLine) {
            this(score, describe, colorLine, Color.parseColor("#999999"), Color.parseColor("#333333"));
        }

        public PieData(float score, String describe, int colorLine, int colorDescribe, int colorScore) {
            this.score = score;
            this.describe = describe;
            this.colorLine = colorLine;
            this.colorDescribe = colorDescribe;
            this.colorScore = colorScore;
        }

        @Override
        public int compareTo(@NonNull PieData pieData) {
            return this.centerPoint.y - pieData.centerPoint.y;
        }
    }
}
