package com.ghostfire.vo;

import com.ghostfire.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "replyUser", ignore = true)
    CommentVO toVO(Comment comment);

    List<CommentVO> toVOList(List<Comment> comments);
}
