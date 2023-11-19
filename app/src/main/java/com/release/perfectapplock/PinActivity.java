package com.release.perfectapplock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PinActivity extends AppCompatActivity {

    TextView tv_title;
    TextView tv_text;
    TextView tv_reset;
    StringBuilder password;
    String pass;
    ImageView[] dots;
    int wrong_count = 0;
    private boolean isEnable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        tv_title = findViewById(R.id.textView4);
        tv_text = findViewById(R.id.textView5);
        tv_reset = findViewById(R.id.reset2);
        TextView tv_pin = findViewById(R.id.pin);
        password = new StringBuilder();
        dots = new ImageView[4];
        dots[0] = findViewById(R.id.iv1);
        dots[1] = findViewById(R.id.iv2);
        dots[2] = findViewById(R.id.iv3);
        dots[3] = findViewById(R.id.iv4);
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        tv_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "ChangeTo_Pattern");
                mFirebaseAnalytics.logEvent("ChangeTo_Pattern", bundle);

                Intent intent = new Intent(PinActivity.this, PatternActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tv_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PinActivity.this, PatternActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    void reset(){
        wrong_count = 0;
        pass = null;
        password = new StringBuilder();
        tv_text.setTextColor(Color.parseColor("#464646"));
        for(int i = 0 ; i < 4; i++){
            dots[i].setImageResource(R.drawable.elip);
            dots[i].setImageTintList(ColorStateList.valueOf(Color.parseColor("#6D6D6D")));
        }
        tv_title.setText(R.string.pin_text);
        tv_text.setText(R.string.pin_text);
        tv_reset.setVisibility(View.GONE);
    }

    void remove(){
        if (password.length() > 0) {
            dots[password.length() - 1].setImageTintList(ColorStateList.valueOf(Color.parseColor("#6D6D6D")));
            password.deleteCharAt(password.length() - 1);
        }
    }

    void wrongPassword(){
        password = new StringBuilder();
        isEnable = false;

        String str = tv_title.getText().toString();
        tv_title.setText(R.string.retry);
        tv_text.setText(R.string.wrong_pin);
        tv_text.setTextColor(Color.parseColor("#A30000"));
        for(int i = 0 ; i < 4; i++){
            dots[i].setImageResource(R.drawable.elip);
            dots[i].setImageTintList(ColorStateList.valueOf(Color.parseColor("#A30000")));
        }

        wrong_count++;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isEnable = true;
                if(wrong_count != 2) {
                    tv_text.setTextColor(Color.parseColor("#464646"));
                    tv_text.setText(R.string.pin_text);
                    tv_title.setText(str);
                    for (int i = 0; i < 4; i++) {
                        dots[i].setImageResource(R.drawable.elip);
                        dots[i].setImageTintList(ColorStateList.valueOf(Color.parseColor("#6D6D6D")));
                    }
                }else{
                    reset();
                }
            }

        }, 500);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public void onClick(View v){
        if (!isEnable) return;

        if(v.getId() == R.id.iv_remove){
            remove();
        }else{
            char c = ((TextView) v).getText().charAt(0);
            password.append(c);
            dots[password.length()-1].setImageTintList(ColorStateList.valueOf(Color.parseColor("#186599")));
            if(password.length() == 4){
                if(pass == null){
                    pass = password.toString();
                    password = new StringBuilder();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0 ; i < 4; i++){
                                dots[i].setImageResource(R.drawable.elip);
                                dots[i].setImageTintList(ColorStateList.valueOf(Color.parseColor("#6D6D6D")));
                            }
                        }
                    }, 500);
                    tv_title.setText(R.string.check_pin);
                    tv_reset.setVisibility(View.VISIBLE);
                }else{
                    if(pass.equals(password.toString())){
                        savePassword(strToSha1(pass));
                        finish();
                    }else{
                        wrongPassword();
                    }
                }
            }
        }
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

    void savePassword(String sha1){
        SharedPreferences.Editor editor = getSharedPreferences("User", MODE_PRIVATE).edit();
        editor.remove("pattern");
        editor.putString("password", sha1);
        editor.apply();
    }
}