package com.qc.casserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.qc.casserver.pojo.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
