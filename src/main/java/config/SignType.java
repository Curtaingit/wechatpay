package config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Curtain
 * @date 2018/8/30 15:45
 */
public enum SignType {
    MD5, RSA, RSA2;

    private static Map<String, SignType> values = new HashMap<>();

    static {
        for (SignType value : values()) {
            values.put(value.name(), value);
        }
    }

    public static SignType from(String strValue) {
        SignType value = values.get(strValue);
        if (value != null) {
            return value;
        } else {
            return null;
        }
    }

    public static SignType from(String strValue, SignType defaultValue) {
        SignType value = from(strValue);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @Override
    public String toString() {
        return this.name();
    }

}
