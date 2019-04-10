package com.scriptures.shareApp.service;

import com.scriptures.shareApp.contants.SmsConstants.SmsCaptchaType;
import com.scriptures.shareApp.controller.request.CaptchaRequestBean;
import com.scriptures.shareApp.controller.response.CaptchaResponseBean;

/**
 * @Title: MobileCaptchaService.java
 * @Package cc.uworks.library.service
 * @author liyuchang
 * @Description: 短信验证码发送、验证
 * @date 2017年1月23日
 * @version V1.0
 */
public interface MobileCaptchaService {

  /**
   * 发送验证码
   * @param mobile 手机号
   * @param type 类型:1注册,2修改密码,3找回密码
   * @return
   */
  CaptchaResponseBean send(CaptchaRequestBean bean);

  /**
   * 验证码验证
   * @param mobile 手机号
   * @param captcha 验证码
   * @param type 类型:1注册,2修改密码,3找回密码
   */
  boolean verify(String mobile, String captcha, SmsCaptchaType type);
}