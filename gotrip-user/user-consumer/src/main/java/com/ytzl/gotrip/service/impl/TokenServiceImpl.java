package com.ytzl.gotrip.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ytzl.gotrip.rpc.api.RpcTokenService;
import com.ytzl.gotrip.service.TokenService;
import com.ytzl.gotrip.utils.common.Constants;
import com.ytzl.gotrip.utils.common.Dto;
import com.ytzl.gotrip.utils.common.DtoUtil;
import com.ytzl.gotrip.utils.common.ErrorCode;
import com.ytzl.gotrip.utils.exception.GotripException;
import com.ytzl.gotrip.vo.ItripTokenVO;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


@Service("tokenService")
public class TokenServiceImpl implements TokenService {



    @Reference
    private RpcTokenService rpcTokenService;
    /**
     * 置换Token
     * <p>
     * 1.验证token是否有效/存在
     * 2.token置换保护其（1H）
     * ttl获取剩余token 剩余时间>0 || 剩余时间==-1（没有过期时间返回-1）
     * 3.生成并保存新的token
     * 4.删除token
     *
     * @param token     旧的Token
     * @param userAgent user-agent
     * @return 新的token
     * @throws Exception
     */
    @Override
    public String replaceToken(String token, String userAgent) throws Exception {
        // 1.判断token是否存在
        if (!rpcTokenService.existsToken(token)) {
            throw new GotripException("token有误或以失效，请核对！", ErrorCode.AUTH_PARAMETER_ERROR);
        }

        // 2.当前时间 - token中的生成时间 = token存在时间
        String[] tokenSplit = token.split("-");
        Date genTokenTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(tokenSplit[3]);
        long exisTime = Calendar.getInstance().getTimeInMillis() - genTokenTime.getTime();
        // 如果存在时间小于一小时，不允许在替换
        if(exisTime < Constants.RedisExpire.SESSION_TIMEOUT*500){
            throw new GotripException("token处于置换保护期内，剩余："+(Constants.RedisExpire.SESSION_TIMEOUT*500-exisTime)+"秒，禁止置换！",ErrorCode.AUTH_PARAMETER_ERROR);
        }


        // 3.置换token
        return rpcTokenService.replaceToken(token, userAgent);

    }
}
