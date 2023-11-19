package com.release.perfectapplock;

import static android.content.Context.MODE_PRIVATE;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Util {

    public static final int LOCK_MODE_APP_OFF = 1;
    public static final int LOCK_MODE_DISPLAY_OFF = 2;

    public static boolean isBegin(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getBoolean("begin", true);
    }
    public static void setBegin(Context context, boolean b){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("begin", b);
        editor.apply();
    }


    public static boolean isFinger(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getBoolean("finger", true);
    }
    public static void setFinger(Context context, boolean b){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("finger", b);
        editor.apply();
    }

    public static boolean isTransPattern(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getBoolean("trans_pattern", false);
    }
    public static void setTransPattern(Context context, boolean b){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("trans_pattern", b);
        editor.apply();
    }

    public static boolean isSpyCamera(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getBoolean("spy_camera", false);
    }
    public static void setSpyCamera(Context context, boolean b){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("spy_camera", b);
        editor.apply();
    }

    public static int getSpyCameraPos(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getInt("spy_camera_count", 2);
    }
    public static void setSpyCameraPos(Context context, int m){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("spy_camera_count", m);
        editor.apply();
    }



    public static boolean isNotification(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getBoolean("notification", true);
    }
    public static void setNotification(Context context, boolean b){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notification", b);
        editor.apply();
    }

    public static int getLockMode(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getInt("lockmode", LOCK_MODE_APP_OFF);
    }
    public static void setLockMode(Context context, int m){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lockmode", m);
        editor.apply();
    }

    public static int getLauncher(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getInt("launcher", 0);
    }
    public static void setLauncher(Context context, int m){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("launcher", m);
        editor.apply();
    }

    public static String getShortCut(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getString("shortcut", "applock1121");
    }
    public static void setShortCut(Context context, String id){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("shortcut", id);
        editor.apply();
    }

    public static void addLock(String packageName, Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        try {
            String s = preferences.getString("locklist", "");
            JSONObject jsonObject;
            if (s.equals("")){
                jsonObject = new JSONObject();
            }else{
                jsonObject = new JSONObject(s);
            }
            JSONArray array;
            if (jsonObject.isNull("list")){
                array = new JSONArray();
            }else{
                array = jsonObject.getJSONArray("list");
            }
            array.put(packageName);
            jsonObject.put("list", array);
            editor.putString("locklist", jsonObject.toString());
            editor.apply();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }



    public static void addLockedList(String packageName, Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        try {
            String s = preferences.getString("current_lock", "");
            JSONObject jsonObject;
            if (s.equals("")){
                jsonObject = new JSONObject();
            }else{
                jsonObject = new JSONObject(s);
            }
            JSONArray array;
            if (jsonObject.isNull("list")){
                array = new JSONArray();
            }else{
                array = jsonObject.getJSONArray("list");
            }
            array.put(packageName);
            jsonObject.put("list", array);
            editor.putString("current_lock", jsonObject.toString());
            editor.apply();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean isLockedList(String packageName, Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        try {
            JSONObject jsonObject = new JSONObject(preferences.getString("current_lock", ""));
            JSONArray array = jsonObject.getJSONArray("list");
            for (int i = 0; i < array.length(); i++){
                if (array.getString(i).equals(packageName)) return true;
            }
            return false;
        } catch (JSONException e) {
            return false;
        }
    }

    public static void removeLockedList(String packageName, Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        try {
            JSONObject jsonObject = new JSONObject(preferences.getString("current_lock", ""));
            JSONArray array = jsonObject.getJSONArray("list");
            for (int i = 0; i < array.length(); i++){
                if (array.getString(i).equals(packageName)) {
                    array.remove(i);
                    jsonObject.put("list", array);
                    editor.putString("current_lock", jsonObject.toString());
                    editor.apply();
                    return;
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLockedList(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);

        try {
            JSONObject jsonObject = new JSONObject(preferences.getString("current_lock", ""));
            JSONArray array = jsonObject.getJSONArray("list");
            return array.toString();
        } catch (JSONException e) {
            return "";
        }
    }



    public static void removeAllLockedList(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("current_lock");
        editor.apply();
    }

    public static boolean isRightPattern(Context context, String sha1){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getString("pattern", "").equals(sha1);
    }
    public static boolean isRightPin(Context context, String sha1){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.getString("password", "").equals(sha1);
    }
    public static boolean getLockType(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return !preferences.getString("pattern", "").equals("");
    }



    public static boolean getIsLock(Context context){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
        return preferences.contains("pattern") || preferences.contains("password");
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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

    public static String getAppLabel(Context context, String packageName){
        PackageManager packagemanager = context.getPackageManager();
        try {
            ApplicationInfo info = packagemanager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return packagemanager.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "app";
        }
    }
    public static Drawable getAppIcon(Context context, String packageName){
        PackageManager packagemanager = context.getPackageManager();
        try {
            ApplicationInfo info = packagemanager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return packagemanager.getApplicationIcon(info);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isLock(Context context, String packageName){
        SharedPreferences preferences = context.getSharedPreferences("User", MODE_PRIVATE);
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


    public static void setAlarm(Context context){
        Intent receiverIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent mAlarmIntent = PendingIntent.getBroadcast(context, 958, receiverIntent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager mAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5, mAlarmIntent);
    }

    public static void removeAlarm(Context context){
        AlarmManager mAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent receiverIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent mAlarmIntent = PendingIntent.getBroadcast(context, 958, receiverIntent, PendingIntent.FLAG_MUTABLE);
        mAlarmMgr.cancel(mAlarmIntent);
        mAlarmIntent.cancel();
    }








}
