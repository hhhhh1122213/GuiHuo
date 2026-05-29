package com.ghostfire.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghostfire.config.WxConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxService {

    private final WxConfig wxConfig;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用微信 jscode2session 接口，获取 openid 和 session_key
     * @param code 小程序 wx.login() 获取的临时凭证
     * @return openid
     */
    public String getOpenid(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wxConfig.getAppId(), wxConfig.getAppSecret(), code);
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);
            if (json.has("openid")) {
                return json.get("openid").asText();
            }
            String errMsg = json.has("errmsg") ? json.get("errmsg").asText() : "unknown error";
            log.error("WeChat jscode2session failed: {}", errMsg);
            throw new RuntimeException("微信登录失败: " + errMsg);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("WeChat jscode2session request error", e);
            throw new RuntimeException("微信登录请求失败", e);
        }
    }
}
