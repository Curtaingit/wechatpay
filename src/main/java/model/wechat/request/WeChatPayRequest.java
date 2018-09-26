package model.wechat.request;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * 支付请求参数
 * @author Curtain
 * @date 2018/8/30 15:16
 */
@XStreamAlias("xml")
@Data
public class WeChatPayRequest {

    private String appid;

    @XStreamAlias("mch_id")
    private String mchId;

    @XStreamAlias("nonce_str")
    private String nonceStr;

    private String sign;

    private String attach;

    private String body;

    private String detail;


    @XStreamAlias("notify_url")
    private String notifyUrl;

    private String openid;

    @XStreamAlias("out_trade_no")
    private String outTradeNo;

    @XStreamAlias("spbill_create_ip")
    private String spbillCreateIp;

    @XStreamAlias("total_fee")
    private Long totalFee;

    @XStreamAlias("trade_type")
    private String tradeType;

    /**
     * 统一下单接口
     */
    @XStreamAlias("prepay_id")
    private String prepayId;
    /**
     * 信息
     */
    @XStreamAlias("scene_info")
    private String sceneInfo;
}
