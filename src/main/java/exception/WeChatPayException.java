package exception;


import enums.WeChatPayResultEnum;

/**
 * @author Curtain
 * @date 2018/8/30 14:56
 */
public class WeChatPayException extends RuntimeException {
    private Integer code;

    public WeChatPayException(WeChatPayResultEnum resultEnum) {
        super(resultEnum.getMsg());
        code = resultEnum.getCode();
    }

    public Integer getCode() {
        return code;
    }
}
