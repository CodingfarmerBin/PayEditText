package com.zqb.payedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by 张清斌 on 2018/11/11.
 * 
 * 支付EditText 
 * 
 */

public class PayEditText extends android.support.v7.widget.AppCompatEditText implements
    PayAction, TextWatcher {

    private int mFigures;//需要输入的位数
    private int mPayMargin;//数字之间的间距
    private int mPayDegree;//边框角度
    private int mBackgroundColor;//背景颜色
    private OnPayChangedListener onPayChangedListener;
    private int mEachRectLength = 0;//每个矩形的边长
    private Paint mBackgroundPaint;
    private boolean isPassWord;
    private Paint mSelectPwdPaint;

    public PayEditText(Context context) {
        this(context, null);
    }

    public PayEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PayEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));//防止出现下划线
        initPaint();
        setFocusableInTouchMode(true);
        super.addTextChangedListener(this);
    }

    /**
     * 初始化paint
     */
    private void initPaint() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(2);

        mSelectPwdPaint = new Paint();
        mSelectPwdPaint.setColor(Color.BLACK);
        mSelectPwdPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 初始化Attrs
     */
    private void initAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.PayEditText);
        mFigures = ta.getInteger(R.styleable.PayEditText_payFigures, 4);
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            if(attrs.getAttributeName(i).equals("inputType") && attrs.getAttributeValue(i).equals("0x12")){
                isPassWord=true;
            }
        }
        mPayMargin = (int) ta.getDimension(R.styleable.PayEditText_payMargin, 0);
        mPayDegree = (int) ta.getDimension(R.styleable.PayEditText_payDegree, 0);
        mBackgroundColor = ta.getColor(R.styleable.PayEditText_payBackgroundColor,
                getCurrentTextColor());
        ta.recycle();
    }


    @Override
    final public void setCursorVisible(boolean visible) {
        super.setCursorVisible(false);//隐藏光标的显示
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthResult = 0, heightResult = 0;
        //最终的宽度
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            widthResult = widthSize;
        } else {
            widthResult = getScreenWidth(getContext());
        }
        //每个矩形形的宽度
        mEachRectLength = (widthResult - (mPayMargin * (mFigures - 1))) / mFigures -1;
        //最终的高度
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            heightResult = heightSize;
        } else {
            heightResult = mEachRectLength;
        }
        setMeasuredDimension(widthResult, heightResult);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            setSelection(getText().length());
            showKeyBoard(getContext());
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        int width = mEachRectLength - getPaddingLeft() - getPaddingRight();
        final int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        for (int i = 0; i < mFigures; i++) {
            canvas.save();
            int start = width * i + i * mPayMargin;
            int end = width + start;
            //画一个矩形
            canvas.drawRoundRect(new RectF(start, 0, end, height), mPayDegree, mPayDegree, mBackgroundPaint);
            canvas.restore();
        }
        //绘制文字
        String value = getText().toString();
        for (int i = 0; i < value.length(); i++) {
            canvas.save();
            int start = width * i + i * mPayMargin;
            final float x = start + width / 2;
            if (isPassWord) {
                canvas.drawCircle(x,height/2,13,mSelectPwdPaint);
            }else{
                TextPaint paint = getPaint();
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(getCurrentTextColor());
                Paint.FontMetrics fontMetrics = paint.getFontMetrics();
                float baseline = (height - fontMetrics.bottom + fontMetrics.top) / 2
                        - fontMetrics.top;
                canvas.drawText(String.valueOf(value.charAt(i)), x, baseline, paint);
            }
            canvas.restore();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        postInvalidate();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        postInvalidate();
        if (onPayChangedListener != null) {
            onPayChangedListener.onPayChanged(getText(), start, before, count);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        postInvalidate();
        if (getText().length() == mFigures) {
            if (onPayChangedListener != null) {
                onPayChangedListener.onInputCompleted(getText());
            }
        } else if (getText().length() > mFigures) {
            getText().delete(mFigures, getText().length());
        }
    }

    @Override
    public void setFigures(int figures) {
        mFigures = figures;
        postInvalidate();
    }

    @Override
    public void setVerCodeMargin(int margin) {
        mPayMargin = margin;
        postInvalidate();
    }

    @Override
    public void setOnPayChangedListener(OnPayChangedListener listener) {
        this.onPayChangedListener = listener;
    }

    /**
     * 返回颜色
     */
    private int getColor(@ColorRes int color) {
        return ContextCompat.getColor(getContext(), color);
    }

    /**
     * dp转px
     */
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     * 获取手机屏幕的宽度
     */
    static int getScreenWidth(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public void showKeyBoard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this, InputMethodManager.SHOW_FORCED);
    }
}
