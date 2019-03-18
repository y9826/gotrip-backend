package com.ytzl.gotrip.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ytzl.gotrip.ext.utils.RedisUtils;
import com.ytzl.gotrip.model.GotripUser;
import com.ytzl.gotrip.rpc.api.RpcGotripUserService;
import com.ytzl.gotrip.rpc.api.RpcSendMessageService;
import com.ytzl.gotrip.service.GotripUserService;
import com.ytzl.gotrip.utils.common.*;
import com.ytzl.gotrip.utils.exception.GotripException;
import com.ytzl.gotrip.vo.userinfo.ItripUserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service(value = "gotripUserService")
public class GotripUserServiceImpl implements GotripUserService {

    private Logger LOG = LoggerFactory.getLogger(GotripUserServiceImpl.class);


    @Reference
    private RpcGotripUserService rpcGotripUserService;

    @Reference(timeout = 10000,retries = 0)
    private RpcSendMessageService rpcSendMessageService;

    @Resource
    private RedisUtils redisUtils;



    @Override
    public GotripUser findByUserCode(String userCode) throws Exception {

        if(EmptyUtils.isEmpty(userCode)){
            throw new GotripException("用户Code不能为空！", ErrorCode.AUTH_PARAMETER_ERROR);
        }

        Map<String,Object> params = new HashMap<>();
        params.put("userCode",userCode);
        List<GotripUser> gotripUserList = rpcGotripUserService.getGotripUserListByMap(params);
        /*if (EmptyUtils.isEmpty(gotripUserList)) {
            throw new GotripException("登陆账号不存在！", ErrorCode.AUTH_PARAMETER_ERROR);
        }
        return gotripUserList.get(0);*/
        return EmptyUtils.isEmpty(gotripUserList)?null:gotripUserList.get(0);
    }


    @Override
    public void registerByPhone(ItripUserVO itripUserVO) throws Exception {
        // 数据校验
        checkRegisterDate(itripUserVO);
        // 验证手机号
        if (!validPhone(itripUserVO.getUserCode())) {
            throw new GotripException("手机号格式不正确",ErrorCode.AUTH_PARAMETER_ERROR);
        }
        // 判断用户是否存在
        ckeckUsr(itripUserVO.getUserName());
        /*GotripUser user = this.findByUserCode(itripUserVO.getUserCode());
        if (EmptyUtils.isNotEmpty(user)) {
            throw new GotripException("用户已存在！",ErrorCode.AUTH_USER_ALREADY_EXISTS);
        }*/
        // 构建用户信息
        GotripUser gotripUser = new GotripUser();
        BeanUtils.copyProperties(itripUserVO,gotripUser);
        gotripUser.setActivated(Constants.UserActivate.USER_ACTIVATE_DISABLE);

        // 密码加密
        String md5UserPassword = DigestUtil.hmacSign(gotripUser.getUserPassword());
        gotripUser.setUserPassword(md5UserPassword);


        // 数据入库
        Integer resultSize = rpcGotripUserService.insertGotripUser(gotripUser);
        // 发送短信验证码
        // 构建四位验证码
        int code = DigestUtil.randomCode();
        rpcSendMessageService.sendMessage(gotripUser.getUserCode(),"1",""+code);
        // 将验证码保存到redis中
        String key = Constants.RedisKeyPrefix.ACTIVATION_MOBILE_PREFIX + gotripUser.getUserCode();
        redisUtils.set(key,""+code,60*3);
    }

    @Override
    public void validatePhone(String user, String code) throws Exception {

        // 验证手机号格式是否正确
        if (!validPhone(user)) {
            throw new GotripException("请输入正确的手机号!",ErrorCode.AUTH_PARAMETER_ERROR);
        }


        // 验证用户是否存在
        ckeckUsr(user);
        GotripUser gotripUser = this.findByUserCode(user);
        /*if (EmptyUtils.isEmpty(gotripUser)) {
            throw new GotripException("用户不存在！",ErrorCode.AUTH_PARAMETER_ERROR);
        }*/


        // 获取redis中存储的短信验证码
        String key = Constants.RedisKeyPrefix.ACTIVATION_MOBILE_PREFIX + user;
        String cacheCode = (String) redisUtils.get(key);
        if (EmptyUtils.isEmpty(cacheCode) || !cacheCode.equals(code)) {
            throw new GotripException("验证码已失效",ErrorCode.AUTH_PARAMETER_ERROR);
        }

        // 激活用户
        gotripUser.setActivated(Constants.UserActivate.USER_ACTIVATE_ENABLE);
        gotripUser.setUserType(0);
        gotripUser.setFlatID(gotripUser.getId());
        rpcGotripUserService.updateGotripUser(gotripUser);


        LOG.info("-------------->   用户[{}]激活成功！",user);

    }

    /**
     * 通过邮箱号注册
     *
     * @param itripUserVO 用户数据
     * @throws Exception
     */
    @Override
    public void registerByEmail(ItripUserVO itripUserVO) throws Exception {
        LOG.info("----------------------》 进入方法：registerByEmail 准备注册邮箱 ");
        // 数据校验
        checkRegisterDate(itripUserVO);
        // 验证邮箱格式是否正确
        if (!validEmail(itripUserVO.getUserCode())) {
            throw new GotripException("邮箱格式错误！",ErrorCode.AUTH_PARAMETER_ERROR);
        }

        // 构建用户信息
        GotripUser gotripUser = new GotripUser();
        BeanUtils.copyProperties(itripUserVO,gotripUser);
        gotripUser.setActivated(Constants.UserActivate.USER_ACTIVATE_DISABLE);
        // 密码加密
        String hmacSign = DigestUtil.hmacSign(gotripUser.getUserPassword());
        gotripUser.setUserPassword(hmacSign);
        // 数据入库
        Integer resultSize = rpcGotripUserService.insertGotripUser(gotripUser);
        LOG.info("----------------------》 储存数据库中成功！ ");
        // 发送短信验证码
        // 构建四位验证码
        int code = DigestUtil.randomCode();
        rpcSendMessageService.sendMessageEmail(gotripUser.getUserCode(),""+code);
        //this.sendMessageEmail(gotripUser.getUserCode(),""+code);
        LOG.info("----------------------》 发送邮件成功！ ");
        // 将验证码保存到redis中
        String key = Constants.RedisKeyPrefix.ACTIVATION_EMAIL_PREFIX + itripUserVO.getUserCode();
        LOG.info("----------------------》 key: "+key);
        redisUtils.set(key,""+code,60*3);
    }


    @Override
    public void validateEmail(String user, String code) throws Exception{
        // 验证邮箱格式是否正确
        if (!validEmail(user)) {
            throw new GotripException("邮箱格式错误！",ErrorCode.AUTH_PARAMETER_ERROR);
        }
        // 判断用户是否存在
       GotripUser gotripUser = findByUserCode(user);
         if (EmptyUtils.isEmpty(gotripUser)) {
            throw new GotripException("邮箱账号不存在！",ErrorCode.AUTH_PARAMETER_ERROR);
        }
        // 获取redis中存储的短信验证码
        String key = Constants.RedisKeyPrefix.ACTIVATION_EMAIL_PREFIX + user;
        String cacheCode = (String) redisUtils.get(key);
        if (EmptyUtils.isEmpty(cacheCode)) {
            throw new GotripException("激活码已失效，请重新发送！",ErrorCode.AUTH_PARAMETER_ERROR);
        }else if(!cacheCode.equals(code)){
            throw new GotripException("激活码错误，请重新输入！",ErrorCode.AUTH_PARAMETER_ERROR);
        }
        // 激活用户
        gotripUser.setActivated(Constants.UserActivate.USER_ACTIVATE_ENABLE); // 修改激活状态为1
        gotripUser.setUserType(0); // 用户类型
        gotripUser.setFlatID(gotripUser.getId()); // 平台Id
        rpcGotripUserService.updateGotripUser(gotripUser);// 修改

        LOG.info("-------------->   用户[{}]激活成功！",user);
    }


    /**
     * 校验注册数据
     *
     * @param itripUserVO 用户数据
     * @throws GotripException
     */
    private void checkRegisterDate(ItripUserVO itripUserVO) throws GotripException {
        if (EmptyUtils.isEmpty(itripUserVO)) {
            throw new GotripException("请传递参数", ErrorCode.AUTH_PARAMETER_ERROR);
        }
        if (EmptyUtils.isEmpty(itripUserVO.getUserCode())) {
            throw new GotripException("用户账号不能为空", ErrorCode.AUTH_PARAMETER_ERROR);
        }
        if (EmptyUtils.isEmpty(itripUserVO.getUserName())) {
            throw new GotripException("用户昵称不能为空", ErrorCode.AUTH_PARAMETER_ERROR);
        }
        if (EmptyUtils.isEmpty(itripUserVO.getUserPassword())) {
            throw new GotripException("用户密码不能为空", ErrorCode.AUTH_PARAMETER_ERROR);
        }
    }



    @Override
    public void ckeckUsr(String name) throws Exception {
        // 判断用户是否存在
        GotripUser user = findByUserCode(name);
        if (EmptyUtils.isNotEmpty(user)) {
            throw new GotripException("用户已存在！",ErrorCode.AUTH_PARAMETER_ERROR);
        }
    }

    /**			 *
     * 合法E-mail地址：
     * 1. 必须包含一个并且只有一个符号“@”
     * 2. 第一个字符不得是“@”或者“.”
     * 3. 不允许出现“@.”或者.@
     * 4. 结尾不得是字符“@”或者“.”
     * 5. 允许“@”前的字符中出现“＋”
     * 6. 不允许“＋”在最前面，或者“＋@”
     */
    private boolean validEmail(String email){

        String regex="^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$"  ;
        return Pattern.compile(regex).matcher(email).find();
    }
    /**
     * 验证是否合法的手机号
     * @param phone
     * @return
     */
    private boolean validPhone(String phone) {
        String regex="^1[356789]{1}\\d{9}$";
        return Pattern.compile(regex).matcher(phone).find();
    }






}
