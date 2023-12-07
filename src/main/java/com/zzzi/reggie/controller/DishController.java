package com.zzzi.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzzi.reggie.common.R;
import com.zzzi.reggie.dto.DishDto;
import com.zzzi.reggie.entity.AddressBook;
import com.zzzi.reggie.entity.Category;
import com.zzzi.reggie.entity.Dish;
import com.zzzi.reggie.entity.DishFlavor;
import com.zzzi.reggie.service.CategoryService;
import com.zzzi.reggie.service.DishFlavorService;
import com.zzzi.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzzi
 * @date 2023/12/3 10:33
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    //新增菜品的代码
    //不仅要保存到菜品表中。还需要保存到口味表中
    //使用数据传输对象接受前端传递来的数据，包含菜品和口味
    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto) {
        log.info("构造好的菜品信息为：{}", dishDto);
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    //菜品的分页查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("接收到的分页查询条件为：page:{}，pageSize:{}，name:{}");
        Page<Dish> pageInfo = new Page(page, pageSize);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Dish::getSort);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //如果动态传递了当前的菜品名称，那么还需要将名称的筛选条件动态拼接
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);

        dishService.page(pageInfo, queryWrapper);

        /**@author zzzi
         * @date 2023/12/4 9:55
         * 开始封装一个新的分页对象，内部保存了每一个菜品对应的分类名称
         */
        //封装分类的名称，提供给前端，便于前端进行展示，直接将其封装到DishDto中
        Page<DishDto> dishDtoPage = new Page<>();
        //直接进行拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        //二次处理查询得到的数据,新增一个菜品分类的名称
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> newRecords = new ArrayList<>();
        //遍历得到的每一个菜品，将其对应的分类名称查询出来
        /**@author zzzi
         * @date 2023/12/4 9:55
         * 根据菜品中的分类id查询得到分类名称，将其保存到新的记录中
         */
        for (Dish record : records) {
            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            DishDto dishDto = new DishDto();
            //填充好dishDto
            BeanUtils.copyProperties(record, dishDto);
            //尤其是填充这个多出来的字段
            dishDto.setCategoryName(category.getName());

            newRecords.add(dishDto);
        }
        /**@author zzzi
         * @date 2023/12/4 9:54
         * 核心就是构造一个新的分页对象返回
         */
        dishDtoPage.setRecords(newRecords);
        return R.success(dishDtoPage);
    }

    //按照菜品id查询菜品
    @GetMapping("/{id}")
    public R<DishDto> getDishById(@PathVariable Long id) {
        //1. 查询菜品基本信息
        Dish dish = dishService.getById(id);
        //2. 根据菜品id查询到所有的口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());

        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //3. 封装一个结果对象返回
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(flavors);

        //将查询到的菜品信息以及菜品相关口味返回
        return R.success(dishDto);
    }

    //将修改的菜品信息及其对应的口味信息更新
    @PutMapping
    public R<String> updateDish(@RequestBody DishDto dishDto) {
        log.info("构造好的菜品信息为：{}", dishDto);
        dishService.updateWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }


    //根据传递来的菜品名称或者菜品分类id查询结果
    //查询菜品时，不仅需要查询菜品，还需要将其口味查询出来
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        log.info("查询的参数为：{}", dish);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //状态为停售的菜品就不查询了
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.like(dish.getName() != null, Dish::getName, dish.getName());
        queryWrapper.orderByDesc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        //查询到的所有菜品
        List<Dish> list = dishService.list(queryWrapper);
        List<DishDto> res = new ArrayList<>();
        //每一个菜品对应一个口味列表
        for (Dish dishList : list) {
            LambdaQueryWrapper<DishFlavor> Wrapper = new LambdaQueryWrapper<>();
            //根据当前菜品的id查询到其所有的口味
            Wrapper.eq(DishFlavor::getDishId, dishList.getId());
            List<DishFlavor> flavorList = dishFlavorService.list(Wrapper);

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dishList, dishDto);
            dishDto.setFlavors(flavorList);

            res.add(dishDto);
        }

        return R.success(res);
    }

    //针对菜品的停售或者起售
    @PostMapping("/status/{id}")
    public R<String> update(@PathVariable int id, Long[] ids) {
        log.info("接受到新的菜品信息为：{}", ids);

        //针对每一个菜品批量修改状态
        for (Long dishId : ids) {
            Dish dish = dishService.getById(dishId);
            dish.setStatus(id);

            dishService.updateById(dish);
        }

        return R.success("修改菜品信息成功！");
    }

    //根据传递而来的菜品id删除菜品信息及其口味
    @DeleteMapping
    public R<String> deleteWithFlavors(Long[] ids) {
        log.info("接收到的待删除的菜品ids:{}", ids);

        //针对传递而来的所有ids一个一个的删除
        dishService.deleteWithFlavors(ids);
        return R.success("删除成功！");
    }


}
