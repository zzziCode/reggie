package com.zzzi.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzzi.reggie.dto.DishDto;
import com.zzzi.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //保存菜品的同时还需要保存口味
    public void saveWithFlavor(DishDto dishDto);

    public void updateWithFlavor(DishDto dishDto);

    public void deleteWithFlavors(Long[] ids);
}
