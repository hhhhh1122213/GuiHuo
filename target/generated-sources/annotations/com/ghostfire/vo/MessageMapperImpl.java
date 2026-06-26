package com.ghostfire.vo;

import com.ghostfire.entity.Message;
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
public class MessageMapperImpl implements MessageMapper {

    @Override
    public MessageVO toVO(Message message) {
        if ( message == null ) {
            return null;
        }

        MessageVO messageVO = new MessageVO();

        messageVO.setId( message.getId() );
        messageVO.setContent( message.getContent() );
        messageVO.setStatus( message.getStatus() );
        messageVO.setCreateTime( message.getCreateTime() );

        return messageVO;
    }

    @Override
    public List<MessageVO> toVOList(List<Message> messages) {
        if ( messages == null ) {
            return null;
        }

        List<MessageVO> list = new ArrayList<MessageVO>( messages.size() );
        for ( Message message : messages ) {
            list.add( toVO( message ) );
        }

        return list;
    }
}
