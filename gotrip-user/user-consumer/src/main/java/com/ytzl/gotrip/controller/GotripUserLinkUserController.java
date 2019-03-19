package com.ytzl.gotrip.controller;

import com.alibaba.fastjson.JSON;
import com.ytzl.gotrip.model.GotripUserLinkUser;
import com.ytzl.gotrip.service.GotripUserLinkUserService;
import com.ytzl.gotrip.utils.common.Dto;
import com.ytzl.gotrip.utils.common.DtoUtil;
import com.ytzl.gotrip.vo.userinfo.ItripAddUserLinkUserVO;
import com.ytzl.gotrip.vo.userinfo.ItripModifyUserLinkUserVO;
import com.ytzl.gotrip.vo.userinfo.ItripSearchUserLinkUserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.List;

@Api(description = "个人中心控制器")
@RestController
@RequestMapping("/api/userinfo")
public class GotripUserLinkUserController {

    @Resource
    private GotripUserLinkUserService gotripUserLinkUserService;

    /**
     * POST /api/userinfo/queryuserlinkuser
     *
     *  POST /api/userinfo/modifyuserlinkuser
     *
     *  GET /api/userinfo/deluserlinkuser
     *
     *  POST /api/userinfo/adduserlinkuser
     *
     */

    @ApiOperation("查询常用联系人信息(可根据联系人姓名进行模糊查询)")
    @PostMapping("/queryuserlinkuser")
    public Dto queryUserLinkUser(@ApiParam("token令牌") @RequestHeader("token") String token,
                                 @ApiParam(hidden = true)@RequestHeader("user-agent") String userAgent,
                                 @ApiParam("相关联的用户名")@RequestBody ItripSearchUserLinkUserVO itripSearchUserLinkUserVO) throws Exception{


        List<GotripUserLinkUser> linkUsers = gotripUserLinkUserService.queryUserLinkUser(token, userAgent,itripSearchUserLinkUserVO);

        return DtoUtil.returnDataSuccess(JSON.toJSON(linkUsers));
    }

   @ApiOperation("修改常用联系人信息")
    @PostMapping("/modifyuserlinkuser")
    public Dto modifyUserLinkUser(@ApiParam("token令牌") @RequestHeader("token") String token,
                                 @ApiParam(hidden = true)@RequestHeader("user-agent") String userAgent,
                                  @ApiParam("参数") @RequestBody ItripModifyUserLinkUserVO itripModifyUserLinkUserVO) throws Exception{

        Integer i = gotripUserLinkUserService.modifyUserLinkUser(token, userAgent,itripModifyUserLinkUserVO);

        return DtoUtil.returnDataSuccess(i);
    }

    @ApiOperation("添加常用联系人信息")
    @PostMapping("/adduserlinkuser")
    public Dto addUserLinkUser(@ApiParam("token令牌") @RequestHeader("token") String token,
                                 @ApiParam(hidden = true)@RequestHeader("user-agent") String userAgent,
                               @ApiParam("参数") @RequestBody ItripAddUserLinkUserVO itripAddUserLinkUserVO) throws Exception{
        Integer i = gotripUserLinkUserService.addUserLinkUser(token, userAgent,itripAddUserLinkUserVO);

        return DtoUtil.returnDataSuccess(i);
    }

    @ApiOperation("删除常用联系人信息")
    @GetMapping("/deluserlinkuser")
    public Dto delUserLinkUser(@ApiParam("token令牌") @RequestHeader("token") String token,
                                 @ApiParam(hidden = true)@RequestHeader("user-agent") String userAgent,
                               Long[] ids) throws Exception{


        Integer i = gotripUserLinkUserService.delUserLinkUser(token, userAgent,ids);
        return DtoUtil.returnDataSuccess(i);
    }
}
