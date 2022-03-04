package com.pictroom.android.common;

/**
 * Used to callback the result from iap api.
 *
 * @since 2019/12/9
 */
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
