package com.niucong.scsystem.app;

import android.app.Application;

import java.util.Formatter;

/**
 * Created by think on 2016/10/25.
 */

public class App extends Application {
    public static App app;

    public AppSharedPreferences share;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        share = new AppSharedPreferences(this);

//        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this,"581bdb7fe88bad6afb00332c",""));
    }

    /**
     * 保存价格-String转int
     *
     * @param price
     * @return
     */
    public int savePrice(String price) {
        try {
            return (int) (Float.valueOf(price) * 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 显示价格-int转String
     *
     * @param price
     * @return
     */
    public String showPrice(int price) {
        float pf = price;
        return new Formatter().format("%.2f", pf / 100).toString();
    }
}
