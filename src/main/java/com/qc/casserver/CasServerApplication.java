package com.qc.casserver;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qc.casserver.common.MyString;
import com.qc.casserver.mapper.PermissionMapper;
import com.qc.casserver.pojo.entity.Permission;
import com.qc.casserver.service.IRedisService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@Slf4j
public class CasServerApplication implements CommandLineRunner {


    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private IRedisService iRedisService;
    public static void main(String[] args) {
        SpringApplication.run(CasServerApplication.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
        //启动成功执行该方法
        log.info("启动");
        LambdaQueryWrapper<Permission> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<Permission> permissions = permissionMapper.selectList(lambdaQueryWrapper);
        for (Permission permission:
                permissions) {
            iRedisService.hashPut(MyString.permission_key, String.valueOf(permission.getId()),permission);
        }
    }

}
