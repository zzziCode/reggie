package com.zzzi.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzzi.reggie.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;

@Mapper
public interface SetMealDishMapper extends BaseMapper<SetmealDish> {
}
