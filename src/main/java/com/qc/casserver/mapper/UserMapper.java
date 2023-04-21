package com.qc.casserver.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qc.casserver.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends BaseMapper<User> {

//    public List<User> getAllUserIncludeDeleted();
//
//    public User getUserIncludeDeleted(@Param("id") Long id);
}
