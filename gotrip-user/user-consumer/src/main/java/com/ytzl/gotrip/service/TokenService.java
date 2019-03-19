package com.ytzl.gotrip.service;

import com.ytzl.gotrip.utils.common.Dto;

public interface TokenService {

    /**
     * 置换Token
     *
     * 1.验证token是否有效/存在
     * 2.token置换保护其（1H）
     *      ttl获取剩余token 剩余时间>0 || 剩余时间==-1（没有过期时间返回-1）
     * 3.生成并保存新的token
     * 4.删除token
     *
     *
     * @param token 旧的Token
     * @param userAgent user-agent
     * @return 新的token
     * @throws Exception
     */
    String replaceToken(String token, String userAgent) throws Exception;
}
