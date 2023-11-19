package com.release.perfectapplock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(!Util.isServiceRunning(context)) {
                    Intent in = new Intent(context, LockService.class);
                    in.setAction("ACTION_START");
                    context.startForegroundService(in);
                }
            } else {
                if(!Util.isServiceRunning(context)) {
                    Intent in = new Intent(context, LockService.class);
                    in.setAction("ACTION_START");
                    context.startService(in);
                }
            }
        }catch (Exception e){

        }
    }

}