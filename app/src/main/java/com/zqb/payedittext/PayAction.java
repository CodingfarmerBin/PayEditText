package com.zqb.payedittext;

import android.support.annotation.ColorRes;

/**
 * Created by 张清斌 on 2018/11/11.
 */

public interface PayAction {

    /**
     * 设置位数
     */
    void setFigures(int figures);

    /**
     * 设置验证码之间的间距
     */
    void setVerCodeMargin(int margin);
    

    /**
     * 设置边线背景色
     */
    void setBackgroundColor(@ColorRes int selectedBackground);
    
    /**
     * 设置当验证码变化时候的监听器
     */
    void setOnPayChangedListener(OnPayChangedListener listener);

    /**
     * 支付密码变化时候的监听事件
     */
    interface OnPayChangedListener {

        /**
         * 当支付密码变化的时候
         */
        void onPayChanged(CharSequence s, int start, int before, int count);

        /**
         * 输入完毕后的回调
         */
        void onInputCompleted(CharSequence s);
    }
}
