package model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.net.URI;

/**
 * 支付时响应参数
 * @author Curtain
 * @date 2018/8/30 15:05
 */
@Data
public class PayResponse {

    private String prePayParams;

    private URI payUri;

    /** 以下字段仅在微信h5支付返回. */
    private String appId;

    private String timeStamp;

    private String nonceStr;

    @JsonProperty("package")
    private String packAge;

    private String signType;

    private String paySign;

    /** 以下字段在微信异步通知下返回. */
    private Long orderAmount;

    private String orderId;

    //第三方支付的流水号
    private String outTradeNo;

    private String mwebUrl;
}
