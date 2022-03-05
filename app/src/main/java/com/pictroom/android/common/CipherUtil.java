package com.pictroom.android.common;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Signature related tools.
 *
 * @since 2019/12/9
 */
public class CipherUtil {
    private static final String TAG = "CipherUtil";

    // The SHA256WithRSA algorithm.
    private static final String SIGN_ALGORITHMS = "SHA256WithRSA";

    // The Iap public key of this App.
    private static final String PUBLIC_KEY = "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAvGcZWI8yDLkeCSpETPuhaVbhrMOBVUOs9Wx+lbGuPsewQwmPjf6izaLIce2wJ7ZEkQ02o+PORePaPeVMQy8TDHi73Q4akHBN1HQk1em+AVvuGGdFredy3SaiLzbL91qmfyYd/Eb/uwOcEB1XyEsI76oFlDmLahARtnyKMn1pQBhp+H561fIgQFE/FzaYdeuKpdg6NnKBsAXVrC/DzksDT38tJM2q4u0BLl3DFojzz4eK35rKRgoMHMrFvII1oM8bpK4B5OZVaXRKJ593qM1WIdbqibvFI3vq/PJyZzTCLK0xd0wSh28l/cvylJ3rFxfo7OqXrBjl76Evn5pmWhR1sI6SgcqUCR06ZhBh49fWAi7JKnzLp1T6S5e9b9GsTHD4Kd0iLwRSE5zfr4wcvi3GGSDnfE5fx10NGtUqIyxJILZGG7gA6fwyVDjidABtMMsuLNU77wbzJdzCGzDXeIPfiIiBX1pOfJy9x982DePc5E0ux8i5p2SBpH7JZxkMQYGbAgMBAAE=";

    /**
     * The method to check the signature for the data returned from the interface.
     *
     * @param content Unsigned data.
     * @param sign The signature for content.
     * @param publicKey The public of the application.
     * @return boolean
     */
    public static boolean doCheck(String content, String sign, String publicKey) {
        if (TextUtils.isEmpty(publicKey)) {
            Log.e(TAG, "publicKey is null");
            return false;
        }

        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(sign)) {
            Log.e(TAG, "data is error");
            return false;
        }

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode(publicKey, Base64.DEFAULT);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

            signature.initVerify(pubKey);
            signature.update(content.getBytes("utf-8"));

            boolean bverify = signature.verify(Base64.decode(sign, Base64.DEFAULT));
            return bverify;

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "doCheck NoSuchAlgorithmException" + e);
        } catch (InvalidKeySpecException e) {
            Log.e(TAG, "doCheck InvalidKeySpecException" + e);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "doCheck InvalidKeyException" + e);
        } catch (SignatureException e) {
            Log.e(TAG, "doCheck SignatureException" + e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "doCheck UnsupportedEncodingException" + e);
        }
        return false;
    }

    /**
     * Get the publicKey of the application.
     * During the encoding process, avoid storing the public key in clear text.
     *
     * @return publickey
     */
    public static String getPublicKey(){
        return PUBLIC_KEY;
    }

}
