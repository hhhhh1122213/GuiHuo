package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.Draft;

import java.util.List;

public interface DraftService extends IService<Draft> {

    List<Draft> listByUserId(Long userId);

    Draft saveDraft(Long userId, Long categoryId, String title, String content);
}
