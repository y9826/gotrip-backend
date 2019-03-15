package com.ytzl.gotrip.service;

import com.ytzl.gotrip.model.GotripUser;
import com.ytzl.gotrip.utils.exception.GotripException;
import com.ytzl.gotrip.vo.userinfo.ItripUserVO;

public interface GotripUserService {

    /**
     * 根据用户登陆账号查询用户信息
     *
     * @param userCode 登陆账号
     * @return 用户信息（包含密码）
     */
    public GotripUser findByUserCode(String userCode) throws Exception;

    /**
     * 通过手机号注册
     *
     * @param itripUserVO 用户数据
     */
    void registerByPhone(ItripUserVO itripUserVO) throws Exception;

    /**
     * 手机账号激活
     *
     * @param user 登陆账号
     * @param code 验证码
     */
    void validatePhone(String user, String code) throws Exception;

    /**
     * 邮箱账号激活
     *
     * @param user 登陆账号
     * @param code 激活码
     */
    void validateEmail(String user, String code);
}