package com.ghostfire.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    PostSummaryVO toSummary(Post post);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "isLiked", ignore = true)
    @Mapping(target = "isFavorited", ignore = true)
    PostDetailVO toDetail(Post post);

    default Page<PostSummaryVO> toSummaryPage(Page<Post> page, List<PostSummaryVO> list) {
        Page<PostSummaryVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(list);
        return result;
    }
}
