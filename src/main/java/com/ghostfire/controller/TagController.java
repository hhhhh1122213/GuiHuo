package com.ghostfire.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.ghostfire.common.Result;
import com.ghostfire.entity.Tag;
import com.ghostfire.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/list")
    public Result<List<Tag>> list() {
        return Result.ok(tagService.list());
    }
    @SaCheckRole("ADMIN")
    @PostMapping("/create")
    public Result<?> create(Tag tag) {
        return tagService.save(tag) ? Result.ok() : Result.fail("创建失败");
    }
}
