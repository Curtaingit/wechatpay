package model.response;

import lombok.Data;
import sun.dc.pr.PRError;

/**
 * @author Curtain
 * @date 2018/9/27 14:55
 * 查询时响应参数
 */

@Data
public class QueryResponse {

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 用户openid
     */
    private String openid;

    /**
     * 总金额
     */
    private Integer total_fee;

    /**
     * 现金支付金额
     */
    private String cashFee;

    /**
     * 代金券（微信红包）金额
     */
    private String couponFee;

    /**
     * 标价币种
     */
    private String feeType;

    /**
     * 付款银行
     */
    private String bankType;

    /**
     * 交易状态
     * SUCCESS—支付成功
     * REFUND—转入退款
     * NOTPAY—未支付
     * CLOSED—已关闭
     * REVOKED—已撤销（刷卡支付）
     * USERPAYING--用户支付中
     * PAYERROR--支付失败(其他原因，如银行返回失败)
     */
    private String tradeState;

}
