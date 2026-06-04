package com.ghostfire.vo;

import com.ghostfire.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {

    @Mapping(target = "fromUser", ignore = true)
    @Mapping(target = "toUser", ignore = true)
    MessageVO toVO(Message message);

    List<MessageVO> toVOList(List<Message> messages);
}
