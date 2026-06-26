package com.ghostfire.vo;

import com.ghostfire.entity.UserWalletLog;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T17:35:57+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class WalletLogMapperImpl implements WalletLogMapper {

    @Override
    public WalletLogVO toVO(UserWalletLog log) {
        if ( log == null ) {
            return null;
        }

        WalletLogVO walletLogVO = new WalletLogVO();

        walletLogVO.setTypeName( mapType( log.getType() ) );
        walletLogVO.setCurrentBalance( log.getBalanceAfter() );
        walletLogVO.setId( log.getId() );
        walletLogVO.setAmount( log.getAmount() );
        walletLogVO.setType( mapType( log.getType() ) );
        walletLogVO.setCreateTime( log.getCreateTime() );

        return walletLogVO;
    }

    @Override
    public List<WalletLogVO> toVOList(List<UserWalletLog> logs) {
        if ( logs == null ) {
            return null;
        }

        List<WalletLogVO> list = new ArrayList<WalletLogVO>( logs.size() );
        for ( UserWalletLog userWalletLog : logs ) {
            list.add( toVO( userWalletLog ) );
        }

        return list;
    }
}
