package com.limengyuan.partner.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.limengyuan.partner.common.entity.Admin;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 管理员 Mapper
 */
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 根据用户名查找管理员
     */
    @Select("SELECT * FROM admins WHERE username = #{username}")
    Admin findByUsername(String username);

    /**
     * 更新最后登录时间
     */
    @Update("UPDATE admins SET last_login_at = NOW() WHERE admin_id = #{adminId}")
    void updateLastLoginTime(Long adminId);
}
