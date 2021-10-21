/*
 * Copyright 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ychen9.pwa;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.trusted.TrustedWebActivityIntentBuilder;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.androidbrowserhelper.trusted.TwaLauncher;
import com.huawei.agconnect.applinking.AGConnectAppLinking;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.AudioFocusType;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.ads.splash.SplashAdDisplayListener;
import com.huawei.hms.ads.splash.SplashView;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;
import com.ychen9.pwa.common.CipherUtil;
import com.ychen9.pwa.common.Constants;
import com.ychen9.pwa.common.DeliveryUtils;
import com.ychen9.pwa.common.ExceptionHandle;
import com.ychen9.pwa.common.IapApiCallback;
import com.ychen9.pwa.common.IapRequestHelper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class LauncherActivity
        extends com.google.androidbrowserhelper.trusted.LauncherActivity {

    private static final String TAG = LauncherActivity.class.getSimpleName();
    private View mCustomView;
    private FrameLayout customViewContainer;

    //geoloationPermissions
    private String mGeolocationOrigin;
    private GeolocationPermissions.Callback mGeolocationCallback;
    private static final int REQUEST_FINE_LOCATION=0;

    //HMS ADS
    private BannerView defaultBannerView;
    private static final int REFRESH_TIME = 30;
    private static final int AD_TIMEOUT = 5000;
    private static final int MSG_AD_TIMEOUT = 1001;
    private boolean hasPaused = false;
    private SplashView splashView;

    //    private boolean splashEnable = false;
//    private boolean bannerEnable = false;
//    private boolean enableTopBanner = false;
    private boolean splashEnable = false;
    private boolean bannerEnable = false;
    private boolean enableTopBanner = false;
    private boolean chromeClientEnable = true;

    //HMS ANALYTICS
    HiAnalyticsInstance instance;

    //HMS IAP
    private static final String[] CONSUMABLES = new String[]{"CProduct01", "CProduct02"};
    private IapClient mClient;
    private List<ProductInfo> consumableProducts = new ArrayList<ProductInfo>();

    private Handler timeoutHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (LauncherActivity.this.hasWindowFocus()) {
                jump();
            }
            return false;
        }
    });

    private SplashView.SplashAdLoadListener splashAdLoadListener = new SplashView.SplashAdLoadListener() {
        @Override
        public void onAdLoaded() {
            // Call this method when an ad is successfully loaded.
            Log.i(TAG, "SplashAdLoadListener onAdLoaded.");
//            Toast.makeText(MainActivity.this, getString(R.string.status_load_ad_success), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            // Call this method when an ad fails to be loaded.
            Log.i(TAG, "SplashAdLoadListener onAdFailedToLoad, errorCode: " + errorCode);
//            Toast.makeText(MainActivity.this, getString(R.string.status_load_ad_fail) + errorCode, Toast.LENGTH_SHORT).show();
            jump();
        }

        @Override
        public void onAdDismissed() {
            // Call this method when the ad display is complete.
            Log.i(TAG, "SplashAdLoadListener onAdDismissed.");
//            Toast.makeText(MainActivity.this, getString(R.string.status_ad_dismissed), Toast.LENGTH_SHORT).show();
            jump();
        }
    };

    private SplashAdDisplayListener adDisplayListener = new SplashAdDisplayListener() {
        @Override
        public void onAdShowed() {
            // Call this method when an ad is displayed.
            Log.i(TAG, "SplashAdDisplayListener onAdShowed.");
        }

        @Override
        public void onAdClick() {
            // Call this method when an ad is clicked.
            Log.i(TAG, "SplashAdDisplayListener onAdClick.");
        }
    };

    private AdListener adListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            // Called when an ad is loaded successfully.
//            showToast("Ad loaded.");
        }

        @Override
        public void onAdFailed(int errorCode) {
            // Called when an ad fails to be loaded.
//            showToast(String.format(Locale.ROOT, "Ad failed to load with error code %d.", errorCode));
        }

        @Override
        public void onAdOpened() {
            // Called when an ad is opened.
//            showToast(String.format("Ad opened "));
        }

        @Override
        public void onAdClicked() {
            // Called when a user taps an ad.
//            showToast("Ad clicked");
        }

        @Override
        public void onAdLeave() {
            // Called when a user has left the app.
//            showToast("Ad Leave");
        }

        @Override
        public void onAdClosed() {
            // Called when an ad is closed.
//            showToast("Ad closed");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setting an orientation crashes the app due to the transparent background on Android 8.0
        // Oreo and below. We only set the orientation on Oreo and above. This only affects the
        // splash screen and Chrome will still respect the orientation.
        // See https://github.com/GoogleChromeLabs/bubblewrap/issues/496 for details.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        HwAds.init(this);
        HiAnalyticsTools.enableLog();
        instance = HiAnalytics.getInstance(this);

        setContentView(R.layout.activity_main);
        customViewContainer = (FrameLayout) findViewById(R.id.customViewContainer);
        getToken();

        try{
            ConstraintLayout layout = new ConstraintLayout(this);
            ConstraintLayout.LayoutParams layoutParamsparent = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            layout.setLayoutParams(layoutParamsparent);
            layout.setBackgroundColor(Color.BLUE);
            layout.bringToFront();
        }catch(Exception e){
            Log.d(TAG, "error loading test layout >> "+e);
        }

        show_children(getWindow().getDecorView());
        initApplinking();
    }

    private void show_children(View v) {
        ViewGroup viewgroup=(ViewGroup)v;
        for (int i=0;i<viewgroup.getChildCount();i++) {
            View v1=viewgroup.getChildAt(i);
            if (v1 instanceof ViewGroup) show_children(v1);
            Log.d("APPNAME",v1.toString());
        }
    }

    @Override
    protected Uri getLaunchingUrl() {
        // Get the original launch Url.
        Uri uri = super.getLaunchingUrl();

        

        return uri;
    }

    private void loadDefaultBannerAd() {
        // Obtain BannerView based on the configuration in layout/activity_main.xml.
        //enableTopBanner
        if(enableTopBanner){
            defaultBannerView = findViewById(R.id.top_banner);
        }else{
            defaultBannerView = findViewById(R.id.bot_banner);
        }

        defaultBannerView.setAdListener(adListener);
        defaultBannerView.setBannerRefresh(REFRESH_TIME);

        AdParam adParam = new AdParam.Builder().build();
        defaultBannerView.loadAd(adParam);
    }

    private void loadSplashAd() {
        Log.i(TAG, "Start to load ad");

        AdParam adParam = new AdParam.Builder().build();
        splashView = findViewById(R.id.splash_ad_view);
        splashView.setAdDisplayListener(adDisplayListener);

        // Set a default app launch image.
        splashView.setSloganResId(R.drawable.default_slogan);

        // Set a logo image.
        splashView.setLogoResId(R.mipmap.ic_launcher);
        // Set logo description.
        splashView.setMediaNameResId(R.string.media_name);
        // Set the audio focus type for a video splash ad.
        splashView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE);

        splashView.load(getString(R.string.ad_id_splash), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, adParam, splashAdLoadListener);
        Log.i(TAG, "End to load ad");

        // Remove the timeout message from the message queue.
        timeoutHandler.removeMessages(MSG_AD_TIMEOUT);
        // Send a delay message to ensure that the app home screen can be displayed when the ad display times out.
        timeoutHandler.sendEmptyMessageDelayed(MSG_AD_TIMEOUT, AD_TIMEOUT);
    }

    private void jump() {
        Log.i(TAG, "jump hasPaused: " + hasPaused);
        if (!hasPaused) {
            hasPaused = true;
            Log.i(TAG, "jump into application");
            if(splashView!=null){
                splashView.destroyView();
                splashView.setVisibility(View.GONE);
            }
        }
    }

    private void getToken() {
        new Thread() {
            @Override
            public void run() {
                try {
                    // read from agconnect-services.json
//                    String appId = "103400103";
                    String appId = AGConnectServicesConfig.fromContext(LauncherActivity.this).getString("client/app_id");
                    String token = HmsInstanceId.getInstance(LauncherActivity.this).getToken(appId, "HCM");
                    Log.i(TAG, "get token:" + token);
//                    if(!TextUtils.isEmpty(token)) {
//                        sendRegTokenToServer(token);
//                    }
                } catch (ApiException e) {
                    Log.e(TAG, "get token failed, " + e);
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_FINE_LOCATION:
                boolean allow = false;
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // user has allowed this permission
                    allow = true;
                }
                if (mGeolocationCallback != null) {
                    // call back to web chrome client
                    mGeolocationCallback.invoke(mGeolocationOrigin, allow, false);
                }
                break;
        }
    }

    private void queryProducts() {
        List<String> productIds = Arrays.asList(CONSUMABLES);
        IapRequestHelper.obtainProductInfo(mClient, productIds, IapClient.PriceType.IN_APP_CONSUMABLE, new IapApiCallback<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                Log.i(TAG, "obtainProductInfo, success");
                if (result == null) {
                    return;
                }
                if (result.getProductInfoList() != null) {
                    consumableProducts = result.getProductInfoList();
                }
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainProductInfo: " + e.getMessage());
                ExceptionHandle.handle(LauncherActivity.this, e);
            }
        });
    }

    private void queryPurchases(String continuationToken) {
        final String tag = "obtainOwnedPurchases";
        IapRequestHelper.obtainOwnedPurchases(mClient, IapClient.PriceType.IN_APP_CONSUMABLE, continuationToken, new IapApiCallback<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                if (result == null) {
                    Log.e(TAG, tag + " result is null");
                    return;
                }
                Log.i(TAG, "obtainOwnedPurchases, success");
                if (result.getInAppPurchaseDataList() != null) {
                    List<String> inAppPurchaseDataList = result.getInAppPurchaseDataList();
                    List<String> inAppSignature= result.getInAppSignature();
                    for (int i = 0; i < inAppPurchaseDataList.size(); i++) {
                        final String inAppPurchaseData = inAppPurchaseDataList.get(i);
                        final String inAppPurchaseDataSignature = inAppSignature.get(i);
                        deliverProduct(inAppPurchaseData, inAppPurchaseDataSignature);
                    }
                }
                if (!TextUtils.isEmpty(result.getContinuationToken())) {
                    queryPurchases(result.getContinuationToken());
                }
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainOwnedPurchases, type=" + IapClient.PriceType.IN_APP_CONSUMABLE + ", " + e.getMessage());
                ExceptionHandle.handle(LauncherActivity.this, e);
            }
        });

    }

    private void deliverProduct(final String inAppPurchaseDataStr, final String inAppPurchaseDataSignature) {
        // Check whether the signature of the purchase data is valid.
        if (CipherUtil.doCheck(inAppPurchaseDataStr, inAppPurchaseDataSignature, CipherUtil.getPublicKey())) {
            try {
                InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseDataStr);
                if (inAppPurchaseDataBean.getPurchaseState() != InAppPurchaseData.PurchaseState.PURCHASED) {
                    return;
                }
                String purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
                String productId = inAppPurchaseDataBean.getProductId();
                if (DeliveryUtils.isDelivered(LauncherActivity.this, purchaseToken)) {
                    Toast.makeText(this, productId + " has been delivered", Toast.LENGTH_SHORT).show();
                    IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken);
                } else {
                    if (DeliveryUtils.deliverProduct(this, productId, purchaseToken)) {
                        Log.i(TAG, "delivery success");
                        Toast.makeText(this, productId + " delivery success", Toast.LENGTH_SHORT).show();
//                        updateNumberOfGems();
                        // To consume the product after successfully delivering.
                        IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken);
                    } else {
                        Log.e(TAG, productId + " delivery fail");
                        Toast.makeText(this, productId + " delivery fail", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "delivery:" + e.getMessage());
            }
        } else {
            Log.e(TAG, "delivery:" + getString(R.string.verify_signature_fail));
            Toast.makeText(this, getString(R.string.verify_signature_fail), Toast.LENGTH_SHORT).show();
        }
    }

    private void buy(int index) {
        ProductInfo productInfo = consumableProducts.get(index);
        IapRequestHelper.createPurchaseIntent(mClient, productInfo.getProductId(), IapClient.PriceType.IN_APP_CONSUMABLE, new IapApiCallback<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                if (result == null) {
                    Log.e(TAG, "result is null");
                    return;
                }
                Status status = result.getStatus();
                if (status == null) {
                    Log.e(TAG, "status is null");
                    return;
                }
                // You should pull up the page to complete the payment process.
                IapRequestHelper.startResolutionForResult(LauncherActivity.this, status, Constants.REQ_CODE_BUY);
            }

            @Override
            public void onFail(Exception e) {
                int errorCode = ExceptionHandle.handle(LauncherActivity.this, e);
                if (errorCode != ExceptionHandle.SOLVED) {
                    Log.e(TAG, "createPurchaseIntent, returnCode: " + errorCode);
                    switch (errorCode) {
                        case OrderStatusCode.ORDER_PRODUCT_OWNED:
                            queryPurchases(null);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQ_CODE_BUY) {
            if (data == null) {
                Log.e(TAG, "data is null");
                return;
            }
            // Parses payment result data.
            PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    Toast.makeText(this, "Order has been canceled!", Toast.LENGTH_SHORT).show();
                    break;
                case OrderStatusCode.ORDER_STATE_FAILED:
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    queryPurchases(null);
                    break;
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    deliverProduct(purchaseResultInfo.getInAppPurchaseData(), purchaseResultInfo.getInAppDataSignature());
                    break;
                default:
                    break;
            }
            return;
        }
    }

    public void initApplinking(){
        Log.d(TAG, "init app linking");
        AGConnectAppLinking.getInstance()
                .getAppLinking(this)
                .addOnSuccessListener(
                        resolvedLinkData -> {
                            Log.d(TAG, "app linking >> on success");
                            Uri deepLink = null;
                            if (resolvedLinkData != null) {
                                deepLink = resolvedLinkData.getDeepLink();
                            }

//                            TextView textView = findViewById(R.id.deepLink);
//                            textView.setText(deepLink != null ? deepLink.toString() : "");

                            if (deepLink != null) {
                                String path = deepLink.getLastPathSegment();
                                Log.d(TAG, "deeplink >> "+ deepLink);
                                Log.d(TAG, "deepLink.getLastPathSegment() >> "+ deepLink.getLastPathSegment());
                                if (path.contains("iap")) {
//                                    Intent intent = new Intent(getBaseContext(), DetailActivity.class);
//                                    for (String name : deepLink.getQueryParameterNames()) {
//                                        intent.putExtra(name, deepLink.getQueryParameter(name));
//                                    }
//                                    startActivity(intent);
                                    buy(0);
                                }
//                                if ("setting".equals(path)) {
//                                    Intent intent = new Intent(getBaseContext(), SettingActivity.class);
//                                    for (String name : deepLink.getQueryParameterNames()) {
//                                        intent.putExtra(name, deepLink.getQueryParameter(name));
//                                    }
//                                    startActivity(intent);
//                                }
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            Log.d(TAG, "app linking >> failure >> ", e);
                        });
    }
}
