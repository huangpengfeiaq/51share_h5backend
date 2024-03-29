package com.scriptures.shareApp.util;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.scriptures.shareApp.config.SmsConfig;


@Service
public class AliYunSmsUtil {
	
	@Resource
	private SmsConfig smsConfig;


    private final static String ERRORKEY = "errorMessage";  //返回错误的key

    // @phoneNum: 目标手机号，多个手机号可以逗号分隔;
    // @params: 短信模板中的变量，数字必须转换为字符串，如短信模板中变量为${no}",则参数params的值为{"no":"123456"}
    public void sendMsg(String phoneNum, String params){
    	 String accessKeyId = smsConfig.getAccessKeyID();
         String accessSecret = smsConfig.getAccessKeySecret();
         java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         df.setTimeZone(new java.util.SimpleTimeZone(0, "GMT"));// 这里一定要设置GMT时区
         java.util.Map<String, String> paras = new java.util.HashMap<String, String>();
         // 1. 系统参数
         paras.put("SignatureMethod", "HMAC-SHA1");
         paras.put("SignatureNonce", java.util.UUID.randomUUID().toString());
         paras.put("AccessKeyId", accessKeyId);
         paras.put("SignatureVersion", "1.0");
         paras.put("Timestamp", df.format(new java.util.Date()));
         paras.put("Format", "XML");
         // 2. 业务API参数
         paras.put("Action", "SendSms");
         paras.put("Version", "2017-05-25");
         paras.put("RegionId", "cn-hangzhou");
         paras.put("PhoneNumbers", phoneNum);
         paras.put("SignName", smsConfig.getSign());
         paras.put("TemplateParam", params);
         paras.put("TemplateCode", smsConfig.getTemplateCode());
         paras.put("OutId", "123");
         // 3. 去除签名关键字Key
         if (paras.containsKey("Signature"))
             paras.remove("Signature");
         // 4. 参数KEY排序
         java.util.TreeMap<String, String> sortParas = new java.util.TreeMap<String, String>();
         sortParas.putAll(paras);
         // 5. 构造待签名的字符串
         java.util.Iterator<String> it = sortParas.keySet().iterator();
         StringBuilder sortQueryStringTmp = new StringBuilder();
         
         try {
        	 while (it.hasNext()) {
                 String key = it.next();
                 sortQueryStringTmp.append("&").append(specialUrlEncode(key)).append("=").append(specialUrlEncode(paras.get(key)));
             }
             String sortedQueryString = sortQueryStringTmp.substring(1);// 去除第一个多余的&符号
             StringBuilder stringToSign = new StringBuilder();
        	 stringToSign.append("GET").append("&");
             stringToSign.append(specialUrlEncode("/")).append("&");
             stringToSign.append(specialUrlEncode(sortedQueryString));
             String sign = sign(accessSecret + "&", stringToSign.toString());
             // 6. 签名最后也要做特殊URL编码
             String signature = specialUrlEncode(sign);
            
             System.out.println(paras.get("SignatureNonce"));
             System.out.println("\r\n=========\r\n");
             System.out.println(paras.get("Timestamp"));
             System.out.println("\r\n=========\r\n");
             System.out.println(sortedQueryString);
             System.out.println("\r\n=========\r\n");
             System.out.println(stringToSign.toString());
             System.out.println("\r\n=========\r\n");
             System.out.println(sign);
             System.out.println("\r\n=========\r\n");
             System.out.println(signature);
             System.out.println("\r\n=========\r\n");
             // 最终打印出合法GET请求的URL
             System.out.println( HttpUtil.doGet("http://"+smsConfig.getUrl()+"/?Signature=" + signature + sortQueryStringTmp));
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    			
    }
    
    
    public static String specialUrlEncode(String value) throws Exception {
        return java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
    }
    
    public static String sign(String accessSecret, String stringToSign) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
        mac.init(new javax.crypto.spec.SecretKeySpec(accessSecret.getBytes("UTF-8"), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        return new sun.misc.BASE64Encoder().encode(signData);
    }

    
}
