package com.release.perfectapplock;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;

public class LockActivity extends AppCompatActivity {
    String packageName;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private PatternLockView mPatternLockView;
    private RelativeLayout pattern_view;
    private RelativeLayout pin_view;
    private TextView tv_lock;
    private ImageView finger;
    private ImageView[] dots;
    private StringBuilder password;

    private int wrong_finger;
    private int wrong_count;
    private boolean isMain;

    private boolean isActivity = false;

    private Handler handler;

    private boolean isEnable = true;
    FirebaseAnalytics mFirebaseAnalytics;

    public boolean isHome = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lock);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        isHome = false;

        Intent intent = getIntent();
        packageName = intent.getStringExtra("package");
        isMain = intent.getBooleanExtra("main", false);

        tv_lock = findViewById(R.id.lock);
        mPatternLockView = findViewById(R.id.pattern);
        pattern_view = findViewById(R.id.patternView);
        pin_view = findViewById(R.id.pinView);
        finger = findViewById(R.id.finger);
        ImageView iv_ad = findViewById(R.id.add_off);
        LinearLayout admother = findViewById(R.id.admother);

        TextView tv_name = findViewById(R.id.tv_name);
        ImageView iv_icon = findViewById(R.id.icon);
        ImageView more = findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isMain){
                    isActivity = true;
                }
                Intent intent = new Intent(LockActivity.this, ResetActivity.class);
                intent.putExtra("package", packageName);
                startActivity(intent);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "AppLock_UnLock");
                mFirebaseAnalytics.logEvent("AppLock_UnLock", bundle);
            }
        });

        String label = Util.getAppLabel(this, packageName);

        tv_name.setText(label);

        Drawable icon = Util.getAppIcon(this, packageName);
        if(icon != null){
            iv_icon.setImageDrawable(icon);
        }

        dots = new ImageView[4];
        dots[0] = findViewById(R.id.iv1);
        dots[1] = findViewById(R.id.iv2);
        dots[2] = findViewById(R.id.iv3);
        dots[3] = findViewById(R.id.iv4);

        AdView adView = null;
        if (LockService.adManger != null) {
            adView = LockService.adManger.getAdView();
            if (adView.getParent() != null)
                ((ViewGroup) adView.getParent()).removeView(adView);
            adView.setVisibility(View.VISIBLE);
            adView.setActivated(true);
            admother.addView(adView);
        }

        if(isMain){
            if (adView != null){
                adView.setVisibility(View.INVISIBLE);
            }
            iv_ad.setVisibility(View.INVISIBLE);
            more.setVisibility(View.INVISIBLE);
        }else{

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "AppLock");
            mFirebaseAnalytics.logEvent("AppLock", bundle);

            handler = new Handler();
            AdView finalAdView = adView;
            new BillingEntireManager(LockActivity.this, new CheckPurchaseCallback() {
                @Override
                void onPurchaseComplete(List<Purchase> purchases, BillingResult billingResult) {
                    boolean isPurchase = purchases.size() > 0;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isPurchase) {
                                if (finalAdView != null){
                                    finalAdView.setVisibility(View.VISIBLE);
                                }
                                iv_ad.setVisibility(View.VISIBLE);
                            } else {
                                if (finalAdView != null){
                                    finalAdView.setVisibility(View.INVISIBLE);
                                    finalAdView.setActivated(false);
                                }
                                iv_ad.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            });
        }

        iv_ad.setVisibility(View.VISIBLE);

        iv_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isMain){
                    isActivity = true;
                }
                Intent intent = new Intent(LockActivity.this, AdActivity.class);
                startActivity(intent);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "AppLock_Noads");
                mFirebaseAnalytics.logEvent("AppLock_Noads", bundle);
            }
        });

        if(Util.isFinger(LockActivity.this)){
            executor = ContextCompat.getMainExecutor(this);
            BiometricManager biometricManager = BiometricManager.from(this);
            biometricPrompt = new BiometricPrompt(this,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(getApplicationContext(),
                                    R.string.finger_error, Toast.LENGTH_SHORT)
                            .show();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    openApp();

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "AppLock_Pass_FingerPrint");
                    mFirebaseAnalytics.logEvent("AppLock_Pass_FingerPrint", bundle);
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    wrong_finger++;
                    if(wrong_finger == 10){
                        wrong_finger = 0;
                        showLocker();
                        biometricPrompt.cancelAuthentication();
                    }else{
                        Toast.makeText(getApplicationContext(),
                                        R.string.retry, Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.finger))
                    .setNegativeButtonText(getString(R.string.cancel))
                    .setDeviceCredentialAllowed(false)
                    .build();

            switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
                case BiometricManager.BIOMETRIC_SUCCESS:
                    biometricPrompt.authenticate(promptInfo);
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                    showLocker();
                    break;
                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                    // Prompts the user to create credentials that your app accepts.
                    Toast.makeText(LockActivity.this,
                            R.string.finger_no_enroll, Toast.LENGTH_SHORT).show();
                    showLocker();
                    break;
            }

        }else{
            showLocker();
        }

        if(Util.isTransPattern(LockActivity.this)){
            mPatternLockView.setInStealthMode(true);
        }

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
                    if(Util.isRightPattern(LockActivity.this, sha1)){
                        openApp();

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "AppLock_Pass_Pattern");
                        mFirebaseAnalytics.logEvent("AppLock_Pass_Pattern", bundle);
                    }else{
                        wrong_count++;
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

        tv_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLocker();
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "AppLock_Other");
                mFirebaseAnalytics.logEvent("AppLock_Other", bundle);
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    void remove(){
        if (password.length() > 0) {
            dots[password.length() - 1].setImageTintList(ColorStateList.valueOf(Color.parseColor("#6D6D6D")));
            password.deleteCharAt(password.length() - 1);
        }
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
                    String sha1 = strToSha1(password.toString());
                    if(Util.isRightPin(LockActivity.this, sha1)) {
                       openApp();

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, "AppLock_Pass_Pincode");
                        mFirebaseAnalytics.logEvent("AppLock_Pass_Pincode", bundle);
                    }else {
                        wrongPassword();
                    }
            }
        }
    }

    void showLocker(){
        finger.setVisibility(View.GONE);
        tv_lock.setVisibility(View.GONE);

        if(Util.getLockType(LockActivity.this)){
            pattern_view.setVisibility(View.VISIBLE);
        }else{
            password = new StringBuilder();
            pin_view.setVisibility(View.VISIBLE);
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
        int count = getResources().getIntArray(R.array.spinner_int_array)[Util.getSpyCameraPos(LockActivity.this)];
        if(wrong_count == count){
            CameraView frontCapture = new CameraView(
                    LockActivity.this);
            frontCapture.capturePhoto();
            wrong_count = 0;
        }
    }

    void wrongPassword(){
        password = new StringBuilder();
        isEnable = false;

        for(int i = 0 ; i < 4; i++){
            dots[i].setImageResource(R.drawable.elip);
            dots[i].setImageTintList(ColorStateList.valueOf(Color.parseColor("#A30000")));
        }

        wrong_count++;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isEnable = true;
                for (int i = 0; i < 4; i++) {
                    dots[i].setImageResource(R.drawable.elip);
                    dots[i].setImageTintList(ColorStateList.valueOf(Color.parseColor("#6D6D6D")));
                }
            }

        }, 500);

        int count = getResources().getIntArray(R.array.spinner_int_array)[Util.getSpyCameraPos(LockActivity.this)];
        if(wrong_count == count){
            CameraView frontCapture = new CameraView(
                    LockActivity.this);
            frontCapture.capturePhoto();
            wrong_count = 0;
        }
    }

    void openApp(){
        if(!isMain) {
            Util.addLockedList(packageName, LockActivity.this);

            SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            int cnt = preferences.getInt("lockCount", 0) + 1;
            if (cnt == 10){
                showRateDialog(LockActivity.this);
            }
            editor.putInt("lockCount", cnt);
            editor.apply();
        }else{
            int m = Util.getLauncher(LockActivity.this);
            Intent intent = null;
            try {
                intent = new Intent(LockActivity.this, getClassLoader().loadClass(IconActivity.names[m]));
                intent.putExtra("lock", true);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 100);
    }

    public void showRateDialog(Context context){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rate_dialog);
        dialog.setCancelable(true);

        TextView tv_rate = dialog.findViewById(R.id.rate);
        tv_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                startActivity(intent);
                dialog.dismiss();
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
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isActivity = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        LockService.isStop = false;

        if(isActivity || LockService.isAdClick){
            finish();
            return;
        }
        if(isMain) return;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LockService.isStop = false;
    }

    @Override
    public void onBackPressed() {
        gotoHome();
    }

    private void gotoHome(){
        isHome = true;
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(homeIntent);
        finish();
    }
}