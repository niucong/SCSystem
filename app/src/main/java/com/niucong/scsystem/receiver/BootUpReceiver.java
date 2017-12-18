package com.niucong.scsystem.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.niucong.scsystem.MainActivity;

/**
 * Created by think on 2017/9/21.
 */

public class BootUpReceiver extends BroadcastReceiver {

    public void onReceive(Context paramContext, Intent paramIntent) {
        paramIntent = new Intent(paramContext, MainActivity.class);
        paramIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        paramContext.startActivity(paramIntent);
    }
}
