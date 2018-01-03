package com.niucong.scsystem.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import static com.niucong.scsystem.app.App.app;

/**
 * Created by think on 2018/1/2.
 */

public class USBReceiver extends BroadcastReceiver {

    private static final String TAG = USBReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            String mountPath = intent.getData().getPath();
            Log.d(TAG, "mountPath = " + mountPath);
            if (!TextUtils.isEmpty(mountPath)) {
                //读取到U盘路径再做其他业务逻辑
                app.share.saveStringMessage("SC", "USBPath", mountPath);
            }
        } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED) || action.equals(Intent.ACTION_MEDIA_EJECT)) {
            Toast.makeText(context, "No services information detected !", Toast.LENGTH_SHORT).show();
        } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            //如果是开机完成，则需要调用另外的方法获取U盘的路径
        }
    }

}
