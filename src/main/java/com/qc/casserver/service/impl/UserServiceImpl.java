package com.qc.casserver.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.qc.casserver.common.Code;
import com.qc.casserver.common.CustomException;
import com.qc.casserver.common.MyString;

import com.qc.casserver.common.R;
import com.qc.casserver.mapper.UserMapper;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.Authorize;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.Permission;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.pojo.vo.RegisterUser;
import com.qc.casserver.service.*;
import com.qc.casserver.utils.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;

import static com.qc.casserver.utils.ParamsCalibration.checkSensitiveWords;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    private final IRedisService iRedisService;

    @Autowired
    private InviteCodeService inviteCodeService;
    private final CommonService commonService;

    @Autowired
    private Map<String,Object> usersMap;

    @Autowired
    public UserServiceImpl(IRedisService iRedisService, CommonService commonService) {
        this.iRedisService = iRedisService;
        this.commonService = commonService;
    }


    @Transactional
    @Override
    public boolean removeById(Serializable id) {
        User one = this.getById(id);
        if (one==null){
            throw new CustomException("异常");
        }
        //权限校验
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser==null){
            throw new CustomException("无权限");
        }
        Permission permissionMyId = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(currentUser.getPermission()));
        Permission permissionRemoveId = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(one.getPermission()));
        if (permissionMyId.getWeight()<=permissionRemoveId.getWeight()){
            throw new CustomException("权限不足");
        }

        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(User::getDeleteTime,new Date().getTime());
        lambdaUpdateWrapper.set(User::getIsDeleted,1);
        lambdaUpdateWrapper.eq(User::getId,id);

        return this.update(lambdaUpdateWrapper);
    }

    @Transactional
    @Override
    public boolean removeByMap(Map<String, Object> columnMap) {
        throw new CustomException("暂时禁用此方法异常");
    }

    /**
     * 删除肯定是登录过的用户
     * @param queryWrapper 实体包装类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     * @return
     */
    @Transactional
    @Override
    public boolean remove(Wrapper<User> queryWrapper) {
        User one = this.getOne(queryWrapper);
        if (one==null){
            throw new CustomException("异常");
        }
        //权限校验
        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser==null){
            throw new CustomException("无权限");
        }
        Permission permissionMyId = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(currentUser.getPermission()));
        Permission permissionRemoveId = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(one.getPermission()));
        if (permissionMyId.getWeight()<=permissionRemoveId.getWeight()){
            throw new CustomException("权限不足");
        }
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(User::getDeleteTime,new Date().getTime());
        lambdaUpdateWrapper.set(User::getIsDeleted,1);
        lambdaUpdateWrapper.eq(User::getId,one.getId());
        return this.update(lambdaUpdateWrapper);
    }

    @Transactional
    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            this.removeById(id);
        }
        return true;
    }

    public User getManyUserById(Long id){
        User user = (User) usersMap.get(String.valueOf(id));
        if (user!=null){
            return user;
        }
        User userNew = getById(id);
        if (userNew==null){
            throw new CustomException("异常");
        }
        usersMap.put(String.valueOf(id),userNew);
        return userNew;
    }

    @Override
    public User getById(Serializable id) {
        User user = super.getById(id);
        if (user==null){
            throw new CustomException("异常");
        }
        if (!StringUtils.isEmpty(user.getAvatar())){
            String fileFromMinio = commonService.getFileFromMinio(user.getAvatar());
            user.setAvatar(fileFromMinio);
        }
        return user;
    }

    //重写save方法,校验用户名重复
    //不使用唯一索引是为了逻辑删除后避免用户名不能再次使用
    @Transactional
    @Override
    public boolean save(User entity) {
        if (StringUtils.isEmpty(entity.getUsername())){
            throw new CustomException("err:user:save");
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUsername,entity.getUsername());
        //会自动加上条件判断没有删除
        long count = this.count(lambdaQueryWrapper);//此处不包括逻辑删除的
        if (count> 0L){
            throw new CustomException("用户名已经存在");
        }
        entity.setOpenid(RandomName.getUUID());
        return super.save(entity);
    }

    /**
     * @param username
     * @param password
     * @return TGC
     */
    @Override
    public UserResult login(String username, String password,Integer day30) {
        if (username==null||username.equals("")){
            throw new CustomException("用户名不存在");
        }
        if (password==null||password.equals("")){
            throw new CustomException("密码不存在");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (username.contains("@")){
            //邮箱
            queryWrapper.eq(User::getEmail,username);

        }else {
            queryWrapper.eq(User::getUsername,username);
        }

        User one = this.getOne(queryWrapper);
        if (one==null){
            throw new CustomException("用户名或密码错误");
        }
        String salt = one.getSalt();
        if (!PWDMD5.getMD5Encryption(password,salt).equals(one.getPassword())){//前端传入的明文密码加上后端的盐，处理后跟库中密码比对，一样登陆成功
            throw new CustomException("用户名或密码错误");
        }

        if(one.getStatus() == 0){
            throw new CustomException("账号已禁用!");
        }
        Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(one.getPermission()));
        //生成TGC和TGT ----  通过此方式登录说明该用户在此设备上的此浏览器没有登录过
        String tgt = TicketUtil.addNewTGT(one.getUsername(),one.getId(), one.getPermission());//作为value存于redis
        //返回给浏览器，写入cookie
        String tgc = TicketUtil.addNewTGC(String.valueOf(one.getId()),one.getUsername());
        if (day30==1){
            //长期认证
            iRedisService.addTGCWithTGT(tgc, tgt,30 * 24 * 3600L);//token作为value，id是不允许更改的
        }else {
            iRedisService.addTGCWithTGT(tgc, tgt,12 * 3600L);//token作为value，id是不允许更改的
        }
        UserResult userResult = new UserResult();
        userResult.setId(String.valueOf(one.getId()));
        userResult.setName(one.getName());
        userResult.setEmail(one.getEmail());
        userResult.setTgc(tgc);
        userResult.setStudentId(String.valueOf(one.getStudentId()));
        userResult.setUsername(one.getUsername());
        userResult.setCreateTime(one.getCreateTime());
        userResult.setUpdateTime(one.getUpdateTime());
        userResult.setPermission(one.getPermission());
        userResult.setAvatar(one.getAvatar());
        userResult.setStatus(one.getStatus());
        userResult.setPermissionName(permission.getName());
        userResult.setPhone(one.getPhone());
        userResult.setSex(one.getSex());

        return userResult;
    }

    @Override
    public UserResult loginbytgc(String tgc) {
        if (StringUtils.isEmpty(tgc)){
            throw new CustomException("出错了");
        }
        String tgt = iRedisService.getTGT(tgc);
        if (StringUtils.isEmpty(tgt)){
            throw new CustomException("需要认证");
        }
        String userIdByTGT = TicketUtil.getUserIdByTGT(tgt);
        User one = getById(userIdByTGT);
        if (one==null){
            throw new CustomException("需要认证");
        }

        if(one.getStatus() == 0){
            throw new CustomException("账号已禁用!");
        }
        Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(one.getPermission()));
        iRedisService.addTGCWithTGT(tgc, tgt,12*3600L);//token作为value，id是不允许更改的
        //15过期的st,防止网络缓慢
        UserResult userResult = new UserResult();
        userResult.setId(String.valueOf(one.getId()));
        userResult.setName(one.getName());
        userResult.setEmail(one.getEmail());
        userResult.setTgc(tgc);
        userResult.setStudentId(String.valueOf(one.getStudentId()));
        userResult.setUsername(one.getUsername());
        userResult.setCreateTime(one.getCreateTime());
        userResult.setUpdateTime(one.getUpdateTime());
        userResult.setPermission(one.getPermission());
        userResult.setPermissionName(permission.getName());
        userResult.setPhone(one.getPhone());
        userResult.setSex(one.getSex());
        userResult.setAvatar(one.getAvatar());
        userResult.setStatus(one.getStatus());
        return userResult;
    }



//    @Override
//    public UserResult checkST(String st) {
//        log.info(st);
//        if (StringUtils.isEmpty(st)){
//            throw new CustomException("认证失败");
//        }
//        String serviceValue= iRedisService.getSTValue(st);
//        log.info("st={},stvalue={}",st,serviceValue);
//        if (StringUtils.isEmpty(serviceValue)){
//            throw new CustomException("认证失败");
//        }
//        String userId = TicketUtil.getUserId(st);
//        User one = getById(Long.valueOf(userId));
//        if (one==null){
//            throw new CustomException("业务异常;");
//        }
//        Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(one.getPermission()));
//
//        UserResult userResult = new UserResult();
//        userResult.setId(String.valueOf(one.getId()));
//        userResult.setName(one.getName());
//        userResult.setEmail(one.getEmail());
//        userResult.setStudentId(String.valueOf(one.getStudentId()));
//        userResult.setUsername(one.getUsername());
//        userResult.setCreateTime(one.getCreateTime());
//        userResult.setUpdateTime(one.getUpdateTime());
//        userResult.setPermission(one.getPermission());
//        userResult.setPermissionName(permission.getName());
//        userResult.setPhone(one.getPhone());
//        userResult.setAvatar(one.getAvatar());
//        userResult.setStatus(one.getStatus());
//        userResult.setSex(one.getSex());
//
//        return userResult;
//    }

    @Override
    public R<PageData> getUserList(Integer pageNum, Integer pageSize, String name) {
        if (pageNum==null){
            return R.error("传参错误");
        }
        if (pageSize==null){
            return R.error("传参错误");
        }
        Page pageInfo = new Page(pageNum,pageSize);
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByAsc(User::getId);
        //添加过滤条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),User::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(User::getCreateTime);//按照创建时间排序
        this.page(pageInfo,lambdaQueryWrapper);
        PageData<UserResult> pageData = new PageData<>();
        List<UserResult> results = new ArrayList<>();
        for (Object user : pageInfo.getRecords()) {
            User user1 = (User) user;
            Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(user1.getPermission()));

            UserResult userResult = new UserResult();
            userResult.setId(String.valueOf(user1.getId()));
            userResult.setUsername(user1.getUsername());
            userResult.setName(user1.getName());
            userResult.setPhone(user1.getPhone());
            userResult.setSex(user1.getSex());
            userResult.setStudentId(String.valueOf(user1.getStudentId()));
            userResult.setStatus(user1.getStatus());
            userResult.setCreateTime(user1.getCreateTime());
            userResult.setUpdateTime(user1.getUpdateTime());
            userResult.setPermission(user1.getPermission());
            userResult.setPermissionName(permission.getName());
            userResult.setEmail(user1.getEmail());
            userResult.setAvatar(user1.getAvatar());
            results.add(userResult);
        }
        pageData.setPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        pageData.setCountId(pageInfo.getCountId());
        pageData.setCurrent(pageInfo.getCurrent());
        pageData.setSize(pageInfo.getSize());
        pageData.setRecords(results);
        pageData.setMaxLimit(pageInfo.getMaxLimit());
        return R.success(pageData);
    }

//    @Override
//    public R<UserResult> loginByEmail(String email, String password) {
//        if (email==null||email.equals("")){
//            return R.error("邮箱不存在");
//        }
//        if (password==null||password.equals("")){
//            return R.error("密码不存在");
//        }
//        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(User::getEmail,email);
//        User one = this.getOne(queryWrapper);
//        if (one==null){
//            return R.error("用户名或密码错误");
//        }
//        String salt = one.getSalt();
//        if (!PWDMD5.getMD5Encryption(password,salt).equals(one.getPassword())){//前端传入的明文密码加上后端的盐，处理后跟库中密码比对，一样登陆成功
//            return R.error("用户名或密码错误");
//        }
//
//        if(one.getStatus() == 0){
//            return R.error("账号已禁用!");
//        }
//        //jwt生成token，token里面有userid，redis里存uuid
//        String uuid = RandomName.getUUID();//uuid作为key
//
//        Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(one.getPermission()));
//
//
//        String token = JWTUtil.getToken(String.valueOf(one.getId()),String.valueOf(one.getPermission()),uuid);
//        iRedisService.setTokenWithTime(uuid, String.valueOf(one.getId()),3600L);//token作为value，id是不允许更改的
//        UserResult UserResult = new UserResult(String.valueOf(one.getId()),one.getUsername(),one.getName(),one.getPhone(),one.getSex(),String.valueOf(one.getStudentId()),one.getStatus(),one.getCreateTime(),one.getUpdateTime(),one.getPermission(),permission.getName(),token,one.getEmail(),one.getAvatar());
//        return R.success(UserResult);
//    }

    @Transactional
    @Override
    public R<String> createUser(RegisterUser user, Long userId) {
        if (user.getPermission()==null){
            user.setPermission(2);
        }
        if (StringUtils.isEmpty(user.getName())){
            user.setName(RandomName.getUUID());
        }
        if (StringUtils.isEmpty(user.getSex())){
            user.setSex("未知");
        }

        if (StringUtils.isEmpty(user.getUsername())){
            throw new CustomException("yichang");
        }
        if (StringUtils.isEmpty(user.getPassword())){
            throw new CustomException("password");
        }
        if (!StringUtils.isEmpty(user.getEmail())){
            throw new CustomException("参数异常");
        }
        if (user.getUsername().contains("@")){
            throw new CustomException("不可包含'@'");
        }
        if (!user.getPassword().matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$")){
            return R.error("密码必须字母加数字,8-16位");
        }
        User byId = getById(userId);
        Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(user.getPermission()));
        Permission permissionMyId = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(byId.getPermission()));
        if (permissionMyId.getWeight()<=permission.getWeight()){
            return R.error("权限不足");
        }
        checkSensitiveWords(user.getName());
        String password = user.getPassword();
        String salt = PWDMD5.getSalt();
        String md5Encryption = PWDMD5.getMD5Encryption(password,salt);
        user.setPassword(md5Encryption);
        user.setSalt(salt);
        User user1 = new User();
        BeanUtils.copyProperties(user,user1);
        log.info("{}",user1);
        boolean save = this.save(user1);
        if (save){
            return R.success("创建成功");
        }
        throw new CustomException("yichang");
    }
    @Transactional
    @Override
    public R<String> registerUser(RegisterUser user) {

        user.setPermission(2);
        if (StringUtils.isEmpty(user.getName())){
            user.setName("亲爱的用户请改名");
        }
        if (StringUtils.isEmpty(user.getSex())){
            user.setSex("未知");
        }

        if (StringUtils.isEmpty(user.getUsername())){
            throw new CustomException("yichang");
        }
        if (StringUtils.isEmpty(user.getPassword())){
            throw new CustomException("password");
        }

        if (StringUtils.isEmpty(user.getInviteCode())){
            throw new CustomException("邀请码缺少");
        }
        if (user.getUsername().contains("@")){
            throw new CustomException("不可包含'@'");
        }
        if (!user.getPassword().matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$")){
            return R.error("密码必须字母加数字,8-16位");
        }

        //校验邀请码
        boolean isTrue = inviteCodeService.useInviteCode(user.getInviteCode());
        if (!isTrue){
            throw new CustomException("邀请码错误");
        }

        checkSensitiveWords(user.getName());
        if (StringUtils.isEmpty(user.getEmail())){
            throw new CustomException("邮箱缺少");
        }
        if (StringUtils.isEmpty(user.getMailCode())){
            throw new CustomException("邮箱验证码缺少");
        }

        String password = user.getPassword();
        String salt = PWDMD5.getSalt();
        String md5Encryption = PWDMD5.getMD5Encryption(password,salt);
        user.setPassword(md5Encryption);
        user.setSalt(salt);
        if (StringUtils.isEmpty(user.getEmail())){
            throw new CustomException("邮箱缺少");
        }
        String mailCode = iRedisService.getValue(MyString.pre_email_redis+user.getEmail());
        if (StringUtils.isEmpty(mailCode)||!mailCode.equals(user.getMailCode())){
            throw new CustomException("邮箱验证码错误");
        }

        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(User::getEmail,null);
        lambdaUpdateWrapper.eq(User::getEmail,user.getEmail());
        this.update(lambdaUpdateWrapper);//对之前绑定过这个邮箱的号解绑
        User user1 = new User();
        BeanUtils.copyProperties(user,user1);
        log.info("{}",user1);
        boolean save = this.save(user1);
        if (save){
            iRedisService.del(MyString.pre_email_redis+user.getEmail());
            return R.success("创建成功");
        }
        throw new CustomException("异常");
    }

    @Override
    public R<UserResult> logout(String tgc) {

        String tgt = iRedisService.getTGT(tgc);
        if (StringUtils.isEmpty(tgt)) {
            return R.error(Code.DEL_TOKEN,"登陆过期");
        }
        String userIdByTGT = TicketUtil.getUserIdByTGT(tgt);
        if (StringUtils.isEmpty(userIdByTGT)) {
            return R.error(Code.DEL_TOKEN,"登陆过期");
        }
        Set<String> logout = iRedisService.getLogout(userIdByTGT);
        for (String log:
             logout) {
            iRedisService.del(log);
        }

        iRedisService.delLogout(Long.valueOf(userIdByTGT));
        iRedisService.del(tgc);
        return R.error(Code.DEL_TOKEN,"单点登出成功");

    }
//
//    @Override
//    public R<UserResult> loginByToken(String token) {
//        DecodedJWT decodedJWT = JWTUtil.deToken(token);
//        Claim uuid = decodedJWT.getClaim("uuid");
//        String value = iRedisService.getValue(uuid.asString());
//        if (StringUtils.isEmpty(value)) {
//            return R.error("登录过期");
//        }
//        User one =getById(Long.valueOf(value));
//        if (one==null){
//            return R.error("err");
//        }
//        Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(one.getPermission()));
//
//        UserResult UserResult = new UserResult(String.valueOf(one.getId()),one.getUsername(),one.getName(),one.getPhone(),one.getSex(),String.valueOf(one.getStudentId()),one.getStatus(),one.getCreateTime(),one.getUpdateTime(),one.getPermission(),permission.getName(),token,one.getEmail(),one.getAvatar());
//        return R.success(UserResult);
//    }
//
    @Transactional
    @Override
    public R<String> updataUserStatus(String id,String status, Long userId) {
        if (StringUtils.isEmpty(id)){
            return R.error("无操作对象");
        }
        if (StringUtils.isEmpty(status)){
            return R.error("无操作对象");
        }
        if (userId==null){
            return R.error("无操作对象");
        }
        User myId = getById(userId);
        if (myId==null){
            //don't hava object
            throw new CustomException("没有对象");
        }
        //权限校验
        Permission permissionMyId = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(myId.getPermission()));
        List<User> users = new ArrayList<>();
        boolean update = false;
        if (id.contains(",")){
            String[] split = id.split(",");
            for (String s:
                    split) {
                User byId = getById(Long.valueOf(s));
                if (byId==null){
                    //don't hava object
                    throw new CustomException("没有对象");
                }
                Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(byId.getPermission()));
                if (permissionMyId.getWeight()<=permission.getWeight()){
                    throw new CustomException("权限不足");
                }
                if (userId.equals(Long.valueOf(s))){
                    throw new CustomException("禁止操作自己账号");
                }
                User user = new User();
                user.setId(Long.valueOf(s));
                user.setStatus(Integer.valueOf(status));
                users.add(user);
            }
            update = this.updateBatchById(users);
        }else {
            User byId = getById(Long.valueOf(id));
            if (byId==null){
                //don't hava object
                throw new CustomException("没有对象");
            }
            Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(byId.getPermission()));
            if (permissionMyId.getWeight()<=permission.getWeight()){
                throw new CustomException("权限不足");
            }
            if (userId.equals(Long.valueOf(id))){
                return R.error("禁止操作自己账号");
            }
            LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(User::getStatus,Integer.valueOf(status));
            lambdaUpdateWrapper.eq(User::getId,Long.valueOf(id));
            update = this.update(lambdaUpdateWrapper);
        }
        if (update){
            return R.success("更改成功");
        }
        return R.error("无操作对象");
    }

    @Transactional
    @Override
    public R<UserResult> updataForUser(User user) {
        if (user.getId()==null){
            return R.error("更新失败");
        }
        if (user.getUsername()==null){
            return R.error("更新失败");
        }
        if (user.getName()==null){
            return R.error("更新失败");
        }
        if (user.getSex()==null){
            return R.error("更新失败");
        }if (user.getStudentId()==null){
            return R.error("更新失败");
        }
        if (user.getPhone()==null){
            return R.error("更新失败");
        }

        if (user.getStudentId()>999999999999L){
            return R.error("不能超过12位学号");
        }
        if (user.getId().equals(1L)){
            return R.error("禁止操作admin");
        }

        User currentUser = ThreadLocalUtil.getCurrentUser();
        if (currentUser==null){
            throw new CustomException("无权限");
        }
        User one = this.getById(user.getId());
        Permission permissionMyId = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(currentUser.getPermission()));
        Permission permissionRemoveId = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(one.getPermission()));
        if (permissionMyId.getWeight()<=permissionRemoveId.getWeight()){
            throw new CustomException("权限不足");
        }

        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getId,user.getId());
        lambdaUpdateWrapper.set(User::getName,user.getName());
        lambdaUpdateWrapper.set(User::getStudentId,user.getStudentId());
        lambdaUpdateWrapper.set(User::getUsername,user.getUsername());
        lambdaUpdateWrapper.set(User::getSex,user.getSex());
        lambdaUpdateWrapper.set(User::getPermission,user.getPermission());
        lambdaUpdateWrapper.set(User::getStatus,user.getStatus());
        lambdaUpdateWrapper.set(User::getPhone,user.getPhone());
        lambdaUpdateWrapper.set(User::getAvatar,user.getAvatar());
        boolean update = this.update(lambdaUpdateWrapper);
        if (update){
            return R.successOnlyMsg("更新成功");
        }
        return R.error("err");
    }
    @Transactional
    @Override
    public R<UserResult> updataForUserSelf(User user) {
        if (user.getId()==null){
            return R.error("更新失败");
        }
        if (user.getUsername()==null){
            return R.error("更新失败");
        }
        if (user.getName()==null){
            return R.error("更新失败");
        }
        if (user.getSex()==null){
            return R.error("更新失败");
        }if (user.getStudentId()==null){
            return R.error("更新失败");
        }
        if (user.getPhone()==null){
            return R.error("更新失败");
        }
        if (user.getStudentId()>999999999999L){
            return R.error("不能超过12位学号");
        }
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getId,user.getId());
        lambdaUpdateWrapper.set(User::getName,user.getName());
        lambdaUpdateWrapper.set(User::getStudentId,user.getStudentId());
        lambdaUpdateWrapper.set(User::getUsername,user.getUsername());
        lambdaUpdateWrapper.set(User::getSex,user.getSex());
        lambdaUpdateWrapper.set(User::getPhone,user.getPhone());
        lambdaUpdateWrapper.set(User::getAvatar,user.getAvatar());
        boolean update = this.update(lambdaUpdateWrapper);
        if (update){
            return R.successOnlyMsg("更新成功");
        }
        return R.error("err");
    }

    @Transactional
    @Override
    public R<UserResult> changePassword(String id, String username, String password, String newpassword, String checknewpassword) {
        if (id==null){
            return R.error(Code.DEL_TOKEN,"环境异常,强制下线");
        }
        if (username==null){
            return R.error(Code.DEL_TOKEN,"环境异常,强制下线");
        }
        if (password==null){
            return  R.error("请输入原密码");
        }
        if (newpassword==null){
            return R.error("请输入新密码");
        }
        if (checknewpassword==null){
            return R.error("请输入确认密码");
        }
        if (!newpassword.equals(checknewpassword)){
            return R.error("两次密码不一致!");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId,Long.valueOf(id)).eq(User::getUsername,username);
        User one = this.getOne(queryWrapper);
        if (one==null){
            return R.error(Code.DEL_TOKEN,"环境异常,强制下线");
        }
        String salt = one.getSalt();
        if (!PWDMD5.getMD5Encryption(password,salt).equals(one.getPassword())){//前端传入的明文密码加上后端的盐，处理后跟库中密码比对，一样登陆成功
            return R.error("原密码错误");
        }

        String newSalt = PWDMD5.getSalt();
        String newMD5Password = PWDMD5.getMD5Encryption(newpassword,newSalt);
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(User::getId,Long.valueOf(id)).eq(User::getUsername,username);
        User user = new User();
        user.setId(Long.valueOf(id));
        user.setUsername(username);
        user.setPassword(newMD5Password);
        user.setSalt(newSalt);
//        employee.setUpdateUser(Long.valueOf(id));
        //操作数据库更新密码和盐
        boolean update = this.update(user, lambdaUpdateWrapper);
        if (update){
            return R.successOnlyMsg("修改成功");
        }

        return R.error("修改失败");
    }
//
//    @Override
//    public R<String> updataUser(String userid, String name, String username, String phone, String idNumber, String status, String grouping, String sex, String token) {
//        return null;
//    }
//
//    @Override
//    public R<PageData> getUserList(Integer pageNum, Integer pageSize, String name, Long userId) {
//        if (pageNum==null){
//            return R.error("传参错误");
//        }
//        if (pageSize==null){
//            return R.error("传参错误");
//        }
//        if (userId==null){
//            throw new CustomException("业务异常");
//        }
//        User byId = getById(userId);
//        if (byId==null){
//            throw new CustomException("业务异常");
//        }
//
//
//        if (byId.getPermission()==2){
//            //当前是User身份,不返回数据
//            return R.error("你好像没权限欸!");
//        }
//        Page pageInfo = new Page(pageNum,pageSize);
//        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        //添加过滤条件
//        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),User::getName,name);
//        //添加排序条件
//        lambdaQueryWrapper.orderByAsc(User::getCreateTime);//按照创建时间排序
//        this.page(pageInfo,lambdaQueryWrapper);
//        PageData<UserResult> pageData = new PageData<>();
//        List<UserResult> results = new ArrayList<>();
//        for (Object user : pageInfo.getRecords()) {
//            User user1 = (User) user;
//            Permission permission = (Permission) iRedisService.getHash(MyString.permission_key, String.valueOf(user1.getPermission()));
//
//            UserResult userResult = new UserResult(String.valueOf(user1.getId()),user1.getUsername(),user1.getName(),user1.getPhone(),user1.getSex(),String.valueOf(user1.getStudentId()),user1.getStatus(),user1.getCreateTime(),user1.getUpdateTime(),user1.getPermission(),permission.getName(),null,user1.getEmail(),user1.getAvatar());
//            results.add(userResult);
//        }
//        pageData.setPages(pageInfo.getPages());
//        pageData.setTotal(pageInfo.getTotal());
//        pageData.setCountId(pageInfo.getCountId());
//        pageData.setCurrent(pageInfo.getCurrent());
//        pageData.setSize(pageInfo.getSize());
//        pageData.setRecords(results);
//        pageData.setMaxLimit(pageInfo.getMaxLimit());
//        return R.success(pageData);
//    }
//
    @Transactional
    @Override
    public R<String> deleteUsers(String id,Long userId) {
        if (StringUtils.isEmpty(id)){
            return R.error("无操作对象");
        }
        if (userId==null){
            throw new CustomException("环境异常");
        }

        Collection<Long> ids = new ArrayList<>();
        if (id.contains(",")){
            String[] split = id.split(",");
            for (String s:
                    split) {
                if (s.equals("1")){
                    throw new CustomException("admin不可删除");
                }
                ids.add(Long.valueOf(s));
            }
            this.removeByIds(ids);
        }else {
            if (Long.valueOf(id).equals(1L)){
                throw new CustomException("admin不可删除");
            }
            this.removeById(Long.valueOf(id));
        }
        return R.success("删除成功");
    }

    @Override
    public R<String> hasUserName(String username) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUsername,username);
        int count = this.count(userLambdaQueryWrapper);
        if (count==0){
            return R.success("可用");
        }
        return R.error("请换一个用户名试试!");

    }
//
    @Transactional
    @Override
    public R<String> emailWithUser(String originEmail, String originCode,String newEmail, String newCode,Long userId) {
        if (StringUtils.isEmpty(newEmail)||StringUtils.isEmpty(newCode)||userId==null){
            throw new CustomException("参数异常");
        }
        if (originEmail.equals(newEmail)){
            throw new CustomException("？？？什么操作");
        }
        //校验新邮箱验证码
        if (!iRedisService.getValue(MyString.pre_email_redis+newEmail).equals(newCode)){
            throw new CustomException("新邮箱验证码错误");
        }
        //判断当前用户是否以前绑定过邮箱
        User byId = getById(userId);
        if (byId==null){
            throw new CustomException("用户不存在");
        }
        if (!StringUtils.isEmpty(byId.getEmail())){
            if (StringUtils.isEmpty(originEmail)||StringUtils.isEmpty(originCode)){
                throw new CustomException("参数异常");
            }
            //校验原邮箱验证码
            if (!iRedisService.getValue(MyString.pre_email_redis+originEmail).equals(originCode)){
                throw new CustomException("原邮箱验证码错误");
            }
        }
        //对之前绑定过这个邮箱的号解绑
        LambdaUpdateWrapper<User> lambdaUpdateWrappers = new LambdaUpdateWrapper<>();
        lambdaUpdateWrappers.set(User::getEmail,null);
        lambdaUpdateWrappers.eq(User::getEmail,newEmail);
        this.update(lambdaUpdateWrappers);

        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(User::getEmail,newEmail);
        lambdaUpdateWrapper.eq(User::getId,userId);
        boolean update = this.update(lambdaUpdateWrapper);
        if (update){
            //删除验证码
            iRedisService.del(MyString.pre_email_redis+newEmail);
            return R.success("绑定成功");
        }
        return R.error("异常");
    }

    @Transactional
    @Override
    public R<String> findPassword(RegisterUser registerUser) {
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
        //校验邮箱验证码
        String value = iRedisService.getValue(MyString.pre_email_redis + registerUser.getEmail());
        if (StringUtils.isEmpty(value)) {
            return R.error("邮箱验证码已过期");
        }
        if (!value.equals(registerUser.getMailCode())) {
            return R.error("邮箱验证码错误");
        }
        //校验邮箱是否存在
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getEmail, registerUser.getEmail());
        User one = this.getOne(lambdaQueryWrapper);
        if (one == null) {
            return R.error("邮箱不存在");
        }
        //修改密码
        LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        String salt = PWDMD5.getSalt();
        String md5Encryption = PWDMD5.getMD5Encryption(registerUser.getPassword(), salt);
        lambdaUpdateWrapper.set(User::getPassword,md5Encryption);
        lambdaUpdateWrapper.set(User::getSalt,salt);
        lambdaUpdateWrapper.eq(User::getId, one.getId());
        boolean update = this.update(lambdaUpdateWrapper);
        if (update) {
            //删除验证码
            iRedisService.del(MyString.pre_email_redis + registerUser.getEmail());
            return R.success("修改成功");
        }
        return R.success("修改失败");
    }

    @Override
    public R<String> getLogoutSize(String tgcInRequest) {
        String tgt = iRedisService.getTGT(tgcInRequest);
        if (StringUtils.isEmpty(tgt)){
            return R.error("未登录");
        }
        String userIdByTGT = TicketUtil.getUserIdByTGT(tgt);
        if (StringUtils.isEmpty(userIdByTGT)){
            return R.error("未登录");
        }


        Long logoutSize = iRedisService.getLogoutSize(userIdByTGT);
        return R.success(logoutSize.toString());
    }

}
