package com.ghostfire.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghostfire.entity.UserCheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface UserCheckInMapper extends BaseMapper<UserCheckIn> {

    boolean existsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
