package com.release.perfectapplock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rc;
    private final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 991;
    private final int ACTION_MANAGE_ACCESSIBILITY_REQUEST_CODE = 998;
    private boolean ist = false;
    private boolean isRequest = false;
    private Button btn_lock;
    private AppListAdopter appListAdopter;

    public boolean isActivity = false;

    DrawerLayout drawerLayout;

    private Handler handler;

    private final int REQUEST_CODE_AD = 9876;

    FirebaseAnalytics mFirebaseAnalytics;

    public boolean isPermission = false;
    public boolean isFirst = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Main");
        mFirebaseAnalytics.logEvent("Main", bundle);

        if(Util.isBegin(MainActivity.this)){
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }

        Intent main = getIntent();
        if(Util.getIsLock(this)){
            if(!main.getBooleanExtra("lock", false)) {
                Intent intent = new Intent(this, LockActivity.class);
                intent.putExtra("main", true);
                intent.putExtra("package", getPackageName());
                startActivity(intent);
                finish();
            }
        }

        isFirst = !Util.getIsLock(MainActivity.this);
        Log.d("first", String.valueOf(isFirst));

        setContentView(R.layout.activity_main);
        rc = findViewById(R.id.rc);
        btn_lock = findViewById(R.id.lock);
        drawerLayout = findViewById(R.id.dl_main_drawer_root);
        startService();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });

        handler = new Handler();


        LinearLayout camera = findViewById(R.id.camera);
        LinearLayout icon = findViewById(R.id.icon);
        LinearLayout ads = findViewById(R.id.ad_off);
        LinearLayout info = findViewById(R.id.info);
        LinearLayout review = findViewById(R.id.review);

        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActivity = true;
                String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Review");
                mFirebaseAnalytics.logEvent("Review", bundle);
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActivity = true;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy)));
                startActivity(browserIntent);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Tos");
                mFirebaseAnalytics.logEvent("Tos", bundle);
            }
        });


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActivity = true;
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Spycamera");
                mFirebaseAnalytics.logEvent("Spycamera", bundle);
            }
        });

        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActivity = true;
                Intent intent = new Intent(MainActivity.this, IconActivity.class);
                startActivity(intent);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "CamoIcon");
                mFirebaseAnalytics.logEvent("CamoIcon", bundle);
            }
        });

        ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActivity = true;
                Intent intent = new Intent(MainActivity.this, AdActivity.class);
                startActivityForResult(intent, REQUEST_CODE_AD);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "NoAds");
                mFirebaseAnalytics.logEvent("NoAds", bundle);
            }
        });

        ImageView more = findViewById(R.id.more);
        ImageView setting = findViewById(R.id.setting);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActivity = true;
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drawerLayout.isDrawerOpen(Gravity.LEFT)){
                    drawerLayout.closeDrawer(Gravity.LEFT);
                }else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });

        rc.setLayoutManager(new LinearLayoutManager(this));

        btn_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lock_click();
            }
        });

        getPackageList();



        Util.removeAlarm(this);
        Util.setAlarm(this);

        if (!isFirst){
            btn_lock.setVisibility(View.GONE);
        }else{
            btn_lock.setVisibility(View.VISIBLE);
        }
    }


    public void lock_click(){
        if(isSetPattern()){
            if(appListAdopter != null) {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Lock_Confirm");
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainScreen");
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
                    mFirebaseAnalytics.logEvent("Lock_Confirm", bundle);

                    setLock(appListAdopter.getLockList(), false);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }else {
            LoadingView loadingView = new LoadingView(MainActivity.this);
            loadingView.show("Loading...");

            FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600).build();
            config.setConfigSettingsAsync(settings);
            config.fetchAndActivate()
                    .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            loadingView.stop();

                            if (task.isSuccessful()) {
                                String a = config.getString("FingerPrint");
                                if (a.equals("First")) {
                                    showFingerDialog();
                                } else {
                                    SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();

                                    isActivity = true;
                                    editor.putBoolean("finger", false);
                                    editor.apply();
                                    Intent intent = new Intent(MainActivity.this, PatternActivity.class);
                                    startActivity(intent);
                                    isRequest = true;
                                }
                                Log.d("fetch", a);
                            } else {
                                SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();

                                isActivity = true;
                                editor.putBoolean("finger", false);
                                editor.apply();
                                Intent intent = new Intent(MainActivity.this, PatternActivity.class);
                                startActivity(intent);
                                isRequest = true;
                            }
                        }
                    });
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        isActivity = false;
        if(isRequest){
            makeDialog();
            isRequest = false;
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isActivity = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isActivity){
            finish();
        }
    }

    public void getPackageList() {
        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0); // 실행가능한 Package만 추출.
        mApps.sort(new ResolveInfo.DisplayNameComparator(pkgMgr));

        int count = 0;

        List<App> apps = new ArrayList<>();


        for(Iterator<ResolveInfo> iterator = mApps.listIterator(); iterator.hasNext();){
            ResolveInfo info = iterator.next();
            boolean islock = Util.isLock(this, info.activityInfo.packageName);
            if(islock){
                count++;
                if(!info.activityInfo.packageName.equals(getPackageName())){
                    iterator.remove();
                    apps.add(new App(info.activityInfo.packageName, info.activityInfo.loadLabel(pkgMgr).toString(), info.activityInfo.loadIcon(pkgMgr), islock,false));
                }
            }
        }

        List<String> def_app = Arrays.asList(AppConstants.DEF_LOCK_APP);


        for(Iterator<ResolveInfo> iterator = mApps.listIterator(); iterator.hasNext();){
            ResolveInfo info = iterator.next();
            boolean islock = Util.isLock(this, info.activityInfo.packageName);
            if(def_app.contains(info.activityInfo.packageName)){
                apps.add(new App(info.activityInfo.packageName, info.activityInfo.loadLabel(pkgMgr).toString(), info.activityInfo.loadIcon(pkgMgr), islock, true));
                iterator.remove();
            }
        }

        for(ResolveInfo info : mApps){
            boolean islock = Util.isLock(this, info.activityInfo.packageName);
            if(!islock){
                if(!info.activityInfo.packageName.equals(getPackageName())){
                    apps.add(new App(info.activityInfo.packageName, info.activityInfo.loadLabel(pkgMgr).toString(), info.activityInfo.loadIcon(pkgMgr), islock,false));
                }
            }
        }

        btn_lock.setText(String.format(getString(R.string.locked) + "(%d)", count));
        int finalCount = count;


        appListAdopter = new AppListAdopter(apps, finalCount, false);
        rc.setAdapter(appListAdopter);

        new BillingEntireManager(MainActivity.this, new CheckPurchaseCallback() {
            @Override
            void onPurchaseComplete(List<Purchase> purchases, BillingResult billingResult) {
                 boolean isPurchase = purchases.size() > 0;
                 handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(isPurchase) {
                            appListAdopter = new AppListAdopter(apps, finalCount, true);
                            rc.setAdapter(appListAdopter);
                        }
                    }
                });
            }
        });



    }




    public class App{
        String name;
        Drawable icon;
        String pack;
        boolean isLock;
        boolean isDefault;

        public App(String pack, String name, Drawable icon, boolean isLock, boolean isDefault){
            this.pack = pack;
            this.name = name;
            this.icon = icon;
            this.isLock = isLock;
            this.isDefault = isDefault;
        }
    }

    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isSetPattern(){
        SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
        return preferences.contains("pattern") || preferences.contains("password");
    }

    public boolean checkDrawPermission() {
        return Settings.canDrawOverlays(this);
    }

    public void requestPermission(){
        isActivity = true;
        Uri uri = Uri.parse("package:"+ getPackageName());
        try {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, uri), ACTION_MANAGE_ACCESSIBILITY_REQUEST_CODE);
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri), ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            checkAlarmRemainderPermission();
        }catch (ActivityNotFoundException e){
            Toast.makeText(MainActivity.this, "This is an unsupported device.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkAlarmRemainderPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            Log.d("check", "permiss");
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //안드로이드 13 대응
            if(!alarmManager.canScheduleExactAlarms()) {
                Intent appDetail = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName()));
                appDetail.addCategory(Intent.CATEGORY_DEFAULT);
                appDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(appDetail);
            } else {
                Log.d("check", "permiss11");
                return true;
            }
        } else {
            return true;
        }
        return false;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE || requestCode == ACTION_MANAGE_ACCESSIBILITY_REQUEST_CODE) {
            if(isAccessGranted() && checkDrawPermission() && checkAlarmRemainderPermission() && !ist){
                isPermission = true;
                if(isSetPattern()){
                    if(appListAdopter != null) {
                        try {
                            setLock(appListAdopter.getLockList(), true);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }else{
                    LoadingView loadingView = new LoadingView(MainActivity.this);
                    loadingView.show("Loading...");

                    FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
                    FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600).build();
                    config.setConfigSettingsAsync(settings);
                    config.fetchAndActivate()
                                    .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Boolean> task) {
                                             loadingView.stop();
                                             if (task.isSuccessful()){
                                                 String a = config.getString("FingerPrint");
                                                 if (a.equals("First")){
                                                     showFingerDialog();
                                                 }else{
                                                     SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
                                                     SharedPreferences.Editor editor = preferences.edit();

                                                     isActivity = true;
                                                     editor.putBoolean("finger", false);
                                                     editor.apply();
                                                     Intent intent = new Intent(MainActivity.this, PatternActivity.class);
                                                     startActivity(intent);
                                                     isRequest = true;
                                                 }
                                                 Log.d("fetch", a);
                                             }else{
                                                 SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
                                                 SharedPreferences.Editor editor = preferences.edit();

                                                 isActivity = true;
                                                 editor.putBoolean("finger", false);
                                                 editor.apply();
                                                 Intent intent = new Intent(MainActivity.this, PatternActivity.class);
                                                 startActivity(intent);
                                                 isRequest = true;
                                             }
                                        }
                                    });


                }
            }
        }
        if(requestCode == REQUEST_CODE_AD && resultCode == 98 && data.getAction().equals("ADS_OFF")){
            getPackageList();
        }
    }

    public void showFingerDialog(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mdialog = inflater.inflate(R.layout.pingerprint_dialog, null);
        AlertDialog.Builder buider = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        buider.setView(mdialog);
        buider.setCancelable(false);
        Dialog dialog = buider.create();
        MaterialCardView cancel = mdialog.findViewById(R.id.cancel);
        MaterialCardView ok = mdialog.findViewById(R.id.ok);

        SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActivity = true;
                editor.putBoolean("finger", true);
                editor.apply();
                Intent intent = new Intent(MainActivity.this, PatternActivity.class);
                startActivity(intent);
                dialog.dismiss();
                isRequest = true;
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActivity = true;
                editor.putBoolean("finger", false);
                editor.apply();
                Intent intent = new Intent(MainActivity.this, PatternActivity.class);
                startActivity(intent);
                dialog.dismiss();
                isRequest = true;
            }
        });

        dialog.show();
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }



    public void makeDialog(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mdialog = inflater.inflate(R.layout.permission_dialog, null);
        AlertDialog.Builder buider = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        buider.setView(mdialog);
        buider.setCancelable(true);
        Dialog dialog = buider.create();
        MaterialCardView ok = mdialog.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
                ist = false;
                dialog.dismiss();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Authorize");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainScreen");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
                mFirebaseAnalytics.logEvent("Authorize", bundle);
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }


    public void doneLockDialog(){
        ist = true;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mdialog = inflater.inflate(R.layout.permission_dialog, null);
        AlertDialog.Builder buider = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        buider.setView(mdialog);
        buider.setCancelable(true);
        Dialog dialog = buider.create();
        MaterialCardView ok = mdialog.findViewById(R.id.ok);
        TextView tv_title = mdialog.findViewById(R.id.textView12);
        TextView tv_text = mdialog.findViewById(R.id.textView6);
        TextView tv_ok = mdialog.findViewById(R.id.tvok);
        ImageView done = mdialog.findViewById(R.id.done);
        tv_title.setText(R.string.done_lock);
        tv_ok.setText(R.string.ok);
        tv_text.setVisibility(View.INVISIBLE);
        done.setVisibility(View.VISIBLE);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Lock_Complete");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainScreen");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
                mFirebaseAnalytics.logEvent("Lock_Complete", bundle);
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }


    public void setLock(ArrayList<String> packages, boolean isSetting) throws JSONException {
        if(isAccessGranted() && checkDrawPermission() && checkAlarmRemainderPermission()){
            SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            JSONObject jsonObject = new JSONObject();
            JSONArray array = new JSONArray();
            for(String p : packages){
                array.put(p);
            }
            jsonObject.put("list", array);
            editor.putString("locklist", jsonObject.toString());
            editor.apply();

            if(!isServiceRunning()){
                Intent serviceIntent = new Intent(this, LockService.class);
                serviceIntent.setAction("ACTION_START");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                }else{
                    startService(serviceIntent);
                }
            }else{
                LockService.isStop = array.length() == 0;
            }

            if (isSetting) {
                doneLockDialog();
            }
            btn_lock.setVisibility(View.GONE);
        }else{
            makeDialog();
        }
    }

    public void startService(){
        if(!isServiceRunning()){
            Intent serviceIntent = new Intent(this, LockService.class);
            serviceIntent.setAction("ACTION_START");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }


    private boolean isServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LockService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }




    public class AppListAdopter extends RecyclerView.Adapter<AppListAdopter.ViewHolder> {

        Context context;
        List<App> mApps;

        private RelativeLayout lock_all;
        private TextView tv_all;
        private ImageView iv_all;
        private int count;

        private TextView st_title;
        private TextView st_text;
        private ImageView st_icon;

        private CardView adCard;
        private AdView adView;

        private final boolean isPremium;

        AppListAdopter(List<App> mApps, int count, boolean isPremium){
            this.mApps = mApps;
            this.count = count;
            this.isPremium = isPremium;
            if(!isSetPattern()){
                for (App app : mApps){
                    if (app.isDefault) this.count++;
                }
                btn_lock.setText(String.format(getString(R.string.lock) + "(%d)", this.count));
            }

        }

        @NonNull
        @Override
        public AppListAdopter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
            View view = inflater.inflate(R.layout.app_list_layout, parent, false);
            return new AppListAdopter.ViewHolder(view);
        }



        public void setUnlock(String packageName) throws JSONException {
            SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            String s = preferences.getString("locklist", "");

            JSONObject jsonObject;
            JSONArray array;

            if(!s.equals("")){
                jsonObject = new JSONObject(s);
                array = jsonObject.getJSONArray("list");
                for(int i = 0; i < array.length(); i++){
                    if(array.getString(i).equals(packageName)){
                        array.remove(i);
                        break;
                    }
                }
                LockService.isStop = array.length() == 0;

                jsonObject.put("list", array);
                editor.putString("locklist", jsonObject.toString());
                editor.apply();
            }
        }





        public ArrayList<String> getLockList(){
            ArrayList<String> list = new ArrayList<>();
            for(App app : mApps){
                if(app.isLock) list.add(app.pack);
            }
            return list;
        }

        @Override
        public void onBindViewHolder(@NonNull AppListAdopter.ViewHolder holder, int position) {

            holder.adView.setTag(position);

            if(position == 0){
                this.lock_all = holder.lock_all;
                this.tv_all = holder.tv_all;
                this.iv_all = holder.iv_all;
                this.st_title = holder.st_title;
                this.st_icon = holder.st_icon;
                this.st_text = holder.st_text;

                this.adCard = holder.adCard;
                this.adView = holder.adView;

                holder.cd.setVisibility(View.VISIBLE);
                holder.lock_all.setVisibility(View.VISIBLE);

                if(count == mApps.size()){
                    lock_all.setTag(true);
                    tv_all.setTextColor(Color.parseColor("#186599"));
                    iv_all.setImageResource(R.drawable.check_all);
                    iv_all.setImageTintList(ColorStateList.valueOf(Color.parseColor("#186599")));
                }else{
                    holder.lock_all.setTag(false);
                    holder.tv_all.setTextColor(Color.parseColor("#818181"));
                    holder.iv_all.setImageResource(R.drawable.check_off);
                    holder.iv_all.setImageTintList(ColorStateList.valueOf(Color.parseColor("#818181")));
                }

                int lock_count = 0;
                for(App app : mApps){
                    if(app.isLock) lock_count++;
                }
                if(lock_count == mApps.size()){
                    holder.st_icon.setImageResource(R.drawable.heart);
                    holder.st_title.setText("Perfect!");
                    holder.st_text.setText(R.string.perfect);
                }else if(lock_count >= 5){
                    holder.st_icon.setImageResource(R.drawable.happy);
                    holder.st_title.setText("Good!");
                    holder.st_text.setText(R.string.safe);
                }else{
                    holder.st_icon.setImageResource(R.drawable.cry);
                    holder.st_title.setText("Weak!");
                    holder.st_text.setText(R.string.week);
                }
                setAd(isPremium, holder.adView, holder.adCard);
            } else{
                holder.cd.setVisibility(View.GONE);
                holder.lock_all.setVisibility(View.GONE);
                holder.adView.setActivated(false);
                holder.adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                    }
                });
                holder.adCard.setVisibility(View.GONE);
            }

            App app = mApps.get(position);
            String name = app.name;
            Drawable drawable = app.icon;
            holder.name.setText(name);
            Glide.with(MainActivity.this).load(drawable).into(holder.icon);

            if(app.isLock){
                holder.app.setCardBackgroundColor(Color.parseColor("#E0ECF2"));
                holder.lock.setImageResource(R.drawable.lock_48px);
                holder.lock.setImageTintList(ColorStateList.valueOf(Color.parseColor("#265499")));
            }else{
                holder.app.setCardBackgroundColor(Color.WHITE);
                holder.lock.setImageResource(R.drawable.lock_open_48px);
                holder.lock.setImageTintList(ColorStateList.valueOf(Color.parseColor("#eeeeee")));
            }

            Object[] objects = new Object[]{holder.app, holder.lock, position};
            holder.lock.setTag(objects);
            holder.app.setTag(objects);

            if(isFirst && app.isDefault){
                holder.app.setCardBackgroundColor(Color.parseColor("#E0ECF2"));
                holder.lock.setImageResource(R.drawable.lock_48px);
                holder.lock.setImageTintList(ColorStateList.valueOf(Color.parseColor("#265499")));
                app.isLock = true;
                app.isDefault = false;

                if(count == mApps.size()){
                    lock_all.setTag(true);
                    tv_all.setTextColor(Color.parseColor("#186599"));
                    iv_all.setImageResource(R.drawable.check_all);
                    iv_all.setImageTintList(ColorStateList.valueOf(Color.parseColor("#186599")));
                }
            }

            holder.lock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Object[] objects = (Object[]) view.getTag();
                    CardView cardView = (CardView) objects[0];
                    ImageView iv = (ImageView) objects[1];
                    int pos = (int) objects[2];
                    App app = mApps.get(pos);
                    lock(cardView, iv, app);
                }
            });

            holder.app.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Object[] objects = (Object[]) view.getTag();
                    CardView cardView = (CardView) objects[0];
                    ImageView iv = (ImageView) objects[1];
                    int pos = (int) objects[2];

                    App app = mApps.get(pos);
                    lock(cardView, iv, app);
                }
            });

            holder.lock_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean tag = (boolean) view.getTag();
                    if(tag){
                        unlockAll();
                    }else{
                        lockAll();
                    }
                }
            });

        }

        public void setAd(boolean isPremium, AdView adView, CardView adCard){
            if(isPremium){
                if(adCard != null){
                    adView.setActivated(false);
                    adCard.setVisibility(View.GONE);
                }
            }else{
                if(adCard != null){
                    adCard.setVisibility(View.INVISIBLE);
                    adView.setActivated(true);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    adView.loadAd(adRequest);
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            super.onAdLoaded();
                            adCard.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            LockService.isAdClick = true;
                        }

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            LockService.isAdClick = false;
                        }
                    });
                }
            }
        }

        public void lock(CardView cardView, ImageView iv, App app){
            if(app.isLock){
                try {
                    setUnlock(app.pack);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                app.isLock = false;
                count--;
                cardView.setCardBackgroundColor(Color.WHITE);
                iv.setImageResource(R.drawable.lock_open_48px);
                iv.setImageTintList(ColorStateList.valueOf(Color.parseColor("#eeeeee")));

                btn_lock.setText(String.format(getString(R.string.lock) + "(%d)", count));

                lock_all.setTag(false);
                tv_all.setTextColor(Color.parseColor("#818181"));
                iv_all.setImageResource(R.drawable.check_off);
                iv_all.setImageTintList(ColorStateList.valueOf(Color.parseColor("#818181")));

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Unlock");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainScreen");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
                mFirebaseAnalytics.logEvent("Unlock", bundle);
            }else{

                cardView.setCardBackgroundColor(Color.parseColor("#E0ECF2"));
                iv.setImageResource(R.drawable.lock_48px);
                iv.setImageTintList(ColorStateList.valueOf(Color.parseColor("#265499")));

                app.isLock = true;
                count++;
                btn_lock.setText(String.format(getString(R.string.lock) + "(%d)", count));
                if(count == mApps.size()){
                    lock_all.setTag(true);
                    tv_all.setTextColor(Color.parseColor("#186599"));
                    iv_all.setImageResource(R.drawable.check_all);
                    iv_all.setImageTintList(ColorStateList.valueOf(Color.parseColor("#186599")));
                }

                if (isSetPattern()){
                    lock_click();
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Lock");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainScreen");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
                mFirebaseAnalytics.logEvent("Lock", bundle);
            }
            int lock_count = 0;
            for(App appq : mApps){
                if(appq.isLock) lock_count++;
            }
            if(lock_count == mApps.size()){
                st_icon.setImageResource(R.drawable.heart);
                st_title.setText("Perfect!");
                st_text.setText(getString(R.string.perfect));
            }else if(lock_count >= 5){
                st_icon.setImageResource(R.drawable.happy);
                st_title.setText("Good!");
                st_text.setText(getString(R.string.safe));
            }else{
                st_icon.setImageResource(R.drawable.cry);
                st_title.setText("Weak!");
                st_text.setText(getString(R.string.week));
            }
        }

        public void lockAll(){
            for(App app : mApps){
                app.isLock = true;
            }
            notifyDataSetChanged();
            count = mApps.size();
            btn_lock.setText(String.format(getString(R.string.lock) + "(%d)", count));

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "PA_AllLock");
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainScreen");
            bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
            mFirebaseAnalytics.logEvent("PA_AllLock", bundle);

            if (isSetPattern()){
                lock_click();
            }
        }

        public void unlockAll(){
            for(App app : mApps){
                app.isLock = false;
                try {
                    setUnlock(app.pack);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            count = 0;
            isFirst = false;

            notifyDataSetChanged();
            btn_lock.setText(String.format(getString(R.string.lock) + "(%d)", count));

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "PA_UnLock");
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainScreen");
            bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
            mFirebaseAnalytics.logEvent("PA_UnLock", bundle);
        }

        @Override
        public int getItemCount() {
            return mApps.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView st_icon;
            TextView st_title;
            TextView st_text;

            RelativeLayout lock_all;
            TextView tv_all;
            ImageView iv_all;
            CardView cd;

            CardView app;
            ImageView icon;
            TextView name;
            ImageView lock;

            CardView adCard;

            AdView adView;

            ViewHolder(View itemView) {
                super(itemView);
                st_icon = itemView.findViewById(R.id.st_icon);
                st_text = itemView.findViewById(R.id.st_text);
                st_title = itemView.findViewById(R.id.st_title);
                lock_all = itemView.findViewById(R.id.lock_all);
                tv_all = itemView.findViewById(R.id.tv_all);
                iv_all = itemView.findViewById(R.id.iv_all);

                cd = itemView.findViewById(R.id.cd);
                app = itemView.findViewById(R.id.app);
                icon = itemView.findViewById(R.id.icon);
                name = itemView.findViewById(R.id.name);
                lock = itemView.findViewById(R.id.lock);

                adCard = itemView.findViewById(R.id.ads);
                adView = itemView.findViewById(R.id.adView);

                adView.setAdListener(new LockService.MyAdListener(MainActivity.this));
            }
        }


    }




    }