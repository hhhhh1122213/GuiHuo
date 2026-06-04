package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.dto.BoastDto;
import com.ghostfire.entity.Boast;
import com.ghostfire.entity.BoastBet;

public interface BoastService extends IService<Boast> {
    Boast create(BoastDto dto, Long userId);

    Page<Boast> list(int page, int size, Integer status);

    Boast detail(Long id);

    BoastBet bet(Long boastId, Integer optionType, Long userId);

    Boast settle(Long id, Integer result, Long userId);
}
