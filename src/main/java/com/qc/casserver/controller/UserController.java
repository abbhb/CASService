package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Ticket;
import com.qc.casserver.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@CrossOrigin("*")
@Slf4j
@RestController//@ResponseBody+@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 登录成功
     * 写入cookie
     * 并且给302状态码
     * 后端直接重定向
     * @param user
     * @return
     */
    @PostMapping("/auth/login")
    public R<UserResult> login(HttpServletResponse response, String service, @RequestBody Map<String, Object> user){
        /**
         * 对密码进行加密传输
         */
        String username = (String) user.get("username");
        String password = (String) user.get("password");
        log.info("{}",response.getStatus());
        UserResult userResult = userService.login(username, password);
        if (StringUtils.isEmpty(userResult.getTgc())){
            return R.error("好奇怪，出错了!");
        }
        log.info("写入{}",userResult.getTgc());
        Cookie cookie = new Cookie("tgc", userResult.getTgc());
        cookie.setMaxAge(3 * 60 * 60); // 后面可以加入7天过期的功能
        cookie.setPath("/");
        response.addCookie(cookie);
        //重定向交给前端吧
        if (!StringUtils.isEmpty(service)){
            userResult.setService(service);
            return R.successOnlyObjectWithStatus(userResult,302);
        }else {
            //正常登录平台
            userResult.setSt(null);
            return R.success(userResult);
        }

    }

    /**
     * 登录成功
     * 写入cookie
     * 并且给302状态码
     * 后端直接重定向
     * @return
     */
    @NeedLogin
    @GetMapping("/auth/loginbytgc")
    public R<UserResult> loginbytgc(HttpServletRequest request, String service){
        log.info(service);
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return R.error("好奇怪，出错了!");
        }
        String tgc = "";
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("tgc")) {
                tgc = cookie.getValue();
                break;
            }
        }
        if (StringUtils.isEmpty(tgc)){
            return R.error("好奇怪，出错了!");
        }
        UserResult userResult = userService.loginbytgc(tgc);
        if (StringUtils.isEmpty(userResult.getTgc())){
            return R.error("好奇怪，出错了!");
        }
        //重定向交给前端吧
        if (!StringUtils.isEmpty(service)){
            userResult.setService(service);
            return R.successOnlyObjectWithStatus(userResult,302);
        }else {
            //正常登录平台
            userResult.setSt(null);
            return R.success(userResult);
        }
    }

    /**
     * SG校验
     * @param ticket
     * @return 返回用户基本信息
     */
    @PostMapping("/auth/sg")
    public R<UserResult> checkST(@RequestBody Ticket ticket){
        if (ticket==null){
            return R.error("访问被拒绝");
        }
        UserResult userResult = userService.checkST(ticket.getSt());
        userResult.setSt(null);
        return R.success(userResult);
    }

}
