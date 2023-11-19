package com.release.perfectapplock;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;

import java.util.List;

public abstract class CheckPurchaseCallback{

    void onBillingConnected(){}

    void onSuccess(Purchase purchase){}
    void onFailure(){}

    void onSkuLoadComplete(List<SkuDetails> skuDetailsList, BillingResult billingResult){}

    void onPurchaseComplete(List<Purchase> purchases, BillingResult billingResult){}
}
