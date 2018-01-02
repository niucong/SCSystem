package com.niucong.scsystem.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.niucong.scsystem.R;
import com.niucong.scsystem.app.App;

/**
 * @Title: NiftyDialogBuilder
 * @Package: com.rongchuang.logistics.view
 * @Description: 自定义dialog
 * @author HeDongDong
 * @Date: 2015-11-24 下午3:55:06
 * @Version: 1.0
 */
public class NiftyDialogBuilder extends Dialog implements DialogInterface {

    private final String defTextColor = "#FFFFFFFF";

    private final String defDividerColor = "#11000000";

    private final String defMsgColor = "#FFFFFFFF";

    private final String defDialogColor = "#FFE74C3C";

    private static Context tmpContext;

    private LinearLayout mLinearLayoutTopView;

    private FrameLayout mFrameLayoutCustomView;

    private View mDialogView;

    private View mDivider;

    private TextView mTitle;

    public TextView mMessage;

    private ImageView mIcon;

    private Button mButton1;

    private Button mButton2;

    private Button mButton3;

    private int mDuration = 500;

    private boolean isCancelable = true;

    private int type = 1;

    private static NiftyDialogBuilder instance;

    public NiftyDialogBuilder(Context context) {
        super(context);
        init(context);

    }

    public NiftyDialogBuilder(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    public static NiftyDialogBuilder getInstance(Context context) {

        if (instance == null || !tmpContext.equals(context)) {
            synchronized (NiftyDialogBuilder.class) {
                if (instance == null || !tmpContext.equals(context)) {
                    instance = new NiftyDialogBuilder(context, R.style.dialog_untran);
                }
            }
        }
        tmpContext = context;
        return instance;

    }

    private void init(Context context) {

        mDialogView = View.inflate(context, R.layout.dialog_layout, null);
        mLinearLayoutTopView = (LinearLayout) mDialogView.findViewById(R.id.topPanel);
        mFrameLayoutCustomView = (FrameLayout) mDialogView.findViewById(R.id.customPanel);

        mTitle = (TextView) mDialogView.findViewById(R.id.alertTitle);
        mMessage = (TextView) mDialogView.findViewById(R.id.message);
        mIcon = (ImageView) mDialogView.findViewById(R.id.icon);
        mDivider = mDialogView.findViewById(R.id.titleDivider);
        mButton1 = (Button) mDialogView.findViewById(R.id.button1);
        mButton2 = (Button) mDialogView.findViewById(R.id.button2);
        mButton3 = (Button) mDialogView.findViewById(R.id.button3);

        mButton1.setVisibility(View.GONE);
        mButton2.setVisibility(View.GONE);
        mButton3.setVisibility(View.GONE);
        setContentView(mDialogView);
    }

    /**
     * 默认颜色设置
     */
    public void toDefault() {
        mTitle.setTextColor(Color.parseColor(defTextColor));
        mDivider.setBackgroundColor(Color.parseColor(defDividerColor));
        mMessage.setTextColor(Color.parseColor(defMsgColor));
    }

    /**
     * 设置分割线颜色
     *
     * @param colorString
     * @return
     */
    public NiftyDialogBuilder withDividerColor(String colorString) {
        mDivider.setBackgroundColor(Color.parseColor(colorString));
        return this;
    }

    /**
     * 设置标题文字 int类型
     *
     * @param color
     * @return
     */
    public NiftyDialogBuilder withDividerColor(int color) {
        mDivider.setBackgroundColor(color);
        return this;
    }

    /**
     * 设置标题文字 CharSequence类型
     *
     * @param title
     * @return
     */
    public NiftyDialogBuilder withTitle(CharSequence title) {
        toggleView(mLinearLayoutTopView, title);
        mTitle.setText(title);
        return this;
    }

    /**
     * 设置标题字体颜色 String类型
     *
     * @param colorString
     * @return
     */
    public NiftyDialogBuilder withTitleColor(String colorString) {
        mTitle.setTextColor(Color.parseColor(colorString));
        return this;
    }

    /**
     * 设置标题字体颜色 int类型
     *
     * @param color
     * @return
     */
    public NiftyDialogBuilder withTitleColor(int color) {
        mTitle.setTextColor(color);
        return this;
    }

    /**
     * 设置消息内容 int类型
     *
     * @param textResId
     * @return
     */
    public NiftyDialogBuilder withMessage(int textResId) {
        toggleView(mMessage, textResId);
        mMessage.setText(textResId);
        return this;
    }

    /**
     * 设置消息内容 CharSequence类型
     *
     * @param msg
     * @return
     */
    public NiftyDialogBuilder withMessage(CharSequence msg) {
        toggleView(mMessage, msg);
        mMessage.setText(msg);
        return this;
    }

    /**
     * 设置消息字体颜色 String类型
     *
     * @param colorString
     * @return
     */
    public NiftyDialogBuilder withMessageColor(String colorString) {
        mMessage.setTextColor(Color.parseColor(colorString));
        return this;
    }

    /**
     * 设置消息字体颜色 int类型
     *
     * @param color
     * @return
     */
    public NiftyDialogBuilder withMessageColor(int color) {
        mMessage.setTextColor(color);
        return this;
    }

    /**
     * 设置标题提示图标
     *
     * @param drawableResId
     * @return
     */
    public NiftyDialogBuilder withIcon(int drawableResId) {
        mIcon.setImageResource(drawableResId);
        return this;
    }

    /**
     * 设置标题提示图标
     *
     * @param icon
     * @return
     */
    public NiftyDialogBuilder withIcon(Drawable icon) {
        mIcon.setImageDrawable(icon);
        return this;
    }

    /**
     * 设置动画时间
     *
     * @param duration
     * @return
     */
    public NiftyDialogBuilder withDuration(int duration) {
        this.mDuration = duration;
        return this;
    }

    /**
     * 设置动画样式
     *
     * @param type
     * @return
     */
    public NiftyDialogBuilder withType(int type) {
        this.type = type;
        return this;
    }

    /**
     * 设置按钮的背景Background
     *
     * @param resid
     * @return
     */
    public NiftyDialogBuilder withButtonDrawable(int resid) {
        mButton1.setBackgroundResource(resid);
        mButton2.setBackgroundResource(resid);
        return this;
    }

    /**
     * 设置第一个按钮文字
     * @param text
     * @param textColorId 字体颜色
     * @return
     */
    public NiftyDialogBuilder withButton1Text(CharSequence text,int textColorId) {
        mButton1.setVisibility(View.VISIBLE);
        mButton1.setText(text);
        if (textColorId != 0) {
            mButton1.setTextColor(App.app.getResources().getColor(textColorId));
        }
        return this;
    }

    /**
     * 设置第二个按钮文字
     * @param text
     * @param textColorId 字体颜色
     * @return
     */
    public NiftyDialogBuilder withButton2Text(CharSequence text,int textColorId) {
        mButton2.setVisibility(View.VISIBLE);
        mButton2.setText(text);
        if (textColorId != 0) {
            mButton2.setTextColor(App.app.getResources().getColor(textColorId));
        }
        return this;
    }

    /**
     * 设置第三个按钮文字
     * @param text
     * @param textColorId 字体颜色
     * @return
     */
    public NiftyDialogBuilder withButton3Text(CharSequence text,int textColorId) {
        mButton3.setVisibility(View.VISIBLE);
        mButton3.setText(text);
        if (textColorId != 0) {
            mButton3.setTextColor(App.app.getResources().getColor(textColorId));
        }
        return this;
    }

    /**
     * 设置第一个按钮点击事件
     *
     * @param click
     * @return
     */
    public NiftyDialogBuilder setButton1Click(View.OnClickListener click) {
        mButton1.setOnClickListener(click);
        return this;
    }

    /**
     * 设置第二个按钮点击事件
     *
     * @param click
     * @return
     */
    public NiftyDialogBuilder setButton2Click(View.OnClickListener click) {
        mButton2.setOnClickListener(click);
        return this;
    }

    /**
     * 设置第三个按钮点击事件
     *
     * @param click
     * @return
     */
    public NiftyDialogBuilder setButton3Click(View.OnClickListener click) {
        mButton3.setOnClickListener(click);
        return this;
    }

    /**
     * 设置自定义view
     *
     * @param resId
     * @param context
     * @return
     */
    public NiftyDialogBuilder setCustomView(int resId, Context context) {
        View customView = View.inflate(context, resId, null);
        if (mFrameLayoutCustomView.getChildCount() > 0) {
            mFrameLayoutCustomView.removeAllViews();
        }
        mFrameLayoutCustomView.addView(customView);
        return this;
    }

    /**
     * 设置自定义view
     *
     * @param view
     * @param context
     * @return
     */
    public NiftyDialogBuilder setCustomView(View view, Context context) {
        if (view != null) {
            if (mFrameLayoutCustomView.getChildCount() > 0) {
                mFrameLayoutCustomView.removeAllViews();
            }
            mFrameLayoutCustomView.addView(view);
        }else {
            mFrameLayoutCustomView.removeAllViews();
        }
        return this;
    }

    /**
     * 设置dialog 关闭方式
     *
     * @param cancelable
     *            true:点击dialog外面可以取消 false:只能物理返回键取消
     * @return
     */
    public NiftyDialogBuilder isCancelableOnTouchOutside(boolean cancelable) {
        this.isCancelable = cancelable;
        this.setCanceledOnTouchOutside(cancelable);
        return this;
    }

    /**
     * 设置dialog 关闭方式
     *
     * @param cancelable
     *            true:可以取消 false:只能用按钮取消
     * @return
     */
    public NiftyDialogBuilder isCancelable(boolean cancelable) {
        this.isCancelable = cancelable;
        this.setCancelable(cancelable);
        return this;
    }

    /**
     * 切换view
     *
     * @param view
     * @param obj
     */
    private void toggleView(View view, Object obj) {
        if (obj == null) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

}
