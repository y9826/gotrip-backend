package com.ytzl.gotrip.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ytzl.gotrip.model.GotripUser;
import com.ytzl.gotrip.rpc.api.RpcTokenService;
import com.ytzl.gotrip.service.LoginService;
import com.ytzl.gotrip.utils.common.Constants;
import com.ytzl.gotrip.utils.common.DigestUtil;
import com.ytzl.gotrip.utils.common.EmptyUtils;
import com.ytzl.gotrip.utils.common.ErrorCode;
import com.ytzl.gotrip.utils.exception.GotripException;
import org.springframework.stereotype.Service;

@Service("loginService")
public class LoginServiceImpl implements LoginService {

    @Reference
    private RpcTokenService rpcTokenService;

    @Override
    public String doLogin(GotripUser gotripUser, String password,String userAgent) throws Exception {
        // 判断用户是否为空
        if (EmptyUtils.isEmpty(gotripUser)) {
            throw new GotripException("用户不存在！", ErrorCode.AUTH_PARAMETER_ERROR);
        }

        // 判断密码
        String md5Password = DigestUtil.hmacSign(password);

        if (!md5Password.equals(gotripUser.getUserPassword())) {
            throw new GotripException("用户名或密码错误！",ErrorCode.AUTH_PARAMETER_ERROR);
        }

        // 用户是否激活
        if (gotripUser.getActivated()!= Constants.UserActivate.USER_ACTIVATE_ENABLE) {
            throw new GotripException("用户未激活！",ErrorCode.AUTH_PARAMETER_ERROR);
        }

        gotripUser.setUserPassword(null);

        // 生成token
        String token = rpcTokenService.generateToken(gotripUser, userAgent);
        // 保存token
        rpcTokenService.saveToken(token,gotripUser);

        return token;
    }

    @Override
    public void logout(String token,String userAgent) throws Exception{
        if (rpcTokenService.verifyToken(token,userAgent)) {
            rpcTokenService.removeToken(token);
        }else{
            throw new GotripException("token无效",ErrorCode.AUTH_PARAMETER_ERROR);
        }
    }
}
