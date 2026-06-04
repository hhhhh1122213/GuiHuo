package com.ghostfire.vo;

import com.ghostfire.entity.User;
import com.ghostfire.entity.UserStat;
import org.mapstruct.*;
import org.springframework.lang.Nullable;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthInfoMapper {

    @Mapping(target = "coin", ignore = true)
    @Mapping(target = "postCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "signCount", ignore = true)
    AuthInfoVO toVO(User user);

    default void enrich(AuthInfoVO vo, @Nullable UserStat stat) {
        if (stat == null) return;
        vo.setCoin(stat.getCoin());
        vo.setPostCount(stat.getPostCount());
        vo.setLikeCount(stat.getLikeCount());
        vo.setSignCount(stat.getSignCount());
    }
}
