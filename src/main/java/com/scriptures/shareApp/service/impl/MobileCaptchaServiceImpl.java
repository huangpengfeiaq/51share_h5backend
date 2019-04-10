package com.scriptures.shareApp.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.scriptures.shareApp.cache.MobileCaptchaCache;
import com.scriptures.shareApp.config.OSSConfig;
import com.scriptures.shareApp.config.PavilionConfig;
import com.scriptures.shareApp.contants.Errors;
import com.scriptures.shareApp.contants.SmsConstants;
import com.scriptures.shareApp.contants.SmsConstants.SmsCaptchaType;
import com.scriptures.shareApp.controller.request.CaptchaRequestBean;
import com.scriptures.shareApp.controller.response.CaptchaResponseBean;
import com.scriptures.shareApp.service.MemcachedService;
import com.scriptures.shareApp.service.MobileCaptchaService;
import com.scriptures.shareApp.service.SmsService;
import com.scriptures.shareApp.service.MemberService;
import com.scriptures.shareApp.util.ExceptionUtil;
import com.scriptures.shareApp.util.StringUtil;

/**
 * @Title: MobileCaptchaServiceImpl.java
 * @Package cc.uworks.library.service.impl
 * @author liyuchang
 * @Description: 短信验证码发送、验证
 * @date 2017年1月24日
 * @version V1.0
 */
@Service("mobileCaptchaService")
public class MobileCaptchaServiceImpl implements MobileCaptchaService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Resource
  private PavilionConfig pavilionConfig;
  @Resource
  private SmsService smsService;
  @Resource
  private MemcachedService memcachedService;
  @Resource
  private MemberService MemberService;
  /**
   * 发送验证码
   * 
   * @param mobile 手机号
   * @param type 类型:1注册,2修改密码,3找回密码，4注册+登陆，5绑定会员卡
   *        code=1手机号有误;code=2未超过发送冷却时间，coolSeconds=剩余发送冷却时间(单位秒)；code=3送失败请稍后再试
   * @return
   */
  @Override
  public CaptchaResponseBean send(CaptchaRequestBean bean) {
    // 发送验证
    sendVidate(bean);
    // 随机6位验证码数字
    String captcha = StringUtil.random(6);
    // 放入缓存
    MobileCaptchaCache mobileCaptcha = new MobileCaptchaCache();
    mobileCaptcha.setCaptcha(captcha);
    mobileCaptcha.setSentTime(new Date());
    this.memcachedService.set(captchaKey(bean.getType(), bean.getMobile()), SmsConstants.SMS_VALID_TIME, JSON.toJSONString(mobileCaptcha));
    // 验证码发送,是否存库待定
    /*if (!this.smsService.send(bean.getMobile(),
        String.format(SmsCaptchaType.getTemplet(bean.getType()), captcha, SmsConstants.SMS_VALID_TIME / 60))) {
      logger.error("发送验证码失败,{}");
      ExceptionUtil.throwException(3, "发送失败请稍后再试");
    }*/
    if (!this.smsService.send(bean.getMobile(),captcha)) {
          logger.error("发送验证码失败,{}");
          ExceptionUtil.throwException(3, "发送失败请稍后再试");
        }
    CaptchaResponseBean responseBeanBean = new CaptchaResponseBean();
    responseBeanBean.setValidSeconds(SmsConstants.SMS_VALID_TIME);
    responseBeanBean.setCoolSeconds(SmsConstants.SMS_COOL_TIME);
    return responseBeanBean;
  }

  /**
   * 验证码缓存key
   * 
   * @param type
   * @param mobile
   * @return groupId_SMS_CAPTCHA_type_mobile
   */
  private String captchaKey(Byte type, String mobile) {
    return String.format(SmsConstants.CAPTCHA_KEY, type, mobile);
  }

  /**
   * 发送验证,手机号是否注册、是否发送频繁等
   * 
   * @param bean
   */
  private void sendVidate(CaptchaRequestBean bean) {
    // 手机号验证
    if (!bean.getMobile().matches(SmsConstants.MOBILE_REGEX)) {
      ExceptionUtil.throwException(Errors.SYSTEM_CUSTOM_ERROR.code, "手机号有误");
    }
    // 注册 验证该手机号是否已存在
    if (bean.getType() == SmsCaptchaType.REGIST.code) {
      if (this.MemberService.checkPhone(bean.getMobile())) {
        ExceptionUtil.throwException(Errors.USER_MOBILE_EXISTS.code, "手机号已注册");
      }
    }
    // 找回密码
    else if (bean.getType() == SmsCaptchaType.RESET_PWD.code) {
      if (!this.MemberService.checkPhone(bean.getMobile())) {
        ExceptionUtil.throwException(Errors.USER_MOBILE_NOT_REGISTER.code, "手机号未注册");
      }
    }
    // 是否频繁发送
    String mobileCaptchaJson = (String) this.memcachedService.get(captchaKey(bean.getType(), bean.getMobile()));
    if (StringUtil.isBlank(mobileCaptchaJson)) {
      return;
    }
    MobileCaptchaCache mobileCaptcha = JSON.parseObject(mobileCaptchaJson, MobileCaptchaCache.class);
    // 是否未超过冷却时间
    long sendTime = mobileCaptcha.getSentTime().getTime();
    int seconds = (int) (System.currentTimeMillis() - sendTime) / 1000;
    if (seconds < SmsConstants.SMS_COOL_TIME) {
      ExceptionUtil.throwException(Errors.SYSTEM_CUSTOM_ERROR.code, "请求太过频繁，请稍后再试");
    }
  }

  /**
   * 验证码验证
   * 
   * @param mobile 手机号
   * @param captcha 验证码
   * @param type 类型:1注册,2修改密码,3找回密码
   */
  @Override
  public boolean verify(String mobile, String captcha, SmsCaptchaType type) {
    String captchaKey = captchaKey(type.code, mobile);
    String mobileCaptchaJson = (String) this.memcachedService.get(captchaKey);
    if (StringUtil.isBlank(mobileCaptchaJson)) {
      ExceptionUtil.throwException(Errors.CAPTCHA_IS_NULL.code, "未发送验证码");
    }
    MobileCaptchaCache mobileCaptcha = JSON.parseObject(mobileCaptchaJson, MobileCaptchaCache.class);
    if (!mobileCaptcha.getCaptcha().equals(captcha)) {
      ExceptionUtil.throwException(Errors.CAPTCHA_ERROR.code, "验证码有误");
    }
    this.memcachedService.delete(captchaKey);
    return true;
  }

}