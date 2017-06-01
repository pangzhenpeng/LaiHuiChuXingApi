package com.lhcx.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.lhcx.model.Order;
import com.lhcx.model.PushNotification;
import com.lhcx.service.IAlipayLogService;
import com.lhcx.service.IPushNotificationService;
import com.lhcx.utils.ConfigUtils;
import com.lhcx.utils.JpushClientUtil;
import com.lhcx.utils.MD5Kit;
import com.lhcx.utils.PayConfigUtils;
import com.lhcx.utils.XmlParse;

@Controller
public class AlipayNotifyController {

	private static Logger log = Logger.getLogger(AlipayNotifyController.class);
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private IAlipayLogService alipayLogService;
	@Autowired
	private IPushNotificationService pushNotificationService;

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/wxpay/pay.action", method = RequestMethod.POST)
    public void wxPay(HttpServletResponse response) {
		String response_content="<xml> \n" +
                "  <return_code><![CDATA[FAIL]]></return_code>\n" +
                "  <return_msg><![CDATA[ERROR]]></return_msg>\n" +
                "</xml> \n"; 
		PrintWriter out = null;
		try {
            out=response.getWriter();
            //step1: 获取回执参数
            System.out.println("-----开始处理微信通知------");
            InputStream is;
            String return_xml=null;
            is = request.getInputStream();
            return_xml= IOUtils.toString(is, "utf-8");
            boolean is_success = false;
            Document doc ;
            Map<String, String> parameterMap = new HashMap<>();
            doc = DocumentHelper.parseText(return_xml); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            Iterator return_code = rootElt.elementIterator("return_code"); // 获取根节点下的子节点return_code
            while (return_code.hasNext()) {
                Element recordEle = (Element) return_code.next();
                String code = recordEle.getText(); // 拿到return_code返回值
                log.debug("code:" + code);
                if(code!=null&&code.equals("SUCCESS"))
                {
                    is_success=true;
                    break;
                }              
            }
            if(is_success){
                parameterMap= XmlParse.parse(return_xml);
                String sign = parameterMap.get("sign");
                String result_code = parameterMap.get("result_code");               
                List<String> keys=new ArrayList<>(parameterMap.keySet());
                keys.remove("sign");
                String result_parameter = "";
                Collections.sort(keys);
                for (String str : keys) {
                    result_parameter = result_parameter + str +"="+ parameterMap.get(str)+"&";
                }
                result_parameter=result_parameter+"key="+ PayConfigUtils.getWx_app_secret_key();
                String current_sign=MD5Kit.encode(result_parameter).toUpperCase();
                if(current_sign.equals(sign))
                {
                    is_success=true;
                }else {
                    is_success=false;
                }
                System.out.println("微信支付签名校验："+is_success);

                if(is_success){
                    if(result_code.equals("SUCCESS")){
                    	//1：查询是否已经收到异步通知,如果已收到则停止执行                    	
                    	//2：未收到通知开始创建支付log
                    	Order order = alipayLogService.alipayNotify(parameterMap);
                    	response.reset();
                		out = response.getWriter();
                		response_content="<xml> \n" +
                                 "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                                 "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                                 "</xml> \n";
                		
                		//3：推送给司机     		
    					String orderId = order.getOrderid();
    					String driverPhone = order.getDriverphone();
    					String passengerPhone = order.getPassengerphone();
    					String content = "【来回出行】手机号为" + passengerPhone + "的乘客通过微信支付了" 
    							+new BigDecimal(Integer.parseInt(parameterMap.get("total_fee"))/100d)
    							+"元。请查验，订单编号为：" + orderId;
    					
    					Map<String, String> extrasParam= new HashMap<String, String>();
    					extrasParam.put("OrderId", orderId);				
    					
    					int flag = JpushClientUtil.getInstance(ConfigUtils.JPUSH_APP_KEY,ConfigUtils.JPUSH_MASTER_SECRET)
    							.sendToRegistrationId("11", driverPhone,
    									content, content, content,
    									extrasParam);
    					
    					if (flag == 1) {
    						PushNotification pushNotification = new PushNotification();
    						pushNotification.setPushPhone(driverPhone);
    						pushNotification.setReceivePhone(passengerPhone);
    						pushNotification.setOrderId(orderId);
    						pushNotification.setAlert(content);
    						pushNotification.setPushType(1);
    						pushNotification.setData(extrasParam.toString());
    						pushNotificationService.insertSelective(pushNotification);
    					}
                		
                    }else{
                    	response.reset();
                        out = response.getWriter();
                        response_content="<xml> \n" +
                                "  <return_code><![CDATA[FAIL]]></return_code>\n" +
                                "  <return_msg><![CDATA[result code fail]]></return_msg>\n" +
                                "</xml> \n";
                    }
                }else {
                    //直接停止执行
                	response.reset();
                    out = response.getWriter();
                    response_content="<xml> \n" +
                            "  <return_code><![CDATA[FAIL]]></return_code>\n" +
                            "  <return_msg><![CDATA[Signature verification error]]></return_msg>\n" +
                            "</xml> \n";
				}

            }else {
                //直接停止执行
            	response.reset();
                out = response.getWriter();
                response_content="<xml> \n" +
                        "  <return_code><![CDATA[FAIL]]></return_code>\n" +
                        "  <return_msg><![CDATA[return code fail]]></return_msg>\n" +
                        "</xml> \n";                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();                         
        }finally {
        	out.write(response_content);
            out.close();
        }       
	}
}
