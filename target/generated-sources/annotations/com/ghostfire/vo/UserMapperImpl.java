package com.ghostfire.vo;

import com.ghostfire.entity.User;
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
public class UserMapperImpl implements UserMapper {

    @Override
    public SimpleUserVO toSimpleVO(User user) {
        if ( user == null ) {
            return null;
        }

        SimpleUserVO simpleUserVO = new SimpleUserVO();

        simpleUserVO.setId( user.getId() );
        simpleUserVO.setNickname( user.getNickname() );
        simpleUserVO.setAvatar( user.getAvatar() );

        return simpleUserVO;
    }

    @Override
    public List<SimpleUserVO> toSimpleVOList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<SimpleUserVO> list = new ArrayList<SimpleUserVO>( users.size() );
        for ( User user : users ) {
            list.add( toSimpleVO( user ) );
        }

        return list;
    }
}
