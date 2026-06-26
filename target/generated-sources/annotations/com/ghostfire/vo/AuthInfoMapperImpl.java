package com.ghostfire.vo;

import com.ghostfire.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T17:35:57+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class AuthInfoMapperImpl implements AuthInfoMapper {

    @Override
    public AuthInfoVO toVO(User user) {
        if ( user == null ) {
            return null;
        }

        AuthInfoVO authInfoVO = new AuthInfoVO();

        authInfoVO.setId( user.getId() );
        authInfoVO.setUsername( user.getUsername() );
        authInfoVO.setNickname( user.getNickname() );
        authInfoVO.setAvatar( user.getAvatar() );
        authInfoVO.setRole( user.getRole() );
        authInfoVO.setStatus( user.getStatus() );
        authInfoVO.setCreateTime( user.getCreateTime() );

        return authInfoVO;
    }
}
