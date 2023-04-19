package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController//@ResponseBody+@Controller
@RequestMapping("/cas/user")
@CrossOrigin("*")
@Slf4j
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
    @PostMapping("/login")
    public R<String> login(HttpServletResponse response, @RequestBody Map<String, Object> user){
        /**
         * 对密码进行加密传输
         */
        String username = (String) user.get("username");
        String password = (String) user.get("password");


        return userService.login(username,password);


    }
}
