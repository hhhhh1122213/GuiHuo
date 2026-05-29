package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 用户主表 */
@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（唯一登录标识） */
    private String username;
    /** 密码（BCrypt 加密） */
    private String password;
    /** 昵称 */
    private String nickname;
    /** 头像 URL */
    private String avatar;
    /** 角色：admin / user */
    private String role;
    /** 状态：1=正常, 0=封禁 */
    private Integer status;
    /** 用户来源：web / wechat */
    private String source;
    /** 微信 openid（仅微信用户） */
    private String openid;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
