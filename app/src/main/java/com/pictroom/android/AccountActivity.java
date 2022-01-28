/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.pictroom.android;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.huawei.agconnect.applinking.AGConnectAppLinking;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.pictroom.android.logger.Constant;
import com.pictroom.android.logger.Log;
import com.pictroom.android.logger.LogFragment;
import com.pictroom.android.logger.LoggerActivity;

/**
 * Codelab
 * Demonstration of HuaweiId
 */
public class AccountActivity extends LoggerActivity implements OnClickListener {

    //Log tag
    public static final String TAG = "HuaweiIdActivity";
    private AccountAuthService mAuthManager;
    private AccountAuthParams mAuthParam;

    private Boolean appLinkReceived = false;
    private Boolean iapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_account);
        findViewById(R.id.account_signin).setOnClickListener(this);
        findViewById(R.id.account_signout).setOnClickListener(this);
        findViewById(R.id.account_signInCode).setOnClickListener(this);
        findViewById(R.id.account_silent_signin).setOnClickListener(this);
        findViewById(R.id.cancel_authorization).setOnClickListener(this);
        findViewById(R.id.open_extra_headers).setOnClickListener(this);
        //sample log Please ignore
        addLogFragment();

        initApplinking();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Codelab Code
     * Pull up the authorization interface by getSignInIntent
     */
    private void signIn() {
        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
                .setAccessToken()
                .createParams();
        mAuthManager = AccountAuthManager.getService(AccountActivity.this, mAuthParam);
        startActivityForResult(mAuthManager.getSignInIntent(), Constant.REQUEST_SIGN_IN_LOGIN);
    }

    private void signInCode() {
        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setProfile()
                .setAuthorizationCode()
                .createParams();
        mAuthManager = AccountAuthManager.getService(AccountActivity.this, mAuthParam);
        startActivityForResult(mAuthManager.getSignInIntent(), Constant.REQUEST_SIGN_IN_LOGIN_CODE);
    }

    /**
     * Codelab Code
     * sign Out by signOut
     */
    private void signOut() {
        Task<Void> signOutTask = mAuthManager.signOut();
        signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "signOut Success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "signOut fail");
            }
        });
    }

    /**
     * Codelab Code
     * Silent SignIn by silentSignIn
     */
    private void silentSignIn() {
        Task<AuthAccount> task = mAuthManager.silentSignIn();
        task.addOnSuccessListener(new OnSuccessListener<AuthAccount>() {
            @Override
            public void onSuccess(AuthAccount authAccount) {
                Log.i(TAG, "silentSignIn success");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                //if Failed use getSignInIntent
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    signIn();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.account_signin:
                signIn();
                break;
            case R.id.account_signout:
                signOut();
                break;
            case R.id.account_signInCode:
                signInCode();
                break;
            case R.id.account_silent_signin:
                silentSignIn();
                break;
            case R.id.cancel_authorization:
                cancelAuthorization();
                break;
            case R.id.open_extra_headers:
                openExtraHeaders();
                break;
            default:
                break;
        }
    }

    private void openExtraHeaders(){
        Log.d(TAG, "openExtraHeaders");
        String url = "https://pictroom.com/";
        CustomTabColorSchemeParams params = new CustomTabColorSchemeParams.Builder()
                .setNavigationBarColor(ContextCompat.getColor(this,R.color.colorNav))
                .setToolbarColor(ContextCompat.getColor(this,R.color.colorPrimary))
                .setSecondaryToolbarColor(ContextCompat.getColor(this,R.color.backgroundColor))
                .build();

        CustomTabsIntent customTabsIntent = new CustomTabsIntent
                .Builder()
//                    .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_LIGHT, params)
                .setDefaultColorSchemeParams(params)
                .build();

        Bundle headers = new Bundle();
        headers.putString("header1", "PWA CCT");
        customTabsIntent.intent.putExtra(Browser.EXTRA_HEADERS, headers);

        CustomTabActivityHelper.openCustomTab(
                this, customTabsIntent, Uri.parse(url), new WebviewFallback());
    }

    private void cancelAuthorization() {
        mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setProfile()
                .setAuthorizationCode()
                .createParams();
        mAuthManager = AccountAuthManager.getService(AccountActivity.this, mAuthParam);
        Task<Void> task = mAuthManager.cancelAuthorization();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "cancelAuthorization success");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.i(TAG, "cancelAuthorization failure：" + e.getClass().getSimpleName());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_SIGN_IN_LOGIN) {
            //login success
            //get user message by parseAuthResultFromIntent
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                AuthAccount authAccount = authAccountTask.getResult();
                Log.i(TAG, authAccount.getDisplayName() + " signIn success ");
                Log.i(TAG, "AccessToken: " + authAccount.getAccessToken());
                Log.i(TAG, "authAccount.toString(): " + authAccount.toString());
            } else {
                Log.i(TAG, "signIn failed: " + ((ApiException) authAccountTask.getException()).getStatusCode());
            }
        }
        if (requestCode == Constant.REQUEST_SIGN_IN_LOGIN_CODE) {
            //login success
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                AuthAccount authAccount = authAccountTask.getResult();
                Log.i(TAG, "signIn get code success.");
                Log.i(TAG, "ServerAuthCode: " + authAccount.getAuthorizationCode());

                /**** english doc:For security reasons, the operation of changing the code to an AT must be performed on your server. The code is only an example and cannot be run. ****/
                /**********************************************************************************************/
            } else {
                Log.i(TAG, "signIn get code failed: " + ((ApiException) authAccountTask.getException()).getStatusCode());
            }
        }
    }

    /**
     * sample log Please ignore
     */
    private void addLogFragment() {
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        final LogFragment fragment = new LogFragment();
        transaction.replace(R.id.framelog, fragment);
        transaction.commit();
    }

    public void initApplinking(){
        android.util.Log.d(TAG, "init app linking");
        AGConnectAppLinking.getInstance()
                .getAppLinking(this)
                .addOnSuccessListener(
                        resolvedLinkData -> {
                            android.util.Log.d(TAG, "app linking >> on success");
                            Uri deepLink = null;
                            if (resolvedLinkData != null) {
                                deepLink = resolvedLinkData.getDeepLink();
                            }

//                            TextView textView = findViewById(R.id.deepLink);
//                            textView.setText(deepLink != null ? deepLink.toString() : "");

                            if (deepLink != null) {
                                String path = deepLink.getLastPathSegment();
                                android.util.Log.d(TAG, "deeplink >> "+ deepLink);
                                android.util.Log.d(TAG, "deepLink.getLastPathSegment() >> "+ deepLink.getLastPathSegment());

                                for (String name : deepLink.getQueryParameterNames()) {
                                    String queryParam = deepLink.getQueryParameter(name);
                                    android.util.Log.d(TAG, "deepLink.getQueryParameter(name) >> " + queryParam);

                                    if(name.contains("id")){
                                        appLinkReceived = true;
                                        //startPurchase();
                                    }
                                }
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            android.util.Log.d(TAG, "app linking >> failure >> ", e);
                        });
    }

}
