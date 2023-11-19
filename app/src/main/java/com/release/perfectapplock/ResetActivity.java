package com.release.perfectapplock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

public class ResetActivity extends AppCompatActivity {


    private PatternLockView mPatternLockView;
    private RelativeLayout pattern_view;
    private RelativeLayout pin_view;
    private TextView tv_lock;
    private ImageView[] dots;
    private StringBuilder password;

    private String pack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        Intent intent = getIntent();
        pack = intent.getStringExtra("package");

        tv_lock = findViewById(R.id.lock);
        mPatternLockView = findViewById(R.id.pattern);
        pattern_view = findViewById(R.id.patternView);
        pin_view = findViewById(R.id.pinView);
        ImageView iv_icon = findViewById(R.id.icon);
        iv_icon.setImageResource(R.drawable.logo);

        dots = new ImageView[4];
        dots[0] = findViewById(R.id.iv1);
        dots[1] = findViewById(R.id.iv2);
        dots[2] = findViewById(R.id.iv3);
        dots[3] = findViewById(R.id.iv4);
        showLocker();

        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> dots) {
                if(dots.size() > 3){
                    String sha1 = PatternLockUtils.patternToSha1(mPatternLockView, dots);
                    if(Util.isRightPattern(ResetActivity.this, sha1)){
                        try {
                            setUnlock(pack);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        finish();
                    }else{
                        wrongPattern();
                    }
                }else{
                    wrongPattern();
                }
            }

            @Override
            public void onCleared() {

            }
        });


        ImageView back = findViewById(R.id.more2);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    void wrongPattern(){
        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
        mPatternLockView.setInputEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPatternLockView.setInputEnabled(true);
                mPatternLockView.clearPattern();
            }
        }, 500);
    }

    void remove(){
        dots[password.length()-1].setImageTintList(ColorStateList.valueOf(Color.parseColor("#6D6D6D")));
        password.deleteCharAt(password.length()-1);
    }
    void wrongPassword(){
        password = new StringBuilder();
        for(int i = 0 ; i < 4; i++){
            dots[i].setImageResource(R.drawable.elip);
            dots[i].setImageTintList(ColorStateList.valueOf(Color.parseColor("#A30000")));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 4; i++) {
                    dots[i].setImageResource(R.drawable.elip);
                    dots[i].setImageTintList(ColorStateList.valueOf(Color.parseColor("#6D6D6D")));
                }
            }

        }, 1000);
    }

    String strToSha1(String s){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1"); // 이 부분을 SHA-256, MD5로만 바꿔주면 된다.
            md.update(s.getBytes());

            byte[] byteData = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte byteDatum : byteData) {
                sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
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

    public void onClick(View v){
        if(v.getId() == R.id.iv_remove){
            remove();
        }else{
            char c = ((TextView) v).getText().charAt(0);
            password.append(c);
            dots[password.length()-1].setImageTintList(ColorStateList.valueOf(Color.parseColor("#186599")));
            if(password.length() == 4){
                String sha1 = strToSha1(password.toString());
                if(Util.isRightPin(ResetActivity.this, sha1)) {
                    try {
                        setUnlock(pack);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    finish();
                }else {
                    wrongPassword();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
    void showLocker(){

        if(Util.getLockType(ResetActivity.this)){
            tv_lock.setText(R.string.draw);
            pattern_view.setVisibility(View.VISIBLE);
        }else{
            tv_lock.setText(R.string.pin_input);
            password = new StringBuilder();
            pin_view.setVisibility(View.VISIBLE);
        }
    }
}