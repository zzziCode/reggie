package com.zzzi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzzi.reggie.common.CustomException;
import com.zzzi.reggie.entity.Category;
import com.zzzi.reggie.entity.Dish;
import com.zzzi.reggie.entity.Setmeal;
import com.zzzi.reggie.mapper.CategoryMapper;
import com.zzzi.reggie.service.CategoryService;
import com.zzzi.reggie.service.DishService;
import com.zzzi.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetMealService setMealService;
    //根据id删除没有被关联的分类
    @Override
    public void remove(Long id) {
        //查询当前分类是否关联了菜品
        LambdaQueryWrapper<Dish> dishQueryWrapper=new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId,id);
        int dishCount = dishService.count(dishQueryWrapper);
        //说明当前分类关联了菜品，不能删除
        if(dishCount!=0){
            //需要抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        //查新当前分类是否关联了套餐
        LambdaQueryWrapper<Setmeal> setMealQueryWrapper=new LambdaQueryWrapper<>();
        setMealQueryWrapper.eq(Setmeal::getCategoryId,id);
        int setMealCount = setMealService.count(setMealQueryWrapper);
        //说明当前分类关联了套餐，不能删除
        if(setMealCount!=0){
            //需要抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //在这里说明没有关联菜品或者套餐，此时直接删除
        removeById(id);
    }
}
