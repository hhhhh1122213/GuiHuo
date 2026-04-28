package com.ghostfire.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghostfire.entity.UserStat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserStatMapper extends BaseMapper<UserStat> {
}
