package com.ghostfire.vo;

import com.ghostfire.entity.UserWalletLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import static com.ghostfire.common.Constant.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletLogMapper {

    @Mapping(target = "typeName", source = "type")
    WalletLogVO toVO(UserWalletLog log);

    List<WalletLogVO> toVOList(List<UserWalletLog> logs);

    default String mapType(String type) {
        return switch (type) {
            case WALLET_POST -> "发帖奖励";
            case WALLET_SIGN_IN -> "签到奖励";
            case WALLET_LIKE -> "点赞奖励";
            case WALLET_RED_PACKET_SEND -> "发红包";
            case WALLET_RED_PACKET_RECEIVE -> "抢红包";
            case WALLET_BOAST_BET -> "吹牛下注";
            case WALLET_BOAST_WIN -> "吹牛获胜";
            default -> type;
        };
    }
}
