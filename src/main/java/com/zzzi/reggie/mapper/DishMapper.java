package com.zzzi.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzzi.reggie.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
