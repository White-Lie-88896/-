package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 微信用户端 JWT 令牌校验拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验微信用户 JWT 令牌
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截到的是 Controller 的方法（动态请求）还是其他静态资源（如 HTML, CSS, JS, 图片或 Swagger/Knife4j 页面等）
        // 💡 为什么需要这个判断并直接放行？
        // 1. 静态资源免检：静态资源属于 ResourceHttpRequestHandler 实例，不需要也不应该进行 JWT 校验，直接放行以保证静态资源（如接口文档 doc.html、图片）能正常加载。
        // 2. 避免类型转换异常：防御性设计，防止后续如果需要将 handler 强转为 HandlerMethod（例如获取方法/类上的自定义注解）时发生 ClassCastException。
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        // 1. 从请求头中获取微信用户的令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());

        // 2. 校验令牌
        try {
            log.info("微信用户端 JWT 校验: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前微信用户 id: {}", userId);

            // 将当前微信用户 ID 放入 ThreadLocal 中保存
            BaseContext.setCurrentId(userId);

            // 3. 校验通过，放行
            return true;
        } catch (Exception ex) {
            log.error("微信用户端 JWT 校验失败: {}", ex.getMessage());
            // 4. 校验不通过，响应 401 状态码并拦截
            response.setStatus(401);
            return false;
        }
    }
}
