package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.Tag;
import com.ghostfire.mapper.TagMapper;
import com.ghostfire.service.TagService;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}
