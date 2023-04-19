package com.qc.casserver.config;


import com.qc.casserver.common.JacksonObjectMapper;
import com.qc.casserver.common.interceptor.LoginInterceptor;
import com.qc.casserver.common.interceptor.PermissionInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {


    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private PermissionInterceptor permissionInterceptor;
    /**
     * 设置静态资源映射
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {

    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截规则
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/user/login","/user/logout","/swagger-ui/**").order(2);
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/**").excludePathPatterns("/user/login","/user/logout","/swagger-ui/**").order(3);
        // 拦截路径，员工请求的路径都拦截
        //ir.addPathPatterns("/employee/**");
        //ir.addPathPatterns("/store/**");
        // 不拦截路径，如：注册、登录、忘记密码等
    }


    /**
     * 扩展mvc框架的消息转换器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将Java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0,messageConverter);
    }

}
