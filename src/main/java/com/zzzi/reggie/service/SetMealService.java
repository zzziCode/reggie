package com.zzzi.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzzi.reggie.dto.SetmealDto;
import com.zzzi.reggie.entity.Setmeal;

public interface SetMealService extends IService<Setmeal> {
     public void saveWithDish(SetmealDto setmealDto);

     public SetmealDto getSetmealDtoById(Long id);

     public void updateWithFlavors(SetmealDto setmealDto);

     public void deleteWithDish(Long[] ids);
}
