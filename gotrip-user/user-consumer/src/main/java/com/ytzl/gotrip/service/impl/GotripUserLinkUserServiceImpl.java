package com.ytzl.gotrip.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ytzl.gotrip.ext.utils.RedisUtils;
import com.ytzl.gotrip.model.GotripUser;
import com.ytzl.gotrip.model.GotripUserLinkUser;
import com.ytzl.gotrip.rpc.api.RpcGotripUserLinkUserService;
import com.ytzl.gotrip.rpc.api.RpcTokenService;
import com.ytzl.gotrip.service.GotripUserLinkUserService;
import com.ytzl.gotrip.utils.common.EmptyUtils;
import com.ytzl.gotrip.utils.common.ErrorCode;
import com.ytzl.gotrip.utils.exception.GotripException;
import com.ytzl.gotrip.vo.userinfo.ItripAddUserLinkUserVO;
import com.ytzl.gotrip.vo.userinfo.ItripModifyUserLinkUserVO;
import com.ytzl.gotrip.vo.userinfo.ItripSearchUserLinkUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("gotripUserLinkUserService")
public class GotripUserLinkUserServiceImpl implements GotripUserLinkUserService {



    @Reference
    private RpcGotripUserLinkUserService rpcGotripUserLinkUserService;

    @Reference
    private RpcTokenService rpcTokenService;


    /**
     * 查询常用联系人信息
     *
     * @param token     得到用户id查询与他相关的用户
     * @param userAgent 浏览器标识
     * @return 相关联的用户
     */
    @Override
    public List<GotripUserLinkUser> queryUserLinkUser(String token, String userAgent, ItripSearchUserLinkUserVO itripSearchUserLinkUserVO) throws Exception {
        long userId = existsGotripUser(token, userAgent);
        Map<String,Object> param = new HashMap<>();
        param.put("userId",userId);
        if (EmptyUtils.isNotEmpty(itripSearchUserLinkUserVO.getLinkUserName())) {
            String linkUserName = itripSearchUserLinkUserVO.getLinkUserName();
            param.put("linkUserName",linkUserName);
        }
        return rpcGotripUserLinkUserService.getGotripUserLinkUserListByIdOrLink(param);
    }

    /**
     * 修改常用联系人信息
     *
     * @param token                  得到用户信息
     * @param userAgent              浏览器标识
     * @param itripModifyUserLinkUserVO 修改后的常用联系人信息
     * @return
     */
    @Override
    public Integer modifyUserLinkUser(String token, String userAgent, ItripModifyUserLinkUserVO itripModifyUserLinkUserVO) throws Exception {
        long userId = existsGotripUser(token, userAgent);

        if(null!=itripModifyUserLinkUserVO){
            // 修改有值，直接调用添加方法即可  存入ItripUserLinkUser
            // 前有值，后为空
            GotripUserLinkUser gotripUserLinkUser = new GotripUserLinkUser();
            BeanUtils.copyProperties(itripModifyUserLinkUserVO,gotripUserLinkUser);
            gotripUserLinkUser.setModifiedBy(userId);
            gotripUserLinkUser.setModifyDate(new Date(System.currentTimeMillis()));
            gotripUserLinkUser.setUserId((int) userId);

            System.out.println(gotripUserLinkUser.toString());
            rpcGotripUserLinkUserService.updateGotripUserLinkUser(gotripUserLinkUser);

        }else if(null==itripModifyUserLinkUserVO){
            throw new GotripException("不能提交为空，请填写常用联系人信息！",ErrorCode.AUTH_PARAMETER_ERROR);
        }else{
            throw new GotripException("token失效，请重新登陆！",ErrorCode.AUTH_PARAMETER_ERROR);
        }



        return null;
    }


    /**
     * 添加常用联系人信息
     *
     * @param token
     * @param userAgent              浏览器标识
     * @param itripAddUserLinkUserVO 添加的常用联系人信息
     * @return
     */
    @Override
    public Integer addUserLinkUser(String token, String userAgent, ItripAddUserLinkUserVO itripAddUserLinkUserVO) throws Exception{
        long userId = existsGotripUser(token, userAgent);


        if(null!=itripAddUserLinkUserVO){
            // 添加，直接调用添加方法即可  存入ItripUserLinkUser
            GotripUserLinkUser gotripUserLinkUser = new GotripUserLinkUser();
            BeanUtils.copyProperties(itripAddUserLinkUserVO,gotripUserLinkUser);
            gotripUserLinkUser.setUserId((int) userId);
            gotripUserLinkUser.setCreatedBy(userId);
            gotripUserLinkUser.setCreationDate(new Date(System.currentTimeMillis()));


            rpcGotripUserLinkUserService.insertGotripUserLinkUser(gotripUserLinkUser);

        }else if(null==itripAddUserLinkUserVO){
            throw new GotripException("不能提交为空，请填写常用联系人！",ErrorCode.AUTH_PARAMETER_ERROR);
        }else{
            throw new GotripException("token失效，请重新登陆！",ErrorCode.AUTH_PARAMETER_ERROR);
        }


        return null;
    }



    /**
     * 删除常用联系人信息
     *
     * @param token     得到用户id
     * @param userAgent 浏览器标识
     * @param ids       要删除的常用联系人
     * @return
     */
    @Override
    public Integer delUserLinkUser(String token, String userAgent, Long[] ids) throws Exception{
        existsGotripUser(token, userAgent);

        if(EmptyUtils.isNotEmpty(ids)){
            // 删除
            rpcGotripUserLinkUserService.deleteGotripUserLinkUserById(ids);

        }else if(EmptyUtils.isEmpty(ids)){
            throw new GotripException("请选择要删除的常用联系人！",ErrorCode.AUTH_PARAMETER_ERROR);
        }else{
            throw new GotripException("token失效，请重新登陆！",ErrorCode.AUTH_PARAMETER_ERROR);
        }

        return null;
    }

    /**
     * 是否存在 gotripUser（token是否失效）
     *  为空就抛出异常
     * @return 返回登陆用户的id
     */
    public long existsGotripUser(String token,String userAgent) throws Exception{
        GotripUser gotripUser = rpcTokenService.getGotripUser(token, userAgent);
        if (EmptyUtils.isEmpty(gotripUser)) {
            throw new GotripException("token失效，请重新登陆！", ErrorCode.AUTH_PARAMETER_ERROR);
        }
        return gotripUser.getId();
    }
}
