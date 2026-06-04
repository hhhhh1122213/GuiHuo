package com.ghostfire.vo;

import com.ghostfire.entity.Post;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T08:41:23+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Override
    public PostSummaryVO toSummary(Post post) {
        if ( post == null ) {
            return null;
        }

        PostSummaryVO postSummaryVO = new PostSummaryVO();

        postSummaryVO.setId( post.getId() );
        postSummaryVO.setTitle( post.getTitle() );
        postSummaryVO.setContent( post.getContent() );
        postSummaryVO.setViewCount( post.getViewCount() );
        postSummaryVO.setLikeCount( post.getLikeCount() );
        postSummaryVO.setCommentCount( post.getCommentCount() );
        postSummaryVO.setIsTop( post.getIsTop() );
        postSummaryVO.setIsEssence( post.getIsEssence() );
        postSummaryVO.setCreateTime( post.getCreateTime() );
        postSummaryVO.setCategoryId( post.getCategoryId() );

        return postSummaryVO;
    }

    @Override
    public PostDetailVO toDetail(Post post) {
        if ( post == null ) {
            return null;
        }

        PostDetailVO postDetailVO = new PostDetailVO();

        postDetailVO.setId( post.getId() );
        postDetailVO.setUserId( post.getUserId() );
        postDetailVO.setTitle( post.getTitle() );
        postDetailVO.setContent( post.getContent() );
        postDetailVO.setViewCount( post.getViewCount() );
        postDetailVO.setLikeCount( post.getLikeCount() );
        postDetailVO.setCommentCount( post.getCommentCount() );
        postDetailVO.setIsTop( post.getIsTop() );
        postDetailVO.setIsEssence( post.getIsEssence() );
        postDetailVO.setCreateTime( post.getCreateTime() );
        postDetailVO.setUpdateTime( post.getUpdateTime() );
        postDetailVO.setCategoryId( post.getCategoryId() );

        return postDetailVO;
    }
}
