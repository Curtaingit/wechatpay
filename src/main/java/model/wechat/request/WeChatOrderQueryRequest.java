package model.wechat.request;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * @author Curtain
 * @date 2018/9/27 14:45
 * 查询订单请求参数
 */

@XStreamAlias("xml")
@Data
public class WeChatOrderQueryRequest {
    private String appid;

    @XStreamAlias("mch_id")
    private String mchId;

    @XStreamAlias("out_trade_no")
    private String outTradeNo;

    @XStreamAlias("nonce_str")
    private String nonceStr;

    private String sign;

    @XStreamAlias("sign_type")
    private String signType;

}
