package model.request;

import enums.WeChatPayTypeEnum;
import lombok.Data;


/**
 * 支付时请求参数
 * @author Curtain
 * @date 2018/8/30 15:05
 */
@Data
public class PayRequest {
    /**
     * 支付方式.
     */
    private WeChatPayTypeEnum payTypeEnum;

    /**
     * 订单号.
     */
    private String orderId;

    /**
     * 订单金额.
     */
    private Long orderAmount;

    /**
     * 订单名字.
     */
    private String orderName;

    /**
     * 微信openid, 仅微信支付时需要
     */
    private String openid;


    /**
     * 信息
     */
    private String sceneInfo;

    /**
     * ip
     */
    private String spbillCreateIp;
}
