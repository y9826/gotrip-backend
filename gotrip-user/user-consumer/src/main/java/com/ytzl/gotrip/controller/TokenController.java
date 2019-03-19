package com.ytzl.gotrip.controller;

import com.ytzl.gotrip.service.TokenService;
import com.ytzl.gotrip.utils.common.Constants;
import com.ytzl.gotrip.utils.common.Dto;
import com.ytzl.gotrip.utils.common.DtoUtil;
import com.ytzl.gotrip.vo.ItripTokenVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;


@Api(description = "置换Token")
@RestController
@RequestMapping("/api")
public class TokenController {

    @Resource
    private TokenService tokenService;

    @ApiOperation("提供客户端置换token操作，服务器需要获取客户端header中的token串")
    @PostMapping("/retoken")
    public Dto retoken(@ApiParam("token") @RequestHeader("token") String token,
                       @ApiParam(hidden = true) @RequestHeader("user-agent") String userAgent) throws Exception {

        tokenService.replaceToken(token, userAgent);

        // 构建返回结果
        ItripTokenVO tokenVO = new ItripTokenVO();
        tokenVO.setToken(token);
        // 获取token的生成时间
        String currentTimeStr = token.split("-")[3];
        long currTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(currentTimeStr).getTime();
        tokenVO.setGenTime(currTime);
        tokenVO.setExpTime(currTime + Constants.RedisExpire.SESSION_TIMEOUT * 1000);

        return DtoUtil.returnDataSuccess(tokenVO);
    }

}
