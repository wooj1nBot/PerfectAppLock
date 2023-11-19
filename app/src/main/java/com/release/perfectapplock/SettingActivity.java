package com.release.perfectapplock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.analytics.FirebaseAnalytics;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        RelativeLayout reset = findViewById(R.id.r1);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, PatternActivity.class);
                startActivity(intent);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "PasswordReset");
                mFirebaseAnalytics.logEvent("PasswordReset", bundle);
            }
        });

        RelativeLayout finger = findViewById(R.id.r2);
        ImageView tg_finger = findViewById(R.id.tg_finger);

        if(Util.isFinger(SettingActivity.this)){
            tg_finger.setImageResource(R.drawable.toggle_on);
            finger.setTag(true);
        }else{
            tg_finger.setImageResource(R.drawable.toggle_off);
            finger.setTag(false);
        }

        finger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((boolean) view.getTag()){
                    tg_finger.setImageResource(R.drawable.toggle_off);
                    finger.setTag(false);
                    Util.setFinger(SettingActivity.this, false);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "FingerPrintLock_Off");
                    mFirebaseAnalytics.logEvent("FingerPrintLock_Off", bundle);
                }else{
                    tg_finger.setImageResource(R.drawable.toggle_on);
                    finger.setTag(true);
                    Util.setFinger(SettingActivity.this, true);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "FingerPrintLock_On");
                    mFirebaseAnalytics.logEvent("FingerPrintLock_On", bundle);
                }
            }
        });

        RelativeLayout pattern = findViewById(R.id.r3);
        ImageView tg_pattern = findViewById(R.id.tg_pattern);

        if(Util.isTransPattern(SettingActivity.this)){
            tg_pattern.setImageResource(R.drawable.toggle_on);
            pattern.setTag(true);


        }else{
            tg_pattern.setImageResource(R.drawable.toggle_off);
            pattern.setTag(false);


        }

        pattern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((boolean) view.getTag()){
                    tg_pattern.setImageResource(R.drawable.toggle_off);
                    pattern.setTag(false);
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Transparent_Off");
                    mFirebaseAnalytics.logEvent("Transparent_Off", bundle);

                    Util.setTransPattern(SettingActivity.this, false);
                }else{
                    tg_pattern.setImageResource(R.drawable.toggle_on);
                    pattern.setTag(true);
                    Util.setTransPattern(SettingActivity.this, true);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Transparent_On");
                    mFirebaseAnalytics.logEvent("Transparent_On", bundle);

                }
            }
        });

        RelativeLayout alert = findViewById(R.id.r4);
        ImageView tg_alert = findViewById(R.id.tg_alert);

        if(Util.isNotification(SettingActivity.this)){
            tg_alert.setImageResource(R.drawable.toggle_on);
            alert.setTag(true);
        }else{
            tg_alert.setImageResource(R.drawable.toggle_off);
            alert.setTag(false);
        }

        alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((boolean) view.getTag()){
                    tg_alert.setImageResource(R.drawable.toggle_off);
                    alert.setTag(false);
                    Util.setNotification(SettingActivity.this, false);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Notice_Off");
                    mFirebaseAnalytics.logEvent("Notice_Off", bundle);
                }else{
                    tg_alert.setImageResource(R.drawable.toggle_on);
                    alert.setTag(true);
                    Util.setNotification(SettingActivity.this, true);

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "Notice_On");
                    mFirebaseAnalytics.logEvent("Notice_On", bundle);
                }
            }
        });

        RelativeLayout display = findViewById(R.id.r5);
        ImageView check_display = findViewById(R.id.check1);

        RelativeLayout app = findViewById(R.id.r6);
        ImageView check_app = findViewById(R.id.check2);


        if(Util.getLockMode(SettingActivity.this) == Util.LOCK_MODE_APP_OFF){
            check_app.setVisibility(View.VISIBLE);
            check_display.setVisibility(View.INVISIBLE);
        }else{
            check_app.setVisibility(View.INVISIBLE);
            check_display.setVisibility(View.VISIBLE);
        }

        display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check_app.setVisibility(View.INVISIBLE);
                check_display.setVisibility(View.VISIBLE);
                Util.setLockMode(SettingActivity.this, Util.LOCK_MODE_DISPLAY_OFF);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "ReLock_AppOut");
                mFirebaseAnalytics.logEvent("ReLock_AppOut", bundle);
            }
        });
        app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check_display.setVisibility(View.INVISIBLE);
                check_app.setVisibility(View.VISIBLE);
                Util.setLockMode(SettingActivity.this, Util.LOCK_MODE_APP_OFF);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "ReLock_DisplayOff");
                mFirebaseAnalytics.logEvent("ReLock_DisplayOff", bundle);
            }
        });

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}