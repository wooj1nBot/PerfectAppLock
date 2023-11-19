package com.release.perfectapplock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

public class PatternActivity extends AppCompatActivity {

    PatternLockView mPatternLockView;
    String pattern;
    TextView tv_title;
    TextView tv_text;
    TextView tv_reset;
    int wrong_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);
        tv_reset = findViewById(R.id.reset);
        tv_title = findViewById(R.id.textView4);
        tv_text = findViewById(R.id.textView5);
        TextView tv_pin = findViewById(R.id.pin);
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        tv_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "ChangeTo_Pincode");
                mFirebaseAnalytics.logEvent("ChangeTo_Pincode", bundle);

                Intent intent = new Intent(PatternActivity.this, PinActivity.class);
                startActivity(intent);
                finish();
            }
        });
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mPatternLockView = findViewById(R.id.pattern);
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
                    if(pattern == null){
                        mPatternLockView.setInputEnabled(false);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mPatternLockView.setInputEnabled(true);
                                mPatternLockView.clearPattern();
                            }
                        }, 500);
                        pattern = sha1;
                        tv_title.setText(R.string.check_pattern);
                        tv_reset.setVisibility(View.VISIBLE);
                    }else{
                        if(pattern.equals(sha1)){
                            savePattern(sha1);
                            finish();
                        }else{
                            wrong_count++;
                            wrongPattern();
                        }
                    }
                }else{
                    wrongPattern();
                }
            }

            @Override
            public void onCleared() {

            }
        });

        tv_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

    }

    void wrongPattern(){
        String str = tv_title.getText().toString();
        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
        tv_text.setText(R.string.wrong_pattern);
        tv_text.setTextColor(Color.parseColor("#A30000"));
        tv_title.setText(R.string.retry);
        mPatternLockView.setInputEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPatternLockView.setInputEnabled(true);
                if(wrong_count != 2) {
                    tv_title.setText(R.string.pattern_title);
                    tv_text.setTextColor(Color.parseColor("#464646"));
                    tv_text.setText(R.string.connect);
                    tv_title.setText(str);
                    mPatternLockView.clearPattern();
                }else{
                    reset();
                }

            }
        }, 500);
    }

    void savePattern(String sha1){
        SharedPreferences.Editor editor = getSharedPreferences("User", MODE_PRIVATE).edit();
        editor.remove("password");
        editor.putString("pattern", sha1);
        editor.apply();
    }

    void reset(){
        wrong_count = 0;
        mPatternLockView.clearPattern();
        tv_text.setTextColor(Color.parseColor("#464646"));
        tv_text.setText(R.string.connect);
        tv_title.setText(R.string.pattern_title);
        tv_reset.setVisibility(View.GONE);
        pattern = null;
    }
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

}