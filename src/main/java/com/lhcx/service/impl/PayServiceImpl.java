package com.lhcx.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lhcx.model.Order;
import com.lhcx.service.IPayService;
import com.lhcx.utils.MD5Kit;
import com.lhcx.utils.PayConfigUtils;
import com.lhcx.utils.Utils;

@Transactional(rollbackFor=Exception.class)
@Service
public class PayServiceImpl implements IPayService{
	
	@Autowired
	private HttpServletRequest request;

	@Override
	public Map<String, Object> payForWX(Order order) {
		Map<String, Object> result = new HashMap<String, Object>();
		String now_ip = Utils.getIpAddr(request);
        String nonce_str = Utils.getCharAndNum(32);
        double inputFee = order.getFee().doubleValue();
        
        inputFee =order.getFee().doubleValue() * 100;
        
        String total_fee = (int)inputFee + "";
        String body = "出行费用";
        String description = "出行费用";
        String prepay_id = null;
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("appid", PayConfigUtils.getWx_laihui_app_id());
        paraMap.put("attach", description);
        paraMap.put("body", body);
        paraMap.put("mch_id", PayConfigUtils.getWx_laihui_mch_id());
        paraMap.put("nonce_str", nonce_str);
        paraMap.put("notify_url", PayConfigUtils.getWx_pay_laihui_notify_url());
        paraMap.put("out_trade_no", order.getOrderid());
        paraMap.put("spbill_create_ip", now_ip);
        paraMap.put("total_fee", total_fee);
        paraMap.put("trade_type", "APP");
        List<String> keys = new ArrayList<>(paraMap.keySet());
        Collections.sort(keys);

        StringBuilder authInfo = new StringBuilder();
        for (int i = 0; i < keys.size() - 1; i++) {
            String value = paraMap.get(keys.get(i));
            authInfo.append(keys.get(i) + "=" + value + "&");
        }
        authInfo.append(keys.get(keys.size() - 1) + "=" + paraMap.get(keys.get(keys.size() - 1)));
        String stringA = authInfo.toString() + "&key=" + PayConfigUtils.getWx_laihui_app_secret_key();
        String sign = MD5Kit.encode(stringA).toUpperCase();
        //封装xml
        String paras = "<xml>\n" +
                "   <appid>" + PayConfigUtils.getWx_laihui_app_id() + "</appid>\n" +
                "   <attach>" + description + "</attach>\n" +
                "   <body>" + body + "</body>\n" +
                "   <mch_id>" + PayConfigUtils.getWx_laihui_mch_id() + "</mch_id>\n" +
                "   <nonce_str>" + nonce_str + "</nonce_str>\n" +
                "   <notify_url>" + PayConfigUtils.getWx_pay_laihui_notify_url() + "</notify_url>\n" +
                "   <out_trade_no>" + order.getOrderid() + "</out_trade_no>\n" +
                "   <spbill_create_ip>" + now_ip + "</spbill_create_ip>\n" +
                "   <total_fee>" + total_fee + "</total_fee>\n" +
                "   <trade_type>APP</trade_type>\n" +
                "   <sign>" + sign + "</sign>\n" +
                "</xml>";
        try {
        	String content = senPost(paras);
        	if (content != null) {
                prepay_id = Utils.readStringXml(content);
            }
        	if (prepay_id != null) {
                String current_noncestr = Utils.getCharAndNum(32);
                String current_sign = null;
                long current_timestamp = System.currentTimeMillis() / 1000;
                result.put("appid", PayConfigUtils.getWx_app_id());
                result.put("partnerid", PayConfigUtils.getWx_mch_id());
                result.put("prepayid", prepay_id);
                result.put("package", "Sign=WXPay");
                result.put("noncestr", current_noncestr);
                result.put("timestamp", current_timestamp);
                //加密算法
                String nowStringA = "appid=" + PayConfigUtils.getWx_app_id() + "&noncestr=" + current_noncestr + "&package=Sign=WXPay&partnerid=" + PayConfigUtils.getWx_mch_id() + "&prepayid=" + prepay_id + "&timestamp=" + current_timestamp + "&key=" + PayConfigUtils.getWx_app_secret_key();
                current_sign = MD5Kit.encode(nowStringA).toUpperCase();
                result.put("sign", current_sign);
            }
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}
        return result;
	}
	
	@SuppressWarnings({ "deprecation", "resource" })
	public static String senPost(String paras) throws IOException {
	        boolean is_success = true;
	        int i = 0;
	        String result = "";
	        while (is_success) {

	            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	            HttpClient httpClient = new DefaultHttpClient();
	            HttpPost post = new HttpPost(url);
	            StringEntity postingString = new StringEntity(paras, "UTF-8");// xml传递
	            post.setEntity(postingString);
	            post.setHeader("Content-type", "text/html; charset=UTF-8");
	            HttpResponse response = httpClient.execute(post);
	            result = EntityUtils.toString(response.getEntity());

	            if (result == null || result.isEmpty()) {
	                i++;
	            } else {
	                break;
	            }
	            if (i > 2) {
	                break;
	            }
	        }

	        return result;
	    }

}
