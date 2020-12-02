package com.niucong.scsystem.app;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.facebook.stetho.Stetho;
import com.niucong.scsystem.andserver.util.FileUtils;
import com.niucong.scsystem.dao.DrugInfo;
import com.umeng.analytics.MobclickAgent;
import com.yanzhenjie.andserver.util.IOUtils;

import java.io.File;
import java.util.Formatter;
import java.util.List;

/**
 * Created by think on 2016/10/25.
 */

public class App extends Application {
    public static App app;

    public AppSharedPreferences share;

    public List<DrugInfo> list;
    public boolean refresh;

    private File mRootDir;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        share = new AppSharedPreferences(this);

        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this, "581bdb7fe88bad6afb00332c", ""));

        Stetho.initializeWithDefaults(this);

        initRootPath(this);
    }

    public File getRootDir() {
        return mRootDir;
    }

    private void initRootPath(Context context) {
        if (mRootDir != null) return;

        if (FileUtils.storageAvailable()) {
            mRootDir = Environment.getExternalStorageDirectory();
        } else {
            mRootDir = context.getFilesDir();
        }
        mRootDir = new File(mRootDir, "AndServer");
        IOUtils.createFolder(mRootDir);
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
