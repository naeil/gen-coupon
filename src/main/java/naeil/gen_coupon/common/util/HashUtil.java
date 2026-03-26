package naeil.gen_coupon.common.util;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;

public class HashUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * HMAC-SHA256를 이용한 결정론적 해싱 (Blind Index)
     * @param plainText 평문 (전화번호 등)
     * @param key SecretKey
     * @return Base64 인코딩된 해시값
     */
    public static String hash(String plainText, SecretKey key) {
        if (plainText == null) return null;

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(key);
            byte[] hashBytes = mac.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Hash failed", e);
        }
    }
}
