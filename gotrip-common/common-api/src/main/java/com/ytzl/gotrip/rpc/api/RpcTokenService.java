package com.ytzl.gotrip.rpc.api;

import com.ytzl.gotrip.model.GotripUser;
import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

public interface RpcTokenService {

    // token:[MOBILE|PC]-userCode(md5)-userId-yyyyMMddHHmmss-浏览器标识

    /**
     * 生成token
     *
     * @param gotripUser 用户信息
     * @param userAgent  浏览器内核版本
     * @return token令牌
     */
    public String generateToken(GotripUser gotripUser, String userAgent);


    /**
     * 保存token
     *
     * @param token      token令牌
     * @param gotripUser 用户信息
     */
    public void saveToken(String token, GotripUser gotripUser);


    /**
     * 验证token是否有效
     *
     * @param token     token令牌
     * @param userAgent 浏览器内核版本验证
     * @return 验证结果
     */
    public boolean verifyToken(String token, String userAgent);


    /**
     * 删除token
     *
     * @param token token令牌
     */
    public void removeToken(String token);


    /**
     * 根据token获取用户
     *
     * @param token     token令牌
     * @param userAgent 浏览器标识
     * @return 用户数据
     */
    public GotripUser getGotripUser(String token, String userAgent) throws Exception;
}
