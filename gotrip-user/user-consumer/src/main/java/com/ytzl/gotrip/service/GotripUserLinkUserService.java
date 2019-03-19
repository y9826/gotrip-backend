package com.ytzl.gotrip.service;

import com.ytzl.gotrip.model.GotripUserLinkUser;
import com.ytzl.gotrip.vo.userinfo.ItripAddUserLinkUserVO;
import com.ytzl.gotrip.vo.userinfo.ItripModifyUserLinkUserVO;
import com.ytzl.gotrip.vo.userinfo.ItripSearchUserLinkUserVO;

import java.util.List;

public interface GotripUserLinkUserService {

    /**
     * 查询常用联系人信息
     *
     * @param token 得到用户id查询与他相关的用户
     * @param userAgent 浏览器标识
     * @return 相关联的用户
     */
    List<GotripUserLinkUser> queryUserLinkUser(String token, String userAgent, ItripSearchUserLinkUserVO itripSearchUserLinkUserVO) throws Exception;

    /**
     * 修改常用联系人信息
     *
     * @param token 得到用户信息
     * @param userAgent 浏览器标识
     * @param itripModifyUserLinkUserVO 修改后的常用联系人信息
     * @return
     */
    Integer modifyUserLinkUser(String token, String userAgent, ItripModifyUserLinkUserVO itripModifyUserLinkUserVO) throws Exception;

    /**
     * 删除常用联系人信息
     *
     * @param token 得到用户id
     * @param userAgent 浏览器标识
     * @param ids 要删除的常用联系人
     * @return
     */
    Integer delUserLinkUser(String token,String userAgent,Long[] ids) throws Exception;

    /**
     * 添加常用联系人信息
     *
     * @param token
     * @param userAgent 浏览器标识
     * @param itripAddUserLinkUserVO 添加的常用联系人信息
     * @return
     */
    Integer addUserLinkUser(String token,String userAgent,ItripAddUserLinkUserVO itripAddUserLinkUserVO) throws Exception;

}
