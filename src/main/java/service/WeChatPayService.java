package service;


import config.SignType;
import model.request.PayRequest;
import model.request.RefundRequest;
import model.response.PayResponse;
import model.response.RefundResponse;

import java.util.Map;

/**
 * @author Curtain
 * @date 2018/8/30 15:43
 */
public interface WeChatPayService {

    /**
     * 公众号h5支付
     * @param request
     * @return
     */
    PayResponse pay(PayRequest request);

    /**
     * wap h5支付
     * @param request
     * @return
     */
    PayResponse h5pay(PayRequest request);

    /**
     * App支付
     * @param request
     * @return
     */
    PayResponse appPay(PayRequest request);


    /**
     * 验证支付结果. 包括同步和异步.
     *
     * @param toBeVerifiedParamMap 待验证的支付结果参数.
     * @param signType             签名方式.
     * @param sign                 签名.
     * @return 验证结果.
     */
    boolean verify(Map<String, String> toBeVerifiedParamMap, SignType signType, String sign);

    /**
     * 同步回调
     * @param notifyData
     * @return
     */
    PayResponse syncNotify(String notifyData);

    /**
     * 异步回调
     * @param notifyData
     * @return
     */
    PayResponse asyncNotify(String notifyData);

    /**
     * 退款
     * @param request
     * @return
     */
    RefundResponse refund(RefundRequest request);

}
