package com.zzzi.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzzi.reggie.common.R;
import com.zzzi.reggie.dto.SetmealDto;
import com.zzzi.reggie.entity.Category;
import com.zzzi.reggie.entity.Setmeal;
import com.zzzi.reggie.entity.SetmealDish;
import com.zzzi.reggie.service.CategoryService;
import com.zzzi.reggie.service.SetMealDishService;
import com.zzzi.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {
    @Autowired
    private SetMealService setMealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetMealDishService setMealDishService;

    //保存传递而来的套餐信息，一个套餐包含多个菜品，将这个对应关系保存到套餐菜品关系表中

    /**
     * @author zzzi
     * @date 2023/12/4 13:51
     * 保存时不仅需要保存套餐信息，还需要完善套餐菜品表中关联的套餐id，然后再进行保存
     * 也就是要操作两张表
     */
    @PostMapping
    public R<String> saveWithDish(@RequestBody SetmealDto setmealDto) {
        log.info("接收到的套餐参数为：{}", setmealDto);
        setMealService.saveWithDish(setmealDto);
        return R.success("套餐新增成功！");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("接收到的分页查询条件为：page:{},pageSize:{},name:{}", page, pageSize, name);

        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //传递了名称才进行更新
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //此时pageInfo中就保存了分页查询得到的结果
        setMealService.page(pageInfo, queryWrapper);
        /**@author zzzi
         * @date 2023/12/4 14:41
         * 重新开始构造一个新的分页查询对象
         */
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //除了records都拷贝，records需要重新构造
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> newRecords = new ArrayList<>();
        //根据每一个套餐中的分类id查询到其分类数据
        /**@author zzzi
         * @date 2023/12/4 14:41
         * 构造新的分页查询中的records，里面要包含分类名称
         * 根据旧records中的分类id查询到分类信息
         * 然后填充新records中的分类名称
         */
        for (Setmeal record : records) {
            Category category = categoryService.getById(record.getCategoryId());
            //构造一条新纪录
            SetmealDto newRecord = new SetmealDto();
            //填充所有的属性
            BeanUtils.copyProperties(record, newRecord);
            newRecord.setCategoryName(category.getName());

            //将新纪录保存到新records中
            newRecords.add(newRecord);
        }

        //填充新分页查询对象中的records
        setmealDtoPage.setRecords(newRecords);

        //返回全新构造过后的分页查询对象
        return R.success(setmealDtoPage);
    }

    //根据传递来的id先查询得到一个套餐信息，包含套餐以及内部的所有菜品信息
    //查询两个表，封装一个SetmealDto返回
    @GetMapping("/{id}")
    public R<SetmealDto> getSetmealDtoById(@PathVariable Long id) {
        log.info("接收到的套餐id为：{}", id);
        SetmealDto res = setMealService.getSetmealDtoById(id);

        return R.success(res);
    }

    //根据新修改的套餐及其包含的菜品更新两个表
    //先删除旧的，然后插入新的
    @PutMapping
    public R<String> updateWithFlavors(@RequestBody SetmealDto setmealDto) {
        log.info("新的套餐信息为:{}", setmealDto);

        setMealService.updateWithFlavors(setmealDto);
        return R.success("套餐信息修改成功");
    }

    //对套餐进行停售或者起售
    @PostMapping("/status/{id}")
    public R<String> updateStatus(@PathVariable int id, Long[] ids) {
        log.info("接收到的id为:{}", ids);

        //根据传递而来的id拿到所有的套餐，针对每一个套餐都更新状态
        for (Long setMealId : ids) {
            Setmeal setmeal = setMealService.getById(setMealId);
            setmeal.setStatus(id);
            setMealService.updateById(setmeal);
        }
        return R.success("修改状态成功");
    }

    //根据id删除套餐，还需要在套餐菜品关系表中删除对应的套餐中的菜品

    /**
     * @author zzzi
     * @date 2023/12/4 16:24
     * 为了事务的统一，批量删除最好传递一个数组
     */
    @DeleteMapping
    public R<String> deleteWithDish(Long[] ids) {
        log.info("接收到的待删除的套餐id为:{}", ids);

        //删除所有的套餐
        setMealService.deleteWithDish(ids);
        return R.success("删除套餐成功！");
    }


    //查询当前分类下有多少套餐
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        log.info("前端传递来的请求为：{}", setmeal);

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //只要分类id是这个的全部查询出来
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //查询出一批数据
        List<Setmeal> list = setMealService.list(queryWrapper);
        if (list != null)
            return R.success(list);
        return R.error("未查询到相关数据");
    }

    //@GetMapping("/dish/{id}")
    //public R<SetmealDto> dish(@PathVariable Long id) {
    //    log.info("传递来的id为：{}", id);
    //
    //    //根据传递来的id将套餐下的所有菜品查询到
    //    LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
    //    queryWrapper.eq(SetmealDish::getSetmealId, id);
    //    List<SetmealDish> list = setMealDishService.list(queryWrapper);
    //
    //    Setmeal setmeal = setMealService.getById(id);
    //
    //    SetmealDto setmealDto = new SetmealDto();
    //    BeanUtils.copyProperties(setmeal, setmealDto);
    //    setmealDto.setSetmealDishes(list);
    //
    //    return R.success(setmealDto);
    //}

}
