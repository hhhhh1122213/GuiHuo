package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.Category;
import com.ghostfire.mapper.CategoryMapper;
import com.ghostfire.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
