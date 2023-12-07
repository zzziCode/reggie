package com.zzzi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzzi.reggie.common.CustomException;
import com.zzzi.reggie.dto.DishDto;
import com.zzzi.reggie.entity.Dish;
import com.zzzi.reggie.entity.DishFlavor;
import com.zzzi.reggie.mapper.DishMapper;
import com.zzzi.reggie.service.DishFlavorService;
import com.zzzi.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    //保存菜品及其对应的口味
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //先保存菜品
        save(dishDto);

        //保存完毕之后，菜品的id就生成好了
        Long dishId = dishDto.getId();

        //将口味与其菜品id进行关联
        List<DishFlavor> flavors = dishDto.getFlavors();
        //每一个口味都关联上当前菜品
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);

        }

        dishFlavorService.saveBatch(flavors);

    }

    //修改菜品及其对应的口味
    @Transactional
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //1. 更新dish表
        updateById(dishDto);
        //2. 删除dish_flavor表
        //构造条件
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        //根据条件删除
        dishFlavorService.remove(queryWrapper);
        //3. 新增dish_flavor表
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
            flavor.setId(null);//清除原来的id，防止出现旧口味的id重复
        }
        dishFlavorService.saveBatch(flavors);
    }

    //根据id删除菜品信息以及对应的口味信息
    @Override
    //加上事务注解，当其中一个删除失败，其余的都无法删除
    @Transactional
    public void deleteWithFlavors(Long[] ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        int count = count(queryWrapper);
        //当前有菜品还是起售状态就不能删除
        if (count > 0) {
            throw new CustomException("当前有菜品处于起售状态，无法删除");
        } else {
            //删除所有菜品信息
            for (Long id : ids) {
                removeById(id);

                //删除对应的口味信息
                LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(DishFlavor::getDishId, id);
                dishFlavorService.remove(lambdaQueryWrapper);
            }

        }
    }
}
