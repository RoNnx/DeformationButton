package com.ronnx.deformationbutton;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 酷炫动画按钮
 * RoNnx
 */
public class DeformationButton extends View {

    public static final String SLIGHT = "slight";
    public static final String NORMAL = "normal";
    public static final String VIOLENT = "violent";

    private Paint mRectPaint, mPbPaint, mTextPaint, mOvalPaint, mYesPaint;
    private RectF mRectF, mRectFPb;
    private Canvas mCanvas;

    private float mWidth;
    private float mHeight;
    private float mLeftW;
    private int mStartAngle;
    private Rect mStartRect;
    private Rect mEndRect;

    private int mRepeatOrder, mRotationTimes;
    private boolean isDrawProgressBar, isFirstOnLayout, isCanDeformation, isFinishing, isCanFinish;
    private boolean finishSuccess, isRecovery, isShake;

    // 自定义属性
    private float mRadius, mPbPadding, mCenterOvalPadding, mYesStrokeWidth;
    private int mDeformationTime, mShakeTime, mMaxTime, mPbSpeed;
    private int mBgColor, mPbColor, mTextColor, mSuccessOvalColor, mSuccessYesColor;
    private String mShakeLevel;
    private String mText;
    private int mTextSize;

    private ValueAnimator mRotationVA, mRecoveryVA, mDeformationVA, mHidePbVA, mShakeVA;
    private AnimatorSet mSuccessAS;

    private OnStateChangeListener mOnStateChangeListener;
    private OnSuccessListener mOnSuccessListener;
    private OnFailureListener mOnFailureListener;

    public DeformationButton(Context context) {
        super(context);
        init();
    }

    public DeformationButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public DeformationButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init();
    }

    private void init() {
        Log.i("DeformationButton_TEST", "init()");

        mRectF = new RectF();
        initPaint();

    }

    /**
     * 初始化自定义属性集
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeformationButton);

        mRadius = dp2px(a.getFloat(R.styleable.DeformationButton_db_radius, 0f));
        mDeformationTime = a.getInt(R.styleable.DeformationButton_db_deformation_time, 1000);
        mShakeLevel = a.getString(R.styleable.DeformationButton_db_shake_level);
        mShakeTime = a.getInt(R.styleable.DeformationButton_db_shake_time, 500);
        mPbColor = a.getColor(R.styleable.DeformationButton_db_pb_color, Color.WHITE);
        mBgColor = a.getColor(R.styleable.DeformationButton_db_btn_color, ContextCompat.getColor(context, R.color.bg_default));
        mPbPadding = a.getFloat(R.styleable.DeformationButton_db_pb_padding, 5);
        mPbSpeed = a.getInt(R.styleable.DeformationButton_db_pb_speed, 1000);
        mMaxTime = a.getInt(R.styleable.DeformationButton_db_max_time, 999);
        mText = a.getString(R.styleable.DeformationButton_db_text);
        mTextColor = a.getColor(R.styleable.DeformationButton_db_text_color, Color.BLACK);
        mTextSize = (int) dp2px(a.getInt(R.styleable.DeformationButton_db_text_size, 14));
        mSuccessOvalColor = a.getColor(R.styleable.DeformationButton_db_success_oval_color, Color.WHITE);
        mSuccessYesColor = a.getColor(R.styleable.DeformationButton_db_success_yes_color, Color.WHITE);
        mYesStrokeWidth = dp2px(a.getFloat(R.styleable.DeformationButton_db_success_yes_stroke_width, 2f));
        mCenterOvalPadding = a.getFloat(R.styleable.DeformationButton_db_success_oval_padding, 5);
        a.recycle();
    }

    /**
     * 画笔初始化
     */
    private void initPaint() {

        // 创建画笔
        mRectPaint = new Paint();
        mRectPaint.setColor(mBgColor);
        mRectPaint.setStrokeWidth(5f);
        mRectPaint.setStyle(Paint.Style.FILL);
        mRectPaint.setAntiAlias(true);

        mPbPaint = new Paint();
        mPbPaint.setColor(mPbColor);
        mPbPaint.setStrokeWidth(5f);
        mPbPaint.setStyle(Paint.Style.STROKE);
        mPbPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setAntiAlias(true);

        mOvalPaint = new Paint();
        mOvalPaint.setColor(mSuccessOvalColor);
        mOvalPaint.setStrokeWidth(5f);
        mOvalPaint.setStyle(Paint.Style.FILL);
        mOvalPaint.setAntiAlias(true);

        mYesPaint = new Paint();
        mYesPaint.setColor(mSuccessYesColor);
        mYesPaint.setStrokeWidth(mYesStrokeWidth);
        mYesPaint.setStyle(Paint.Style.FILL);
        mYesPaint.setAntiAlias(true);
        mYesPaint.setAlpha(0);
        mYesPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 获取宽-测量规则的模式和大小
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        // 获取高-测量规则的模式和大小
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 设置wrap_content的默认宽 / 高值
        // 默认宽/高的设定并无固定依据,根据需要灵活设置
        // 类似TextView,ImageView等针对wrap_content均在onMeasure()对设置默认宽 / 高值有特殊处理,具体读者可以自行查看
        int mWidth = (int) dp2px(300);
        int mHeight = (int) dp2px(50);

        // 当布局参数设置为wrap_content时，设置默认值
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, mHeight);
            // 宽 / 高任意一个布局参数为= wrap_content时，都设置默认值
        } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, heightSize);
        } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, mHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 初始化一次
        if (!isFirstOnLayout) {
            initAtFirstOnLayout();
            initShakeVA();
            initHidePbVA();
            initDeformationVA();
            initRecoveryVA();
            initRotationVA();
        }
    }

    private void initAtFirstOnLayout() {
        mWidth = getWidth();
        mHeight = getHeight();
        mLeftW = 0;
        mStartAngle = 0;
        mRotationTimes = (mMaxTime * 1000)/mPbSpeed - 1;
        isCanDeformation = true;
        isCanFinish = false;
        isRecovery = true;
        mStartRect = new Rect(mRadius, mWidth, 255);
        mEndRect = new Rect(mHeight / 2f, mHeight, 0);
        if (TextUtils.isEmpty(mShakeLevel)) {
            mShakeLevel = "";
        }

        float left = mLeftW;
        float top = 0;
        float right = mWidth + left;
        float bottom = mHeight;

        mRectF.set(left, top, right, bottom);

        isFirstOnLayout = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;

        float left = mLeftW;
        float top = 0;
        float right = mWidth + left;
        float bottom = mHeight;

        mRectF.set(left, top, right, bottom);

        canvas.drawRoundRect(mRectF, mRadius, mRadius, mRectPaint);
        if (!TextUtils.isEmpty(mText)) {
            drawText();
        }

        if (isDrawProgressBar) {
            drawProgressBar();
        }

        if (finishSuccess) {
            drawCenterOval();
            drawCenterYes();
        }

    }

    /**
     * 绘制中心的文字
     */
    private void drawText() {
        float startX = getWidth() / 2f - mTextPaint.measureText(mText) / 2f;

        Paint.FontMetricsInt fm = mTextPaint.getFontMetricsInt();
        int startY = getHeight() / 2 - fm.descent + (fm.descent - fm.ascent) / 2;

        // 绘制文字
        mCanvas.drawText(mText, startX, startY, mTextPaint);
    }

    /**
     * 绘制 ProgressBar
     */
    private void drawProgressBar() {
        mRectFPb = new RectF(mRectF.left + dp2px(mPbPadding), mRectF.top + dp2px(mPbPadding),
                mRectF.right - dp2px(mPbPadding), mRectF.bottom - dp2px(mPbPadding));
        mCanvas.drawArc(mRectFPb, mStartAngle, 120, false, mPbPaint);
    }

    /**
     * 绘制 finishSuccess 后中心的圆
     */
    private void drawCenterOval() {
        RectF rectF = new RectF(mRectF.left + dp2px(mCenterOvalPadding), mRectF.top + dp2px(mCenterOvalPadding),
                mRectF.right - dp2px(mCenterOvalPadding), mRectF.bottom - dp2px(mCenterOvalPadding));
        mCanvas.drawOval(rectF, mOvalPaint);
    }

    /**
     * 绘制 finishSuccess 后中心的 √
     */
    private void drawCenterYes() {
        float sX = mRectF.left + mRectF.width()*1/3f - mRectF.width()/30f;
        float sY = mRectF.bottom*1/2f;
        float x1 = mRectF.left + mRectF.width()/2f - mRectF.width()/30f;
        float y1 = mRectF.bottom*2/3f;
        float eX = mRectF.left + mRectF.width()*2/3f + mRectF.width()/20f;
        float eY = mRectF.top + mRectF.bottom*1/3f + mRectF.width()/30f;
        mCanvas.drawLine(sX, sY, x1, y1, mYesPaint);
        mCanvas.drawLine(x1, y1, eX, eY, mYesPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:

            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                if (isCanDeformation) {
                    deformation(mDeformationTime);
                }
                return true;
        }
        //这句话不要修改
        return super.onTouchEvent(event);
    }

    private void initDeformationVA() {

        mDeformationVA = ValueAnimator.ofObject(new RectEvaluator(), mStartRect, mEndRect);
        mDeformationVA.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Rect r = (Rect) animation.getAnimatedValue();

                mRadius = r.getRadius();
                mWidth = r.getWidth();
                mLeftW = (getWidth() - r.getWidth())/2;
                mTextPaint.setAlpha((int) r.getAlpha());

                postInvalidate();//更新视图
            }
        });

        mDeformationVA.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mOnStateChangeListener != null) {
                    mOnStateChangeListener.onStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isDrawProgressBar = true;
                isCanDeformation = false;
                rotation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void initRecoveryVA() {
        mRecoveryVA = ValueAnimator.ofObject(new RectEvaluator(), mEndRect, mStartRect);
        mRecoveryVA.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Rect r = (Rect) animation.getAnimatedValue();

                mRadius = r.getRadius();
                mWidth = r.getWidth();
                mLeftW = (getWidth() - r.getWidth())/2;
                mTextPaint.setAlpha((int) r.getAlpha());

                postInvalidate();//更新视图
            }
        });

        mRecoveryVA.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isCanDeformation = true;
                if (isShake) {
                    shake();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        Log.e("DeformationButton_TEST", "initR");
    }

    private void initRotationVA() {
        mRotationVA = ValueAnimator.ofInt(mStartAngle, 360);
        mRotationVA.setInterpolator(new LinearInterpolator());
        mRotationVA.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mStartAngle == 360) {
                    mStartAngle = 0;
                }

                mStartAngle = (int) animation.getAnimatedValue();

                postInvalidate();//更新视图
            }
        });

        mRotationVA.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mOnStateChangeListener != null) {
                    mOnStateChangeListener.onProgress();
                }
                isCanFinish = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCanDeformation) {
                    if (isRecovery) {
                        recovery(mDeformationTime/2);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // 重复次数到达最大次数
                if (++mRepeatOrder >= mRotationTimes) {
                    hidePb();
                }
            }
        });
    }

    private void initHidePbVA() {
        mHidePbVA = ValueAnimator.ofInt(255, 0);
        mHidePbVA.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int alpha = (int) animation.getAnimatedValue();
                mPbPaint.setAlpha(alpha);
            }
        });
        mHidePbVA.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isCanFinish = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    private void initShakeVA() {
        int shakeNum = mShakeTime/25;
        int shakeAmplitude;

        switch (mShakeLevel) {
            case SLIGHT:
                shakeAmplitude = 10;
                break;
            case VIOLENT:
                shakeAmplitude = 40;
                break;
            case NORMAL:
            default:
                shakeAmplitude = 20;
                break;
        }

        Shake defaultShake = new Shake(0, 0);
        List<Shake> shakeList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < shakeNum; i++) {
            boolean isNegativeX = random.nextBoolean();
            boolean isNegativeY = random.nextBoolean();
            int x = random.nextInt(shakeAmplitude);
            int y = random.nextInt(shakeAmplitude);
            if (isNegativeX) {
                x = -x;
            }
            if (isNegativeY) {
                y = -y;
            }
            Shake shake = new Shake(x, y);
            shakeList.add(defaultShake);
            shakeList.add(shake);
        }
        shakeList.add(defaultShake);
        mShakeVA = ValueAnimator.ofObject(new ShakeEvaluator(), shakeList.toArray());
        mShakeVA.setInterpolator(new LinearInterpolator());
        mShakeVA.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Shake shake = (Shake) animation.getAnimatedValue();

                setTranslationX(shake.getTranslateX());
                setTranslationY(shake.getTranslateY());

                postInvalidate();
            }
        });

        mShakeVA.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mOnFailureListener != null) {
                    mOnFailureListener.onFailureAnimStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnFailureListener != null) {
                    mOnFailureListener.onFailed();
                }
                if (mOnStateChangeListener != null) {
                    mOnStateChangeListener.onFinished();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void initSuccessAnim() {
        ObjectAnimator centerOvalAnim = ObjectAnimator.ofFloat(this, "centerOvalPadding",
                mCenterOvalPadding, px2dp(mRectF.width()/2));
        centerOvalAnim.setDuration(300);
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(this, "scaleX",
                1f, 1.5f, 1f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(this, "scaleY",
                1f, 1.5f, 1f);
        ObjectAnimator yesAlphaAnim = ObjectAnimator.ofInt(this, "yesAlpha",
                0, 255);
        scaleXAnim.setDuration(500);
        scaleYAnim.setDuration(500);
        yesAlphaAnim.setDuration(500);

        AnimatorSet ovalScaleAS = new AnimatorSet();
        ovalScaleAS.playTogether(scaleXAnim, scaleYAnim, yesAlphaAnim);

        mSuccessAS = new AnimatorSet();
        mSuccessAS.playSequentially(centerOvalAnim, ovalScaleAS);

        mSuccessAS.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnSuccessListener != null) {
                    mOnSuccessListener.onSucceeded();
                }
                if (mOnStateChangeListener != null) {
                    mOnStateChangeListener.onFinished();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 变形
     */
    private void deformation(int millisecond) {
        if (mDeformationVA == null) {
            initDeformationVA();
        }

        if (mDeformationVA.isRunning()) {
            return;
        }

        mDeformationVA.setDuration(millisecond);
        mDeformationVA.start();

    }

    /**
     * 恢复形状
     * @param millisecond 时间
     */
    private void recovery(int millisecond) {
        if (mRecoveryVA == null) {
            initRecoveryVA();
        }

        if (mRecoveryVA.isRunning()) {
            return;
        }

        mRecoveryVA.setDuration(millisecond);
        mRecoveryVA.start();
    }

    /**
     * 旋转 ProgressBar
     */
    private void rotation() {
        mRepeatOrder = 0;
        mPbPaint.setAlpha(255);
        mStartAngle = 0;

        if (mRotationVA == null) {
            initRotationVA();
        }

        if (mRotationVA.isRunning()) {
            return;
        }

        mRotationVA.setDuration(mPbSpeed);
        mRotationVA.setRepeatCount(mRotationTimes);
        mRotationVA.start();
    }

    /**
     * 隐藏 ProgressBar
     */
    private void hidePb() {
        if (mHidePbVA == null) {
            initHidePbVA();
        }

        if (mHidePbVA.isRunning()) {
            return;
        }

        mHidePbVA.setDuration(mPbSpeed);
        mHidePbVA.start();
    }

    private void shake() {
        initShakeVA();

        mShakeVA.setDuration(mShakeTime);
        mShakeVA.start();
    }

    /**
     * 结束并恢复，只有在旋转时调用才有效果
     */
    public void finish() {
        if (!isCanFinish) {
            return;
        }
        isFinishing = true;
        isRecovery = true;
        isShake = false;
        hidePb();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mRotationVA.end();
                isFinishing = false;
                isCanFinish = false;
            }
        }, mPbSpeed);
    }

    /**
     * 带状态的结束，有在旋转时调用才有效果
     * @param isSuccess
     */
    public void finish(boolean isSuccess) {
        if (!isCanFinish) {
            return;
        }
        finish();
        if (isSuccess) {
            isDrawProgressBar = false;
            finishSuccess = true;
            isRecovery = false;
            isShake = false;
            success();
        } else {
            finishSuccess = false;
            isRecovery = true;
            isShake = true;
        }
    }

    /**
     * 播放成功动画
     */
    private void success() {
        if (mSuccessAS == null) {
            initSuccessAnim();
        }

        mSuccessAS.start();
    }

    /**
     * 设置文字
     */
    public void setText(String s) {
        mText = s;
        postInvalidate();
    }

    @Override
    public void setScaleX(float scaleX) {
        super.setScaleX(scaleX);
        postInvalidate();
    }

    @Override
    public void setScaleY(float scaleY) {
        super.setScaleY(scaleY);
        postInvalidate();
    }

    public void setYesAlpha(int alpha) {
        mYesPaint.setAlpha(alpha);
        postInvalidate();
    }

    public int getYesAlpha() {
        return mYesPaint.getAlpha();
    }

    public boolean isFinishing() {
        return isFinishing;
    }

    /**
     * 设置变形时间
     */
    public void setDeformationTime(int millisecond) {
        mDeformationTime = millisecond;
    }

    public int getDeformationTime() {
        return mDeformationTime;
    }

    public float getCenterOvalPadding() {
        return mCenterOvalPadding;
    }

    public void setCenterOvalPadding(float centerOvalPadding) {
        this.mCenterOvalPadding = centerOvalPadding;
        postInvalidate();
    }

    public float getRadius() {
        return px2dp(mRadius);
    }

    public void setRadius(float radius) {
        this.mRadius = dp2px(radius);
        postInvalidate();
    }

    public float getPbPadding() {
        return mPbPadding;
    }

    public void setPbPadding(float pbPadding) {
        this.mPbPadding = pbPadding;
        postInvalidate();
    }

    public float getYesStrokeWidth() {
        return px2dp(mYesPaint.getStrokeWidth());
    }

    public void setYesStrokeWidth(float yesStrokeWidth) {
        this.mYesStrokeWidth = yesStrokeWidth;
        mYesPaint.setStrokeWidth(dp2px(yesStrokeWidth));
    }

    public int getShakeTime() {
        return mShakeTime;
    }

    public void setShakeTime(int millisecond) {
        this.mShakeTime = millisecond;
    }

    public int getMaxTime() {
        return mMaxTime;
    }

    public void setMaxTime(int millisecond) {
        this.mMaxTime = millisecond;
    }

    public int getPbSpeed() {
        return mPbSpeed;
    }

    public void setPbSpeed(int pbSpeed) {
        this.mPbSpeed = pbSpeed;
    }

    public int getBgColor() {
        return mBgColor;
    }

    public void setBgColor(@ColorInt int color) {
        mRectPaint.setColor(color);
        mBgColor = color;
        postInvalidate();
    }

    public int getPbColor() {
        return mPbColor;
    }

    public void setPbColor(@ColorInt int color) {
        mPbPaint.setColor(color);
        this.mPbColor = color;
        postInvalidate();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(@ColorInt int color) {
        this.mTextColor = color;
        mTextPaint.setColor(color);
        postInvalidate();
    }

    public int getSuccessOvalColor() {
        return mSuccessOvalColor;
    }

    public void setSuccessOvalColor(@ColorInt int color) {
        this.mSuccessOvalColor = color;
        mOvalPaint.setColor(color);
        postInvalidate();
    }

    public int getSuccessYesColor() {
        return mSuccessYesColor;
    }

    public void setSuccessYesColor(@ColorInt int color) {
        this.mSuccessYesColor = color;
        mYesPaint.setColor(color);
        postInvalidate();
    }

    public String getShakeLevel() {
        return mShakeLevel;
    }

    public void setShakeLevel(String mShakeLevel) {
        this.mShakeLevel = mShakeLevel;
    }

    public String getText() {
        return mText;
    }

    public int getTextSize() {
        return px2dp(mTextSize);
    }

    public void setTextSize(int dp) {
        this.mTextSize = (int) dp2px(dp);
        mTextPaint.setTextSize(dp2px(dp));
        postInvalidate();
    }

    private float dp2px(final float dpValue) {
        final float scale = this.getContext().getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }

    public static int px2dp(final float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        mOnStateChangeListener = onStateChangeListener;
    }

    public interface OnStateChangeListener {
        void onStart();
        void onProgress();
        void onFinished();
    }

    public void setOnSuccessListener(OnSuccessListener onSuccessListener) {
        mOnSuccessListener = onSuccessListener;
    }

    public interface OnSuccessListener {
        void onSucceeded();
    }

    public void setOnFailureListener(OnFailureListener onFailureListener) {
        mOnFailureListener = onFailureListener;
    }

    public interface OnFailureListener {
        void onFailed();
        void onFailureAnimStart();
    }
}
