package com.qc.casserver.common.interceptor;


import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.UserService;

import com.qc.casserver.utils.ThreadLocalUtil;
import com.qc.casserver.utils.TicketUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private IRedisService iRedisService;



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            System.out.println("OPTIONS请求，放行");
            return true;
        }
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if (method.getAnnotation(NeedLogin.class) == null){
            //如果没有前置条件 需要登陆
            //权限校验直接通过
            return true;
        }
        //校验校验用户localstorage中携带的TGC 认证成功才给通过
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        String tgc = "";
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("tgc")) {
                tgc = cookie.getValue();
                break;
            }
        }
        if (StringUtils.isEmpty(tgc)){
            return false;
        }

        String tgt = iRedisService.getValue(tgc);
        log.info("tgt={},tgc={}",tgt,tgc);
        if (StringUtils.isEmpty(tgt)){
            return false;
        }
        String userId = TicketUtil.getUserIdByTGT(tgt);
        Integer userPermission = TicketUtil.getUserPermissionByTGT(tgt);

        //到了这里已经是登录的了,现在在这里做个优化,如果此时快过期了，可以无感知更新下票的有效期(注意同步更新前端的票)
        if (iRedisService.getTokenTTL(tgc)<1500L){
            iRedisService.setTTL(tgc,3*3600L);
            Cookie cookie = new Cookie("tgc", tgc);
            cookie.setMaxAge(3 * 60 * 60); // 后面可以加入7天过期的功能,刷新cookie
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        User user = new User();
        user.setId(Long.valueOf(userId));
        user.setPermission(userPermission);
        ThreadLocalUtil.addCurrentUser(user);

        //暂时直接过
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //防止内存泄露，对ThreadLocal里的对象进行清除
        ThreadLocalUtil.remove();
    }
}
