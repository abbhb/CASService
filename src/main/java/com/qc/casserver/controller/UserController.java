package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.common.annotation.PermissionCheck;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.Ticket;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.pojo.vo.RegisterUser;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.UserService;

import com.qc.casserver.utils.ParamsCalibration;
import com.qc.casserver.utils.TGTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


@CrossOrigin("*")
@Slf4j
@RestController//@ResponseBody+@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final IRedisService iRedisService;

    public UserController(UserService userService, IRedisService iRedisService) {
        this.userService = userService;
        this.iRedisService = iRedisService;
    }

    /**
     * 登录成功
     * 写入cookie
     * 并且给302状态码
     * 后端直接重定向
     *
     * @param user
     * @return
     */
    @PostMapping("/auth/login")
    public R<UserResult> login(HttpServletResponse response, String service, @RequestBody Map<String, Object> user) {
        /**
         * 对密码进行加密传输
         */
        String username = (String) user.get("username");//用户名可以是用户名也可以用邮箱
        String password = (String) user.get("password");
        log.info("{}", response.getStatus());
        UserResult userResult = userService.login(username, password);
        if (StringUtils.isEmpty(userResult.getTgc())) {
            return R.error("好奇怪，出错了!");
        }
        log.info("写入{}", userResult.getTgc());
        Cookie cookie = new Cookie("tgc", userResult.getTgc());
        cookie.setMaxAge(3 * 60 * 60); // 后面可以加入7天过期的功能
        cookie.setPath("/");
        response.addCookie(cookie);
        //重定向交给前端吧
        if (!ParamsCalibration.haveMust(userResult)) {
            //必要信息不全，必须绑定
            userResult.setSt(null);
            return R.successOnlyObjectWithStatus(userResult, 308);
        }
        if (!StringUtils.isEmpty(service)) {
            if (service.contains("#")) {
                service = service.substring(0, service.lastIndexOf("#"));
            }
            userResult.setService(service);
            return R.successOnlyObjectWithStatus(userResult, 302);
        } else {
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
     *
     * @return
     */
    @NeedLogin
    @GetMapping("/auth/loginbytgc")
    public R<UserResult> loginbytgc(HttpServletRequest request, String service) {
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
        if (StringUtils.isEmpty(tgc)) {
            return R.error("好奇怪，出错了!");
        }
        UserResult userResult = userService.loginbytgc(tgc);
        if (StringUtils.isEmpty(userResult.getTgc())) {
            return R.error("好奇怪，出错了!");
        }
        //重定向交给前端吧
        if (!StringUtils.isEmpty(service)) {
            userResult.setService(service);
            return R.successOnlyObjectWithStatus(userResult, 302);
        } else {
            //正常登录平台
            userResult.setSt(null);
            return R.success(userResult);
        }
    }

    /**
     * SG校验
     *
     * @param ticket
     * @return 返回用户基本信息
     */
    @PostMapping("/auth/sg")
    public R<UserResult> checkST(@RequestBody Ticket ticket) {
        if (ticket == null) {
            return R.error("访问被拒绝");
        }
        UserResult userResult = userService.checkST(ticket.getSt());
        userResult.setSt(null);
        return R.success(userResult);
    }

    /**
     * 后期加入权限过滤器
     *
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @NeedLogin
    @PermissionCheck("1")
    @GetMapping("/get")
    public R<PageData> getUserList(Integer pageNum, Integer pageSize, String name) {
        log.info("pageNum = {},pageSize = {},name = {}", pageNum, pageSize, name);
        return userService.getUserList(pageNum, pageSize, name);
    }


    /**
     * 此接口需要加密
     *
     * @param username
     * @return
     */
    @GetMapping("/hasUserName")
    public R<String> hasUserName(String username) {
        return userService.hasUserName(username);
    }

    /**
     * 删除用户
     *
     * @param id      代操作id
     * @param request
     * @return
     */
    @NeedLogin
    @PermissionCheck("1")
    @DeleteMapping("/delete")
    public R<String> deleteUsers(String id, HttpServletRequest request) {
        log.info("id = {}", id);
        if (StringUtils.isEmpty(id)) {
            return R.error("无操作对象");
        }
        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        if (userId == null) {
            return R.error("删除失败");
        }
        return userService.deleteUsers(id, userId);

    }

    @NeedLogin
    @PermissionCheck("1")
    @PutMapping("/updatauserstatus")
    public R<String> updataUserStatus(HttpServletRequest request, @RequestBody Map<String, Object> user) {
        if (StringUtils.isEmpty((String) user.get("id"))) {
            return R.error("无操作对象");
        }
        if (StringUtils.isEmpty((String) user.get("status"))) {
            return R.error("无操作对象");
        }
        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        if (userId == null) {
            return R.error("无操作对象");
        }
        return userService.updataUserStatus((String) user.get("id"), (String) user.get("status"), userId);
    }

    /**
     * 更新操作(admin)
     * 独立掉 用户自己更新信息方便加权限注解
     * @param user
     * @return
     */
    @NeedLogin
    @PermissionCheck("1")
    @PutMapping("/updataforuser")
    public R<UserResult> updataForUser(@RequestBody User user) {
        log.info("user = {}", user);

        if (user.getId() == null) {
            return R.error("更新失败");
        }
        if (user.getUsername() == null) {
            return R.error("更新失败");
        }
        if (user.getName() == null) {
            return R.error("更新失败");
        }
        if (user.getSex() == null) {
            return R.error("更新失败");
        }
        if (user.getStudentId() == null) {
            return R.error("更新失败");
        }
        if (user.getPhone() == null) {
            return R.error("更新失败");
        }
        if (user.getStatus() == null) {
            return R.error("更新失败");
        }
        if (user.getPermission() == null) {
            return R.error("更新失败");
        }
        return userService.updataForUser(user);
    }


    /**
     * 更新操作(user)
     * 此Api只允许更新自己的信息
     * 独立掉 用户自己更新信息方便加权限注解
     *
     * @param user
     * @return
     */
    @NeedLogin
    @PutMapping("/updataforuserself")
    public R<UserResult> updataForUserSelf(HttpServletRequest request, @RequestBody User user) {
        log.info("user = {}", user);

        if (user.getId() == null) {
            return R.error("更新失败");
        }
        if (user.getUsername() == null) {
            return R.error("更新失败");
        }
        if (user.getName() == null) {
            return R.error("更新失败");
        }
        if (user.getSex() == null) {
            return R.error("更新失败");
        }
        if (user.getStudentId() == null) {
            return R.error("更新失败");
        }
        if (user.getPhone() == null) {
            return R.error("更新失败");
        }
        Long userIdByTGTInRequest = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        log.info("{}", userIdByTGTInRequest);
        if (userIdByTGTInRequest == null) {
            return R.error("更新失败");
        }

        user.setId(userIdByTGTInRequest);
        return userService.updataForUserSelf(user);
    }

    @NeedLogin
    @PermissionCheck("1")
    @PostMapping("/add")
    public R<String> add(@RequestBody RegisterUser user, HttpServletRequest request) {
        System.out.println("user = " + user);
        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        return userService.createUser(user, userId);

    }

    @NeedLogin
    @PostMapping("/emailwithuser")
    public R<String> emailWithUser(@RequestBody Map<String, Object> email, HttpServletRequest request) {
        Long userId = TGTUtil.getUserIdByTGTInRequest(request, iRedisService);
        System.out.println("email = " + email);
        String originEmail = (String) email.get("originEmail");
        String originCode = (String) email.get("originCode");
        String newEmail = (String) email.get("newEmail");
        String newCode = (String) email.get("newCode");
        return userService.emailWithUser(originEmail, originCode, newEmail, newCode, userId);
    }

    @PostMapping("/register")
    public R<String> registerUser(@RequestBody RegisterUser user) {
        System.out.println("user = " + user);
        return userService.registerUser(user);
    }

    @NeedLogin
    @PutMapping("/changepassword")
    public R<UserResult> changePassword(@RequestBody Map<String, Object> user) {
        System.out.println("user = " + user);
        String id = (String) user.get("id");
        String username = (String) user.get("username");
        String password = (String) user.get("password");
        String newpassword = (String) user.get("newpassword");
        String checknewpassword = (String) user.get("checknewpassword");
        return userService.changePassword(id, username, password, newpassword, checknewpassword);
    }

    @NeedLogin
    @PostMapping("/logout")
    public R<UserResult> logout(HttpServletRequest request, HttpServletResponse response) {
        String tgcInRequest = TGTUtil.getTGCInRequest(request);
        Cookie cookie = new Cookie("tgc", "");
        cookie.setMaxAge(0); // 使其过期
        cookie.setPath("/");
        response.addCookie(cookie);
        return userService.logout(tgcInRequest);
    }


}
