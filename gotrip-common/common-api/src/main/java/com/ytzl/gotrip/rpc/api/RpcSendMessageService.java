package com.ytzl.gotrip.rpc.api;

/**
 * 短信发送RPC
 */
public interface RpcSendMessageService {

    /**
     * 发送短信
     *
     * @param phone      手机号，多手机好使用【，】分割
     * @param templateId 模板Id，未上线应用填写（官方提供，固定模板）
     * @param code       短信验证码
     */
    public void sendMessage(String phone, String templateId, String code);

}
