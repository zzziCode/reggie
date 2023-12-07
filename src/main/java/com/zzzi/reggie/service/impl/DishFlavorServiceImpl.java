package com.zzzi.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzzi.reggie.entity.DishFlavor;
import com.zzzi.reggie.mapper.DishFlavorMapper;
import com.zzzi.reggie.service.DishFlavorService;
import com.zzzi.reggie.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor>implements DishFlavorService {
}
