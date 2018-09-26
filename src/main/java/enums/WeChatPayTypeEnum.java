package enums;


import exception.WeChatPayException;

/**
 * @author Curtain
 * @date 2018/8/30 14:54
 */
public enum WeChatPayTypeEnum {

    WXPAY_H5("wxpay_h5", "微信公众账号支付"),
    ;

    private String code;

    private String name;

    WeChatPayTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static WeChatPayTypeEnum getByCode(String code) {
        for (WeChatPayTypeEnum bestPayTypeEnum : WeChatPayTypeEnum.values()) {
            if (bestPayTypeEnum.getCode().equals(code)) {
                return bestPayTypeEnum;
            }
        }
        throw new WeChatPayException(WeChatPayResultEnum.PAY_TYPE_ERROR);
    }
}
