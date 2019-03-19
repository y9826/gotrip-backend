package com.ytzl.gotrip.rpc.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.ytzl.gotrip.ext.utils.RedisUtils;
import com.ytzl.gotrip.model.GotripUser;
import com.ytzl.gotrip.rpc.api.RpcTokenService;
import com.ytzl.gotrip.utils.common.*;
import com.ytzl.gotrip.utils.exception.GotripException;
import javafx.scene.control.Toggle;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * token 服务提供
 *
 */
@Component
@Service(interfaceClass = RpcTokenService.class)
public class RpcTokenServiceImpl implements RpcTokenService {

    @Resource
    RedisUtils redisUtils;



    @Override
    public String generateToken(GotripUser gotripUser, String userAgent) {
        // token:[MOBILE|PC]-userCode(md5)-userId-yyyyMMddHHmmss-浏览器标识

        StringBuffer sbToken = new StringBuffer("token:");
        if (UserAgentUtil.checkAgent(userAgent)) {
            // 移动设备
            sbToken.append("MOBILE-");
        }else{
            // pc设备
            sbToken.append("PC-");
        }

        String md5UserCode = DigestUtil.hmacSign(gotripUser.getUserCode());
        sbToken.append(md5UserCode).append("-");

        sbToken.append(gotripUser.getId()).append("-");

        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        sbToken.append(createDate).append("-");

        String md5UserAgent = DigestUtil.hmacSign(userAgent, 6);
        sbToken.append(md5UserAgent);

        return sbToken.toString();
    }

    @Override
    public void saveToken(String token, GotripUser gotripUser) {
        if (token.contains("PC-")) {
            redisUtils.set(token, JSON.toJSONString(gotripUser), Constants.RedisExpire.SESSION_TIMEOUT);
        }else if(token.contains("MOBILE-")){
            redisUtils.set(token, JSON.toJSONString(gotripUser));
        }
    }

    @Override
    public boolean verifyToken(String token, String userAgent) {
        // token是否存在
        if (!redisUtils.exist(token)) {
            return false;
        }
        // 创建token浏览器是否和当前浏览器一致
        String md5UserAgent = DigestUtil.hmacSign(userAgent, 6);

        return token.contains(md5UserAgent);
    }

    @Override
    public void removeToken(String token) {
        redisUtils.expire(token,3);
    }

    @Override
    public GotripUser getGotripUser(String token, String userAgent) throws Exception{
        // 验证浏览器
        if (!this.verifyToken(token,userAgent)) {
            throw new GotripException("Token无效！", ErrorCode.AUTH_PARAMETER_ERROR);
        }

        String gotripUserJson  = (String) redisUtils.get(token);

        return JSON.parseObject(gotripUserJson,GotripUser.class);
    }

    @Override
    public boolean existsToken(String token){
        return !EmptyUtils.isEmpty(redisUtils.get(token));
    }

    /**
     * 置换token
     *
     * @param token     旧的 token
     * @param userAgent 浏览器标识
     * @return 新的token
     */
    @Override
    public String replaceToken(String token, String userAgent) throws Exception {

        String newToken="";

        // 得到对象信息
        GotripUser gotripUser = this.getGotripUser(token, userAgent);
        // 剩余的token时间
        long ttl = redisUtils.ttl(token);
        if(ttl>=-1){
            // 生成新的Token
            newToken = this.generateToken(gotripUser,userAgent);
            // 缓存新的Token
            this.saveToken(newToken,gotripUser);
            // 1分钟后旧token消失
            redisUtils.set(token, JSON.toJSONString(gotripUser),Constants.RedisExpire.DEFAULT_EXPIRE);

        }else{
            throw new GotripException("token过期时间异常，请稍后重试！",ErrorCode.AUTH_PARAMETER_ERROR);
        }
        return newToken;
    }
}
