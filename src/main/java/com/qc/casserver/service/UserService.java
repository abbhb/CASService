package com.qc.casserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qc.casserver.common.R;
import com.qc.casserver.pojo.UserResult;
import com.qc.casserver.pojo.entity.PageData;
import com.qc.casserver.pojo.entity.User;


public interface UserService extends IService<User> {
    UserResult login(String username, String password);

    UserResult loginbytgc(String tgc);

    UserResult checkST(String st);

    R<PageData> getUserList(Integer pageNum, Integer pageSize, String name);


//    R<UserResult> loginByEmail(String email, String password);
//
    R<String> createUser(User user, Long userId);
//
    R<UserResult> logout(String tgc);
//
//    R<UserResult> loginByToken(String token);
//
    R<String> updataUserStatus(String id,String status, Long userId);
//
    R<UserResult> updataForUser(User user);
//
    R<UserResult> updataForUserSelf(User user);
//
//    R<UserResult> changePassword(String id, String username, String password, String newpassword, String checknewpassword);
//
//    R<String> updataUser(String userid, String name, String username, String phone, String idNumber, String status, String grouping, String sex, String token);
//
//
    R<String> deleteUsers(String id,Long userId);
//
    R<String> hasUserName(String username);
//
//    R<String> emailWithUser(String emails, String code, String token);

}
