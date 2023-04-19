package com.qc.casserver.common.interceptor;


import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


import java.lang.reflect.Method;

@Slf4j
@Component

public class LoginInterceptor implements HandlerInterceptor {
    @Setter
    private UserService userService;

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
//        ThreadLocalUtil.addCurrentUser(user);
        //暂时直接过
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //防止内存泄露，对ThreadLocal里的对象进行清除
//        ThreadLocalUtil.remove();
    }
}
