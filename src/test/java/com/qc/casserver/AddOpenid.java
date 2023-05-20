package com.qc.casserver;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qc.casserver.pojo.entity.User;
import com.qc.casserver.service.UserService;
import com.qc.casserver.utils.RandomName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class AddOpenid {
    @Autowired
    private UserService userService;
    @Test
    void addOpenids() {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getOpenid,"");
        lambdaQueryWrapper.select(User::getId,User::getOpenid);

        List<User> list = userService.list(lambdaQueryWrapper);
        for (User user
             : list) {
            LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(User::getOpenid, RandomName.getUUID());
            userService.update(lambdaUpdateWrapper.eq(User::getId,user.getId()));
        }


    }
}
