package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.dto.DraftDto;
import com.ghostfire.entity.Draft;
import com.ghostfire.service.DraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drafts")
@RequiredArgsConstructor
public class DraftController {

    private final DraftService draftService;

    /** 草稿列表 */
    @GetMapping("/list")
    public Result<List<Draft>> list() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(draftService.listByUserId(userId));
    }

    /** 保存草稿 */
    @PostMapping("/save")
    public Result<Draft> save(@Valid @RequestBody DraftDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        Draft draft = draftService.saveDraft(userId, dto.getCategoryId(), dto.getTitle(), dto.getContent());
        return Result.ok(draft);
    }

    /** 获取草稿详情 */
    @GetMapping("/{id}")
    public Result<Draft> detail(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        Draft draft = draftService.getById(id);
        if (draft == null || !draft.getUserId().equals(userId)) {
            return Result.fail("草稿不存在");
        }
        return Result.ok(draft);
    }

    /** 修改草稿 */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody DraftDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        Draft draft = draftService.getById(id);
        if (draft == null || !draft.getUserId().equals(userId)) {
            return Result.fail("草稿不存在");
        }
        draft.setCategoryId(dto.getCategoryId());
        draft.setTitle(dto.getTitle());
        draft.setContent(dto.getContent());
        draftService.updateById(draft);
        return Result.ok();
    }

    /** 删除草稿 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        Draft draft = draftService.getById(id);
        if (draft == null || !draft.getUserId().equals(userId)) {
            return Result.fail("草稿不存在");
        }
        draftService.removeById(id);
        return Result.ok();
    }
}
