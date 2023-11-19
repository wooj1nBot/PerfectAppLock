package com.release.perfectapplock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AdActivity extends AppCompatActivity {

    BillingEntireManager billingEntireManager;
    HashMap<String, PriceTagView> views;
    ImageView back;
    private Handler handler;
    private List<SkuDetails> skuDetailsList;
    private TextView title;
    private ImageView imageView;

    private Purchase purchase;

    private boolean isPurchase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPurchase){
                    Intent intent = new Intent();
                    intent.setAction("ADS_OFF");
                    setResult(98, intent);
                }
                finish();
            }
        });

        title = findViewById(R.id.textView9);
        imageView = findViewById(R.id.spy);

        TextView title1 = findViewById(R.id.tv_title1);
        TextView t1 = findViewById(R.id.tv_pur1);
        CardView c1 = findViewById(R.id.card1);
        PriceTagView p1 = new PriceTagView(c1, title1, t1, BillingEntireManager.SUB_IDS[0]);

        TextView title2 = findViewById(R.id.tv_title2);
        TextView t2 = findViewById(R.id.tv_pur2);
        CardView c2 = findViewById(R.id.card2);

        PriceTagView p2 = new PriceTagView(c2, title2, t2, BillingEntireManager.SUB_IDS[1]);

        TextView title3 = findViewById(R.id.tv_title3);
        TextView t3 = findViewById(R.id.tv_pur3);
        CardView c3 = findViewById(R.id.card3);
        PriceTagView p3 = new PriceTagView(c3, title3, t3, BillingEntireManager.SUB_IDS[2]);

        views = new HashMap<>();
        views.put(p1.id, p1);
        views.put(p2.id, p2);
        views.put(p3.id, p3);

        handler = new Handler();

        billingEntireManager = new BillingEntireManager(AdActivity.this, new CheckPurchaseCallback() {

            @Override
            void onSuccess(Purchase purchase) {
                isPurchase = true;
                purchaseUpdate(purchase);
            }

            @Override
            void onFailure() {

            }

            @Override
            void onSkuLoadComplete(List<SkuDetails> skuDetailsList, BillingResult billingResult) {
                AdActivity.this.skuDetailsList = skuDetailsList;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(SkuDetails skuDetails : skuDetailsList){
                            String id = skuDetails.getSku();
                            String price = skuDetails.getPrice();
                            views.get(id).setPrice(price);
                        }
                    }
                });
                if (purchase != null){
                    purchaseUpdate(purchase);
                }
            }

            @Override
            void onPurchaseComplete(List<Purchase> purchases, BillingResult billingResult) {
                if(purchases.size() > 0) {
                    AdActivity.this.purchase = purchases.get(0);
                    if(skuDetailsList != null) {
                        purchaseUpdate(purchases.get(0));
                    }
                }
            }
        });

    }

    public void purchaseUpdate(Purchase purchase){
        handler.post(new Runnable() {
            @Override
            public void run() {
                title.setText(R.string.removed_ad);
                imageView.setImageResource(R.drawable.check_circle_48px);
                imageView.setImageTintList(ColorStateList.valueOf(Color.parseColor("#7CA9C8")));

                for (SkuDetails skuDetail : skuDetailsList){
                    if(skuDetail.getSku().equals(purchase.getSkus().get(0))){
                        for(PriceTagView view : views.values()){
                            if(Objects.equals(view.id, skuDetail.getSku())){
                                view.purchase();
                            }else{
                                view.lock();
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(LockService.isPlayStore){
            LockService.isPlayStore = false;
        }else{
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(isPurchase){
            Intent intent = new Intent();
            intent.setAction("ADS_OFF");
            setResult(98, intent);
        }
        finish();
    }

    class PriceTagView{

        CardView cardView;
        TextView tv_tag;
        TextView tv_pur;

        String id;

        boolean isPurchased;

        PriceTagView(CardView cardView, TextView tv_tag, TextView tv_pur, String id){
            this.cardView = cardView;
            this.tv_tag = tv_tag;
            this.tv_pur = tv_pur;
            this.id = id;
            this.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    billingEntireManager.purchase(id, AdActivity.this);
                }
            });
        }

        void purchase(){
            cardView.setCardBackgroundColor(Color.parseColor("#7CA9C8"));
            tv_tag.setTextColor(Color.WHITE);
            tv_pur.setTextColor(Color.WHITE);
            tv_pur.setText(R.string.sub);
            isPurchased = true;
            cardView.setClickable(false);
            cardView.setEnabled(false);
        }

        void lock(){
            cardView.setCardBackgroundColor(Color.parseColor("#F9FEFF"));
            cardView.setClickable(false);
            cardView.setEnabled(false);
            tv_pur.setVisibility(View.GONE);
        }




        void setPrice(String price){
            if(id.contains("12")){
                tv_tag.setText(price + " / " + getString(R.string.year));
            } else if (id.contains("3")) {
                tv_tag.setText(price + " / " + getString(R.string.month_three));
            } else{
                tv_tag.setText(price + " / " + getString(R.string.month));
            }
        }

    }
}