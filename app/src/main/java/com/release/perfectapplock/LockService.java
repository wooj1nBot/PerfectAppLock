package com.release.perfectapplock;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.RuntimeExecutionException;
import com.google.android.play.core.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class LockService extends Service{

    private Timer appTimer;
    public static boolean isStop = false;
    public static boolean isAdClick = false;

    public static boolean isPlayStore = false;

    public static AdManger adManger;

    public boolean isUpdate = false;
    public String lockName = "";

    public LockService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public void appCheckTask(){
        TimerTask purposeTask = new TimerTask() {
            @Override
            public void run() {
                String name = getPackageName(LockService.this);
                Log.d("locklog", name + "," + isPlayStore + "," + isStop + "," + Util.getLockedList(LockService.this));
                if(isAdClick) return;
                if(isPlayStore) return;
                if (isStop) return;

                if(isLock(name)){
                    if(!Util.isLockedList(name, LockService.this)) {
                        lockName = name;
                        lock(name);
                    }
                }

                if (!lockName.equals(name) && !name.equals(getPackageName())){
                    Log.d("locklog", "off:" + name + " , " + lockName);
                    if(Util.getLockMode(LockService.this) == Util.LOCK_MODE_APP_OFF) {
                        Util.removeAllLockedList(LockService.this);
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (Objects.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)){
                    isUpdate = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isUpdate) return;
                            String pack = intent.getDataString().replace("package:", "");
                            if (Settings.canDrawOverlays(LockService.this)){
                                showDialog(LockService.this, pack, Util.getAppLabel(LockService.this, pack));
                            }
                        }
                    }, 2000);
                }

                if (Objects.equals(intent.getAction(), Intent.ACTION_PACKAGE_REPLACED)){
                    isUpdate = true;
                }
            }
        };
        registerReceiver(receiver, intentFilter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new PhoneUnlockedReceiver(), filter);

        appTimer = new Timer();
        int DEFAULT_CHECK_TIME = 200;
        appTimer.schedule(purposeTask, 0, DEFAULT_CHECK_TIME);
        adManger.startLoadAd();
    }



    public class PhoneUnlockedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
                Util.removeAllLockedList(LockService.this);
                LockService.isStop = false;

                if(!Util.isServiceRunning(LockService.this)) {
                    Intent in = new Intent(context, LockService.class);
                    in.setAction("ACTION_START");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(in);
                    }else{
                        startService(in);
                    }
                }
            }
        }
    }

    public void showDialog(Context context, String packageName, String appName){

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.install_lock_dialog);
        dialog.setCancelable(false);

        TextView cancel = dialog.findViewById(R.id.cancel);
        TextView ok = dialog.findViewById(R.id.ok);



        ImageView iv_logo = dialog.findViewById(R.id.iv_logo);
        TextView tv_name = dialog.findViewById(R.id.tv_text);
        TextView tv_sub = dialog.findViewById(R.id.tv_text2);
        ImageView imageView = dialog.findViewById(R.id.imageView5);

        imageView.setImageResource(R.drawable.logo);

        tv_name.setText(appName);
        String text = context.getString(R.string.install_text).replace("_value_", appName);
        tv_sub.setText(text);
        iv_logo.setImageDrawable(Util.getAppIcon(LockService.this, packageName));

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.addLock(packageName, context);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
        });
        int LAYOUT_FLAG;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        dialog.getWindow().setType(LAYOUT_FLAG);

        dialog.show();




        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        ok.setTag(dialog);
        cancel.setTag(dialog);
    }

    public void lock(String name){
        Intent intent = new Intent(LockService.this, LockActivity.class);
        if(!name.equals(getPackageName())) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        isStop = true;
        intent.putExtra("time", System.currentTimeMillis());
        intent.putExtra("package", name);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }




    public boolean isLock(String packageName){
        SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
        String s = preferences.getString("locklist", "");
        if(!s.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray lockList = jsonObject.getJSONArray("list");
                for (int i = 0; i < lockList.length(); i++) {
                    if(lockList.getString(i).equals(packageName)) return true;
                }
            } catch (JSONException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isStop = false;
        showForegroundNotification();
        adManger = new AdManger(LockService.this);
        appCheckTask();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setAlarmTimer();
        Thread.currentThread().interrupt();
        if(appTimer != null){
            appTimer.cancel();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(LockService.this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }



    public static String getPackageName(@NonNull Context context) {

        // UsageStatsManager 선언
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

        // 마지막 실행 앱 타임스탬프
        long lastRunAppTimeStamp = 0L;

        // 얼마만큼의 시간동안 수집한 앱의 이름을 가져오는지 정하기 (begin ~ end 까지의 앱 이름을 수집한다)
        final long INTERVAL = 1000 * 60 * 5;
        final long end = System.currentTimeMillis();
        final long begin = end - INTERVAL; // 5분전


        LongSparseArray<Object> packageNameMap = new LongSparseArray<>();

        // 수집한 이벤트들을 담기 위한 UsageEvents
        final UsageEvents usageEvents = usageStatsManager.queryEvents(begin, end);

        // 이벤트가 여러개 있을 경우 (최소 존재는 해야 hasNextEvent가 null이 아니니까)
        while (usageEvents.hasNextEvent()) {

            // 현재 이벤트를 가져오기
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);

            // 현재 이벤트가 포그라운드 상태라면(현재 화면에 보이는 앱이라면)
            if(isForeGroundEvent(event)) {

                // 해당 앱 이름을 packageNameMap에 넣는다.
                packageNameMap.put(event.getTimeStamp(), event.getPackageName());

                // 가장 최근에 실행 된 이벤트에 대한 타임스탬프를 업데이트 해준다.
                if(event.getTimeStamp() > lastRunAppTimeStamp) {
                    lastRunAppTimeStamp = event.getTimeStamp();
                }
            }
        }
        // 가장 마지막까지 있는 앱의 이름을 리턴해준다.
        return packageNameMap.get(lastRunAppTimeStamp, "").toString();
    }

    // 앱이 포그라운드 상태인지 체크
    private static boolean isForeGroundEvent(UsageEvents.Event event) {

        // 이벤트가 없으면 false 반환
        if(event == null)
            return false;

        // 이벤트가 포그라운드 상태라면 true 반환
        if(BuildConfig.VERSION_CODE >= 29)
            return event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED;

        return event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND;
    }






    private void showForegroundNotification(){
        String channelId = "applock_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setChannelId(channelId);
        builder.setSmallIcon(R.drawable.logo);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setAutoCancel(false);
        builder.setWhen(0);
        builder.setOnlyAlertOnce(true);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Applock Service", NotificationManager.IMPORTANCE_MIN);
                notificationChannel.setDescription("This channel is used by applock service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        startForeground(8829, builder.build());
    }

    public static class MyAdListener extends AdListener{

        MainActivity mainActivity;

        MyAdListener(){}

        MyAdListener(MainActivity mainActivity){
            this.mainActivity = mainActivity;
        }

        @Override
        public void onAdClicked() {
            super.onAdClicked();
            isAdClick = true;
            if(mainActivity != null){
                mainActivity.isActivity = true;
            }
        }

        @Override
        public void onAdClosed() {
            super.onAdClosed();
            isAdClick = false;
        }
    }



}