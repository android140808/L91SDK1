package cn.appscomm.pedometer.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import cn.appscomm.pedometer.model.CircleRing;
import cn.l11.appscomm.pedometer.activity.R;



/**
 * Created with Eclipse.
 * Author: Tim Liu  email:9925124@qq.com
 * Date: 14-3-20
 * Time: 22:14
 */
public class CircularRangeSeekBar extends View {

	// 画实心圆的画笔
	private Paint mCirclePaint;
	// 画圆环的画笔
	private Paint mRingPaint;
	// 画圆环背景的画笔
	private Paint mRingPaint_bg;
	//
	private Paint mStepRingPaint,mStepRingPaint_bg;
	private Paint mDistRingPaint,mDistRingPaint_bg;
	private Paint mCalRingPaint,mCalRingPaint_bg;
	private Paint mSleepRingPaint,mSleepRingPaint_bg;
	
	// 画字体的画笔
	private Paint mTextPaint;
	// 画字体1的画笔1
	private Paint mTextPaint1;
	// 圆形颜色
	private int mCircleColor;
	// 圆环颜色
	private int mRingColor;
	private int mRingColor_bg;
	// 半径
	private float mRadius;
	// 圆环半径
	private float mRingRadius;
	// 圆环宽度
	private float mStrokeWidth;
	// 圆心x坐标
	private int mXCenter;
	// 圆心y坐标
	private int mYCenter;
	// 字的长度
	private float mTxtWidth;
	// 字的高度
	private float mTxtHeight;
	// 字1的长度
	private float mTxtWidth1;
	// 字1的高度
	private float mTxtHeight1;
	private String text;
	private String text1;
	// 总进度
	private int mTotalProgress = 100;
	// 当前进度
	private int mProgress;
	private CircleRing cr;
//	private final  Paint paint;
	private final Context context;

	public CircularRangeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		
		// 获取自定义的属性
		initAttrs(context, attrs);
		initVariable();
	}
	
	public void setText1(String text1){
		this.text1 = text1;
	}

	private void initAttrs(Context context, AttributeSet attrs) {
		TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.CircularRangeSeekBar, 0, 0);
		mRadius = typeArray.getDimension(R.styleable.CircularRangeSeekBar_radius, 80);
		mStrokeWidth = typeArray.getDimension(R.styleable.CircularRangeSeekBar_strokeWidth, 10);
		mCircleColor = typeArray.getColor(R.styleable.CircularRangeSeekBar_circleColor, 0xFFFFFFFF);
		mRingColor = typeArray.getColor(R.styleable.CircularRangeSeekBar_ringColor, 0xFFFFFFFF);
		mRingColor_bg = typeArray.getColor(R.styleable.CircularRangeSeekBar_ringColorBg, 0xFFFFFFFF);
		
		mRingRadius = mRadius + mStrokeWidth / 2;
		this.text = "0%";
		this.text1 = context.getString(R.string.steps1) +  " 0";
		this.mProgress = 0;
		typeArray.recycle(); //一定要调用，否则这次的设定会对下次的使用造成影响
	}

	private void initStep() {
		mStepRingPaint = new Paint();
		mStepRingPaint.setAntiAlias(true);
		mStepRingPaint.setColor(Color.parseColor(context
				.getString(R.color.step_ring_color)));
		mStepRingPaint.setStyle(Paint.Style.STROKE);
		mStepRingPaint.setStrokeWidth(mStrokeWidth);

		mStepRingPaint_bg = new Paint();
		mStepRingPaint_bg.setAntiAlias(true);
		mStepRingPaint_bg.setColor(Color.parseColor(context
				.getString(R.color.step_ring_color_bg)));
		mStepRingPaint_bg.setStyle(Paint.Style.STROKE);
		mStepRingPaint_bg.setStrokeWidth(mStrokeWidth);
	}
	
	private void initDist() {
		mDistRingPaint = new Paint();
		mDistRingPaint.setAntiAlias(true);
		mDistRingPaint.setColor(Color.parseColor(context
				.getString(R.color.dist_ring_color)));
		mDistRingPaint.setStyle(Paint.Style.STROKE);
		mDistRingPaint.setStrokeWidth(mStrokeWidth);

		mDistRingPaint_bg = new Paint();
		mDistRingPaint_bg.setAntiAlias(true);
		mDistRingPaint_bg.setColor(Color.parseColor(context
				.getString(R.color.dist_ring_color_bg)));
		mDistRingPaint_bg.setStyle(Paint.Style.STROKE);
		mDistRingPaint_bg.setStrokeWidth(mStrokeWidth);
	}
	
	private void initCal() {
		mCalRingPaint = new Paint();
		mCalRingPaint.setAntiAlias(true);
		mCalRingPaint.setColor(Color.parseColor(context
				.getString(R.color.cal_ring_color)));
		mCalRingPaint.setStyle(Paint.Style.STROKE);
		mCalRingPaint.setStrokeWidth(mStrokeWidth);

		mCalRingPaint_bg = new Paint();
		mCalRingPaint_bg.setAntiAlias(true);
		mCalRingPaint_bg.setColor(Color.parseColor(context
				.getString(R.color.cal_ring_color_bg)));
		mCalRingPaint_bg.setStyle(Paint.Style.STROKE);
		mCalRingPaint_bg.setStrokeWidth(mStrokeWidth);
	}
	
	private void initSleep() {
		mSleepRingPaint = new Paint();
		mSleepRingPaint.setAntiAlias(true);
		mSleepRingPaint.setColor(Color.parseColor(context
				.getString(R.color.sleep_ring_color)));
		mSleepRingPaint.setStyle(Paint.Style.STROKE);
		mSleepRingPaint.setStrokeWidth(mStrokeWidth);

		mSleepRingPaint_bg = new Paint();
		mSleepRingPaint_bg.setAntiAlias(true);
		mSleepRingPaint_bg.setColor(Color.parseColor(context
				.getString(R.color.sleep_ring_color_bg)));
		mSleepRingPaint_bg.setStyle(Paint.Style.STROKE);
		mSleepRingPaint_bg.setStrokeWidth(mStrokeWidth);
	}
	
	private void initVariable() {
		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setColor(mCircleColor);
		mCirclePaint.setStyle(Paint.Style.FILL);
		
		mRingPaint = new Paint();
		mRingPaint.setAntiAlias(true);
		mRingPaint.setColor(mRingColor);
		mRingPaint.setStyle(Paint.Style.STROKE);
		mRingPaint.setStrokeWidth(mStrokeWidth);
		
		mRingPaint_bg = new Paint();
		mRingPaint_bg.setAntiAlias(true);
		mRingPaint_bg.setColor(mRingColor_bg);
		mRingPaint_bg.setStyle(Paint.Style.STROKE);
		mRingPaint_bg.setStrokeWidth(mStrokeWidth);
		
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setStyle(Paint.Style.FILL);
//		mTextPaint.setARGB(255, 255, 255, 255);// 白色
		mTextPaint.setColor(mRingColor);// 字体颜色设置成与圆环颜色一样
		mTextPaint.setTextSize(mRadius / 2);		
		FontMetrics fm = mTextPaint.getFontMetrics();
		mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);
		
		mTextPaint1 = new Paint();
		mTextPaint1.setAntiAlias(true);
		mTextPaint1.setStyle(Paint.Style.FILL);
//		mTextPaint.setARGB(255, 255, 255, 255);// 白色
		mTextPaint1.setColor(mRingColor);// 字体颜色设置成与圆环颜色一样
		mTextPaint1.setTextSize(mRadius / 4);		
		FontMetrics fm1 = mTextPaint1.getFontMetrics();
		mTxtHeight1 = (int) Math.ceil(fm1.descent - fm1.ascent);
		
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (cr == null) {
			cr = new CircleRing();
			cr.setTargetStep(7000);
			cr.setTargetDist(3);
			cr.setTargetCal(350);
			cr.setTargetSleep(8);
		}
		
		mXCenter = getWidth() / 2;
		mYCenter = dip2px(context, 75);//getHeight() / 2;
		
		canvas.drawCircle(mXCenter, mYCenter, mRadius, mCirclePaint);
		
		
		RectF oval1 = new RectF();
		oval1.left = (mXCenter - mRingRadius);
		oval1.top = (mYCenter - mRingRadius);
		oval1.right = mRingRadius * 2 + (mXCenter - mRingRadius);
		oval1.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);
		canvas.drawArc(oval1, 0, 360, false, mRingPaint_bg); //
		canvas.drawArc(oval1, 0, 270, false, mRingPaint); //
		
		initStep();
		initDist();
		initCal();
		initSleep();
		
		// 步数环的背景颜色绘制
		RectF oval = new RectF();
		oval.left = mXCenter - mRingRadius;
		oval.top = mYCenter - mRingRadius;
		oval.right = mRingRadius * 2 + (mXCenter - mRingRadius);
		oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);
		canvas.drawArc(oval, 0, 360, false, mStepRingPaint);
		canvas.drawArc(oval, -90, (float)(cr.getRealStep()/cr.getTargetStep()) * 360, false, mStepRingPaint_bg);
		
		// 距离环的背景颜色绘制
		oval.left = mXCenter - mRingRadius + dip2px(context, 10);
		oval.top = mYCenter - mRingRadius + dip2px(context, 10);
		oval.right = mRingRadius * 2 + (mXCenter - mRingRadius - dip2px(context, 10));
		oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius - dip2px(context, 10));
		canvas.drawArc(oval, 0, 360, false, mDistRingPaint);
		canvas.drawArc(oval, -90, (float)(cr.getRealDist()/cr.getTargetDist()) * 360, false, mDistRingPaint_bg);
		
		// 卡路里环的背景颜色绘制
		oval.left = mXCenter - mRingRadius + dip2px(context, 20);
		oval.top = mYCenter - mRingRadius + dip2px(context, 20);
		oval.right = mRingRadius * 2 + (mXCenter - mRingRadius - dip2px(context, 20));
		oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius - dip2px(context, 20));
		canvas.drawArc(oval, 0, 360, false, mCalRingPaint);
		canvas.drawArc(oval, -90, (float)(cr.getRealCal()/cr.getTargetCal()) * 360, false, mCalRingPaint_bg);
		
		// 睡眠环的背景颜色绘制
		oval.left = mXCenter - mRingRadius + dip2px(context, 30);
		oval.top = mYCenter - mRingRadius + dip2px(context, 30);
		oval.right = mRingRadius * 2 + (mXCenter - mRingRadius - dip2px(context, 30));
		oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius - dip2px(context, 30));
		canvas.drawArc(oval, 0, 360, false, mSleepRingPaint);
		canvas.drawArc(oval, -90, (float)(cr.getRealSleep()/cr.getTargetSleep()) * 360, false, mSleepRingPaint_bg);
		
		// 文字		
		/**
		mTxtWidth1 = mTextPaint1.measureText(text1, 0, text1.length());
		canvas.drawText(text1, mXCenter - mTxtWidth1 / 2  , mYCenter + mTxtHeight/ 4 + mTxtHeight1 - 20 , mTextPaint1);
		*/
		/**
		if (mProgress >= 0 && false) {
			RectF oval = new RectF();
			oval.left = (mXCenter - mRingRadius);
			oval.top = (mYCenter - mRingRadius);
			oval.right = mRingRadius * 2 + (mXCenter - mRingRadius);
			oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);
			canvas.drawArc(oval, -90, ((float)mProgress / mTotalProgress) * 360, false, mRingPaint); //
//			canvas.drawArc(oval, -90, 360, false, mRingPaint_bg); //
//			canvas.drawCircle(mXCenter, mYCenter, mRadius + mStrokeWidth / 2, mRingPaint);
			
			this.text = mProgress + "%";
			mTxtWidth = mTextPaint.measureText(this.text, 0, this.text.length());
			canvas.drawText(this.text, mXCenter - mTxtWidth / 2, mYCenter + mTxtHeight/ 4 - 20, mTextPaint);
			
		}
		*/
		
	}
	
	public void setProgress(int progress) {
//		if(progress > 100) progress = 100;
		mProgress = progress;
//		invalidate();
		postInvalidate();
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public CircleRing getCr() {
		return cr;
	}

	public void setCr(CircleRing cr) {
		this.cr = cr;
	}
}
