package com.release.perfectapplock;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BillingEntireManager implements PurchasesUpdatedListener {
    private BillingClient mBillingClient;
    private String TAG ="BILL";
    // 아이템 상세정보 리스트
    private List<SkuDetails> mSkuDetailsList_item;
    private Context ctx;
    // 아이템 소비 리스너
    private CheckPurchaseCallback callback;

    public static String[] SUB_IDS = {"subscribe_12month", "subscribe_3m", "subscribe_1month" };

    public BillingEntireManager(Context ctx, CheckPurchaseCallback callback) {
        this.ctx = ctx;
        this.callback = callback;
        Log.d(TAG, "구글 결제 매니저를 초기화 하고 있습니다.");


        mBillingClient = BillingClient.newBuilder(ctx).setListener(this).enablePendingPurchases().build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    callback.onBillingConnected();
                    getSkuDetailList();
                    loadSubscribed();
                } else {
                    Log.d(TAG, "구글 결제 서버 접속에 실패하였습니다.\n오류코드: " + billingResult.getResponseCode());
                    callback.onFailure();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                callback.onFailure();
            }
        });

    }

    private void getSkuDetailList() {
        // 구글 상품 정보들의 ID를 만들어 줌

        // SkuDetailsList 객체를 만듬
        SkuDetailsParams.Builder params_item = SkuDetailsParams.newBuilder();
        params_item.setSkusList(Arrays.asList(SUB_IDS)).setType(BillingClient.SkuType.SUBS);

        // 비동기 상태로 앱의 정보를 가지고 옴
        mBillingClient.querySkuDetailsAsync(params_item.build(), new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {

                // 상품 정보를 가지고 오지 못한 경우
                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "(인앱) 상품 정보를 가지고 오던 중 오류가 발생했습니다.\n오류코드: " + billingResult.getResponseCode());
                    return;
                }
                if (skuDetailsList == null) {
                    Log.d(TAG, "(인앱) 상품 정보가 존재하지 않습니다.");
                    return;
                }
                //응답 받은 데이터들의 숫자를 출력
                Log.d(TAG, "(인앱) 응답 받은 데이터 숫자: " + skuDetailsList.size());

                //받아온 상품 정보를 차례로 호출
                for (int sku_idx = 0; sku_idx < skuDetailsList.size(); sku_idx++) {
                    //해당 인덱스의 객체를 가지고 옴
                    SkuDetails _skuDetail = skuDetailsList.get(sku_idx);
                    //해당 인덱스의 상품 정보를 출력
                    Log.d(TAG, _skuDetail.getSku() + ": " + _skuDetail.getTitle() + ", " + _skuDetail.getPrice());
                    Log.d(TAG, _skuDetail.getOriginalJson());
                }

                //받은 값을 멤버 변수로 저장
                mSkuDetailsList_item = skuDetailsList;
                callback.onSkuLoadComplete(skuDetailsList, billingResult);
            }
        });
    }

    //실제 구입 처리를 하는 메소드
    public void purchase(String item, Activity act) {
        LockService.isPlayStore = true;
        SkuDetails skuDetails = null;
        if (null != mSkuDetailsList_item) {
            for (int i = 0; i < mSkuDetailsList_item.size(); i++) {
                SkuDetails details = mSkuDetailsList_item.get(i);
                if (details.getSku().equals(item)) {
                    skuDetails = details;
                    break;
                }
            }

            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            mBillingClient.launchBillingFlow(act, flowParams);
        }
    }
    // 결제 요청 후 상품에 대해 소비 시켜 주는 함수
    private void handlePurchase(Purchase purchase) {

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {

            AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
            mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                    callback.onSuccess(purchase);
                }
            });
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING){
            //거래 중지 등등 ... 결제관련 문제가 발생했을때
        } else {
            //그외 알 수 없는 에러들...
        }
    }
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        LockService.isPlayStore = false;
        //결제에 성공한 경우
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            Log.d(TAG, "결제에 성공했으며, 아래에 구매한 상품들이 나열됨");
            for (Purchase _pur : purchases) {
                Log.e(TAG, "purchases: " + purchases);
                handlePurchase(_pur);
            }
        }
    }


    public void loadSubscribed(){
        mBillingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                List<Purchase> res = new ArrayList<>();
                for(Purchase purchase : list){
                    if(purchase.isAcknowledged() && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                        res.add(purchase);
                    }
                    if (!purchase.isAcknowledged() && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
                        handlePurchase(purchase);
                    }
                }
                callback.onPurchaseComplete(res, billingResult);
            }
        });
    }



}
