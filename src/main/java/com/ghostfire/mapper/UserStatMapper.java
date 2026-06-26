package com.ghostfire.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghostfire.dto.ReconMismatch;
import com.ghostfire.entity.UserStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserStatMapper extends BaseMapper<UserStat> {

    /**
     * 全表对账：找出 user_stat.coin 与 SUM(user_wallet_log.amount) 不一致的用户。
     * 此查询对已有 user_wallet_log 数据的用户有效。
     * 新用户（无任何流水）的 SUM=0, coin=0，不会被返回。
     */
    @Select("SELECT us.user_id AS userId, us.coin AS statCoin, " +
            "COALESCE(SUM(wl.amount), 0) AS sumLogAmount, " +
            "us.coin - COALESCE(SUM(wl.amount), 0) AS diff " +
            "FROM user_stat us " +
            "LEFT JOIN user_wallet_log wl ON us.user_id = wl.user_id " +
            "GROUP BY us.user_id, us.coin " +
            "HAVING us.coin != COALESCE(SUM(wl.amount), 0)")
    List<ReconMismatch> selectReconMismatches();
}
