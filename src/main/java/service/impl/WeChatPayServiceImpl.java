package service.impl;

import config.SignType;
import config.WeChatPayConfig;
import constants.WeChatPayConstants;
import lombok.extern.slf4j.Slf4j;
import model.WeChatPayApi;
import model.request.PayRequest;
import model.request.RefundRequest;
import model.response.PayResponse;
import model.response.RefundResponse;
import model.wechat.request.WeChatPayRefundRequest;
import model.wechat.request.WeChatPayRequest;
import model.wechat.response.WeChatPayAsyncResponse;
import model.wechat.response.WeChatPayRefundResponse;
import model.wechat.response.WeChatPaySyncResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import service.WeChatPayService;
import utils.RandomUtil;
import utils.SignatureUtil;
import utils.XmlUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Curtain
 * @date 2018/8/30 15:48
 */

@Slf4j
public class WeChatPayServiceImpl implements WeChatPayService {

    private WeChatPayConfig weChatPayConfig;

    public void setWeChatPayConfig(WeChatPayConfig weChatPayConfig) {
        this.weChatPayConfig = weChatPayConfig;
    }

    @Override
    public PayResponse pay(PayRequest request) {
        WeChatPayRequest weChatPayRequest = new WeChatPayRequest();
        weChatPayRequest.setOutTradeNo(request.getOrderId());
        weChatPayRequest.setTotalFee(request.getOrderAmount());
        weChatPayRequest.setBody(request.getOrderName());
        weChatPayRequest.setOpenid(request.getOpenid());

        weChatPayRequest.setTradeType("JSAPI");
        weChatPayRequest.setSpbillCreateIp("8.8.8.8");

        WeChatPaySyncResponse response = unifiedorder(weChatPayRequest);

        return buildH5PayResponse(response);
    }

    @Override
    public PayResponse appPay(PayRequest request) {
        WeChatPayRequest weChatPayRequest = new WeChatPayRequest();
        weChatPayRequest.setOutTradeNo(request.getOrderId());
        weChatPayRequest.setTotalFee(request.getOrderAmount());
        weChatPayRequest.setBody(request.getOrderName());
        weChatPayRequest.setSpbillCreateIp(request.getSpbillCreateIp());

        weChatPayRequest.setTradeType("APP");

        WeChatPaySyncResponse response = unifiedorder(weChatPayRequest);

        return buildH5PayResponse(response);
    }

    @Override
    public PayResponse h5pay(PayRequest request) {

        WeChatPayRequest weChatPayRequest = new WeChatPayRequest();
        weChatPayRequest.setOutTradeNo(request.getOrderId());
        weChatPayRequest.setTotalFee(request.getOrderAmount());
        weChatPayRequest.setBody(request.getOrderName());
//        weChatPayRequest.setSceneInfo(request.getSceneInfo());
        weChatPayRequest.setSpbillCreateIp(request.getSpbillCreateIp());

        weChatPayRequest.setTradeType("MWEB");

        WeChatPaySyncResponse response = unifiedorder(weChatPayRequest);

        return buildH5PayResponse(response);
    }

    /*统一下单*/
    private WeChatPaySyncResponse unifiedorder(WeChatPayRequest weChatPayRequest) {

        //配置秘钥信息
        weChatPayRequest.setAppid(weChatPayConfig.getAppId());
        weChatPayRequest.setMchId(weChatPayConfig.getMchId());
        weChatPayRequest.setNotifyUrl(weChatPayConfig.getNotifyUrl());
        weChatPayRequest.setNonceStr(RandomUtil.getRandomStr());

        //签名
        weChatPayRequest.setSign(SignatureUtil.sign(buildMap(weChatPayRequest), weChatPayConfig.getMchKey()));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WeChatPayConstants.WE_CHAT_PAY_BASE_URL)
                //xml转化器   SimpleXmlConverterFactory 需要另外添加依赖  不在retrofit2中
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
        String xml = XmlUtil.toXMl(weChatPayRequest);
        System.out.println(xml);
        RequestBody body = RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), xml);
        Call<WeChatPaySyncResponse> call = retrofit.create(WeChatPayApi.class).unifiedOrder(body);
        Response<WeChatPaySyncResponse> retrofitResponse = null;
        try {
            retrofitResponse = call.execute();
            System.out.println(retrofitResponse.body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!retrofitResponse.isSuccessful()) {
            throw new RuntimeException("【微信统一支付】发起支付, 网络异常");
        }
        WeChatPaySyncResponse response = retrofitResponse.body();
//        log.info("【微信统一支付】response={}", JsonUtil.toJson(response));

        if (!response.getReturnCode().equals("SUCCESS")) {
            throw new RuntimeException("【微信统一支付】发起支付, returnCode != SUCCESS, returnMsg = " + response.getReturnMsg());
        }
        if (!response.getResultCode().equals("SUCCESS")) {
            throw new RuntimeException("【微信统一支付】发起支付, resultCode != SUCCESS, err_code = " + response.getErrCode() + " err_code_des=" + response.getErrCodeDes());
        }
        return response;
    }

    @Override
    public boolean verify(Map<String, String> toBeVerifiedParamMap, SignType signType, String sign) {
        return false;
    }

    @Override
    public PayResponse syncNotify(String notifyData) {
        return null;
    }

    @Override
    public PayResponse asyncNotify(String notifyData) {
        //xml解析为对象
        WeChatPayAsyncResponse asyncResponse = (WeChatPayAsyncResponse) XmlUtil.fromXML(notifyData, WeChatPayAsyncResponse.class);

        //签名校验
        if (!SignatureUtil.verify(buildMap(asyncResponse), weChatPayConfig.getMchKey())) {
            log.error("【微信支付异步通知】签名验证失败, response={}", asyncResponse);
            throw new RuntimeException("【微信支付异步通知】签名验证失败");
        }

        if (!asyncResponse.getReturnCode().equals(WeChatPayConstants.SUCCESS)) {
            throw new RuntimeException("【微信支付异步通知】发起支付, returnCode != SUCCESS, returnMsg = " + asyncResponse.getReturnMsg());
        }
        //该订单已支付直接返回
        if (!asyncResponse.getResultCode().equals(WeChatPayConstants.SUCCESS)
                && "ORDERPAID".equals(asyncResponse.getErrCode())) {
            return buildPayResponse(asyncResponse);
        }

        if (!asyncResponse.getResultCode().equals(WeChatPayConstants.SUCCESS)) {
            throw new RuntimeException("【微信支付异步通知】发起支付, resultCode != SUCCESS, err_code = " + asyncResponse.getErrCode() + " err_code_des=" + asyncResponse.getErrCodeDes());
        }

        return buildPayResponse(asyncResponse);
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        WeChatPayRefundRequest weChatPayRefundRequest = new WeChatPayRefundRequest();
        weChatPayRefundRequest.setOutTradeNo(request.getOrderId());
        weChatPayRefundRequest.setOutRefundNo(request.getOrderId());
        weChatPayRefundRequest.setTotalFee(request.getOrderAmount().intValue());
        weChatPayRefundRequest.setRefundFee(request.getOrderAmount().intValue());

        weChatPayRefundRequest.setAppid(weChatPayConfig.getAppId());
        weChatPayRefundRequest.setMchId(weChatPayConfig.getMchId());
        weChatPayRefundRequest.setNonceStr(RandomUtil.getRandomStr());
        weChatPayRefundRequest.setSign(SignatureUtil.sign(buildMap(weChatPayRefundRequest), weChatPayConfig.getMchKey()));

        //初始化证书
        if (weChatPayConfig.getSslContext() == null) {
            weChatPayConfig.initSSLContext();
        }
        OkHttpClient.Builder okHttpClient = new OkHttpClient()
                .newBuilder()
                .sslSocketFactory(weChatPayConfig.getSslContext().getSocketFactory());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WeChatPayConstants.WE_CHAT_PAY_BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .client(okHttpClient.build())
                .build();

        String xml = XmlUtil.toXMl(weChatPayRefundRequest);
        RequestBody body = RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), xml);
        Call<WeChatPayRefundResponse> call = retrofit.create(WeChatPayApi.class).refund(body);
        Response<WeChatPayRefundResponse> retrofitResponse = null;

        try {
            retrofitResponse = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!retrofitResponse.isSuccessful()) {
            throw new RuntimeException("【微信退款】发起退款, 网络异常");
        }
        WeChatPayRefundResponse response = retrofitResponse.body();

        if (!response.getReturnCode().equals(WeChatPayConstants.SUCCESS)) {
            throw new RuntimeException("【微信退款】发起退款, returnCode != SUCCESS, returnMsg = " + response.getReturnMsg());
        }
        if (!response.getResultCode().equals(WeChatPayConstants.SUCCESS)) {
            throw new RuntimeException("【微信退款】发起退款, resultCode != SUCCESS, err_code = " + response.getErrCode() + " err_code_des=" + response.getErrCodeDes());
        }

        return buildRefundResponse(response);
    }


    /**
     * 构造map
     *
     * @param weChatPayRequest
     * @return
     */
    private Map<String, String> buildMap(WeChatPayRequest weChatPayRequest) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatPayRequest.getAppid());
        map.put("mch_id", weChatPayRequest.getMchId());
        map.put("nonce_str", weChatPayRequest.getNonceStr());
        map.put("sign", weChatPayRequest.getSign());
        map.put("body", weChatPayRequest.getBody());
        map.put("notify_url", weChatPayRequest.getNotifyUrl());
        map.put("out_trade_no", weChatPayRequest.getOutTradeNo());
        map.put("spbill_create_ip", weChatPayRequest.getSpbillCreateIp());
        map.put("total_fee", String.valueOf(weChatPayRequest.getTotalFee()));
        map.put("trade_type", weChatPayRequest.getTradeType());
        map.put("scene_info", weChatPayRequest.getSceneInfo());

        return map;
    }

    private Map<String, String> buildMap(WeChatPayAsyncResponse response) {
        Map<String, String> map = new HashMap<>(64);
        map.put("return_code", response.getReturnCode());
        map.put("return_msg", response.getReturnMsg());
        map.put("appid", response.getAppid());
        map.put("mch_id", response.getMchId());
        map.put("device_info", response.getDeviceInfo());
        map.put("nonce_str", response.getNonceStr());
        map.put("sign", response.getSign());
        map.put("settlement_total_fee", String.valueOf(response.getSettlementTotalFee()));
        map.put("result_code", response.getResultCode());
        map.put("err_code", response.getErrCode());
        map.put("err_code_des", response.getErrCodeDes());
        map.put("openid", response.getOpenid());
        map.put("is_subscribe", response.getIsSubscribe());
        map.put("trade_type", response.getTradeType());
        map.put("bank_type", response.getBankType());
        map.put("total_fee", String.valueOf(response.getTotalFee()));
        map.put("fee_type", response.getFeeType());
        map.put("cash_fee", String.valueOf(response.getCashFee()));
        map.put("cash_fee_type", response.getCashFeeType());
        map.put("coupon_fee", String.valueOf(response.getCouponFee()));
        map.put("coupon_id_0", response.getCouponId0());
        map.put("coupon_fee_0", String.valueOf(response.getCouponFee0()));
        map.put("coupon_type_0",response.getCouponType0());
        map.put("coupon_count", String.valueOf(response.getCouponCount()));
        map.put("transaction_id", response.getTransactionId());
        map.put("out_trade_no", response.getOutTradeNo());
        map.put("attach", response.getAttach());
        map.put("time_end", response.getTimeEnd());
        return map;
    }

    private Map<String, String> buildMap(WeChatPayRefundRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", request.getAppid());
        map.put("mch_id", request.getMchId());
        map.put("nonce_str", request.getNonceStr());
        map.put("sign", request.getSign());
        map.put("sign_type", request.getSignType());
        map.put("transaction_id", request.getTransactionId());
        map.put("out_trade_no", request.getOutTradeNo());
        map.put("out_refund_no", request.getOutRefundNo());
        map.put("total_fee", String.valueOf(request.getTotalFee()));
        map.put("refund_fee", String.valueOf(request.getRefundFee()));
        map.put("refund_fee_type", request.getRefundFeeType());
        map.put("refund_desc", request.getRefundDesc());
        map.put("refund_account", request.getRefundAccount());
        return map;
    }

    /**
     * 返回给h5的参数
     *
     * @param response
     * @return
     */
    private PayResponse buildH5PayResponse(WeChatPaySyncResponse response) {
        //跳转的url
        PayResponse payResponse = new PayResponse();
        payResponse.setMwebUrl(response.getMwebUrl());

        return payResponse;
    }

    /**
     * 支付回调参数
     *
     * @param response
     * @return
     */
    private PayResponse buildPayResponse(WeChatPayAsyncResponse response) {
        PayResponse payResponse = new PayResponse();
        payResponse.setOrderAmount(Long.valueOf(response.getTotalFee()));
        payResponse.setOrderId(response.getOutTradeNo());
        payResponse.setOutTradeNo(response.getTransactionId());
        return payResponse;
    }


    /**
     * 退款返回参数
     *
     * @param response
     * @return
     */
    private RefundResponse buildRefundResponse(WeChatPayRefundResponse response) {
        RefundResponse refundResponse = new RefundResponse();
        refundResponse.setOrderId(response.getOutTradeNo());
        refundResponse.setOrderAmount(Long.valueOf(response.getTotalFee()));
        refundResponse.setOutTradeNo(response.getTransactionId());
        refundResponse.setRefundId(response.getOutRefundNo());
        refundResponse.setOutRefundNo(response.getRefundId());
        return refundResponse;
    }


}
