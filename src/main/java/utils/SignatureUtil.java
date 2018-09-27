package utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Curtain
 * @date 2018/8/30 15:49
 */
public class SignatureUtil {
    /**
     * 签名
     * @param params
     * @param signKey
     * @return
     */
    public static String sign(Map<String, String> params, String signKey) {
        SortedMap<String, String> sortedMap = new TreeMap<>(params);

        StringBuilder toSign = new StringBuilder();
        for (String key : sortedMap.keySet()) {
            String value = params.get(key);
            if (StringUtils.isNotEmpty(value) && !"sign".equals(key) && !"key".equals(key)) {
                toSign.append(key).append("=").append(value).append("&");
            }
        }

        //加上商户秘钥  注册签名串
        toSign.append("key=").append(signKey);
        return DigestUtils.md5Hex(toSign.toString()).toUpperCase();
    }

    /**
     * 校验签名
     * @param params
     * @param signKey
     * @return
     */
    public static Boolean verify(Map<String, String> params, String signKey) {
        String sign = sign(params, signKey);
        return sign.equals(params.get("sign"));
    }
}
