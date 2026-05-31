package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    // 微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        // 调用微信接口服务，获取当前微信用户的 openid
        String openid = getOpenid(userLoginDTO.getCode());

        // 判断 openid 是否为空，如果为空表示登录失败，抛出业务异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 判断当前用户是否为系统的新用户
        User user = userMapper.getByOpenid(openid);

        // 如果是新用户，自动完成注册
        // 💡 为什么仅凭 openid 就能完成注册？
        // 1. 微信限制：wx.login 接口仅返回 code，用其换取的只有 openid。手机号、头像等敏感信息需用户在后续显式授权。
        // 2. 极致体验：采用“静默注册”降低用户首次使用门槛，避免繁杂的表单流失用户（“先上车，后补票”思想）。
        // 3. 数据库设计支持：user 表除主键和 openid 字段外，其余字段（如姓名、手机号等）均允许为 NULL，技术上天然支持极简初始化。
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        // 返回微信用户对象
        return user;
    }

    /**
     * 调用微信接口，获取微信用户的 openid
     * @param code 授权码
     * @return openid
     */
    private String getOpenid(String code) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", code);
        paramMap.put("grant_type", "authorization_code");

        // 发送 GET 请求到微信接口服务
        String json = HttpClientUtil.doGet(WX_LOGIN, paramMap);

        log.info("微信接口返回结果: {}", json);

        // 解析 JSON 数据并提取 openid
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
