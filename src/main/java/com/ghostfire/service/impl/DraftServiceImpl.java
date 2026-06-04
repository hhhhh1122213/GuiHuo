package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.Draft;
import com.ghostfire.mapper.DraftMapper;
import com.ghostfire.service.DraftService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DraftServiceImpl extends ServiceImpl<DraftMapper, Draft> implements DraftService {

    @Override
    public List<Draft> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<Draft>()
                .eq(Draft::getUserId, userId)
                .orderByDesc(Draft::getUpdateTime));
    }

    @Override
    public Draft saveDraft(Long userId, Long categoryId, String title, String content) {
        Draft draft = new Draft();
        draft.setUserId(userId);
        draft.setCategoryId(categoryId);
        draft.setTitle(title);
        draft.setContent(content);
        save(draft);
        return draft;
    }
}
