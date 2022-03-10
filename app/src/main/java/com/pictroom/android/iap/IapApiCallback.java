package com.pictroom.android.iap;

public interface IapApiCallback<T> {

    /**
     * The request is successful.
     *
     * @param result The result of a successful response.
     */
    void onSuccess(T result);

    /**
     * Callback fail.
     *
     * @param e An Exception from IAPSDK.
     */
    void onFail(Exception e);
}
