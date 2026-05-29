package com.ghostfire.controller.mini;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.dto.ReplyDto;
import com.ghostfire.entity.Comment;
import com.ghostfire.service.CommentService;
import com.ghostfire.vo.CommentMapper;
import com.ghostfire.vo.CommentVO;
import com.ghostfire.vo.VoEnricher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mini/comments")
@RequiredArgsConstructor
public class MiniCommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final VoEnricher voEnricher;

    @GetMapping("/list")
    public Result<Page<CommentVO>> list(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Comment> topPage = commentService.pageTopLevel(postId, page, size);
        List<Comment> topList = topPage.getRecords();

        List<Long> parentIds = topList.stream().map(Comment::getId).toList();
        List<Comment> children = commentService.listChildren(postId, parentIds);
        Map<Long, List<Comment>> childrenMap = children.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        List<Comment> allComments = new ArrayList<>(topList);
        children.forEach(c -> { if (!allComments.contains(c)) allComments.add(c); });
        List<CommentVO> allVos = allComments.stream().map(commentMapper::toVO).toList();
        voEnricher.enrichBatchComments(allVos, allComments);

        Map<Long, CommentVO> voMap = new java.util.HashMap<>();
        for (int i = 0; i < allComments.size(); i++) {
            voMap.put(allComments.get(i).getId(), allVos.get(i));
        }

        List<CommentVO> vos = topList.stream().map(c -> {
            CommentVO vo = voMap.get(c.getId());
            List<Comment> childList = childrenMap.getOrDefault(c.getId(), List.of());
            vo.setChildren(childList.stream().map(ch -> voMap.get(ch.getId())).toList());
            return vo;
        }).toList();

        Page<CommentVO> voPage = new Page<>(topPage.getCurrent(), topPage.getSize(), topPage.getTotal());
        voPage.setRecords(vos);
        return Result.ok(voPage);
    }

    @PostMapping("/create")
    public Result<CommentVO> create(@Valid @RequestBody ReplyDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        Comment comment = commentService.addComment(dto.getPostId(), userId, dto.getParentId(), dto.getReplyUserId(), dto.getContent());
        CommentVO vo = commentMapper.toVO(comment);
        voEnricher.enrich(vo, comment);
        return Result.ok(vo);
    }
}
