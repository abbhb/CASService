package com.qc.casserver.controller;

import com.qc.casserver.common.R;
import com.qc.casserver.common.annotation.NeedLogin;
import com.qc.casserver.common.annotation.PermissionCheck;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Authorize;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.Ticket;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.pojo.vo.RegisterUser;
import com.qc.casserver.service.IRedisService;
import com.qc.casserver.service.OauthService;
import com.qc.casserver.service.UserService;

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

    private final OauthService oauthService;

    public UserController(UserService userService, IRedisService iRedisService, OauthService oauthService) {
        this.userService = userService;
        this.iRedisService = iRedisService;
        this.oauthService = oauthService;
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
    public R<UserResult> login(HttpServletResponse response, @RequestBody Map<String, Object> user) {
        /**
         * 对密码进行加密传输
         */
        String username = (String) user.get("username");//用户名可以是用户名也可以用邮箱
        String password = (String) user.get("password");
        String responseType = (String) user.get("responseType");
        //redirectUri：如果服务端定义了，就以服务端定义的为准
        String redirectUri = (String) user.get("redirectUri");
        String service = (String) user.get("service");
        String state = (String) user.get("state");
        String clientId = (String) user.get("clientId");
        UserResult userResult = userService.login(username, password);
        if (StringUtils.isEmpty(userResult.getTgc())) {
            return R.error("好奇怪，出错了!");
        }
        log.info("写入{}", userResult.getTgc());
        /**
         * 传入参数
         */
        Authorize authorize = new Authorize();
        authorize.setResponseType(responseType);
        authorize.setRedirectUri(redirectUri);
        authorize.setState(state);
        authorize.setClientId(clientId);
        authorize.setService(service);
        log.info("authorize = {}", authorize);

        return oauthService.loginAggregationReturns(userResult, authorize);


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
    @PostMapping("/auth/loginbytgc")
    public R<UserResult> loginbytgc(HttpServletRequest request,@RequestBody Authorize authorize) {
        log.info(authorize.getRedirectUri());
        String tgc = request.getHeader("tgc");
        if (StringUtils.isEmpty(tgc)) {
            return R.error("好奇怪，出错了!");
        }
        UserResult userResult = userService.loginbytgc(tgc);
        userResult.setTgc(tgc);
        if (StringUtils.isEmpty(userResult.getTgc())) {
            return R.error("好奇怪，出错了!");
        }
        return oauthService.loginAggregationReturns(userResult, authorize);
    }

    /**
     * 此接口被替代
     */
//    /**
//     * ST校验
//     *
//     * @param ticket
//     * @return 返回用户基本信息
//     */
//    @PostMapping("/auth/sg")
//    public R<UserResult> checkST(@RequestBody Ticket ticket) {
//        if (ticket == null) {
//            return R.error("访问被拒绝");
//        }
//        UserResult userResult = userService.checkST(ticket.getSt());
//        userResult.setSt(null);
//        return R.success(userResult);
//    }

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

    @PutMapping("/v1/findPassword")
    public R<String> findPassword(@RequestBody RegisterUser registerUser) {
        log.info("registerUser = {}", registerUser);
        if (StringUtils.isEmpty(registerUser.getEmail())) {
            return R.error("邮箱不能为空");
        }
        if (StringUtils.isEmpty(registerUser.getPassword())) {
            return R.error("密码不能为空");
        }
        if (StringUtils.isEmpty(registerUser.getRePassword())) {
            return R.error("确认密码不能为空");
        }
        if (!registerUser.getPassword().equals(registerUser.getRePassword())) {
            return R.error("两次密码不一致");
        }
        if (StringUtils.isEmpty(registerUser.getMailCode())) {
            return R.error("邮箱验证码不能为空");
        }
        return userService.findPassword(registerUser);
    }

    /**
     * 变更为单点登出
     * 登出该用户下所有的授权
     * @param request
     * @param response
     * @return
     */
    @NeedLogin
    @PostMapping("/logout")
    public R<UserResult> logout(HttpServletRequest request, HttpServletResponse response) {
        String tgcInRequest = TGTUtil.getTGCInRequest(request);
        return userService.logout(tgcInRequest);
    }

    @NeedLogin
    @GetMapping("/logoutSize")
    public R<String> getLogoutSize(HttpServletRequest request, HttpServletResponse response) {
        String tgcInRequest = TGTUtil.getTGCInRequest(request);
        return userService.getLogoutSize(tgcInRequest);
    }


}
