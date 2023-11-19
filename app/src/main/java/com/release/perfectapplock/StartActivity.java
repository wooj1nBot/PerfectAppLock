package com.release.perfectapplock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

public class StartActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Button button = findViewById(R.id.button);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(StartActivity.this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.setBegin(StartActivity.this, false);
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "PA_START");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "StartScreen");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "StartActivity");
                mFirebaseAnalytics.logEvent("PA_START", bundle);
            }
        });

        TextView textView = findViewById(R.id.tv_app);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy)));
                startActivity(browserIntent);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "PA_Privacy");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "StartScreen");
                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "StartActivity");
                mFirebaseAnalytics.logEvent("PA_Privacy", bundle);
            }
        });

    }





}