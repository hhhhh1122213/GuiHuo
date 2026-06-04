package com.ghostfire.vo;

import com.ghostfire.entity.Comment;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T08:41:23+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class CommentMapperImpl implements CommentMapper {

    @Override
    public CommentVO toVO(Comment comment) {
        if ( comment == null ) {
            return null;
        }

        CommentVO commentVO = new CommentVO();

        commentVO.setId( comment.getId() );
        commentVO.setPostId( comment.getPostId() );
        commentVO.setParentId( comment.getParentId() );
        commentVO.setContent( comment.getContent() );
        commentVO.setLikeCount( comment.getLikeCount() );
        commentVO.setCreateTime( comment.getCreateTime() );

        return commentVO;
    }

    @Override
    public List<CommentVO> toVOList(List<Comment> comments) {
        if ( comments == null ) {
            return null;
        }

        List<CommentVO> list = new ArrayList<CommentVO>( comments.size() );
        for ( Comment comment : comments ) {
            list.add( toVO( comment ) );
        }

        return list;
    }
}
