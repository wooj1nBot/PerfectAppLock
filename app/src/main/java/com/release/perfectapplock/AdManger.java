package com.release.perfectapplock;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Timer;
import java.util.TimerTask;

public class AdManger {

    AdView adView;
    Context context;

    public AdManger(Context context){
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        this.context = context;
        adView = new AdView(context);
        adView.setAdUnitId(context.getString(R.string.ad_rect_banner_unitId));
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdListener(new LockService.MyAdListener());
    }

    public void startLoadAd(){

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public AdView getAdView(){
        if (!adView.isLoading()){
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
        return adView;
    }

}
