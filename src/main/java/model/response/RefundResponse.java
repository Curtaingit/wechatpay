package model.response;

import lombok.Data;

/**
 * 退款时响应参数
 * @author Curtain
 * @date 2018/8/30 15:06
 */
@Data
public class RefundResponse {
    /**
     * 订单号.
     */
    private String orderId;

    /**
     * 订单金额.
     */
    private Long orderAmount;

    /**
     * 第三方支付流水号.
     */
    private String outTradeNo;

    /**
     * 退款号.
     */
    private String refundId;

    /**
     * 第三方退款流水号.
     */
    private String outRefundNo;

}
