package com.pictroom.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.agconnect.applinking.AGConnectAppLinking;
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
import com.pictroom.android.iap.ProductListAdapter;
import com.pictroom.android.iap.CipherUtil;
import com.pictroom.android.iap.Constants;
import com.pictroom.android.iap.DeliveryUtils;
import com.pictroom.android.iap.ExceptionHandle;
import com.pictroom.android.iap.IapApiCallback;
import com.pictroom.android.iap.IapRequestHelper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsumptionActivity extends Activity {
    private String TAG = "ConsumptionActivity";
    private TextView countTextView;

    // ListView for displaying consumables.
    private ListView consumableProductsListview;

    // The list of products to be purchased.
    private List<ProductInfo> consumableProducts = new ArrayList<ProductInfo>();

    // The product ID array of products to be purchased.
    private static final String[] CONSUMABLES = new String[]{"testConsumable1", "testConsumable2",
    "testConsumable3","testConsumable4"};

    // The Adapter for consumableProductsListview.
    private ProductListAdapter adapter;

    // Use this IapClient instance to call the APIs of IAP.
    private IapClient mClient;

    private Boolean appLinkReceived = false;
    private Boolean iapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);
        mClient = Iap.getIapClient(this);
        initView();
        // To check if there exists consumable products that a user has purchased but has not been delivered.
        queryPurchases(null);

        initApplinking();
    }

    /**
     * Initialize the UI.
     */
    private void initView() {
        countTextView = (TextView) findViewById(R.id.gems_count);
        countTextView.setText(String.valueOf(DeliveryUtils.getCountOfGems(this)));
        consumableProductsListview = (ListView) findViewById(R.id.consumable_product_list1);
        consumableProductsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                buy(position);

            }
        });
        queryProducts();
    }

    /**
     * Obtains product details of products and show the products.
     */
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
                    Log.d(TAG, "iap products >> "+ consumableProducts.size());
                    iapReady = true;
                    startPurchase();
                }
                showProducts();
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "obtainProductInfo: " + e.getMessage());
                ExceptionHandle.handle(com.pictroom.android.ConsumptionActivity.this, e);
                showProducts();
            }
        });
    }

    /**
     * Show products on the page.
     */
    private void showProducts() {
        adapter = new ProductListAdapter(com.pictroom.android.ConsumptionActivity.this, consumableProducts);
        consumableProductsListview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * Call the obtainOwnedPurchases API to obtain the data about consumable products that the user has purchased but has not been delivered.
     */
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
                ExceptionHandle.handle(com.pictroom.android.ConsumptionActivity.this, e);
            }
        });

    }

    /**
     * Deliver the product.
     *
     * @param inAppPurchaseDataStr Includes the purchase details.
     * @param inAppPurchaseDataSignature The signature String of inAppPurchaseDataStr.
     */
    private void deliverProduct(final String inAppPurchaseDataStr, final String inAppPurchaseDataSignature) {
        // Check whether the signature of the purchase data is valid.
        Log.d(TAG, "deliverProduct");
        if (CipherUtil.doCheck(inAppPurchaseDataStr, inAppPurchaseDataSignature, CipherUtil.getPublicKey())) {
            try {
                InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseDataStr);
                if (inAppPurchaseDataBean.getPurchaseState() != InAppPurchaseData.PurchaseState.PURCHASED) {
                    return;
                }
                String purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
                String productId = inAppPurchaseDataBean.getProductId();
                if (DeliveryUtils.isDelivered(com.pictroom.android.ConsumptionActivity.this, purchaseToken)) {
                    Toast.makeText(this, productId + " has been delivered", Toast.LENGTH_SHORT).show();
                    IapRequestHelper.consumeOwnedPurchase(mClient, purchaseToken);
                } else {
                    if (DeliveryUtils.deliverProduct(this, productId, purchaseToken)) {
                        Log.i(TAG, "delivery success");
                        Toast.makeText(this, productId + " delivery success", Toast.LENGTH_SHORT).show();
                        updateNumberOfGems();
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

    /**
     * Update the number of gems on the page.
     */
    private void updateNumberOfGems() {
        // Update the number of gems.
        String countOfGems = String.valueOf(DeliveryUtils.getCountOfGems(com.pictroom.android.ConsumptionActivity.this));
        countTextView.setText(countOfGems);
    }

    /**
     * Initiate a purchase.
     *
     * @param index Item to be purchased.
     */
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
                IapRequestHelper.startResolutionForResult(com.pictroom.android.ConsumptionActivity.this, status, Constants.REQ_CODE_BUY);
            }

            @Override
            public void onFail(Exception e) {
                int errorCode = ExceptionHandle.handle(com.pictroom.android.ConsumptionActivity.this, e);
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
            String url="";

            PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
            switch(purchaseResultInfo.getReturnCode()) {
                case OrderStatusCode.ORDER_STATE_CANCEL:
                    Toast.makeText(this, "Order has been canceled!", Toast.LENGTH_SHORT).show();
                    url = "https://pictroom.com/purchase/failed";
                    break;
                case OrderStatusCode.ORDER_STATE_FAILED:
                    Toast.makeText(this, "Order has failed!", Toast.LENGTH_SHORT).show();
                    url = "https://pictroom.com/purchase/failed";
                    break;
                case OrderStatusCode.ORDER_PRODUCT_OWNED:
                    //queryPurchases(null);
                    url = "https://pictroom.com/purchase/owned";
                    Toast.makeText(this, "Product already owned", Toast.LENGTH_LONG).show();
                    return;
//                    break;
                case OrderStatusCode.ORDER_STATE_SUCCESS:
                    deliverProduct(purchaseResultInfo.getInAppPurchaseData(), purchaseResultInfo.getInAppDataSignature());
                    url = "https://pictroom.com/purchase/success";
                    break;
                default:
                    break;
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.setData(Uri.parse(url));
            startActivity(intent);

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

                            if (deepLink != null) {
                                String path = deepLink.getLastPathSegment();
                                Log.d(TAG, "deeplink >> "+ deepLink);
                                Log.d(TAG, "deepLink.getLastPathSegment() >> "+ deepLink.getLastPathSegment());

                                for (String name : deepLink.getQueryParameterNames()) {
                                    String queryParam = deepLink.getQueryParameter(name);
                                    Log.d(TAG, "deepLink.getQueryParameter(name) >> " + queryParam);

                                    if(name.contains("id")){
                                        appLinkReceived = true;
                                        startPurchase();
                                    }
                                }
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            Log.d(TAG, "app linking >> failure >> ", e);
                        });
    }

    public void startPurchase(){
        if(iapReady && appLinkReceived){
            buy(0);
        }
    }

}
